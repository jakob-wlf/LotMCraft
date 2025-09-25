package de.jakob.lotm.util.helper.skin;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.jakob.lotm.network.PacketHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinManager {
    private static final Map<String, GameProfile> skinCache = new ConcurrentHashMap<>();
    private static final Map<String, Property> originalSkins = new ConcurrentHashMap<>();
    private static final Map<String, String> playerSkinOverrides = new ConcurrentHashMap<>();

    public static void changeSkin(ServerPlayer player, String targetPlayerName, String newSkinPlayerName) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        // Check permissions
        if (!hasPermission(player, "skinchanger.change")) {
            player.sendSystemMessage(Component.literal("You don't have permission to change skins"));
            return;
        }

        // Get the target player
        ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            player.sendSystemMessage(Component.literal("Target player not found"));
            return;
        }

        // Store original skin if not already stored
        String playerId = targetPlayer.getUUID().toString();
        if (!originalSkins.containsKey(playerId)) {
            Property originalSkin = Iterables.getFirst(
                    targetPlayer.getGameProfile().getProperties().get("textures"), null);
            if (originalSkin != null) {
                originalSkins.put(playerId, originalSkin);
            }
        }

        player.sendSystemMessage(Component.literal("Fetching skin data for " + newSkinPlayerName + "..."));

        // Fetch skin data for the new skin
        fetchAndApplySkin(targetPlayer, newSkinPlayerName);
    }

    private static void fetchAndApplySkin(ServerPlayer targetPlayer, String newSkinPlayerName) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return fetchSkinData(newSkinPlayerName);
            } catch (Exception e) {
                System.out.println("Error fetching skin data: " + e.getMessage());
                return null;
            }
        }).thenAccept(gameProfile -> {
            if (gameProfile != null && targetPlayer.isAlive()) {
                applySkinToPlayer(targetPlayer, gameProfile, newSkinPlayerName);
            } else {
                targetPlayer.sendSystemMessage(Component.literal("Failed to fetch skin for " + newSkinPlayerName));
            }
        });
    }

    private static GameProfile fetchSkinData(String playerName) throws Exception {
        // Check cache first
        String cacheKey = playerName.toLowerCase();
        if (skinCache.containsKey(cacheKey)) {
            return skinCache.get(cacheKey);
        }

        // Create HTTP request to get UUID
        String uuidUrl = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uuidUrl))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalArgumentException("Player not found: " + playerName);
        }

        // Parse UUID response
        JsonObject uuidJson = JsonParser.parseString(response.body()).getAsJsonObject();
        String uuid = uuidJson.get("id").getAsString();
        String name = uuidJson.get("name").getAsString();

        // Format UUID properly
        String formattedUuid = uuid.replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        );

        // Get skin data
        String skinUrl = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
        HttpRequest skinRequest = HttpRequest.newBuilder()
                .uri(URI.create(skinUrl))
                .build();

        HttpResponse<String> skinResponse = client.send(skinRequest, HttpResponse.BodyHandlers.ofString());

        if (skinResponse.statusCode() != 200) {
            throw new IllegalArgumentException("Failed to fetch skin data for: " + playerName);
        }

        // Parse skin response
        JsonObject skinJson = JsonParser.parseString(skinResponse.body()).getAsJsonObject();
        GameProfile gameProfile = new GameProfile(UUID.fromString(formattedUuid), name);

        if (skinJson.has("properties")) {
            JsonArray properties = skinJson.getAsJsonArray("properties");
            for (JsonElement element : properties) {
                JsonObject property = element.getAsJsonObject();
                String propName = property.get("name").getAsString();
                String value = property.get("value").getAsString();
                String signature = property.has("signature") ? property.get("signature").getAsString() : null;

                gameProfile.getProperties().put(propName, new Property(propName, value, signature));
            }
        }

        // Cache the result
        skinCache.put(cacheKey, gameProfile);
        return gameProfile;
    }

    private static void applySkinToPlayer(ServerPlayer player, GameProfile skinProfile, String newSkinPlayerName) {
        Property skinProperty = Iterables.getFirst(skinProfile.getProperties().get("textures"), null);
        if (skinProperty != null) {
            // Update the player's game profile with new skin data
            player.getGameProfile().getProperties().removeAll("textures");
            player.getGameProfile().getProperties().put("textures", skinProperty);

            // Track the skin override
            playerSkinOverrides.put(player.getUUID().toString(), newSkinPlayerName);

            // Sync to all clients
            PacketHandler.syncSkinDataToAllPlayers(
                    player.getName().getString(),
                    skinProperty.value(),
                    skinProperty.signature() != null ? skinProperty.signature() : ""
            );

            // Force player list update
            refreshPlayerForClients(player);

            player.sendSystemMessage(Component.literal("Skin changed to " + newSkinPlayerName));
        }
    }

    private static void refreshPlayerForClients(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        // Send to all players including the target player
        for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
            // Remove and re-add player to player list to force skin refresh
            ClientboundPlayerInfoRemovePacket removePacket =
                    new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID()));
            ClientboundPlayerInfoUpdatePacket addPacket =
                    new ClientboundPlayerInfoUpdatePacket(
                            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                            player
                    );

            otherPlayer.connection.send(removePacket);

            // Small delay to ensure proper processing
            server.execute(() -> {
                if (otherPlayer.connection != null) {
                    otherPlayer.connection.send(addPacket);
                }
            });
        }

        // Also force respawn packets to ensure skin is properly updated
        server.execute(() -> {
            if (player.connection != null) {
                // Force a position update to refresh the player's appearance
                player.connection.send(new ClientboundSetEntityDataPacket(
                        player.getId(),
                        player.getEntityData().getNonDefaultValues()
                ));
            }
        });
    }

    private static boolean hasPermission(ServerPlayer player, String permission) {
        // Simple permission check - integrate with permission system if needed
        return player.hasPermissions(2); // Op level 2
    }

    public static void restoreOriginalSkin(ServerPlayer player) {
        String playerId = player.getUUID().toString();

        if (!playerSkinOverrides.containsKey(playerId)) {
            player.sendSystemMessage(Component.literal("No skin override to restore"));
            return;
        }

        Property originalSkin = originalSkins.get(playerId);
        if (originalSkin != null) {
            // Restore original skin
            player.getGameProfile().getProperties().removeAll("textures");
            player.getGameProfile().getProperties().put("textures", originalSkin);

            // Remove tracking
            playerSkinOverrides.remove(playerId);
            originalSkins.remove(playerId);

            // Sync to all clients
            PacketHandler.syncSkinDataToAllPlayers(
                    player.getName().getString(),
                    originalSkin.value(),
                    originalSkin.signature() != null ? originalSkin.signature() : ""
            );

            // Force refresh
            refreshPlayerForClients(player);

            player.sendSystemMessage(Component.literal("Original skin restored"));
        } else {
            player.sendSystemMessage(Component.literal("No original skin data found"));
        }
    }

    public static boolean hasSkinOverride(ServerPlayer player) {
        return playerSkinOverrides.containsKey(player.getUUID().toString());
    }

    public static String getCurrentSkinOverride(ServerPlayer player) {
        return playerSkinOverrides.get(player.getUUID().toString());
    }
}
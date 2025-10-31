package de.jakob.lotm.util.helper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SkinChanger {

    /**
     * Changes a player's skin to match another player's skin
     * @param player The player whose skin should be changed
     * @param targetUsername The username of the player whose skin to copy
     * @return CompletableFuture<Boolean> - true if successful, false if failed
     */
    public static CompletableFuture<Boolean> changePlayerSkin(ServerPlayer player, String targetUsername) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get the game profile cache
                GameProfileCache profileCache = ServerLifecycleHooks.getCurrentServer().getProfileCache();

                // Look up the target player's profile
                Optional<GameProfile> targetProfileOpt = profileCache.get(targetUsername);

                if (targetProfileOpt.isEmpty()) {
                    // Try to fetch profile by looking up existing online players or using UUID lookup
                    try {
                        // First try to find if the player is currently online
                        ServerPlayer onlinePlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(targetUsername);
                        if (onlinePlayer != null) {
                            GameProfile onlineProfile = onlinePlayer.getGameProfile();
                            if (onlineProfile.getProperties().containsKey("textures")) {
                                targetProfileOpt = Optional.of(onlineProfile);
                            }
                        }

                        // If still not found and we have no other way, default to Steve
                        if (targetProfileOpt.isEmpty()) {
                            clearPlayerSkin(player);
                            return true;
                        }
                    } catch (Exception e) {
                        // If we can't fetch the profile, default to Steve skin
                        clearPlayerSkin(player);
                        return true;
                    }
                }

                GameProfile targetProfile = targetProfileOpt.get();

                // Get the skin properties from target profile
                Property textureProperty = targetProfile.getProperties().get("textures").iterator().next();

                if (textureProperty == null) {
                    // No skin data found, default to Steve
                    clearPlayerSkin(player);
                    return true;
                }

                // Apply the skin to the player
                applySkinToPlayer(player, textureProperty);
                return true;

            } catch (Exception e) {
                // On any error, default to Steve skin
                clearPlayerSkin(player);
                return false;
            }
        });
    }

    /**
     * Applies a skin texture property to a player
     * @param player The player to apply the skin to
     * @param textureProperty The texture property containing skin data
     */
    private static void applySkinToPlayer(ServerPlayer player, Property textureProperty) {
        try {
            // Clear existing texture properties
            player.getGameProfile().getProperties().removeAll("textures");

            // Add the new texture property
            player.getGameProfile().getProperties().put("textures", textureProperty);

            // Update the player's appearance for all clients
            updatePlayerAppearance(player);

        } catch (Exception e) {
            throw e; // Re-throw to be caught by the main function
        }
    }

    /**
     * Clears a player's skin, reverting to Steve skin
     * @param player The player whose skin to clear
     */
    private static void clearPlayerSkin(ServerPlayer player) {
        // Remove all texture properties to default to Steve skin
        player.getGameProfile().getProperties().removeAll("textures");

        // Update the player's appearance for all clients
        updatePlayerAppearance(player);
    }

    /**
     * Updates the player's appearance for all clients in the server
     * @param player The player whose appearance to update
     */
    private static void updatePlayerAppearance(ServerPlayer player) {
        try {
            System.out.println("Updating player appearance for: " + player.getName().getString());

            // Simple approach - just refresh the player in the tab list
            // This should trigger clients to re-read the skin data
            var playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();

            // Create update packet with the player's current data
            ClientboundPlayerInfoUpdatePacket updatePacket = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                    java.util.List.of(player));

            System.out.println("Broadcasting update packet...");
            playerList.broadcastAll(updatePacket);

            System.out.println("Player appearance update completed");

        } catch (Exception e) {
            System.out.println("Error in updatePlayerAppearance: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be caught by the main function
        }
    }

    /**
     * Simple synchronous test version
     */
    public static boolean changePlayerSkinTest(ServerPlayer player, String targetUsername) {
        System.out.println("TEST: Starting synchronous skin change");
        try {
            clearPlayerSkin(player);
            System.out.println("TEST: Cleared skin successfully");
            return true;
        } catch (Exception e) {
            System.out.println("TEST: Exception occurred: " + e.getMessage());
            return false;
        }
    }

    /**
     * Simple synchronous version that should work
     */
    public static boolean changePlayerSkinSimple(ServerPlayer player, String targetUsername) {
        try {
            System.out.println("SIMPLE: Starting simple skin change for: " + targetUsername);

            if (targetUsername == null || targetUsername.trim().isEmpty()) {
                System.out.println("SIMPLE: Target username is null or empty");
                return false;
            }

            // Get the game profile cache
            GameProfileCache profileCache = ServerLifecycleHooks.getCurrentServer().getProfileCache();
            Optional<GameProfile> targetProfileOpt = profileCache.get(targetUsername);
            System.out.println("SIMPLE: Profile cache lookup result: " + (targetProfileOpt.isPresent() ? "FOUND" : "NOT FOUND"));

            if (targetProfileOpt.isPresent()) {
                GameProfile cachedProfile = targetProfileOpt.get();
                System.out.println("SIMPLE: Cached profile properties: " + cachedProfile.getProperties().keySet());
                if (cachedProfile.getProperties().containsKey("textures")) {
                    System.out.println("SIMPLE: Profile has texture data");
                    Property textureProperty = cachedProfile.getProperties().get("textures").iterator().next();
                    applySkinToPlayer(player, textureProperty);
                    System.out.println("SIMPLE: Successfully applied skin - returning true");
                    return true;
                } else {
                    System.out.println("SIMPLE: Profile found but no texture data, will try to fetch");
                }
            }

            // Try online players
            ServerPlayer onlinePlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(targetUsername);
            System.out.println("SIMPLE: Online player lookup result: " + (onlinePlayer != null ? "FOUND" : "NOT FOUND"));

            if (onlinePlayer != null) {
                System.out.println("SIMPLE: Online player profile properties: " + onlinePlayer.getGameProfile().getProperties().keySet());
                if (onlinePlayer.getGameProfile().getProperties().containsKey("textures")) {
                    System.out.println("SIMPLE: Online player has texture data");
                    Property textureProperty = onlinePlayer.getGameProfile().getProperties().get("textures").iterator().next();
                    applySkinToPlayer(player, textureProperty);
                    System.out.println("SIMPLE: Successfully applied online player skin - returning true");
                    return true;
                } else {
                    System.out.println("SIMPLE: Online player found but no texture data");
                }
            }

            // If we have a cached profile, try to fill it with properties
            if (targetProfileOpt.isPresent()) {
                try {
                    System.out.println("SIMPLE: Attempting to fill profile properties from session service");
                    GameProfile profileToFill = targetProfileOpt.get();

                    // Try to get texture properties using the session service
                    var sessionService = ServerLifecycleHooks.getCurrentServer().getSessionService();
                    var profileResult = sessionService.fetchProfile(profileToFill.getId(), true);

                    if (profileResult != null && profileResult.profile() != null) {
                        GameProfile filledProfile = profileResult.profile();
                        System.out.println("SIMPLE: Filled profile properties: " + filledProfile.getProperties().keySet());

                        if (filledProfile.getProperties().containsKey("textures")) {
                            System.out.println("SIMPLE: Filled profile has texture data");
                            Property textureProperty = filledProfile.getProperties().get("textures").iterator().next();
                            applySkinToPlayer(player, textureProperty);
                            System.out.println("SIMPLE: Successfully applied filled profile skin - returning true");
                            return true;
                        }
                    }
                } catch (Exception fetchException) {
                    System.out.println("SIMPLE: Failed to fetch profile properties: " + fetchException.getMessage());
                    // Continue to default Steve skin
                }
            }

            // Default to Steve
            System.out.println("SIMPLE: Defaulting to Steve skin");
            clearPlayerSkin(player);
            System.out.println("SIMPLE: Successfully cleared skin - returning true");
            return true;

        } catch (Exception e) {
            System.out.println("SIMPLE: Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Simple example usage with chat debug messages
     */
    public static void exampleUsageWithDebug(ServerPlayer player, String targetUsername) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Starting skin change for: " + targetUsername));

        try {
            if (targetUsername == null || targetUsername.trim().isEmpty()) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[DEBUG] Target username is null or empty"));
                return;
            }

            // Get the game profile cache
            GameProfileCache profileCache = ServerLifecycleHooks.getCurrentServer().getProfileCache();
            Optional<GameProfile> targetProfileOpt = profileCache.get(targetUsername);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Profile cache lookup: " + (targetProfileOpt.isPresent() ? "FOUND" : "NOT FOUND")));

            if (targetProfileOpt.isPresent()) {
                GameProfile cachedProfile = targetProfileOpt.get();
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Cached profile properties: " + cachedProfile.getProperties().keySet()));
                if (cachedProfile.getProperties().containsKey("textures")) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[DEBUG] Profile has texture data - applying"));
                    Property textureProperty = cachedProfile.getProperties().get("textures").iterator().next();
                    applySkinToPlayerWithDebug(player, textureProperty);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a✓ Successfully applied skin!"));
                    return;
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Profile found but no texture data, will try to fetch"));
                }
            }

            // Try online players
            ServerPlayer onlinePlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(targetUsername);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Online player lookup: " + (onlinePlayer != null ? "FOUND" : "NOT FOUND")));

            if (onlinePlayer != null) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Online player properties: " + onlinePlayer.getGameProfile().getProperties().keySet()));
                if (onlinePlayer.getGameProfile().getProperties().containsKey("textures")) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[DEBUG] Online player has texture data - applying"));
                    Property textureProperty = onlinePlayer.getGameProfile().getProperties().get("textures").iterator().next();
                    applySkinToPlayerWithDebug(player, textureProperty);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a✓ Successfully applied online player skin!"));
                    return;
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Online player found but no texture data"));
                }
            }

            // If we have a cached profile, try to fill it with properties
            if (targetProfileOpt.isPresent()) {
                try {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Attempting to fetch skin data from Mojang..."));
                    GameProfile profileToFill = targetProfileOpt.get();

                    var sessionService = ServerLifecycleHooks.getCurrentServer().getSessionService();
                    var profileResult = sessionService.fetchProfile(profileToFill.getId(), true);

                    if (profileResult != null && profileResult.profile() != null) {
                        GameProfile filledProfile = profileResult.profile();
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Filled profile properties: " + filledProfile.getProperties().keySet()));

                        if (filledProfile.getProperties().containsKey("textures")) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[DEBUG] Filled profile has texture data - applying"));
                            Property textureProperty = filledProfile.getProperties().get("textures").iterator().next();
                            applySkinToPlayerWithDebug(player, textureProperty);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a✓ Successfully applied fetched skin!"));
                            return;
                        }
                    }
                } catch (Exception fetchException) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[DEBUG] Failed to fetch profile: " + fetchException.getMessage()));
                }
            }

            // Default to Steve
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Defaulting to Steve skin"));
            clearPlayerSkinWithDebug(player);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a✓ Successfully cleared skin (Steve)"));

        } catch (Exception e) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[DEBUG] Exception: " + e.getMessage()));
        }
    }

    private static void applySkinToPlayerWithDebug(ServerPlayer player, Property textureProperty) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Applying skin texture..."));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Texture data length: " + textureProperty.value().length()));

        // Clear and apply
        player.getGameProfile().getProperties().removeAll("textures");
        player.getGameProfile().getProperties().put("textures", textureProperty);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Applied texture, current properties: " + player.getGameProfile().getProperties().keySet()));

        // Update appearance
        updatePlayerAppearanceWithDebug(player);
    }

    private static void clearPlayerSkinWithDebug(ServerPlayer player) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Clearing skin properties..."));
        player.getGameProfile().getProperties().removeAll("textures");
        updatePlayerAppearanceWithDebug(player);
    }

    private static void updatePlayerAppearanceWithDebug(ServerPlayer player) {
        try {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Updating player appearance..."));

            var playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();

            // Try multiple packet approaches
            ClientboundPlayerInfoUpdatePacket updatePacket = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                    java.util.List.of(player));

            playerList.broadcastAll(updatePacket);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Sent player info update packet"));

            // Try respawn refresh
            try {
                var connection = player.connection;

                connection.send(new net.minecraft.network.protocol.game.ClientboundRespawnPacket(
                        player.createCommonSpawnInfo(player.serverLevel()),
                        (byte) 0x03
                ));

                connection.send(new net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.getYRot(),
                        player.getXRot(),
                        java.util.Set.of(),
                        0
                ));

                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Sent respawn packets"));

            } catch (Exception respawnException) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[DEBUG] Respawn failed: " + respawnException.getMessage()));
            }

        } catch (Exception e) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[DEBUG] Update appearance failed: " + e.getMessage()));
        }
    }

    /**
     * Simple example usage with chat debug messages
     */
    public static void exampleUsageSimple(ServerPlayer player, String targetUsername) {
        System.out.println("SIMPLE EXAMPLE: Starting simple example");
        boolean success = changePlayerSkinSimple(player, targetUsername);
        System.out.println("SIMPLE EXAMPLE: Result: " + success);

        if (success) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Successfully changed skin to " + targetUsername + "'s skin!"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Failed to change skin."));
        }
    }

    /**
     * Simple synchronous version for testing
     */
    public static void exampleUsageSync(ServerPlayer player, String targetUsername) {
        System.out.println("SYNC EXAMPLE: Starting synchronous skin change");
        boolean success = changePlayerSkinTest(player, targetUsername);
        System.out.println("SYNC EXAMPLE: Result was: " + success);

        if (success) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Successfully changed skin (sync test)!"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Failed to change skin (sync test)."));
        }
    }

    /**
     * Example usage method
     */
    public static void exampleUsage(ServerPlayer player, String targetUsername) {
        System.out.println("EXAMPLE: Starting exampleUsage for " + targetUsername);

        changePlayerSkin(player, targetUsername)
                .thenAccept(success -> {
                    System.out.println("EXAMPLE: CompletableFuture resolved with: " + success);
                    if (success) {
                        System.out.println("EXAMPLE: Sending success message");
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "Successfully changed skin to " + targetUsername + "'s skin (or Steve if no custom skin found)!"));
                    } else {
                        System.out.println("EXAMPLE: Sending failure message (success was false)");
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "An error occurred while changing skin."));
                    }
                })
                .exceptionally(throwable -> {
                    System.out.println("EXAMPLE: CompletableFuture exception occurred: " + throwable.getMessage());
                    throwable.printStackTrace();
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "An error occurred while changing skin."));
                    return null;
                });

        System.out.println("EXAMPLE: exampleUsage method completed (async operation started)");
    }
}
package de.jakob.lotm.util.helper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.EnumSet;

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
            System.out.println("Applying skin to player: " + player.getName().getString());

            // Clear existing texture properties
            player.getGameProfile().getProperties().removeAll("textures");

            // Add the new texture property
            player.getGameProfile().getProperties().put("textures", textureProperty);

            System.out.println("Applied texture property, updating appearance...");

            // Update the player's appearance for all clients
            updatePlayerAppearance(player);

            System.out.println("Skin application completed - about to return from applySkinToPlayer");
        } catch (Exception e) {
            System.out.println("Error in applySkinToPlayer: " + e.getMessage());
            e.printStackTrace();
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
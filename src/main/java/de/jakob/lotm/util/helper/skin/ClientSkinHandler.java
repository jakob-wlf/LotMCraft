package de.jakob.lotm.util.helper.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ClientSkinHandler {
    private static final Map<String, GameProfile> clientSkinCache = new ConcurrentHashMap<>();

    public static void updatePlayerSkin(String playerName, String skinTexture, String skinSignature) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Find the player entity
        AbstractClientPlayer targetPlayer = null;
        for (AbstractClientPlayer clientPlayer : mc.level.players()) {
            if (clientPlayer.getName().getString().equals(playerName)) {
                targetPlayer = clientPlayer;
                break;
            }
        }

        if (targetPlayer == null) return;

        try {
            // Create new game profile with skin data
            GameProfile newProfile = new GameProfile(targetPlayer.getUUID(), playerName);
            Property skinProperty = new Property("textures", skinTexture, skinSignature.isEmpty() ? null : skinSignature);
            newProfile.getProperties().put("textures", skinProperty);

            // Store in cache
            clientSkinCache.put(playerName, newProfile);

            // Update the player's actual game profile
            targetPlayer.getGameProfile().getProperties().removeAll("textures");
            targetPlayer.getGameProfile().getProperties().put("textures", skinProperty);

            // Clear Minecraft's skin cache
            clearMinecraftSkinCache(targetPlayer);

            // Update PlayerInfo if available
            updatePlayerInfo(targetPlayer);

            System.out.println("Updated skin for player: " + playerName);

        } catch (Exception e) {
            System.out.println("Could not update skin for " + playerName);
        }
    }

    private static void clearMinecraftSkinCache(AbstractClientPlayer player) {
        try {
            Minecraft mc = Minecraft.getInstance();
            net.minecraft.client.resources.SkinManager skinManager = mc.getSkinManager();

            // Try to find and clear the skins cache
            Field[] fields = SkinManager.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(skinManager);

                if (value instanceof Map<?, ?> map) {
                    // Try to remove entries related to this player
                    map.remove(player.getGameProfile());
                    map.remove(player.getUUID());

                    // Remove by name if keys are strings
                    map.remove(player.getName().getString());

                    // Remove entries where the key contains the player's profile
                    try {
                        map.entrySet().removeIf(entry -> {
                            Object key = entry.getKey();
                            if (key instanceof GameProfile profile) {
                                return player.getUUID().equals(profile.getId());
                            }
                            return false;
                        });
                    } catch (Exception ignored) {
                        // Ignore concurrent modification or other exceptions
                    }
                }
            }

            System.out.println("Cleared skin cache for player: " + player.getName().getString());
        } catch (Exception e) {
            System.out.println("Could not clear Minecraft skin cache for " + player.getName().getString());
        }
    }

    private static void updatePlayerInfo(AbstractClientPlayer player) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
                PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(player.getUUID());
                if (playerInfo != null) {
                    // Try to update the PlayerInfo's profile
                    Field[] fields = PlayerInfo.class.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getType() == GameProfile.class) {
                            field.setAccessible(true);
                            field.set(playerInfo, player.getGameProfile());
                            System.out.println("Updated PlayerInfo for player: " + player.getName().getString());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not update PlayerInfo for " + player.getName().getString());
        }
    }

    public static void clearPlayerSkin(String playerName) {
        clientSkinCache.remove(playerName);

        // Find and clear the player from Minecraft's caches
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                for (AbstractClientPlayer player : mc.level.players()) {
                    if (player.getName().getString().equals(playerName)) {
                        clearMinecraftSkinCache(player);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not clear player skin cache for " + playerName);
        }
    }

}
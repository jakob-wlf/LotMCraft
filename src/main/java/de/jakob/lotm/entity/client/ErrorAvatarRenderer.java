package de.jakob.lotm.entity.client;

import com.mojang.authlib.GameProfile;
import de.jakob.lotm.entity.custom.ErrorAvatarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ErrorAvatarRenderer extends MobRenderer<ErrorAvatarEntity, PlayerModel<ErrorAvatarEntity>> {
    // Cache for player skins to avoid repeated lookups
    private static final Map<UUID, ResourceLocation> SKIN_CACHE = new ConcurrentHashMap<>();

    public ErrorAvatarRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(ErrorAvatarEntity entity) {
        UUID ownerUUID = entity.getOriginalOwner();

        // If no owner, use fallback
        if (ownerUUID == null) {
            return entity.getSkinTexture();
        }

        // Check cache first
        if (SKIN_CACHE.containsKey(ownerUUID)) {
            return SKIN_CACHE.get(ownerUUID);
        }

        // Try to get the player's skin
        ResourceLocation playerSkin = getPlayerSkin(ownerUUID);

        // Cache and return the result (either player skin or fallback)
        SKIN_CACHE.put(ownerUUID, playerSkin);
        return playerSkin;
    }

    /**
     * Attempts to retrieve the player's skin texture
     * @param playerUUID The UUID of the player
     * @return The ResourceLocation of the player's skin, or fallback texture
     */
    private ResourceLocation getPlayerSkin(UUID playerUUID) {
        Minecraft mc = Minecraft.getInstance();

        // Method 1: Try to get the player from the current level
        if (mc.level != null) {
            Player player = mc.level.getPlayerByUUID(playerUUID);
            if (player != null) {
                PlayerSkin skin = mc.getSkinManager().getInsecureSkin(player.getGameProfile());
                return skin.texture();
            }
        }

        // Method 2: Try to get from player info (for online players in multiplayer)
        if (mc.getConnection() != null) {
            PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(playerUUID);
            if (playerInfo != null) {
                return playerInfo.getSkin().texture();
            }
        }

        // Method 3: Try to load skin asynchronously from profile
        try {
            GameProfile profile = new GameProfile(playerUUID, null);
            PlayerSkin skin = mc.getSkinManager().getInsecureSkin(profile);
            return skin.texture();
        } catch (Exception e) {
            // If anything goes wrong, fall through to default
        }

        // Method 4: Use default Steve/Alex skin based on UUID
        PlayerSkin defaultSkin = DefaultPlayerSkin.get(playerUUID);
        return defaultSkin.texture();
    }

    /**
     * Clears the skin cache. Can be called when needed (e.g., on resource reload)
     */
    public static void clearSkinCache() {
        SKIN_CACHE.clear();
    }
}
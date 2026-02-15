package de.jakob.lotm.util.shapeShifting;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSkinData {
    private static final Map<UUID, ResourceLocation> SKIN_TEXTURES = new ConcurrentHashMap<>();
    private static final Map<UUID, PlayerSkin.Model> SKIN_MODELS = new ConcurrentHashMap<>();

    public static void fetchAndCacheSkin(UUID playerId, GameProfile profile) {
        SkinManager skinManager = Minecraft.getInstance().getSkinManager();

        skinManager.getOrLoad(profile).thenAccept(skin -> {
            ResourceLocation texture = skin.texture();
            PlayerSkin.Model model = skin.model();
            SKIN_TEXTURES.put(playerId, texture);
            SKIN_MODELS.put(playerId, model);
        }).exceptionally(throwable -> {
            throwable.printStackTrace();

            PlayerSkin defaultSkin = skinManager.getInsecureSkin(profile);
            SKIN_TEXTURES.put(playerId, defaultSkin.texture());
            SKIN_MODELS.put(playerId, defaultSkin.model());
            return null;
        });
    }

    public static ResourceLocation getSkinTexture(UUID playerId) {
        ResourceLocation texture = SKIN_TEXTURES.get(playerId);
        return texture;
    }

    // optional but i wanted to do it
    public static boolean isSlimModel(UUID playerId) {
        PlayerSkin.Model model = SKIN_MODELS.get(playerId);
        boolean slim = model == PlayerSkin.Model.SLIM;
        return slim;
    }
}
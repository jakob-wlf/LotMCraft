package de.jakob.lotm.util.shapeShifting;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSkinData {
    private static final Map<UUID, ResourceLocation> SKIN_TEXTURES = new ConcurrentHashMap<>();
    private static final Map<UUID, PlayerSkin.Model> SKIN_MODELS = new ConcurrentHashMap<>();

    public static void fetchAndCacheSkin(UUID playerId) {
        Minecraft mc = Minecraft.getInstance();

        // run in background thread to not lag the server
        CompletableFuture.runAsync(() -> {
            // get server profile from players UUID
            var sessionService = mc.getMinecraftSessionService();
            var profileResult = sessionService.fetchProfile(playerId, false);

            if (profileResult == null) return;
            GameProfile resolvedProfile = profileResult.profile();

            // I think this should happen on main thread
            // get the skins and map them
            mc.execute(() -> {
                SkinManager skinManager = mc.getSkinManager();

                skinManager.getOrLoad(resolvedProfile).thenAccept(skin -> {
                    ResourceLocation texture = skin.texture();
                    PlayerSkin.Model model = skin.model();


                    SKIN_TEXTURES.put(playerId, texture);
                    SKIN_MODELS.put(playerId, model);
                }).exceptionally(throwable -> {

                    PlayerSkin defaultSkin = skinManager.getInsecureSkin(resolvedProfile);
                    SKIN_TEXTURES.put(playerId, defaultSkin.texture());
                    SKIN_MODELS.put(playerId, defaultSkin.model());
                    return null;
                });
            });
        });
    }

    public static ResourceLocation getSkinTexture(UUID playerId) {
        ResourceLocation texture = SKIN_TEXTURES.get(playerId);
        return texture;
    }

    public static boolean isSlimModel(UUID playerId) {
        PlayerSkin.Model model = SKIN_MODELS.get(playerId);
        boolean slim = model == PlayerSkin.Model.SLIM;
        return slim;
    }
}
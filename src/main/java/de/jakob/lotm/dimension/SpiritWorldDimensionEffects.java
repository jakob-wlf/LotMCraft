package de.jakob.lotm.dimension;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpiritWorldDimensionEffects{

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"),
                new SpiritWorldDimensionEffects.SpiritWorldEffects()
        );
    }

    public static class SpiritWorldEffects extends DimensionSpecialEffects {

        public SpiritWorldEffects() {
            super(Float.NaN, true, SkyType.END, false, false);
        }

        @Override
        public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
            // Constantly cycle through colors based on game time
            long time = System.currentTimeMillis();

            float hue = (((float) time / 50) % 360) / 360.0f; // Full color cycle every 18 seconds
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f);

            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;

            return new Vec3(r, g, b);
        }

        @Override
        public boolean isFoggyAt(int i, int i1) {
            return true;
        }

        @Override
        public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
            return super.renderClouds(level, ticks, partialTick, poseStack, camX, camY, camZ, modelViewMatrix, projectionMatrix);
        }

        @Override
        public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
            return super.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
        }

        @Override
        public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick,
                                         net.minecraft.client.renderer.LightTexture lightTexture,
                                         double camX, double camY, double camZ) {
            return false; // No weather
        }

        @Override
        public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
            return super.tickRain(level, ticks, camera);
        }

        @Override
        public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float blockLightRedFlicker, float skyLight, int pixelX, int pixelY, Vector3f colors) {
            super.adjustLightmapColors(level, partialTicks, skyDarken, blockLightRedFlicker, skyLight, pixelX, pixelY, colors);
        }
    }
}
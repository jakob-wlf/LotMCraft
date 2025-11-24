package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpiritWorldFogHandler {

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        if (camera.getEntity().level() instanceof ClientLevel level) {
            if (level.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
                // Set very close fog distance for disorienting effect
                event.setNearPlaneDistance(15.0f);
                event.setFarPlaneDistance(32.0f);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Camera camera = event.getCamera();
        if (camera.getEntity().level() instanceof ClientLevel level) {
            if (level.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
                // Multi-layered color shifting for psychedelic effect
                long time = System.currentTimeMillis();
                
                // Primary fast cycle
                float hue1 = ((time / 10) % 360) / 360.0f;
                
                // Secondary slower cycle, offset
                float hue2 = ((time / 80 + 180) % 360) / 360.0f;
                
                // Tertiary very slow cycle
                float hue3 = ((time / 150 + 90) % 360) / 360.0f;
                
                // Convert hues to RGB with full saturation
                int rgb1 = java.awt.Color.HSBtoRGB(hue1, 1.0f, 1.0f);
                int rgb2 = java.awt.Color.HSBtoRGB(hue2, 0.95f, 1.0f);
                int rgb3 = java.awt.Color.HSBtoRGB(hue3, 0.9f, 1.0f);
                
                // Extract RGB components
                float r1 = ((rgb1 >> 16) & 0xFF) / 255.0f;
                float g1 = ((rgb1 >> 8) & 0xFF) / 255.0f;
                float b1 = (rgb1 & 0xFF) / 255.0f;
                
                float r2 = ((rgb2 >> 16) & 0xFF) / 255.0f;
                float g2 = ((rgb2 >> 8) & 0xFF) / 255.0f;
                float b2 = (rgb2 & 0xFF) / 255.0f;
                
                float r3 = ((rgb3 >> 16) & 0xFF) / 255.0f;
                float g3 = ((rgb3 >> 8) & 0xFF) / 255.0f;
                float b3 = (rgb3 & 0xFF) / 255.0f;
                
                // Blend colors with pulsing weights
                float pulse = (float) Math.sin(time / 100.0) * 0.3f + 0.7f;
                float r = (r1 * 0.5f + r2 * 0.3f + r3 * 0.2f) * pulse;
                float g = (g1 * 0.5f + g2 * 0.3f + g3 * 0.2f) * pulse;
                float b = (b1 * 0.5f + b2 * 0.3f + b3 * 0.2f) * pulse;
                
                // Normalize to maintain brightness
                float max = Math.max(Math.max(r, g), b);
                if (max > 0 && max < 1.0f) {
                    float scale = 1.0f / max;
                    r *= scale;
                    g *= scale;
                    b *= scale;
                }
                
                event.setRed(r);
                event.setGreen(g);
                event.setBlue(b);
            }
        }
    }
}
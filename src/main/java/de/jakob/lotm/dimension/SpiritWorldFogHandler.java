package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpiritWorldFogHandler {
    
    private static final float[] currentColor = { 0.6f, 0.2f, 0.8f };
    
    private static SpiritWorldBiome lastBiome = null;
    
    private static long transitionStartMs = 0L;
    
    private static final long TRANSITION_MS = 3_000L;


    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        if (!(camera.getEntity().level() instanceof ClientLevel level)) return;
        if (!level.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) return;

        event.setNearPlaneDistance(50.0f);
        event.setFarPlaneDistance(80.0f);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Camera camera = event.getCamera();
        if (!(camera.getEntity().level() instanceof ClientLevel level)) return;
        if (!level.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) return;

        long now = System.currentTimeMillis();

        
        BlockPos pos = camera.getEntity().blockPosition();
        SpiritWorldBiome biome = SpiritWorldBiome.getBiomeAt(pos.getX(), pos.getZ());

        
        if (biome != lastBiome) {
            lastBiome = biome;
            transitionStartMs = now;
        }

        
        float[] target = biome.getFogColor(now);

        
        float maxT = Math.max(target[0], Math.max(target[1], target[2]));
        if (maxT > 0 && maxT < 1.0f) {
            target[0] /= maxT;
            target[1] /= maxT;
            target[2] /= maxT;
        }
        float rawT      = Math.min(1.0f, (now - transitionStartMs) / (float) TRANSITION_MS);
        float blendFactor = smoothstep(rawT);

        currentColor[0] = lerp(currentColor[0], target[0], blendFactor);
        currentColor[1] = lerp(currentColor[1], target[1], blendFactor);
        currentColor[2] = lerp(currentColor[2], target[2], blendFactor);

        event.setRed(currentColor[0]);
        event.setGreen(currentColor[1]);
        event.setBlue(currentColor[2]);
    }
    private static float lerp(float a, float b, float t)   { return a + t * (b - a); }
    private static float smoothstep(float x)               { return x * x * (3 - 2 * x); }
}
package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Client-side fog handler for the Spirit World dimension.
 *
 * Each biome has a distinct fog colour theme (see {@link SpiritWorldBiome#getFogColor}):
 *
 *   WOOL_MEADOWS      – fast full-spectrum rainbow cycling
 *   CRYSTALLINE_PEAKS – slow blue → cyan → purple drift
 *   VOID_GARDENS      – deep purple slow pulse
 *   EMBER_WASTES      – red/orange fast fire-flicker
 *   QUARTZ_FLATS      – warm soft gold, barely animated
 *   TERRACOTTA_CANYON – amber/sunset slow pulse
 *
 * When the player crosses a biome boundary, the fog colour blends smoothly over
 * {@value #TRANSITION_MS} milliseconds using a smoothstep curve so the change
 * never looks like an abrupt snap.
 *
 * Fog distances are kept intentionally close (near=13, far=32) to maintain
 * the disorienting, otherworldly feel of the dimension regardless of biome.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpiritWorldFogHandler {

    // -------------------------------------------------------------------------
    // Transition state  (static – lives for the duration of the game session)
    // -------------------------------------------------------------------------

    /** Displayed fog colour, updated each frame by blending toward the target. */
    private static final float[] currentColor = { 0.6f, 0.2f, 0.8f }; // start: purple

    /** The biome the player was last seen in. Used to detect boundary crossings. */
    private static SpiritWorldBiome lastBiome = null;

    /** System-time timestamp (ms) when the current biome transition started. */
    private static long transitionStartMs = 0L;

    /** Duration of a full biome-to-biome colour blend in milliseconds. */
    private static final long TRANSITION_MS = 3_000L;

    // -------------------------------------------------------------------------
    // Fog distance
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        if (!(camera.getEntity().level() instanceof ClientLevel level)) return;
        if (!level.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) return;

        event.setNearPlaneDistance(30.0f);
        event.setFarPlaneDistance(72.0f);
        event.setCanceled(true);
    }

    // -------------------------------------------------------------------------
    // Fog colour
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Camera camera = event.getCamera();
        if (!(camera.getEntity().level() instanceof ClientLevel level)) return;
        if (!level.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) return;

        long now = System.currentTimeMillis();

        // Determine which biome the camera is currently in
        BlockPos pos = camera.getEntity().blockPosition();
        SpiritWorldBiome biome = SpiritWorldBiome.getBiomeAt(pos.getX(), pos.getZ());

        // Start a new blend whenever the biome changes
        if (biome != lastBiome) {
            lastBiome = biome;
            transitionStartMs = now;
        }

        // Compute the biome's current cycling fog colour
        float[] target = biome.getFogColor(now);

        // Normalise target brightness to 1.0 so dimly-lit colours don't darken the fog
        float maxT = Math.max(target[0], Math.max(target[1], target[2]));
        if (maxT > 0 && maxT < 1.0f) {
            target[0] /= maxT;
            target[1] /= maxT;
            target[2] /= maxT;
        }

        // Smoothstep blend factor: 0 at transition start → 1 after TRANSITION_MS
        float rawT      = Math.min(1.0f, (now - transitionStartMs) / (float) TRANSITION_MS);
        float blendFactor = smoothstep(rawT);

        // Blend current colour toward target
        currentColor[0] = lerp(currentColor[0], target[0], blendFactor);
        currentColor[1] = lerp(currentColor[1], target[1], blendFactor);
        currentColor[2] = lerp(currentColor[2], target[2], blendFactor);

        event.setRed(currentColor[0]);
        event.setGreen(currentColor[1]);
        event.setBlue(currentColor[2]);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static float lerp(float a, float b, float t)   { return a + t * (b - a); }
    private static float smoothstep(float x)               { return x * x * (3 - 2 * x); }
}
package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.rendering.effectRendering.ActiveMovableEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A vertical sky beam shooting straight up from the player during the
 * Sefirah Castle accommodation ritual.
 *
 * <p>Rendered as two concentric cylinders (outer glow + core) with additive
 * blending, plus camera-aligned billboard sparkles that orbit and rise along
 * the beam. Color smooth-cycles between Fool (purple), Error (blue), and
 * Door (cyan) every 6 seconds.
 */
public class SefirahSkyBeamEffect extends ActiveMovableEffect {

    // ── Pathway colors: fool=purple, error=blue, door=cyan ───────────────────
    private static final float[][] PATH_COLORS = {
            {0x86 / 255f, 0x4e / 255f, 0xc7 / 255f}, // fool
            {0x00 / 255f, 0x18 / 255f, 0xb8 / 255f}, // error
            {0x89 / 255f, 0xf5 / 255f, 0xf5 / 255f}, // door
    };

    /** How many blocks tall the beam is. */
    private static final float BEAM_HEIGHT   = 300f;
    /** Number of cylinder facets (kept low for performance). */
    private static final int   SEGMENTS      = 8;
    /**
     * Client ticks for a complete 3-color cycle.
     * 120 t ≈ 6 s at 20 TPS (but this runs on client render ticks).
     */
    private static final float COLOR_CYCLE   = 120f;

    // ── Orbital sparkles ──────────────────────────────────────────────────────
    private static final int SPARKLE_COUNT = 40;
    private final float[] spAngle  = new float[SPARKLE_COUNT];
    private final float[] spHeight = new float[SPARKLE_COUNT];
    private final float[] spRadius = new float[SPARKLE_COUNT];
    private final float[] spSpeed  = new float[SPARKLE_COUNT];
    private final float[] spPhase  = new float[SPARKLE_COUNT];
    private final float[] spSize   = new float[SPARKLE_COUNT];

    // ─────────────────────────────────────────────────────────────────────────

    public SefirahSkyBeamEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);

        RandomSource rng = RandomSource.create();
        for (int i = 0; i < SPARKLE_COUNT; i++) {
            spAngle[i]  = rng.nextFloat() * Mth.TWO_PI;
            // Concentrate sparkles in the lower half for a grounded look
            spHeight[i] = rng.nextFloat() * BEAM_HEIGHT * 0.5f;
            spRadius[i] = 0.6f + rng.nextFloat() * 2.0f;
            spSpeed[i]  = (rng.nextFloat() > 0.5f ? 1f : -1f) * (0.008f + rng.nextFloat() * 0.02f);
            spPhase[i]  = rng.nextFloat() * Mth.TWO_PI;
            spSize[i]   = 0.07f + rng.nextFloat() * 0.13f;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main render entry
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void render(PoseStack poseStack, float tick) {
        if (Minecraft.getInstance().level == null) return;

        poseStack.pushPose();
        poseStack.translate(location.getPosition().x,
                            location.getPosition().y,
                            location.getPosition().z);
        Matrix4f m = poseStack.last().pose();

        // ── Smooth 3-color cycle (smooth-step per segment) ────────────────────
        float phase = (tick % COLOR_CYCLE) / COLOR_CYCLE * 3f;
        int   fi    = (int) phase % 3;
        int   ti    = (fi + 1) % 3;
        float t     = phase - (int) phase;
        t = t * t * (3f - 2f * t); // smooth-step
        float cr = PATH_COLORS[fi][0] + (PATH_COLORS[ti][0] - PATH_COLORS[fi][0]) * t;
        float cg = PATH_COLORS[fi][1] + (PATH_COLORS[ti][1] - PATH_COLORS[fi][1]) * t;
        float cb = PATH_COLORS[fi][2] + (PATH_COLORS[ti][2] - PATH_COLORS[fi][2]) * t;

        // ── Camera vectors for billboard sparkles ─────────────────────────────
        Quaternionf camRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        Vector3f right = new Vector3f(1f, 0f, 0f).rotate(camRot);
        Vector3f up    = new Vector3f(0f, 1f, 0f).rotate(camRot);

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        // Disable fog so the beam is visible through render-distance fog
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);

        // Dark transparent edge halo — standard blend so it darkens the
        // surroundings, giving the beam a shadowed black border.
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                               GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        renderCylinder(m, tick, 2.8f, 0f, 0f, 0f, 0.38f);

        // Additive blend — beam glows and blooms
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                               GlStateManager.DestFactor.ONE);

        // Outer glow cylinder (wide, faint)
        renderCylinder(m, tick, 1.8f, cr, cg, cb, 0.18f);
        // Core beam (tight, bright)
        renderCylinder(m, tick, 0.50f, cr, cg, cb, 0.65f);
        // Orbital sparkles
        renderSparkles(m, tick, right, up, cr, cg, cb);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    /**
     * Returns the current interpolated RGB color [r, g, b] used by the beam.
     * Called by VFXRenderer to drive the global fog tint visible to all players.
     */
    public float[] computeColor() {
        float phase = (currentTick % COLOR_CYCLE) / COLOR_CYCLE * 3f;
        int fi = (int) phase % 3;
        int ti = (fi + 1) % 3;
        float t = phase - (int) phase;
        t = t * t * (3f - 2f * t); // smooth-step
        return new float[]{
                PATH_COLORS[fi][0] + (PATH_COLORS[ti][0] - PATH_COLORS[fi][0]) * t,
                PATH_COLORS[fi][1] + (PATH_COLORS[ti][1] - PATH_COLORS[fi][1]) * t,
                PATH_COLORS[fi][2] + (PATH_COLORS[ti][2] - PATH_COLORS[fi][2]) * t
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Geometry helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders a vertical cylinder as quad strips: bottom row is bright,
     * top row fades to nearly transparent so the beam tapers into the sky.
     */
    private static void renderCylinder(Matrix4f m, float tick, float radius,
                                       float r, float g, float b, float alpha) {
        float rotation = tick * 0.005f;
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < SEGMENTS; i++) {
            float a1 = (float) (i       * Math.PI * 2 / SEGMENTS) + rotation;
            float a2 = (float) ((i + 1) * Math.PI * 2 / SEGMENTS) + rotation;
            float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;
            float x2 = Mth.cos(a2) * radius, z2 = Mth.sin(a2) * radius;

            float pulse = 0.85f + 0.15f * Mth.sin(tick * 0.07f + i * 0.8f);
            float bot   = alpha * pulse;
            float top   = alpha * pulse * 0.03f; // almost invisible at sky height

            // Bottom (player base) — bright
            buf.addVertex(m, x1, 0f,          z1).setColor(r, g, b, bot);
            buf.addVertex(m, x2, 0f,          z2).setColor(r, g, b, bot);
            // Top (sky) — nearly transparent
            buf.addVertex(m, x2, BEAM_HEIGHT, z2).setColor(r, g, b, top);
            buf.addVertex(m, x1, BEAM_HEIGHT, z1).setColor(r, g, b, top);
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    /**
     * Camera-aligned billboard sparkle quads that orbit and slowly rise
     * along the beam, looping back to the bottom when they reach the top.
     */
    private void renderSparkles(Matrix4f m, float tick,
                                Vector3f right, Vector3f up,
                                float r, float g, float b) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < SPARKLE_COUNT; i++) {
            float angle  = spAngle[i] + tick * spSpeed[i] * 60f;
            // Rise slowly and loop
            float ht     = (spHeight[i] + tick * 0.15f) % BEAM_HEIGHT;
            float sx     = Mth.cos(angle) * spRadius[i];
            float sz     = Mth.sin(angle) * spRadius[i];
            float sy     = ht;
            float alpha  = 0.35f + 0.65f * Mth.sin(tick * 0.05f + spPhase[i]);
            float size   = spSize[i] * (0.8f + 0.4f * Mth.sin(tick * 0.08f + spPhase[i]));

            float rx = right.x * size, ry = right.y * size, rz = right.z * size;
            float ux = up.x    * size, uy = up.y    * size, uz = up.z    * size;

            // Diamond-gradient billboard: fade at corners, bright at mid-edges
            buf.addVertex(m, sx - rx - ux, sy - ry - uy, sz - rz - uz).setColor(r, g, b, 0f);
            buf.addVertex(m, sx + rx - ux, sy + ry - uy, sz + rz - uz).setColor(r, g, b, alpha);
            buf.addVertex(m, sx + rx + ux, sy + ry + uy, sz + rz + uz).setColor(r, g, b, 0f);
            buf.addVertex(m, sx - rx + ux, sy - ry + uy, sz - rz + uz).setColor(r, g, b, alpha);
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}

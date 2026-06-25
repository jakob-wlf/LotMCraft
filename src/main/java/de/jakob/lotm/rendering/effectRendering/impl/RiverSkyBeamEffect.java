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
 * A vertical sky beam for the River of Eternal Darkness accommodation ritual.
 *
 * <p>Color smooth-cycles between Darkness (deep purple-blue), Death (dark forest
 * green) and Twilight Giant (amber-orange) every 6 seconds.
 */
public class RiverSkyBeamEffect extends ActiveMovableEffect {

    // ── Pathway colors ────────────────────────────────────────────────────────
    private static final float[][] PATH_COLORS = {
            {0x33 / 255f, 0x00 / 255f, 0xb5 / 255f}, // darkness  – deep purple-blue
            {0x33 / 255f, 0x4f / 255f, 0x23 / 255f}, // death     – dark forest green
            {0x94 / 255f, 0x4b / 255f, 0x16 / 255f}, // twilight_giant – amber-orange
    };

    /** How many blocks tall the beam is. */
    private static final float BEAM_HEIGHT = 300f;
    /** Number of cylinder facets. */
    private static final int   SEGMENTS    = 8;
    /** Client ticks for a complete 3-color cycle (~6 s at 20 TPS). */
    private static final float COLOR_CYCLE = 120f;

    // ── Orbital sparkles ──────────────────────────────────────────────────────
    private static final int SPARKLE_COUNT = 40;
    private final float[] spAngle  = new float[SPARKLE_COUNT];
    private final float[] spHeight = new float[SPARKLE_COUNT];
    private final float[] spRadius = new float[SPARKLE_COUNT];
    private final float[] spSpeed  = new float[SPARKLE_COUNT];
    private final float[] spPhase  = new float[SPARKLE_COUNT];
    private final float[] spSize   = new float[SPARKLE_COUNT];

    // ─────────────────────────────────────────────────────────────────────────

    public RiverSkyBeamEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);

        RandomSource rng = RandomSource.create();
        for (int i = 0; i < SPARKLE_COUNT; i++) {
            spAngle[i]  = rng.nextFloat() * Mth.TWO_PI;
            spHeight[i] = rng.nextFloat() * BEAM_HEIGHT * 0.5f;
            spRadius[i] = 0.6f + rng.nextFloat() * 2.0f;
            spSpeed[i]  = (rng.nextFloat() > 0.5f ? 1f : -1f) * (0.008f + rng.nextFloat() * 0.02f);
            spPhase[i]  = rng.nextFloat() * Mth.TWO_PI;
            spSize[i]   = 0.07f + rng.nextFloat() * 0.13f;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void render(PoseStack poseStack, float tick) {
        if (Minecraft.getInstance().level == null) return;

        poseStack.pushPose();
        poseStack.translate(location.getPosition().x,
                            location.getPosition().y,
                            location.getPosition().z);
        Matrix4f m = poseStack.last().pose();

        // ── Smooth 3-color cycle ──────────────────────────────────────────────
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
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);

        // Dark edge halo (standard blend)
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                               GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        renderCylinder(m, tick, 2.8f, 0f, 0f, 0f, 0.38f);

        // Additive blend for glow
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                               GlStateManager.DestFactor.ONE);
        renderCylinder(m, tick, 1.8f, cr, cg, cb, 0.18f);
        renderCylinder(m, tick, 0.50f, cr, cg, cb, 0.65f);
        renderSparkles(m, tick, right, up, cr, cg, cb);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    public float[] computeColor() {
        float phase = (currentTick % COLOR_CYCLE) / COLOR_CYCLE * 3f;
        int fi = (int) phase % 3;
        int ti = (fi + 1) % 3;
        float t = phase - (int) phase;
        t = t * t * (3f - 2f * t);
        return new float[]{
                PATH_COLORS[fi][0] + (PATH_COLORS[ti][0] - PATH_COLORS[fi][0]) * t,
                PATH_COLORS[fi][1] + (PATH_COLORS[ti][1] - PATH_COLORS[fi][1]) * t,
                PATH_COLORS[fi][2] + (PATH_COLORS[ti][2] - PATH_COLORS[fi][2]) * t
        };
    }

    // ─────────────────────────────────────────────────────────────────────────

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
            float top   = alpha * pulse * 0.03f;

            buf.addVertex(m, x1, 0f,          z1).setColor(r, g, b, bot);
            buf.addVertex(m, x2, 0f,          z2).setColor(r, g, b, bot);
            buf.addVertex(m, x2, BEAM_HEIGHT, z2).setColor(r, g, b, top);
            buf.addVertex(m, x1, BEAM_HEIGHT, z1).setColor(r, g, b, top);
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderSparkles(Matrix4f m, float tick,
                                Vector3f right, Vector3f up,
                                float r, float g, float b) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < SPARKLE_COUNT; i++) {
            float angle  = spAngle[i] + tick * spSpeed[i] * 60f;
            float ht     = (spHeight[i] + tick * 0.15f) % BEAM_HEIGHT;
            float sx     = Mth.cos(angle) * spRadius[i];
            float sz     = Mth.sin(angle) * spRadius[i];
            float sy     = ht;
            float alpha  = 0.35f + 0.65f * Mth.sin(tick * 0.05f + spPhase[i]);
            float size   = spSize[i] * (0.8f + 0.4f * Mth.sin(tick * 0.08f + spPhase[i]));

            float rx = right.x * size, ry = right.y * size, rz = right.z * size;
            float ux = up.x    * size, uy = up.y    * size, uz = up.z    * size;

            buf.addVertex(m, sx - rx - ux, sy - ry - uy, sz - rz - uz).setColor(r, g, b, 0f);
            buf.addVertex(m, sx + rx - ux, sy + ry - uy, sz + rz - uz).setColor(r, g, b, alpha);
            buf.addVertex(m, sx + rx + ux, sy + ry + uy, sz + rz + uz).setColor(r, g, b, 0f);
            buf.addVertex(m, sx - rx + ux, sy - ry + uy, sz - rz + uz).setColor(r, g, b, alpha);
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}

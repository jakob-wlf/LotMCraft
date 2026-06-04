package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class NationOfTheDeadEffect extends ActiveEffect {

    private static final int WISP_COUNT   = 180;
    private static final int TENDRIL_COUNT = 72;
    private static final int MOTE_COUNT   = 600;
    private static final int RING_SEGS    = 70;
    private static final float RADIUS     = 35f;

    private final RandomSource rng = RandomSource.create();

    private final float[] wPhi    = new float[WISP_COUNT];
    private final float[] wTheta  = new float[WISP_COUNT];
    private final float[] wSpeed  = new float[WISP_COUNT];
    private final float[] wSize   = new float[WISP_COUNT];
    private final float[] wPhase  = new float[WISP_COUNT];
    private final float[] wOrbit  = new float[WISP_COUNT];
    private final float[] wR      = new float[WISP_COUNT];
    private final float[] wG      = new float[WISP_COUNT];
    private final float[] wB      = new float[WISP_COUNT];

    private final float[] tAngle  = new float[TENDRIL_COUNT];
    private final float[] tPhase  = new float[TENDRIL_COUNT];
    private final float[] tSpeed  = new float[TENDRIL_COUNT];
    private final float[] tLen    = new float[TENDRIL_COUNT];

    private final float[] mPx     = new float[MOTE_COUNT];
    private final float[] mPy     = new float[MOTE_COUNT];
    private final float[] mPz     = new float[MOTE_COUNT];
    private final float[] mVy     = new float[MOTE_COUNT];
    private final float[] mLife   = new float[MOTE_COUNT];
    private final float[] mSize   = new float[MOTE_COUNT];
    private final float[] mPhase  = new float[MOTE_COUNT];

    private final int duration;

    public NationOfTheDeadEffect(double x, double y, double z) {
        super(x, y, z, 20 * 106);
        this.duration = 20 * 101;
        bake();
    }

    private void bake() {
        for (int i = 0; i < WISP_COUNT; i++) {
            wPhi[i]   = rng.nextFloat() * Mth.TWO_PI;
            wTheta[i] = rng.nextFloat() * Mth.TWO_PI;
            wSpeed[i] = 0.004f + rng.nextFloat() * 0.008f;
            wSize[i]  = 0.18f  + rng.nextFloat() * 0.38f;
            wPhase[i] = rng.nextFloat() * Mth.TWO_PI;
            wOrbit[i] = RADIUS * (0.85f + rng.nextFloat() * 0.30f);
            float roll = rng.nextFloat();
            if (roll < 0.15f) {
                wR[i] = 0.55f; wG[i] = 0.0f; wB[i] = 0.80f;
            } else if (roll < 0.35f) {
                wR[i] = 0.10f; wG[i] = 0.0f; wB[i] = 0.60f;
            } else {
                wR[i] = 0.02f; wG[i] = 0.0f; wB[i] = 0.20f;
            }
        }

        for (int i = 0; i < TENDRIL_COUNT; i++) {
            tAngle[i] = (Mth.TWO_PI * i) / TENDRIL_COUNT;
            tPhase[i] = rng.nextFloat() * Mth.TWO_PI;
            tSpeed[i] = 0.012f + rng.nextFloat() * 0.010f;
            tLen[i]   = 3.5f   + rng.nextFloat() * 3.0f;
        }

        for (int i = 0; i < MOTE_COUNT; i++) {
            resetMote(i, rng.nextFloat() * 360f);
            mPhase[i] = rng.nextFloat() * Mth.TWO_PI;
        }
    }

    private void resetMote(int i, float ageOffset) {
        float a   = rng.nextFloat() * Mth.TWO_PI;
        float r   = RADIUS * (0.1f + rng.nextFloat() * 0.9f);
        mPx[i]    = Mth.cos(a) * r;
        mPy[i]    = -2f + rng.nextFloat() * 4f;
        mPz[i]    = Mth.sin(a) * r;
        mVy[i]    = 0.010f + rng.nextFloat() * 0.018f;
        mLife[i]  = 40f + rng.nextFloat() * 60f;
        mSize[i]  = 0.06f + rng.nextFloat() * 0.10f;
    }

    @Override
    protected void render(PoseStack poseStack, float partialTick) {
        if (Minecraft.getInstance().level == null) return;

        float age = currentTick + partialTick;
        float fadeIn  = Mth.clamp(age / 15f, 0f, 1f);
        float fadeOut = Mth.clamp((duration - age) / 15f, 0f, 1f);
        float fade    = fadeIn * fadeOut;
        if (fade < 0.001f) return;

        Quaternionf camRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        Vector3f right = new Vector3f(1f, 0f, 0f).rotate(camRot);
        Vector3f up    = new Vector3f(0f, 1f, 0f).rotate(camRot);

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        Matrix4f m = poseStack.last().pose();

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        renderGroundRings(m, age, fade);
        renderTendrils(m, age, fade);
        renderWisps(m, age, fade, right, up);
        renderMotes(m, age, fade, right, up);

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        renderDomainSurface(m, age, fade);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private void renderGroundRings(Matrix4f m, float age, float fade) {
        for (int ring = 0; ring < 3; ring++) {
            float r        = RADIUS * (0.35f + ring * 0.32f);
            float pulse    = 0.5f + 0.5f * Mth.sin(age * 0.06f + ring * 1.3f);
            float alpha    = fade * (0.25f + pulse * 0.20f);
            float ringWidth = 0.22f + pulse * 0.15f;

            BufferBuilder buf = Tesselator.getInstance()
                    .begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= RING_SEGS; i++) {
                float a = i * Mth.TWO_PI / RING_SEGS;
                float c = Mth.cos(a), s = Mth.sin(a);
                buf.addVertex(m, c * r,             0.05f, s * r)            .setColor(0.40f, 0f, 0.80f, alpha);
                buf.addVertex(m, c * (r + ringWidth), 0.05f, s * (r + ringWidth)).setColor(0.05f, 0f, 0.15f, 0f);
            }
            BufferUploader.drawWithShader(buf.buildOrThrow());
        }

        float pulse = 0.5f + 0.5f * Mth.sin(age * 0.10f);
        float alpha = fade * (0.55f + pulse * 0.30f);
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= RING_SEGS; i++) {
            float a = i * Mth.TWO_PI / RING_SEGS;
            float c = Mth.cos(a), s = Mth.sin(a);
            buf.addVertex(m, c * (RADIUS - 0.4f), 0.05f, s * (RADIUS - 0.4f)).setColor(0.55f, 0f, 1.0f, alpha);
            buf.addVertex(m, c * (RADIUS + 0.4f), 0.05f, s * (RADIUS + 0.4f)).setColor(0.02f, 0f, 0.05f, 0f);
        }
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderTendrils(Matrix4f m, float age, float fade) {
        for (int i = 0; i < TENDRIL_COUNT; i++) {
            float baseAngle  = tAngle[i] + age * tSpeed[i];
            float heightAnim = Mth.sin(age * 0.04f + tPhase[i]);
            float len        = tLen[i] * (0.7f + 0.3f * heightAnim);
            float alpha      = fade * (0.35f + 0.20f * (0.5f + 0.5f * heightAnim));

            int steps = 20;
            BufferBuilder buf = Tesselator.getInstance()
                    .begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int s = 0; s <= steps; s++) {
                float t       = (float) s / steps;
                float curAngle = baseAngle + Mth.sin(t * Mth.PI + tPhase[i]) * 0.5f;
                float curR    = RADIUS - t * 1.5f;
                float curY    = t * len;
                float cx      = Mth.cos(curAngle) * curR;
                float cz      = Mth.sin(curAngle) * curR;
                float w       = (0.06f + (1f - t) * 0.10f);
                float fa      = alpha * (1f - t);
                buf.addVertex(m, cx - w, curY, cz).setColor(0.40f, 0f, 0.85f, fa);
                buf.addVertex(m, cx + w, curY, cz).setColor(0.05f, 0f, 0.20f, fa * 0.3f);
            }
            BufferUploader.drawWithShader(buf.buildOrThrow());
        }
    }

    private void renderWisps(Matrix4f m, float age, float fade, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < WISP_COUNT; i++) {
            float theta   = wTheta[i] + age * wSpeed[i];
            float phi     = wPhi[i]   + Mth.sin(age * wSpeed[i] * 0.7f + wPhase[i]) * 0.4f;
            float orbitR  = wOrbit[i] + Mth.sin(age * wSpeed[i] * 1.3f + wPhase[i]) * 1.2f;

            float px = orbitR * Mth.sin(phi) * Mth.cos(theta);
            float py = orbitR * Mth.cos(phi);
            float pz = orbitR * Mth.sin(phi) * Mth.sin(theta);

            float pulse = 0.5f + 0.5f * Mth.sin(age * 0.08f + wPhase[i]);
            float alpha = fade * (0.55f + pulse * 0.40f);
            float size  = wSize[i] * (0.8f + pulse * 0.4f);

            quad(buf, m, px, py, pz, size, right, up, wR[i], wG[i], wB[i], alpha);
            drawn++;
        }
        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderMotes(Matrix4f m, float age, float fade, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < MOTE_COUNT; i++) {
            float localAge = (age + mPhase[i] * (mLife[i] / Mth.TWO_PI)) % mLife[i];
            float t        = localAge / mLife[i];
            float alpha    = fade * (float) Math.sin(t * Math.PI) * 0.75f;
            if (alpha < 0.005f) continue;

            float px = mPx[i];
            float py = mPy[i] + mVy[i] * localAge;
            float pz = mPz[i];

            quad(buf, m, px, py, pz, mSize[i], right, up, 0.35f, 0f, 0.75f, alpha);
            drawn++;
        }
        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderDomainSurface(Matrix4f m, float age, float fade) {
        int latBands = 18, lonBands = 36;
        float pulse  = 0.5f + 0.5f * Mth.sin(age * 0.04f);
        float alpha  = fade * (0.65f + pulse * 0.15f);

        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int lat = 0; lat < latBands; lat++) {
            float phi0 = Mth.PI * lat / latBands;
            float phi1 = Mth.PI * (lat + 1) / latBands;
            for (int lon = 0; lon <= lonBands; lon++) {
                float theta = Mth.TWO_PI * lon / lonBands;
                float c = Mth.cos(theta), s = Mth.sin(theta);
                buf.addVertex(m,
                        RADIUS * Mth.sin(phi0) * c,
                        RADIUS * Mth.cos(phi0),
                        RADIUS * Mth.sin(phi0) * s)
                        .setColor(0.12f, 0f, 0.28f, alpha);
                buf.addVertex(m,
                        RADIUS * Mth.sin(phi1) * c,
                        RADIUS * Mth.cos(phi1),
                        RADIUS * Mth.sin(phi1) * s)
                        .setColor(0.06f, 0f, 0.14f, alpha * 0.6f);
            }
        }
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private static void quad(BufferBuilder buf, Matrix4f m,
                              float cx, float cy, float cz, float size,
                              Vector3f right, Vector3f up,
                              float r, float g, float b, float a) {
        float rx = right.x * size, ry = right.y * size, rz = right.z * size;
        float ux = up.x * size,    uy = up.y * size,    uz = up.z * size;
        buf.addVertex(m, cx - rx - ux, cy - ry - uy, cz - rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx - ux, cy + ry - uy, cz + rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx + ux, cy + ry + uy, cz + rz + uz).setColor(r, g, b, a);
        buf.addVertex(m, cx - rx + ux, cy - ry + uy, cz - rz + uz).setColor(r, g, b, a);
    }
}
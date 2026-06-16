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

public class UniquenessSpawnEffect extends ActiveEffect {

    private static final float DURATION      = 20 * 7;
    private static final float GATHER_END    = 72f;
    private static final int   GATHER_COUNT  = 70;
    private static final int   WISP_COUNT    = 45;
    private static final int   TENDRIL_COUNT = 20;
    private static final int   BURST_COUNT   = 50;
    private static final int   EMBER_COUNT   = 40;

    private final RandomSource rng = RandomSource.create();

    private final float[] gAngle  = new float[GATHER_COUNT];
    private final float[] gRadius = new float[GATHER_COUNT];
    private final float[] gHeight = new float[GATHER_COUNT];
    private final float[] gSpeed  = new float[GATHER_COUNT];
    private final float[] gSize   = new float[GATHER_COUNT];
    private final float[] gDelay  = new float[GATHER_COUNT];
    private final float[] gHue    = new float[GATHER_COUNT];

    private final float[] wAngle  = new float[WISP_COUNT];
    private final float[] wRadius = new float[WISP_COUNT];
    private final float[] wHeight = new float[WISP_COUNT];
    private final float[] wSpeed  = new float[WISP_COUNT];
    private final float[] wSize   = new float[WISP_COUNT];
    private final float[] wPhase  = new float[WISP_COUNT];
    private final float[] wHue    = new float[WISP_COUNT];

    private final float[] tAngle  = new float[TENDRIL_COUNT];
    private final float[] tSpeed  = new float[TENDRIL_COUNT];
    private final float[] tLength = new float[TENDRIL_COUNT];
    private final float[] tPhase  = new float[TENDRIL_COUNT];
    private final float[] tSize   = new float[TENDRIL_COUNT];

    private final float[] bVx     = new float[BURST_COUNT];
    private final float[] bVy     = new float[BURST_COUNT];
    private final float[] bVz     = new float[BURST_COUNT];
    private final float[] bSize   = new float[BURST_COUNT];
    private final float[] bMaxAge = new float[BURST_COUNT];
    private final float[] bHue    = new float[BURST_COUNT];

    private final float[] eAngle  = new float[EMBER_COUNT];
    private final float[] eSpeed  = new float[EMBER_COUNT];
    private final float[] eRadius = new float[EMBER_COUNT];
    private final float[] eHeight = new float[EMBER_COUNT];
    private final float[] eSize   = new float[EMBER_COUNT];
    private final float[] ePhase  = new float[EMBER_COUNT];
    private final float[] eDelay  = new float[EMBER_COUNT];

    public UniquenessSpawnEffect(double x, double y, double z) {
        super(x, y, z, (int) DURATION);
        bake();
    }

    private void bake() {
        for (int i = 0; i < GATHER_COUNT; i++) {
            gAngle[i]  = rng.nextFloat() * Mth.TWO_PI;
            gRadius[i] = 2.5f + rng.nextFloat() * 3.5f;
            gHeight[i] = -0.8f + rng.nextFloat() * 3.0f;
            gSpeed[i]  = (rng.nextBoolean() ? 1f : -1f) * (0.012f + rng.nextFloat() * 0.018f);
            gSize[i]   = 0.06f + rng.nextFloat() * 0.10f;
            gDelay[i]  = rng.nextFloat() * 30f;
            gHue[i]    = rng.nextFloat();
        }

        for (int i = 0; i < WISP_COUNT; i++) {
            wAngle[i]  = rng.nextFloat() * Mth.TWO_PI;
            wRadius[i] = 0.2f + rng.nextFloat() * 1.2f;
            wHeight[i] = 0.1f + rng.nextFloat() * 2.2f;
            wSpeed[i]  = (rng.nextBoolean() ? 1f : -1f) * (0.025f + rng.nextFloat() * 0.04f);
            wSize[i]   = 0.09f + rng.nextFloat() * 0.16f;
            wPhase[i]  = rng.nextFloat() * Mth.TWO_PI;
            wHue[i]    = rng.nextFloat();
        }

        for (int i = 0; i < TENDRIL_COUNT; i++) {
            tAngle[i]  = rng.nextFloat() * Mth.TWO_PI;
            tSpeed[i]  = (rng.nextBoolean() ? 1f : -1f) * (0.008f + rng.nextFloat() * 0.015f);
            tLength[i] = 4 + rng.nextInt(5);
            tPhase[i]  = rng.nextFloat() * Mth.TWO_PI;
            tSize[i]   = 0.05f + rng.nextFloat() * 0.07f;
        }

        for (int i = 0; i < BURST_COUNT; i++) {
            float theta = rng.nextFloat() * Mth.TWO_PI;
            float phi   = rng.nextFloat() * Mth.PI;
            float speed = 0.06f + rng.nextFloat() * 0.20f;
            bVx[i]     = Mth.sin(phi) * Mth.cos(theta) * speed;
            bVy[i]     = Math.abs(Mth.cos(phi)) * speed * 0.6f + 0.015f;
            bVz[i]     = Mth.sin(phi) * Mth.sin(theta) * speed;
            bSize[i]   = 0.08f + rng.nextFloat() * 0.16f;
            bMaxAge[i] = 12f + rng.nextFloat() * 14f;
            bHue[i]    = rng.nextFloat();
        }

        for (int i = 0; i < EMBER_COUNT; i++) {
            eAngle[i]  = rng.nextFloat() * Mth.TWO_PI;
            eSpeed[i]  = (rng.nextBoolean() ? 1f : -1f) * (0.03f + rng.nextFloat() * 0.05f);
            eRadius[i] = 0.4f + rng.nextFloat() * 1.8f;
            eHeight[i] = rng.nextFloat() * 2.5f;
            eSize[i]   = 0.04f + rng.nextFloat() * 0.07f;
            ePhase[i]  = rng.nextFloat() * Mth.TWO_PI;
            eDelay[i]  = rng.nextFloat() * 8f;
        }
    }

    @Override
    protected void render(PoseStack poseStack, float partialTick) {
        if (Minecraft.getInstance().level == null) return;

        float age        = currentTick + partialTick;
        float globalFade = Mth.clamp((DURATION - age) / 10f, 0f, 1f);

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

        renderGather(m, age, globalFade, right, up);
        renderTendrils(m, age, globalFade, right, up);
        renderWisps(m, age, globalFade, right, up);

        if (age >= GATHER_END) {
            float burstAge = age - GATHER_END;
            renderFlare(m, burstAge, right, up);
            renderBurst(m, burstAge, right, up);
            renderEmbers(m, burstAge, globalFade, right, up);
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private void renderGather(Matrix4f m, float age, float globalFade, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < GATHER_COUNT; i++) {
            float localAge = age - gDelay[i];
            if (localAge <= 0f) continue;

            float dur = GATHER_END - gDelay[i];
            if (dur <= 0f) continue;

            float t     = Mth.clamp(localAge / dur, 0f, 1f);
            float eased = t * t * t;

            float currentRadius = gRadius[i] * (1f - eased);
            float angle         = gAngle[i] + localAge * gSpeed[i];
            float heightDrift   = gHeight[i] * (1f - eased * 0.7f);

            float px = Mth.cos(angle) * currentRadius;
            float py = heightDrift;
            float pz = Mth.sin(angle) * currentRadius;

            float fadeIn  = Mth.clamp(localAge * 0.12f, 0f, 1f);
            float fadeOut = 1f - eased * eased;
            float alpha   = fadeIn * fadeOut * 0.80f * globalFade;
            if (alpha < 0.005f) continue;

            float size = gSize[i] * (1f + eased * 0.8f);

            float[] col = mysteriousColor(gHue[i], eased);
            addQuad(buf, m, px, py, pz, size, right, up, col[0], col[1], col[2], alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderTendrils(Matrix4f m, float age, float globalFade, Vector3f right, Vector3f up) {
        float presenceFade = Mth.clamp(age / 20f, 0f, 1f);
        float gatherFade   = Mth.clamp((GATHER_END - age) / 15f, 0f, 1f);
        float fade         = presenceFade * gatherFade * globalFade;
        if (fade <= 0f) return;

        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < TENDRIL_COUNT; i++) {
            float baseAngle = tAngle[i] + age * tSpeed[i];

            for (int seg = 0; seg < tLength[i]; seg++) {
                float t      = seg / (float) tLength[i];
                float warp   = Mth.sin(age * 0.06f + tPhase[i] + t * Mth.PI) * 0.6f;
                float radius = (1f - t) * (2.5f + warp);
                float angle  = baseAngle + warp * 0.4f;

                float px = Mth.cos(angle) * radius;
                float py = t * 2.8f + Mth.sin(age * 0.05f + tPhase[i]) * 0.3f;
                float pz = Mth.sin(angle) * radius;

                float segAlpha = (float) Math.sin(t * Math.PI) * 0.5f * fade;
                if (segAlpha < 0.005f) continue;

                float darkness = 0.15f + t * 0.25f;
                addQuad(buf, m, px, py, pz, tSize[i], right, up, darkness * 0.4f, 0f, darkness, segAlpha);
                drawn++;
            }
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderWisps(Matrix4f m, float age, float globalFade, Vector3f right, Vector3f up) {
        float presenceFade = Mth.clamp(age / 12f, 0f, 1f);

        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < WISP_COUNT; i++) {
            float angle = wAngle[i] + age * wSpeed[i];
            float bob   = Mth.sin(age * 0.055f + wPhase[i]) * 0.35f;
            float pulse = Mth.sin(age * 0.09f + wPhase[i] * 1.7f);

            float px = Mth.cos(angle) * wRadius[i];
            float py = wHeight[i] + bob;
            float pz = Mth.sin(angle) * wRadius[i];

            float flicker = 0.5f + 0.5f * pulse;
            float alpha   = presenceFade * flicker * 0.60f * globalFade;
            if (alpha < 0.005f) continue;

            float t     = age / DURATION;
            float[] col = mysteriousColor(wHue[i], t);
            float size  = wSize[i] * (1f + 0.2f * pulse);

            addQuad(buf, m, px, py, pz, size, right, up, col[0], col[1], col[2], alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderFlare(Matrix4f m, float burstAge, Vector3f right, Vector3f up) {
        final float MAX = 12f;
        if (burstAge >= MAX) return;

        float t     = burstAge / MAX;
        float alpha = (1f - t) * (1f - t) * 0.75f;
        float size  = 0.5f + t * 3.5f;

        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addQuad(buf, m, 0f, 1.0f, 0f, size,       right, up, 0.45f, 0f, 0.7f,  alpha);
        addQuad(buf, m, 0f, 1.0f, 0f, size * 0.5f, right, up, 0.8f, 0.3f, 1f, alpha * 0.6f);
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderBurst(Matrix4f m, float burstAge, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < BURST_COUNT; i++) {
            if (burstAge > bMaxAge[i]) continue;
            float t     = burstAge / bMaxAge[i];
            float alpha = (float) Math.sin(t * Math.PI) * 0.85f;
            if (alpha < 0.005f) continue;

            float px = bVx[i] * burstAge;
            float py = bVy[i] * burstAge;
            float pz = bVz[i] * burstAge;
            float size = bSize[i] * (1f + t * 0.8f);

            float[] col = mysteriousColor(bHue[i], t);
            addQuad(buf, m, px, py, pz, size, right, up, col[0], col[1], col[2], alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderEmbers(Matrix4f m, float burstAge, float globalFade, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < EMBER_COUNT; i++) {
            float localAge = burstAge - eDelay[i];
            if (localAge <= 0f) continue;

            float angle = eAngle[i] + localAge * eSpeed[i];
            float drift = eRadius[i] * (1f + localAge * 0.012f);
            float rise  = eHeight[i] + localAge * 0.025f;
            float bob   = Mth.sin(localAge * 0.08f + ePhase[i]) * 0.2f;

            float px = Mth.cos(angle) * drift;
            float py = rise + bob;
            float pz = Mth.sin(angle) * drift;

            float fadeIn  = Mth.clamp(localAge * 0.2f, 0f, 1f);
            float fadeOut = globalFade;
            float flicker = 0.6f + 0.4f * Mth.sin(localAge * 0.15f + ePhase[i]);
            float alpha   = fadeIn * fadeOut * flicker * 0.65f;
            if (alpha < 0.005f) continue;

            float t   = Mth.clamp(localAge / 28f, 0f, 1f);
            float[] col = mysteriousColor(ePhase[i] / Mth.TWO_PI, t);
            addQuad(buf, m, px, py, pz, eSize[i], right, up, col[0], col[1], col[2], alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private static float[] mysteriousColor(float hue, float t) {
        float teal   = 0.15f + (1f - t) * 0.25f;
        float violet = 0.35f + t * 0.30f;

        if (hue < 0.25f) {
            return new float[]{ teal * 0.4f, teal, teal * 0.8f };
        } else if (hue < 0.50f) {
            return new float[]{ violet * 0.6f, 0f, violet };
        } else if (hue < 0.75f) {
            return new float[]{ 0.05f, teal * 0.5f, violet * 0.9f };
        } else {
            float mid = (teal + violet) * 0.5f;
            return new float[]{ mid * 0.5f, 0f, mid * 1.1f };
        }
    }

    private static void addQuad(BufferBuilder buf, Matrix4f m,
                                float cx, float cy, float cz, float size,
                                Vector3f right, Vector3f up,
                                float r, float g, float b, float a) {
        float rx = right.x * size, ry = right.y * size, rz = right.z * size;
        float ux = up.x    * size, uy = up.y    * size, uz = up.z    * size;
        buf.addVertex(m, cx - rx - ux, cy - ry - uy, cz - rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx - ux, cy + ry - uy, cz + rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx + ux, cy + ry + uy, cz + rz + uz).setColor(r, g, b, a);
        buf.addVertex(m, cx - rx + ux, cy - ry + uy, cz - rz + uz).setColor(r, g, b, a);
    }
}
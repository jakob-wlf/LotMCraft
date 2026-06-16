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

public class HolyImpactEffect extends ActiveEffect {

    private static final int   BURST_COUNT  = 40;
    private static final int   DRIFT_COUNT  = 25;
    private static final float DURATION     = 20f;

    private final RandomSource rng = RandomSource.create();

    private final float[] bVx     = new float[BURST_COUNT];
    private final float[] bVy     = new float[BURST_COUNT];
    private final float[] bVz     = new float[BURST_COUNT];
    private final float[] bSize   = new float[BURST_COUNT];
    private final float[] bMaxAge = new float[BURST_COUNT];
    private final float[] bWhite  = new float[BURST_COUNT];

    private final float[] dVx     = new float[DRIFT_COUNT];
    private final float[] dVy     = new float[DRIFT_COUNT];
    private final float[] dVz     = new float[DRIFT_COUNT];
    private final float[] dSize   = new float[DRIFT_COUNT];
    private final float[] dDelay  = new float[DRIFT_COUNT];
    private final float[] dLife   = new float[DRIFT_COUNT];

    public HolyImpactEffect(double x, double y, double z) {
        super(x, y, z, (int) DURATION);
        bake();
    }

    private void bake() {
        for (int i = 0; i < BURST_COUNT; i++) {
            float theta = rng.nextFloat() * Mth.TWO_PI;
            float phi   = rng.nextFloat() * Mth.PI;
            float speed = 0.10f + rng.nextFloat() * 0.22f;
            bVx[i]     = Mth.sin(phi) * Mth.cos(theta) * speed;
            bVy[i]     = Math.abs(Mth.cos(phi)) * speed + 0.04f;
            bVz[i]     = Mth.sin(phi) * Mth.sin(theta) * speed;
            bSize[i]   = 0.06f + rng.nextFloat() * 0.10f;
            bMaxAge[i] = 8f + rng.nextFloat() * 8f;
            bWhite[i]  = rng.nextFloat();
        }

        for (int i = 0; i < DRIFT_COUNT; i++) {
            float theta = rng.nextFloat() * Mth.TWO_PI;
            float speed = 0.015f + rng.nextFloat() * 0.025f;
            dVx[i]    = Mth.cos(theta) * speed;
            dVy[i]    = 0.02f + rng.nextFloat() * 0.03f;
            dVz[i]    = Mth.sin(theta) * speed;
            dSize[i]  = 0.08f + rng.nextFloat() * 0.14f;
            dDelay[i] = rng.nextFloat() * 6f;
            dLife[i]  = 8f + rng.nextFloat() * 6f;
        }
    }

    @Override
    protected void render(PoseStack poseStack, float partialTick) {
        if (Minecraft.getInstance().level == null) return;

        float age = currentTick + partialTick;

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

        renderFlash(m, age, right, up);
        renderBurst(m, age, right, up);
        renderDrift(m, age, right, up);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private void renderFlash(Matrix4f m, float age, Vector3f right, Vector3f up) {
        final float MAX = 5f;
        if (age >= MAX) return;
        float t     = age / MAX;
        float alpha = (1f - t) * (1f - t) * 0.85f;
        float size  = 0.4f + t * 2.2f;
        quad(m, 0f, 0f, 0f, size, right, up, 1f, 1f, 0.85f, alpha);
    }

    private void renderBurst(Matrix4f m, float age, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < BURST_COUNT; i++) {
            if (age > bMaxAge[i]) continue;
            float t     = age / bMaxAge[i];
            float alpha = (float) Math.sin(t * Math.PI) * 0.9f;
            if (alpha < 0.005f) continue;

            float px = bVx[i] * age;
            float py = bVy[i] * age;
            float pz = bVz[i] * age;
            float size = bSize[i] * (1f + t * 0.4f);

            float g = 0.85f + bWhite[i] * 0.15f;
            float b = 0.55f + bWhite[i] * 0.45f;

            buf.addVertex(m, px - right.x*size - up.x*size, py - right.y*size - up.y*size, pz - right.z*size - up.z*size).setColor(1f, g, b, alpha);
            buf.addVertex(m, px + right.x*size - up.x*size, py + right.y*size - up.y*size, pz + right.z*size - up.z*size).setColor(1f, g, b, alpha);
            buf.addVertex(m, px + right.x*size + up.x*size, py + right.y*size + up.y*size, pz + right.z*size + up.z*size).setColor(1f, g, b, alpha);
            buf.addVertex(m, px - right.x*size + up.x*size, py - right.y*size + up.y*size, pz - right.z*size + up.z*size).setColor(1f, g, b, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderDrift(Matrix4f m, float age, Vector3f right, Vector3f up) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < DRIFT_COUNT; i++) {
            float local = age - dDelay[i];
            if (local <= 0f || local > dLife[i]) continue;
            float t     = local / dLife[i];
            float alpha = Mth.clamp(local * 0.4f, 0f, 1f) * (1f - t) * 0.6f;
            if (alpha < 0.005f) continue;

            float px = dVx[i] * local;
            float py = dVy[i] * local;
            float pz = dVz[i] * local;
            float size = dSize[i] * (1f + t * 1.2f);

            buf.addVertex(m, px - right.x*size - up.x*size, py - right.y*size - up.y*size, pz - right.z*size - up.z*size).setColor(1f, 0.95f, 0.6f, alpha);
            buf.addVertex(m, px + right.x*size - up.x*size, py + right.y*size - up.y*size, pz + right.z*size - up.z*size).setColor(1f, 0.95f, 0.6f, alpha);
            buf.addVertex(m, px + right.x*size + up.x*size, py + right.y*size + up.y*size, pz + right.z*size + up.z*size).setColor(1f, 0.95f, 0.6f, alpha);
            buf.addVertex(m, px - right.x*size + up.x*size, py - right.y*size + up.y*size, pz - right.z*size + up.z*size).setColor(1f, 0.95f, 0.6f, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void quad(Matrix4f m, float cx, float cy, float cz, float size,
                      Vector3f right, Vector3f up, float r, float g, float b, float a) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.addVertex(m, cx - right.x*size - up.x*size, cy - right.y*size - up.y*size, cz - right.z*size - up.z*size).setColor(r, g, b, a);
        buf.addVertex(m, cx + right.x*size - up.x*size, cy + right.y*size - up.y*size, cz + right.z*size - up.z*size).setColor(r, g, b, a);
        buf.addVertex(m, cx + right.x*size + up.x*size, cy + right.y*size + up.y*size, cz + right.z*size + up.z*size).setColor(r, g, b, a);
        buf.addVertex(m, cx - right.x*size + up.x*size, cy - right.y*size + up.y*size, cz - right.z*size + up.z*size).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}
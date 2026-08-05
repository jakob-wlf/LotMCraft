package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RotatingRingsEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final ObsidianRing[] rings = new ObsidianRing[3];

    private static final ResourceLocation OBSIDIAN_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/block/obsidian.png");

    public RotatingRingsEffect(double x, double y, double z) {
        super(x, y, z, 20 * 60 * 5);

        rings[0] = new ObsidianRing(3.0f, 4.4f,  0.045f,  0.031f,  0.017f);
        rings[1] = new ObsidianRing(3.5f, 5.0f, -0.032f,  0.024f, -0.011f);
        rings[2] = new ObsidianRing(2.5f, 3.8f,  0.058f, -0.019f,  0.027f);
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        bufferSource.endBatch(RenderType.lightning());

        VertexConsumer texConsumer = bufferSource.getBuffer(RenderType.entitySolid(OBSIDIAN_TEXTURE));
        for (ObsidianRing ring : rings) {
            ring.update(tick);
            renderObsidianRing(texConsumer, matrix, normalMatrix, ring);
        }

        poseStack.popPose();
    }

    private void renderObsidianRing(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix, ObsidianRing ring) {
        int segments = 48;
        float uvTiles = 8f;

        float nx = -Mth.cos(ring.tiltX) * Mth.sin(ring.tiltZ);
        float ny =  Mth.cos(ring.tiltX) * Mth.cos(ring.tiltZ);
        float nz =  Mth.sin(ring.tiltX);

        for (int i = 0; i < segments; i++) {
            float phi1 = (i       / (float) segments) * Mth.TWO_PI + ring.spinAngle;
            float phi2 = ((i + 1) / (float) segments) * Mth.TWO_PI + ring.spinAngle;

            float u1 = (i       / (float) segments) * uvTiles;
            float u2 = ((i + 1) / (float) segments) * uvTiles;

            float[] innerA = tiltPoint(Mth.cos(phi1) * ring.innerRadius, 0, Mth.sin(phi1) * ring.innerRadius, ring.tiltX, ring.tiltZ);
            float[] outerA = tiltPoint(Mth.cos(phi1) * ring.outerRadius, 0, Mth.sin(phi1) * ring.outerRadius, ring.tiltX, ring.tiltZ);
            float[] innerB = tiltPoint(Mth.cos(phi2) * ring.innerRadius, 0, Mth.sin(phi2) * ring.innerRadius, ring.tiltX, ring.tiltZ);
            float[] outerB = tiltPoint(Mth.cos(phi2) * ring.outerRadius, 0, Mth.sin(phi2) * ring.outerRadius, ring.tiltX, ring.tiltZ);

            addTexVertex(consumer, matrix, normalMatrix, innerA[0], innerA[1], innerA[2], u1, 0f, nx, ny, nz);
            addTexVertex(consumer, matrix, normalMatrix, outerA[0], outerA[1], outerA[2], u1, 1f, nx, ny, nz);
            addTexVertex(consumer, matrix, normalMatrix, outerB[0], outerB[1], outerB[2], u2, 1f, nx, ny, nz);
            addTexVertex(consumer, matrix, normalMatrix, innerB[0], innerB[1], innerB[2], u2, 0f, nx, ny, nz);

            addTexVertex(consumer, matrix, normalMatrix, innerB[0], innerB[1], innerB[2], u2, 0f, -nx, -ny, -nz);
            addTexVertex(consumer, matrix, normalMatrix, outerB[0], outerB[1], outerB[2], u2, 1f, -nx, -ny, -nz);
            addTexVertex(consumer, matrix, normalMatrix, outerA[0], outerA[1], outerA[2], u1, 1f, -nx, -ny, -nz);
            addTexVertex(consumer, matrix, normalMatrix, innerA[0], innerA[1], innerA[2], u1, 0f, -nx, -ny, -nz);
        }
    }

    private float[] tiltPoint(float x, float y, float z, float tiltX, float tiltZ) {
        float ry = y * Mth.cos(tiltX) - z * Mth.sin(tiltX);
        float rz = y * Mth.sin(tiltX) + z * Mth.cos(tiltX);
        float fx = x * Mth.cos(tiltZ) - ry * Mth.sin(tiltZ);
        float fy = x * Mth.sin(tiltZ) + ry * Mth.cos(tiltZ);
        return new float[]{fx, fy, rz};
    }

    private void addTexVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix,
                              float x, float y, float z, float u, float v,
                              float nx, float ny, float nz) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(nx, ny, nz);
    }

    private static class ObsidianRing {
        float innerRadius;
        float outerRadius;
        float spinSpeed;
        float spinAngle;
        float tiltX;
        float tiltZ;
        final float precessSpeedX;
        final float precessSpeedZ;

        ObsidianRing(float innerRadius, float outerRadius,
                     float spinSpeed,
                     float precessSpeedX, float precessSpeedZ) {
            this.innerRadius   = innerRadius;
            this.outerRadius   = outerRadius;
            this.spinSpeed     = spinSpeed;
            this.spinAngle     = 0f;
            this.tiltX         = (float)(Math.random() * Mth.TWO_PI);
            this.tiltZ         = (float)(Math.random() * Mth.TWO_PI);
            this.precessSpeedX = precessSpeedX;
            this.precessSpeedZ = precessSpeedZ;
        }

        void update(float tick) {
            spinAngle = tick * spinSpeed;
            tiltX    += precessSpeedX;
            tiltZ    += precessSpeedZ;
        }
    }
}
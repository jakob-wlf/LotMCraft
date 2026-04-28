package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class ProhibitionEffect extends ActiveEffect {

    private static final float MAX_RADIUS = 40f;
    private static final float HEIGHT = 6f;

    public ProhibitionEffect(double x, double y, double z) {
        super(x, y, z, 160); // 8 seconds total
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = getProgress();

        // Phase 1 (0-40t = 0.0-0.25): cylinder expands from 0 to MAX_RADIUS
        // Phase 2 (40-120t = 0.25-0.75): hold at full size with slow pulsing glow
        // Phase 3 (120-160t = 0.75-1.0): fade out
        float radius;
        float alpha;

        if (progress < 0.25f) {
            float phase = progress / 0.25f;
            radius = phase * MAX_RADIUS;
            alpha = 0.5f + 0.4f * phase;
        } else if (progress < 0.75f) {
            float phase = (progress - 0.25f) / 0.5f;
            radius = MAX_RADIUS;
            alpha = 0.65f + 0.25f * Mth.sin(phase * Mth.TWO_PI * 2.5f);
        } else {
            float phase = (progress - 0.75f) / 0.25f;
            radius = MAX_RADIUS;
            alpha = 0.9f * (1.0f - phase);
        }

        if (radius < 0.01f || alpha < 0.01f) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float r = 1.0f, g = 0.85f, b = 0.1f;
        int segments = 48; // more segments for larger radius

        renderRing(consumer, matrix, radius, 0.0f, r, g, b, alpha, segments);
        renderRing(consumer, matrix, radius, HEIGHT, r, g, b, alpha, segments);
        renderVerticalWall(consumer, matrix, radius, 0.0f, HEIGHT, r, g, b, alpha * 0.35f, segments);
        renderSealQuads(consumer, matrix, radius, HEIGHT / 2f, tick, r, g, b, alpha * 0.9f);

        poseStack.popPose();
    }

    private void renderRing(VertexConsumer consumer, Matrix4f matrix, float radius, float y,
                            float r, float g, float b, float alpha, int segments) {
        float thickness = 0.3f;
        float inner = radius - thickness;
        float outer = radius + thickness;
        for (int i = 0; i < segments; i++) {
            float a1 = (i / (float) segments) * Mth.TWO_PI;
            float a2 = ((i + 1) / (float) segments) * Mth.TWO_PI;
            float ix1 = Mth.cos(a1) * inner, iz1 = Mth.sin(a1) * inner;
            float ox1 = Mth.cos(a1) * outer, oz1 = Mth.sin(a1) * outer;
            float ix2 = Mth.cos(a2) * inner, iz2 = Mth.sin(a2) * inner;
            float ox2 = Mth.cos(a2) * outer, oz2 = Mth.sin(a2) * outer;
            addVertex(consumer, matrix, ix1, y, iz1, r, g, b, alpha);
            addVertex(consumer, matrix, ix2, y, iz2, r, g, b, alpha);
            addVertex(consumer, matrix, ox2, y, oz2, r, g, b, alpha * 0.3f);
            addVertex(consumer, matrix, ox1, y, oz1, r, g, b, alpha * 0.3f);
        }
    }

    private void renderVerticalWall(VertexConsumer consumer, Matrix4f matrix, float radius, float yBottom, float yTop,
                                    float r, float g, float b, float alpha, int segments) {
        for (int i = 0; i < segments; i++) {
            float a1 = (i / (float) segments) * Mth.TWO_PI;
            float a2 = ((i + 1) / (float) segments) * Mth.TWO_PI;
            float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;
            float x2 = Mth.cos(a2) * radius, z2 = Mth.sin(a2) * radius;
            addVertex(consumer, matrix, x1, yBottom, z1, r, g, b, alpha);
            addVertex(consumer, matrix, x2, yBottom, z2, r, g, b, alpha);
            addVertex(consumer, matrix, x2, yTop, z2, r, g, b, alpha * 0.1f);
            addVertex(consumer, matrix, x1, yTop, z1, r, g, b, alpha * 0.1f);
        }
    }

    private void renderSealQuads(VertexConsumer consumer, Matrix4f matrix, float radius, float y,
                                 float tick, float r, float g, float b, float alpha) {
        int count = 32;
        float size = 0.5f;
        float rotOffset = tick * 0.015f;
        for (int i = 0; i < count; i++) {
            float angle = (i / (float) count) * Mth.TWO_PI + rotOffset;
            float sx = Mth.cos(angle) * radius;
            float sz = Mth.sin(angle) * radius;
            addVertex(consumer, matrix, sx - size, y - size, sz, r, g, b, alpha);
            addVertex(consumer, matrix, sx + size, y - size, sz, r, g, b, alpha);
            addVertex(consumer, matrix, sx + size, y + size, sz, r, g, b, alpha);
            addVertex(consumer, matrix, sx - size, y + size, sz, r, g, b, alpha);
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix,
                           float x, float y, float z, float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }
}
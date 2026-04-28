package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class ImprisonEffect extends ActiveEffect {

    private static final int BAR_COUNT = 8;
    private static final float BAR_RADIUS = 1.2f;
    private static final float BAR_HEIGHT = 3.0f;

    public ImprisonEffect(double x, double y, double z) {
        super(x, y, z, 120);
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = getProgress();

        // Phase 1 (0-20t = 0.0-0.167): bars slide down from above
        // Phase 2 (20-100t = 0.167-0.833): hold, rotate slowly, golden pulse
        // Phase 3 (100-120t = 0.833-1.0): fade out
        float alpha;
        float rotation;
        float yOffset;

        if (progress < 0.167f) {
            float phase = progress / 0.167f;
            yOffset = (1.0f - phase) * BAR_HEIGHT;
            alpha = phase;
            rotation = 0f;
        } else if (progress < 0.833f) {
            float phase = (progress - 0.167f) / 0.666f;
            yOffset = 0f;
            rotation = phase * Mth.TWO_PI * 0.25f;
            alpha = 0.75f + 0.2f * Mth.sin(phase * Mth.TWO_PI * 3f);
        } else {
            float phase = (progress - 0.833f) / 0.167f;
            yOffset = 0f;
            rotation = 0.666f * Mth.TWO_PI * 0.25f;
            alpha = 1.0f - phase;
        }

        if (alpha < 0.01f) return;

        poseStack.pushPose();
        poseStack.translate(x, y + yOffset, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float barR = 0.55f, barG = 0.55f, barB = 0.6f;
        float goldR = 0.85f, goldG = 0.7f, goldB = 0.1f;
        float barWidth = 0.09f;

        for (int i = 0; i < BAR_COUNT; i++) {
            float angle = (i / (float) BAR_COUNT) * Mth.TWO_PI + rotation;
            float bx = Mth.cos(angle) * BAR_RADIUS;
            float bz = Mth.sin(angle) * BAR_RADIUS;
            renderBar(consumer, matrix, bx, bz, barWidth, BAR_HEIGHT, barR, barG, barB, alpha);
            renderBar(consumer, matrix, bx, bz, barWidth * 0.35f, BAR_HEIGHT, goldR, goldG, goldB, alpha * 0.4f);
        }

        renderHorizontalRing(consumer, matrix, BAR_HEIGHT, goldR, goldG, goldB, alpha, 24, rotation);
        renderHorizontalRing(consumer, matrix, 0.0f, goldR, goldG, goldB, alpha, 24, rotation);

        poseStack.popPose();
    }

    private void renderBar(VertexConsumer consumer, Matrix4f matrix,
                           float bx, float bz, float width, float height,
                           float r, float g, float b, float alpha) {
        addVertex(consumer, matrix, bx - width, 0f, bz, r, g, b, alpha);
        addVertex(consumer, matrix, bx + width, 0f, bz, r, g, b, alpha);
        addVertex(consumer, matrix, bx + width, height, bz, r, g, b, alpha * 0.5f);
        addVertex(consumer, matrix, bx - width, height, bz, r, g, b, alpha * 0.5f);
    }

    private void renderHorizontalRing(VertexConsumer consumer, Matrix4f matrix,
                                      float y, float r, float g, float b, float alpha,
                                      int segments, float rotation) {
        float inner = BAR_RADIUS - 0.1f;
        float outer = BAR_RADIUS + 0.1f;
        for (int i = 0; i < segments; i++) {
            float a1 = (i / (float) segments) * Mth.TWO_PI + rotation;
            float a2 = ((i + 1) / (float) segments) * Mth.TWO_PI + rotation;
            float ix1 = Mth.cos(a1) * inner, iz1 = Mth.sin(a1) * inner;
            float ox1 = Mth.cos(a1) * outer, oz1 = Mth.sin(a1) * outer;
            float ix2 = Mth.cos(a2) * inner, iz2 = Mth.sin(a2) * inner;
            float ox2 = Mth.cos(a2) * outer, oz2 = Mth.sin(a2) * outer;
            addVertex(consumer, matrix, ix1, y, iz1, r, g, b, alpha);
            addVertex(consumer, matrix, ix2, y, iz2, r, g, b, alpha);
            addVertex(consumer, matrix, ox2, y, oz2, r, g, b, alpha * 0.4f);
            addVertex(consumer, matrix, ox1, y, oz1, r, g, b, alpha * 0.4f);
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix,
                           float x, float y, float z, float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }
}

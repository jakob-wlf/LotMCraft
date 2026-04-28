package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class AncientCourtEffect extends ActiveEffect {

    private static final float RING_RADIUS = 50f;
    private static final float PILLAR_HEIGHT = 15f;

    private static final float GOLD_R = 1.0f, GOLD_G = 0.80f, GOLD_B = 0.10f;
    private static final float WHITE_R = 1.0f, WHITE_G = 1.0f, WHITE_B = 0.85f;

    public AncientCourtEffect(double x, double y, double z) {
        super(x, y, z, 120); // 6 seconds — entity re-fires every 5s for overlap
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Fade in (0-10t), hold (10-100t), fade out (100-120t)
        float alpha;
        if (tick < 10f) {
            alpha = tick / 10f;
        } else if (tick > 100f) {
            alpha = (120f - tick) / 20f;
        } else {
            alpha = 0.80f + 0.20f * Mth.sin((tick - 10f) * 0.07f);
        }
        alpha = Mth.clamp(alpha, 0f, 1f);
        if (alpha < 0.01f) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buf.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        renderOuterRing(consumer, matrix, alpha);
        renderPillars(consumer, matrix, alpha);
        renderCompassLines(consumer, matrix, alpha);
        renderCentralColumn(consumer, matrix, alpha);
        renderRotatingRunes(consumer, matrix, tick, alpha);

        poseStack.popPose();
    }

    // ── Outer boundary ring ───────────────────────────────────────────────────

    private void renderOuterRing(VertexConsumer consumer, Matrix4f matrix, float alpha) {
        int segments = 64;
        float inner = RING_RADIUS - 0.6f;
        float outer = RING_RADIUS + 0.6f;
        for (int i = 0; i < segments; i++) {
            float a1 = (i / (float) segments) * Mth.TWO_PI;
            float a2 = ((i + 1) / (float) segments) * Mth.TWO_PI;
            float ix1 = Mth.cos(a1) * inner, iz1 = Mth.sin(a1) * inner;
            float ox1 = Mth.cos(a1) * outer, oz1 = Mth.sin(a1) * outer;
            float ix2 = Mth.cos(a2) * inner, iz2 = Mth.sin(a2) * inner;
            float ox2 = Mth.cos(a2) * outer, oz2 = Mth.sin(a2) * outer;
            addVertex(consumer, matrix, ix1, 0.02f, iz1, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, ix2, 0.02f, iz2, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, ox2, 0.02f, oz2, GOLD_R, GOLD_G, GOLD_B, alpha * 0.25f);
            addVertex(consumer, matrix, ox1, 0.02f, oz1, GOLD_R, GOLD_G, GOLD_B, alpha * 0.25f);
        }
    }

    // ── 8 evenly spaced pillars ───────────────────────────────────────────────

    private void renderPillars(VertexConsumer consumer, Matrix4f matrix, float alpha) {
        int pillarCount = 8;
        float pillarHalfWidth = 0.35f;
        for (int i = 0; i < pillarCount; i++) {
            float angle = (i / (float) pillarCount) * Mth.TWO_PI;
            float cx = Mth.cos(angle) * RING_RADIUS;
            float cz = Mth.sin(angle) * RING_RADIUS;

            // Perpendicular offset for pillar face width
            float px = -Mth.sin(angle) * pillarHalfWidth;
            float pz = Mth.cos(angle) * pillarHalfWidth;

            // Main pillar quad (facing tangentially along ring)
            addVertex(consumer, matrix, cx - px, 0f, cz - pz, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, cx + px, 0f, cz + pz, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, cx + px, PILLAR_HEIGHT, cz + pz, GOLD_R, GOLD_G, GOLD_B, 0f);
            addVertex(consumer, matrix, cx - px, PILLAR_HEIGHT, cz - pz, GOLD_R, GOLD_G, GOLD_B, 0f);

            // Ornamental horizontal rings on each pillar
            renderSmallRing(consumer, matrix, cx, PILLAR_HEIGHT * 0.25f, cz, 1.2f, alpha * 0.85f);
            renderSmallRing(consumer, matrix, cx, PILLAR_HEIGHT * 0.55f, cz, 1.2f, alpha * 0.65f);
            renderSmallRing(consumer, matrix, cx, PILLAR_HEIGHT * 0.85f, cz, 1.0f, alpha * 0.45f);
        }
    }

    private void renderSmallRing(VertexConsumer consumer, Matrix4f matrix,
                                  float cx, float cy, float cz, float radius, float alpha) {
        int seg = 16;
        float inner = radius - 0.15f, outer = radius + 0.15f;
        for (int i = 0; i < seg; i++) {
            float a1 = (i / (float) seg) * Mth.TWO_PI;
            float a2 = ((i + 1) / (float) seg) * Mth.TWO_PI;
            float ix1 = cx + Mth.cos(a1) * inner, iz1 = cz + Mth.sin(a1) * inner;
            float ox1 = cx + Mth.cos(a1) * outer, oz1 = cz + Mth.sin(a1) * outer;
            float ix2 = cx + Mth.cos(a2) * inner, iz2 = cz + Mth.sin(a2) * inner;
            float ox2 = cx + Mth.cos(a2) * outer, oz2 = cz + Mth.sin(a2) * outer;
            addVertex(consumer, matrix, ix1, cy, iz1, WHITE_R, WHITE_G, WHITE_B, alpha);
            addVertex(consumer, matrix, ix2, cy, iz2, WHITE_R, WHITE_G, WHITE_B, alpha);
            addVertex(consumer, matrix, ox2, cy, oz2, WHITE_R, WHITE_G, WHITE_B, alpha * 0.15f);
            addVertex(consumer, matrix, ox1, cy, oz1, WHITE_R, WHITE_G, WHITE_B, alpha * 0.15f);
        }
    }

    // ── 8 compass lines from center to ring ──────────────────────────────────

    private void renderCompassLines(VertexConsumer consumer, Matrix4f matrix, float alpha) {
        int lineCount = 8;
        float halfWidth = 0.12f;
        float lineAlpha = alpha * 0.45f;
        for (int i = 0; i < lineCount; i++) {
            float angle = (i / (float) lineCount) * Mth.TWO_PI;
            float ex = Mth.cos(angle) * RING_RADIUS;
            float ez = Mth.sin(angle) * RING_RADIUS;
            float px = -Mth.sin(angle) * halfWidth;
            float pz = Mth.cos(angle) * halfWidth;
            addVertex(consumer, matrix, -px, 0.05f, -pz, GOLD_R, GOLD_G, GOLD_B, lineAlpha);
            addVertex(consumer, matrix,  px, 0.05f,  pz, GOLD_R, GOLD_G, GOLD_B, lineAlpha);
            addVertex(consumer, matrix, ex + px, 0.05f, ez + pz, GOLD_R, GOLD_G, GOLD_B, 0f);
            addVertex(consumer, matrix, ex - px, 0.05f, ez - pz, GOLD_R, GOLD_G, GOLD_B, 0f);
        }
    }

    // ── Central beacon column ─────────────────────────────────────────────────

    private void renderCentralColumn(VertexConsumer consumer, Matrix4f matrix, float alpha) {
        float w = 0.18f;
        float h = 10f;
        int sides = 4;
        for (int i = 0; i < sides; i++) {
            float a1 = (i / (float) sides) * Mth.TWO_PI;
            float a2 = ((i + 1) / (float) sides) * Mth.TWO_PI;
            float x1 = Mth.cos(a1) * w, z1 = Mth.sin(a1) * w;
            float x2 = Mth.cos(a2) * w, z2 = Mth.sin(a2) * w;
            addVertex(consumer, matrix, x1, 0f, z1, WHITE_R, WHITE_G, WHITE_B, alpha);
            addVertex(consumer, matrix, x2, 0f, z2, WHITE_R, WHITE_G, WHITE_B, alpha);
            addVertex(consumer, matrix, x2, h, z2, WHITE_R, WHITE_G, WHITE_B, 0f);
            addVertex(consumer, matrix, x1, h, z1, WHITE_R, WHITE_G, WHITE_B, 0f);
        }
        // Base ring
        renderSmallRing(consumer, matrix, 0f, 0.1f, 0f, 1.5f, alpha * 0.7f);
    }

    // ── Rotating runes orbiting the perimeter ────────────────────────────────

    private void renderRotatingRunes(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        int count = 24;
        float size = 1.2f;
        float rotSpeed = tick * 0.006f;
        for (int i = 0; i < count; i++) {
            float angle = (i / (float) count) * Mth.TWO_PI + rotSpeed;
            float rx = Mth.cos(angle) * RING_RADIUS;
            float rz = Mth.sin(angle) * RING_RADIUS;
            float ry = 1.5f + Mth.sin(angle * 4f + tick * 0.025f) * 2.0f;
            float runeAlpha = alpha * (0.4f + 0.6f * Mth.sin(i * 1.3f + tick * 0.04f) * 0.5f + 0.5f);

            // Face runes toward center
            float facingX = -Mth.cos(angle) * size;
            float facingZ = -Mth.sin(angle) * size;
            float perpX = -Mth.sin(angle) * size;
            float perpZ = Mth.cos(angle) * size;

            addVertex(consumer, matrix, rx - perpX, ry - size, rz - perpZ, GOLD_R, GOLD_G, GOLD_B, runeAlpha);
            addVertex(consumer, matrix, rx + perpX, ry - size, rz + perpZ, GOLD_R, GOLD_G, GOLD_B, runeAlpha);
            addVertex(consumer, matrix, rx + perpX, ry + size, rz + perpZ, GOLD_R, GOLD_G, GOLD_B, runeAlpha * 0.2f);
            addVertex(consumer, matrix, rx - perpX, ry + size, rz - perpZ, GOLD_R, GOLD_G, GOLD_B, runeAlpha * 0.2f);
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private void addVertex(VertexConsumer consumer, Matrix4f matrix,
                           float x, float y, float z, float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }
}

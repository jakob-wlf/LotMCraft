package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class HolyBeamEffect extends ActiveEffect {

    private static final float[] WHITE         = {1f, 1f, 1f};
    private static final float[] YELLOW        = {1f, 0.95f, 0.5f};
    private static final float[] BRIGHT_YELLOW = {1f, 0.85f, 0.2f};

    private static final int RING_SIDES  = 8;
    private static final int BEAM_STEPS  = 12;

    private final float[] cosTable = new float[RING_SIDES];
    private final float[] sinTable = new float[RING_SIDES];

    private Vec3 perp1;
    private Vec3 perp2;

    public HolyBeamEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);

        for (int i = 0; i < RING_SIDES; i++) {
            float angle = (float) (i * Math.PI * 2 / RING_SIDES);
            cosTable[i] = Mth.cos(angle);
            sinTable[i] = Mth.sin(angle);
        }
    }

    private void ensurePerpVectors() {
        if (perp1 != null) return;

        Vec3 direction = getDirection();

        Vec3 base = (Math.abs(direction.x) < 1e-6 && Math.abs(direction.z) < 1e-6)
                ? new Vec3(1, 0, 0)
                : direction;

        perp1 = new Vec3(-base.z, 0, base.x).normalize();
        perp2 = direction.cross(perp1).normalize();
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ensurePerpVectors();

        float progress = tick / maxDuration;
        float fadeIn  = Mth.clamp(progress / 0.1f, 0f, 1f);
        float fadeOut = Mth.clamp(1f - (progress - 0.9f) / 0.1f, 0f, 1f);
        float intensity = fadeIn * fadeOut;
        if (intensity <= 0f) return;

        poseStack.pushPose();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        renderCylinder(consumer, matrix, 0.18f, WHITE,         intensity * 0.9f);
        renderCylinder(consumer, matrix, 0.35f, YELLOW,        intensity * 0.55f);
        renderCylinder(consumer, matrix, 0.55f, BRIGHT_YELLOW, intensity * 0.25f);

        poseStack.popPose();
    }

    private void renderCylinder(VertexConsumer consumer, Matrix4f matrix,
                                float radius, float[] color, float alpha) {
        if (alpha <= 0f) return;

        float p1x = (float) perp1.x * radius;
        float p1y = (float) perp1.y * radius;
        float p1z = (float) perp1.z * radius;
        float p2x = (float) perp2.x * radius;
        float p2y = (float) perp2.y * radius;
        float p2z = (float) perp2.z * radius;

        for (int step = 0; step < BEAM_STEPS; step++) {
            float t1 = step / (float) BEAM_STEPS;
            float t2 = (step + 1) / (float) BEAM_STEPS;

            float ax1 = (float) (getStartPos().x + (getEndPos().x - getStartPos().x) * t1);
            float ay1 = (float) (getStartPos().y + (getEndPos().y - getStartPos().y) * t1);
            float az1 = (float) (getStartPos().z + (getEndPos().z - getStartPos().z) * t1);

            float ax2 = (float) (getStartPos().x + (getEndPos().x - getStartPos().x) * t2);
            float ay2 = (float) (getStartPos().y + (getEndPos().y - getStartPos().y) * t2);
            float az2 = (float) (getStartPos().z + (getEndPos().z - getStartPos().z) * t2);

            for (int s = 0; s < RING_SIDES; s++) {
                int sNext = (s + 1) % RING_SIDES;

                float c0 = cosTable[s];
                float s0 = sinTable[s];
                float c1 = cosTable[sNext];
                float s1 = sinTable[sNext];

                float ox0 = p1x * c0 + p2x * s0;
                float oy0 = p1y * c0 + p2y * s0;
                float oz0 = p1z * c0 + p2z * s0;

                float ox1 = p1x * c1 + p2x * s1;
                float oy1 = p1y * c1 + p2y * s1;
                float oz1 = p1z * c1 + p2z * s1;

                addVertex(consumer, matrix, ax1 + ox0, ay1 + oy0, az1 + oz0, color, alpha);
                addVertex(consumer, matrix, ax1 + ox1, ay1 + oy1, az1 + oz1, color, alpha);
                addVertex(consumer, matrix, ax2 + ox1, ay2 + oy1, az2 + oz1, color, alpha);
                addVertex(consumer, matrix, ax2 + ox0, ay2 + oy0, az2 + oz0, color, alpha);

                addVertex(consumer, matrix, ax2 + ox0, ay2 + oy0, az2 + oz0, color, alpha);
                addVertex(consumer, matrix, ax2 + ox1, ay2 + oy1, az2 + oz1, color, alpha);
                addVertex(consumer, matrix, ax1 + ox1, ay1 + oy1, az1 + oz1, color, alpha);
                addVertex(consumer, matrix, ax1 + ox0, ay1 + oy0, az1 + oz0, color, alpha);
            }
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix,
                           float x, float y, float z, float[] color, float a) {
        consumer.addVertex(matrix, x, y, z).setColor(color[0], color[1], color[2], a);
    }
}
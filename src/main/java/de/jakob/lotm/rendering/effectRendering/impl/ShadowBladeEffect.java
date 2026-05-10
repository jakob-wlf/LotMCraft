package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveDirectionalEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ShadowBladeEffect extends ActiveDirectionalEffect {
    public ShadowBladeEffect(double startX, double startY, double startZ,
                             double endX, double endY, double endZ, int maxDuration) {
        super(startX, startY, startZ, endX, endY, endZ, maxDuration);
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || distance < 0.01) {
            return;
        }

        float progress = Mth.clamp(getProgress(), 0.0f, 1.0f);
        float leading = Math.min(1.0f, progress + 0.25f);
        float trailing = Math.max(0.0f, progress - 0.08f);
        float alpha = progress < 0.15f ? progress / 0.15f : (progress > 0.82f ? 1.0f - ((progress - 0.82f) / 0.18f) : 1.0f);

        Vec3 bladeStart = getInterpolatedPosition(trailing);
        Vec3 bladeEnd = getInterpolatedPosition(leading);
        Vec3 travel = bladeEnd.subtract(bladeStart);
        if (travel.lengthSqr() < 0.0001) {
            return;
        }

        Vec3 dir = travel.normalize();
        Vec3 up = Math.abs(dir.y) > 0.92 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side = dir.cross(up).normalize();
        Vec3 bladeNormal = dir.cross(side).normalize();
        float width = 0.7f + 0.12f * Mth.sin(tick * 0.2f);
        float thickness = 0.2f;

        poseStack.pushPose();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        Vec3 s1 = bladeStart.add(side.scale(width));
        Vec3 s2 = bladeStart.add(side.scale(-width));
        Vec3 e1 = bladeEnd.add(side.scale(width * 0.75f));
        Vec3 e2 = bladeEnd.add(side.scale(-width * 0.75f));

        addQuad(consumer, matrix, s1, s2, e2, e1, 0.05f, 0.05f, 0.08f, alpha);
        addQuad(consumer, matrix, s1.add(bladeNormal.scale(thickness)), s2.add(bladeNormal.scale(thickness)),
                e2.add(bladeNormal.scale(thickness)), e1.add(bladeNormal.scale(thickness)), 0.22f, 0.22f, 0.34f, alpha * 0.82f);

        for (int i = 0; i < 7; i++) {
            float t = i / 6.0f;
            Vec3 p = bladeStart.lerp(bladeEnd, t);
            float shard = width * (1.0f - t * 0.55f);
            Vec3 left = p.add(side.scale(shard));
            Vec3 right = p.add(side.scale(-shard));
            Vec3 tip = p.add(bladeNormal.scale(0.55 + (0.2 * t)));
            addTriangle(consumer, matrix, left, right, tip, 0.18f, 0.18f, 0.28f, alpha * (0.7f - (t * 0.35f)));
        }

        poseStack.popPose();
    }

    private void addQuad(VertexConsumer consumer, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                         float r, float g, float blue, float alpha) {
        addVertex(consumer, matrix, a, r, g, blue, alpha);
        addVertex(consumer, matrix, b, r, g, blue, alpha);
        addVertex(consumer, matrix, c, r, g, blue, alpha * 0.72f);
        addVertex(consumer, matrix, d, r, g, blue, alpha * 0.72f);
    }

    private void addTriangle(VertexConsumer consumer, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 c,
                             float r, float g, float blue, float alpha) {
        addVertex(consumer, matrix, a, r, g, blue, alpha);
        addVertex(consumer, matrix, b, r, g, blue, alpha);
        addVertex(consumer, matrix, c, r, g, blue, alpha * 0.6f);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 vec,
                           float r, float g, float blue, float alpha) {
        consumer.addVertex(matrix, (float) vec.x, (float) vec.y, (float) vec.z).setColor(r, g, blue, alpha);
    }
}

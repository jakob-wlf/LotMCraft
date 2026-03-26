package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class AbyssPillarEffect extends ActiveEffect {

    private static final int SEGMENTS = 12;
    private static final float MAX_HEIGHT = 7f;
    private static final float BASE_RADIUS = 0.75f;

    private final RandomSource random = RandomSource.create();
    private final List<EmberQuad> embers = new ArrayList<>();

    public AbyssPillarEffect(double x, double y, double z) {
        super(x, y, z, 200);
        for (int i = 0; i < 35; i++) {
            embers.add(new EmberQuad());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = getProgress();
        float intensity;
        if (progress < 0.15f) intensity = progress / 0.15f;
        else if (progress > 0.75f) intensity = 1f - (progress - 0.75f) / 0.25f;
        else intensity = 1f;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        renderPillarCylinder(consumer, matrix, tick, intensity);
        renderBaseRing(consumer, matrix, tick, intensity);
        renderEmbers(consumer, matrix, tick, intensity);

        poseStack.popPose();
    }

    private void renderPillarCylinder(VertexConsumer consumer, Matrix4f matrix, float tick, float intensity) {
        int rings = 10;
        float rotation = tick * 0.025f;

        for (int h = 0; h < rings; h++) {
            float y1 = (h / (float) rings) * MAX_HEIGHT;
            float y2 = ((h + 1) / (float) rings) * MAX_HEIGHT;
            float heightFactor = 1f - (float) h / rings;
            float r1 = BASE_RADIUS * (1f - (float) h / rings * 0.45f);
            float r2 = BASE_RADIUS * (1f - (float) (h + 1) / rings * 0.45f);

            float green = 0.55f + 0.45f * heightFactor;
            float red = 0.04f * heightFactor;
            float blue = 0.02f;
            float alpha = intensity * 0.88f * heightFactor;

            for (int i = 0; i < SEGMENTS; i++) {
                float angle1 = (i / (float) SEGMENTS) * Mth.TWO_PI + rotation;
                float angle2 = ((i + 1) / (float) SEGMENTS) * Mth.TWO_PI + rotation;

                float noise1 = Mth.sin(angle1 * 3f + tick * 0.12f + h * 0.5f) * 0.13f;
                float noise2 = Mth.sin(angle2 * 3f + tick * 0.12f + h * 0.5f) * 0.13f;

                float x1 = Mth.cos(angle1) * (r1 + noise1);
                float z1 = Mth.sin(angle1) * (r1 + noise1);
                float x2 = Mth.cos(angle2) * (r2 + noise2);
                float z2 = Mth.sin(angle2) * (r2 + noise2);

                addVertex(consumer, matrix, x1, y1, z1, red, green, blue, alpha);
                addVertex(consumer, matrix, x2, y1, z2, red, green, blue, alpha);
                addVertex(consumer, matrix, x2, y2, z2, red, green * 0.45f, blue, alpha * 0.55f);
                addVertex(consumer, matrix, x1, y2, z1, red, green * 0.45f, blue, alpha * 0.55f);
            }
        }
    }

    private void renderBaseRing(VertexConsumer consumer, Matrix4f matrix, float tick, float intensity) {
        float pulse = 1f + 0.3f * Mth.sin(tick * 0.12f);
        int segments = 24;
        float innerR = 0.2f;
        float outerR = BASE_RADIUS * 2.0f * pulse;

        for (int i = 0; i < segments; i++) {
            float a1 = (i / (float) segments) * Mth.TWO_PI;
            float a2 = ((i + 1) / (float) segments) * Mth.TWO_PI;

            float ix1 = Mth.cos(a1) * innerR, iz1 = Mth.sin(a1) * innerR;
            float ox1 = Mth.cos(a1) * outerR, oz1 = Mth.sin(a1) * outerR;
            float ix2 = Mth.cos(a2) * innerR, iz2 = Mth.sin(a2) * innerR;
            float ox2 = Mth.cos(a2) * outerR, oz2 = Mth.sin(a2) * outerR;

            addVertex(consumer, matrix, ix1, 0.04f, iz1, 0f, 1f, 0.08f, intensity * 0.95f);
            addVertex(consumer, matrix, ix2, 0.04f, iz2, 0f, 1f, 0.08f, intensity * 0.95f);
            addVertex(consumer, matrix, ox2, 0.04f, oz2, 0f, 0.6f, 0.03f, intensity * 0.3f);
            addVertex(consumer, matrix, ox1, 0.04f, oz1, 0f, 0.6f, 0.03f, intensity * 0.3f);
        }
    }

    private void renderEmbers(VertexConsumer consumer, Matrix4f matrix, float tick, float intensity) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        for (EmberQuad ember : embers) {
            ember.update(tick);
            if (ember.alpha <= 0.01f) continue;

            float ex = ember.ex, ey = ember.ey, ez = ember.ez;
            Vec3 toCamera = new Vec3(
                    cameraPos.x - (x + ex),
                    cameraPos.y - (y + ey),
                    cameraPos.z - (z + ez)).normalize();

            Vec3 up = new Vec3(0, 1, 0);
            Vec3 right = toCamera.cross(up).normalize().scale(ember.size);
            Vec3 upv = right.cross(toCamera).normalize().scale(ember.size);

            float a = ember.alpha * intensity;
            addVertex(consumer, matrix, (float) (ex - right.x - upv.x), (float) (ey - right.y - upv.y), (float) (ez - right.z - upv.z), 0.05f, 1f, 0.05f, a);
            addVertex(consumer, matrix, (float) (ex - right.x + upv.x), (float) (ey - right.y + upv.y), (float) (ez - right.z + upv.z), 0.05f, 1f, 0.05f, a);
            addVertex(consumer, matrix, (float) (ex + right.x + upv.x), (float) (ey + right.y + upv.y), (float) (ez + right.z + upv.z), 0f, 0.6f, 0f, a * 0.5f);
            addVertex(consumer, matrix, (float) (ex + right.x - upv.x), (float) (ey + right.y - upv.y), (float) (ez + right.z - upv.z), 0f, 0.6f, 0f, a * 0.5f);
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix,
                           float x, float y, float z, float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }

    private class EmberQuad {
        float ex, ey, ez;
        float alpha, size, speed, angle, dist, phaseOffset;

        EmberQuad() {
            reset(true);
        }

        void reset(boolean randomHeight) {
            angle = random.nextFloat() * Mth.TWO_PI;
            dist = random.nextFloat() * BASE_RADIUS * 0.9f;
            ey = randomHeight ? random.nextFloat() * MAX_HEIGHT : 0f;
            size = 0.04f + random.nextFloat() * 0.07f;
            speed = 0.035f + random.nextFloat() * 0.045f;
            phaseOffset = random.nextFloat() * Mth.TWO_PI;
            alpha = 0f;
        }

        void update(float tick) {
            ey += speed;
            angle += 0.018f;
            ex = Mth.cos(angle) * (dist + Mth.sin(tick * 0.09f + phaseOffset) * 0.18f);
            ez = Mth.sin(angle) * (dist + Mth.sin(tick * 0.09f + phaseOffset) * 0.18f);
            float lifeProgress = ey / MAX_HEIGHT;
            alpha = 1f - lifeProgress;
            if (ey >= MAX_HEIGHT) reset(false);
        }
    }
}

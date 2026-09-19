package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class RingPulseEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<RingParticle> particles = new ArrayList<>();

    private float COLOR_R = 0.4f;
    private float COLOR_G = 0.85f;
    private float COLOR_B = 1.0f;

    private static final int PARTICLE_COUNT = 16;
    private static final float MAX_RADIUS = 4f;
    private static final float RING_THICKNESS = 0.1f;
    private static final float RING_Y = 0.05f;

    private boolean initialized = false;

    public RingPulseEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);
    }

    private void initFromParams() {
        if (initialized) return;
        initialized = true;

        float[] params = getParams();
        if (params[0] >= 0 && params[1] >= 0 && params[2] >= 0
                && params[0] <= 1 && params[1] <= 1 && params[2] <= 1) {
            COLOR_R = params[0];
            COLOR_G = params[1];
            COLOR_B = params[2];
        }

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new RingParticle(i));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        initFromParams();

        float progress = getProgress();

        float radius = progress * MAX_RADIUS;

        float alpha;
        if (progress < 0.15f) {
            alpha = progress / 0.15f;
        } else if (progress > 0.75f) {
            alpha = 1f - ((progress - 0.75f) / 0.25f);
        } else {
            alpha = 1f;
        }
        alpha = Mth.clamp(alpha, 0f, 1f);

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        renderRing(poseStack, bufferSource, radius, alpha);
        renderParticles(poseStack, bufferSource, progress, alpha);

        poseStack.popPose();
    }

    private void renderRing(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float radius, float alpha) {
        if (alpha <= 0f || radius <= 0f) return;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        int segments = 32;
        float inner = Math.max(0f, radius - RING_THICKNESS);
        float outer = radius + RING_THICKNESS;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float xIn1 = Mth.cos(angle1) * inner;
            float zIn1 = Mth.sin(angle1) * inner;
            float xIn2 = Mth.cos(angle2) * inner;
            float zIn2 = Mth.sin(angle2) * inner;

            float xOut1 = Mth.cos(angle1) * outer;
            float zOut1 = Mth.sin(angle1) * outer;
            float xOut2 = Mth.cos(angle2) * outer;
            float zOut2 = Mth.sin(angle2) * outer;

            addVertex(consumer, matrix, xIn1, RING_Y, zIn1,
                    COLOR_R * 0.6f, COLOR_G * 0.6f, COLOR_B * 0.6f, alpha * 0.3f);
            addVertex(consumer, matrix, xIn2, RING_Y, zIn2,
                    COLOR_R * 0.6f, COLOR_G * 0.6f, COLOR_B * 0.6f, alpha * 0.3f);
            addVertex(consumer, matrix, xOut2, RING_Y, zOut2,
                    1f, 1f, 1f, alpha * 0.9f);
            addVertex(consumer, matrix, xOut1, RING_Y, zOut1,
                    1f, 1f, 1f, alpha * 0.9f);
        }
    }

    private void renderParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float ringAlpha) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (RingParticle particle : particles) {
            particle.update(progress);

            if (particle.alpha <= 0f) continue;

            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, particle.r, particle.g, particle.b, particle.alpha * ringAlpha);
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                            float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a);
    }

    private void renderBillboardQuad(VertexConsumer consumer, Matrix4f matrix,
                                      float x, float y, float z, float size,
                                      float r, float g, float b, float a) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        Vec3 toCamera = new Vec3(
                cameraPos.x - (getX() + x),
                cameraPos.y - (getY() + y),
                cameraPos.z - (getZ() + z)
        ).normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCamera.cross(up).normalize().scale(size);
        up = right.cross(toCamera).normalize().scale(size);

        addVertex(consumer, matrix,
                (float) (x - right.x - up.x), (float) (y - right.y - up.y), (float) (z - right.z - up.z),
                r, g, b, a);
        addVertex(consumer, matrix,
                (float) (x - right.x + up.x), (float) (y - right.y + up.y), (float) (z - right.z + up.z),
                r, g, b, a);
        addVertex(consumer, matrix,
                (float) (x + right.x + up.x), (float) (y + right.y + up.y), (float) (z + right.z + up.z),
                r, g, b, a);
        addVertex(consumer, matrix,
                (float) (x + right.x - up.x), (float) (y + right.y - up.y), (float) (z + right.z - up.z),
                r, g, b, a);
    }

    
    private class RingParticle {
        float x, y, z;
        float size;
        float alpha;
        float r, g, b;
        final float angle;
        final float bobPhase;

        RingParticle(int index) {
            this.angle = (float) (index * Math.PI * 2 / PARTICLE_COUNT) + random.nextFloat() * 0.2f;
            this.size = 0.06f + random.nextFloat() * 0.05f;
            this.bobPhase = random.nextFloat() * Mth.TWO_PI;

            if (random.nextFloat() < 0.35f) {
                this.r = 1f;
                this.g = 1f;
                this.b = 1f;
            } else {
                this.r = COLOR_R;
                this.g = COLOR_G;
                this.b = COLOR_B;
            }
        }

        void update(float progress) {
            float particleRadius = progress * MAX_RADIUS * 1.05f;

            this.x = Mth.cos(angle) * particleRadius;
            this.z = Mth.sin(angle) * particleRadius;
            this.y = RING_Y + Mth.sin(progress * Mth.TWO_PI * 3f + bobPhase) * 0.08f;

            if (progress < 0.15f) {
                this.alpha = progress / 0.15f;
            } else if (progress > 0.7f) {
                this.alpha = Mth.clamp(1f - ((progress - 0.7f) / 0.3f), 0f, 1f);
            } else {
                this.alpha = 1f;
            }
        }
    }
}
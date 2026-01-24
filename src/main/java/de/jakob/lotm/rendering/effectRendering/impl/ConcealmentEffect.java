package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ConcealmentEffect extends ActiveEffect {

    private static final float MAX_RADIUS = 27f;
    private static final int DURATION_TICKS = 20 * 5; // 3 seconds

    private final RandomSource random = RandomSource.create();
    private final List<ConcealmentParticle> particles = new ArrayList<>();
    private final List<LargeMistParticle> largeMistParticles = new ArrayList<>();
    private float opacity;

    public ConcealmentEffect(double x, double y, double z) {
        super(x, y, z, DURATION_TICKS);

        // Initialize particles
        for (int i = 0; i < 120; i++) {
            particles.add(new ConcealmentParticle());
        }

        // Initialize larger, slower mist particles
        for (int i = 0; i < 40; i++) {
            largeMistParticles.add(new LargeMistParticle());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        float progress = tick / maxDuration;

        // Calculate expanding radius with smooth easing
        float radius = MAX_RADIUS * easeOutQuad(progress);

        // Opacity fades in quickly, then fades out gradually
        if (progress < 0.15f) {
            opacity = progress / 0.15f;
        } else {
            opacity = 1f - ((progress - 0.15f) / 0.85f);
        }
        opacity = Mth.clamp(opacity, 0f, 1f);

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render main sphere
        renderConcealmentSphere(poseStack, bufferSource, radius, opacity);

        // Render inner glow layers
        renderInnerGlow(poseStack, bufferSource, radius, opacity);

        // Render particles
        renderParticles(poseStack, bufferSource, progress, radius, opacity);

        // Render large mist particles
        renderLargeMistParticles(poseStack, bufferSource, progress, radius, opacity);

        // Render mist effect
        renderMistLayer(poseStack, bufferSource, radius, opacity);

        poseStack.popPose();
    }

    private void renderConcealmentSphere(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                         float radius, float opacity) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        int latSegments = 20;
        int lonSegments = 28;

        for (int lat = 0; lat < latSegments; lat++) {
            float theta1 = (float) (lat * Math.PI / latSegments);
            float theta2 = (float) ((lat + 1) * Math.PI / latSegments);

            for (int lon = 0; lon < lonSegments; lon++) {
                float phi1 = (float) (lon * 2 * Math.PI / lonSegments);
                float phi2 = (float) ((lon + 1) * 2 * Math.PI / lonSegments);

                float x1 = radius * Mth.sin(theta1) * Mth.cos(phi1);
                float y1 = radius * Mth.cos(theta1);
                float z1 = radius * Mth.sin(theta1) * Mth.sin(phi1);

                float x2 = radius * Mth.sin(theta1) * Mth.cos(phi2);
                float y2 = radius * Mth.cos(theta1);
                float z2 = radius * Mth.sin(theta1) * Mth.sin(phi2);

                float x3 = radius * Mth.sin(theta2) * Mth.cos(phi2);
                float y3 = radius * Mth.cos(theta2);
                float z3 = radius * Mth.sin(theta2) * Mth.sin(phi2);

                float x4 = radius * Mth.sin(theta2) * Mth.cos(phi1);
                float y4 = radius * Mth.cos(theta2);
                float z4 = radius * Mth.sin(theta2) * Mth.sin(phi1);

                // White/gray color with translucency
                float grayValue = 0.85f + Mth.sin(currentTick * 0.05f + lat * 0.3f) * 0.1f;
                float alpha = opacity * 0.5f;

                // Render front-facing (outside view)
                addVertex(consumer, matrix, x1, y1, z1, grayValue, grayValue, grayValue + 0.05f, alpha);
                addVertex(consumer, matrix, x2, y2, z2, grayValue, grayValue, grayValue + 0.05f, alpha);
                addVertex(consumer, matrix, x3, y3, z3, grayValue, grayValue, grayValue + 0.05f, alpha);
                addVertex(consumer, matrix, x4, y4, z4, grayValue, grayValue, grayValue + 0.05f, alpha);

                // Render back-facing (inside view) - reversed winding order
                addVertex(consumer, matrix, x1, y1, z1, grayValue, grayValue, grayValue + 0.05f, alpha);
                addVertex(consumer, matrix, x4, y4, z4, grayValue, grayValue, grayValue + 0.05f, alpha);
                addVertex(consumer, matrix, x3, y3, z3, grayValue, grayValue, grayValue + 0.05f, alpha);
                addVertex(consumer, matrix, x2, y2, z2, grayValue, grayValue, grayValue + 0.05f, alpha);
            }
        }
    }

    private void renderInnerGlow(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                 float radius, float opacity) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Multiple layers for depth
        for (int layer = 0; layer < 3; layer++) {
            float layerRadius = radius * (0.7f + layer * 0.1f);
            float layerAlpha = opacity * (0.3f - layer * 0.08f);

            int segments = 16;
            for (int lat = 0; lat < segments; lat++) {
                float theta1 = (float) (lat * Math.PI / segments);
                float theta2 = (float) ((lat + 1) * Math.PI / segments);

                for (int lon = 0; lon < segments * 2; lon++) {
                    float phi1 = (float) (lon * 2 * Math.PI / (segments * 2));
                    float phi2 = (float) ((lon + 1) * 2 * Math.PI / (segments * 2));

                    float x1 = layerRadius * Mth.sin(theta1) * Mth.cos(phi1);
                    float y1 = layerRadius * Mth.cos(theta1);
                    float z1 = layerRadius * Mth.sin(theta1) * Mth.sin(phi1);

                    float x2 = layerRadius * Mth.sin(theta1) * Mth.cos(phi2);
                    float y2 = layerRadius * Mth.cos(theta1);
                    float z2 = layerRadius * Mth.sin(theta1) * Mth.sin(phi2);

                    float x3 = layerRadius * Mth.sin(theta2) * Mth.cos(phi2);
                    float y3 = layerRadius * Mth.cos(theta2);
                    float z3 = layerRadius * Mth.sin(theta2) * Mth.sin(phi2);

                    float x4 = layerRadius * Mth.sin(theta2) * Mth.cos(phi1);
                    float y4 = layerRadius * Mth.cos(theta2);
                    float z4 = layerRadius * Mth.sin(theta2) * Mth.sin(phi1);

                    // Front-facing
                    addVertex(consumer, matrix, x1, y1, z1, 0.95f, 0.95f, 0.98f, layerAlpha);
                    addVertex(consumer, matrix, x2, y2, z2, 0.95f, 0.95f, 0.98f, layerAlpha);
                    addVertex(consumer, matrix, x3, y3, z3, 0.95f, 0.95f, 0.98f, layerAlpha);
                    addVertex(consumer, matrix, x4, y4, z4, 0.95f, 0.95f, 0.98f, layerAlpha);

                    // Back-facing
                    addVertex(consumer, matrix, x1, y1, z1, 0.95f, 0.95f, 0.98f, layerAlpha);
                    addVertex(consumer, matrix, x4, y4, z4, 0.95f, 0.95f, 0.98f, layerAlpha);
                    addVertex(consumer, matrix, x3, y3, z3, 0.95f, 0.95f, 0.98f, layerAlpha);
                    addVertex(consumer, matrix, x2, y2, z2, 0.95f, 0.95f, 0.98f, layerAlpha);
                }
            }
        }
    }

    private void renderParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                 float progress, float radius, float opacity) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (ConcealmentParticle particle : particles) {
            particle.update(progress, radius);

            if (particle.alpha <= 0f) continue;

            float distance = (float) Math.sqrt(particle.x * particle.x +
                    particle.y * particle.y +
                    particle.z * particle.z);

            // Only show particles near the sphere surface
            if (Math.abs(distance - radius) > 4f) continue;

            float particleAlpha = particle.alpha * opacity;
            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, 0.9f, 0.9f, 0.95f, particleAlpha);
        }
    }

    private void renderLargeMistParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                          float progress, float radius, float opacity) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (LargeMistParticle particle : largeMistParticles) {
            particle.update(progress, radius);

            if (particle.alpha <= 0f) continue;

            float distance = (float) Math.sqrt(particle.x * particle.x +
                    particle.y * particle.y +
                    particle.z * particle.z);

            // Show particles in a wider range around the sphere
            if (Math.abs(distance - radius) > 6f) continue;

            float particleAlpha = particle.alpha * opacity;
            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, 0.88f, 0.88f, 0.93f, particleAlpha);
        }
    }

    private void renderMistLayer(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                 float radius, float opacity) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Ground-level mist ring
        float mistRadius = radius * 1.1f;
        int segments = 48;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1Inner = Mth.cos(angle1) * radius * 0.95f;
            float z1Inner = Mth.sin(angle1) * radius * 0.95f;
            float x1Outer = Mth.cos(angle1) * mistRadius;
            float z1Outer = Mth.sin(angle1) * mistRadius;

            float x2Inner = Mth.cos(angle2) * radius * 0.95f;
            float z2Inner = Mth.sin(angle2) * radius * 0.95f;
            float x2Outer = Mth.cos(angle2) * mistRadius;
            float z2Outer = Mth.sin(angle2) * mistRadius;

            float yBase = 0.05f;
            float alphaInner = opacity * 0.35f;
            float alphaOuter = 0f;

            addVertex(consumer, matrix, x1Inner, yBase, z1Inner, 0.88f, 0.88f, 0.92f, alphaInner);
            addVertex(consumer, matrix, x2Inner, yBase, z2Inner, 0.88f, 0.88f, 0.92f, alphaInner);
            addVertex(consumer, matrix, x2Outer, yBase, z2Outer, 0.85f, 0.85f, 0.9f, alphaOuter);
            addVertex(consumer, matrix, x1Outer, yBase, z1Outer, 0.85f, 0.85f, 0.9f, alphaOuter);
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
                cameraPos.x - (this.x + x),
                cameraPos.y - (this.y + y),
                cameraPos.z - (this.z + z)
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

    private float easeOutQuad(float t) {
        return 1f - (1f - t) * (1f - t);
    }

    private class ConcealmentParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float baseAngle;
        float orbitSpeed;

        ConcealmentParticle() {
            random.setSeed(System.nanoTime());
            this.baseAngle = random.nextFloat() * Mth.TWO_PI;
            this.orbitSpeed = 0.01f + random.nextFloat() * 0.02f;
            this.size = 0.08f + random.nextFloat() * 0.12f;

            float elevation = random.nextFloat() * Mth.PI;
            this.vx = (random.nextFloat() - 0.5f) * 0.05f;
            this.vy = (random.nextFloat() - 0.5f) * 0.05f;
            this.vz = (random.nextFloat() - 0.5f) * 0.05f;
        }

        void update(float progress, float radius) {
            baseAngle += orbitSpeed;

            float elevation = random.nextFloat() * Mth.PI;

            // Position particles around the sphere surface
            this.x = Mth.cos(baseAngle) * radius * Mth.sin(elevation);
            this.y = Mth.cos(elevation) * radius;
            this.z = Mth.sin(baseAngle) * radius * Mth.sin(elevation);

            // Add some drift
            this.x += vx * currentTick * 0.1f;
            this.y += vy * currentTick * 0.1f;
            this.z += vz * currentTick * 0.1f;

            // Fade in and out with the effect
            this.alpha = 0.6f + random.nextFloat() * 0.3f;
            this.alpha *= Mth.sin(progress * Mth.PI);
        }
    }

    private class LargeMistParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float baseAngle;
        float orbitSpeed;
        float pulseOffset;

        LargeMistParticle() {
            random.setSeed(System.nanoTime());
            this.baseAngle = random.nextFloat() * Mth.TWO_PI;
            this.orbitSpeed = 0.005f + random.nextFloat() * 0.008f; // Slower than regular particles
            this.size = 0.25f + random.nextFloat() * 0.35f; // Much bigger
            this.pulseOffset = random.nextFloat() * Mth.TWO_PI;

            float elevation = random.nextFloat() * Mth.PI;
            this.vx = (random.nextFloat() - 0.5f) * 0.02f; // Slower drift
            this.vy = (random.nextFloat() - 0.5f) * 0.02f;
            this.vz = (random.nextFloat() - 0.5f) * 0.02f;
        }

        void update(float progress, float radius) {
            baseAngle += orbitSpeed;

            float elevation = random.nextFloat() * Mth.PI;

            // Position particles around the sphere surface
            this.x = Mth.cos(baseAngle) * radius * Mth.sin(elevation);
            this.y = Mth.cos(elevation) * radius;
            this.z = Mth.sin(baseAngle) * radius * Mth.sin(elevation);

            // Add some slow drift
            this.x += vx * currentTick * 0.15f;
            this.y += vy * currentTick * 0.15f;
            this.z += vz * currentTick * 0.15f;

            // Gentle pulsing alpha
            float pulse = Mth.sin(currentTick * 0.03f + pulseOffset) * 0.15f;
            this.alpha = 0.4f + pulse;
            this.alpha *= Mth.sin(progress * Mth.PI);
        }
    }
}
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

public class DeceptionEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<DeceptionCircle> circles = new ArrayList<>();
    private final List<DeceptionParticle> particles = new ArrayList<>();

    private static final float MAX_CIRCLE_RADIUS = 8f;

    public DeceptionEffect(double x, double y, double z) {
        super(x, y, z, 20 * 4); // 4 seconds duration

        // Create overlapping circles with different colors and timings
        circles.add(new DeceptionCircle(0.2f, 0.4f, 0.8f, 0f, 0.8f)); // Blue
        circles.add(new DeceptionCircle(0.3f, 0.8f, 0.5f, 0.15f, 1.0f)); // Green
        circles.add(new DeceptionCircle(0.6f, 0.3f, 0.8f, 0.3f, 0.9f)); // Purple
        circles.add(new DeceptionCircle(0.2f, 0.7f, 0.7f, 0.45f, 0.7f)); // Cyan
        circles.add(new DeceptionCircle(0.5f, 0.4f, 0.9f, 0.6f, 0.85f)); // Purple-Blue

        // Initialize particles
        for (int i = 0; i < 80; i++) {
            particles.add(new DeceptionParticle());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render circles
        for (DeceptionCircle circle : circles) {
            circle.update(progress);
            renderCircle(poseStack, bufferSource, circle, progress);
        }

        // Render particles
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        
        for (DeceptionParticle particle : particles) {
            particle.update(progress);
            if (particle.alpha > 0f) {
                renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, particle.r, particle.g, particle.b, particle.alpha);
            }
        }

        poseStack.popPose();
    }

    private void renderCircle(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, 
                             DeceptionCircle circle, float globalProgress) {
        if (circle.alpha <= 0f) return;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float radius = circle.radius;
        int segments = 48;
        float yOffset = 0.05f + circle.delay * 0.1f; // Slight height variation

        // Render filled circle as triangle fan
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * radius;
            float z1 = Mth.sin(angle1) * radius;
            float x2 = Mth.cos(angle2) * radius;
            float z2 = Mth.sin(angle2) * radius;

            // Create gradient from center to edge
            float edgeAlpha = circle.alpha * 0.5f;
            float centerAlpha = circle.alpha;

            // Triangle: center -> point1 -> point2
            addVertex(consumer, matrix, 0, yOffset, 0, 
                circle.r, circle.g, circle.b, centerAlpha);
            addVertex(consumer, matrix, x1, yOffset, z1, 
                circle.r, circle.g, circle.b, edgeAlpha);
            addVertex(consumer, matrix, x2, yOffset, z2, 
                circle.r, circle.g, circle.b, edgeAlpha);
        }

        // Add glowing edge ring
        float edgeThickness = 0.3f;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1Inner = Mth.cos(angle1) * (radius - edgeThickness);
            float z1Inner = Mth.sin(angle1) * (radius - edgeThickness);
            float x1Outer = Mth.cos(angle1) * (radius + edgeThickness);
            float z1Outer = Mth.sin(angle1) * (radius + edgeThickness);

            float x2Inner = Mth.cos(angle2) * (radius - edgeThickness);
            float z2Inner = Mth.sin(angle2) * (radius - edgeThickness);
            float x2Outer = Mth.cos(angle2) * (radius + edgeThickness);
            float z2Outer = Mth.sin(angle2) * (radius + edgeThickness);

            float glowAlpha = circle.alpha * 0.8f;

            addVertex(consumer, matrix, x1Inner, yOffset + 0.01f, z1Inner, 
                circle.r, circle.g, circle.b, glowAlpha);
            addVertex(consumer, matrix, x2Inner, yOffset + 0.01f, z2Inner, 
                circle.r, circle.g, circle.b, glowAlpha);
            addVertex(consumer, matrix, x2Outer, yOffset + 0.01f, z2Outer, 
                circle.r, circle.g, circle.b, 0f);
            addVertex(consumer, matrix, x1Outer, yOffset + 0.01f, z1Outer, 
                circle.r, circle.g, circle.b, 0f);
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

    private class DeceptionCircle {
        float r, g, b;
        float delay;
        float maxAlpha;
        float radius;
        float alpha;
        float expansionSpeed;

        DeceptionCircle(float r, float g, float b, float delay, float maxAlpha) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.delay = delay;
            this.maxAlpha = maxAlpha;
            this.radius = 0f;
            this.alpha = 0f;
            this.expansionSpeed = 0.7f + random.nextFloat() * 0.4f;
        }

        void update(float progress) {
            // Account for delay
            float adjustedProgress = Mth.clamp((progress - delay) / (1f - delay), 0f, 1f);

            // Expand with easing
            float expansion = (float) Math.pow(adjustedProgress, 0.7);
            this.radius = MAX_CIRCLE_RADIUS * expansion * expansionSpeed;

            // Fade in quickly, fade out slowly
            if (adjustedProgress < 0.2f) {
                this.alpha = maxAlpha * (adjustedProgress / 0.2f);
            } else {
                float fadeProgress = (adjustedProgress - 0.2f) / 0.8f;
                this.alpha = maxAlpha * (1f - fadeProgress * 0.8f);
            }

            // Pulse effect
            this.alpha *= (1f + 0.1f * Mth.sin(progress * 10f + delay * 20f));
            this.alpha = Mth.clamp(this.alpha, 0f, maxAlpha);
        }
    }

    private class DeceptionParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float r, g, b;
        float lifetime;
        float spawnDelay;

        DeceptionParticle() {
            random.setSeed(System.nanoTime());
            this.spawnDelay = random.nextFloat() * 0.5f;
            respawn();
        }

        void respawn() {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * 2f;

            this.x = Mth.cos(angle) * dist;
            this.y = random.nextFloat() * 0.5f;
            this.z = Mth.sin(angle) * dist;

            float speed = 0.08f + random.nextFloat() * 0.1f;
            float outwardAngle = random.nextFloat() * Mth.TWO_PI;
            this.vx = Mth.cos(outwardAngle) * speed;
            this.vy = random.nextFloat() * speed * 0.3f + 0.02f;
            this.vz = Mth.sin(outwardAngle) * speed;

            this.size = 0.08f + random.nextFloat() * 0.1f;
            this.lifetime = random.nextFloat();

            // Random color: green, blue, or purple
            int colorChoice = random.nextInt(3);
            switch (colorChoice) {
                case 0: // Green
                    this.r = 0.3f;
                    this.g = 0.8f;
                    this.b = 0.5f;
                    break;
                case 1: // Blue
                    this.r = 0.2f;
                    this.g = 0.4f;
                    this.b = 0.9f;
                    break;
                case 2: // Purple
                    this.r = 0.6f;
                    this.g = 0.3f;
                    this.b = 0.9f;
                    break;
            }
        }

        void update(float progress) {
            // Wait for spawn delay
            if (progress < spawnDelay) {
                this.alpha = 0f;
                return;
            }

            float adjustedProgress = (progress - spawnDelay) / (1f - spawnDelay);

            this.x += vx;
            this.y += vy;
            this.z += vz;

            // Fade in and out
            if (adjustedProgress < 0.1f) {
                this.alpha = adjustedProgress / 0.1f;
            } else if (adjustedProgress > 0.7f) {
                this.alpha = 1f - ((adjustedProgress - 0.7f) / 0.3f);
            } else {
                this.alpha = 1f;
            }

            this.alpha *= 0.6f + 0.2f * Mth.sin(lifetime * 20f + progress * 15f);
            this.alpha = Mth.clamp(this.alpha, 0f, 0.8f);

            // Respawn if moved too far
            float distance = (float) Math.sqrt(x * x + y * y + z * z);
            if (distance > MAX_CIRCLE_RADIUS * 1.2f) {
                respawn();
            }
        }
    }
}
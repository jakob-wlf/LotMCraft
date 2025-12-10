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

public class LoopholeEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<ContradictionLine> contradictionLines = new ArrayList<>();
    private final List<LoopPath> loopPaths = new ArrayList<>();
    private final List<DeceitParticle> deceitParticles = new ArrayList<>();
    private final List<RealityFracture> realityFractures = new ArrayList<>();
    private final List<BubbleSegment> bubbleSegments = new ArrayList<>();

    private static final float BUBBLE_RADIUS = 6f;
    private float bubbleIntensity = 0f;
    private float contradictionPulse = 0f;

    public LoopholeEffect(double x, double y, double z) {
        super(x, y, z, 20 * 14); // 6 seconds duration

        // Create contradiction lines that criss-cross through the bubble
        for (int i = 0; i < 40; i++) {
            contradictionLines.add(new ContradictionLine());
        }

        // Create looping paths that twist back on themselves
        for (int i = 0; i < 12; i++) {
            loopPaths.add(new LoopPath());
        }

        // Create deceitful particles that flicker and mislead
        for (int i = 0; i < 150; i++) {
            deceitParticles.add(new DeceitParticle());
        }

        // Create reality fractures - cracks in logic
        for (int i = 0; i < 25; i++) {
            realityFractures.add(new RealityFracture());
        }

        // Create bubble segments for more organic look
        int latSegments = 20;
        int lonSegments = 24;
        for (int lat = 0; lat < latSegments; lat++) {
            for (int lon = 0; lon < lonSegments; lon++) {
                bubbleSegments.add(new BubbleSegment(lat, lon, latSegments, lonSegments));
            }
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;

        // Bubble grows quickly, sustains, then collapses
        if (progress < 0.2f) {
            bubbleIntensity = progress / 0.2f;
        } else if (progress > 0.85f) {
            bubbleIntensity = 1f - ((progress - 0.85f) / 0.15f);
        } else {
            bubbleIntensity = 1f;
        }

        contradictionPulse = Mth.sin(tick * 0.15f) * 0.5f + 0.5f;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render in specific order for proper blending
        renderBubbleShell(poseStack, bufferSource, progress);
        renderBubbleSurface(poseStack, bufferSource, progress);
        renderRealityFractures(poseStack, bufferSource, progress);
        renderContradictionLines(poseStack, bufferSource, progress);
        renderLoopPaths(poseStack, bufferSource, progress);
        renderDeceitParticles(poseStack, bufferSource, progress);
        renderInnerVoid(poseStack, bufferSource, progress);
        renderDistortionWaves(poseStack, bufferSource, progress);
        renderBubbleHighlights(poseStack, bufferSource, progress);

        poseStack.popPose();
    }

    private void renderBubbleShell(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float radius = BUBBLE_RADIUS * bubbleIntensity;
        int latSegments = 24;
        int lonSegments = 32;

        for (int lat = 0; lat < latSegments; lat++) {
            float theta1 = (float) (lat * Math.PI / latSegments);
            float theta2 = (float) ((lat + 1) * Math.PI / latSegments);

            for (int lon = 0; lon < lonSegments; lon++) {
                float phi1 = (float) (lon * 2 * Math.PI / lonSegments);
                float phi2 = (float) ((lon + 1) * 2 * Math.PI / lonSegments);

                // Multiple layers of distortion for bubbly effect
                float wobble1 = Mth.sin(phi1 * 4f + currentTick * 0.08f) * 0.12f;
                wobble1 += Mth.sin(theta1 * 6f + currentTick * 0.12f) * 0.08f;
                wobble1 += Mth.sin((phi1 + theta1) * 3f - currentTick * 0.1f) * 0.1f;

                float wobble2 = Mth.sin(phi2 * 4f + currentTick * 0.08f) * 0.12f;
                wobble2 += Mth.sin(theta2 * 6f + currentTick * 0.12f) * 0.08f;
                wobble2 += Mth.sin((phi2 + theta2) * 3f - currentTick * 0.1f) * 0.1f;

                float r1 = radius + wobble1;
                float r2 = radius + wobble2;

                float x1 = r1 * Mth.sin(theta1) * Mth.cos(phi1);
                float y1 = r1 * Mth.cos(theta1);
                float z1 = r1 * Mth.sin(theta1) * Mth.sin(phi1);

                float x2 = r2 * Mth.sin(theta1) * Mth.cos(phi2);
                float y2 = r2 * Mth.cos(theta1);
                float z2 = r2 * Mth.sin(theta1) * Mth.sin(phi2);

                float x3 = r2 * Mth.sin(theta2) * Mth.cos(phi2);
                float y3 = r2 * Mth.cos(theta2);
                float z3 = r2 * Mth.sin(theta2) * Mth.sin(phi2);

                float x4 = r1 * Mth.sin(theta2) * Mth.cos(phi1);
                float y4 = r1 * Mth.cos(theta2);
                float z4 = r1 * Mth.sin(theta2) * Mth.sin(phi1);

                // Iridescent bubble colors - purple/pink/blue shimmer
                float shimmer = Mth.sin(phi1 * 2f + currentTick * 0.1f) * 0.5f + 0.5f;
                float r = 0.4f + shimmer * 0.3f;
                float g = 0.3f + shimmer * 0.2f;
                float b = 0.6f + shimmer * 0.3f;

                float alpha = 0.35f * bubbleIntensity * (0.85f + 0.15f * contradictionPulse);

                addVertex(consumer, matrix, x1, y1, z1, r, g, b, alpha);
                addVertex(consumer, matrix, x2, y2, z2, r, g, b, alpha);
                addVertex(consumer, matrix, x3, y3, z3, r, g, b, alpha);
                addVertex(consumer, matrix, x4, y4, z4, r, g, b, alpha);
            }
        }
    }

    private void renderBubbleSurface(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Add shiny surface reflections moving across the bubble
        for (BubbleSegment segment : bubbleSegments) {
            segment.update(progress, bubbleIntensity);

            if (segment.brightness <= 0.1f) continue;

            float radius = BUBBLE_RADIUS * bubbleIntensity;
            float theta = segment.theta;
            float phi = segment.phi + currentTick * 0.05f;

            // Wobble matching the main bubble
            float wobble = Mth.sin(phi * 4f + currentTick * 0.08f) * 0.12f;
            wobble += Mth.sin(theta * 6f + currentTick * 0.12f) * 0.08f;
            wobble += Mth.sin((phi + theta) * 3f - currentTick * 0.1f) * 0.1f;

            float r = radius + wobble + 0.05f; // Slightly outside the main bubble

            float x = r * Mth.sin(theta) * Mth.cos(phi);
            float y = r * Mth.cos(theta);
            float z = r * Mth.sin(theta) * Mth.sin(phi);

            float size = 0.3f * segment.size;
            float brightness = segment.brightness * bubbleIntensity;

            // Bright shimmery white highlights
            renderBillboardQuad(consumer, matrix, x, y, z, size,
                    1f, 0.95f, 1f, brightness * 0.6f);
        }
    }

    private void renderBubbleHighlights(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Add glossy highlight bands that move across the bubble surface
        int bandCount = 3;
        for (int b = 0; b < bandCount; b++) {
            float bandOffset = b * 0.33f;
            float bandProgress = ((progress * 0.5f + bandOffset) % 1f);

            // Vertical band moving around
            float bandAngle = bandProgress * Mth.TWO_PI;
            float bandWidth = 0.4f;

            int segments = 32;
            for (int i = 0; i < segments; i++) {
                float heightAngle = (i / (float) segments) * Mth.PI;

                float radius = BUBBLE_RADIUS * bubbleIntensity;
                float wobble = Mth.sin(bandAngle * 4f + currentTick * 0.08f) * 0.12f;
                wobble += Mth.sin(heightAngle * 6f + currentTick * 0.12f) * 0.08f;
                radius += wobble;

                float x1 = radius * Mth.sin(heightAngle) * Mth.cos(bandAngle - bandWidth);
                float y1 = radius * Mth.cos(heightAngle);
                float z1 = radius * Mth.sin(heightAngle) * Mth.sin(bandAngle - bandWidth);

                float x2 = radius * Mth.sin(heightAngle) * Mth.cos(bandAngle + bandWidth);
                float y2 = radius * Mth.cos(heightAngle);
                float z2 = radius * Mth.sin(heightAngle) * Mth.sin(bandAngle + bandWidth);

                float alpha = 0.3f * bubbleIntensity * Mth.sin(bandProgress * Mth.PI);

                renderBillboardQuad(consumer, matrix, x1, y1, z1, 0.2f,
                        1f, 0.98f, 1f, alpha);
                renderBillboardQuad(consumer, matrix, x2, y2, z2, 0.2f,
                        1f, 0.98f, 1f, alpha);
            }
        }
    }

    private void renderInnerVoid(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Swirling dark core representing the void of logic
        float coreRadius = 1.5f + Mth.sin(currentTick * 0.08f) * 0.3f;
        int segments = 16;

        for (int lat = 0; lat < segments; lat++) {
            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon < segments * 2; lon++) {
                float phi1 = (float) (lon * 2 * Math.PI / (segments * 2) + currentTick * 0.05f);
                float phi2 = (float) ((lon + 1) * 2 * Math.PI / (segments * 2) + currentTick * 0.05f);

                float x1 = coreRadius * Mth.sin(theta1) * Mth.cos(phi1);
                float y1 = coreRadius * Mth.cos(theta1);
                float z1 = coreRadius * Mth.sin(theta1) * Mth.sin(phi1);

                float x2 = coreRadius * Mth.sin(theta1) * Mth.cos(phi2);
                float y2 = coreRadius * Mth.cos(theta1);
                float z2 = coreRadius * Mth.sin(theta1) * Mth.sin(phi2);

                float x3 = coreRadius * Mth.sin(theta2) * Mth.cos(phi2);
                float y3 = coreRadius * Mth.cos(theta2);
                float z3 = coreRadius * Mth.sin(theta2) * Mth.sin(phi2);

                float x4 = coreRadius * Mth.sin(theta2) * Mth.cos(phi1);
                float y4 = coreRadius * Mth.cos(theta2);
                float z4 = coreRadius * Mth.sin(theta2) * Mth.sin(phi1);

                float alpha = 0.6f * bubbleIntensity;

                addVertex(consumer, matrix, x1, y1, z1, 0.15f, 0.1f, 0.2f, alpha);
                addVertex(consumer, matrix, x2, y2, z2, 0.15f, 0.1f, 0.2f, alpha);
                addVertex(consumer, matrix, x3, y3, z3, 0.15f, 0.1f, 0.2f, alpha);
                addVertex(consumer, matrix, x4, y4, z4, 0.15f, 0.1f, 0.2f, alpha);
            }
        }
    }

    private void renderContradictionLines(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (ContradictionLine line : contradictionLines) {
            line.update(progress, bubbleIntensity);

            if (line.alpha <= 0f) continue;

            // Draw line as a thin quad
            Vec3 dir = new Vec3(
                    line.endX - line.startX,
                    line.endY - line.startY,
                    line.endZ - line.startZ
            );

            Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(line.thickness);

            float alpha = line.alpha * bubbleIntensity;

            addVertex(consumer, matrix,
                    (float)(line.startX - perp.x), (float)(line.startY - perp.y), (float)(line.startZ - perp.z),
                    line.r, line.g, line.b, alpha);
            addVertex(consumer, matrix,
                    (float)(line.startX + perp.x), (float)(line.startY + perp.y), (float)(line.startZ + perp.z),
                    line.r, line.g, line.b, alpha);
            addVertex(consumer, matrix,
                    (float)(line.endX + perp.x), (float)(line.endY + perp.y), (float)(line.endZ + perp.z),
                    line.r, line.g, line.b, alpha * 0.3f);
            addVertex(consumer, matrix,
                    (float)(line.endX - perp.x), (float)(line.endY - perp.y), (float)(line.endZ - perp.z),
                    line.r, line.g, line.b, alpha * 0.3f);
        }
    }

    private void renderLoopPaths(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (LoopPath loop : loopPaths) {
            loop.update(progress, bubbleIntensity);

            if (loop.alpha <= 0f) continue;

            int segments = loop.points.size();
            for (int i = 0; i < segments - 1; i++) {
                Vec3 p1 = loop.points.get(i);
                Vec3 p2 = loop.points.get((i + 1) % segments);

                Vec3 dir = new Vec3(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.08f);

                float t = i / (float) segments;
                float segmentAlpha = loop.alpha * bubbleIntensity * (1f - t * 0.3f);

                addVertex(consumer, matrix,
                        (float)(p1.x - perp.x), (float)(p1.y - perp.y), (float)(p1.z - perp.z),
                        0.5f, 0.3f, 0.6f, segmentAlpha);
                addVertex(consumer, matrix,
                        (float)(p1.x + perp.x), (float)(p1.y + perp.y), (float)(p1.z + perp.z),
                        0.5f, 0.3f, 0.6f, segmentAlpha);
                addVertex(consumer, matrix,
                        (float)(p2.x + perp.x), (float)(p2.y + perp.y), (float)(p2.z + perp.z),
                        0.5f, 0.3f, 0.6f, segmentAlpha);
                addVertex(consumer, matrix,
                        (float)(p2.x - perp.x), (float)(p2.y - perp.y), (float)(p2.z - perp.z),
                        0.5f, 0.3f, 0.6f, segmentAlpha);
            }
        }
    }

    private void renderDeceitParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (DeceitParticle particle : deceitParticles) {
            particle.update(progress, bubbleIntensity);

            if (particle.alpha <= 0f || !particle.visible) continue;

            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, particle.r, particle.g, particle.b, particle.alpha * bubbleIntensity);
        }
    }

    private void renderRealityFractures(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (RealityFracture fracture : realityFractures) {
            fracture.update(progress, bubbleIntensity);

            if (fracture.alpha <= 0f) continue;

            // Draw jagged fracture line
            for (int i = 0; i < fracture.segments.size() - 1; i++) {
                Vec3 p1 = fracture.segments.get(i);
                Vec3 p2 = fracture.segments.get(i + 1);

                Vec3 dir = new Vec3(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.05f);

                float segmentAlpha = fracture.alpha * bubbleIntensity;

                addVertex(consumer, matrix,
                        (float)(p1.x - perp.x), (float)(p1.y - perp.y), (float)(p1.z - perp.z),
                        0.8f, 0.7f, 0.9f, segmentAlpha);
                addVertex(consumer, matrix,
                        (float)(p1.x + perp.x), (float)(p1.y + perp.y), (float)(p1.z + perp.z),
                        0.8f, 0.7f, 0.9f, segmentAlpha);
                addVertex(consumer, matrix,
                        (float)(p2.x + perp.x), (float)(p2.y + perp.y), (float)(p2.z + perp.z),
                        0.8f, 0.7f, 0.9f, segmentAlpha * 0.5f);
                addVertex(consumer, matrix,
                        (float)(p2.x - perp.x), (float)(p2.y - perp.y), (float)(p2.z - perp.z),
                        0.8f, 0.7f, 0.9f, segmentAlpha * 0.5f);
            }
        }
    }

    private void renderDistortionWaves(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Rippling distortion waves emanating from center
        int waveCount = 3;
        for (int w = 0; w < waveCount; w++) {
            float waveOffset = w * 0.33f;
            float waveProgress = ((progress + waveOffset) % 1f);

            float waveRadius = BUBBLE_RADIUS * 0.3f + waveProgress * BUBBLE_RADIUS * 0.6f;
            float waveAlpha = Mth.sin(waveProgress * Mth.PI) * 0.4f * bubbleIntensity;

            int segments = 32;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(angle1) * waveRadius;
                float z1 = Mth.sin(angle1) * waveRadius;
                float x2 = Mth.cos(angle2) * waveRadius;
                float z2 = Mth.sin(angle2) * waveRadius;

                float wave = Mth.sin(currentTick * 0.2f + angle1 * 2f) * 0.3f;

                addVertex(consumer, matrix, x1, wave - 0.2f, z1, 0.4f, 0.3f, 0.5f, waveAlpha);
                addVertex(consumer, matrix, x2, wave - 0.2f, z2, 0.4f, 0.3f, 0.5f, waveAlpha);
                addVertex(consumer, matrix, x2, wave + 0.2f, z2, 0.4f, 0.3f, 0.5f, waveAlpha);
                addVertex(consumer, matrix, x1, wave + 0.2f, z1, 0.4f, 0.3f, 0.5f, waveAlpha);
            }
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

    private class BubbleSegment {
        float theta, phi;
        float brightness;
        float size;
        float pulseSpeed;
        float pulseOffset;

        BubbleSegment(int lat, int lon, int latSegments, int lonSegments) {
            this.theta = (float) ((lat + 0.5f) * Math.PI / latSegments);
            this.phi = (float) ((lon + 0.5f) * 2 * Math.PI / lonSegments);
            this.pulseSpeed = 0.08f + random.nextFloat() * 0.12f;
            this.pulseOffset = random.nextFloat() * Mth.TWO_PI;
            this.size = 0.5f + random.nextFloat() * 0.5f;
        }

        void update(float progress, float intensity) {
            // Create moving highlights across the bubble surface
            float pulse = Mth.sin(currentTick * pulseSpeed + pulseOffset);
            float movingHighlight = Mth.sin(phi * 2f - currentTick * 0.1f + theta * 3f);

            this.brightness = Math.max(0f, (pulse + movingHighlight) * 0.5f + 0.2f);
            this.brightness *= intensity;
        }
    }

    private class ContradictionLine {
        float startX, startY, startZ;
        float endX, endY, endZ;
        float r, g, b;
        float alpha;
        float thickness;
        float phaseOffset;

        ContradictionLine() {
            random.setSeed(System.nanoTime());
            regenerate();
            this.phaseOffset = random.nextFloat() * Mth.TWO_PI;
        }

        void regenerate() {
            // Random position within bubble
            float angle1 = random.nextFloat() * Mth.TWO_PI;
            float angle2 = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * BUBBLE_RADIUS * 0.8f;

            this.startX = Mth.cos(angle1) * dist;
            this.startY = (random.nextFloat() - 0.5f) * BUBBLE_RADIUS;
            this.startZ = Mth.sin(angle1) * dist;

            this.endX = Mth.cos(angle2) * dist;
            this.endY = (random.nextFloat() - 0.5f) * BUBBLE_RADIUS;
            this.endZ = Mth.sin(angle2) * dist;

            // Reddish colors for contradictions
            this.r = 0.6f + random.nextFloat() * 0.3f;
            this.g = 0.2f + random.nextFloat() * 0.2f;
            this.b = 0.3f + random.nextFloat() * 0.2f;

            this.thickness = 0.08f + random.nextFloat() * 0.05f;
        }

        void update(float progress, float intensity) {
            // Pulsing contradictions
            float pulse = Mth.sin(progress * 10f + phaseOffset) * 0.5f + 0.5f;
            this.alpha = 0.5f + pulse * 0.4f;

            // Occasionally regenerate to show changing contradictions
            if (random.nextFloat() < 0.005f) {
                regenerate();
            }
        }
    }

    private class LoopPath {
        List<Vec3> points = new ArrayList<>();
        float alpha;
        float rotationSpeed;
        float scale;

        LoopPath() {
            random.setSeed(System.nanoTime());
            this.rotationSpeed = 0.02f + random.nextFloat() * 0.03f;
            this.scale = 0.6f + random.nextFloat() * 0.4f;
            generateLoop();
        }

        void generateLoop() {
            points.clear();
            int numPoints = 20;
            float baseRadius = BUBBLE_RADIUS * 0.4f * scale;

            for (int i = 0; i < numPoints; i++) {
                float t = i / (float) numPoints;
                float angle = t * Mth.TWO_PI * 2f; // Double loop

                float radius = baseRadius * (1f + 0.3f * Mth.sin(angle * 3f));
                float x = Mth.cos(angle) * radius;
                float y = Mth.sin(angle * 1.5f) * baseRadius * 0.8f;
                float z = Mth.sin(angle) * radius;

                points.add(new Vec3(x, y, z));
            }
        }

        void update(float progress, float intensity) {
            // Rotate the loop
            float rotation = currentTick * rotationSpeed;

            for (int i = 0; i < points.size(); i++) {
                Vec3 p = points.get(i);
                float newX = (float) (p.x * Math.cos(rotationSpeed) - p.z * Math.sin(rotationSpeed));
                float newZ = (float) (p.x * Math.sin(rotationSpeed) + p.z * Math.cos(rotationSpeed));
                points.set(i, new Vec3(newX, p.y, newZ));
            }

            this.alpha = 0.6f * (0.8f + 0.2f * Mth.sin(progress * 8f));
        }
    }

    private class DeceitParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float r, g, b;
        boolean visible;
        float flickerPhase;

        DeceitParticle() {
            random.setSeed(System.nanoTime());
            this.flickerPhase = random.nextFloat() * Mth.TWO_PI;
            respawn();
        }

        void respawn() {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * BUBBLE_RADIUS * 0.7f;

            this.x = Mth.cos(angle) * dist;
            this.y = (random.nextFloat() - 0.5f) * BUBBLE_RADIUS;
            this.z = Mth.sin(angle) * dist;

            float speed = 0.03f + random.nextFloat() * 0.05f;
            this.vx = (random.nextFloat() - 0.5f) * speed;
            this.vy = (random.nextFloat() - 0.5f) * speed;
            this.vz = (random.nextFloat() - 0.5f) * speed;

            this.size = 0.06f + random.nextFloat() * 0.08f;

            // Dark, muted colors
            this.r = 0.4f + random.nextFloat() * 0.3f;
            this.g = 0.2f + random.nextFloat() * 0.3f;
            this.b = 0.4f + random.nextFloat() * 0.3f;

            this.visible = true;
        }

        void update(float progress, float intensity) {
            this.x += vx;
            this.y += vy;
            this.z += vz;

            // Flicker effect - particles blink in and out (deceitful)
            float flicker = Mth.sin(currentTick * 0.3f + flickerPhase);
            this.visible = flicker > -0.3f;

            this.alpha = 0.6f * (0.5f + 0.5f * flicker);

            // Keep within bubble
            float distance = (float) Math.sqrt(x * x + y * y + z * z);
            if (distance > BUBBLE_RADIUS * 0.8f) {
                // Bounce back
                float factor = -0.5f;
                this.vx *= factor;
                this.vy *= factor;
                this.vz *= factor;
            }
        }
    }

    private class RealityFracture {
        List<Vec3> segments = new ArrayList<>();
        float alpha;
        float pulseOffset;

        RealityFracture() {
            random.setSeed(System.nanoTime());
            this.pulseOffset = random.nextFloat() * Mth.TWO_PI;
            generateFracture();
        }

        void generateFracture() {
            segments.clear();

            // Random starting point
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * BUBBLE_RADIUS * 0.5f;

            float x = Mth.cos(angle) * dist;
            float y = (random.nextFloat() - 0.5f) * BUBBLE_RADIUS * 0.8f;
            float z = Mth.sin(angle) * dist;

            segments.add(new Vec3(x, y, z));

            // Create jagged fracture path
            int numSegments = 5 + random.nextInt(5);
            for (int i = 0; i < numSegments; i++) {
                Vec3 last = segments.get(segments.size() - 1);

                float dx = (random.nextFloat() - 0.5f) * 1.5f;
                float dy = (random.nextFloat() - 0.5f) * 1.5f;
                float dz = (random.nextFloat() - 0.5f) * 1.5f;

                segments.add(new Vec3(last.x + dx, last.y + dy, last.z + dz));
            }
        }

        void update(float progress, float intensity) {
            float pulse = Mth.sin(progress * 12f + pulseOffset) * 0.5f + 0.5f;
            this.alpha = 0.4f + pulse * 0.4f;

            // Occasionally regenerate fractures
            if (random.nextFloat() < 0.003f) {
                generateFracture();
            }
        }
    }
}
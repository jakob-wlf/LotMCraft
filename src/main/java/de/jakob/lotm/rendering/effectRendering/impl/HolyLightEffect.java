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

public class HolyLightEffect extends ActiveEffect {

    private float beamProgress;
    private float expansionProgress;
    private float radiance = 1f;
    private final RandomSource random = RandomSource.create();
    private final List<LightRay> lightRays = new ArrayList<>();
    private final List<HolyParticle> holyParticles = new ArrayList<>();
    private final List<DivineSpark> divineSparks = new ArrayList<>();

    private static final float MAX_RADIUS = 45f;


    public HolyLightEffect(double x, double y, double z) {
        super(x, y, z, 20 * 9); // 120 ticks = 6 seconds total

        this.currentTick = 0;
        this.beamProgress = 0f;
        this.expansionProgress = 0f;

        // Initialize light rays emanating from center
        for (int i = 0; i < 64; i++) {
            lightRays.add(new LightRay());
        }

        // Initialize floating holy particles
        for (int i = 0; i < 200; i++) {
            holyParticles.add(new HolyParticle());
        }

        // Initialize divine sparks
        for (int i = 0; i < 150; i++) {
            divineSparks.add(new DivineSpark());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        float progress = tick / maxDuration;

        // === PHASE 1: Beam descends from the sky (0.0 → 0.16) ===
        beamProgress = Mth.clamp(progress / 0.16f, 0f, 1f);

        // === PHASE 2: Expansion starts right when beam hits ground (≥ 0.16) ===
        if (progress > 0.16f) {
            float adjustedProgress = (progress - 0.16f) / 0.84f;
            expansionProgress = Mth.clamp(adjustedProgress, 0f, 1f);
        } else {
            expansionProgress = 0f;
        }


        // Radiance pulses and fades
        radiance = 1f - (float) Math.pow(progress, 1.5);
        radiance *= (1f + 0.15f * Mth.sin(tick * 0.3f));

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // === Render beam & glow while descending AND for a short delay after ===
        if (progress <= 0.33f) { // stays visible until 0.33 progress (~1s after expansion begins)
            float beamFade = 1f;
            if (progress > 0.16f) {
                // start fading out after it hits the ground
                beamFade = 1f - ((progress - 0.16f) / (0.17f)); // fades to 0 at progress=0.33
                beamFade = Mth.clamp(beamFade, 0f, 1f);
            }

            renderDivineBeam(poseStack, bufferSource, beamProgress, radiance * beamFade);
            renderBeamGlow(poseStack, bufferSource, beamProgress, radiance * beamFade);
            renderBeamParticles(poseStack, bufferSource, beamProgress, radiance * beamFade);
        }


        // Render expansion once beam reaches ground
        if (expansionProgress > 0f) {
            renderHolySphere(poseStack, bufferSource, expansionProgress, radiance);
            renderInnerRadiance(poseStack, bufferSource, expansionProgress, radiance);
            renderLightRays(poseStack, bufferSource, expansionProgress, radiance);
            renderDivineRings(poseStack, bufferSource, expansionProgress, radiance);
            renderHolyParticles(poseStack, bufferSource, expansionProgress, radiance);
            renderPurificationWave(poseStack, bufferSource, expansionProgress, radiance);
            renderCelestialSpirals(poseStack, bufferSource, expansionProgress, radiance);
            renderDivineSparks(poseStack, bufferSource, expansionProgress, radiance);
            renderRadiantPulse(poseStack, bufferSource, expansionProgress, radiance);
            renderHeavenlyAura(poseStack, bufferSource, expansionProgress, radiance);
        }

        poseStack.popPose();
    }

    private void renderDivineBeam(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float startHeight = 60f; // starting point high above
        float beamLength = startHeight * progress; // how much of the beam has come down so far
        float topY = startHeight;                  // top always high above
        float bottomY = startHeight - beamLength;  // descends downward over time
        float beamRadius = 1.2f;
        int segments = 32;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * beamRadius;
            float z1 = Mth.sin(angle1) * beamRadius;
            float x2 = Mth.cos(angle2) * beamRadius;
            float z2 = Mth.sin(angle2) * beamRadius;

            // now draw from topY -> bottomY instead of 0 -> beamHeight
            addVertex(consumer, matrix, x1, topY, z1, 1f, 1f, 1f, radiance * 0.9f);
            addVertex(consumer, matrix, x2, topY, z2, 1f, 1f, 1f, radiance * 0.9f);
            addVertex(consumer, matrix, x2, bottomY, z2, 1f, 1f, 1f, radiance);
            addVertex(consumer, matrix, x1, bottomY, z1, 1f, 1f, 1f, radiance);
        }
    }


    private void renderBeamGlow(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float startHeight = 60f; // where the beam starts in the sky
        float beamLength = startHeight * progress; // how far it has descended
        float topY = startHeight;
        float bottomY = startHeight - beamLength;

        // Layered glow cylinders descending with the beam
        for (int layer = 0; layer < 3; layer++) {
            float radius = 1.2f + layer * 0.8f;
            float alpha = radiance * (0.4f - layer * 0.1f);
            int segments = 24;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(angle1) * radius;
                float z1 = Mth.sin(angle1) * radius;
                float x2 = Mth.cos(angle2) * radius;
                float z2 = Mth.sin(angle2) * radius;

                addVertex(consumer, matrix, x1, topY, z1, 1f, 1f, 0.95f, alpha * 0.3f);
                addVertex(consumer, matrix, x2, topY, z2, 1f, 1f, 0.95f, alpha * 0.3f);
                addVertex(consumer, matrix, x2, bottomY, z2, 1f, 1f, 1f, alpha);
                addVertex(consumer, matrix, x1, bottomY, z1, 1f, 1f, 1f, alpha);
            }
        }
    }


    private void renderBeamParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float startHeight = 60f; // top of the beam
        float beamLength = startHeight * progress;
        float topY = startHeight;
        float bottomY = startHeight - beamLength;

        int particleCount = 40;

        for (int i = 0; i < particleCount; i++) {
            float t = (i / (float) particleCount + progress * 2f) % 1f;

            // Y position moves *downward* from top to bottom
            float yPos = topY - beamLength * t;

            random.setSeed(i * 1234L);
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * 0.8f;

            float xPos = Mth.cos(angle) * dist;
            float zPos = Mth.sin(angle) * dist;

            float size = 0.15f * (1f - t * 0.5f);
            float alpha = radiance * (1f - t);

            renderBillboardQuad(consumer, matrix, xPos, yPos, zPos, size, 1f, 1f, 1f, alpha);
        }
    }


    private void renderHolySphere(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float radius = MAX_RADIUS * Mth.clamp((float) Math.pow(progress * 1.3f, 0.8), 0f, 1f);
        int latSegments = 24;
        int lonSegments = 32;

        // Smooth sphere with proper lighting
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

                float distFactor = 1f - (float) Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1) / (MAX_RADIUS * 1.2f);
                float alpha = radiance * 0.6f * Mth.clamp(distFactor, 0.1f, 1f);

                addVertex(consumer, matrix, x1, y1, z1, 1f, 1f, 1f, alpha);
                addVertex(consumer, matrix, x2, y2, z2, 1f, 1f, 1f, alpha);
                addVertex(consumer, matrix, x3, y3, z3, 1f, 1f, 1f, alpha);
                addVertex(consumer, matrix, x4, y4, z4, 1f, 1f, 1f, alpha);
            }
        }
    }

    private void renderInnerRadiance(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Pulsing inner core
        float coreRadius = 2f + Mth.sin(currentTick * 0.2f) * 0.5f;
        int segments = 20;

        for (int lat = 0; lat < segments; lat++) {
            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon < segments * 2; lon++) {
                float phi1 = (float) (lon * 2 * Math.PI / (segments * 2));
                float phi2 = (float) ((lon + 1) * 2 * Math.PI / (segments * 2));

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

                addVertex(consumer, matrix, x1, y1, z1, 1f, 1f, 0.98f, radiance * 0.95f);
                addVertex(consumer, matrix, x2, y2, z2, 1f, 1f, 0.98f, radiance * 0.95f);
                addVertex(consumer, matrix, x3, y3, z3, 1f, 1f, 0.98f, radiance * 0.95f);
                addVertex(consumer, matrix, x4, y4, z4, 1f, 1f, 0.98f, radiance * 0.95f);
            }
        }
    }

    private void renderLightRays(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (LightRay ray : lightRays) {
            ray.update(progress, radiance);

            float length = MAX_RADIUS * 1.5f * ray.length;
            float width = 0.3f * ray.width;

            Vec3 dir = new Vec3(
                Mth.cos(ray.angle) * Mth.sin(ray.elevation),
                Mth.cos(ray.elevation),
                Mth.sin(ray.angle) * Mth.sin(ray.elevation)
            );

            Vec3 end = new Vec3(dir.x * length, dir.y * length, dir.z * length);

            // Perpendicular vector for width
            Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(width);

            float alpha = radiance * ray.alpha * (1f - progress * 0.3f);

            addVertex(consumer, matrix, 0, 0, 0, 1f, 1f, 1f, alpha * 0.9f);
            addVertex(consumer, matrix, (float) perp.x, (float) perp.y, (float) perp.z, 1f, 1f, 1f, alpha * 0.9f);
            addVertex(consumer, matrix, (float) (end.x + perp.x), (float) (end.y + perp.y), (float) (end.z + perp.z), 1f, 1f, 0.95f, 0f);
            addVertex(consumer, matrix, (float) end.x, (float) end.y, (float) end.z, 1f, 1f, 0.95f, 0f);
        }
    }

    private void renderDivineRings(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        int ringCount = 5;
        for (int r = 0; r < ringCount; r++) {
            float ringProgress = Mth.clamp((progress - r * 0.15f) * 1.5f, 0f, 1f);
            if (ringProgress <= 0f) continue;

            float ringRadius = MAX_RADIUS * ringProgress;
            float thickness = 0.4f;
            int segments = 48;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1Inner = Mth.cos(angle1) * (ringRadius - thickness);
                float z1Inner = Mth.sin(angle1) * (ringRadius - thickness);
                float x1Outer = Mth.cos(angle1) * (ringRadius + thickness);
                float z1Outer = Mth.sin(angle1) * (ringRadius + thickness);

                float x2Inner = Mth.cos(angle2) * (ringRadius - thickness);
                float z2Inner = Mth.sin(angle2) * (ringRadius - thickness);
                float x2Outer = Mth.cos(angle2) * (ringRadius + thickness);
                float z2Outer = Mth.sin(angle2) * (ringRadius + thickness);

                float yPos = 0.1f + r * 0.3f;
                float alpha = radiance * 0.5f * (1f - ringProgress) * (1f - r * 0.15f);

                addVertex(consumer, matrix, x1Inner, yPos, z1Inner, 1f, 1f, 1f, alpha);
                addVertex(consumer, matrix, x2Inner, yPos, z2Inner, 1f, 1f, 1f, alpha);
                addVertex(consumer, matrix, x2Outer, yPos, z2Outer, 1f, 1f, 0.95f, alpha * 0.3f);
                addVertex(consumer, matrix, x1Outer, yPos, z1Outer, 1f, 1f, 0.95f, alpha * 0.3f);
            }
        }
    }

    private void renderHolyParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (HolyParticle particle : holyParticles) {
            particle.update(progress, radiance);

            if (particle.alpha <= 0f) continue;

            float distance = (float) Math.sqrt(particle.x * particle.x + particle.y * particle.y + particle.z * particle.z);
            float currentRadius = MAX_RADIUS * progress;

            if (distance > currentRadius + 5f) continue;

            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                particle.size, 1f, 1f, 1f, particle.alpha * radiance);
        }
    }

    private void renderPurificationWave(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Ground-level expanding wave
        float waveRadius = MAX_RADIUS * progress;
        float waveHeight = 0.05f;
        int segments = 64;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * waveRadius;
            float z1 = Mth.sin(angle1) * waveRadius;
            float x2 = Mth.cos(angle2) * waveRadius;
            float z2 = Mth.sin(angle2) * waveRadius;

            float wave1 = Mth.sin(angle1 * 3f + currentTick * 0.3f) * 0.3f;
            float wave2 = Mth.sin(angle2 * 3f + currentTick * 0.3f) * 0.3f;

            float alpha = radiance * 0.6f * (1f - progress * 0.7f);

            addVertex(consumer, matrix, 0, waveHeight, 0, 1f, 1f, 1f, alpha * 0.3f);
            addVertex(consumer, matrix, x1, waveHeight + wave1, z1, 1f, 1f, 0.95f, alpha);
            addVertex(consumer, matrix, x2, waveHeight + wave2, z2, 1f, 1f, 0.95f, alpha);
            addVertex(consumer, matrix, 0, waveHeight, 0, 1f, 1f, 1f, alpha * 0.3f);
        }
    }

    private void renderCelestialSpirals(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        int spiralCount = 3;
        int segmentsPerSpiral = 50;

        for (int s = 0; s < spiralCount; s++) {
            float spiralOffset = s * Mth.TWO_PI / spiralCount;

            for (int i = 0; i < segmentsPerSpiral - 1; i++) {
                float t1 = i / (float) segmentsPerSpiral;
                float t2 = (i + 1) / (float) segmentsPerSpiral;

                float spiralProgress = Mth.clamp((progress - t1 * 0.5f) * 2f, 0f, 1f);
                if (spiralProgress <= 0f) continue;

                float radius1 = t1 * MAX_RADIUS * progress;
                float radius2 = t2 * MAX_RADIUS * progress;

                float angle1 = t1 * Mth.TWO_PI * 3f + spiralOffset + currentTick * 0.05f;
                float angle2 = t2 * Mth.TWO_PI * 3f + spiralOffset + currentTick * 0.05f;

                float x1 = Mth.cos(angle1) * radius1;
                float z1 = Mth.sin(angle1) * radius1;
                float y1 = t1 * 4f;

                float x2 = Mth.cos(angle2) * radius2;
                float z2 = Mth.sin(angle2) * radius2;
                float y2 = t2 * 4f;

                float width = 0.3f;
                float alpha = radiance * 0.5f * (1f - t1) * spiralProgress;

                Vec3 dir = new Vec3(x2 - x1, y2 - y1, z2 - z1).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(width);

                addVertex(consumer, matrix, (float) (x1 - perp.x), y1, (float) (z1 - perp.z), 1f, 1f, 1f, alpha);
                addVertex(consumer, matrix, (float) (x1 + perp.x), y1, (float) (z1 + perp.z), 1f, 1f, 1f, alpha);
                addVertex(consumer, matrix, (float) (x2 + perp.x), y2, (float) (z2 + perp.z), 1f, 1f, 0.95f, alpha * 0.7f);
                addVertex(consumer, matrix, (float) (x2 - perp.x), y2, (float) (z2 - perp.z), 1f, 1f, 0.95f, alpha * 0.7f);
            }
        }
    }

    private void renderDivineSparks(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (DivineSpark spark : divineSparks) {
            spark.update(progress, radiance);

            if (spark.alpha <= 0f) continue;

            float currentRadius = MAX_RADIUS * progress;
            float distance = (float) Math.sqrt(spark.x * spark.x + spark.y * spark.y + spark.z * spark.z);

            if (distance < currentRadius - 2f || distance > currentRadius + 2f) continue;

            renderBillboardQuad(consumer, matrix, spark.x, spark.y, spark.z,
                spark.size, 1f, 1f, 0.98f, spark.alpha * radiance * 0.8f);
        }
    }

    private void renderRadiantPulse(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Expanding pulse shells
        int pulseCount = 4;
        for (int p = 0; p < pulseCount; p++) {
            float pulseDelay = p * 0.1f;
            float pulseProgress = Mth.clamp((progress - pulseDelay) * 1.3f, 0f, 1f);

            if (pulseProgress <= 0f || pulseProgress >= 1f) continue;

            float pulseRadius = MAX_RADIUS * pulseProgress * 0.9f;
            float alpha = radiance * 0.3f * Mth.sin(pulseProgress * Mth.PI);

            int segments = 32;
            for (int lat = 0; lat < 16; lat++) {
                float theta1 = (float) (lat * Math.PI / 16);
                float theta2 = (float) ((lat + 1) * Math.PI / 16);

                for (int lon = 0; lon < segments; lon++) {
                    float phi1 = (float) (lon * 2 * Math.PI / segments);
                    float phi2 = (float) ((lon + 1) * 2 * Math.PI / segments);

                    float x1 = pulseRadius * Mth.sin(theta1) * Mth.cos(phi1);
                    float y1 = pulseRadius * Mth.cos(theta1);
                    float z1 = pulseRadius * Mth.sin(theta1) * Mth.sin(phi1);

                    float x2 = pulseRadius * Mth.sin(theta1) * Mth.cos(phi2);
                    float y2 = pulseRadius * Mth.cos(theta1);
                    float z2 = pulseRadius * Mth.sin(theta1) * Mth.sin(phi2);

                    float x3 = pulseRadius * Mth.sin(theta2) * Mth.cos(phi2);
                    float y3 = pulseRadius * Mth.cos(theta2);
                    float z3 = pulseRadius * Mth.sin(theta2) * Mth.sin(phi2);

                    float x4 = pulseRadius * Mth.sin(theta2) * Mth.cos(phi1);
                    float y4 = pulseRadius * Mth.cos(theta2);
                    float z4 = pulseRadius * Mth.sin(theta2) * Mth.sin(phi1);

                    addVertex(consumer, matrix, x1, y1, z1, 1f, 1f, 1f, alpha);
                    addVertex(consumer, matrix, x2, y2, z2, 1f, 1f, 1f, alpha);
                    addVertex(consumer, matrix, x3, y3, z3, 1f, 1f, 1f, alpha);
                    addVertex(consumer, matrix, x4, y4, z4, 1f, 1f, 1f, alpha);
                }
            }
        }
    }

    private void renderHeavenlyAura(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress, float radiance) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Soft outer glow that fades out
        float auraRadius = MAX_RADIUS * progress * 1.3f;
        int segments = 48;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1Inner = Mth.cos(angle1) * MAX_RADIUS * progress;
            float z1Inner = Mth.sin(angle1) * MAX_RADIUS * progress;
            float x1Outer = Mth.cos(angle1) * auraRadius;
            float z1Outer = Mth.sin(angle1) * auraRadius;

            float x2Inner = Mth.cos(angle2) * MAX_RADIUS * progress;
            float z2Inner = Mth.sin(angle2) * MAX_RADIUS * progress;
            float x2Outer = Mth.cos(angle2) * auraRadius;
            float z2Outer = Mth.sin(angle2) * auraRadius;

            float alphaInner = radiance * 0.4f;
            float alphaOuter = 0f;

            // Ground level aura
            addVertex(consumer, matrix, x1Inner, 0.02f, z1Inner, 1f, 1f, 0.95f, alphaInner);
            addVertex(consumer, matrix, x2Inner, 0.02f, z2Inner, 1f, 1f, 0.95f, alphaInner);
            addVertex(consumer, matrix, x2Outer, 0.02f, z2Outer, 1f, 1f, 0.9f, alphaOuter);
            addVertex(consumer, matrix, x1Outer, 0.02f, z1Outer, 1f, 1f, 0.9f, alphaOuter);

            // Rising aura
            float height = 3f;
            addVertex(consumer, matrix, x1Inner, 0.02f, z1Inner, 1f, 1f, 0.95f, alphaInner * 0.7f);
            addVertex(consumer, matrix, x2Inner, 0.02f, z2Inner, 1f, 1f, 0.95f, alphaInner * 0.7f);
            addVertex(consumer, matrix, x2Outer, height, z2Outer, 1f, 1f, 0.9f, alphaOuter);
            addVertex(consumer, matrix, x1Outer, height, z1Outer, 1f, 1f, 0.9f, alphaOuter);
        }
    }

    // Helper method to add a vertex
    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                          float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a);
    }

    // Helper method to render a billboard quad (always faces camera)
    private void renderBillboardQuad(VertexConsumer consumer, Matrix4f matrix,
                                     float x, float y, float z, float size,
                                     float r, float g, float b, float a) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        // Calculate vectors to orient quad towards camera
        Vec3 toCamera = new Vec3(
            cameraPos.x - (this.x + x),
            cameraPos.y - (this.y + y),
            cameraPos.z - (this.z + z)
        ).normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCamera.cross(up).normalize().scale(size);
        up = right.cross(toCamera).normalize().scale(size);

        // Four corners of the quad
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

    // Inner classes for particle systems
    private class LightRay {
        float angle;
        float elevation;
        float length;
        float width;
        float alpha;
        float speed;

        LightRay() {
            random.setSeed(System.nanoTime());
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.elevation = random.nextFloat() * Mth.PI;
            this.length = 0.8f + random.nextFloat() * 0.4f;
            this.width = 0.8f + random.nextFloat() * 0.4f;
            this.alpha = 0.6f + random.nextFloat() * 0.4f;
            this.speed = 0.5f + random.nextFloat() * 0.5f;
        }

        void update(float progress, float radiance) {
            float pulseProgress = Mth.clamp(progress * speed, 0f, 1f);
            this.alpha = 0.8f * Mth.sin(pulseProgress * Mth.PI) * radiance;
        }
    }

    private class HolyParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float lifetime;
        float rotationSpeed;

        HolyParticle() {
            respawn();
        }

        void respawn() {
            random.setSeed(System.nanoTime());
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * 2f;

            this.x = Mth.cos(angle) * dist;
            this.y = random.nextFloat() * 4f - 2f;
            this.z = Mth.sin(angle) * dist;

            float speed = 0.1f + random.nextFloat() * 0.15f;
            this.vx = (random.nextFloat() - 0.5f) * speed;
            this.vy = random.nextFloat() * speed * 0.5f;
            this.vz = (random.nextFloat() - 0.5f) * speed;

            this.size = 0.1f + random.nextFloat() * 0.15f;
            this.alpha = 0.7f + random.nextFloat() * 0.3f;
            this.lifetime = random.nextFloat();
            this.rotationSpeed = (random.nextFloat() - 0.5f) * 0.2f;
        }

        void update(float progress, float radiance) {
            this.x += vx;
            this.y += vy;
            this.z += vz;

            float distance = (float) Math.sqrt(x * x + y * y + z * z);
            float currentRadius = MAX_RADIUS * progress;

            // Fade based on distance from expansion edge
            float edgeDist = Math.abs(distance - currentRadius);
            float fadeFactor = 1f - Mth.clamp(edgeDist / 5f, 0f, 1f);

            this.alpha = (0.7f + random.nextFloat() * 0.3f) * fadeFactor * radiance;

            // Respawn if too far
            if (distance > currentRadius + 8f) {
                respawn();
            }
        }
    }

    private class DivineSpark {
        float x, y, z;
        float size;
        float alpha;
        float angle;
        float orbitSpeed;
        float orbitRadius;

        DivineSpark() {
            random.setSeed(System.nanoTime());
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.orbitSpeed = 0.02f + random.nextFloat() * 0.03f;
            this.orbitRadius = 0.5f + random.nextFloat() * 1.5f;
            this.size = 0.08f + random.nextFloat() * 0.12f;
            this.alpha = 0.8f + random.nextFloat() * 0.2f;
            updatePosition(0f);
        }

        void update(float progress, float radiance) {
            this.angle += orbitSpeed;
            updatePosition(progress);

            float currentRadius = MAX_RADIUS * progress;
            float distance = (float) Math.sqrt(x * x + y * y + z * z);

            // Sparks appear at the expansion edge
            float edgeDist = Math.abs(distance - currentRadius);
            if (edgeDist < 2f) {
                this.alpha = (0.8f + random.nextFloat() * 0.2f) * (1f - edgeDist / 2f) * radiance;
            } else {
                this.alpha = 0f;
            }
        }

        void updatePosition(float progress) {
            float radius = MAX_RADIUS * progress;
            float elevation = random.nextFloat() * Mth.PI;

            this.x = Mth.cos(angle) * radius + Mth.cos(angle * 3f) * orbitRadius;
            this.y = Mth.cos(elevation) * radius * 0.5f;
            this.z = Mth.sin(angle) * radius + Mth.sin(angle * 3f) * orbitRadius;
        }
    }
}
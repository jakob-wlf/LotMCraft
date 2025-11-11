package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.entity.custom.DistortionFieldEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class DistortionFieldRenderer extends EntityRenderer<DistortionFieldEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/particle/soul_fire_flame.png");

    public DistortionFieldRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DistortionFieldEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        float radius = entity.getRadius();
        float age = entity.tickCount + partialTick;
        float lifetime = entity.getDuration();
        float progress = (entity.tickCount + partialTick) / lifetime;

        poseStack.pushPose();

        // Core effects - render first for layering
        renderPulsingCore(poseStack, buffer, radius, age, progress, packedLight);
        renderEnergySpirals(poseStack, buffer, radius, age, packedLight);

        // Ground and boundary effects
        renderGroundEffect(poseStack, buffer, radius, age, packedLight);
        renderBoundaryRings(poseStack, buffer, radius, age, packedLight);
        renderRotatingRunes(poseStack, buffer, radius, age, packedLight);

        // Wave and distortion effects
        renderDistortionWaves(poseStack, buffer, radius, age, progress, packedLight);
        renderShockwaveBursts(poseStack, buffer, radius, age, packedLight);
        renderRippleEffect(poseStack, buffer, radius, age, packedLight);

        // Particle and pillar effects
        renderParticleField(poseStack, buffer, radius, age, packedLight);
        renderVerticalPillars(poseStack, buffer, radius, age, packedLight);
        renderFloatingOrbs(poseStack, buffer, radius, age, packedLight);

        // Top layer effects
        renderLightningArcs(poseStack, buffer, radius, age, packedLight);
        renderEnergyBeams(poseStack, buffer, radius, age, packedLight);
        renderDomeShield(poseStack, buffer, radius, age, progress, packedLight);

        poseStack.popPose();
    }

    private void renderPulsingCore(PoseStack poseStack, MultiBufferSource buffer,
                                   float radius, float age, float progress, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        float coreSize = 3.0f + Mth.sin(age * 0.2f) * 1.5f;
        int segments = 16;

        for (int layer = 0; layer < 3; layer++) {
            float layerSize = coreSize * (1.0f + layer * 0.3f);
            float layerHeight = layer * 0.5f;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * 2 * Math.PI / segments);
                float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

                float x1 = Mth.cos(angle1) * layerSize;
                float z1 = Mth.sin(angle1) * layerSize;
                float x2 = Mth.cos(angle2) * layerSize;
                float z2 = Mth.sin(angle2) * layerSize;

                float pulsePhase = age * 0.3f + layer;
                float r = 0.6f + Mth.sin(pulsePhase) * 0.3f;
                float g = 0.2f + Mth.sin(pulsePhase + 1) * 0.2f;
                float b = 0.9f + Mth.sin(pulsePhase + 2) * 0.1f;
                float alpha = 0.9f - layer * 0.2f;

                consumer.addVertex(pose, x1, layerHeight, z1).setColor(r, g, b, alpha);
                consumer.addVertex(pose, x2, layerHeight, z2).setColor(r, g, b, alpha);
            }
        }
    }

    private void renderEnergySpirals(PoseStack poseStack, MultiBufferSource buffer,
                                     float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        int spiralCount = 6;
        int spiralSegments = 100;

        for (int s = 0; s < spiralCount; s++) {
            float spiralOffset = (s / (float) spiralCount) * 2 * Mth.PI;

            for (int i = 0; i < spiralSegments - 1; i++) {
                float t1 = i / (float) spiralSegments;
                float t2 = (i + 1) / (float) spiralSegments;

                float h1 = t1 * 30.0f;
                float h2 = t2 * 30.0f;

                float r1 = t1 * radius * 0.8f;
                float r2 = t2 * radius * 0.8f;

                float angle1 = spiralOffset + age * 0.1f + t1 * 4 * Mth.PI;
                float angle2 = spiralOffset + age * 0.1f + t2 * 4 * Mth.PI;

                float x1 = Mth.cos(angle1) * r1;
                float z1 = Mth.sin(angle1) * r1;
                float x2 = Mth.cos(angle2) * r2;
                float z2 = Mth.sin(angle2) * r2;

                float colorMix = t1;
                float r = Mth.lerp(colorMix, 0.4f, 0.8f);
                float g = 0.3f;
                float b = Mth.lerp(colorMix, 0.9f, 0.5f);
                float alpha = (1.0f - t1) * 0.7f;

                consumer.addVertex(pose, x1, h1, z1).setColor(r, g, b, alpha);
                consumer.addVertex(pose, x2, h2, z2).setColor(r, g, b, alpha);
            }
        }
    }

    private void renderBoundaryRings(PoseStack poseStack, MultiBufferSource buffer,
                                     float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        int segments = 80;
        float pulseOffset = age * 0.1f;

        // Multiple ring layers at different heights
        for (int layer = 0; layer < 5; layer++) {
            float height = layer * 5.0f + Mth.sin(age * 0.1f + layer) * 2.0f;

            for (int ring = 0; ring < 4; ring++) {
                float ringOffset = ring * 0.2f;
                float ringRadius = radius + Mth.sin(pulseOffset + ringOffset + layer) * 3.0f;
                float thickness = 0.2f + ring * 0.1f;

                for (int i = 0; i < segments; i++) {
                    float angle1 = (float) (i * 2 * Math.PI / segments);
                    float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

                    float x1 = Mth.cos(angle1) * ringRadius;
                    float z1 = Mth.sin(angle1) * ringRadius;
                    float x2 = Mth.cos(angle2) * ringRadius;
                    float z2 = Mth.sin(angle2) * ringRadius;

                    float colorPhase = (angle1 + pulseOffset + layer) % (2 * Mth.PI);
                    float r = 0.5f + Mth.sin(colorPhase) * 0.3f;
                    float g = 0.3f + Mth.sin(colorPhase + 1.5f) * 0.2f;
                    float b = 0.9f + Mth.cos(colorPhase) * 0.1f;
                    float alpha = 0.8f - (ring * 0.15f) - (layer * 0.1f);

                    consumer.addVertex(pose, x1, height, z1).setColor(r, g, b, alpha);
                    consumer.addVertex(pose, x2, height, z2).setColor(r, g, b, alpha);
                }
            }
        }
    }

    private void renderRotatingRunes(PoseStack poseStack, MultiBufferSource buffer,
                                     float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        int runeCount = 8;
        float runeRadius = radius * 0.7f;

        for (int i = 0; i < runeCount; i++) {
            float angle = (float) (i * 2 * Math.PI / runeCount) + age * 0.05f;
            float x = Mth.cos(angle) * runeRadius;
            float z = Mth.sin(angle) * runeRadius;
            float y = 0.5f + Mth.sin(age * 0.2f + i) * 1.0f;

            // Draw simple rune pattern (cross with diamond)
            float runeSize = 1.5f + Mth.sin(age * 0.3f + i) * 0.5f;
            float glowPhase = (age * 0.2f + i) % (2 * Mth.PI);
            float r = 0.6f + Mth.sin(glowPhase) * 0.4f;
            float g = 0.4f;
            float b = 1.0f;
            float alpha = 0.8f;

            // Vertical line
            consumer.addVertex(pose, x, y - runeSize, z).setColor(r, g, b, alpha);
            consumer.addVertex(pose, x, y + runeSize, z).setColor(r, g, b, alpha);

            // Horizontal line
            consumer.addVertex(pose, x - runeSize, y, z).setColor(r, g, b, alpha);
            consumer.addVertex(pose, x + runeSize, y, z).setColor(r, g, b, alpha);

            // Diamond outline
            float d = runeSize * 0.7f;
            consumer.addVertex(pose, x, y + d, z).setColor(r, g, b, alpha * 0.6f);
            consumer.addVertex(pose, x + d, y, z).setColor(r, g, b, alpha * 0.6f);

            consumer.addVertex(pose, x + d, y, z).setColor(r, g, b, alpha * 0.6f);
            consumer.addVertex(pose, x, y - d, z).setColor(r, g, b, alpha * 0.6f);

            consumer.addVertex(pose, x, y - d, z).setColor(r, g, b, alpha * 0.6f);
            consumer.addVertex(pose, x - d, y, z).setColor(r, g, b, alpha * 0.6f);

            consumer.addVertex(pose, x - d, y, z).setColor(r, g, b, alpha * 0.6f);
            consumer.addVertex(pose, x, y + d, z).setColor(r, g, b, alpha * 0.6f);
        }
    }

    private void renderShockwaveBursts(PoseStack poseStack, MultiBufferSource buffer,
                                       float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        int burstInterval = 30;
        float timeSinceLastBurst = age % burstInterval;

        if (timeSinceLastBurst < 10) {
            float burstProgress = timeSinceLastBurst / 10.0f;
            float burstRadius = burstProgress * radius * 1.2f;
            float burstAlpha = 1.0f - burstProgress;

            int segments = 64;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * 2 * Math.PI / segments);
                float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

                float x1 = Mth.cos(angle1) * burstRadius;
                float z1 = Mth.sin(angle1) * burstRadius;
                float x2 = Mth.cos(angle2) * burstRadius;
                float z2 = Mth.sin(angle2) * burstRadius;

                float height = Mth.sin(burstProgress * Mth.PI) * 5.0f;

                consumer.addVertex(pose, x1, height, z1).setColor(1.0f, 0.8f, 0.3f, burstAlpha * 0.9f);
                consumer.addVertex(pose, x2, height, z2).setColor(1.0f, 0.8f, 0.3f, burstAlpha * 0.9f);
            }
        }
    }

    private void renderRippleEffect(PoseStack poseStack, MultiBufferSource buffer,
                                    float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        int rippleCount = 5;
        for (int r = 0; r < rippleCount; r++) {
            float ripplePhase = (age * 0.1f + r * 10) % 50;
            float rippleRadius = (ripplePhase / 50.0f) * radius;
            float rippleAlpha = Mth.sin((ripplePhase / 50.0f) * Mth.PI) * 0.5f;

            int segments = 48;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * 2 * Math.PI / segments);
                float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

                float x1 = Mth.cos(angle1) * rippleRadius;
                float z1 = Mth.sin(angle1) * rippleRadius;
                float x2 = Mth.cos(angle2) * rippleRadius;
                float z2 = Mth.sin(angle2) * rippleRadius;

                float y = 0.3f + Mth.sin(angle1 * 3 + age * 0.2f) * 0.5f;

                consumer.addVertex(pose, x1, y, z1).setColor(0.5f, 0.3f, 1.0f, rippleAlpha);
                consumer.addVertex(pose, x2, y, z2).setColor(0.5f, 0.3f, 1.0f, rippleAlpha);
            }
        }
    }

    private void renderFloatingOrbs(PoseStack poseStack, MultiBufferSource buffer,
                                    float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.endPortal());

        int orbCount = 12;

        for (int i = 0; i < orbCount; i++) {
            float orbAngle = (float) (i * 2 * Math.PI / orbCount) + age * 0.03f;
            float orbRadius = radius * 0.6f + Mth.sin(age * 0.1f + i) * radius * 0.2f;
            float x = Mth.cos(orbAngle) * orbRadius;
            float z = Mth.sin(orbAngle) * orbRadius;
            float y = 10.0f + Mth.sin(age * 0.15f + i * 2) * 5.0f;

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(-entityRenderDispatcher.camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(entityRenderDispatcher.camera.getXRot()));

            Matrix4f pose = poseStack.last().pose();

            float size = 1.0f + Mth.sin(age * 0.2f + i) * 0.4f;
            float pulsePhase = age * 0.3f + i;
            float r = 0.7f + Mth.sin(pulsePhase) * 0.3f;
            float g = 0.4f;
            float b = 1.0f;
            float alpha = 0.9f;

            consumer.addVertex(pose, -size, -size, 0).setColor(r, g, b, alpha);
            consumer.addVertex(pose, -size, size, 0).setColor(r, g, b, alpha);
            consumer.addVertex(pose, size, size, 0).setColor(r, g, b, alpha);
            consumer.addVertex(pose, size, -size, 0).setColor(r, g, b, alpha);

            poseStack.popPose();
        }
    }

    private void renderLightningArcs(PoseStack poseStack, MultiBufferSource buffer,
                                     float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        int arcCount = 20;

        for (int i = 0; i < arcCount; i++) {
            if ((age + i * 7) % 15 < 3) {
                float angle1 = (float) ((i * 2 * Math.PI / arcCount) + age * 0.05f);
                float angle2 = (float) (((i + 2) * 2 * Math.PI / arcCount) + age * 0.05f);

                float x1 = Mth.cos(angle1) * radius * 0.5f;
                float z1 = Mth.sin(angle1) * radius * 0.5f;
                float x2 = Mth.cos(angle2) * radius * 0.5f;
                float z2 = Mth.sin(angle2) * radius * 0.5f;

                float y1 = 15.0f + Mth.sin(age * 0.2f + i) * 3.0f;
                float y2 = 15.0f + Mth.sin(age * 0.2f + i + 2) * 3.0f;

                int segments = 8;
                for (int s = 0; s < segments; s++) {
                    float t1 = s / (float) segments;
                    float t2 = (s + 1) / (float) segments;

                    float sx1 = Mth.lerp(t1, x1, x2);
                    float sz1 = Mth.lerp(t1, z1, z2);
                    float sy1 = Mth.lerp(t1, y1, y2);

                    float sx2 = Mth.lerp(t2, x1, x2);
                    float sz2 = Mth.lerp(t2, z1, z2);
                    float sy2 = Mth.lerp(t2, y1, y2);

                    float jitter = (float) (Math.random() - 0.5) * 1.5f;

                    consumer.addVertex(pose, sx1, sy1, sz1).setColor(0.9f, 0.9f, 1.0f, 0.9f);
                    consumer.addVertex(pose, sx2 + jitter, sy2, sz2 + jitter).setColor(0.9f, 0.9f, 1.0f, 0.9f);
                }
            }
        }
    }

    private void renderEnergyBeams(PoseStack poseStack, MultiBufferSource buffer,
                                   float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        int beamCount = 6;

        for (int i = 0; i < beamCount; i++) {
            float angle = (float) (i * 2 * Math.PI / beamCount) + age * 0.02f;
            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;

            float beamHeight = 35.0f;
            float beamAlpha = (Mth.sin(age * 0.1f + i) + 1.0f) * 0.5f * 0.6f;

            for (float h = 0; h < beamHeight; h += 1.0f) {
                float heightProgress = h / beamHeight;
                float nextH = h + 1.0f;

                float r = Mth.lerp(heightProgress, 0.5f, 1.0f);
                float g = 0.5f;
                float b = 1.0f;
                float alpha = beamAlpha * (1.0f - heightProgress);

                consumer.addVertex(pose, x, h, z).setColor(r, g, b, alpha);
                consumer.addVertex(pose, x, nextH, z).setColor(r, g, b, alpha * 0.9f);
            }
        }
    }

    private void renderDomeShield(PoseStack poseStack, MultiBufferSource buffer,
                                  float radius, float age, float progress, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.endPortal());
        Matrix4f pose = poseStack.last().pose();

        int latitudes = 16;
        int longitudes = 32;

        for (int lat = 0; lat < latitudes; lat++) {
            float lat1 = (lat / (float) latitudes) * Mth.HALF_PI;
            float lat2 = ((lat + 1) / (float) latitudes) * Mth.HALF_PI;

            for (int lon = 0; lon < longitudes; lon++) {
                float lon1 = (lon / (float) longitudes) * 2 * Mth.PI + age * 0.01f;
                float lon2 = ((lon + 1) / (float) longitudes) * 2 * Mth.PI + age * 0.01f;

                float x1 = Mth.cos(lon1) * Mth.cos(lat1) * radius;
                float y1 = Mth.sin(lat1) * radius;
                float z1 = Mth.sin(lon1) * Mth.cos(lat1) * radius;

                float x2 = Mth.cos(lon2) * Mth.cos(lat1) * radius;
                float y2 = Mth.sin(lat1) * radius;
                float z2 = Mth.sin(lon2) * Mth.cos(lat1) * radius;

                float x3 = Mth.cos(lon2) * Mth.cos(lat2) * radius;
                float y3 = Mth.sin(lat2) * radius;
                float z3 = Mth.sin(lon2) * Mth.cos(lat2) * radius;

                float x4 = Mth.cos(lon1) * Mth.cos(lat2) * radius;
                float y4 = Mth.sin(lat2) * radius;
                float z4 = Mth.sin(lon1) * Mth.cos(lat2) * radius;

                float hexPhase = (lat + lon + age * 0.1f) % 3;
                float r = hexPhase > 1.5f ? 0.6f : 0.3f;
                float g = 0.4f;
                float b = hexPhase > 1.5f ? 1.0f : 0.7f;
                float alpha = 0.15f + Mth.sin(age * 0.1f + lat + lon) * 0.05f;

                consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, alpha);
                consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, alpha);
                consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, alpha);
                consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, alpha);
            }
        }
    }

    private void renderDistortionWaves(PoseStack poseStack, MultiBufferSource buffer,
                                       float radius, float age, float progress, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        int waves = 12;
        float waveSpeed = 0.15f;

        for (int w = 0; w < waves; w++) {
            float wavePhase = (age * waveSpeed + w * 15) % 100;
            float waveRadius = (wavePhase / 100.0f) * radius;

            if (waveRadius > radius) continue;

            float waveAlpha = 1.0f - (waveRadius / radius);
            waveAlpha = Mth.clamp(waveAlpha * 0.7f, 0, 1);

            int segments = 64;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * 2 * Math.PI / segments);
                float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

                float x1 = Mth.cos(angle1) * waveRadius;
                float z1 = Mth.sin(angle1) * waveRadius;
                float x2 = Mth.cos(angle2) * waveRadius;
                float z2 = Mth.sin(angle2) * waveRadius;

                float heightOffset1 = Mth.sin(age * 0.15f + angle1 * 4) * 4.0f;
                float heightOffset2 = Mth.sin(age * 0.15f + angle2 * 4) * 4.0f;

                float colorMix = (float) i / segments;
                float r = Mth.lerp(colorMix, 0.5f, 0.2f);
                float g = 0.2f;
                float b = Mth.lerp(colorMix, 1.0f, 0.4f);

                consumer.addVertex(pose, x1, 0.3f + heightOffset1, z1).setColor(r, g, b, waveAlpha);
                consumer.addVertex(pose, x2, 0.3f + heightOffset2, z2).setColor(r, g, b, waveAlpha);
            }
        }
    }

    private void renderParticleField(PoseStack poseStack, MultiBufferSource buffer,
                                     float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.endPortal());

        int particleCount = 300;

        for (int i = 0; i < particleCount; i++) {
            float seed = i * 12.9898f;
            float particleAngle = (seed * 43758.5453f) % (2 * Mth.PI);
            float particleRadius = ((seed * 23.1406f) % 1.0f) * radius;
            float particleHeight = ((seed * 17.3942f) % 1.0f) * 25.0f;

            float spiralOffset = age * 0.08f + i * 0.1f;
            float x = Mth.cos(particleAngle + spiralOffset) * particleRadius;
            float z = Mth.sin(particleAngle + spiralOffset) * particleRadius;
            float y = particleHeight + Mth.sin(age * 0.15f + seed) * 3.0f;

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(-entityRenderDispatcher.camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(entityRenderDispatcher.camera.getXRot()));

            Matrix4f pose = poseStack.last().pose();

            float size = 0.4f + Mth.sin(age * 0.3f + seed) * 0.2f;

            float colorPhase = (age * 0.08f + seed) % (2 * Mth.PI);
            float r = Mth.lerp(Mth.sin(colorPhase) * 0.5f + 0.5f, 0.4f, 1.0f);
            float g = 0.5f;
            float b = Mth.lerp(Mth.cos(colorPhase) * 0.5f + 0.5f, 0.8f, 1.0f);
            float alpha = 0.85f;

            consumer.addVertex(pose, -size, -size, 0).setColor(r, g, b, alpha);
            consumer.addVertex(pose, -size, size, 0).setColor(r, g, b, alpha);
            consumer.addVertex(pose, size, size, 0).setColor(r, g, b, alpha);
            consumer.addVertex(pose, size, -size, 0).setColor(r, g, b, alpha);

            poseStack.popPose();
        }
    }

    private void renderVerticalPillars(PoseStack poseStack, MultiBufferSource buffer,
                                       float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        int pillarCount = 16;

        for (int i = 0; i < pillarCount; i++) {
            float angle = (float) (i * 2 * Math.PI / pillarCount);
            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;

            float pillarHeight = 30.0f;
            float offset = Mth.sin(age * 0.15f + i) * 6.0f;

            for (float h = 0; h < pillarHeight; h += 0.4f) {
                float heightProgress = h / pillarHeight;
                float waveX = Mth.sin(age * 0.2f + h * 0.4f + i) * 2.5f;
                float waveZ = Mth.cos(age * 0.2f + h * 0.4f + i) * 2.5f;

                float nextH = h + 0.4f;
                float nextWaveX = Mth.sin(age * 0.2f + nextH * 0.4f + i) * 2.5f;
                float nextWaveZ = Mth.cos(age * 0.2f + nextH * 0.4f + i) * 2.5f;

                float r = Mth.lerp(heightProgress, 0.4f, 0.7f);
                float g = Mth.lerp(heightProgress, 0.8f, 0.3f);
                float b = Mth.lerp(heightProgress, 1.0f, 0.9f);
                float alpha = (1.0f - heightProgress) * 0.6f;

                consumer.addVertex(pose, x + waveX, h + offset, z + waveZ)
                        .setColor(r, g, b, alpha);

                consumer.addVertex(pose, x + nextWaveX, nextH + offset, z + nextWaveZ)
                        .setColor(r, g, b, alpha);
            }
        }
    }

    private void renderGroundEffect(PoseStack poseStack, MultiBufferSource buffer,
                                    float radius, float age, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.endPortal());
        Matrix4f pose = poseStack.last().pose();

        int rings = 25;
        int segments = 48;

        for (int r = 0; r < rings; r++) {
            float innerRadius = (r / (float) rings) * radius;
            float outerRadius = ((r + 1) / (float) rings) * radius;

            float ringProgress = r / (float) rings;
            float waveHeight = Mth.sin(age * 0.15f - ringProgress * 6) * 0.7f;

            for (int s = 0; s < segments; s++) {
                float angle1 = (float) (s * 2 * Math.PI / segments);
                float angle2 = (float) ((s + 1) * 2 * Math.PI / segments);

                float x1Inner = Mth.cos(angle1) * innerRadius;
                float z1Inner = Mth.sin(angle1) * innerRadius;
                float x2Inner = Mth.cos(angle2) * innerRadius;
                float z2Inner = Mth.sin(angle2) * innerRadius;

                float x1Outer = Mth.cos(angle1) * outerRadius;
                float z1Outer = Mth.sin(angle1) * outerRadius;
                float x2Outer = Mth.cos(angle2) * outerRadius;
                float z2Outer = Mth.sin(angle2) * outerRadius;

                float colorPhase = (age * 0.08f + r + s) % 2.5f;
                float r1 = colorPhase > 1.5f ? 0.4f : 0.15f;
                float g1 = 0.3f;
                float b1 = colorPhase > 1.5f ? 0.9f : 0.4f;
                float alpha = 0.4f * (1.0f - ringProgress);

                consumer.addVertex(pose, x1Inner, waveHeight + 0.15f, z1Inner)
                        .setColor(r1, g1, b1, alpha);

                consumer.addVertex(pose, x1Outer, waveHeight + 0.15f, z1Outer)
                        .setColor(r1, g1, b1, alpha);

                consumer.addVertex(pose, x2Outer, waveHeight + 0.15f, z2Outer)
                        .setColor(r1, g1, b1, alpha);

                consumer.addVertex(pose, x2Inner, waveHeight + 0.15f, z2Inner)
                        .setColor(r1, g1, b1, alpha);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(DistortionFieldEntity entity) {
        return TEXTURE;
    }
}
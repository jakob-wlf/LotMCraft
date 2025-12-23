package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveMovableEffect;
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

public class HorrorAuraEffect extends ActiveMovableEffect {

    private final RandomSource random = RandomSource.create();
    private final List<DarknessLayer> layers = new ArrayList<>();
    private final List<VoidParticle> voidParticles = new ArrayList<>();
    private final List<DarknessSpike> spikes = new ArrayList<>();
    private final List<DarknessTentacle> tentacles = new ArrayList<>();
    private final List<VoidOrb> voidOrbs = new ArrayList<>();

    private float intensity;
    private float breathePhase;
    private float expansionRadius;
    private static final float MIN_RADIUS = 2.5f;
    private static final float MAX_RADIUS = 7.0f;

    public HorrorAuraEffect(Location location, int maxDuration, boolean infinite) {
        super(location, maxDuration, infinite);

        // Multiple layers of solid darkness
        for (int i = 0; i < 8; i++) {
            layers.add(new DarknessLayer(i));
        }

        // Massive void particles
        for (int i = 0; i < 200; i++) {
            voidParticles.add(new VoidParticle());
        }

        // Large imposing spikes
        for (int i = 0; i < 24; i++) {
            spikes.add(new DarknessSpike(i));
        }

        // Writhing tentacles
        for (int i = 0; i < 12; i++) {
            tentacles.add(new DarknessTentacle(i));
        }

        // Glowing void orbs
        for (int i = 0; i < 16; i++) {
            voidOrbs.add(new VoidOrb());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = getProgress();

        if (infinite) {
            intensity = 1f;
        } else {
            if (progress < 0.15f) {
                intensity = progress / 0.15f;
            } else if (progress > 0.8f) {
                intensity = 1f - ((progress - 0.8f) / 0.2f);
            } else {
                intensity = 1f;
            }
        }

        // Powerful breathing motion
        breathePhase = Mth.sin(tick * 0.03f);
        expansionRadius = MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * (0.5f + 0.5f * breathePhase);

        // Dramatic pulse
        float pulse = 1f + 0.3f * Mth.sin(tick * 0.08f);
        intensity *= pulse;

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Use lightning render type for all rendering - it supports colors properly
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Render massive darkness sphere at center
        renderCoreDarknessSphere(consumer, matrix, tick);

        // Render thick layers of darkness
        for (DarknessLayer layer : layers) {
            layer.update(tick, intensity, expansionRadius);
            renderDarknessLayer(consumer, matrix, layer);
        }

        // Render writhing tentacles
        for (DarknessTentacle tentacle : tentacles) {
            tentacle.update(tick, intensity, expansionRadius);
            renderTentacle(consumer, matrix, tentacle);
        }

        // Render imposing spikes
        for (DarknessSpike spike : spikes) {
            spike.update(tick, intensity, expansionRadius);
            renderDarknessSpike(consumer, matrix, spike);
        }

        // Render void orbs with glow
        for (VoidOrb orb : voidOrbs) {
            orb.update(tick, intensity, expansionRadius);
            if (orb.alpha > 0.01f) {
                renderVoidOrb(consumer, matrix, orb);
            }
        }

        // Render massive void particles
        for (VoidParticle particle : voidParticles) {
            particle.update(tick, intensity, expansionRadius);
            if (particle.alpha > 0.01f) {
                renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                        particle.size, particle.r, particle.g, particle.b, particle.alpha);
            }
        }

        // Render outer darkness wall
        renderDarknessWall(consumer, matrix, tick);

        poseStack.popPose();
    }

    private void renderCoreDarknessSphere(VertexConsumer consumer, Matrix4f matrix, float tick) {
        // Massive rotating dark sphere at center
        int segments = 32;
        int rings = 16;
        float coreRadius = 1.2f + 0.3f * Mth.sin(tick * 0.1f);
        float rotation = tick * 0.03f;

        for (int ring = 0; ring < rings; ring++) {
            float theta1 = (ring / (float) rings) * Mth.PI;
            float theta2 = ((ring + 1) / (float) rings) * Mth.PI;

            for (int i = 0; i < segments; i++) {
                float phi1 = (i / (float) segments) * Mth.TWO_PI + rotation;
                float phi2 = ((i + 1) / (float) segments) * Mth.TWO_PI + rotation;

                float x1 = Mth.sin(theta1) * Mth.cos(phi1) * coreRadius;
                float y1 = Mth.cos(theta1) * coreRadius;
                float z1 = Mth.sin(theta1) * Mth.sin(phi1) * coreRadius;

                float x2 = Mth.sin(theta1) * Mth.cos(phi2) * coreRadius;
                float y2 = Mth.cos(theta1) * coreRadius;
                float z2 = Mth.sin(theta1) * Mth.sin(phi2) * coreRadius;

                float x3 = Mth.sin(theta2) * Mth.cos(phi2) * coreRadius;
                float y3 = Mth.cos(theta2) * coreRadius;
                float z3 = Mth.sin(theta2) * Mth.sin(phi2) * coreRadius;

                float x4 = Mth.sin(theta2) * Mth.cos(phi1) * coreRadius;
                float y4 = Mth.cos(theta2) * coreRadius;
                float z4 = Mth.sin(theta2) * Mth.sin(phi1) * coreRadius;

                // Pure black core with slight blue tint
                float r = 0.0f;
                float g = 0.0f;
                float b = 0.05f;
                float alpha = intensity * 1f;

                addVertex(consumer, matrix, x1, y1, z1, r, g, b, alpha);
                addVertex(consumer, matrix, x2, y2, z2, r, g, b, alpha);
                addVertex(consumer, matrix, x3, y3, z3, r, g, b, alpha);
                addVertex(consumer, matrix, x4, y4, z4, r, g, b, alpha);
            }
        }
    }

    private void renderDarknessLayer(VertexConsumer consumer, Matrix4f matrix, DarknessLayer layer) {
        if (layer.alpha <= 0.01f) return;

        int segments = 48;
        float heightRange = 3f;

        for (int h = 0; h < 8; h++) {
            float y1 = -1.5f + (h / 8f) * heightRange;
            float y2 = -1.5f + ((h + 1) / 8f) * heightRange;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments) + layer.rotation;
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments) + layer.rotation;

                // Chaotic distortion
                float noise1 = Mth.sin(angle1 * 4f + layer.noiseOffset + h * 0.5f) * 0.4f;
                float noise2 = Mth.sin(angle2 * 4f + layer.noiseOffset + h * 0.5f) * 0.4f;

                float r1 = layer.radius + noise1;
                float r2 = layer.radius + noise2;

                float x1 = Mth.cos(angle1) * r1;
                float z1 = Mth.sin(angle1) * r1;
                float x2 = Mth.cos(angle2) * r2;
                float z2 = Mth.sin(angle2) * r2;

                // Deep black with blue highlights
                float heightFactor = h / 8f;
                float r = 0.0f;
                float g = 0.01f * heightFactor;
                float b = 0.08f + layer.blueIntensity * 0.1f * heightFactor;

                addVertex(consumer, matrix, x1, y1, z1, r, g, b, layer.alpha);
                addVertex(consumer, matrix, x2, y1, z2, r, g, b, layer.alpha);
                addVertex(consumer, matrix, x2, y2, z2, r, g * 1.2f, b * 1.2f, layer.alpha);
                addVertex(consumer, matrix, x1, y2, z1, r, g * 1.2f, b * 1.2f, layer.alpha);
            }
        }
    }

    private void renderTentacle(VertexConsumer consumer, Matrix4f matrix, DarknessTentacle tentacle) {
        if (tentacle.alpha <= 0.01f) return;

        int segments = 30;
        for (int i = 0; i < segments - 1; i++) {
            float t1 = i / (float) segments;
            float t2 = (i + 1) / (float) segments;

            Vec3 pos1 = tentacle.getPositionAt(t1);
            Vec3 pos2 = tentacle.getPositionAt(t2);

            float width = tentacle.width * (1f - t1 * 0.6f);

            Vec3 dir = pos2.subtract(pos1).normalize();
            Vec3 perp1 = new Vec3(-dir.z, 0, dir.x).normalize().scale(width);
            Vec3 perp2 = new Vec3(0, 1, 0).cross(dir).normalize().scale(width);

            // Render tentacle as thick quad strips
            Vec3[] corners1 = {
                    new Vec3(pos1.x - perp1.x, pos1.y - perp2.y, pos1.z - perp1.z),
                    new Vec3(pos1.x + perp1.x, pos1.y - perp2.y, pos1.z + perp1.z),
                    new Vec3(pos1.x + perp1.x, pos1.y + perp2.y, pos1.z + perp1.z),
                    new Vec3(pos1.x - perp1.x, pos1.y + perp2.y, pos1.z - perp1.z)
            };

            Vec3[] corners2 = {
                    new Vec3(pos2.x - perp1.x, pos2.y - perp2.y, pos2.z - perp1.z),
                    new Vec3(pos2.x + perp1.x, pos2.y - perp2.y, pos2.z + perp1.z),
                    new Vec3(pos2.x + perp1.x, pos2.y + perp2.y, pos2.z + perp1.z),
                    new Vec3(pos2.x - perp1.x, pos2.y + perp2.y, pos2.z - perp1.z)
            };

            float alpha = tentacle.alpha * (1f - t1 * 0.3f);
            float r = 0.0f;
            float g = 0.0f;
            float b = 0.05f + 0.05f * (1f - t1);

            for (int j = 0; j < 4; j++) {
                Vec3 c1a = corners1[j];
                Vec3 c1b = corners1[(j + 1) % 4];
                Vec3 c2a = corners2[j];
                Vec3 c2b = corners2[(j + 1) % 4];

                addVertex(consumer, matrix, (float)c1a.x, (float)c1a.y, (float)c1a.z, r, g, b, alpha);
                addVertex(consumer, matrix, (float)c1b.x, (float)c1b.y, (float)c1b.z, r, g, b, alpha);
                addVertex(consumer, matrix, (float)c2b.x, (float)c2b.y, (float)c2b.z, r, g, b * 1.3f, alpha);
                addVertex(consumer, matrix, (float)c2a.x, (float)c2a.y, (float)c2a.z, r, g, b * 1.3f, alpha);
            }
        }
    }

    private void renderDarknessSpike(VertexConsumer consumer, Matrix4f matrix, DarknessSpike spike) {
        if (spike.alpha <= 0.01f) return;

        float baseX = Mth.cos(spike.angle) * spike.distance;
        float baseZ = Mth.sin(spike.angle) * spike.distance;
        float baseY = -1f;

        float tipX = Mth.cos(spike.angle) * (spike.distance + spike.length * 1.5f);
        float tipZ = Mth.sin(spike.angle) * (spike.distance + spike.length * 1.5f);
        float tipY = baseY + spike.height * 2f;

        int sides = 8;
        float baseWidth = spike.width;

        for (int i = 0; i < sides; i++) {
            float a1 = (float) (i * Math.PI * 2 / sides) + spike.rotation;
            float a2 = (float) ((i + 1) * Math.PI * 2 / sides) + spike.rotation;

            Vec3 perp1 = new Vec3(
                    Mth.cos(spike.angle + Mth.HALF_PI) * Mth.cos(a1) * baseWidth,
                    Mth.sin(a1) * baseWidth,
                    Mth.sin(spike.angle + Mth.HALF_PI) * Mth.cos(a1) * baseWidth
            );
            Vec3 perp2 = new Vec3(
                    Mth.cos(spike.angle + Mth.HALF_PI) * Mth.cos(a2) * baseWidth,
                    Mth.sin(a2) * baseWidth,
                    Mth.sin(spike.angle + Mth.HALF_PI) * Mth.cos(a2) * baseWidth
            );

            float r = 0.0f;
            float g = 0.0f;
            float b = 0.03f;

            addVertex(consumer, matrix,
                    (float)(baseX + perp1.x), (float)(baseY + perp1.y), (float)(baseZ + perp1.z),
                    r, g, b, spike.alpha);
            addVertex(consumer, matrix,
                    (float)(baseX + perp2.x), (float)(baseY + perp2.y), (float)(baseZ + perp2.z),
                    r, g, b, spike.alpha);
            addVertex(consumer, matrix, tipX, tipY, tipZ, r, g, b * 2f, spike.alpha * 0.7f);
            addVertex(consumer, matrix, tipX, tipY, tipZ, r, g, b * 2f, spike.alpha * 0.7f);
        }
    }

    private void renderVoidOrb(VertexConsumer consumer, Matrix4f matrix, VoidOrb orb) {
        // Large glowing orbs
        float size = orb.size;

        // Multiple layers for glow effect
        for (int layer = 0; layer < 3; layer++) {
            float layerSize = size * (1f + layer * 0.5f);
            float layerAlpha = orb.alpha / (1f + layer * 0.8f);

            renderBillboardQuad(consumer, matrix, orb.x, orb.y, orb.z, layerSize,
                    0.0f, 0.1f * (3 - layer), 0.4f * (3 - layer), layerAlpha);
        }
    }

    private void renderDarknessWall(VertexConsumer consumer, Matrix4f matrix, float tick) {
        // Massive wall of darkness at the perimeter
        int segments = 64;
        float wallRadius = expansionRadius * 1.1f;
        float wallHeight = 4f;
        float rotation = tick * 0.015f;

        for (int h = 0; h < 6; h++) {
            float y1 = -2f + (h / 6f) * wallHeight;
            float y2 = -2f + ((h + 1) / 6f) * wallHeight;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments) + rotation;
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments) + rotation;

                float turbulence1 = Mth.sin(angle1 * 6f + tick * 0.1f + h) * 0.5f;
                float turbulence2 = Mth.sin(angle2 * 6f + tick * 0.1f + h) * 0.5f;

                float x1 = Mth.cos(angle1) * (wallRadius + turbulence1);
                float z1 = Mth.sin(angle1) * (wallRadius + turbulence1);
                float x2 = Mth.cos(angle2) * (wallRadius + turbulence2);
                float z2 = Mth.sin(angle2) * (wallRadius + turbulence2);

                float heightFactor = h / 6f;
                float r = 0.0f;
                float g = 0.0f;
                float b = 0.02f + 0.06f * heightFactor;
                float alpha = intensity * 0.9f;

                addVertex(consumer, matrix, x1, y1, z1, r, g, b, alpha);
                addVertex(consumer, matrix, x2, y1, z2, r, g, b, alpha);
                addVertex(consumer, matrix, x2, y2, z2, r, g * 1.5f, b * 1.5f, alpha);
                addVertex(consumer, matrix, x1, y2, z1, r, g * 1.5f, b * 1.5f, alpha);
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

    private class DarknessLayer {
        float radius;
        float alpha;
        float rotation;
        float noiseOffset;
        float phaseOffset;
        float blueIntensity;

        DarknessLayer(int index) {
            this.phaseOffset = index * 0.125f;
            this.blueIntensity = random.nextFloat();
        }

        void update(float tick, float intensity, float expansionRadius) {
            // Use continuous sine wave instead of modulo for smooth cycling
            float cycle = 0.5f + 0.5f * Mth.sin((tick * 0.03f + phaseOffset) * Mth.TWO_PI);
            this.radius = expansionRadius * (0.5f + cycle * 0.5f);
            this.rotation = tick * 0.02f * (phaseOffset > 0.5f ? 1 : -1);
            this.noiseOffset = tick * 0.15f;

            // Smooth fade in/out based on the cycle
            float fadeAmount = Mth.sin(cycle * Mth.PI);
            this.alpha = intensity * 0.85f * (0.3f + 0.7f * fadeAmount);
        }
    }

    private class VoidParticle {
        float x, y, z;
        float angle;
        float distance;
        float distancePhase;
        float heightPhase;
        float size;
        float alpha;
        float r, g, b;

        VoidParticle() {
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.distancePhase = random.nextFloat() * Mth.TWO_PI;
            this.heightPhase = random.nextFloat() * Mth.TWO_PI;
            this.size = 0.12f + random.nextFloat() * 0.15f;

            this.r = 0.0f;
            this.g = 0.0f;
            this.b = 0.02f + random.nextFloat() * 0.05f;
        }

        void update(float tick, float intensity, float expansionRadius) {
            this.angle += 0.015f;
            float breatheInfluence = 0.5f + 0.5f * Mth.sin(tick * 0.03f + distancePhase);
            this.distance = expansionRadius * breatheInfluence * (0.2f + random.nextFloat() * 0.8f);

            this.x = Mth.cos(angle) * distance;
            this.z = Mth.sin(angle) * distance;

            float heightWave = Mth.sin(tick * 0.05f + heightPhase) * 1.2f;
            this.y = heightWave;

            float distFactor = distance / (expansionRadius * 1.2f);
            this.alpha = intensity * (0.8f - distFactor * 0.3f);
            this.alpha = Mth.clamp(this.alpha, 0f, 1f);
        }
    }

    private class DarknessSpike {
        float angle;
        float distance;
        float length;
        float height;
        float width;
        float alpha;
        float growthPhase;
        float rotation;

        DarknessSpike(int index) {
            this.angle = (index / 24f) * Mth.TWO_PI;
            this.growthPhase = random.nextFloat() * Mth.TWO_PI;
            this.height = 1.5f + random.nextFloat() * 1f;
            this.width = 0.2f + random.nextFloat() * 0.15f;
        }

        void update(float tick, float intensity, float expansionRadius) {
            float growth = 0.6f + 0.4f * Mth.sin(tick * 0.03f + growthPhase);
            this.distance = expansionRadius * 0.85f;
            this.length = growth * 1.2f;
            this.rotation = tick * 0.05f;
            this.alpha = intensity * growth * 0.95f;
        }
    }

    private class DarknessTentacle {
        float baseAngle;
        float alpha;
        float width;
        float length;
        float waveSpeed;
        float phaseOffset;

        DarknessTentacle(int index) {
            this.baseAngle = (index / 12f) * Mth.TWO_PI;
            this.width = 0.25f + random.nextFloat() * 0.2f;
            this.length = 0f;
            this.waveSpeed = 0.8f + random.nextFloat() * 0.5f;
            this.phaseOffset = random.nextFloat() * Mth.TWO_PI;
        }

        void update(float tick, float intensity, float expansionRadius) {
            this.length = expansionRadius * 1.3f;
            this.alpha = intensity * 0.9f;
        }

        Vec3 getPositionAt(float t) {
            float currentLength = length * t;
            float wave1 = Mth.sin(t * 4f + currentTick * 0.08f * waveSpeed + phaseOffset) * 0.8f;
            float wave2 = Mth.cos(t * 3f + currentTick * 0.06f * waveSpeed) * 0.6f;

            float x = Mth.cos(baseAngle) * currentLength + wave1 * Mth.sin(baseAngle);
            float y = -0.5f + t * 1.5f + wave2;
            float z = Mth.sin(baseAngle) * currentLength - wave1 * Mth.cos(baseAngle);

            return new Vec3(x, y, z);
        }
    }

    private class VoidOrb {
        float x, y, z;
        float angle;
        float height;
        float distance;
        float size;
        float alpha;
        float pulsePhase;

        VoidOrb() {
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.height = random.nextFloat() * 2f - 1f;
            this.size = 0.3f + random.nextFloat() * 0.3f;
            this.pulsePhase = random.nextFloat() * Mth.TWO_PI;
        }

        void update(float tick, float intensity, float expansionRadius) {
            this.angle += 0.02f;
            this.distance = expansionRadius * (0.6f + 0.2f * Mth.sin(tick * 0.04f + pulsePhase));

            this.x = Mth.cos(angle) * distance;
            this.y = height + Mth.sin(tick * 0.06f + angle) * 0.4f;
            this.z = Mth.sin(angle) * distance;

            float pulse = 0.7f + 0.3f * Mth.sin(tick * 0.1f + pulsePhase);
            this.alpha = intensity * pulse;
        }
    }
}
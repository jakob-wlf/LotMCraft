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

public class LifeAuraEffect extends ActiveMovableEffect {

    private final RandomSource random = RandomSource.create();
    private final List<FloatingLeaf> leaves = new ArrayList<>();
    private final List<EnergyWisp> wisps = new ArrayList<>();
    private final List<GrowthVine> vines = new ArrayList<>();
    private final List<GoldenOrb> goldenOrbs = new ArrayList<>();
    private final List<LifeParticle> lifeParticles = new ArrayList<>();
    private final List<RadiantRing> rings = new ArrayList<>();

    private static final float CYCLE_DURATION = 80f;

    public LifeAuraEffect(Location location, int maxDuration, boolean infinite) {
        super(location, maxDuration, infinite);

        // Floating leaves
        for (int i = 0; i < 30; i++) {
            leaves.add(new FloatingLeaf());
        }

        // Energy wisps
        for (int i = 0; i < 45; i++) {
            wisps.add(new EnergyWisp());
        }

        // Growing vines
        for (int i = 0; i < 8; i++) {
            vines.add(new GrowthVine(i));
        }

        // Golden orbs
        for (int i = 0; i < 12; i++) {
            goldenOrbs.add(new GoldenOrb());
        }

        // Life particles
        for (int i = 0; i < 120; i++) {
            lifeParticles.add(new LifeParticle());
        }

        // Radiant rings
        for (int i = 0; i < 5; i++) {
            rings.add(new RadiantRing(i));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Calculate looping progress (0 to 1 and back to 0 smoothly)
        float loopProgress = (tick % CYCLE_DURATION) / CYCLE_DURATION;

        // Render core energy sphere
        renderCoreEnergySphere(consumer, matrix, tick, loopProgress);

        // Render radiant rings
        for (RadiantRing ring : rings) {
            ring.update(tick, loopProgress);
            renderRadiantRing(consumer, matrix, ring);
        }

        // Render growth vines
        for (GrowthVine vine : vines) {
            vine.update(tick, loopProgress);
            renderGrowthVine(consumer, matrix, vine);
        }

        // Render golden orbs
        for (GoldenOrb orb : goldenOrbs) {
            orb.update(tick, loopProgress);
            if (orb.alpha > 0.01f) {
                renderGoldenOrb(consumer, matrix, orb);
            }
        }

        // Render energy wisps
        for (EnergyWisp wisp : wisps) {
            wisp.update(tick, loopProgress);
            if (wisp.alpha > 0.01f) {
                renderBillboardQuad(consumer, matrix, wisp.x, wisp.y, wisp.z,
                        wisp.size, wisp.r, wisp.g, wisp.b, wisp.alpha);
            }
        }

        // Render floating leaves
        for (FloatingLeaf leaf : leaves) {
            leaf.update(tick, loopProgress);
            if (leaf.alpha > 0.01f) {
                renderLeaf(consumer, matrix, leaf);
            }
        }

        // Render life particles
        for (LifeParticle particle : lifeParticles) {
            particle.update(tick, loopProgress);
            if (particle.alpha > 0.01f) {
                renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                        particle.size, particle.r, particle.g, particle.b, particle.alpha);
            }
        }

        poseStack.popPose();
    }

    private void renderCoreEnergySphere(VertexConsumer consumer, Matrix4f matrix, float tick, float loopProgress) {
        int segments = 24;
        int rings = 12;
        
        // Pulsing radius that loops smoothly
        float pulsePhase = Mth.sin(loopProgress * Mth.TWO_PI);
        float coreRadius = 0.6f + 0.2f * pulsePhase;
        float rotation = tick * 0.05f;

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

                // Vibrant green-gold core
                float heightFactor = (Mth.cos(theta1) + 1f) * 0.5f;
                float r = 0.4f + 0.3f * heightFactor;
                float g = 0.8f;
                float b = 0.1f + 0.2f * heightFactor;
                float alpha = 0.7f + 0.2f * pulsePhase;

                addVertex(consumer, matrix, x1, y1, z1, r, g, b, alpha);
                addVertex(consumer, matrix, x2, y2, z2, r, g, b, alpha);
                addVertex(consumer, matrix, x3, y3, z3, r, g, b, alpha);
                addVertex(consumer, matrix, x4, y4, z4, r, g, b, alpha);
            }
        }
    }

    private void renderRadiantRing(VertexConsumer consumer, Matrix4f matrix, RadiantRing ring) {
        if (ring.alpha <= 0.01f) return;

        int segments = 48;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments) + ring.rotation;
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments) + ring.rotation;

            float wave1 = Mth.sin(angle1 * 3f + ring.waveOffset) * 0.15f;
            float wave2 = Mth.sin(angle2 * 3f + ring.waveOffset) * 0.15f;

            float innerRadius = ring.radius * 0.85f;
            float outerRadius = ring.radius * 1.15f;

            float x1i = Mth.cos(angle1) * (innerRadius + wave1);
            float z1i = Mth.sin(angle1) * (innerRadius + wave1);
            float x2i = Mth.cos(angle2) * (innerRadius + wave2);
            float z2i = Mth.sin(angle2) * (innerRadius + wave2);

            float x1o = Mth.cos(angle1) * (outerRadius + wave1);
            float z1o = Mth.sin(angle1) * (outerRadius + wave1);
            float x2o = Mth.cos(angle2) * (outerRadius + wave2);
            float z2o = Mth.sin(angle2) * (outerRadius + wave2);

            float segmentFactor = (float) i / segments;
            float r = ring.isGolden ? (0.8f + 0.2f * segmentFactor) : 0.2f;
            float g = ring.isGolden ? (0.6f - 0.1f * segmentFactor) : (0.7f + 0.3f * segmentFactor);
            float b = 0.15f;

            addVertex(consumer, matrix, x1i, ring.height, z1i, r, g, b, ring.alpha);
            addVertex(consumer, matrix, x2i, ring.height, z2i, r, g, b, ring.alpha);
            addVertex(consumer, matrix, x2o, ring.height, z2o, r * 0.7f, g * 0.7f, b * 0.7f, ring.alpha * 0.5f);
            addVertex(consumer, matrix, x1o, ring.height, z1o, r * 0.7f, g * 0.7f, b * 0.7f, ring.alpha * 0.5f);
        }
    }

    private void renderGrowthVine(VertexConsumer consumer, Matrix4f matrix, GrowthVine vine) {
        if (vine.alpha <= 0.01f) return;

        int segments = 25;
        for (int i = 0; i < segments - 1; i++) {
            float t1 = i / (float) segments;
            float t2 = (i + 1) / (float) segments;

            Vec3 pos1 = vine.getPositionAt(t1);
            Vec3 pos2 = vine.getPositionAt(t2);

            float width = vine.width * (1f - t1 * 0.5f) * vine.growthFactor;

            Vec3 dir = pos2.subtract(pos1).normalize();
            Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(width);

            float progressFactor = t1;
            float r = 0.2f + 0.3f * progressFactor;
            float g = 0.5f + 0.3f * (1f - progressFactor);
            float b = 0.1f;
            float alpha = vine.alpha * (1f - t1 * 0.4f);

            float x1 = (float) (pos1.x - perp.x);
            float y1 = (float) (pos1.y - perp.y);
            float z1 = (float) (pos1.z - perp.z);

            float x2 = (float) (pos1.x + perp.x);
            float y2 = (float) (pos1.y + perp.y);
            float z2 = (float) (pos1.z + perp.z);

            float x3 = (float) (pos2.x + perp.x);
            float y3 = (float) (pos2.y + perp.y);
            float z3 = (float) (pos2.z + perp.z);

            float x4 = (float) (pos2.x - perp.x);
            float y4 = (float) (pos2.y - perp.y);
            float z4 = (float) (pos2.z - perp.z);

            addVertex(consumer, matrix, x1, y1, z1, r, g, b, alpha);
            addVertex(consumer, matrix, x2, y2, z2, r, g, b, alpha);
            addVertex(consumer, matrix, x3, y3, z3, r * 1.1f, g * 1.1f, b, alpha);
            addVertex(consumer, matrix, x4, y4, z4, r * 1.1f, g * 1.1f, b, alpha);
        }
    }

    private void renderGoldenOrb(VertexConsumer consumer, Matrix4f matrix, GoldenOrb orb) {
        float size = orb.size;

        for (int layer = 0; layer < 3; layer++) {
            float layerSize = size * (1f + layer * 0.4f);
            float layerAlpha = orb.alpha / (1f + layer);

            float r = 0.9f - layer * 0.1f;
            float g = 0.7f - layer * 0.15f;
            float b = 0.2f;

            renderBillboardQuad(consumer, matrix, orb.x, orb.y, orb.z, layerSize,
                    r, g, b, layerAlpha);
        }
    }

    private void renderLeaf(VertexConsumer consumer, Matrix4f matrix, FloatingLeaf leaf) {
        float halfWidth = leaf.width * 0.5f;
        float halfHeight = leaf.height * 0.5f;

        Vec3 right = new Vec3(Mth.cos(leaf.rotation), 0, Mth.sin(leaf.rotation)).scale(halfWidth);
        Vec3 up = new Vec3(0, 1, 0).scale(halfHeight);

        float x1 = (float) (leaf.x - right.x - up.x);
        float y1 = (float) (leaf.y - right.y - up.y);
        float z1 = (float) (leaf.z - right.z - up.z);

        float x2 = (float) (leaf.x - right.x + up.x);
        float y2 = (float) (leaf.y - right.y + up.y);
        float z2 = (float) (leaf.z - right.z + up.z);

        float x3 = (float) (leaf.x + right.x + up.x);
        float y3 = (float) (leaf.y + right.y + up.y);
        float z3 = (float) (leaf.z + right.z + up.z);

        float x4 = (float) (leaf.x + right.x - up.x);
        float y4 = (float) (leaf.y + right.y - up.y);
        float z4 = (float) (leaf.z + right.z - up.z);

        addVertex(consumer, matrix, x1, y1, z1, 0.3f, 0.7f, 0.2f, leaf.alpha);
        addVertex(consumer, matrix, x2, y2, z2, 0.4f, 0.8f, 0.2f, leaf.alpha);
        addVertex(consumer, matrix, x3, y3, z3, 0.3f, 0.7f, 0.2f, leaf.alpha);
        addVertex(consumer, matrix, x4, y4, z4, 0.2f, 0.6f, 0.15f, leaf.alpha);
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

    private class FloatingLeaf {
        float x, y, z;
        float angle;
        float orbitRadius;
        float heightPhase;
        float rotationSpeed;
        float rotation;
        float width, height;
        float alpha;
        float spawnPhase;

        FloatingLeaf() {
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.orbitRadius = 1.5f + random.nextFloat() * 2f;
            this.heightPhase = random.nextFloat() * Mth.TWO_PI;
            this.rotationSpeed = 0.02f + random.nextFloat() * 0.03f;
            this.width = 0.15f + random.nextFloat() * 0.1f;
            this.height = 0.2f + random.nextFloat() * 0.15f;
            this.spawnPhase = random.nextFloat();
        }

        void update(float tick, float loopProgress) {
            // Smooth loop for spawn/despawn
            float adjustedProgress = (loopProgress + spawnPhase) % 1f;
            float cycleFade = Mth.sin(adjustedProgress * Mth.PI);
            
            this.angle += rotationSpeed;
            this.rotation += rotationSpeed * 2f;

            float orbitPhase = (tick * 0.01f + spawnPhase) % Mth.TWO_PI;
            float currentOrbit = orbitRadius * (0.7f + 0.3f * Mth.sin(loopProgress * Mth.TWO_PI));

            this.x = Mth.cos(angle) * currentOrbit;
            this.z = Mth.sin(angle) * currentOrbit;
            this.y = Mth.sin(tick * 0.04f + heightPhase) * 1.5f;

            this.alpha = 0.6f * cycleFade;
        }
    }

    private class EnergyWisp {
        float x, y, z;
        float angle;
        float spiralHeight;
        float spiralRadius;
        float speed;
        float size;
        float r, g, b;
        float alpha;
        float phaseOffset;

        EnergyWisp() {
            this.phaseOffset = random.nextFloat();
            this.spiralRadius = 0.5f + random.nextFloat() * 2.5f;
            this.speed = 0.015f + random.nextFloat() * 0.02f;
            this.size = 0.08f + random.nextFloat() * 0.08f;

            if (random.nextFloat() > 0.6f) {
                this.r = 0.8f;
                this.g = 0.6f;
                this.b = 0.2f;
            } else {
                this.r = 0.3f;
                this.g = 0.8f;
                this.b = 0.2f;
            }
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + phaseOffset) % 1f;
            float spiralPhase = adjustedProgress * Mth.TWO_PI;

            this.angle = spiralPhase;
            this.spiralHeight = Mth.sin(spiralPhase) * 2f;

            float radiusPulse = 1f + 0.3f * Mth.sin(loopProgress * Mth.TWO_PI * 2f + phaseOffset * Mth.TWO_PI);
            float currentRadius = spiralRadius * radiusPulse;

            this.x = Mth.cos(angle) * currentRadius;
            this.y = spiralHeight;
            this.z = Mth.sin(angle) * currentRadius;

            this.alpha = 0.7f * Mth.sin(adjustedProgress * Mth.PI);
        }
    }

    private class GrowthVine {
        float baseAngle;
        float length;
        float width;
        float alpha;
        float growthFactor;
        float waveSpeed;
        float phaseOffset;

        GrowthVine(int index) {
            this.baseAngle = (index / 8f) * Mth.TWO_PI;
            this.width = 0.12f + random.nextFloat() * 0.08f;
            this.waveSpeed = 0.8f + random.nextFloat() * 0.4f;
            this.phaseOffset = random.nextFloat();
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + phaseOffset) % 1f;
            
            // Smooth growth cycle
            this.growthFactor = Mth.sin(adjustedProgress * Mth.PI);
            this.length = 3f * growthFactor;
            this.alpha = 0.8f * growthFactor;
        }

        Vec3 getPositionAt(float t) {
            float actualT = t * growthFactor;
            float currentLength = length * actualT;
            
            float wave = Mth.sin(actualT * 3f + currentTick * 0.05f * waveSpeed) * 0.4f;
            
            float x = Mth.cos(baseAngle) * (currentLength * 0.5f) + wave * Mth.sin(baseAngle);
            float y = actualT * 2.5f - 1f;
            float z = Mth.sin(baseAngle) * (currentLength * 0.5f) - wave * Mth.cos(baseAngle);

            return new Vec3(x, y, z);
        }
    }

    private class GoldenOrb {
        float x, y, z;
        float angle;
        float orbitRadius;
        float verticalPhase;
        float size;
        float alpha;
        float pulsePhase;

        GoldenOrb() {
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.orbitRadius = 2f + random.nextFloat() * 1.5f;
            this.verticalPhase = random.nextFloat() * Mth.TWO_PI;
            this.size = 0.2f + random.nextFloat() * 0.15f;
            this.pulsePhase = random.nextFloat();
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + pulsePhase) % 1f;
            
            this.angle += 0.025f;

            float orbitPulse = 0.8f + 0.2f * Mth.sin(loopProgress * Mth.TWO_PI);
            float currentOrbit = orbitRadius * orbitPulse;

            this.x = Mth.cos(angle) * currentOrbit;
            this.y = Mth.sin(tick * 0.05f + verticalPhase) * 1.2f;
            this.z = Mth.sin(angle) * currentOrbit;

            float pulse = 0.6f + 0.4f * Mth.sin(adjustedProgress * Mth.TWO_PI * 2f);
            this.alpha = pulse * Mth.sin(adjustedProgress * Mth.PI);
        }
    }

    private class LifeParticle {
        float x, y, z;
        float velocityY;
        float angle;
        float radius;
        float size;
        float r, g, b;
        float alpha;
        float lifePhase;

        LifeParticle() {
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.radius = random.nextFloat() * 3.5f;
            this.velocityY = 0.02f + random.nextFloat() * 0.03f;
            this.size = 0.05f + random.nextFloat() * 0.06f;
            this.lifePhase = random.nextFloat();

            if (random.nextFloat() > 0.5f) {
                this.r = 0.7f;
                this.g = 0.5f;
                this.b = 0.15f;
            } else {
                this.r = 0.25f;
                this.g = 0.75f;
                this.b = 0.15f;
            }
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + lifePhase) % 1f;
            
            float yProgress = adjustedProgress * 4f;
            this.y = -1.5f + (yProgress % 4f);

            this.angle += 0.02f;
            float radiusWave = 1f + 0.2f * Mth.sin(loopProgress * Mth.TWO_PI);
            
            this.x = Mth.cos(angle) * radius * radiusWave;
            this.z = Mth.sin(angle) * radius * radiusWave;

            float verticalFade = 1f - Math.abs((yProgress % 4f) - 2f) / 2f;
            this.alpha = 0.6f * verticalFade * Mth.sin(adjustedProgress * Mth.PI);
        }
    }

    private class RadiantRing {
        float radius;
        float height;
        float alpha;
        float rotation;
        float waveOffset;
        boolean isGolden;
        float phaseOffset;

        RadiantRing(int index) {
            this.phaseOffset = index / 5f;
            this.isGolden = index % 2 == 0;
            this.height = -1f + index * 0.3f;
        }

        void update(float tick, float loopProgress) {
            float adjustedProgress = (loopProgress + phaseOffset) % 1f;
            
            // Expanding and contracting ring
            this.radius = 1.5f + 2f * Mth.sin(adjustedProgress * Mth.PI);
            this.rotation = tick * 0.03f * (isGolden ? 1 : -1);
            this.waveOffset = tick * 0.1f;
            
            // Fade in and out smoothly
            this.alpha = 0.5f * Mth.sin(adjustedProgress * Mth.PI);
        }
    }
}
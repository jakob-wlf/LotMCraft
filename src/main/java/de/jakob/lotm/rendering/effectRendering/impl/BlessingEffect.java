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

public class BlessingEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<AscendingSpiral> spirals = new ArrayList<>();
    private final List<LuckParticle> particles = new ArrayList<>();
    private final List<BlessingRune> runes = new ArrayList<>();
    private final List<LightRay> lightRays = new ArrayList<>();
    private final List<FortuneOrb> fortuneOrbs = new ArrayList<>();
    
    private static final float PRIMARY_R = 170f / 255f;
    private static final float PRIMARY_G = 204f / 255f;
    private static final float PRIMARY_B = 230f / 255f;
    
    private float blessingIntensity = 0f;
    private float spiralRotation = 0f;

    public BlessingEffect(double x, double y, double z) {
        super(x, y, z, 20 * 2); // 8 seconds duration

        // Create multiple ascending spirals
        for (int i = 0; i < 6; i++) {
            spirals.add(new AscendingSpiral(i));
        }

        // Create blessing particles that rise upward
        for (int i = 0; i < 150; i++) {
            particles.add(new LuckParticle());
        }

        // Create floating blessing runes
        for (int i = 0; i < 10; i++) {
            runes.add(new BlessingRune());
        }

        // Create radiant light rays
        for (int i = 0; i < 12; i++) {
            lightRays.add(new LightRay(i));
        }

        // Create fortune orbs that circle
        for (int i = 0; i < 8; i++) {
            fortuneOrbs.add(new FortuneOrb(i));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;

        // Blessing builds up, sustains, then fades
        if (progress < 0.15f) {
            blessingIntensity = progress / 0.15f;
        } else if (progress > 0.85f) {
            blessingIntensity = 1f - ((progress - 0.85f) / 0.15f);
        } else {
            blessingIntensity = 1f;
        }

        spiralRotation += 0.06f;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render in order for proper blending
        renderLightBeam(poseStack, bufferSource, progress);
        renderLightRays(poseStack, bufferSource, progress);
        renderSpirals(poseStack, bufferSource, progress);
        renderFortuneOrbs(poseStack, bufferSource, progress);
        renderRunes(poseStack, bufferSource, progress);
        renderParticles(poseStack, bufferSource, progress);
        renderBlessingAura(poseStack, bufferSource, progress);
        renderHaloRings(poseStack, bufferSource, progress);
        renderShimmeringGlow(poseStack, bufferSource, progress);

        poseStack.popPose();
    }

    private void renderLightBeam(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Central pillar of light
        float beamRadius = 0.6f + Mth.sin(currentTick * 0.08f) * 0.1f;
        int segments = 24;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * beamRadius;
            float z1 = Mth.sin(angle1) * beamRadius;
            float x2 = Mth.cos(angle2) * beamRadius;
            float z2 = Mth.sin(angle2) * beamRadius;

            float alpha = 0.5f * blessingIntensity;

            // Bottom to top gradient
            addVertex(consumer, matrix, x1, -1f, z1,
                    PRIMARY_R * 0.7f, PRIMARY_G * 0.7f, PRIMARY_B * 0.7f, alpha * 0.3f);
            addVertex(consumer, matrix, x2, -1f, z2,
                    PRIMARY_R * 0.7f, PRIMARY_G * 0.7f, PRIMARY_B * 0.7f, alpha * 0.3f);
            addVertex(consumer, matrix, x2, 3f, z2,
                    1f, 1f, 1f, alpha);
            addVertex(consumer, matrix, x1, 3f, z1,
                    1f, 1f, 1f, alpha);
        }
    }

    private void renderSpirals(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (AscendingSpiral spiral : spirals) {
            spiral.update(progress, blessingIntensity, spiralRotation);

            if (spiral.alpha <= 0f) continue;

            // Draw spiral as connected segments
            for (int i = 0; i < spiral.points.size() - 1; i++) {
                Vec3 p1 = spiral.points.get(i);
                Vec3 p2 = spiral.points.get(i + 1);

                Vec3 dir = new Vec3(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(spiral.thickness);

                float t = i / (float) spiral.points.size();
                float segmentAlpha = spiral.alpha * blessingIntensity * (0.6f + t * 0.4f);

                // Gradient from blue to white
                float brightness = 0.5f + t * 0.5f;
                float r = PRIMARY_R + (1f - PRIMARY_R) * brightness;
                float g = PRIMARY_G + (1f - PRIMARY_G) * brightness;
                float b = PRIMARY_B + (1f - PRIMARY_B) * brightness;

                addVertex(consumer, matrix,
                        (float)(p1.x - perp.x), (float)(p1.y - perp.y), (float)(p1.z - perp.z),
                        r, g, b, segmentAlpha);
                addVertex(consumer, matrix,
                        (float)(p1.x + perp.x), (float)(p1.y + perp.y), (float)(p1.z + perp.z),
                        r, g, b, segmentAlpha);
                addVertex(consumer, matrix,
                        (float)(p2.x + perp.x), (float)(p2.y + perp.y), (float)(p2.z + perp.z),
                        r, g, b, segmentAlpha * 1.2f);
                addVertex(consumer, matrix,
                        (float)(p2.x - perp.x), (float)(p2.y - perp.y), (float)(p2.z - perp.z),
                        r, g, b, segmentAlpha * 1.2f);
            }
        }
    }

    private void renderParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (LuckParticle particle : particles) {
            particle.update(progress, blessingIntensity);

            if (particle.alpha <= 0f) continue;

            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, particle.r, particle.g, particle.b, particle.alpha * blessingIntensity);
        }
    }

    private void renderRunes(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (BlessingRune rune : runes) {
            rune.update(progress, blessingIntensity, spiralRotation);

            if (rune.alpha <= 0f) continue;

            float size = rune.size;
            float alpha = rune.alpha * blessingIntensity;

            // Draw rune as a star/cross pattern
            // Vertical line
            addVertex(consumer, matrix, rune.x - size * 0.08f, rune.y - size, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x + size * 0.08f, rune.y - size, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x + size * 0.08f, rune.y + size, rune.z,
                    1f, 1f, 1f, alpha);
            addVertex(consumer, matrix, rune.x - size * 0.08f, rune.y + size, rune.z,
                    1f, 1f, 1f, alpha);

            // Horizontal line
            addVertex(consumer, matrix, rune.x - size, rune.y - size * 0.08f, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x + size, rune.y - size * 0.08f, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x + size, rune.y + size * 0.08f, rune.z,
                    1f, 1f, 1f, alpha);
            addVertex(consumer, matrix, rune.x - size, rune.y + size * 0.08f, rune.z,
                    1f, 1f, 1f, alpha);

            // Diagonal lines for star effect
            float diagSize = size * 0.7f;
            addVertex(consumer, matrix, rune.x - diagSize * 0.08f, rune.y - diagSize, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha * 0.7f);
            addVertex(consumer, matrix, rune.x + diagSize * 0.08f, rune.y - diagSize, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha * 0.7f);
            addVertex(consumer, matrix, rune.x + diagSize * 0.08f, rune.y + diagSize, rune.z,
                    1f, 1f, 1f, alpha * 0.7f);
            addVertex(consumer, matrix, rune.x - diagSize * 0.08f, rune.y + diagSize, rune.z,
                    1f, 1f, 1f, alpha * 0.7f);
        }
    }

    private void renderLightRays(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (LightRay ray : lightRays) {
            ray.update(progress, blessingIntensity);

            if (ray.alpha <= 0f) continue;

            Vec3 start = ray.start;
            Vec3 end = ray.end;

            Vec3 dir = new Vec3(end.x - start.x, end.y - start.y, end.z - start.z);
            Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(ray.width);

            float alpha = ray.alpha * blessingIntensity;

            addVertex(consumer, matrix,
                    (float)(start.x - perp.x), (float)(start.y - perp.y), (float)(start.z - perp.z),
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha * 0.3f);
            addVertex(consumer, matrix,
                    (float)(start.x + perp.x), (float)(start.y + perp.y), (float)(start.z + perp.z),
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha * 0.3f);
            addVertex(consumer, matrix,
                    (float)(end.x + perp.x), (float)(end.y + perp.y), (float)(end.z + perp.z),
                    1f, 1f, 1f, alpha);
            addVertex(consumer, matrix,
                    (float)(end.x - perp.x), (float)(end.y - perp.y), (float)(end.z - perp.z),
                    1f, 1f, 1f, alpha);
        }
    }

    private void renderFortuneOrbs(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (FortuneOrb orb : fortuneOrbs) {
            orb.update(progress, blessingIntensity, spiralRotation);

            if (orb.alpha <= 0f) continue;

            // Render orb as a glowing sphere (simplified as billboard)
            float brightness = 0.8f + 0.2f * Mth.sin(currentTick * 0.15f + orb.phaseOffset);
            renderBillboardQuad(consumer, matrix, orb.x, orb.y, orb.z, orb.size,
                    PRIMARY_R + (1f - PRIMARY_R) * brightness,
                    PRIMARY_G + (1f - PRIMARY_G) * brightness,
                    PRIMARY_B + (1f - PRIMARY_B) * brightness,
                    orb.alpha * blessingIntensity);

            // Render orb glow
            renderBillboardQuad(consumer, matrix, orb.x, orb.y, orb.z, orb.size * 1.5f,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B,
                    orb.alpha * blessingIntensity * 0.4f);
        }
    }

    private void renderBlessingAura(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Gentle aura at the base
        int segments = 20;
        float auraRadius = 2.8f + Mth.sin(currentTick * 0.06f) * 0.3f;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments + spiralRotation * 0.3f);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments + spiralRotation * 0.3f);

            float x1 = Mth.cos(angle1) * auraRadius;
            float z1 = Mth.sin(angle1) * auraRadius;
            float x2 = Mth.cos(angle2) * auraRadius;
            float z2 = Mth.sin(angle2) * auraRadius;

            float wave = Mth.sin(currentTick * 0.08f + angle1 * 3f) * 0.15f;
            float alpha = 0.3f * blessingIntensity;

            addVertex(consumer, matrix, x1, -1f + wave, z1,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, x2, -1f + wave, z2,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, x2, 0f + wave, z2,
                    1f, 1f, 1f, 0f);
            addVertex(consumer, matrix, x1, 0f + wave, z1,
                    1f, 1f, 1f, 0f);
        }
    }

    private void renderHaloRings(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Rising blessing rings
        int ringCount = 4;
        for (int r = 0; r < ringCount; r++) {
            float ringOffset = r * 0.25f;
            float ringProgress = ((progress + ringOffset) % 1f);

            float ringY = -0.5f + ringProgress * 3.5f;
            float ringRadius = 1.2f + ringProgress * 1.2f;
            float ringAlpha = Mth.sin(ringProgress * Mth.PI) * 0.6f * blessingIntensity;

            int segments = 28;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments + spiralRotation * 0.5f);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments + spiralRotation * 0.5f);

                float x1 = Mth.cos(angle1) * ringRadius;
                float z1 = Mth.sin(angle1) * ringRadius;
                float x2 = Mth.cos(angle2) * ringRadius;
                float z2 = Mth.sin(angle2) * ringRadius;

                float brightness = 0.7f + ringProgress * 0.3f;

                addVertex(consumer, matrix, x1, ringY - 0.08f, z1,
                        PRIMARY_R * brightness, PRIMARY_G * brightness, PRIMARY_B * brightness, ringAlpha);
                addVertex(consumer, matrix, x2, ringY - 0.08f, z2,
                        PRIMARY_R * brightness, PRIMARY_G * brightness, PRIMARY_B * brightness, ringAlpha);
                addVertex(consumer, matrix, x2, ringY + 0.08f, z2,
                        1f, 1f, 1f, ringAlpha);
                addVertex(consumer, matrix, x1, ringY + 0.08f, z1,
                        1f, 1f, 1f, ringAlpha);
            }
        }
    }

    private void renderShimmeringGlow(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Shimmering sparkles around the effect
        int sparkleCount = 30;
        for (int i = 0; i < sparkleCount; i++) {
            float angle = (float) (i * Math.PI * 2 / sparkleCount);
            float heightOffset = (i % 5) * 0.6f;
            float radius = 2.2f + Mth.sin(currentTick * 0.1f + i * 0.5f) * 0.4f;

            float x = Mth.cos(angle + spiralRotation) * radius;
            float y = -0.5f + heightOffset + Mth.sin(currentTick * 0.12f + i) * 0.3f;
            float z = Mth.sin(angle + spiralRotation) * radius;

            float twinkle = Mth.sin(currentTick * 0.2f + i * 0.8f) * 0.5f + 0.5f;
            float alpha = 0.7f * blessingIntensity * twinkle;

            renderBillboardQuad(consumer, matrix, x, y, z, 0.08f,
                    1f, 1f, 1f, alpha);
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

    private class AscendingSpiral {
        List<Vec3> points = new ArrayList<>();
        float alpha;
        float thickness;
        float orbitRadius;
        float angleOffset;
        float heightOffset;
        float spiralSpeed;

        AscendingSpiral(int index) {
            this.angleOffset = index * Mth.TWO_PI / 6f;
            this.orbitRadius = 1.3f + (index % 3) * 0.3f;
            this.heightOffset = -index * 0.2f;
            this.thickness = 0.07f + random.nextFloat() * 0.03f;
            this.spiralSpeed = 0.7f + random.nextFloat() * 0.3f;
            generateSpiral();
        }

        void generateSpiral() {
            points.clear();
            int numPoints = 45;

            for (int i = 0; i < numPoints; i++) {
                float t = i / (float) numPoints;
                float angle = t * Mth.TWO_PI * 2.5f; // 2.5 full rotations
                float height = t * 4.5f - 1.5f; // Ascends from bottom to top

                float radius = orbitRadius * (1f - t * 0.2f); // Slightly tightens as it rises
                float x = Mth.cos(angle + angleOffset) * radius;
                float z = Mth.sin(angle + angleOffset) * radius;

                points.add(new Vec3(x, height + heightOffset, z));
            }
        }

        void update(float progress, float intensity, float rotation) {
            // Rotate the entire spiral upward
            for (int i = 0; i < points.size(); i++) {
                Vec3 p = points.get(i);
                float rotAmount = spiralSpeed * 0.04f;
                float newX = (float) (p.x * Math.cos(rotAmount) - p.z * Math.sin(rotAmount));
                float newZ = (float) (p.x * Math.sin(rotAmount) + p.z * Math.cos(rotAmount));
                points.set(i, new Vec3(newX, p.y, newZ));
            }

            this.alpha = 0.8f * (0.85f + 0.15f * Mth.sin(progress * 5f + angleOffset));
        }
    }

    private class LuckParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float r, g, b;
        float lifetime;
        float age;

        LuckParticle() {
            respawn();
        }

        void respawn() {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * 2.5f;

            this.x = Mth.cos(angle) * dist;
            this.y = -1f + random.nextFloat() * 0.5f;
            this.z = Mth.sin(angle) * dist;

            // Spiraling upward motion
            float tangentAngle = angle + Mth.HALF_PI;
            
            this.vx = Mth.cos(tangentAngle) * 0.015f - x * 0.008f;
            this.vy = 0.025f + random.nextFloat() * 0.015f; // Rising
            this.vz = Mth.sin(tangentAngle) * 0.015f - z * 0.008f;

            this.size = 0.04f + random.nextFloat() * 0.05f;
            this.lifetime = 70f + random.nextFloat() * 50f;
            this.age = 0f;

            // Bright variants of the primary color
            float brightness = 0.8f + random.nextFloat() * 0.2f;
            this.r = PRIMARY_R + (1f - PRIMARY_R) * (random.nextFloat() * 0.3f);
            this.g = PRIMARY_G + (1f - PRIMARY_G) * (random.nextFloat() * 0.3f);
            this.b = PRIMARY_B + (1f - PRIMARY_B) * (random.nextFloat() * 0.3f);
        }

        void update(float progress, float intensity) {
            this.x += vx;
            this.y += vy;
            this.z += vz;
            this.age++;

            // Gentle sparkle effect
            float sparkle = Mth.sin(age * 0.3f) * 0.3f + 0.7f;

            // Fade in and out
            float lifetimeProgress = age / lifetime;
            if (lifetimeProgress < 0.2f) {
                this.alpha = lifetimeProgress / 0.2f * 0.9f * sparkle;
            } else if (lifetimeProgress > 0.8f) {
                this.alpha = (1f - lifetimeProgress) / 0.2f * 0.9f * sparkle;
            } else {
                this.alpha = 0.9f * sparkle;
            }

            // Respawn if too old or out of bounds
            if (age >= lifetime || y > 4f) {
                respawn();
            }
        }
    }

    private class BlessingRune {
        float x, y, z;
        float size;
        float alpha;
        float angleOffset;
        float orbitRadius;
        float rotationSpeed;
        float heightBase;

        BlessingRune() {
            this.angleOffset = random.nextFloat() * Mth.TWO_PI;
            this.orbitRadius = 1.9f + random.nextFloat() * 0.4f;
            this.size = 0.18f + random.nextFloat() * 0.08f;
            this.rotationSpeed = 0.025f + random.nextFloat() * 0.015f;
            this.heightBase = 0.5f + random.nextFloat() * 1.5f;
        }

        void update(float progress, float intensity, float rotation) {
            float angle = angleOffset + rotation * rotationSpeed;
            
            this.x = Mth.cos(angle) * orbitRadius;
            this.y = heightBase + Mth.sin(currentTick * 0.1f + angleOffset) * 0.25f;
            this.z = Mth.sin(angle) * orbitRadius;

            float pulse = Mth.sin(progress * 6f + angleOffset) * 0.3f + 0.7f;
            this.alpha = 0.7f * pulse;
        }
    }

    private class LightRay {
        Vec3 start;
        Vec3 end;
        float alpha;
        float width;
        float angleOffset;
        float length;

        LightRay(int index) {
            this.angleOffset = index * Mth.TWO_PI / 12f;
            this.width = 0.12f + random.nextFloat() * 0.08f;
            this.length = 2.5f + random.nextFloat() * 1f;
            updatePositions();
        }

        void updatePositions() {
            float angle = angleOffset + spiralRotation * 0.4f;
            
            this.start = new Vec3(0, -0.5f, 0);
            this.end = new Vec3(
                Mth.cos(angle) * length,
                2f + Mth.sin(currentTick * 0.08f + angleOffset) * 0.4f,
                Mth.sin(angle) * length
            );
        }

        void update(float progress, float intensity) {
            updatePositions();
            
            float pulse = Mth.sin(progress * 7f + angleOffset) * 0.4f + 0.6f;
            this.alpha = 0.5f * pulse;
        }
    }

    private class FortuneOrb {
        float x, y, z;
        float size;
        float alpha;
        float angleOffset;
        float orbitRadius;
        float heightBase;
        float orbitSpeed;
        float phaseOffset;

        FortuneOrb(int index) {
            this.angleOffset = index * Mth.TWO_PI / 8f;
            this.orbitRadius = 1.6f + (index % 2) * 0.3f;
            this.size = 0.12f + random.nextFloat() * 0.06f;
            this.orbitSpeed = 0.03f + random.nextFloat() * 0.02f;
            this.heightBase = 0.3f + (index % 3) * 0.5f;
            this.phaseOffset = random.nextFloat() * Mth.TWO_PI;
        }

        void update(float progress, float intensity, float rotation) {
            float angle = angleOffset + rotation * orbitSpeed;
            
            this.x = Mth.cos(angle) * orbitRadius;
            this.y = heightBase + Mth.sin(currentTick * 0.09f + phaseOffset) * 0.3f;
            this.z = Mth.sin(angle) * orbitRadius;

            float pulse = Mth.sin(progress * 8f + angleOffset) * 0.2f + 0.8f;
            this.alpha = 0.75f * pulse;
        }
    }
}
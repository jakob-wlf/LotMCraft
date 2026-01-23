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

public class BaptismEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<CascadingWave> waves = new ArrayList<>();
    private final List<PurificationParticle> particles = new ArrayList<>();
    private final List<SpiritFragment> fragments = new ArrayList<>();
    private final List<GoldenRay> goldenRays = new ArrayList<>();
    private final List<CleansingOrb> cleansingOrbs = new ArrayList<>();
    
    // Primary color scheme (light blue/white from blessing)
    private static final float PRIMARY_R = 170f / 255f;
    private static final float PRIMARY_G = 204f / 255f;
    private static final float PRIMARY_B = 230f / 255f;
    
    // Golden accent color
    private static final float GOLD_R = 255f / 255f;
    private static final float GOLD_G = 215f / 255f;
    private static final float GOLD_B = 120f / 255f;
    
    private float baptismIntensity = 0f;
    private float waveRotation = 0f;
    private float cleansePulse = 0f;

    public BaptismEffect(double x, double y, double z) {
        super(x, y, z, 20 * 5); // 10 seconds duration

        // Create cascading waves that wash over the player
        for (int i = 0; i < 8; i++) {
            waves.add(new CascadingWave(i));
        }

        // Create purification particles
        for (int i = 0; i < 200; i++) {
            particles.add(new PurificationParticle());
        }

        // Create spirit fragments that dissolve impurities
        for (int i = 0; i < 15; i++) {
            fragments.add(new SpiritFragment());
        }

        // Create golden rays of divine light
        for (int i = 0; i < 6; i++) {
            goldenRays.add(new GoldenRay(i));
        }

        // Create cleansing orbs that orbit
        for (int i = 0; i < 10; i++) {
            cleansingOrbs.add(new CleansingOrb(i));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;

        // Build up intensity, sustain, then fade
        if (progress < 0.1f) {
            baptismIntensity = progress / 0.1f;
        } else if (progress > 0.9f) {
            baptismIntensity = 1f - ((progress - 0.9f) / 0.1f);
        } else {
            baptismIntensity = 1f;
        }

        waveRotation += 0.04f;
        cleansePulse = Mth.sin(currentTick * 0.1f) * 0.5f + 0.5f;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render in order for proper visual layering
        renderDivineColumn(poseStack, bufferSource, progress);
        renderGoldenRays(poseStack, bufferSource, progress);
        renderCascadingWaves(poseStack, bufferSource, progress);
        renderCleansingOrbs(poseStack, bufferSource, progress);
        renderSpiritFragments(poseStack, bufferSource, progress);
        renderPurificationParticles(poseStack, bufferSource, progress);
        renderBaptismVortex(poseStack, bufferSource, progress);
        renderHeavenlyGlow(poseStack, bufferSource, progress);
        renderSpiritualCrown(poseStack, bufferSource, progress);

        poseStack.popPose();
    }

    private void renderDivineColumn(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Central column of descending divine light
        float columnRadius = 0.8f + Mth.sin(currentTick * 0.07f) * 0.15f;
        int segments = 24;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * columnRadius;
            float z1 = Mth.sin(angle1) * columnRadius;
            float x2 = Mth.cos(angle2) * columnRadius;
            float z2 = Mth.sin(angle2) * columnRadius;

            float alpha = 0.6f * baptismIntensity;

            // Top to bottom gradient (descending light)
            addVertex(consumer, matrix, x1, 4f, z1,
                    1f, 1f, 1f, alpha);
            addVertex(consumer, matrix, x2, 4f, z2,
                    1f, 1f, 1f, alpha);
            addVertex(consumer, matrix, x2, -1.5f, z2,
                    PRIMARY_R * 0.6f, PRIMARY_G * 0.6f, PRIMARY_B * 0.6f, alpha * 0.2f);
            addVertex(consumer, matrix, x1, -1.5f, z1,
                    PRIMARY_R * 0.6f, PRIMARY_G * 0.6f, PRIMARY_B * 0.6f, alpha * 0.2f);
        }

        // Inner golden core
        float coreRadius = columnRadius * 0.4f;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * coreRadius;
            float z1 = Mth.sin(angle1) * coreRadius;
            float x2 = Mth.cos(angle2) * coreRadius;
            float z2 = Mth.sin(angle2) * coreRadius;

            float goldenAlpha = 0.4f * baptismIntensity * cleansePulse;

            addVertex(consumer, matrix, x1, 4f, z1,
                    GOLD_R, GOLD_G, GOLD_B, goldenAlpha);
            addVertex(consumer, matrix, x2, 4f, z2,
                    GOLD_R, GOLD_G, GOLD_B, goldenAlpha);
            addVertex(consumer, matrix, x2, -1.5f, z2,
                    GOLD_R * 0.7f, GOLD_G * 0.7f, GOLD_B * 0.7f, goldenAlpha * 0.3f);
            addVertex(consumer, matrix, x1, -1.5f, z1,
                    GOLD_R * 0.7f, GOLD_G * 0.7f, GOLD_B * 0.7f, goldenAlpha * 0.3f);
        }
    }

    private void renderCascadingWaves(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (CascadingWave wave : waves) {
            wave.update(progress, baptismIntensity, currentTick);

            if (wave.alpha <= 0f) continue;

            int segments = 32;
            float waveY = wave.height;
            
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments + wave.rotation);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments + wave.rotation);

                float ripple1 = Mth.sin(angle1 * 4f + currentTick * 0.15f) * 0.15f;
                float ripple2 = Mth.sin(angle2 * 4f + currentTick * 0.15f) * 0.15f;

                float r1 = wave.radius + ripple1;
                float r2 = wave.radius + ripple2;

                float x1 = Mth.cos(angle1) * r1;
                float z1 = Mth.sin(angle1) * r1;
                float x2 = Mth.cos(angle2) * r2;
                float z2 = Mth.sin(angle2) * r2;

                // Mix of blue and gold based on wave phase
                float goldMix = wave.goldRatio;
                float r = PRIMARY_R + (GOLD_R - PRIMARY_R) * goldMix;
                float g = PRIMARY_G + (GOLD_G - PRIMARY_G) * goldMix;
                float b = PRIMARY_B + (GOLD_B - PRIMARY_B) * goldMix;

                addVertex(consumer, matrix, x1, waveY - wave.thickness, z1,
                        r * 0.7f, g * 0.7f, b * 0.7f, wave.alpha * 0.5f);
                addVertex(consumer, matrix, x2, waveY - wave.thickness, z2,
                        r * 0.7f, g * 0.7f, b * 0.7f, wave.alpha * 0.5f);
                addVertex(consumer, matrix, x2, waveY + wave.thickness, z2,
                        1f, 1f, 1f, wave.alpha * baptismIntensity);
                addVertex(consumer, matrix, x1, waveY + wave.thickness, z1,
                        1f, 1f, 1f, wave.alpha * baptismIntensity);
            }
        }
    }

    private void renderPurificationParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (PurificationParticle particle : particles) {
            particle.update(progress, baptismIntensity);

            if (particle.alpha <= 0f) continue;

            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, particle.r, particle.g, particle.b, particle.alpha * baptismIntensity);
        }
    }

    private void renderSpiritFragments(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (SpiritFragment fragment : fragments) {
            fragment.update(progress, baptismIntensity, waveRotation);

            if (fragment.alpha <= 0f) continue;

            // Render fragment as ethereal cross/star
            float size = fragment.size;
            float alpha = fragment.alpha * baptismIntensity;

            // Vertical beam
            addVertex(consumer, matrix, fragment.x - size * 0.1f, fragment.y - size, fragment.z,
                    fragment.r, fragment.g, fragment.b, alpha * 0.6f);
            addVertex(consumer, matrix, fragment.x + size * 0.1f, fragment.y - size, fragment.z,
                    fragment.r, fragment.g, fragment.b, alpha * 0.6f);
            addVertex(consumer, matrix, fragment.x + size * 0.1f, fragment.y + size, fragment.z,
                    1f, 1f, 1f, alpha);
            addVertex(consumer, matrix, fragment.x - size * 0.1f, fragment.y + size, fragment.z,
                    1f, 1f, 1f, alpha);

            // Horizontal beam
            addVertex(consumer, matrix, fragment.x - size, fragment.y - size * 0.1f, fragment.z,
                    fragment.r, fragment.g, fragment.b, alpha * 0.6f);
            addVertex(consumer, matrix, fragment.x + size, fragment.y - size * 0.1f, fragment.z,
                    fragment.r, fragment.g, fragment.b, alpha * 0.6f);
            addVertex(consumer, matrix, fragment.x + size, fragment.y + size * 0.1f, fragment.z,
                    1f, 1f, 1f, alpha);
            addVertex(consumer, matrix, fragment.x - size, fragment.y + size * 0.1f, fragment.z,
                    1f, 1f, 1f, alpha);
        }
    }

    private void renderGoldenRays(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (GoldenRay ray : goldenRays) {
            ray.update(progress, baptismIntensity);

            if (ray.alpha <= 0f) continue;

            Vec3 start = ray.start;
            Vec3 end = ray.end;

            Vec3 dir = new Vec3(end.x - start.x, end.y - start.y, end.z - start.z);
            Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(ray.width);

            float alpha = ray.alpha * baptismIntensity;

            addVertex(consumer, matrix,
                    (float)(start.x - perp.x), (float)(start.y - perp.y), (float)(start.z - perp.z),
                    GOLD_R * 0.7f, GOLD_G * 0.7f, GOLD_B * 0.7f, alpha * 0.4f);
            addVertex(consumer, matrix,
                    (float)(start.x + perp.x), (float)(start.y + perp.y), (float)(start.z + perp.z),
                    GOLD_R * 0.7f, GOLD_G * 0.7f, GOLD_B * 0.7f, alpha * 0.4f);
            addVertex(consumer, matrix,
                    (float)(end.x + perp.x), (float)(end.y + perp.y), (float)(end.z + perp.z),
                    1f, 1f, 0.9f, alpha);
            addVertex(consumer, matrix,
                    (float)(end.x - perp.x), (float)(end.y - perp.y), (float)(end.z - perp.z),
                    1f, 1f, 0.9f, alpha);
        }
    }

    private void renderCleansingOrbs(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (CleansingOrb orb : cleansingOrbs) {
            orb.update(progress, baptismIntensity, waveRotation);

            if (orb.alpha <= 0f) continue;

            // Core orb
            renderBillboardQuad(consumer, matrix, orb.x, orb.y, orb.z, orb.size,
                    orb.r, orb.g, orb.b, orb.alpha * baptismIntensity);

            // Outer glow
            renderBillboardQuad(consumer, matrix, orb.x, orb.y, orb.z, orb.size * 1.8f,
                    orb.r * 0.8f, orb.g * 0.8f, orb.b * 0.8f, orb.alpha * baptismIntensity * 0.3f);
        }
    }

    private void renderBaptismVortex(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Swirling vortex at player level
        int vortexLayers = 3;
        for (int layer = 0; layer < vortexLayers; layer++) {
            float layerY = 0.5f + layer * 0.4f;
            float layerRadius = 1.8f + layer * 0.3f;
            int segments = 28;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments - waveRotation * (1 + layer * 0.3f));
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments - waveRotation * (1 + layer * 0.3f));

                float spiral1 = Mth.sin(angle1 * 3f + currentTick * 0.12f) * 0.2f;
                float spiral2 = Mth.sin(angle2 * 3f + currentTick * 0.12f) * 0.2f;

                float x1 = Mth.cos(angle1) * (layerRadius + spiral1);
                float z1 = Mth.sin(angle1) * (layerRadius + spiral1);
                float x2 = Mth.cos(angle2) * (layerRadius + spiral2);
                float z2 = Mth.sin(angle2) * (layerRadius + spiral2);

                float alpha = 0.4f * baptismIntensity * (1f - layer * 0.25f);

                addVertex(consumer, matrix, x1, layerY - 0.06f, z1,
                        PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
                addVertex(consumer, matrix, x2, layerY - 0.06f, z2,
                        PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
                addVertex(consumer, matrix, x2, layerY + 0.06f, z2,
                        1f, 1f, 1f, alpha * 1.5f);
                addVertex(consumer, matrix, x1, layerY + 0.06f, z1,
                        1f, 1f, 1f, alpha * 1.5f);
            }
        }
    }

    private void renderHeavenlyGlow(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Descending rings of light
        int ringCount = 5;
        for (int r = 0; r < ringCount; r++) {
            float ringOffset = r * 0.2f;
            float ringProgress = ((progress * 1.5f + ringOffset) % 1f);

            float ringY = 4f - ringProgress * 5.5f; // Descends from top
            float ringRadius = 0.8f + ringProgress * 1.5f;
            float ringAlpha = Mth.sin(ringProgress * Mth.PI) * 0.5f * baptismIntensity;

            int segments = 32;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(angle1) * ringRadius;
                float z1 = Mth.sin(angle1) * ringRadius;
                float x2 = Mth.cos(angle2) * ringRadius;
                float z2 = Mth.sin(angle2) * ringRadius;

                // Golden to blue gradient
                float goldMix = 1f - ringProgress;
                float red = PRIMARY_R + (GOLD_R - PRIMARY_R) * goldMix;
                float g = PRIMARY_G + (GOLD_G - PRIMARY_G) * goldMix;
                float b = PRIMARY_B + (GOLD_B - PRIMARY_B) * goldMix;

                addVertex(consumer, matrix, x1, ringY - 0.1f, z1,
                        red * 0.7f, g * 0.7f, b * 0.7f, ringAlpha * 0.6f);
                addVertex(consumer, matrix, x2, ringY - 0.1f, z2,
                        red * 0.7f, g * 0.7f, b * 0.7f, ringAlpha * 0.6f);
                addVertex(consumer, matrix, x2, ringY + 0.1f, z2,
                        1f, 1f, 1f, ringAlpha);
                addVertex(consumer, matrix, x1, ringY + 0.1f, z1,
                        1f, 1f, 1f, ringAlpha);
            }
        }
    }

    private void renderSpiritualCrown(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Crown of light above the effect
        float crownY = 2.5f + Mth.sin(currentTick * 0.08f) * 0.2f;
        int points = 8;

        for (int i = 0; i < points; i++) {
            float angle = (float) (i * Math.PI * 2 / points + waveRotation * 0.5f);
            float nextAngle = (float) ((i + 1) * Math.PI * 2 / points + waveRotation * 0.5f);

            float baseRadius = 1.2f;
            float peakRadius = 1.5f;

            float x1 = Mth.cos(angle) * baseRadius;
            float z1 = Mth.sin(angle) * baseRadius;
            float x2 = Mth.cos(nextAngle) * baseRadius;
            float z2 = Mth.sin(nextAngle) * baseRadius;

            float peakX = Mth.cos(angle) * peakRadius;
            float peakZ = Mth.sin(angle) * peakRadius;
            float peakY = crownY + 0.4f;

            float alpha = 0.6f * baptismIntensity;
            float pulse = Mth.sin(currentTick * 0.15f + i * 0.5f) * 0.3f + 0.7f;

            // Triangle peak
            addVertex(consumer, matrix, x1, crownY, z1,
                    GOLD_R * 0.8f, GOLD_G * 0.8f, GOLD_B * 0.8f, alpha * pulse);
            addVertex(consumer, matrix, x2, crownY, z2,
                    GOLD_R * 0.8f, GOLD_G * 0.8f, GOLD_B * 0.8f, alpha * pulse);
            addVertex(consumer, matrix, peakX, peakY, peakZ,
                    1f, 1f, 0.95f, alpha * pulse * 1.2f);
            addVertex(consumer, matrix, peakX, peakY, peakZ,
                    1f, 1f, 0.95f, alpha * pulse * 1.2f);
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

    private class CascadingWave {
        float height;
        float radius;
        float alpha;
        float thickness;
        float rotation;
        float goldRatio;
        float descendSpeed;
        int index;

        CascadingWave(int index) {
            this.index = index;
            this.thickness = 0.12f;
            this.descendSpeed = 0.035f + random.nextFloat() * 0.015f;
            this.goldRatio = random.nextFloat() * 0.4f;
            reset();
        }

        void reset() {
            this.height = 4f + random.nextFloat() * 1f;
            this.radius = 0.5f;
        }

        void update(float progress, float intensity, float tick) {
            // Descend and expand
            this.height -= descendSpeed;
            this.radius = 0.5f + (4f - height) * 0.4f;
            this.rotation = tick * 0.03f + index * 0.5f;

            // Fade based on height
            if (height > 2f) {
                this.alpha = Mth.clamp((4f - height) / 2f, 0f, 1f) * 0.7f;
            } else if (height < -0.5f) {
                this.alpha = Mth.clamp((height + 1.5f) / 1f, 0f, 1f) * 0.7f;
            } else {
                this.alpha = 0.7f;
            }

            if (height < -1f) {
                reset();
            }
        }
    }

    private class PurificationParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float r, g, b;
        float lifetime;
        float age;

        PurificationParticle() {
            respawn();
        }

        void respawn() {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * 2.2f;
            float height = random.nextFloat() * 5f;

            this.x = Mth.cos(angle) * dist;
            this.y = height;
            this.z = Mth.sin(angle) * dist;

            // Gentle inward and downward drift
            this.vx = -x * 0.006f + (random.nextFloat() - 0.5f) * 0.01f;
            this.vy = -0.02f - random.nextFloat() * 0.01f;
            this.vz = -z * 0.006f + (random.nextFloat() - 0.5f) * 0.01f;

            this.size = 0.05f + random.nextFloat() * 0.06f;
            this.lifetime = 80f + random.nextFloat() * 60f;
            this.age = 0f;

            // Mix of blue and golden particles
            if (random.nextFloat() < 0.3f) {
                this.r = GOLD_R;
                this.g = GOLD_G;
                this.b = GOLD_B;
            } else {
                float brightness = 0.7f + random.nextFloat() * 0.3f;
                this.r = PRIMARY_R + (1f - PRIMARY_R) * brightness;
                this.g = PRIMARY_G + (1f - PRIMARY_G) * brightness;
                this.b = PRIMARY_B + (1f - PRIMARY_B) * brightness;
            }
        }

        void update(float progress, float intensity) {
            this.x += vx;
            this.y += vy;
            this.z += vz;
            this.age++;

            float sparkle = Mth.sin(age * 0.25f) * 0.4f + 0.6f;

            float lifetimeProgress = age / lifetime;
            if (lifetimeProgress < 0.25f) {
                this.alpha = lifetimeProgress / 0.25f * 0.85f * sparkle;
            } else if (lifetimeProgress > 0.75f) {
                this.alpha = (1f - lifetimeProgress) / 0.25f * 0.85f * sparkle;
            } else {
                this.alpha = 0.85f * sparkle;
            }

            if (age >= lifetime || y < -2f) {
                respawn();
            }
        }
    }

    private class SpiritFragment {
        float x, y, z;
        float size;
        float alpha;
        float r, g, b;
        float angleOffset;
        float orbitRadius;
        float rotationSpeed;
        float heightBase;
        float dissolveRate;

        SpiritFragment() {
            this.angleOffset = random.nextFloat() * Mth.TWO_PI;
            this.orbitRadius = 2.0f + random.nextFloat() * 0.5f;
            this.size = 0.15f + random.nextFloat() * 0.1f;
            this.rotationSpeed = 0.02f + random.nextFloat() * 0.015f;
            this.heightBase = 0.8f + random.nextFloat() * 2f;
            this.dissolveRate = 0.005f + random.nextFloat() * 0.003f;

            // Mostly white/blue with occasional gold
            if (random.nextFloat() < 0.25f) {
                this.r = GOLD_R;
                this.g = GOLD_G;
                this.b = GOLD_B;
            } else {
                this.r = PRIMARY_R + (1f - PRIMARY_R) * 0.8f;
                this.g = PRIMARY_G + (1f - PRIMARY_G) * 0.8f;
                this.b = PRIMARY_B + (1f - PRIMARY_B) * 0.8f;
            }
        }

        void update(float progress, float intensity, float rotation) {
            float angle = angleOffset + rotation * rotationSpeed;
            
            this.x = Mth.cos(angle) * orbitRadius;
            this.y = heightBase + Mth.sin(currentTick * 0.11f + angleOffset) * 0.3f;
            this.z = Mth.sin(angle) * orbitRadius;

            // Slowly dissolve and drift inward
            orbitRadius -= dissolveRate;
            if (orbitRadius < 0.3f) {
                orbitRadius = 2.0f + random.nextFloat() * 0.5f;
            }

            float pulse = Mth.sin(progress * 7f + angleOffset) * 0.25f + 0.75f;
            this.alpha = 0.7f * pulse;
        }
    }

    private class GoldenRay {
        Vec3 start;
        Vec3 end;
        float alpha;
        float width;
        float angleOffset;
        float length;

        GoldenRay(int index) {
            this.angleOffset = index * Mth.TWO_PI / 6f;
            this.width = 0.15f + random.nextFloat() * 0.1f;
            this.length = 2.8f + random.nextFloat() * 1.2f;
            updatePositions();
        }

        void updatePositions() {
            float angle = angleOffset + currentTick * 0.02f;
            
            this.start = new Vec3(0, 4f, 0);
            this.end = new Vec3(
                Mth.cos(angle) * length,
                0.5f + Mth.sin(currentTick * 0.1f + angleOffset) * 0.5f,
                Mth.sin(angle) * length
            );
        }

        void update(float progress, float intensity) {
            updatePositions();
            
            float pulse = Mth.sin(progress * 6f + angleOffset) * 0.3f + 0.7f;
            this.alpha = 0.6f * pulse;
        }
    }

    private class CleansingOrb {
        float x, y, z;
        float size;
        float alpha;
        float r, g, b;
        float angleOffset;
        float orbitRadius;
        float heightBase;
        float orbitSpeed;
        float phaseOffset;

        CleansingOrb(int index) {
            this.angleOffset = index * Mth.TWO_PI / 10f;
            this.orbitRadius = 1.4f + (index % 3) * 0.3f;
            this.size = 0.1f + random.nextFloat() * 0.05f;
            this.orbitSpeed = 0.025f + random.nextFloat() * 0.02f;
            this.heightBase = 0.5f + (index % 4) * 0.6f;
            this.phaseOffset = random.nextFloat() * Mth.TWO_PI;

            // Mix of colors
            float colorChoice = random.nextFloat();
            if (colorChoice < 0.3f) {
                // Golden
                this.r = GOLD_R;
                this.g = GOLD_G;
                this.b = GOLD_B;
            } else if (colorChoice < 0.6f) {
                // White
                this.r = 1f;
                this.g = 1f;
                this.b = 1f;
            } else {
                // Light blue
                this.r = PRIMARY_R + (1f - PRIMARY_R) * 0.5f;
                this.g = PRIMARY_G + (1f - PRIMARY_G) * 0.5f;
                this.b = PRIMARY_B + (1f - PRIMARY_B) * 0.5f;
            }
        }

        void update(float progress, float intensity, float rotation) {
            float angle = angleOffset + rotation * orbitSpeed;
            
            this.x = Mth.cos(angle) * orbitRadius;
            this.y = heightBase + Mth.sin(currentTick * 0.1f + phaseOffset) * 0.35f;
            this.z = Mth.sin(angle) * orbitRadius;

            float pulse = Mth.sin(progress * 9f + angleOffset) * 0.2f + 0.8f;
            this.alpha = 0.8f * pulse;
        }
    }
}
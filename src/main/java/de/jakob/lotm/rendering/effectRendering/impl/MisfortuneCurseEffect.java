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

public class MisfortuneCurseEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<CurseSpiral> spirals = new ArrayList<>();
    private final List<MisfortuneParticle> particles = new ArrayList<>();
    private final List<OminousRune> runes = new ArrayList<>();
    private final List<ChainLink> chains = new ArrayList<>();
    
    private static final float PRIMARY_R = 171f / 255f;
    private static final float PRIMARY_G = 116f / 255f;
    private static final float PRIMARY_B = 56f / 255f;
    
    private float curseIntensity = 0f;
    private float spiralRotation = 0f;

    public MisfortuneCurseEffect(double x, double y, double z) {
        super(x, y, z, 20 * 2); // 8 seconds duration

        // Create multiple spirals that orbit and descend
        for (int i = 0; i < 5; i++) {
            spirals.add(new CurseSpiral(i));
        }

        // Create curse particles
        for (int i = 0; i < 120; i++) {
            particles.add(new MisfortuneParticle());
        }

        // Create floating ominous runes
        for (int i = 0; i < 8; i++) {
            runes.add(new OminousRune());
        }

        // Create ethereal chains
        for (int i = 0; i < 6; i++) {
            chains.add(new ChainLink());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;

        // Curse builds up, sustains, then fades
        if (progress < 0.15f) {
            curseIntensity = progress / 0.15f;
        } else if (progress > 0.85f) {
            curseIntensity = 1f - ((progress - 0.85f) / 0.15f);
        } else {
            curseIntensity = 1f;
        }

        spiralRotation += 0.08f;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render in order for proper blending
        renderCoreVortex(poseStack, bufferSource, progress);
        renderChains(poseStack, bufferSource, progress);
        renderSpirals(poseStack, bufferSource, progress);
        renderRunes(poseStack, bufferSource, progress);
        renderParticles(poseStack, bufferSource, progress);
        renderCurseMist(poseStack, bufferSource, progress);
        renderAuraRings(poseStack, bufferSource, progress);

        poseStack.popPose();
    }

    private void renderCoreVortex(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Dark swirling vortex at the center
        float vortexRadius = 0.8f + Mth.sin(currentTick * 0.1f) * 0.2f;
        int segments = 24;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments + spiralRotation);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments + spiralRotation);

            // Inner dark core
            float innerRadius = vortexRadius * 0.3f;
            float outerRadius = vortexRadius;

            float x1 = Mth.cos(angle1) * innerRadius;
            float z1 = Mth.sin(angle1) * innerRadius;
            float x2 = Mth.cos(angle2) * innerRadius;
            float z2 = Mth.sin(angle2) * innerRadius;

            float x3 = Mth.cos(angle2) * outerRadius;
            float z3 = Mth.sin(angle2) * outerRadius;
            float x4 = Mth.cos(angle1) * outerRadius;
            float z4 = Mth.sin(angle1) * outerRadius;

            float alpha = 0.7f * curseIntensity;

            // Dark center
            addVertex(consumer, matrix, x1, 0, z1, 0.1f, 0.05f, 0.0f, alpha);
            addVertex(consumer, matrix, x2, 0, z2, 0.1f, 0.05f, 0.0f, alpha);
            addVertex(consumer, matrix, x3, 0, z3, PRIMARY_R * 0.5f, PRIMARY_G * 0.5f, PRIMARY_B * 0.5f, alpha * 0.6f);
            addVertex(consumer, matrix, x4, 0, z4, PRIMARY_R * 0.5f, PRIMARY_G * 0.5f, PRIMARY_B * 0.5f, alpha * 0.6f);
        }
    }

    private void renderSpirals(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (CurseSpiral spiral : spirals) {
            spiral.update(progress, curseIntensity, spiralRotation);

            if (spiral.alpha <= 0f) continue;

            // Draw spiral as connected segments
            for (int i = 0; i < spiral.points.size() - 1; i++) {
                Vec3 p1 = spiral.points.get(i);
                Vec3 p2 = spiral.points.get(i + 1);

                Vec3 dir = new Vec3(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(spiral.thickness);

                float t = i / (float) spiral.points.size();
                float segmentAlpha = spiral.alpha * curseIntensity * (1f - t * 0.4f);

                // Gradient from dark to bronze
                float r = PRIMARY_R * (0.3f + t * 0.7f);
                float g = PRIMARY_G * (0.3f + t * 0.7f);
                float b = PRIMARY_B * (0.3f + t * 0.7f);

                addVertex(consumer, matrix,
                        (float)(p1.x - perp.x), (float)(p1.y - perp.y), (float)(p1.z - perp.z),
                        r, g, b, segmentAlpha);
                addVertex(consumer, matrix,
                        (float)(p1.x + perp.x), (float)(p1.y + perp.y), (float)(p1.z + perp.z),
                        r, g, b, segmentAlpha);
                addVertex(consumer, matrix,
                        (float)(p2.x + perp.x), (float)(p2.y + perp.y), (float)(p2.z + perp.z),
                        r, g, b, segmentAlpha * 0.7f);
                addVertex(consumer, matrix,
                        (float)(p2.x - perp.x), (float)(p2.y - perp.y), (float)(p2.z - perp.z),
                        r, g, b, segmentAlpha * 0.7f);
            }
        }
    }

    private void renderParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (MisfortuneParticle particle : particles) {
            particle.update(progress, curseIntensity);

            if (particle.alpha <= 0f) continue;

            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, particle.r, particle.g, particle.b, particle.alpha * curseIntensity);
        }
    }

    private void renderRunes(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (OminousRune rune : runes) {
            rune.update(progress, curseIntensity, spiralRotation);

            if (rune.alpha <= 0f) continue;

            // Draw rune as a glowing symbol (simplified as cross pattern)
            float size = rune.size;
            float alpha = rune.alpha * curseIntensity;

            // Vertical line
            addVertex(consumer, matrix, rune.x - size * 0.1f, rune.y - size, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x + size * 0.1f, rune.y - size, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x + size * 0.1f, rune.y + size, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x - size * 0.1f, rune.y + size, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);

            // Horizontal line
            addVertex(consumer, matrix, rune.x - size, rune.y - size * 0.1f, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x + size, rune.y - size * 0.1f, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x + size, rune.y + size * 0.1f, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
            addVertex(consumer, matrix, rune.x - size, rune.y + size * 0.1f, rune.z,
                    PRIMARY_R, PRIMARY_G, PRIMARY_B, alpha);
        }
    }

    private void renderChains(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (ChainLink chain : chains) {
            chain.update(progress, curseIntensity, spiralRotation);

            if (chain.alpha <= 0f) continue;

            // Draw chain as a series of connected segments
            for (int i = 0; i < chain.segments.size() - 1; i++) {
                Vec3 p1 = chain.segments.get(i);
                Vec3 p2 = chain.segments.get(i + 1);

                Vec3 dir = new Vec3(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.06f);

                float alpha = chain.alpha * curseIntensity * 0.7f;

                addVertex(consumer, matrix,
                        (float)(p1.x - perp.x), (float)(p1.y - perp.y), (float)(p1.z - perp.z),
                        PRIMARY_R * 0.6f, PRIMARY_G * 0.6f, PRIMARY_B * 0.6f, alpha);
                addVertex(consumer, matrix,
                        (float)(p1.x + perp.x), (float)(p1.y + perp.y), (float)(p1.z + perp.z),
                        PRIMARY_R * 0.6f, PRIMARY_G * 0.6f, PRIMARY_B * 0.6f, alpha);
                addVertex(consumer, matrix,
                        (float)(p2.x + perp.x), (float)(p2.y + perp.y), (float)(p2.z + perp.z),
                        PRIMARY_R * 0.6f, PRIMARY_G * 0.6f, PRIMARY_B * 0.6f, alpha);
                addVertex(consumer, matrix,
                        (float)(p2.x - perp.x), (float)(p2.y - perp.y), (float)(p2.z - perp.z),
                        PRIMARY_R * 0.6f, PRIMARY_G * 0.6f, PRIMARY_B * 0.6f, alpha);
            }
        }
    }

    private void renderCurseMist(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Creeping mist at the base
        int segments = 16;
        float mistRadius = 2.5f + Mth.sin(currentTick * 0.05f) * 0.3f;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments + spiralRotation * 0.5f);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments + spiralRotation * 0.5f);

            float x1 = Mth.cos(angle1) * mistRadius;
            float z1 = Mth.sin(angle1) * mistRadius;
            float x2 = Mth.cos(angle2) * mistRadius;
            float z2 = Mth.sin(angle2) * mistRadius;

            float wave = Mth.sin(currentTick * 0.1f + angle1 * 2f) * 0.2f;
            float alpha = 0.25f * curseIntensity;

            addVertex(consumer, matrix, x1, -1.5f + wave, z1,
                    PRIMARY_R * 0.4f, PRIMARY_G * 0.4f, PRIMARY_B * 0.4f, alpha);
            addVertex(consumer, matrix, x2, -1.5f + wave, z2,
                    PRIMARY_R * 0.4f, PRIMARY_G * 0.4f, PRIMARY_B * 0.4f, alpha);
            addVertex(consumer, matrix, x2, -0.5f + wave, z2,
                    PRIMARY_R * 0.4f, PRIMARY_G * 0.4f, PRIMARY_B * 0.4f, 0f);
            addVertex(consumer, matrix, x1, -0.5f + wave, z1,
                    PRIMARY_R * 0.4f, PRIMARY_G * 0.4f, PRIMARY_B * 0.4f, 0f);
        }
    }

    private void renderAuraRings(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Pulsing rings of misfortune
        int ringCount = 3;
        for (int r = 0; r < ringCount; r++) {
            float ringOffset = r * 0.33f;
            float ringProgress = ((progress + ringOffset) % 1f);

            float ringY = -1f + ringProgress * 3f;
            float ringRadius = 1.5f + ringProgress * 1f;
            float ringAlpha = Mth.sin(ringProgress * Mth.PI) * 0.5f * curseIntensity;

            int segments = 24;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(angle1) * ringRadius;
                float z1 = Mth.sin(angle1) * ringRadius;
                float x2 = Mth.cos(angle2) * ringRadius;
                float z2 = Mth.sin(angle2) * ringRadius;

                addVertex(consumer, matrix, x1, ringY - 0.1f, z1,
                        PRIMARY_R, PRIMARY_G, PRIMARY_B, ringAlpha);
                addVertex(consumer, matrix, x2, ringY - 0.1f, z2,
                        PRIMARY_R, PRIMARY_G, PRIMARY_B, ringAlpha);
                addVertex(consumer, matrix, x2, ringY + 0.1f, z2,
                        PRIMARY_R, PRIMARY_G, PRIMARY_B, ringAlpha);
                addVertex(consumer, matrix, x1, ringY + 0.1f, z1,
                        PRIMARY_R, PRIMARY_G, PRIMARY_B, ringAlpha);
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

    private class CurseSpiral {
        List<Vec3> points = new ArrayList<>();
        float alpha;
        float thickness;
        float orbitRadius;
        float angleOffset;
        float heightOffset;
        float spiralSpeed;

        CurseSpiral(int index) {
            this.angleOffset = index * Mth.TWO_PI / 5f;
            this.orbitRadius = 1.5f + (index % 2) * 0.5f;
            this.heightOffset = index * 0.3f;
            this.thickness = 0.08f + random.nextFloat() * 0.04f;
            this.spiralSpeed = 0.8f + random.nextFloat() * 0.4f;
            generateSpiral();
        }

        void generateSpiral() {
            points.clear();
            int numPoints = 40;

            for (int i = 0; i < numPoints; i++) {
                float t = i / (float) numPoints;
                float angle = t * Mth.TWO_PI * 3f; // 3 full rotations
                float height = t * 4f - 2f; // Descends from top to bottom

                float radius = orbitRadius * (1f - t * 0.3f); // Tightens as it descends
                float x = Mth.cos(angle + angleOffset) * radius;
                float z = Mth.sin(angle + angleOffset) * radius;

                points.add(new Vec3(x, height + heightOffset, z));
            }
        }

        void update(float progress, float intensity, float rotation) {
            // Rotate the entire spiral
            for (int i = 0; i < points.size(); i++) {
                Vec3 p = points.get(i);
                float rotAmount = spiralSpeed * 0.05f;
                float newX = (float) (p.x * Math.cos(rotAmount) - p.z * Math.sin(rotAmount));
                float newZ = (float) (p.x * Math.sin(rotAmount) + p.z * Math.cos(rotAmount));
                points.set(i, new Vec3(newX, p.y, newZ));
            }

            this.alpha = 0.7f * (0.8f + 0.2f * Mth.sin(progress * 6f + angleOffset));
        }
    }

    private class MisfortuneParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float r, g, b;
        float lifetime;
        float age;

        MisfortuneParticle() {
            respawn();
        }

        void respawn() {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * 2.5f;

            this.x = Mth.cos(angle) * dist;
            this.y = random.nextFloat() * 3f - 1f;
            this.z = Mth.sin(angle) * dist;

            // Spiraling inward motion
            float centerDist = (float) Math.sqrt(x * x + z * z);
            float tangentAngle = angle + Mth.HALF_PI;
            
            this.vx = -x * 0.01f + Mth.cos(tangentAngle) * 0.02f;
            this.vy = -0.02f - random.nextFloat() * 0.01f; // Falling
            this.vz = -z * 0.01f + Mth.sin(tangentAngle) * 0.02f;

            this.size = 0.05f + random.nextFloat() * 0.06f;
            this.lifetime = 60f + random.nextFloat() * 40f;
            this.age = 0f;

            // Darker variants of the primary color
            float brightness = 0.6f + random.nextFloat() * 0.4f;
            this.r = PRIMARY_R * brightness;
            this.g = PRIMARY_G * brightness;
            this.b = PRIMARY_B * brightness;
        }

        void update(float progress, float intensity) {
            this.x += vx;
            this.y += vy;
            this.z += vz;
            this.age++;

            // Fade in and out
            float lifetimeProgress = age / lifetime;
            if (lifetimeProgress < 0.2f) {
                this.alpha = lifetimeProgress / 0.2f * 0.8f;
            } else if (lifetimeProgress > 0.8f) {
                this.alpha = (1f - lifetimeProgress) / 0.2f * 0.8f;
            } else {
                this.alpha = 0.8f;
            }

            // Respawn if too old or out of bounds
            if (age >= lifetime || Math.abs(y) > 3f) {
                respawn();
            }
        }
    }

    private class OminousRune {
        float x, y, z;
        float size;
        float alpha;
        float angleOffset;
        float orbitRadius;
        float rotationSpeed;

        OminousRune() {
            this.angleOffset = random.nextFloat() * Mth.TWO_PI;
            this.orbitRadius = 2f + random.nextFloat() * 0.5f;
            this.size = 0.15f + random.nextFloat() * 0.1f;
            this.rotationSpeed = 0.02f + random.nextFloat() * 0.01f;
        }

        void update(float progress, float intensity, float rotation) {
            float angle = angleOffset + rotation * rotationSpeed;
            
            this.x = Mth.cos(angle) * orbitRadius;
            this.y = 1f + Mth.sin(currentTick * 0.08f + angleOffset) * 0.3f;
            this.z = Mth.sin(angle) * orbitRadius;

            float pulse = Mth.sin(progress * 8f + angleOffset) * 0.5f + 0.5f;
            this.alpha = 0.6f + pulse * 0.3f;
        }
    }

    private class ChainLink {
        List<Vec3> segments = new ArrayList<>();
        float alpha;
        float angleOffset;
        float orbitRadius;

        ChainLink() {
            this.angleOffset = random.nextFloat() * Mth.TWO_PI;
            this.orbitRadius = 2.2f + random.nextFloat() * 0.3f;
            generateChain();
        }

        void generateChain() {
            segments.clear();
            int numLinks = 8;

            for (int i = 0; i < numLinks; i++) {
                float t = i / (float) numLinks;
                float height = 2.5f - t * 4f; // Top to bottom

                segments.add(new Vec3(0, height, 0));
            }
        }

        void update(float progress, float intensity, float rotation) {
            float angle = angleOffset + rotation * 0.3f;

            // Update chain positions to orbit
            for (int i = 0; i < segments.size(); i++) {
                Vec3 base = segments.get(i);
                float sway = Mth.sin(currentTick * 0.1f + i * 0.5f) * 0.2f;
                
                float radius = orbitRadius + sway;
                float x = Mth.cos(angle) * radius;
                float z = Mth.sin(angle) * radius;
                
                segments.set(i, new Vec3(x, base.y, z));
            }

            this.alpha = 0.6f * (0.85f + 0.15f * Mth.sin(progress * 5f + angleOffset));
        }
    }
}
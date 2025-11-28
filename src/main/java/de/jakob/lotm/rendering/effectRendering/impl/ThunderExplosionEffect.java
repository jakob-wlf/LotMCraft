package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ThunderExplosionEffect extends ActiveEffect {

    private float expansionProgress = 0f;
    private float flashIntensity = 1f;
    private final RandomSource random = RandomSource.create();
    private final List<LightningChain> lightningChains = new ArrayList<>();
    private final List<EnergyParticle> energyParticles = new ArrayList<>();

    public ThunderExplosionEffect(double x, double y, double z) {
        super(x, y, z, 60); // 80 ticks = 4 seconds

        // Initialize lightning chains
        for (int i = 0; i < 32; i++) {
            lightningChains.add(new LightningChain());
        }

        // Initialize energy particles
        for (int i = 0; i < 100; i++) {
            energyParticles.add(new EnergyParticle());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        // Update animation state
        float progress = getProgress();
        expansionProgress = Mth.clamp(progress * 1.1f, 0f, 1f);
        flashIntensity = (float) Math.max(0f, 1f - Math.pow(progress, 0.4));

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Render all effect layers
        renderMainSphere(poseStack, expansionProgress, flashIntensity);
        renderInnerCore(poseStack, expansionProgress, flashIntensity);
        renderSecondaryShell(poseStack, expansionProgress, flashIntensity);
        renderLightningBolts(poseStack, expansionProgress, flashIntensity);
        renderLightningChains(poseStack, expansionProgress, flashIntensity);
        renderShockwave(poseStack, expansionProgress, flashIntensity);
        renderGroundCracks(poseStack, expansionProgress, flashIntensity);
        renderElectricArcs(poseStack, expansionProgress, flashIntensity);
        renderEnergyRings(poseStack, expansionProgress, flashIntensity);
        renderEnergyParticles(poseStack, expansionProgress, flashIntensity);
        renderPurpleEnergyWaves(poseStack, expansionProgress, flashIntensity);
        renderElectricSpirals(poseStack, expansionProgress, flashIntensity);
        renderCorona(poseStack, expansionProgress, flashIntensity);

        poseStack.popPose();
    }

    private void renderMainSphere(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 3f + expansion * 20f; // Increased from 2f + 12f
        int segments = 32; // More segments for smoother look

        Matrix4f matrix = poseStack.last().pose();

        // 3 solid layers for more opaque purple
        for (int layer = 0; layer < 3; layer++) {
            float layerRadius = radius * (1f + layer * 0.08f);
            float layerAlpha = intensity * (1f - layer * 0.25f) * 0.75f; // Much higher alpha

            Tesselator tesselator = Tesselator.getInstance();

            for (int lat = 0; lat < segments; lat++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float theta1 = (float) (lat * Math.PI / segments);
                float theta2 = (float) ((lat + 1) * Math.PI / segments);

                for (int lon = 0; lon <= segments; lon++) {
                    float phi = (float) (lon * 2 * Math.PI / segments);

                    Vec3 v1 = spherePoint(layerRadius, theta1, phi);
                    Vec3 v2 = spherePoint(layerRadius, theta2, phi);

                    // Solid deep purple color
                    float r = 0.35f + intensity * 0.15f;
                    float g = 0.15f + intensity * 0.2f;
                    float b = 0.85f + intensity * 0.1f;
                    float a = layerAlpha;

                    // Rare electric flashes
                    if (random.nextFloat() < 0.03f) {
                        r = 0.65f;
                        g = 0.5f;
                        b = 1f;
                        a *= 1.2f;
                    }

                    a = Mth.clamp(a, 0f, 1f);

                    buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(r, g, b, a);
                    buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(r, g, b, a);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderSecondaryShell(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 4f + expansion * 22f; // Increased from 3f + 15f
        int segments = 24; // More segments

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        for (int lat = 0; lat < segments; lat++) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon <= segments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / segments);

                Vec3 v1 = spherePoint(radius, theta1, phi);
                Vec3 v2 = spherePoint(radius, theta2, phi);

                float a = intensity * 0.55f * (1f - expansion * 0.5f); // More opaque

                // Solid purple outer shell
                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(0.45f, 0.25f, 0.9f, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(0.45f, 0.25f, 0.9f, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderInnerCore(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.2f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 2f + expansion * 6f; // Increased from 1.2f + 4f
        float pulseRadius = radius + (float) Math.sin(currentTick * 0.5f) * 0.5f; // Bigger pulse
        Matrix4f matrix = poseStack.last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        int segments = 20; // More segments

        for (int lat = 0; lat < segments; lat++) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon <= segments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / segments);

                Vec3 v1 = spherePoint(pulseRadius, theta1, phi);
                Vec3 v2 = spherePoint(pulseRadius, theta2, phi);

                // Solid purple core
                float a = intensity * intensity * 0.65f; // More opaque
                a = Mth.clamp(a, 0f, 1f);

                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(0.7f, 0.5f, 0.95f, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(0.7f, 0.5f, 0.95f, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderLightningBolts(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float radius = 4f + expansion * 22f; // Increased from 3f + 15f

        // 50 lightning bolts (increased from 40)
        for (int i = 0; i < 50; i++) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float angle = (float) (i * Math.PI * 2 / 50);
            float verticalAngle = (float) ((i % 10) * Math.PI / 5 - Math.PI / 2); // More variation
            float width = 0.15f + intensity * 0.12f; // Slightly thicker

            Vec3 start = Vec3.ZERO;
            Vec3 direction = new Vec3(
                    Math.cos(angle) * Math.cos(verticalAngle),
                    Math.sin(verticalAngle),
                    Math.sin(angle) * Math.cos(verticalAngle)
            ).normalize();

            int segments = 12;
            Vec3 current = start;

            for (int seg = 0; seg < segments; seg++) {
                float segProgress = (float) seg / segments;
                Vec3 next = direction.scale(radius * (segProgress + 1f / segments))
                        .add(
                                (random.nextDouble() - 0.5) * 0.8,
                                (random.nextDouble() - 0.5) * 0.8,
                                (random.nextDouble() - 0.5) * 0.8
                        );

                Vec3 segDir = next.subtract(current).normalize();
                Vec3 perp = new Vec3(-segDir.z, 0, segDir.x).normalize().scale(width);

                float alpha1 = intensity * (1f - segProgress * 0.4f) * 0.5f;
                float alpha2 = intensity * (1f - (segProgress + 1f / segments) * 0.4f) * 0.5f;

                // Deep purple lightning
                float r = 0.5f + random.nextFloat() * 0.2f;
                float g = 0.3f + random.nextFloat() * 0.2f;
                float b = 0.95f + random.nextFloat() * 0.05f;

                buffer.addVertex(matrix,
                                (float)(current.x + perp.x),
                                (float)(current.y + perp.y),
                                (float)(current.z + perp.z))
                        .setColor(r, g, b, alpha1);

                buffer.addVertex(matrix,
                                (float)(current.x - perp.x),
                                (float)(current.y - perp.y),
                                (float)(current.z - perp.z))
                        .setColor(r, g, b, alpha1);

                buffer.addVertex(matrix,
                                (float)(next.x - perp.x),
                                (float)(next.y - perp.y),
                                (float)(next.z - perp.z))
                        .setColor(r, g, b, alpha2);

                buffer.addVertex(matrix,
                                (float)(next.x + perp.x),
                                (float)(next.y + perp.y),
                                (float)(next.z + perp.z))
                        .setColor(r, g, b, alpha2);

                current = next;
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderLightningChains(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (LightningChain chain : lightningChains) {
            chain.update(expansion, intensity);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i < chain.points.size() - 1; i++) {
                Vec3 current = chain.points.get(i);
                Vec3 next = chain.points.get(i + 1);

                Vec3 dir = next.subtract(current).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.12f);

                float alpha = chain.alpha * (1f - (float) i / chain.points.size()) * 0.6f; // Reduced

                // More purple tint
                buffer.addVertex(matrix,
                                (float)(current.x + perp.x),
                                (float)(current.y + perp.y),
                                (float)(current.z + perp.z))
                        .setColor(0.8f, 0.85f, 1f, alpha);

                buffer.addVertex(matrix,
                                (float)(current.x - perp.x),
                                (float)(current.y - perp.y),
                                (float)(current.z - perp.z))
                        .setColor(0.8f, 0.85f, 1f, alpha);

                buffer.addVertex(matrix,
                                (float)(next.x - perp.x),
                                (float)(next.y - perp.y),
                                (float)(next.z - perp.z))
                        .setColor(0.8f, 0.85f, 1f, alpha * 0.7f);

                buffer.addVertex(matrix,
                                (float)(next.x + perp.x),
                                (float)(next.y + perp.y),
                                (float)(next.z + perp.z))
                        .setColor(0.8f, 0.85f, 1f, alpha * 0.7f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderShockwave(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Multiple expanding rings
        for (int ring = 0; ring < 3; ring++) {
            float ringProgress = expansion - ring * 0.15f;
            if (ringProgress < 0) continue;

            float innerRadius = ringProgress * 24f; // Increased from 16f
            float outerRadius = innerRadius + 3f; // Thicker rings
            int segments = 64; // More segments for smoother look

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= segments; i++) {
                float angle = (float) (i * 2 * Math.PI / segments);
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float x1 = cos * innerRadius;
                float z1 = sin * innerRadius;
                float x2 = cos * outerRadius;
                float z2 = sin * outerRadius;

                float alpha = intensity * (1f - ringProgress) * 0.6f; // Reduced

                // Add purple tint
                buffer.addVertex(matrix, x1, 0.1f, z1).setColor(0.5f, 0.7f, 1f, alpha);
                buffer.addVertex(matrix, x2, 0.1f, z2).setColor(0.5f, 0.7f, 1f, 0f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderGroundCracks(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.6f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Radial cracks on the ground - more cracks
        for (int i = 0; i < 24; i++) { // Increased from 16
            float angle = (float) (i * Math.PI * 2 / 24);
            float length = expansion * 18f; // Longer cracks

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float width = 0.2f; // Wider cracks

            float perpX = -sin * width;
            float perpZ = cos * width;

            float alpha = intensity * 0.5f; // Reduced

            // More purple
            buffer.addVertex(matrix, perpX, 0.05f, perpZ).setColor(0.6f, 0.75f, 1f, alpha);
            buffer.addVertex(matrix, -perpX, 0.05f, -perpZ).setColor(0.6f, 0.75f, 1f, alpha);
            buffer.addVertex(matrix, cos * length - perpX, 0.05f, sin * length - perpZ).setColor(0.6f, 0.75f, 1f, 0f);
            buffer.addVertex(matrix, cos * length + perpX, 0.05f, sin * length + perpZ).setColor(0.6f, 0.75f, 1f, 0f);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderElectricArcs(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.4f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float radius = 3f + expansion * 16f; // Increased from 2f + 10f

        // More arcs between points
        for (int i = 0; i < 32; i++) { // Increased from 24
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = random.nextFloat() * (float) Math.PI;
            float phi1 = random.nextFloat() * (float) Math.PI * 2;
            float theta2 = random.nextFloat() * (float) Math.PI;
            float phi2 = random.nextFloat() * (float) Math.PI * 2;

            Vec3 start = spherePoint(radius, theta1, phi1);
            Vec3 end = spherePoint(radius, theta2, phi2);

            int arcSegments = 8;
            Vec3 current = start;

            for (int seg = 0; seg < arcSegments; seg++) {
                float t = (float) (seg + 1) / arcSegments;
                Vec3 next = start.lerp(end, t)
                        .add(
                                (random.nextDouble() - 0.5) * 1.2,
                                (random.nextDouble() - 0.5) * 1.2,
                                (random.nextDouble() - 0.5) * 1.2
                        );

                Vec3 dir = next.subtract(current).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.1f);

                float alpha = intensity * 0.6f; // Reduced

                // More purple
                buffer.addVertex(matrix,
                                (float)(current.x + perp.x),
                                (float)(current.y + perp.y),
                                (float)(current.z + perp.z))
                        .setColor(0.75f, 0.85f, 1f, alpha);

                buffer.addVertex(matrix,
                                (float)(current.x - perp.x),
                                (float)(current.y - perp.y),
                                (float)(current.z - perp.z))
                        .setColor(0.75f, 0.85f, 1f, alpha);

                buffer.addVertex(matrix,
                                (float)(next.x - perp.x),
                                (float)(next.y - perp.y),
                                (float)(next.z - perp.z))
                        .setColor(0.75f, 0.85f, 1f, alpha * 0.6f);

                buffer.addVertex(matrix,
                                (float)(next.x + perp.x),
                                (float)(next.y + perp.y),
                                (float)(next.z + perp.z))
                        .setColor(0.75f, 0.85f, 1f, alpha * 0.6f);

                current = next;
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderEnergyRings(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // More rings at different angles
        for (int ring = 0; ring < 7; ring++) { // Increased from 5
            poseStack.pushPose();

            float angle = currentTick * 4f + ring * 51.4f; // Adjusted for 7 rings
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.mulPose(Axis.XP.rotationDegrees(45f + 25f * ring));

            float radius = 4f + expansion * 12f; // Larger rings
            int segments = 64; // More segments

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            Matrix4f ringMatrix = poseStack.last().pose();

            for (int i = 0; i <= segments; i++) {
                float t = (float) i / segments;
                float ringAngle = t * (float) Math.PI * 2;
                float cos = (float) Math.cos(ringAngle);
                float sin = (float) Math.sin(ringAngle);

                float x = cos * radius;
                float y = sin * radius;

                float alpha = intensity * 0.55f * (float) Math.sin(t * Math.PI); // More opaque

                // Solid purple rings
                buffer.addVertex(ringMatrix, x, y - 0.2f, 0).setColor(0.55f, 0.35f, 0.95f, alpha); // Thicker
                buffer.addVertex(ringMatrix, x, y + 0.2f, 0).setColor(0.55f, 0.35f, 0.95f, alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderEnergyParticles(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (EnergyParticle particle : energyParticles) {
            particle.update(expansion, intensity);

            if (particle.alpha <= 0) continue;

            float size = particle.size;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Billboard quad with more purple
            buffer.addVertex(matrix, (float)(particle.pos.x - size), (float)(particle.pos.y - size), (float)(particle.pos.z - size))
                    .setColor(0.65f, 0.8f, 1f, particle.alpha);
            buffer.addVertex(matrix, (float)(particle.pos.x - size), (float)(particle.pos.y + size), (float)(particle.pos.z + size))
                    .setColor(0.65f, 0.8f, 1f, particle.alpha);
            buffer.addVertex(matrix, (float)(particle.pos.x + size), (float)(particle.pos.y + size), (float)(particle.pos.z + size))
                    .setColor(0.65f, 0.8f, 1f, particle.alpha);
            buffer.addVertex(matrix, (float)(particle.pos.x + size), (float)(particle.pos.y - size), (float)(particle.pos.z - size))
                    .setColor(0.65f, 0.8f, 1f, particle.alpha);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderPurpleEnergyWaves(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Pulsing purple energy waves - solid and vibrant
        for (int wave = 0; wave < 5; wave++) { // More waves
            float waveProgress = (expansion + wave * 0.18f) % 1f;
            float radius = 3f + waveProgress * 18f; // Larger waves
            int segments = 40; // More segments

            Tesselator tesselator = Tesselator.getInstance();

            for (int lat = 0; lat < segments / 2; lat++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float theta1 = (float) (lat * Math.PI / segments);
                float theta2 = (float) ((lat + 1) * Math.PI / segments);

                for (int lon = 0; lon <= segments; lon++) {
                    float phi = (float) (lon * 2 * Math.PI / segments);

                    Vec3 v1 = spherePoint(radius, theta1, phi);
                    Vec3 v2 = spherePoint(radius, theta2, phi);

                    // Deep vibrant purple - more opaque
                    float r = 0.75f;
                    float g = 0.2f + waveProgress * 0.15f;
                    float b = 0.95f;
                    float a = intensity * (1f - waveProgress) * 0.6f; // More opaque

                    buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(r, g, b, a);
                    buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(r, g, b, a);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderElectricSpirals(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Spiraling energy streams
        for (int spiral = 0; spiral < 8; spiral++) { // More spirals
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float startAngle = spiral * 45f; // Adjusted for 8 spirals
            int points = 80; // More points for longer spirals

            for (int i = 0; i < points - 1; i++) {
                float t = (float) i / points;
                float nextT = (float) (i + 1) / points;

                float angle1 = (float) Math.toRadians(startAngle + t * 720f + currentTick * 8f);
                float angle2 = (float) Math.toRadians(startAngle + nextT * 720f + currentTick * 8f);

                float radius1 = (0.8f + t * expansion * 15f); // Larger spiral radius
                float radius2 = (0.8f + nextT * expansion * 15f);
                float height1 = (t - 0.5f) * expansion * 12f; // Taller spirals
                float height2 = (nextT - 0.5f) * expansion * 12f;

                Vec3 pos1 = new Vec3(
                        Math.cos(angle1) * radius1,
                        height1,
                        Math.sin(angle1) * radius1
                );

                Vec3 pos2 = new Vec3(
                        Math.cos(angle2) * radius2,
                        height2,
                        Math.sin(angle2) * radius2
                );

                Vec3 dir = pos2.subtract(pos1).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.18f); // Thicker spirals

                float alpha = intensity * (1f - t * 0.5f) * 0.6f; // Reduced

                // More purple in spirals
                buffer.addVertex(matrix,
                                (float)(pos1.x + perp.x),
                                (float)(pos1.y + perp.y),
                                (float)(pos1.z + perp.z))
                        .setColor(0.65f, 0.75f, 1f, alpha);

                buffer.addVertex(matrix,
                                (float)(pos1.x - perp.x),
                                (float)(pos1.y - perp.y),
                                (float)(pos1.z - perp.z))
                        .setColor(0.65f, 0.75f, 1f, alpha);

                buffer.addVertex(matrix,
                                (float)(pos2.x - perp.x),
                                (float)(pos2.y - perp.y),
                                (float)(pos2.z - perp.z))
                        .setColor(0.65f, 0.75f, 1f, alpha);

                buffer.addVertex(matrix,
                                (float)(pos2.x + perp.x),
                                (float)(pos2.y + perp.y),
                                (float)(pos2.z + perp.z))
                        .setColor(0.65f, 0.75f, 1f, alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderCorona(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.5f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Corona flares radiating outward
        for (int i = 0; i < 48; i++) { // More corona flares
            float angle = (float) (i * Math.PI * 2 / 48);
            float length = (3f + expansion * 14f) * (1f + (float) Math.sin(currentTick * 0.3f + i) * 0.4f); // Longer flares

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float width = 0.28f * intensity; // Wider corona
            float perpX = -sin * width;
            float perpZ = cos * width;

            float startDist = 1.5f; // Start further out

            float alpha1 = intensity * 0.5f;
            float alpha2 = 0f;

            // Purple corona
            buffer.addVertex(matrix, cos * startDist + perpX, 0, sin * startDist + perpZ)
                    .setColor(0.75f, 0.5f, 1f, alpha1);
            buffer.addVertex(matrix, cos * startDist - perpX, 0, sin * startDist - perpZ)
                    .setColor(0.75f, 0.5f, 1f, alpha1);
            buffer.addVertex(matrix, cos * length - perpX, 0, sin * length - perpZ)
                    .setColor(0.5f, 0.3f, 0.9f, alpha2);
            buffer.addVertex(matrix, cos * length + perpX, 0, sin * length + perpZ)
                    .setColor(0.5f, 0.3f, 0.9f, alpha2);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private Vec3 spherePoint(float radius, float theta, float phi) {
        float x = (float) (radius * Math.sin(theta) * Math.cos(phi));
        float y = (float) (radius * Math.cos(theta));
        float z = (float) (radius * Math.sin(theta) * Math.sin(phi));
        return new Vec3(x, y, z);
    }

    // Helper classes for animated elements
    private class LightningChain {
        List<Vec3> points = new ArrayList<>();
        float alpha = 0f;
        float targetRadius;
        float currentRadius = 0f;

        LightningChain() {
            targetRadius = 3f + random.nextFloat() * 8f;
            regeneratePoints();
        }

        void regeneratePoints() {
            points.clear();
            points.add(Vec3.ZERO);

            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;

            Vec3 direction = new Vec3(
                    Math.sin(theta) * Math.cos(phi),
                    Math.cos(theta),
                    Math.sin(theta) * Math.sin(phi)
            ).normalize();

            int segments = 10;
            for (int i = 1; i <= segments; i++) {
                float t = (float) i / segments;
                Vec3 point = direction.scale(currentRadius * t)
                        .add(
                                (random.nextDouble() - 0.5) * 0.6,
                                (random.nextDouble() - 0.5) * 0.6,
                                (random.nextDouble() - 0.5) * 0.6
                        );
                points.add(point);
            }
        }

        void update(float expansion, float intensity) {
            currentRadius = expansion * targetRadius;
            alpha = intensity * (0.6f + random.nextFloat() * 0.4f);

            if (random.nextFloat() < 0.05f) {
                regeneratePoints();
            }
        }
    }

    private class EnergyParticle {
        Vec3 pos;
        Vec3 velocity;
        float alpha;
        float size;
        float lifetime;
        float maxLifetime;

        EnergyParticle() {
            reset();
        }

        void reset() {
            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;
            float dist = random.nextFloat() * 2f;

            pos = new Vec3(
                    Math.sin(theta) * Math.cos(phi) * dist,
                    Math.cos(theta) * dist,
                    Math.sin(theta) * Math.sin(phi) * dist
            );

            velocity = new Vec3(
                    (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.3
            );

            size = 0.08f + random.nextFloat() * 0.12f;
            maxLifetime = 20f + random.nextFloat() * 40f;
            lifetime = 0f;
            alpha = 0f;
        }

        void update(float expansion, float intensity) {
            lifetime++;

            float progress = lifetime / maxLifetime;

            if (progress > 1f) {
                reset();
                return;
            }

            pos = pos.add(velocity.scale(expansion * 0.5));
            velocity = velocity.add(pos.normalize().scale(-0.02));

            alpha = intensity * (float) Math.sin(progress * Math.PI) * 0.6f; // Reduced from 0.8
        }
    }
}
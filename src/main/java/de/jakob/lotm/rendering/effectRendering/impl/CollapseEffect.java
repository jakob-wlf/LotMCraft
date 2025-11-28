package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class CollapseEffect extends ActiveEffect {

    private float expansionProgress = 0f;
    private float dominanceIntensity = 1f;
    private final RandomSource random = RandomSource.create();
    private final List<Chain> chains = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<CrushingWave> crushingWaves = new ArrayList<>();

    public CollapseEffect(double x, double y, double z) {
        super(x, y, z, 70); // 3.5 seconds of absolute dominance

        // Initialize dominance chains
        for (int i = 0; i < 40; i++) {
            chains.add(new Chain());
        }

        // Initialize conquest particles
        for (int i = 0; i < 120; i++) {
            particles.add(new Particle());
        }

        // Initialize crushing waves
        for (int i = 0; i < 8; i++) {
            crushingWaves.add(new CrushingWave(i * 0.125f));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        // Update animation state
        float progress = getProgress();
        expansionProgress = Mth.clamp(progress * 1.15f, 0f, 1f);
        dominanceIntensity = (float) Math.max(0f, 1f - Math.pow(progress, 0.35));

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Render all effect layers - from core to outer elements
        renderCore(poseStack, expansionProgress, dominanceIntensity);
        renderBloodSphere(poseStack, expansionProgress, dominanceIntensity);
        renderDarknessShell(poseStack, expansionProgress, dominanceIntensity);
        renderConquestSpikes(poseStack, expansionProgress, dominanceIntensity);
        renderDominanceChains(poseStack, expansionProgress, dominanceIntensity);
        renderCrushingWaves(poseStack, expansionProgress, dominanceIntensity);
        renderAbyssalRings(poseStack, expansionProgress, dominanceIntensity);
        renderBloodTendrils(poseStack, expansionProgress, dominanceIntensity);
        renderOppressiveAura(poseStack, expansionProgress, dominanceIntensity);
        renderConquestParticles(poseStack, expansionProgress, dominanceIntensity);
        renderDarknessVortex(poseStack, expansionProgress, dominanceIntensity);
        renderCrimsonPulse(poseStack, expansionProgress, dominanceIntensity);
        renderDominationSpirals(poseStack, expansionProgress, dominanceIntensity);
        renderVoidCracks(poseStack, expansionProgress, dominanceIntensity);
        renderSupremacyCorona(poseStack, expansionProgress, dominanceIntensity);

        poseStack.popPose();
    }

    private void renderBloodSphere(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 3f + expansion * 22f;
        int segments = 32;
        Matrix4f matrix = poseStack.last().pose();

        // Multiple layers for deep purple appearance
        for (int layer = 0; layer < 3; layer++) {
            float layerRadius = radius * (1f + layer * 0.1f);
            float layerAlpha = intensity * (1f - layer * 0.3f) * 0.7f;

            Tesselator tesselator = Tesselator.getInstance();

            for (int lat = 0; lat < segments; lat++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float theta1 = (float) (lat * Math.PI / segments);
                float theta2 = (float) ((lat + 1) * Math.PI / segments);

                for (int lon = 0; lon <= segments; lon++) {
                    float phi = (float) (lon * 2 * Math.PI / segments);

                    Vec3 v1 = spherePoint(layerRadius, theta1, phi);
                    Vec3 v2 = spherePoint(layerRadius, theta2, phi);

                    // Deep purple with dark undertones
                    float r = 0.45f + intensity * 0.2f;
                    float g = 0.1f + intensity * 0.05f;
                    float b = 0.65f + intensity * 0.2f;
                    float a = layerAlpha;

                    // Occasional dark pulses
                    if (random.nextFloat() < 0.04f) {
                        r *= 0.4f;
                        g *= 0.3f;
                        b *= 0.4f;
                        a *= 1.3f;
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

    private void renderDarknessShell(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 4.5f + expansion * 24f;
        int segments = 28;
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

                float a = intensity * 0.5f * (1f - expansion * 0.4f);

                // Dark gray-black shell with purple highlights
                float noise = random.nextFloat();
                float r = 0.15f + noise * 0.08f;
                float g = 0.12f + noise * 0.05f;
                float b = 0.18f + noise * 0.1f;

                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(r, g, b, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(r, g, b, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderCore(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.15f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 2.5f + expansion * 7f;
        float pulseRadius = radius + (float) Math.sin(currentTick * 0.6f) * 0.7f;
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        int segments = 24;

        for (int lat = 0; lat < segments; lat++) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon <= segments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / segments);

                Vec3 v1 = spherePoint(pulseRadius, theta1, phi);
                Vec3 v2 = spherePoint(pulseRadius, theta2, phi);

                // Intense purple core
                float a = intensity * intensity * 0.7f;
                a = Mth.clamp(a, 0f, 1f);

                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(105 / 255f, 27 / 255f, 168 / 255f, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(105 / 255f, 27 / 255f, 168 / 255f, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderConquestSpikes(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float radius = 4.5f + expansion * 24f;

        // Jagged spikes radiating outward
        for (int i = 0; i < 60; i++) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float angle = (float) (i * Math.PI * 2 / 60);
            float verticalAngle = (float) ((i % 12) * Math.PI / 6 - Math.PI / 2);
            float width = 0.2f + intensity * 0.15f;

            Vec3 start = Vec3.ZERO;
            Vec3 direction = new Vec3(
                    Math.cos(angle) * Math.cos(verticalAngle),
                    Math.sin(verticalAngle),
                    Math.sin(angle) * Math.cos(verticalAngle)
            ).normalize();

            int segments = 14;
            Vec3 current = start;

            for (int seg = 0; seg < segments; seg++) {
                float segProgress = (float) seg / segments;
                Vec3 next = direction.scale(radius * (segProgress + 1f / segments))
                        .add(
                                (random.nextDouble() - 0.5) * 0.5,
                                (random.nextDouble() - 0.5) * 0.5,
                                (random.nextDouble() - 0.5) * 0.5
                        );

                Vec3 segDir = next.subtract(current).normalize();
                Vec3 perp = new Vec3(-segDir.z, 0, segDir.x).normalize().scale(width);

                float alpha1 = intensity * (1f - segProgress * 0.3f) * 0.55f;
                float alpha2 = intensity * (1f - (segProgress + 1f / segments) * 0.3f) * 0.55f;

                // Dark purple spikes with black edges
                float r = 0.35f - segProgress * 0.1f;
                float g = 0.05f;
                float b = 0.55f - segProgress * 0.15f;

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
                        .setColor(r * 0.5f, g, b * 0.5f, alpha2);

                buffer.addVertex(matrix,
                                (float)(next.x + perp.x),
                                (float)(next.y + perp.y),
                                (float)(next.z + perp.z))
                        .setColor(r * 0.5f, g, b * 0.5f, alpha2);

                current = next;
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderDominanceChains(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (Chain chain : chains) {
            chain.update(expansion, intensity);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i < chain.points.size() - 1; i++) {
                Vec3 current = chain.points.get(i);
                Vec3 next = chain.points.get(i + 1);

                Vec3 dir = next.subtract(current).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.14f);

                float alpha = chain.alpha * (1f - (float) i / chain.points.size()) * 0.65f;

                // Dark purple chains
                float r = 0.3f + (float) i / chain.points.size() * 0.2f;
                float g = 0.08f;
                float b = 0.5f + (float) i / chain.points.size() * 0.2f;

                buffer.addVertex(matrix,
                                (float)(current.x + perp.x),
                                (float)(current.y + perp.y),
                                (float)(current.z + perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(current.x - perp.x),
                                (float)(current.y - perp.y),
                                (float)(current.z - perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(next.x - perp.x),
                                (float)(next.y - perp.y),
                                (float)(next.z - perp.z))
                        .setColor(r * 0.7f, g, b * 0.7f, alpha * 0.7f);

                buffer.addVertex(matrix,
                                (float)(next.x + perp.x),
                                (float)(next.y + perp.y),
                                (float)(next.z + perp.z))
                        .setColor(r * 0.7f, g, b * 0.7f, alpha * 0.7f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderCrushingWaves(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (CrushingWave wave : crushingWaves) {
            wave.update(expansion, intensity);

            if (wave.progress < 0 || wave.progress > 1f) continue;

            float innerRadius = wave.progress * 26f;
            float outerRadius = innerRadius + 3.5f;
            int segments = 72;

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

                float alpha = wave.alpha * (1f - wave.progress) * 0.7f;

                // Dark purple crushing wave
                buffer.addVertex(matrix, x1, 0.15f, z1).setColor(0.4f, 0.1f, 0.6f, alpha);
                buffer.addVertex(matrix, x2, 0.15f, z2).setColor(0.2f, 0.05f, 0.3f, 0f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderAbyssalRings(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Rotating dark rings with purple highlights
        for (int ring = 0; ring < 9; ring++) {
            poseStack.pushPose();

            float angle = currentTick * 3.5f + ring * 40f;
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.mulPose(Axis.XP.rotationDegrees(35f + 20f * ring));

            float radius = 5f + expansion * 14f;
            int segments = 72;

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

                float alpha = intensity * 0.45f * (float) Math.sin(t * Math.PI);

                // Dark rings with purple highlights
                float r = 0.2f + (float) Math.sin(t * Math.PI * 4) * 0.15f;
                float g = 0.1f;
                float b = 0.35f + (float) Math.sin(t * Math.PI * 4) * 0.2f;

                buffer.addVertex(ringMatrix, x, y - 0.25f, 0).setColor(r, g, b, alpha);
                buffer.addVertex(ringMatrix, x, y + 0.25f, 0).setColor(r, g, b, alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderBloodTendrils(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.3f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float radius = 3.5f + expansion * 18f;

        // Writhing tendrils between distant points
        for (int i = 0; i < 40; i++) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = random.nextFloat() * (float) Math.PI;
            float phi1 = random.nextFloat() * (float) Math.PI * 2;
            float theta2 = random.nextFloat() * (float) Math.PI;
            float phi2 = random.nextFloat() * (float) Math.PI * 2;

            Vec3 start = spherePoint(radius, theta1, phi1);
            Vec3 end = spherePoint(radius, theta2, phi2);

            int arcSegments = 10;
            Vec3 current = start;

            for (int seg = 0; seg < arcSegments; seg++) {
                float t = (float) (seg + 1) / arcSegments;
                Vec3 next = start.lerp(end, t)
                        .add(
                                (random.nextDouble() - 0.5) * 1.4,
                                (random.nextDouble() - 0.5) * 1.4,
                                (random.nextDouble() - 0.5) * 1.4
                        );

                Vec3 dir = next.subtract(current).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.12f);

                float alpha = intensity * 0.6f * (1f - t * 0.4f);

                // Purple tendrils
                float r = 0.45f + random.nextFloat() * 0.15f;
                float g = 0.08f;
                float b = 0.65f + random.nextFloat() * 0.2f;

                buffer.addVertex(matrix,
                                (float)(current.x + perp.x),
                                (float)(current.y + perp.y),
                                (float)(current.z + perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(current.x - perp.x),
                                (float)(current.y - perp.y),
                                (float)(current.z - perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(next.x - perp.x),
                                (float)(next.y - perp.y),
                                (float)(next.z - perp.z))
                        .setColor(r * 0.6f, g, b * 0.6f, alpha * 0.6f);

                buffer.addVertex(matrix,
                                (float)(next.x + perp.x),
                                (float)(next.y + perp.y),
                                (float)(next.z + perp.z))
                        .setColor(r * 0.6f, g, b * 0.6f, alpha * 0.6f);

                current = next;
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderOppressiveAura(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Pulsing waves of dominance
        for (int wave = 0; wave < 6; wave++) {
            float waveProgress = (expansion + wave * 0.16f) % 1f;
            float radius = 3.5f + waveProgress * 20f;
            int segments = 48;

            Tesselator tesselator = Tesselator.getInstance();

            for (int lat = 0; lat < segments / 2; lat++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float theta1 = (float) (lat * Math.PI / segments);
                float theta2 = (float) ((lat + 1) * Math.PI / segments);

                for (int lon = 0; lon <= segments; lon++) {
                    float phi = (float) (lon * 2 * Math.PI / segments);

                    Vec3 v1 = spherePoint(radius, theta1, phi);
                    Vec3 v2 = spherePoint(radius, theta2, phi);

                    // Dark purple to black gradient
                    float r = 0.3f - waveProgress * 0.2f;
                    float g = 0.05f;
                    float b = 0.5f - waveProgress * 0.3f;
                    float a = intensity * (1f - waveProgress) * 0.55f;

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

    private void renderConquestParticles(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (Particle particle : particles) {
            particle.update(expansion, intensity);

            if (particle.alpha <= 0) continue;

            float size = particle.size;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Dark particles with purple highlights
            float r = particle.isRed ? 0.5f : 0.2f;
            float g = particle.isRed ? 0.1f : 0.15f;
            float b = particle.isRed ? 0.7f : 0.3f;

            buffer.addVertex(matrix, (float)(particle.pos.x - size), (float)(particle.pos.y - size), (float)(particle.pos.z - size))
                    .setColor(r, g, b, particle.alpha);
            buffer.addVertex(matrix, (float)(particle.pos.x - size), (float)(particle.pos.y + size), (float)(particle.pos.z + size))
                    .setColor(r, g, b, particle.alpha);
            buffer.addVertex(matrix, (float)(particle.pos.x + size), (float)(particle.pos.y + size), (float)(particle.pos.z + size))
                    .setColor(r, g, b, particle.alpha);
            buffer.addVertex(matrix, (float)(particle.pos.x + size), (float)(particle.pos.y - size), (float)(particle.pos.z - size))
                    .setColor(r, g, b, particle.alpha);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderDarknessVortex(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Swirling vortex of darkness
        for (int vortex = 0; vortex < 4; vortex++) {
            float vortexOffset = vortex * 0.25f;
            int segments = 48;

            Tesselator tesselator = Tesselator.getInstance();

            for (int lat = 0; lat < segments / 2; lat++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float theta1 = (float) (lat * Math.PI / segments);
                float theta2 = (float) ((lat + 1) * Math.PI / segments);

                for (int lon = 0; lon <= segments; lon++) {
                    float phi = (float) (lon * 2 * Math.PI / segments);
                    float spiralOffset = (float) Math.sin(expansion * Math.PI * 2 + vortexOffset) * 0.3f;

                    float radius = (2f + expansion * 16f) * (1f + spiralOffset);

                    Vec3 v1 = spherePoint(radius, theta1, phi + currentTick * 0.05f);
                    Vec3 v2 = spherePoint(radius, theta2, phi + currentTick * 0.05f);

                    // Dark swirling mass with purple tint
                    float r = 0.15f;
                    float g = 0.1f;
                    float b = 0.2f;
                    float a = intensity * 0.35f * (1f - expansion * 0.6f);

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

    private void renderCrimsonPulse(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float pulseIntensity = (float) Math.abs(Math.sin(currentTick * 0.15f));

        // Pulsing purple energy layer
        float radius = 3.2f + expansion * 19f + pulseIntensity * 1.5f;
        int segments = 36;

        Tesselator tesselator = Tesselator.getInstance();

        for (int lat = 0; lat < segments; lat++) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon <= segments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / segments);

                Vec3 v1 = spherePoint(radius, theta1, phi);
                Vec3 v2 = spherePoint(radius, theta2, phi);

                float a = intensity * pulseIntensity * 0.4f;

                // Pulsing purple
                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(0.55f, 0.12f, 0.75f, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(0.55f, 0.12f, 0.75f, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderDominationSpirals(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Spiraling dominance energy
        for (int spiral = 0; spiral < 10; spiral++) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float startAngle = spiral * 36f;
            int points = 90;

            for (int i = 0; i < points - 1; i++) {
                float t = (float) i / points;
                float nextT = (float) (i + 1) / points;

                float angle1 = (float) Math.toRadians(startAngle + t * 900f + currentTick * 7f);
                float angle2 = (float) Math.toRadians(startAngle + nextT * 900f + currentTick * 7f);

                float radius1 = (1f + t * expansion * 17f);
                float radius2 = (1f + nextT * expansion * 17f);
                float height1 = (t - 0.5f) * expansion * 14f;
                float height2 = (nextT - 0.5f) * expansion * 14f;

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
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.2f);

                float alpha = intensity * (1f - t * 0.4f) * 0.6f;

                // Dark purple spirals
                float r = 0.35f - t * 0.1f;
                float g = 0.08f;
                float b = 0.55f - t * 0.15f;

                buffer.addVertex(matrix,
                                (float)(pos1.x + perp.x),
                                (float)(pos1.y + perp.y),
                                (float)(pos1.z + perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(pos1.x - perp.x),
                                (float)(pos1.y - perp.y),
                                (float)(pos1.z - perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(pos2.x - perp.x),
                                (float)(pos2.y - perp.y),
                                (float)(pos2.z - perp.z))
                        .setColor(r * 0.6f, g, b * 0.6f, alpha);

                buffer.addVertex(matrix,
                                (float)(pos2.x + perp.x),
                                (float)(pos2.y + perp.y),
                                (float)(pos2.z + perp.z))
                        .setColor(r * 0.6f, g, b * 0.6f, alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderVoidCracks(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.5f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Radiating cracks of oppression
        for (int i = 0; i < 30; i++) {
            float angle = (float) (i * Math.PI * 2 / 30);
            float length = expansion * 22f;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float width = 0.25f;

            float perpX = -sin * width;
            float perpZ = cos * width;

            float alpha = intensity * 0.65f;

            // Dark cracks with purple glow
            buffer.addVertex(matrix, perpX, 0.08f, perpZ).setColor(0.35f, 0.08f, 0.55f, alpha);
            buffer.addVertex(matrix, -perpX, 0.08f, -perpZ).setColor(0.35f, 0.08f, 0.55f, alpha);
            buffer.addVertex(matrix, cos * length - perpX, 0.08f, sin * length - perpZ).setColor(0.15f, 0.05f, 0.2f, 0f);
            buffer.addVertex(matrix, cos * length + perpX, 0.08f, sin * length + perpZ).setColor(0.15f, 0.05f, 0.2f, 0f);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderSupremacyCorona(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.4f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Crown-like flares of domination
        for (int i = 0; i < 60; i++) {
            float angle = (float) (i * Math.PI * 2 / 60);
            float length = (3.5f + expansion * 16f) * (1f + (float) Math.sin(currentTick * 0.25f + i) * 0.5f);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float width = 0.32f * intensity;
            float perpX = -sin * width;
            float perpZ = cos * width;

            float startDist = 2f;

            float alpha1 = intensity * 0.6f;
            float alpha2 = 0f;

            // Purple corona
            buffer.addVertex(matrix, cos * startDist + perpX, 0, sin * startDist + perpZ)
                    .setColor(0.5f, 0.15f, 0.75f, alpha1);
            buffer.addVertex(matrix, cos * startDist - perpX, 0, sin * startDist - perpZ)
                    .setColor(0.5f, 0.15f, 0.75f, alpha1);
            buffer.addVertex(matrix, cos * length - perpX, 0, sin * length - perpZ)
                    .setColor(0.2f, 0.08f, 0.35f, alpha2);
            buffer.addVertex(matrix, cos * length + perpX, 0, sin * length + perpZ)
                    .setColor(0.2f, 0.08f, 0.35f, alpha2);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private Vec3 spherePoint(float radius, float theta, float phi) {
        float x = (float) (radius * Math.sin(theta) * Math.cos(phi)) * 1.6f;
        float y = (float) (radius * Math.cos(theta)) * 0.3f;
        float z = (float) (radius * Math.sin(theta) * Math.sin(phi)) * 1.6f;
        return new Vec3(x, y, z);
    }


    // Helper classes for animated elements
    private class Chain {
        List<Vec3> points = new ArrayList<>();
        float alpha = 0f;
        float targetRadius;
        float currentRadius = 0f;

        Chain() {
            targetRadius = 3.5f + random.nextFloat() * 9f;
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

            int segments = 12;
            for (int i = 1; i <= segments; i++) {
                float t = (float) i / segments;
                Vec3 point = direction.scale(currentRadius * t)
                        .add(
                                (random.nextDouble() - 0.5) * 0.7,
                                (random.nextDouble() - 0.5) * 0.7,
                                (random.nextDouble() - 0.5) * 0.7
                        );
                points.add(point);
            }
        }

        void update(float expansion, float intensity) {
            currentRadius = expansion * targetRadius;
            alpha = intensity * (0.5f + random.nextFloat() * 0.5f);

            if (random.nextFloat() < 0.06f) {
                regeneratePoints();
            }
        }
    }

    private class Particle {
        Vec3 pos;
        Vec3 velocity;
        float alpha;
        float size;
        float lifetime;
        float maxLifetime;
        boolean isRed;

        Particle() {
            reset();
        }

        void reset() {
            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;
            float dist = random.nextFloat() * 2.5f;

            pos = new Vec3(
                    Math.sin(theta) * Math.cos(phi) * dist,
                    Math.cos(theta) * dist,
                    Math.sin(theta) * Math.sin(phi) * dist
            );

            velocity = new Vec3(
                    (random.nextDouble() - 0.5) * 0.35,
                    (random.nextDouble() - 0.5) * 0.35,
                    (random.nextDouble() - 0.5) * 0.35
            );

            size = 0.1f + random.nextFloat() * 0.15f;
            maxLifetime = 25f + random.nextFloat() * 45f;
            lifetime = 0f;
            alpha = 0f;
            isRed = random.nextFloat() < 0.6f; // 60% purple, 40% dark
        }

        void update(float expansion, float intensity) {
            lifetime++;

            float progress = lifetime / maxLifetime;

            if (progress > 1f) {
                reset();
                return;
            }

            pos = pos.add(velocity.scale(expansion * 0.6));
            velocity = velocity.add(pos.normalize().scale(-0.025));

            alpha = intensity * (float) Math.sin(progress * Math.PI) * 0.7f;
        }
    }

    private class CrushingWave {
        float progress = 0f;
        float alpha = 0f;
        float offset;

        CrushingWave(float offset) {
            this.offset = offset;
        }

        void update(float expansion, float intensity) {
            progress = expansion - offset;
            alpha = intensity;
        }
    }
}
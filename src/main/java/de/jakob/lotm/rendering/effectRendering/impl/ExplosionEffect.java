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

public class ExplosionEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<DebrisParticle> debrisParticles = new ArrayList<>();
    private final List<SmokeColumn> smokeColumns = new ArrayList<>();
    private float expansionProgress = 0f;
    private float intensity = 1f;

    public ExplosionEffect(double x, double y, double z) {
        super(x, y, z, 60); // 3 seconds explosion

        // Initialize debris particles
        for (int i = 0; i < 80; i++) {
            debrisParticles.add(new DebrisParticle());
        }

        // Initialize smoke columns
        for (int i = 0; i < 12; i++) {
            smokeColumns.add(new SmokeColumn());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        float progress = getProgress();
        expansionProgress = Mth.clamp(progress * 1.2f, 0f, 1f);
        intensity = (float) Math.max(0f, 1f - Math.pow(progress, 0.5));

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Render layers from back to front
        renderShockwave(poseStack, expansionProgress, intensity);
        renderFireSphere(poseStack, expansionProgress, intensity);
        renderFireBurst(poseStack, expansionProgress, intensity);
        renderSmokeColumns(poseStack, expansionProgress, intensity);
        renderDebrisParticles(poseStack, expansionProgress, intensity);
        renderFlashCore(poseStack, expansionProgress, intensity);
        renderHeatDistortion(poseStack, expansionProgress, intensity);

        poseStack.popPose();
    }

    private void renderFlashCore(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.3f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 1.5f + expansion * 3f;
        float pulseRadius = radius + (float) Math.sin(currentTick * 0.8f) * 0.5f;
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        int segments = 20;

        for (int lat = 0; lat < segments; lat++) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon <= segments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / segments);

                Vec3 v1 = spherePoint(pulseRadius, theta1, phi);
                Vec3 v2 = spherePoint(pulseRadius, theta2, phi);

                // Bright white-yellow core
                float a = intensity * intensity * 0.9f;
                a = Mth.clamp(a, 0f, 1f);

                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                        .setColor(1f, 0.95f, 0.7f, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                        .setColor(1f, 0.95f, 0.7f, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFireSphere(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 2f + expansion * 12f;
        int segments = 28;
        Matrix4f matrix = poseStack.last().pose();

        // Multiple layers for fire effect
        for (int layer = 0; layer < 3; layer++) {
            float layerRadius = radius * (1f + layer * 0.15f);
            float layerAlpha = intensity * (1f - layer * 0.35f) * 0.6f;

            Tesselator tesselator = Tesselator.getInstance();

            for (int lat = 0; lat < segments; lat++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float theta1 = (float) (lat * Math.PI / segments);
                float theta2 = (float) ((lat + 1) * Math.PI / segments);

                for (int lon = 0; lon <= segments; lon++) {
                    float phi = (float) (lon * 2 * Math.PI / segments);

                    Vec3 v1 = spherePoint(layerRadius, theta1, phi);
                    Vec3 v2 = spherePoint(layerRadius, theta2, phi);

                    // Fire colors: bright orange to red
                    float noise = random.nextFloat();
                    float r = 1f;
                    float g = 0.4f + noise * 0.3f - layer * 0.2f;
                    float b = 0.1f + noise * 0.1f - layer * 0.1f;
                    float a = layerAlpha;

                    // Flickering
                    if (random.nextFloat() < 0.1f) {
                        g *= 1.3f;
                        b *= 1.5f;
                    }

                    a = Mth.clamp(a, 0f, 1f);

                    buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                            .setColor(r, g, b, a);
                    buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                            .setColor(r, g, b, a);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
        }

        RenderSystem.enableCull();
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

        // Ground shockwave
        float innerRadius = expansion * 18f;
        float outerRadius = innerRadius + 4f;
        int segments = 64;

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

            float alpha = intensity * (1f - expansion) * 0.8f;

            buffer.addVertex(matrix, x1, 0.1f, z1)
                    .setColor(1f, 0.7f, 0.3f, alpha);
            buffer.addVertex(matrix, x2, 0.1f, z2)
                    .setColor(1f, 0.5f, 0.2f, 0f);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        // Spherical shockwave
        float shockRadius = 3f + expansion * 15f;
        int shockSegments = 32;

        for (int lat = 0; lat < shockSegments / 4; lat++) {
            buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = (float) (lat * Math.PI / shockSegments);
            float theta2 = (float) ((lat + 1) * Math.PI / shockSegments);

            for (int lon = 0; lon <= shockSegments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / shockSegments);

                Vec3 v1 = spherePoint(shockRadius, theta1, phi);
                Vec3 v2 = spherePoint(shockRadius, theta2, phi);

                float a = intensity * (1f - expansion) * 0.5f;

                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                        .setColor(0.9f, 0.6f, 0.3f, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                        .setColor(0.9f, 0.6f, 0.3f, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFireBurst(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.2f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Radiating fire rays
        for (int i = 0; i < 40; i++) {
            float angle = (float) (i * Math.PI * 2 / 40);
            float verticalAngle = (float) ((i % 8) * Math.PI / 4 - Math.PI / 2);

            Vec3 direction = new Vec3(
                    Math.cos(angle) * Math.cos(verticalAngle),
                    Math.sin(verticalAngle),
                    Math.sin(angle) * Math.cos(verticalAngle)
            ).normalize();

            float length = expansion * 14f;
            float width = 0.4f * intensity;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            Vec3 end = direction.scale(length);
            Vec3 perp = new Vec3(-direction.z, 0, direction.x).normalize().scale(width);

            float alpha = intensity * (1f - expansion * 0.7f) * 0.7f;

            buffer.addVertex(matrix, (float) perp.x, (float) perp.y, (float) perp.z)
                    .setColor(1f, 0.6f, 0.2f, alpha);
            buffer.addVertex(matrix, (float) -perp.x, (float) -perp.y, (float) -perp.z)
                    .setColor(1f, 0.6f, 0.2f, alpha);
            buffer.addVertex(matrix, (float) (end.x - perp.x), (float) (end.y - perp.y), (float) (end.z - perp.z))
                    .setColor(1f, 0.3f, 0.1f, 0f);
            buffer.addVertex(matrix, (float) (end.x + perp.x), (float) (end.y + perp.y), (float) (end.z + perp.z))
                    .setColor(1f, 0.3f, 0.1f, 0f);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderDebrisParticles(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (DebrisParticle particle : debrisParticles) {
            particle.update(expansion, intensity);

            if (particle.alpha <= 0) continue;

            float size = particle.size;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Glowing debris
            float r = particle.isHot ? 1f : 0.3f;
            float g = particle.isHot ? 0.4f : 0.3f;
            float b = particle.isHot ? 0.1f : 0.3f;

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

    private void renderSmokeColumns(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (SmokeColumn column : smokeColumns) {
            column.update(expansion, intensity);

            for (int i = 0; i < column.puffs.size(); i++) {  // Changed: removed "- 1" to render all puffs
                SmokePuff puff = column.puffs.get(i);

                if (puff.alpha <= 0) continue;

                float radius = puff.size;
                int segments = 12;

                Tesselator tesselator = Tesselator.getInstance();

                // Render full sphere instead of half
                for (int lat = 0; lat < segments; lat++) {  // Changed: full sphere (was segments/2)
                    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                    float theta1 = (float) (lat * Math.PI / segments);
                    float theta2 = (float) ((lat + 1) * Math.PI / segments);

                    for (int lon = 0; lon <= segments; lon++) {
                        float phi = (float) (lon * 2 * Math.PI / segments);

                        Vec3 v1 = spherePoint(radius, theta1, phi).add(puff.pos);
                        Vec3 v2 = spherePoint(radius, theta2, phi).add(puff.pos);

                        // Dark smoke
                        float gray = 0.15f + puff.brightness * 0.2f;

                        buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                                .setColor(gray, gray, gray, puff.alpha);
                        buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                                .setColor(gray, gray, gray, puff.alpha);
                    }

                    BufferUploader.drawWithShader(buffer.buildOrThrow());
                }
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderHeatDistortion(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.4f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 2.5f + expansion * 10f;
        Matrix4f matrix = poseStack.last().pose();
        int segments = 24;

        Tesselator tesselator = Tesselator.getInstance();

        for (int lat = 0; lat < segments; lat++) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon <= segments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / segments);

                float wobble = (float) Math.sin(currentTick * 0.5f + phi * 3) * 0.2f;
                Vec3 v1 = spherePoint(radius * (1f + wobble), theta1, phi);
                Vec3 v2 = spherePoint(radius * (1f + wobble), theta2, phi);

                float a = intensity * 0.15f;

                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                        .setColor(1f, 0.9f, 0.8f, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                        .setColor(1f, 0.9f, 0.8f, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
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

    // Helper classes
    private class DebrisParticle {
        Vec3 pos;
        Vec3 velocity;
        float alpha;
        float size;
        float lifetime;
        float maxLifetime;
        boolean isHot;

        DebrisParticle() {
            reset();
        }

        void reset() {
            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;

            pos = Vec3.ZERO;

            velocity = new Vec3(
                    Math.sin(theta) * Math.cos(phi),
                    Math.cos(theta) * 0.7 + 0.3,
                    Math.sin(theta) * Math.sin(phi)
            ).normalize().scale(0.3 + random.nextDouble() * 0.5);

            size = 0.15f + random.nextFloat() * 0.2f;
            maxLifetime = 30f + random.nextFloat() * 25f;
            lifetime = 0f;
            alpha = 0f;
            isHot = random.nextFloat() < 0.4f;
        }

        void update(float expansion, float intensity) {
            lifetime++;

            float progress = lifetime / maxLifetime;

            if (progress > 1f) {
                alpha = 0f;
                return;
            }

            pos = pos.add(velocity);
            velocity = velocity.add(0, -0.015, 0); // Gravity

            alpha = intensity * (float) Math.sin(progress * Math.PI) * 0.8f;

            if (isHot) {
                alpha *= (1f - progress * 0.5f);
            }
        }
    }

    private class SmokeColumn {
        List<SmokePuff> puffs = new ArrayList<>();
        float angle;
        float distance;

        SmokeColumn() {
            angle = random.nextFloat() * (float) Math.PI * 2;
            distance = 1.5f + random.nextFloat() * 3f;

            for (int i = 0; i < 8; i++) {
                puffs.add(new SmokePuff(i));
            }
        }

        void update(float expansion, float intensity) {
            for (SmokePuff puff : puffs) {
                puff.update(expansion, intensity, angle, distance);
            }
        }
    }

    private class SmokePuff {
        Vec3 pos;
        float alpha;
        float size;
        float height;
        float brightness;
        int index;

        SmokePuff(int index) {
            this.index = index;
            this.brightness = random.nextFloat();
        }

        void update(float expansion, float intensity, float angle, float distance) {
            height = expansion * 8f + index * 1.2f;

            float spread = expansion * 2f;
            pos = new Vec3(
                    Math.cos(angle) * (distance + spread),
                    height,
                    Math.sin(angle) * (distance + spread)
            );

            size = 0.8f + expansion * 1.5f + index * 0.3f;

            float fadeIn = Mth.clamp(expansion * 5f, 0f, 1f);
            float fadeOut = 1f - expansion * 0.5f;
            alpha = fadeIn * fadeOut * 0.6f;
        }
    }
}
package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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

public class AbilityTheftEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<ConquestParticle> conquestParticles = new ArrayList<>();

    // Harmonized color palette - ethereal teal and deep purple
    private static final float[] TEAL_COLOR = {0.2f, 0.8f, 0.7f};      // Cyan-teal (51, 204, 178)
    private static final float[] PURPLE_COLOR = {0.4f, 0.2f, 0.7f};     // Deep purple (102, 51, 178)
    private static final float[] ACCENT_COLOR = {0.6f, 0.9f, 1.0f};    // Light cyan highlight

    public AbilityTheftEffect(double x, double y, double z) {
        super(x, y, z, 8);

        for (int i = 0; i < 150; i++) {
            conquestParticles.add(new ConquestParticle());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        float progress = getProgress();
        float expansionProgress = Mth.clamp(progress * 1.15f, 0f, 1f);
        float dominanceIntensity = (float) Math.max(0f, 1f - Math.pow(progress, 0.35));
        float pulseEffect = (float) (Math.sin(progress * Math.PI * 4) * 0.15 + 0.85);

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Outer teal sphere with pulse
        renderSphere(poseStack, expansionProgress, dominanceIntensity * pulseEffect,
                TEAL_COLOR[0], TEAL_COLOR[1], TEAL_COLOR[2], 3.8f, 2.4f, 0.5f);

        // Inner purple core with inverse pulse
        renderSphere(poseStack, expansionProgress, dominanceIntensity * (2f - pulseEffect),
                PURPLE_COLOR[0], PURPLE_COLOR[1], PURPLE_COLOR[2], 2.0f, 1.8f, 0.7f);

        // Accent shimmer layer
        renderSphere(poseStack, expansionProgress, dominanceIntensity * 0.3f * pulseEffect,
                ACCENT_COLOR[0], ACCENT_COLOR[1], ACCENT_COLOR[2], 2.8f, 2.0f, 0.3f);

        renderConquestParticles(poseStack, expansionProgress, dominanceIntensity, progress);
        renderEnergyStreaks(poseStack, expansionProgress, dominanceIntensity, progress);

        poseStack.popPose();
    }

    private void renderSphere(PoseStack poseStack, float expansion, float intensity,
                              float r, float g, float b, float startRadius, float slope, float baseAlpha) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = -slope * expansion + startRadius;
        int segments = 32;
        Matrix4f matrix = poseStack.last().pose();

        for (int layer = 0; layer < 3; layer++) {
            float layerRadius = radius * (1f + layer * 0.015f);
            float layerAlpha = intensity * (1f - layer * 0.15f) * baseAlpha;

            Tesselator tesselator = Tesselator.getInstance();

            for (int lat = 0; lat < segments; lat++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float theta1 = (float) (lat * Math.PI / segments);
                float theta2 = (float) ((lat + 1) * Math.PI / segments);

                for (int lon = 0; lon <= segments; lon++) {
                    float phi = (float) (lon * 2 * Math.PI / segments);

                    Vec3 v1 = spherePoint(layerRadius, theta1, phi);
                    Vec3 v2 = spherePoint(layerRadius, theta2, phi);

                    // Add gradient effect from poles
                    float gradient1 = 0.7f + 0.3f * (float)Math.abs(Math.cos(theta1));
                    float gradient2 = 0.7f + 0.3f * (float)Math.abs(Math.cos(theta2));

                    float a1 = Mth.clamp(layerAlpha * gradient1, 0f, 1f);
                    float a2 = Mth.clamp(layerAlpha * gradient2, 0f, 1f);

                    buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(r, g, b, a1);
                    buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(r, g, b, a2);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderConquestParticles(PoseStack poseStack, float expansion, float intensity, float globalProgress) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (ConquestParticle particle : conquestParticles) {
            particle.update(expansion, intensity, globalProgress);

            if (particle.alpha <= 0) continue;

            float size = particle.size;
            float r, g, b;

            if (particle.particleType == 0) {
                // Teal particles
                r = TEAL_COLOR[0];
                g = TEAL_COLOR[1];
                b = TEAL_COLOR[2];
            } else if (particle.particleType == 1) {
                // Purple particles
                r = PURPLE_COLOR[0];
                g = PURPLE_COLOR[1];
                b = PURPLE_COLOR[2];
            } else {
                // Accent particles
                r = ACCENT_COLOR[0];
                g = ACCENT_COLOR[1];
                b = ACCENT_COLOR[2];
            }

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

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

    private void renderEnergyStreaks(PoseStack poseStack, float expansion, float intensity, float progress) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        int streakCount = 8;
        for (int i = 0; i < streakCount; i++) {
            float angle = (float) (i * 2 * Math.PI / streakCount + progress * Math.PI * 2);
            float radius = 2.5f - expansion * 1.5f;
            float streakAlpha = intensity * 0.4f;

            if (streakAlpha <= 0 || radius <= 0) continue;

            float x1 = (float) (Math.cos(angle) * radius);
            float z1 = (float) (Math.sin(angle) * radius);
            float x2 = (float) (Math.cos(angle) * (radius * 0.3f));
            float z2 = (float) (Math.sin(angle) * (radius * 0.3f));

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float r = i % 2 == 0 ? TEAL_COLOR[0] : ACCENT_COLOR[0];
            float g = i % 2 == 0 ? TEAL_COLOR[1] : ACCENT_COLOR[1];
            float b = i % 2 == 0 ? TEAL_COLOR[2] : ACCENT_COLOR[2];

            buffer.addVertex(matrix, x1, 0.3f, z1).setColor(r, g, b, streakAlpha);
            buffer.addVertex(matrix, x1, -0.3f, z1).setColor(r, g, b, streakAlpha);
            buffer.addVertex(matrix, x2, 0.15f, z2).setColor(r, g, b, 0f);
            buffer.addVertex(matrix, x2, -0.15f, z2).setColor(r, g, b, 0f);

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

    private class ConquestParticle {
        Vec3 pos;
        Vec3 velocity;
        float alpha;
        float size;
        float lifetime;
        float maxLifetime;
        int particleType; // 0 = teal, 1 = purple, 2 = accent
        float orbitSpeed;

        ConquestParticle() {
            reset();
        }

        void reset() {
            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;
            float dist = 1.2f + random.nextFloat() * 1.2f;

            pos = new Vec3(
                    Math.sin(theta) * Math.cos(phi) * dist,
                    Math.cos(theta) * dist,
                    Math.sin(theta) * Math.sin(phi) * dist
            );

            velocity = new Vec3(
                    (random.nextDouble() - 0.5) * 0.8,
                    (random.nextDouble() - 0.5) * 0.8,
                    (random.nextDouble() - 0.5) * 0.8
            );

            size = 0.06f + random.nextFloat() * 0.10f;
            maxLifetime = 30f + random.nextFloat() * 50f;
            lifetime = 0f;
            alpha = 0f;
            orbitSpeed = (random.nextFloat() - 0.5f) * 0.02f;

            float rand = random.nextFloat();
            if (rand < 0.5f) {
                particleType = 0; // Teal
            } else if (rand < 0.85f) {
                particleType = 1; // Purple
            } else {
                particleType = 2; // Accent
            }
        }

        void update(float expansion, float intensity, float globalProgress) {
            lifetime++;

            float progress = lifetime / maxLifetime;

            if (progress > 1f) {
                reset();
                return;
            }

            // Orbital motion
            double currentRadius = Math.sqrt(pos.x * pos.x + pos.z * pos.z);
            double currentAngle = Math.atan2(pos.z, pos.x);
            double newAngle = currentAngle + orbitSpeed;

            Vec3 orbitalVelocity = new Vec3(
                    Math.cos(newAngle) * currentRadius - pos.x,
                    0,
                    Math.sin(newAngle) * currentRadius - pos.z
            ).scale(0.1);

            pos = pos.add(velocity.scale(expansion * 0.5).add(orbitalVelocity));
            velocity = velocity.add(pos.normalize().scale(-0.02));

            // Pulsing alpha with fade in/out
            float fadeIn = Mth.clamp(progress * 4f, 0f, 1f);
            float fadeOut = Mth.clamp((1f - progress) * 2f, 0f, 1f);
            float pulse = (float) (Math.sin(globalProgress * Math.PI * 3 + lifetime * 0.1) * 0.2 + 0.8);

            alpha = intensity * (float) Math.sin(progress * Math.PI) * fadeIn * fadeOut * pulse * 0.8f;
        }
    }
}
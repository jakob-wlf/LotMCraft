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


    public AbilityTheftEffect(double x, double y, double z) {
        super(x, y, z, 8);

        for (int i = 0; i < 120; i++) {
            conquestParticles.add(new ConquestParticle());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        // Update animation state
        float progress = getProgress();
        float expansionProgress = Mth.clamp(progress * 1.15f, 0f, 1f);
        float dominanceIntensity = (float) Math.max(0f, 1f - Math.pow(progress, 0.35));

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Light green sphere (144, 238, 144 = light green)
        renderSphere(poseStack, expansionProgress, dominanceIntensity, 144 / 255f, 238 / 255f, 144 / 255f, 3.5f, 2.2f);
        // Dark blue sphere (0, 0, 139 = dark blue)
        renderSphere(poseStack, expansionProgress, dominanceIntensity, 0 / 255f, 0 / 255f, 139 / 255f, 2.2f, 2.2f);
        renderConquestParticles(poseStack, expansionProgress, dominanceIntensity);

        poseStack.popPose();
    }

    private void renderSphere(PoseStack poseStack, float expansion, float intensity, float r, float g, float b, float startRadius, float slope) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = -slope * expansion + startRadius;
        int segments = 32;
        Matrix4f matrix = poseStack.last().pose();

        for (int layer = 0; layer < 3; layer++) {
            float layerRadius = radius * (1f + layer * 0.01f);
            float layerAlpha = intensity * (1f - layer * 0.01f) * 0.7f;

            Tesselator tesselator = Tesselator.getInstance();

            for (int lat = 0; lat < segments; lat++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float theta1 = (float) (lat * Math.PI / segments);
                float theta2 = (float) ((lat + 1) * Math.PI / segments);

                for (int lon = 0; lon <= segments; lon++) {
                    float phi = (float) (lon * 2 * Math.PI / segments);

                    Vec3 v1 = spherePoint(layerRadius, theta1, phi);
                    Vec3 v2 = spherePoint(layerRadius, theta2, phi);
                    float a = layerAlpha;

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

    private void renderConquestParticles(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (ConquestParticle particle : conquestParticles) {
            particle.update(expansion, intensity);

            if (particle.alpha <= 0) continue;

            float size = particle.size;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Light green or dark blue particles
            float r = particle.isGreen ? 144 / 255f : 0 / 255f;
            float g = particle.isGreen ? 238 / 255f : 0 / 255f;
            float b = particle.isGreen ? 144 / 255f : 139 / 255f;

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
        boolean isGreen;

        ConquestParticle() {
            reset();
        }

        void reset() {
            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;
            float dist = random.nextFloat() * 1.8f; // Reduced from 2.5f

            pos = new Vec3(
                    Math.sin(theta) * Math.cos(phi) * dist,
                    Math.cos(theta) * dist,
                    Math.sin(theta) * Math.sin(phi) * dist
            );

            velocity = new Vec3(
                    (random.nextDouble() - 0.5) * 1.2,  // Reduced from 1.5
                    (random.nextDouble() - 0.5) * 1.2,  // Reduced from 1.5
                    (random.nextDouble() - 0.5) * 1.2   // Reduced from 1.5
            );

            size = 0.08f + random.nextFloat() * 0.12f; // Reduced from 0.1f + 0.15f
            maxLifetime = 25f + random.nextFloat() * 45f;
            lifetime = 0f;
            alpha = 0f;
            isGreen = random.nextFloat() < 0.6f; // 60% green, 40% blue
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
}
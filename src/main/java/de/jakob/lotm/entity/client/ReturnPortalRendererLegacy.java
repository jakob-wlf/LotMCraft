package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.entity.custom.ReturnPortalEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ReturnPortalRendererLegacy extends EntityRenderer<ReturnPortalEntity> {
    private final RandomSource random = RandomSource.create();
    private final List<EnergyBolt> energyBolts = new ArrayList<>();
    private final List<FloatingOrb> floatingOrbs = new ArrayList<>();
    private float rotation = 0f;
    private boolean initialized = false;

    public ReturnPortalRendererLegacy(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ReturnPortalEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        // Initialize particles on first render
        if (!initialized) {
            initializeEffects();
            initialized = true;
        }

        float ageInTicks = entity.getTickCount() + partialTick;
        float progress = (ageInTicks % 200) / 200f; // Cycle every 10 seconds

        // Pulsating intensity
        float pulseSpeed = 3f;
        float pulse = (float) (0.7 + 0.3 * Math.sin(progress * pulseSpeed * Math.PI * 2));
        float secondaryPulse = (float) (0.6 + 0.4 * Math.sin(progress * pulseSpeed * 1.5 * Math.PI * 2));

        // Overall intensity
        float intensity = 1.0f;

        rotation += 0.5f * partialTick;

        poseStack.pushPose();

        // Render pulsating core
        renderCore(poseStack, intensity, pulse, secondaryPulse);

        // Render energy waves
        renderEnergyWaves(poseStack, intensity, progress);

        // Render rotating outer rings
        renderOuterRings(poseStack, intensity, rotation);

        // Render energy bolts
        renderEnergyBolts(poseStack, intensity, progress, partialTick);

        // Render floating orbs
        renderFloatingOrbs(poseStack, intensity, pulse, partialTick);

        poseStack.popPose();
    }

    private void initializeEffects() {
        // Initialize energy bolts
        for (int i = 0; i < 12; i++) {
            energyBolts.add(new EnergyBolt());
        }

        // Initialize floating orbs
        for (int i = 0; i < 8; i++) {
            floatingOrbs.add(new FloatingOrb());
        }
    }

    private void renderCore(PoseStack poseStack, float intensity, float pulse, float secondaryPulse) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float baseRadius = 0.8f;
        float radius = baseRadius * pulse;
        int segments = 28;
        Matrix4f matrix = poseStack.last().pose();

        // Inner purple core
        renderSphereLayer(matrix, radius * 0.8f, 0.7f, 0.2f, 1.0f, intensity * 0.9f, segments);

        // Mid purple layer
        renderSphereLayer(matrix, radius * 1.0f, 0.6f, 0.2f, 0.9f, intensity * 0.7f, segments);

        // Blue middle glow
        renderSphereLayer(matrix, radius * 1.2f * secondaryPulse, 0.3f, 0.5f, 1.0f, intensity * 0.6f, segments);

        // Purple outer glow
        renderSphereLayer(matrix, radius * 1.4f, 0.5f, 0.3f, 0.9f, intensity * 0.4f, segments);

        // Distant blue aura
        renderSphereLayer(matrix, radius * 1.7f, 0.2f, 0.4f, 0.8f, intensity * 0.2f, segments);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderEnergyWaves(PoseStack poseStack, float intensity, float progress) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        int segments = 32;

        // Multiple expanding waves
        for (int wave = 0; wave < 3; wave++) {
            float waveProgress = (progress * 2f + wave * 0.33f) % 1f;
            float waveRadius = 0.5f + waveProgress * 2.5f;
            float waveAlpha = intensity * (1f - waveProgress) * 0.3f;

            if (waveAlpha > 0.05f) {
                float r = wave % 2 == 0 ? 0.5f : 0.3f;
                float g = wave % 2 == 0 ? 0.3f : 0.5f;
                float b = 0.9f;

                renderSphereLayer(matrix, waveRadius, r, g, b, waveAlpha, segments);
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderSphereLayer(Matrix4f matrix, float radius, float r, float g, float b, float alpha, int segments) {
        Tesselator tesselator = Tesselator.getInstance();

        for (int lat = 0; lat < segments; lat++) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon <= segments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / segments);

                Vec3 v1 = spherePoint(radius, theta1, phi);
                Vec3 v2 = spherePoint(radius, theta2, phi);

                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                        .setColor(r, g, b, alpha);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                        .setColor(r, g, b, alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }
    }

    private void renderOuterRings(PoseStack poseStack, float intensity, float rotation) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Ring 1 - horizontal rotation (largest)
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0, 1, 0, rotation));
        renderRing(poseStack, 2.5f, 0.12f, 0.4f, 0.3f, 1.0f, intensity * 0.7f);
        poseStack.popPose();

        // Ring 2 - tilted rotation
        poseStack.pushPose();
        Vector3f axis2 = new Vector3f(1, 0, 1).normalize();
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(axis2.x, axis2.y, axis2.z, -rotation * 1.3f));
        renderRing(poseStack, 2.1f, 0.1f, 0.5f, 0.2f, 0.9f, intensity * 0.6f);
        poseStack.popPose();

        // Ring 3 - different axis
        poseStack.pushPose();
        Vector3f axis3 = new Vector3f(1, 1, 0).normalize();
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(axis3.x, axis3.y, axis3.z, rotation * 0.8f));
        renderRing(poseStack, 2.8f, 0.08f, 0.6f, 0.4f, 1.0f, intensity * 0.5f);
        poseStack.popPose();

        // Ring 4 - counter-rotating
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0, 1, 0, -rotation * 0.6f));
        renderRing(poseStack, 1.8f, 0.09f, 0.3f, 0.4f, 0.95f, intensity * 0.6f);
        poseStack.popPose();

        // Ring 5 - vertical-ish
        poseStack.pushPose();
        Vector3f axis5 = new Vector3f(0, 1, 1).normalize();
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(axis5.x, axis5.y, axis5.z, rotation * 1.5f));
        renderRing(poseStack, 2.3f, 0.07f, 0.55f, 0.35f, 0.92f, intensity * 0.45f);
        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderRing(PoseStack poseStack, float radius, float thickness, float r, float g, float b, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        int segments = 64;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);

            float x1 = cos * (radius - thickness);
            float z1 = sin * (radius - thickness);
            float x2 = cos * (radius + thickness);
            float z2 = sin * (radius + thickness);

            // Smooth gradient from center to edge
            float edgeFade = 0.5f;
            buffer.addVertex(matrix, x1, 0, z1).setColor(r, g, b, alpha);
            buffer.addVertex(matrix, x2, 0, z2).setColor(r, g, b, alpha * edgeFade);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void renderEnergyBolts(PoseStack poseStack, float intensity, float progress, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (EnergyBolt bolt : energyBolts) {
            bolt.update(intensity, progress, partialTick);

            if (bolt.alpha <= 0.05f) continue;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float r = bolt.isPurple ? 0.7f : 0.3f;
            float g = bolt.isPurple ? 0.3f : 0.6f;
            float b = 1.0f;

            for (int i = 0; i < bolt.points.size() - 1; i++) {
                Vec3 p1 = bolt.points.get(i);
                Vec3 p2 = bolt.points.get(i + 1);

                float segmentAlpha = bolt.alpha * (1f - (float)i / bolt.points.size());
                float width = bolt.width * (1f - (float)i / bolt.points.size() * 0.5f);

                Vec3 dir = p2.subtract(p1).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(width);

                buffer.addVertex(matrix, (float)(p1.x - perp.x), (float)(p1.y - perp.y), (float)(p1.z - perp.z))
                        .setColor(r, g, b, segmentAlpha);
                buffer.addVertex(matrix, (float)(p1.x + perp.x), (float)(p1.y + perp.y), (float)(p1.z + perp.z))
                        .setColor(r, g, b, segmentAlpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFloatingOrbs(PoseStack poseStack, float intensity, float pulse, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        int segments = 12;

        for (FloatingOrb orb : floatingOrbs) {
            orb.update(intensity, pulse, partialTick);

            if (orb.alpha <= 0.05f) continue;

            float r = orb.isPurple ? 0.7f : 0.3f;
            float g = orb.isPurple ? 0.3f : 0.6f;
            float b = 1.0f;

            poseStack.pushPose();
            poseStack.translate(orb.pos.x, orb.pos.y, orb.pos.z);

            Tesselator tesselator = Tesselator.getInstance();
            for (int lat = 0; lat < segments / 2; lat++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float theta1 = (float) (lat * Math.PI / (segments / 2));
                float theta2 = (float) ((lat + 1) * Math.PI / (segments / 2));

                for (int lon = 0; lon <= segments; lon++) {
                    float phi = (float) (lon * 2 * Math.PI / segments);

                    Vec3 v1 = spherePoint(orb.radius, theta1, phi);
                    Vec3 v2 = spherePoint(orb.radius, theta2, phi);

                    buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                            .setColor(r, g, b, orb.alpha);
                    buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                            .setColor(r, g, b, orb.alpha);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }

            poseStack.popPose();
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

    @Override
    public ResourceLocation getTextureLocation(ReturnPortalEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    }

    // Inner classes for effect particles
    private class EnergyBolt {
        List<Vec3> points;
        float alpha;
        float width;
        float lifetime;
        float maxLifetime;
        boolean isPurple;

        EnergyBolt() {
            points = new ArrayList<>();
            reset();
        }

        void reset() {
            points.clear();

            float startAngle = random.nextFloat() * (float) Math.PI * 2;
            float startRadius = 0.6f + random.nextFloat() * 0.4f;

            Vec3 start = new Vec3(
                    Math.cos(startAngle) * startRadius,
                    (random.nextFloat() - 0.5f) * 0.6f,
                    Math.sin(startAngle) * startRadius
            );

            points.add(start);

            int segments = 8 + random.nextInt(6);
            Vec3 current = start;

            for (int i = 0; i < segments; i++) {
                Vec3 toCenter = current.normalize().scale(-1);
                Vec3 randomOffset = new Vec3(
                        (random.nextDouble() - 0.5) * 0.4,
                        (random.nextDouble() - 0.5) * 0.4,
                        (random.nextDouble() - 0.5) * 0.4
                );

                current = current.add(toCenter.scale(0.3)).add(randomOffset);
                points.add(current);
            }

            width = 0.08f + random.nextFloat() * 0.06f;
            maxLifetime = 20f + random.nextFloat() * 30f;
            lifetime = 0f;
            alpha = 0f;
            isPurple = random.nextFloat() < 0.6f;
        }

        void update(float intensity, float progress, float partialTick) {
            lifetime += partialTick;

            float lifeProgress = lifetime / maxLifetime;

            if (lifeProgress > 1f) {
                reset();
                return;
            }

            alpha = intensity * (float) Math.sin(lifeProgress * Math.PI) * 0.8f;
        }
    }

    private class FloatingOrb {
        Vec3 pos;
        Vec3 velocity;
        float radius;
        float alpha;
        float lifetime;
        float orbitAngle;
        float orbitRadius;
        float orbitSpeed;
        boolean isPurple;

        FloatingOrb() {
            reset();
        }

        void reset() {
            orbitAngle = random.nextFloat() * (float) Math.PI * 2;
            orbitRadius = 1.8f + random.nextFloat() * 0.6f;
            orbitSpeed = 0.01f + random.nextFloat() * 0.02f;

            float verticalOffset = (random.nextFloat() - 0.5f) * 1.5f;

            pos = new Vec3(
                    Math.cos(orbitAngle) * orbitRadius,
                    verticalOffset,
                    Math.sin(orbitAngle) * orbitRadius
            );

            velocity = new Vec3(0, (random.nextFloat() - 0.5f) * 0.01f, 0);
            radius = 0.12f + random.nextFloat() * 0.08f;
            lifetime = 0f;
            alpha = 0f;
            isPurple = random.nextFloat() < 0.5f;
        }

        void update(float intensity, float pulse, float partialTick) {
            lifetime += partialTick;

            orbitAngle += orbitSpeed * partialTick;

            pos = new Vec3(
                    Math.cos(orbitAngle) * orbitRadius,
                    pos.y + velocity.y * partialTick,
                    Math.sin(orbitAngle) * orbitRadius
            );

            if (Math.abs(pos.y) > 2f) {
                velocity = new Vec3(velocity.x, -velocity.y, velocity.z);
            }

            alpha = intensity * pulse * 0.5f;
        }
    }
}
package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.entity.custom.SunKingdomEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SunKingdomEntityRenderer extends EntityRenderer<SunKingdomEntity> {

    public SunKingdomEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SunKingdomEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        float lifetime = entity.tickCount + partialTicks;
        int radius = entity.getRadius();

        poseStack.pushPose();

        // Render divine particles/motes floating around
        renderDivineParticles(poseStack, buffer, lifetime, radius, packedLight);

        // Render floor rays radiating outward (now double-sided)
        renderFloorRays(poseStack, buffer, lifetime, radius, packedLight);

        // Render secondary ray layer for depth
        renderSecondaryRays(poseStack, buffer, lifetime, radius, packedLight);

        // Render the central sun core
        renderCentralSun(poseStack, buffer, lifetime, packedLight);

        // Render divine halo rings above the sun
        renderDivineHalos(poseStack, buffer, lifetime, packedLight);

        // Render the divine boundary ring
        renderBoundaryRing(poseStack, buffer, lifetime, radius, packedLight);

        // Render ascending light beams
        renderAscendingBeams(poseStack, buffer, lifetime, radius, packedLight);

        poseStack.popPose();
    }

    private void renderCentralSun(PoseStack poseStack, MultiBufferSource buffer, float lifetime, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 1.5, 0);

        // Pulsing animation
        float pulse = 1.0f + Mth.sin(lifetime * 0.1f) * 0.15f;
        poseStack.scale(pulse, pulse, pulse);

        // Rotation for divine feel
        poseStack.mulPose(Axis.YP.rotationDegrees(lifetime * 2.0f));

        VertexConsumer consumer = buffer.getBuffer(RenderType.energySwirl(getTextureLocation(null), 0, 0));

        // Core sun sphere (multiple layers for glow effect)
        for (int layer = 0; layer < 4; layer++) {
            float scale = 3.0f + layer * 1.2f;
            float alpha = 1.0f - (layer * 0.2f);

            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);

            if (layer > 0) {
                poseStack.mulPose(Axis.XP.rotationDegrees(lifetime * (1.5f + layer * 0.5f)));
            }

            // Warmer, more golden colors
            float r = 1.0f;
            float g = 0.95f - layer * 0.05f;
            float b = 0.4f + layer * 0.1f;

            renderSphere(poseStack, consumer, r, g, b, alpha, packedLight);
            poseStack.popPose();
        }

        // Outer divine aura with shimmer
        poseStack.pushPose();
        poseStack.scale(9.0f, 9.0f, 9.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(lifetime * -1.0f));
        float shimmer = Mth.sin(lifetime * 0.2f) * 0.1f + 0.25f;
        renderSphere(poseStack, consumer, 1.0f, 0.9f, 0.3f, shimmer, packedLight);
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderSphere(PoseStack poseStack, VertexConsumer consumer, float r, float g, float b, float alpha, int packedLight) {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int segments = 16;

        for (int lat = 0; lat < segments; lat++) {
            float theta1 = (float) (lat * Math.PI / segments);
            float theta2 = (float) ((lat + 1) * Math.PI / segments);

            for (int lon = 0; lon < segments; lon++) {
                float phi1 = (float) (lon * 2 * Math.PI / segments);
                float phi2 = (float) ((lon + 1) * 2 * Math.PI / segments);

                float x1 = Mth.sin(theta1) * Mth.cos(phi1);
                float y1 = Mth.cos(theta1);
                float z1 = Mth.sin(theta1) * Mth.sin(phi1);

                float x2 = Mth.sin(theta1) * Mth.cos(phi2);
                float y2 = Mth.cos(theta1);
                float z2 = Mth.sin(theta1) * Mth.sin(phi2);

                float x3 = Mth.sin(theta2) * Mth.cos(phi2);
                float y3 = Mth.cos(theta2);
                float z3 = Mth.sin(theta2) * Mth.sin(phi2);

                float x4 = Mth.sin(theta2) * Mth.cos(phi1);
                float y4 = Mth.cos(theta2);
                float z4 = Mth.sin(theta2) * Mth.sin(phi1);

                consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x1, y1, z1);
                consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x2, y2, z2);
                consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x3, y3, z3);
                consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(x4, y4, z4);
            }
        }
    }

    @Override
    public boolean shouldRender(SunKingdomEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double camX, double camY, double camZ) {
        return true;
    }

    private void renderFloorRays(PoseStack poseStack, MultiBufferSource buffer, float lifetime, int radius, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        poseStack.translate(0, 0.05, 0);

        int rayCount = 32;
        float rayWidth = 2.5f;

        for (int i = 0; i < rayCount; i++) {
            float angle = (float) (2 * Math.PI * i / rayCount);
            float animatedAngle = angle + lifetime * 0.02f;

            // Animated wave effect on rays
            float wave = Mth.sin(lifetime * 0.15f + i * 0.5f) * 0.3f + 0.7f;

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(animatedAngle)));

            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            // Gradient from center to edge
            float alpha1 = 0.9f * wave;
            float alpha2 = 0.0f;

            // Ray quad TOP SIDE (visible from above)
            consumer.addVertex(pose, -rayWidth, 0, 0).setColor(1.0f, 0.95f, 0.5f, alpha1).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
            consumer.addVertex(pose, rayWidth, 0, 0).setColor(1.0f, 0.95f, 0.5f, alpha1).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
            consumer.addVertex(pose, rayWidth, 0, radius).setColor(1.0f, 0.9f, 0.3f, alpha2).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
            consumer.addVertex(pose, -rayWidth, 0, radius).setColor(1.0f, 0.9f, 0.3f, alpha2).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);

            // Ray quad BOTTOM SIDE (visible from below) - reversed winding order
            consumer.addVertex(pose, -rayWidth, 0, radius).setColor(1.0f, 0.9f, 0.3f, alpha2).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
            consumer.addVertex(pose, rayWidth, 0, radius).setColor(1.0f, 0.9f, 0.3f, alpha2).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
            consumer.addVertex(pose, rayWidth, 0, 0).setColor(1.0f, 0.95f, 0.5f, alpha1).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
            consumer.addVertex(pose, -rayWidth, 0, 0).setColor(1.0f, 0.95f, 0.5f, alpha1).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderSecondaryRays(PoseStack poseStack, MultiBufferSource buffer, float lifetime, int radius, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        poseStack.translate(0, 0.08, 0);

        int rayCount = 16;
        float rayWidth = 1.5f;

        for (int i = 0; i < rayCount; i++) {
            float angle = (float) (2 * Math.PI * i / rayCount);
            float animatedAngle = angle - lifetime * 0.03f; // Counter-rotating

            float wave = Mth.sin(lifetime * 0.12f + i * 0.8f) * 0.25f + 0.6f;

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(animatedAngle)));

            Matrix4f pose = poseStack.last().pose();

            float alpha1 = 0.7f * wave;
            float alpha2 = 0.0f;

            // Top side
            consumer.addVertex(pose, -rayWidth, 0, 0).setColor(1.0f, 0.9f, 0.4f, alpha1).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
            consumer.addVertex(pose, rayWidth, 0, 0).setColor(1.0f, 0.9f, 0.4f, alpha1).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
            consumer.addVertex(pose, rayWidth, 0, radius).setColor(1.0f, 0.85f, 0.3f, alpha2).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
            consumer.addVertex(pose, -rayWidth, 0, radius).setColor(1.0f, 0.85f, 0.3f, alpha2).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);

            // Bottom side
            consumer.addVertex(pose, -rayWidth, 0, radius).setColor(1.0f, 0.85f, 0.3f, alpha2).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
            consumer.addVertex(pose, rayWidth, 0, radius).setColor(1.0f, 0.85f, 0.3f, alpha2).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
            consumer.addVertex(pose, rayWidth, 0, 0).setColor(1.0f, 0.9f, 0.4f, alpha1).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
            consumer.addVertex(pose, -rayWidth, 0, 0).setColor(1.0f, 0.9f, 0.4f, alpha1).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderDivineHalos(PoseStack poseStack, MultiBufferSource buffer, float lifetime, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        for (int halo = 0; halo < 3; halo++) {
            poseStack.pushPose();

            float height = 1.5f + 8.0f + halo * 2.5f;
            float haloRadius = 6.0f + halo * 2.0f;

            poseStack.translate(0, height, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(lifetime * (0.5f + halo * 0.3f)));

            float tilt = 5.0f * (halo + 1);
            poseStack.mulPose(Axis.XP.rotationDegrees(tilt));

            Matrix4f pose = poseStack.last().pose();

            int segments = 48;
            float thickness = 0.3f;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (2 * Math.PI * i / segments);
                float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

                float x1Inner = Mth.cos(angle1) * (haloRadius - thickness);
                float z1Inner = Mth.sin(angle1) * (haloRadius - thickness);
                float x1Outer = Mth.cos(angle1) * (haloRadius + thickness);
                float z1Outer = Mth.sin(angle1) * (haloRadius + thickness);

                float x2Inner = Mth.cos(angle2) * (haloRadius - thickness);
                float z2Inner = Mth.sin(angle2) * (haloRadius - thickness);
                float x2Outer = Mth.cos(angle2) * (haloRadius + thickness);
                float z2Outer = Mth.sin(angle2) * (haloRadius + thickness);

                float pulse = Mth.sin(lifetime * 0.15f + i * 0.15f + halo) * 0.3f + 0.7f;
                float alpha = (0.7f - halo * 0.2f) * pulse;

                // Top side
                consumer.addVertex(pose, x1Inner, 0, z1Inner).setColor(1.0f, 0.95f, 0.6f, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
                consumer.addVertex(pose, x2Inner, 0, z2Inner).setColor(1.0f, 0.95f, 0.6f, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
                consumer.addVertex(pose, x2Outer, 0, z2Outer).setColor(1.0f, 0.9f, 0.5f, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
                consumer.addVertex(pose, x1Outer, 0, z1Outer).setColor(1.0f, 0.9f, 0.5f, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);

                // Bottom side
                consumer.addVertex(pose, x1Outer, 0, z1Outer).setColor(1.0f, 0.9f, 0.5f, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
                consumer.addVertex(pose, x2Outer, 0, z2Outer).setColor(1.0f, 0.9f, 0.5f, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
                consumer.addVertex(pose, x2Inner, 0, z2Inner).setColor(1.0f, 0.95f, 0.6f, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
                consumer.addVertex(pose, x1Inner, 0, z1Inner).setColor(1.0f, 0.95f, 0.6f, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
            }

            poseStack.popPose();
        }
    }

    private void renderDivineParticles(PoseStack poseStack, MultiBufferSource buffer, float lifetime, int radius, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        int particleCount = 50;

        for (int i = 0; i < particleCount; i++) {
            float angle = (float) (2 * Math.PI * i / particleCount);
            float distance = (radius * 0.7f) * ((i % 3 + 1) / 3.0f);

            float orbitSpeed = 0.02f + (i % 5) * 0.005f;
            float currentAngle = angle + lifetime * orbitSpeed;

            float x = Mth.cos(currentAngle) * distance;
            float z = Mth.sin(currentAngle) * distance;
            float y = 0.5f + Mth.sin(lifetime * 0.1f + i) * 3.0f + (i % 7);

            poseStack.pushPose();
            poseStack.translate(x, y, z);

            float size = 0.15f + (i % 3) * 0.05f;
            float pulse = Mth.sin(lifetime * 0.2f + i) * 0.3f + 0.7f;
            float alpha = 0.8f * pulse;

            Matrix4f pose = poseStack.last().pose();

            // Billboard quad
            consumer.addVertex(pose, -size, -size, 0).setColor(1.0f, 0.95f, 0.5f, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
            consumer.addVertex(pose, size, -size, 0).setColor(1.0f, 0.95f, 0.5f, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
            consumer.addVertex(pose, size, size, 0).setColor(1.0f, 0.9f, 0.4f, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
            consumer.addVertex(pose, -size, size, 0).setColor(1.0f, 0.9f, 0.4f, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);

            poseStack.popPose();
        }
    }

    private void renderAscendingBeams(PoseStack poseStack, MultiBufferSource buffer, float lifetime, int radius, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        int beamCount = 8;

        for (int i = 0; i < beamCount; i++) {
            float angle = (float) (2 * Math.PI * i / beamCount) + lifetime * 0.01f;
            float distance = radius * 0.5f;

            float x = Mth.cos(angle) * distance;
            float z = Mth.sin(angle) * distance;

            float beamHeight = 20.0f + Mth.sin(lifetime * 0.08f + i) * 5.0f;
            float width = 0.8f;
            float pulse = Mth.sin(lifetime * 0.1f + i * 0.7f) * 0.3f + 0.7f;

            poseStack.pushPose();
            poseStack.translate(x, 0.1f, z);

            Matrix4f pose = poseStack.last().pose();

            float r = 1.0f * pulse;
            float g = 0.93f * pulse;
            float b = 0.45f * pulse;

            // Four sides for volume
            // North
            consumer.addVertex(pose, -width, 0, -width).setColor(r, g, b, 0.6f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, -1);
            consumer.addVertex(pose, width, 0, -width).setColor(r, g, b, 0.6f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, -1);
            consumer.addVertex(pose, width, beamHeight, -width).setColor(r, g, b, 0.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, -1);
            consumer.addVertex(pose, -width, beamHeight, -width).setColor(r, g, b, 0.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, -1);

            // South
            consumer.addVertex(pose, -width, 0, width).setColor(r, g, b, 0.6f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, 1);
            consumer.addVertex(pose, width, 0, width).setColor(r, g, b, 0.6f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, 1);
            consumer.addVertex(pose, width, beamHeight, width).setColor(r, g, b, 0.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, 1);
            consumer.addVertex(pose, -width, beamHeight, width).setColor(r, g, b, 0.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, 1);

            // East
            consumer.addVertex(pose, width, 0, -width).setColor(r, g, b, 0.6f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(1, 0, 0);
            consumer.addVertex(pose, width, 0, width).setColor(r, g, b, 0.6f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(1, 0, 0);
            consumer.addVertex(pose, width, beamHeight, width).setColor(r, g, b, 0.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(1, 0, 0);
            consumer.addVertex(pose, width, beamHeight, -width).setColor(r, g, b, 0.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(1, 0, 0);

            // West
            consumer.addVertex(pose, -width, 0, -width).setColor(r, g, b, 0.6f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(-1, 0, 0);
            consumer.addVertex(pose, -width, 0, width).setColor(r, g, b, 0.6f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(-1, 0, 0);
            consumer.addVertex(pose, -width, beamHeight, width).setColor(r, g, b, 0.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(-1, 0, 0);
            consumer.addVertex(pose, -width, beamHeight, -width).setColor(r, g, b, 0.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(-1, 0, 0);

            poseStack.popPose();
        }
    }

    private void renderBoundaryRing(PoseStack poseStack, MultiBufferSource buffer, float lifetime, int radius, int packedLight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        poseStack.pushPose();

        // Multiple ring layers for divine effect
        for (int layer = 0; layer < 3; layer++) {
            float heightOffset = 0.1f + layer * 3.0f;
            float radiusOffset = layer * 0.5f;
            float thickness = 1.5f - layer * 0.3f;

            poseStack.pushPose();
            poseStack.translate(0, heightOffset, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(lifetime * (1.0f + layer * 0.5f)));

            Matrix4f pose = poseStack.last().pose();

            int segments = 64;
            float actualRadius = radius + radiusOffset;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (2 * Math.PI * i / segments);
                float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

                float x1Inner = Mth.cos(angle1) * (actualRadius - thickness);
                float z1Inner = Mth.sin(angle1) * (actualRadius - thickness);
                float x1Outer = Mth.cos(angle1) * (actualRadius + thickness);
                float z1Outer = Mth.sin(angle1) * (actualRadius + thickness);

                float x2Inner = Mth.cos(angle2) * (actualRadius - thickness);
                float z2Inner = Mth.sin(angle2) * (actualRadius - thickness);
                float x2Outer = Mth.cos(angle2) * (actualRadius + thickness);
                float z2Outer = Mth.sin(angle2) * (actualRadius + thickness);

                // Pulsing golden color
                float pulse = Mth.sin(lifetime * 0.1f + i * 0.1f) * 0.2f + 0.8f;
                float r = 1.0f * pulse;
                float g = 0.95f * pulse;
                float b = 0.4f * pulse;
                float alpha = 0.9f - layer * 0.2f;

                // Ring segment quad - TOP SIDE
                consumer.addVertex(pose, x1Inner, 0, z1Inner).setColor(r, g, b, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
                consumer.addVertex(pose, x2Inner, 0, z2Inner).setColor(r, g, b, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
                consumer.addVertex(pose, x2Outer, 0, z2Outer).setColor(r, g, b, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);
                consumer.addVertex(pose, x1Outer, 0, z1Outer).setColor(r, g, b, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 1, 0);

                // Ring segment quad - BOTTOM SIDE
                consumer.addVertex(pose, x1Outer, 0, z1Outer).setColor(r, g, b, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
                consumer.addVertex(pose, x2Outer, 0, z2Outer).setColor(r, g, b, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
                consumer.addVertex(pose, x2Inner, 0, z2Inner).setColor(r, g, b, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);
                consumer.addVertex(pose, x1Inner, 0, z1Inner).setColor(r, g, b, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, -1, 0);

                // Vertical pillars of light at intervals
                if (i % 8 == 0 && layer == 0) {
                    float pillarHeight = 15.0f + Mth.sin(lifetime * 0.1f + i) * 3.0f;
                    float pillarX = Mth.cos(angle1) * actualRadius;
                    float pillarZ = Mth.sin(angle1) * actualRadius;

                    renderLightPillar(pose, consumer, pillarX, pillarZ, pillarHeight, lifetime + i);
                }
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderLightPillar(Matrix4f pose, VertexConsumer consumer, float x, float z, float height, float time) {
        float width = 0.5f;
        float pulse = Mth.sin(time * 0.15f) * 0.3f + 0.7f;

        float r = 1.0f * pulse;
        float g = 0.95f * pulse;
        float b = 0.5f * pulse;

        // Four sides of pillar
        // Front
        consumer.addVertex(pose, x - width, 0, z).setColor(r, g, b, 0.8f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, -1);
        consumer.addVertex(pose, x + width, 0, z).setColor(r, g, b, 0.8f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, -1);
        consumer.addVertex(pose, x + width, height, z).setColor(r, g, b, 0.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, -1);
        consumer.addVertex(pose, x - width, height, z).setColor(r, g, b, 0.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0, 0, -1);

        // Right
        consumer.addVertex(pose, x, 0, z - width).setColor(r, g, b, 0.8f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(-1, 0, 0);
        consumer.addVertex(pose, x, 0, z + width).setColor(r, g, b, 0.8f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(-1, 0, 0);
        consumer.addVertex(pose, x, height, z + width).setColor(r, g, b, 0.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(-1, 0, 0);
        consumer.addVertex(pose, x, height, z - width).setColor(r, g, b, 0.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(-1, 0, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(SunKingdomEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}
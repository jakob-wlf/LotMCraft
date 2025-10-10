package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.entity.custom.SpaceCollapseEntityLegacy;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class SpaceCollapseRendererLegacy extends EntityRenderer<SpaceCollapseEntityLegacy> {

    public SpaceCollapseRendererLegacy(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SpaceCollapseEntityLegacy entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        float size = entity.getSize();
        int stage = entity.getGrowthStage();
        float time = (entity.getTickCount() + partialTicks);
        float alpha = stage == 3 ? Mth.clamp(size / 8.0f, 0.0f, 1.0f) : 1.0f;

        poseStack.pushPose();

        // Layer 1: Deep swirling void - the abyss itself
        renderSwirlingVoidLayers(poseStack, buffer, size, time, alpha);

        // Layer 2: MASSIVE black core spheres from all angles
        for (int angle = 0; angle < 6; angle++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(angle * 60));
            poseStack.mulPose(Axis.XP.rotationDegrees(angle * 30));
            renderSolidBlackSphere(poseStack, buffer, size, alpha);
            poseStack.popPose();
        }

        // Layer 3: Violent tearing edges with lightning
        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 90 + time * 5));
            poseStack.mulPose(Axis.XP.rotationDegrees(i * 45));
            renderTornEdges(poseStack, buffer, size, stage, time, alpha);
            poseStack.popPose();
        }

        // Layer 4: Reality cracks spreading outward
        renderRealityCracks(poseStack, buffer, size, stage, time, alpha);

        // Layer 5: Massive energy vortex being pulled in
        renderEnergyVortex(poseStack, buffer, size, stage, time, alpha);

        // Layer 6: Outer destruction aura
        if (stage >= 2) {
            renderDestructionAura(poseStack, buffer, size, time, alpha);
        }

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderSwirlingVoidLayers(PoseStack poseStack, MultiBufferSource buffer,
                                          float size, float time, float alpha) {
        // Render multiple swirling layers of darkness
        for (int layer = 0; layer < 5; layer++) {
            poseStack.pushPose();

            float layerRotation = time * (3 + layer) * (layer % 2 == 0 ? 1 : -1);
            float layerSize = size * (1.0f + layer * 0.15f);

            poseStack.mulPose(Axis.YP.rotationDegrees(layerRotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(layerRotation * 0.7f));
            poseStack.mulPose(Axis.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(this.entityRenderDispatcher.camera.getXRot()));

            VertexConsumer consumer = buffer.getBuffer(RenderType.endPortal());
            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();

            int segments = 48;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (2 * Math.PI * i / segments);
                float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

                // Spiral distortion
                float spiral = (float) Math.sin(angle1 * 3 + time * 0.1f) * 0.2f;

                float x1 = Mth.cos(angle1) * layerSize * (1.0f + spiral);
                float y1 = Mth.sin(angle1) * layerSize * (1.0f + spiral);
                float x2 = Mth.cos(angle2) * layerSize * (1.0f + spiral);
                float y2 = Mth.sin(angle2) * layerSize * (1.0f + spiral);

                // Dark purple-black swirling energy
                float darkness = 0.05f + layer * 0.02f;
                float layerAlpha = alpha * (0.9f - layer * 0.15f);

                vertex(consumer, matrix, pose, 0, 0, 0, darkness, 0.0f, darkness * 2, layerAlpha);
                vertex(consumer, matrix, pose, x1, y1, 0, darkness, 0.0f, darkness * 2, layerAlpha);
                vertex(consumer, matrix, pose, x2, y2, 0, darkness, 0.0f, darkness * 2, layerAlpha);
            }

            poseStack.popPose();
        }
    }

    private void renderSolidBlackSphere(PoseStack poseStack, MultiBufferSource buffer,
                                        float size, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(
                ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png")));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(this.entityRenderDispatcher.camera.getXRot()));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        float renderSize = size * 1.8f;
        int segments = 64;

        // Fill the entire disc with pure black
        for (int ring = 0; ring < 8; ring++) {
            float innerRadius = renderSize * (ring / 8.0f);
            float outerRadius = renderSize * ((ring + 1) / 8.0f);

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (2 * Math.PI * i / segments);
                float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

                float x1 = Mth.cos(angle1) * innerRadius;
                float y1 = Mth.sin(angle1) * innerRadius;
                float x2 = Mth.cos(angle2) * innerRadius;
                float y2 = Mth.sin(angle2) * innerRadius;
                float x3 = Mth.cos(angle1) * outerRadius;
                float y3 = Mth.sin(angle1) * outerRadius;
                float x4 = Mth.cos(angle2) * outerRadius;
                float y4 = Mth.sin(angle2) * outerRadius;

                vertex(consumer, matrix, pose, x1, y1, 0, 0.0f, 0.0f, 0.0f, alpha, 0, 0);
                vertex(consumer, matrix, pose, x3, y3, 0, 0.0f, 0.0f, 0.0f, alpha, 0, 1);
                vertex(consumer, matrix, pose, x4, y4, 0, 0.0f, 0.0f, 0.0f, alpha, 1, 1);

                vertex(consumer, matrix, pose, x1, y1, 0, 0.0f, 0.0f, 0.0f, alpha, 0, 0);
                vertex(consumer, matrix, pose, x4, y4, 0, 0.0f, 0.0f, 0.0f, alpha, 1, 1);
                vertex(consumer, matrix, pose, x2, y2, 0, 0.0f, 0.0f, 0.0f, alpha, 1, 0);
            }
        }

        poseStack.popPose();
    }

    private void renderTornEdges(PoseStack poseStack, MultiBufferSource buffer,
                                 float size, int stage, float time, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(this.entityRenderDispatcher.camera.getXRot()));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        int segments = 128;
        float edgeWidth = size * 0.35f;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

            // EXTREME jagged distortion
            float chaos1 = (float) (Math.sin(angle1 * 17 + time * 0.25f) * 0.3f +
                    Math.cos(angle1 * 11 - time * 0.2f) * 0.25f +
                    Math.sin(angle1 * 23 + time * 0.15f) * 0.2f);
            float chaos2 = (float) (Math.sin(angle2 * 17 + time * 0.25f) * 0.3f +
                    Math.cos(angle2 * 11 - time * 0.2f) * 0.25f +
                    Math.sin(angle2 * 23 + time * 0.15f) * 0.2f);

            float innerR1 = size * (1.0f + chaos1);
            float outerR1 = size * (1.0f + chaos1) + edgeWidth * (1.0f + Math.abs(chaos1));
            float innerR2 = size * (1.0f + chaos2);
            float outerR2 = size * (1.0f + chaos2) + edgeWidth * (1.0f + Math.abs(chaos2));

            float x1 = Mth.cos(angle1) * innerR1;
            float y1 = Mth.sin(angle1) * innerR1;
            float x2 = Mth.cos(angle1) * outerR1;
            float y2 = Mth.sin(angle1) * outerR1;
            float x3 = Mth.cos(angle2) * innerR2;
            float y3 = Mth.sin(angle2) * innerR2;
            float x4 = Mth.cos(angle2) * outerR2;
            float y4 = Mth.sin(angle2) * outerR2;

            // INTENSE bright white energy
            float pulse = (float) (Math.sin(time * 0.5f + angle1 * 7) * 0.5f + 0.5f);
            float r = 1.0f;
            float g = 1.0f;
            float b = 1.0f;

            vertex(consumer, matrix, pose, x1, y1, 0, r, g, b, alpha * pulse);
            vertex(consumer, matrix, pose, x2, y2, 0, r * 0.4f, g * 0.3f, b * 0.6f, alpha * pulse * 0.4f);
            vertex(consumer, matrix, pose, x4, y4, 0, r * 0.4f, g * 0.3f, b * 0.6f, alpha * pulse * 0.4f);

            vertex(consumer, matrix, pose, x1, y1, 0, r, g, b, alpha * pulse);
            vertex(consumer, matrix, pose, x4, y4, 0, r * 0.4f, g * 0.3f, b * 0.6f, alpha * pulse * 0.4f);
            vertex(consumer, matrix, pose, x3, y3, 0, r, g, b, alpha * pulse);
        }

        poseStack.popPose();
    }

    private void renderRealityCracks(PoseStack poseStack, MultiBufferSource buffer,
                                     float size, int stage, float time, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        int crackCount = 32 + (stage * 24);

        for (int i = 0; i < crackCount; i++) {
            // 3D cracks in all directions
            float theta = (float) (2 * Math.PI * i / crackCount);
            float phi = (float) (Math.PI * (i % 7) / 7.0 - Math.PI / 2);

            float startDist = size * 1.1f;
            float endDist = size * (3.0f + (i % 5) * 0.5f);

            float startX = Mth.cos(theta) * Mth.cos(phi) * startDist;
            float startY = Mth.sin(phi) * startDist;
            float startZ = Mth.sin(theta) * Mth.cos(phi) * startDist;

            // Jagged crack path
            float prevX = startX, prevY = startY, prevZ = startZ;
            int segments = 5 + stage * 2;

            for (int seg = 1; seg <= segments; seg++) {
                float t = (float) seg / segments;
                float jitter = (float) (Math.sin(time * 0.15f + i * 13 + seg * 7) * 0.4f);

                float dist = Mth.lerp(t, startDist, endDist);
                float x = Mth.cos(theta + jitter) * Mth.cos(phi + jitter * 0.5f) * dist;
                float y = Mth.sin(phi + jitter * 0.5f) * dist;
                float z = Mth.sin(theta + jitter) * Mth.cos(phi + jitter * 0.5f) * dist;

                float brightness = 1.0f - t * 0.6f;
                float crackAlpha = alpha * brightness * (float) Math.sin(time * 0.25f + i * 0.3f);

                if (crackAlpha > 0.05f) {
                    vertex(consumer, matrix, pose, prevX, prevY, prevZ, 1.0f * brightness, 0.95f * brightness, 1.0f, crackAlpha);
                    vertex(consumer, matrix, pose, x, y, z, 0.6f * brightness, 0.5f * brightness, 0.9f, crackAlpha * 0.6f);
                }

                prevX = x; prevY = y; prevZ = z;
            }
        }
    }

    private void renderEnergyVortex(PoseStack poseStack, MultiBufferSource buffer,
                                    float size, int stage, float time, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        int spiralCount = 8;
        int pointsPerSpiral = 40;

        for (int spiral = 0; spiral < spiralCount; spiral++) {
            float spiralOffset = (float) (2 * Math.PI * spiral / spiralCount);

            for (int point = 0; point < pointsPerSpiral; point++) {
                float t = (float) point / pointsPerSpiral;
                float angle = t * (float) Math.PI * 8 + spiralOffset + time * 0.2f;
                float dist = size * (3.5f - t * 2.5f);

                float x = Mth.cos(angle) * dist;
                float y = (t - 0.5f) * size * 3;
                float z = Mth.sin(angle) * dist;

                float brightness = 1.0f - t * 0.5f;
                float vortexAlpha = alpha * 0.6f * brightness;

                if (point > 0 && vortexAlpha > 0.05f) {
                    float prevT = (float) (point - 1) / pointsPerSpiral;
                    float prevAngle = prevT * (float) Math.PI * 8 + spiralOffset + time * 0.2f;
                    float prevDist = size * (3.5f - prevT * 2.5f);

                    float prevX = Mth.cos(prevAngle) * prevDist;
                    float prevY = (prevT - 0.5f) * size * 3;
                    float prevZ = Mth.sin(prevAngle) * prevDist;

                    vertex(consumer, matrix, pose, prevX, prevY, prevZ, 0.8f, 0.7f, 1.0f, vortexAlpha);
                    vertex(consumer, matrix, pose, x, y, z, 0.6f, 0.5f, 0.9f, vortexAlpha * 0.8f);
                }
            }
        }
    }

    private void renderDestructionAura(PoseStack poseStack, MultiBufferSource buffer,
                                       float size, float time, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        // Multiple expanding shockwave rings
        for (int ring = 0; ring < 12; ring++) {
            float ringPhase = (time * 0.1f + ring * 0.3f) % (float) (Math.PI * 2);
            float ringSize = size * (2.5f + (float) Math.sin(ringPhase) * 1.5f);

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(ring * 30 + time * 2));
            poseStack.mulPose(Axis.XP.rotationDegrees(ring * 20));

            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();

            int segments = 48;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (2 * Math.PI * i / segments);
                float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

                float x1 = Mth.cos(angle1) * ringSize;
                float y1 = Mth.sin(angle1) * ringSize;
                float x2 = Mth.cos(angle2) * ringSize;
                float y2 = Mth.sin(angle2) * ringSize;

                float ringAlpha = alpha * 0.4f * (float) Math.sin(ringPhase);

                if (ringAlpha > 0.05f) {
                    vertex(consumer, matrix, pose, x1, y1, 0, 0.7f, 0.3f, 0.9f, ringAlpha);
                    vertex(consumer, matrix, pose, x2, y2, 0, 0.7f, 0.3f, 0.9f, ringAlpha);
                }
            }

            poseStack.popPose();
        }
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose,
                        float x, float y, float z, float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15)
                .setNormal(pose, 0, 1, 0);
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose,
                        float x, float y, float z, float r, float g, float b, float a,
                        float u, float v) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15)
                .setNormal(pose, 0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(SpaceCollapseEntityLegacy entity) {
        return ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png");
    }
}
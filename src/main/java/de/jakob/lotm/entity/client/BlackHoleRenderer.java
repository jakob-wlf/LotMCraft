package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.BlackHoleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Black hole renderer: renders a black sphere + orange accretion disk.
 * Compatible with NeoForge 1.21.1.
 */
public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    private static final ResourceLocation BLACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,"textures/entity/black_hole/black.png");
    private static final ResourceLocation DISK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,"textures/entity/black_hole/black_hole_disk.png");

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float radius = entity.getRadius();
        float rotation = (entity.tickCount + partialTicks) * 7.5f;

        // --- RENDER BLACK HOLE SPHERE ---
        poseStack.pushPose();
        {
            poseStack.scale(radius * 0.55f, radius * 0.55f, radius * 0.55f);
            Matrix4f matrix = poseStack.last().pose();

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(BLACK_TEXTURE));

            renderSphere(matrix, buffer); // Your existing sphere-drawing method
        }
        poseStack.popPose();

        // --- RENDER ACCRETION DISK ---
        poseStack.pushPose();
        {
            // Position slightly above center to avoid z-fighting
            poseStack.translate(0.0, 0.01, 0.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            // Keep it horizontal
            poseStack.scale(radius * 1.8f, 1.0f, radius * 1.8f);

            Matrix4f matrix = poseStack.last().pose();
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(
                    DISK_TEXTURE
            ));

            int light = 0xF000F0; // full brightness
            float halfSize = 1.0f;

            buffer.addVertex(matrix, -halfSize, 0, -halfSize)
                    .setColor(255, 255, 255, 220)
                    .setUv(0f, 0f)
                    .setLight(light)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(0, 1, 0);

            buffer.addVertex(matrix, -halfSize, 0, halfSize)
                    .setColor(255, 255, 255, 220)
                    .setUv(0f, 1f)
                    .setLight(light)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(0, 1, 0);

            buffer.addVertex(matrix, halfSize, 0, halfSize)
                    .setColor(255, 255, 255, 220)
                    .setUv(1f, 1f)
                    .setLight(light)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(0, 1, 0);

            buffer.addVertex(matrix, halfSize, 0, -halfSize)
                    .setColor(255, 255, 255, 220)
                    .setUv(1f, 0f)
                    .setLight(light)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(0, 1, 0);
        }
        poseStack.popPose();

        poseStack.popPose();
    }


    private void renderSphere(Matrix4f matrix, VertexConsumer buffer) {
        int rings = 12;
        int segments = 24;

        for (int i = 0; i < rings; i++) {
            float theta1 = (float) (Math.PI * i / rings);
            float theta2 = (float) (Math.PI * (i + 1) / rings);

            for (int j = 0; j < segments; j++) {
                float phi1 = (float) (2 * Math.PI * j / segments);
                float phi2 = (float) (2 * Math.PI * (j + 1) / segments);

                Vector3f v1 = spherical(theta1, phi1);
                Vector3f v2 = spherical(theta2, phi1);
                Vector3f v3 = spherical(theta2, phi2);
                Vector3f v4 = spherical(theta1, phi2);

                putQuad(buffer, matrix, v1, v2, v3, v4);
            }
        }
    }

    private Vector3f spherical(float theta, float phi) {
        float x = (float) (Math.sin(theta) * Math.cos(phi));
        float y = (float) Math.cos(theta);
        float z = (float) (Math.sin(theta) * Math.sin(phi));
        return new Vector3f(x, y, z);
    }

    private void putQuad(VertexConsumer buffer, Matrix4f matrix, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4) {
        // Add 4 vertices — each must have color, UV, light, overlay, and normal
        buffer.addVertex(matrix, v1.x, v1.y, v1.z)
                .setColor(255, 255, 255, 255)
                .setUv(0f, 0f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, v2.x, v2.y, v2.z)
                .setColor(255, 255, 255, 255)
                .setUv(0f, 1f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, v3.x, v3.y, v3.z)
                .setColor(255, 255, 255, 255)
                .setUv(1f, 1f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, v4.x, v4.y, v4.z)
                .setColor(255, 255, 255, 255)
                .setUv(1f, 0f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);
    }


    private void renderDisk(Matrix4f matrix, VertexConsumer buffer) {
        int segments = 64;
        float innerRadius = 0.4f;
        float outerRadius = 1.0f;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

            Vector3f v1 = new Vector3f(Mth.cos(angle1) * innerRadius, 0, Mth.sin(angle1) * innerRadius);
            Vector3f v2 = new Vector3f(Mth.cos(angle2) * innerRadius, 0, Mth.sin(angle2) * innerRadius);
            Vector3f v3 = new Vector3f(Mth.cos(angle2) * outerRadius, 0, Mth.sin(angle2) * outerRadius);
            Vector3f v4 = new Vector3f(Mth.cos(angle1) * outerRadius, 0, Mth.sin(angle1) * outerRadius);

            putQuad(buffer, matrix, v1, v2, v3, v4);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity entity) {
        return BLACK_TEXTURE;
    }
}

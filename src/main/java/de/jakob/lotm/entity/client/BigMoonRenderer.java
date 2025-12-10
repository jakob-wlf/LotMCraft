package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.BigMoonEntity;
import de.jakob.lotm.entity.custom.BigSunEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BigMoonRenderer extends EntityRenderer<BigMoonEntity> {

    private static final ResourceLocation GOLD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,"textures/entity/moon/crimson.png");

    public BigMoonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BigMoonEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float radius = 12f;

        poseStack.pushPose();
        poseStack.scale(radius, radius, radius);
        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(GOLD_TEXTURE));

        renderSphere(matrix, buffer);
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    protected int getBlockLightLevel(BigMoonEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BigMoonEntity sunEntity) {
        return GOLD_TEXTURE;
    }

    private void renderSphere(Matrix4f matrix, VertexConsumer buffer) {
        int rings = 24;
        int segments = 48;

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
}

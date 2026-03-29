package de.jakob.lotm.entity.client.ability_entities.door_pathway.black_hole;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.BlackHoleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    private static final ResourceLocation BLACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/black_hole/black.png");
    private static final ResourceLocation DISK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/black_hole/black_hole_disk.png");

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        boolean petrified = entity.getTags().contains("petrified");

        poseStack.pushPose();

        float radius = entity.getRadius();
        float rotation = (entity.tickCount + partialTicks) * 7.5f;

        // --- RENDER BLACK HOLE SPHERE ---
        poseStack.pushPose();
        {
            poseStack.scale(radius * 0.55f, radius * 0.55f, radius * 0.55f);
            Matrix4f matrix = poseStack.last().pose();

            RenderType renderType = petrified ? RenderType.entityCutoutNoCull(LOTMCraft.STONE_TEXTURE) :
                    RenderType.entityCutoutNoCull(BLACK_TEXTURE);

            VertexConsumer buffer = bufferSource.getBuffer(renderType);
            renderSphere(matrix, buffer, petrified);
        }
        poseStack.popPose();

        // --- RENDER ACCRETION DISK ---
        poseStack.pushPose();
        if(!petrified) {
            poseStack.translate(0.0, 0.01, 0.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.scale(radius * 1.8f, 1.0f, radius * 1.8f);

            Matrix4f matrix = poseStack.last().pose();

            RenderType renderType = RenderType.entityTranslucent(DISK_TEXTURE);
            VertexConsumer buffer = bufferSource.getBuffer(renderType);

            int light = 0xF000F0;
            float halfSize = 1.0f;

            // When petrified, tint disk gray
            int r = petrified ? 128 : 255;
            int g = petrified ? 128 : 255;
            int b = petrified ? 128 : 255;

            buffer.addVertex(matrix, -halfSize, 0, -halfSize)
                    .setColor(r, g, b, 220)
                    .setUv(0f, 0f)
                    .setLight(light)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(0, 1, 0);

            buffer.addVertex(matrix, -halfSize, 0, halfSize)
                    .setColor(r, g, b, 220)
                    .setUv(0f, 1f)
                    .setLight(light)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(0, 1, 0);

            buffer.addVertex(matrix, halfSize, 0, halfSize)
                    .setColor(r, g, b, 220)
                    .setUv(1f, 1f)
                    .setLight(light)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(0, 1, 0);

            buffer.addVertex(matrix, halfSize, 0, -halfSize)
                    .setColor(r, g, b, 220)
                    .setUv(1f, 0f)
                    .setLight(light)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setNormal(0, 1, 0);
        }
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderSphere(Matrix4f matrix, VertexConsumer buffer, boolean petrified) {
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

                putQuad(buffer, matrix, v1, v2, v3, v4, petrified);
            }
        }
    }

    private Vector3f spherical(float theta, float phi) {
        float x = (float) (Math.sin(theta) * Math.cos(phi));
        float y = (float) Math.cos(theta);
        float z = (float) (Math.sin(theta) * Math.sin(phi));
        return new Vector3f(x, y, z);
    }

    private void putQuad(VertexConsumer buffer, Matrix4f matrix, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, boolean petrified) {
        int r = petrified ? 128 : 255;
        int g = petrified ? 128 : 255;
        int b = petrified ? 128 : 255;

        buffer.addVertex(matrix, v1.x, v1.y, v1.z)
                .setColor(r, g, b, 255)
                .setUv(0f, 0f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, v2.x, v2.y, v2.z)
                .setColor(r, g, b, 255)
                .setUv(0f, 1f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, v3.x, v3.y, v3.z)
                .setColor(r, g, b, 255)
                .setUv(1f, 1f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, v4.x, v4.y, v4.z)
                .setColor(r, g, b, 255)
                .setUv(1f, 0f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity entity) {
        return BLACK_TEXTURE;
    }
}
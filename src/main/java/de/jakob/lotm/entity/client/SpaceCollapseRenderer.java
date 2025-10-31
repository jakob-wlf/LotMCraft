package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.SpaceCollapseEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SpaceCollapseRenderer extends EntityRenderer<SpaceCollapseEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/space_collapse/space_collapse.png");
    
    public SpaceCollapseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SpaceCollapseEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float radius = entity.getRadius();
        float age = entity.getAge() + partialTick;
        int maxAge = 100;
        float progress = Math.min(1.0F, age / maxAge);

        // Rotate to face camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // Draw multiple layers for depth effect
        for (int layer = 0; layer < 5; layer++) {
            float layerScale = 1.0F + (layer * 0.15F);
            float layerAlpha = (1.0F - (layer * 0.15F)) * (1.0F - progress * 0.3F);

            poseStack.pushPose();
            poseStack.scale(radius * layerScale, radius * layerScale, radius * layerScale);

            // Rotate each layer slightly for animation
            float rotation = (age + layer * 20) * 2.0F;
            poseStack.mulPose(new org.joml.Quaternionf().rotationZ((float) Math.toRadians(rotation)));

            // Draw black crack using simple render type
            drawSimpleCrack(poseStack, buffer, layerAlpha);
            drawCrack(poseStack, buffer, layerAlpha, 0.1F, 0.1F, 0.1F); // Dark gray crack

            // Draw blue edges
            if (layer == 0) {
                drawEdges(poseStack, buffer, layerAlpha * 0.7F, progress);
            }

            poseStack.popPose();
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void drawSimpleCrack(PoseStack poseStack, MultiBufferSource buffer, float alpha) {
        // Use a more reliable render type
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        // Simple quad facing camera
        float r = 0.0F, g = 0.0F, b = 0.0F; // Black

        // Draw main quad
        addVertex(consumer, matrix, -1, -1, 0, 0, 1, r, g, b, alpha);
        addVertex(consumer, matrix, -1, 1, 0, 0, 0, r, g, b, alpha);
        addVertex(consumer, matrix, 1, 1, 0, 1, 0, r, g, b, alpha);
        addVertex(consumer, matrix, 1, -1, 0, 1, 1, r, g, b, alpha);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(0, 1, 0);
    }

    private void drawCrack(PoseStack poseStack, MultiBufferSource buffer, float alpha, float r, float g, float b) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.energySwirl(TEXTURE, 0, 0));
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Draw central quad
        drawVertex(consumer, matrix, normal, -1, -1, 0, 0, 1, r, g, b, alpha);
        drawVertex(consumer, matrix, normal, -1, 1, 0, 0, 0, r, g, b, alpha);
        drawVertex(consumer, matrix, normal, 1, 1, 0, 1, 0, r, g, b, alpha);
        drawVertex(consumer, matrix, normal, 1, -1, 0, 1, 1, r, g, b, alpha);

        // Draw jagged crack lines
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            // Create jagged effect
            float jag1 = 0.3F + (i % 2) * 0.2F;
            float jag2 = 0.6F + ((i + 1) % 2) * 0.2F;

            drawVertex(consumer, matrix, normal, 0, 0, 0, 0.5F, 0.5F, r, g, b, alpha);
            drawVertex(consumer, matrix, normal, cos * jag1, sin * jag1, 0, 0.5F, 0.5F, r, g, b, alpha * 0.7F);
            drawVertex(consumer, matrix, normal, cos * jag2, sin * jag2, 0, 0.5F, 0.5F, r, g, b, alpha * 0.5F);
            drawVertex(consumer, matrix, normal, cos, sin, 0, 0.5F, 0.5F, r, g, b, alpha * 0.3F);
        }
    }

    private void drawEdges(PoseStack poseStack, MultiBufferSource buffer, float alpha, float progress) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Blue glow color
        float r = 0.2F;
        float g = 0.4F;
        float b = 1.0F;

        // Draw glowing edge ring
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            double angle1 = (i / (double) segments) * Math.PI * 2;
            double angle2 = ((i + 1) / (double) segments) * Math.PI * 2;

            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);
            float cos2 = (float) Math.cos(angle2);
            float sin2 = (float) Math.sin(angle2);

            float innerRadius = 0.9F;
            float outerRadius = 1.0F + progress * 0.3F;

            drawVertex(consumer, matrix, normal, cos1 * innerRadius, sin1 * innerRadius, 0, 0, 0, r, g, b, alpha * 0.5F);
            drawVertex(consumer, matrix, normal, cos1 * outerRadius, sin1 * outerRadius, 0, 1, 0, r, g, b, alpha);
            drawVertex(consumer, matrix, normal, cos2 * outerRadius, sin2 * outerRadius, 0, 1, 1, r, g, b, alpha);
            drawVertex(consumer, matrix, normal, cos2 * innerRadius, sin2 * innerRadius, 0, 0, 1, r, g, b, alpha * 0.5F);
        }
    }

    private void drawVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                            float x, float y, float z, float u, float v,
                            float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(SpaceCollapseEntity entity) {
        return TEXTURE;
    }
}
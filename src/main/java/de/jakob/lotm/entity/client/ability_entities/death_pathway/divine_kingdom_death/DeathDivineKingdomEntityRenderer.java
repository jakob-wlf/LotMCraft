package de.jakob.lotm.entity.client.ability_entities.death_pathway.divine_kingdom_death;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.DeathDivineKingdomEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class DeathDivineKingdomEntityRenderer extends EntityRenderer<DeathDivineKingdomEntity> {

    private static final ResourceLocation BLACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/black_hole/black.png");

    public DeathDivineKingdomEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DeathDivineKingdomEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        float radius = DeathDivineKingdomEntity.RADIUS;

        poseStack.pushPose();

        // Scale the unit sphere up to domain radius, centered on entity origin
        poseStack.scale(radius, radius, radius);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(BLACK_TEXTURE));
        renderSphere(poseStack, consumer, 1.0f, 1.0f, 1.0f, 0.6f);

        poseStack.popPose();
    }

    /** Tessellated unit sphere (radius 1). Scale via poseStack before calling. */
    private void renderSphere(PoseStack poseStack, VertexConsumer consumer,
                               float r, float g, float b, float alpha) {
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

                consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(x1, y1, z1);
                consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(x2, y2, z2);
                consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(x3, y3, z3);
                consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(x4, y4, z4);
            }
        }
    }

    @Override
    public boolean shouldRender(DeathDivineKingdomEntity entity,
                                net.minecraft.client.renderer.culling.Frustum frustum,
                                double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(DeathDivineKingdomEntity entity) {
        return BLACK_TEXTURE;
    }
}

package de.jakob.lotm.entity.client.beyonder_npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class TradeIndicatorLayer extends RenderLayer<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> {
    private static TradeIndicatorModel<Entity> model;
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/npc/trade_indicator.png");

    public TradeIndicatorLayer(RenderLayerParent<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       BeyonderNPCEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.hasTrades()) {
            return;
        }

        if (model == null) {
            model = new TradeIndicatorModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TradeIndicatorModel.LAYER_LOCATION)
            );
        }

        poseStack.pushPose();
        poseStack.scale(.7f, .7f, .7f);
        poseStack.translate(0, -2.65, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks));

        // Get the vertex consumer with your texture
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));



        model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
    }

}

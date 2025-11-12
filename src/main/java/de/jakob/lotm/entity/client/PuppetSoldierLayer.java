package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class PuppetSoldierLayer extends RenderLayer<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> {
    private static final ResourceLocation CREEPER_ARMOR = ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
    private final PlayerModel<BeyonderNPCEntity> model;

    public PuppetSoldierLayer(RenderLayerParent<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new PlayerModel<>(modelSet.bakeLayer(ModelLayers.PLAYER), false);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, BeyonderNPCEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isInvisible()) {
            // Copy the parent model's animation state
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
            this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            // Get the vertex consumer for the outline rendering
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.energySwirl(CREEPER_ARMOR, 0.0F, partialTick * 0.01F));

            // Render the model slightly larger with red color and transparency
            poseStack.pushPose();
            poseStack.scale(1.025F, 1.025F, 1.025F);
            // ARGB format: Alpha=0x80 (50% transparent), Red=0xFF, Green=0x00, Blue=0x00
            int color = 0x80FF0000; // Semi-transparent red
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), color);
            poseStack.popPose();
        }
    }
}
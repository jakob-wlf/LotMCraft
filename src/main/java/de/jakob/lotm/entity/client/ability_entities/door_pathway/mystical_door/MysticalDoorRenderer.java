package de.jakob.lotm.entity.client.ability_entities.door_pathway.mystical_door;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.return_portal.HighSequenceDoorsModel;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.MysticalDoorEntity;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.ReturnPortalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class MysticalDoorRenderer extends EntityRenderer<MysticalDoorEntity> {
    private final HighSequenceDoorsModel<MysticalDoorEntity> model;

    public MysticalDoorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new HighSequenceDoorsModel<>(context.bakeLayer(HighSequenceDoorsModel.LAYER_LOCATION));
    }

    @Override
    public void render(MysticalDoorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(entity.getSize(), -entity.getSize(), entity.getSize());
        poseStack.translate(0, -1.45, 0);

        // Render the model
        var vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(MysticalDoorEntity projectileEntity, BlockPos blockpos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(MysticalDoorEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(MysticalDoorEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/doors/mystical_door_" + entity.getTextureIndex() + ".png");

    }
}
package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ReturnPortalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class ReturnPortalRenderer extends EntityRenderer<ReturnPortalEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/doors/mystical_door_3.png");
    private final HighSequenceDoorsModel<ReturnPortalEntity> model;

    public ReturnPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new HighSequenceDoorsModel<>(context.bakeLayer(HighSequenceDoorsModel.LAYER_LOCATION));
    }

    @Override
    public void render(ReturnPortalEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1, -1, 1);
        poseStack.translate(0, -1.45, 0);

        // Render the model
        var vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(ReturnPortalEntity projectileEntity, BlockPos blockpos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(ReturnPortalEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(ReturnPortalEntity entity) {
        return TEXTURE;
    }
}
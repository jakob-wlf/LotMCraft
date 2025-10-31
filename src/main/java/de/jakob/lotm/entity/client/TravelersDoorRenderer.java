package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.TravelersDoorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TravelersDoorRenderer extends EntityRenderer<TravelersDoorEntity> {

    private final TravelersDoorModel<TravelersDoorEntity> model;
    private static final float SCALE_FACTOR = 2.5f;

    public TravelersDoorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TravelersDoorModel<>(context.bakeLayer(TravelersDoorModel.LAYER_LOCATION));
    }

    @Override
    public void render(TravelersDoorEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        // Position entity correctly
        poseStack.translate(0.0D, 1.5D, 0.0D);

        // Apply entity yaw and pitch
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot())); // horizontal rotation
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));  // vertical tilt (if needed)

        // Flip model upright
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));

        // Scale up
        poseStack.scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);

        // Render model
        RenderType renderType = RenderType.entityTranslucent(this.getTextureLocation(entity));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        int color = 0xFFFFFFFF;

        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }


    @Override
    protected int getBlockLightLevel(TravelersDoorEntity entity, BlockPos blockPos) {
        return 15; // Always fully lit for ethereal effect
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TravelersDoorEntity entity) {
        int frame = (entity.tickCount / 3) % 7;
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/travelers_door/travelers_door_" + frame + ".png");
    }
}
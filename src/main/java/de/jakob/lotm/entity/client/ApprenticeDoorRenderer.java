package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ApprenticeDoorEntity;
import de.jakob.lotm.entity.custom.WindBladeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ApprenticeDoorRenderer extends EntityRenderer<ApprenticeDoorEntity> {

    private final ApprenticeDoorModel<ApprenticeDoorEntity> model;

    public ApprenticeDoorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ApprenticeDoorModel<>(context.bakeLayer(ApprenticeDoorModel.LAYER_LOCATION));
    }

    @Override
    public void render(ApprenticeDoorEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        // First, flip the model right-side up (it's currently upside down)
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));

        // Position the model correctly - after flipping, adjust positioning
        poseStack.translate(0, -1.5, 0); // Negative because we flipped it

        // Now handle the facing direction - try different mapping
        Direction facing = entity.getFacing();
        float yRotation = 0;

        // Since the entity's hitbox is facing correctly, use the entity's actual rotation
        // But we need to add 90 degrees because the model's default orientation is rotated
        yRotation = -entity.getYRot() + 90.0f; // Add 90 degree offset

        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));

        // Get the render type - using translucent for ethereal effect
        RenderType renderType = RenderType.entityTranslucent(this.getTextureLocation(entity));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        // Add some transparency and glow effect
        int color = 0x96FFFFFF;

        // Render the model
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(ApprenticeDoorEntity entity, BlockPos blockPos) {
        return 15; // Always fully lit for ethereal effect
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ApprenticeDoorEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/apprentice_door/apprentice_door.png");
    }
}
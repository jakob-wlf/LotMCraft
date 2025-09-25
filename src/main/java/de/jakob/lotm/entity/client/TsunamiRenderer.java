package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.TsunamiEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class TsunamiRenderer extends EntityRenderer<TsunamiEntity> {

    public static final float scale = 3.75F;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/tsunami/tsunami.png");
    private final TsunamiModel<TsunamiEntity> model;

    public TsunamiRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TsunamiModel<>(context.bakeLayer(TsunamiModel.LAYER_LOCATION));
    }

    @Override
    public void render(TsunamiEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Scale the model up (adjust this value to get the desired size)
        poseStack.scale(scale, scale, scale);

        // Flip the model right-side up
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        // Calculate horizontal rotation based on movement direction
        Vec3 direction = entity.getDirectionFacing();
        if (direction.lengthSqr() > 0) {
            // Adjust the rotation calculation to fix the facing direction
            // Adding 90 degrees to correct the east->north orientation
            float yaw = (float) Math.atan2(-direction.x, direction.z) + (float) (Math.PI / 2);
            poseStack.mulPose(Axis.YP.rotation(yaw));
        }

        // Move the model down after flipping to keep it at ground level
        poseStack.translate(0, -3.0F * scale, 0); // Adjust this value based on your model's pivot point

        // Render the model
        var vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(TsunamiEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(TsunamiEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(TsunamiEntity entity) {
        return TEXTURE;
    }
}
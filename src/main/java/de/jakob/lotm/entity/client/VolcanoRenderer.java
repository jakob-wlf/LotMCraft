package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.JusticeSwordEntity;
import de.jakob.lotm.entity.custom.VolcanoEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class VolcanoRenderer extends EntityRenderer<VolcanoEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/volcano/volcano.png");
    private final VolcanoModel<VolcanoEntity> model;

    public VolcanoRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new VolcanoModel<>(context.bakeLayer(VolcanoModel.LAYER_LOCATION));
    }

    @Override
    public void render(VolcanoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        Random random = new Random(entity.getId());

        poseStack.scale(12, -12, 12);
        poseStack.translate(0, -1, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(random.nextInt(360)));

        // Render the model
        var vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(VolcanoEntity projectileEntity, BlockPos blockpos) {
        return 15;
    }


    @Override
    public ResourceLocation getTextureLocation(VolcanoEntity entity) {
        return TEXTURE;
    }
}
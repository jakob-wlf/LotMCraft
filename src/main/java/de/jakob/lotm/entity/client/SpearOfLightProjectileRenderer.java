package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.SpearOfLightProjectileEntity;
import de.jakob.lotm.entity.custom.UnshadowedSpearProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class SpearOfLightProjectileRenderer extends EntityRenderer<SpearOfLightProjectileEntity> {

    private SpearOfLightProjectileModel model;

    public SpearOfLightProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SpearOfLightProjectileModel(context.bakeLayer(SpearOfLightProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(SpearOfLightProjectileEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(2.75f, 2.75f, 2.75f);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));
        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(
                buffer, this.model.renderType(this.getTextureLocation(entity)), false, false
        );
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    protected int getBlockLightLevel(SpearOfLightProjectileEntity projectileEntity, BlockPos blockpos) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SpearOfLightProjectileEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spear_of_light/spear_of_light.png");
    }


}

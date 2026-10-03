package de.jakob.lotm.entity.client.projectiles.frost_spear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.projectiles.FrostSpearProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class FrostSpearProjectileRenderer extends EntityRenderer<FrostSpearProjectileEntity> {

    private FrostSpearProjectileModel model;

    public FrostSpearProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FrostSpearProjectileModel(context.bakeLayer(FrostSpearProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(FrostSpearProjectileEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        boolean petrified = entity.getTags().contains("petrified");

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));

        RenderType renderType = petrified ? this.model.renderType(LOTMCraft.STONE_TEXTURE) :
                this.model.renderType(this.getTextureLocation(entity));

        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(
                buffer, renderType, false, false
        );
        int color = petrified ? 0xFF808080 : 0xFFFFFFFF;
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    protected int getBlockLightLevel(FrostSpearProjectileEntity projectileEntity, BlockPos blockpos) { return 15; }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FrostSpearProjectileEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/frost_spear/frost_spear.png");
    }
}
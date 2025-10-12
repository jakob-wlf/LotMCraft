package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.TsunamiEntity;
import de.jakob.lotm.entity.custom.WarBannerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class WarBannerRenderer extends EntityRenderer<WarBannerEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/war_banner/war_banner.png");
    private final WarBannerModel<WarBannerEntity> model;

    public WarBannerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WarBannerModel<>(context.bakeLayer(WarBannerModel.LAYER_LOCATION));
    }

    @Override
    public void render(WarBannerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float rotation = (entity.tickCount + partialTicks) * 2.5f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        poseStack.scale(1.5f, -2.75f, 1.5f);
        poseStack.translate(0, -1, 0);

        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(
                buffer, this.model.renderType(this.getTextureLocation(entity)), false, false
        );
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(WarBannerEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(WarBannerEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(WarBannerEntity entity) {
        return TEXTURE;
    }
}
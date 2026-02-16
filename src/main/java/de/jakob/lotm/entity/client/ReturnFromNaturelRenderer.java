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

public class ReturnFromNaturelRenderer extends EntityRenderer<ReturnPortalEntity> {

    public ReturnFromNaturelRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ReturnPortalEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
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
        return null;
    }
}
package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.entity.custom.LocationGraftingEntity;
import de.jakob.lotm.entity.custom.ReturnPortalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class GraftingLocationRenderer extends EntityRenderer<LocationGraftingEntity> {

    public GraftingLocationRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LocationGraftingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    protected int getBlockLightLevel(LocationGraftingEntity projectileEntity, BlockPos blockpos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(LocationGraftingEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(LocationGraftingEntity entity) {
        return null;
    }
}
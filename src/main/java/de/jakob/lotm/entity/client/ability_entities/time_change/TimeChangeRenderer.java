package de.jakob.lotm.entity.client.ability_entities.time_change;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.entity.custom.ability_entities.TimeChangeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class TimeChangeRenderer extends EntityRenderer<TimeChangeEntity> {


    public TimeChangeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TimeChangeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    protected int getBlockLightLevel(TimeChangeEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(TimeChangeEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(TimeChangeEntity entity) {
        return null;
    }
}
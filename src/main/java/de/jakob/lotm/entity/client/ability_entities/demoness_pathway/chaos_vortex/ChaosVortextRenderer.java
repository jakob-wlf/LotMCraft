package de.jakob.lotm.entity.client.ability_entities.demoness_pathway.chaos_vortex;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.entity.custom.ability_entities.PortalEntity;
import de.jakob.lotm.entity.custom.ability_entities.demoness_pathway.ChaosVortexEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class ChaosVortextRenderer extends EntityRenderer<ChaosVortexEntity> {
    public ChaosVortextRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ChaosVortexEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(ChaosVortexEntity portalEntity) {
        return null;
    }

    @Override
    protected int getBlockLightLevel(ChaosVortexEntity entity, BlockPos pos) {
        return 15;
    }

}

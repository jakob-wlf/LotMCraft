package de.jakob.lotm.entity.client.spirits.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.dervish.SpiritDervishModel;
import de.jakob.lotm.entity.custom.spirits.SpiritDervishEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritGhostEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpiritGhostRenderer extends MobRenderer<SpiritGhostEntity, SpiritGhostModel<SpiritGhostEntity>> {
    public SpiritGhostRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritGhostModel<>(context.bakeLayer(SpiritGhostModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritGhostEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/spirit_ghost/spirit_ghost.png");
    }

    @Override
    public void render(SpiritGhostEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, -.2D, 0.0D);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

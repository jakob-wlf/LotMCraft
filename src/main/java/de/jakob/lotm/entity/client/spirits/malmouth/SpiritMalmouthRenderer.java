package de.jakob.lotm.entity.client.spirits.malmouth;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.ghost.SpiritGhostModel;
import de.jakob.lotm.entity.custom.spirits.SpiritGhostEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritMalmouthEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpiritMalmouthRenderer extends MobRenderer<SpiritMalmouthEntity, SpiritMalmouthModel<SpiritMalmouthEntity>> {
    public SpiritMalmouthRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritMalmouthModel<>(context.bakeLayer(SpiritMalmouthModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritMalmouthEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/spirit_malmouth/spirit_malmouth.png");
    }

    @Override
    public void render(SpiritMalmouthEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, -.2D, 0.0D);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

package de.jakob.lotm.entity.client.spirits.spirit_bane;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.bizarro_bane.SpiritBizarroBaneModel;
import de.jakob.lotm.entity.custom.spirits.SpiritBaneEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBizarroBaneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpiritBaneRenderer extends MobRenderer<SpiritBaneEntity, SpiritBaneModel<SpiritBaneEntity>> {
    public SpiritBaneRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritBaneModel<>(context.bakeLayer(SpiritBaneModel.LAYER_LOCATION)), .3f);
    }
    @Override
    public void render(SpiritBaneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(2.5f, 2.5f, 2.5f);
        poseStack.translate(0.0D, -.2D, 0.0D);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritBaneEntity spiritBlueWizardEntity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/spirit_bane/spirit_bane.png");
    }
}

package de.jakob.lotm.entity.client.spirits.bizarro_bane;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.blue_wizard.SpiritBlueWizardModel;
import de.jakob.lotm.entity.custom.spirits.SpiritBizarroBaneEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBlueWizardEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpiritBizarroBaneRenderer extends MobRenderer<SpiritBizarroBaneEntity, SpiritBizarroBaneModel<SpiritBizarroBaneEntity>> {
    public SpiritBizarroBaneRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritBizarroBaneModel<>(context.bakeLayer(SpiritBizarroBaneModel.LAYER_LOCATION)), .3f);
    }
    @Override
    public void render(SpiritBizarroBaneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(2, 2, 2);
        poseStack.translate(0.0D, -.2D, 0.0D);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritBizarroBaneEntity spiritBlueWizardEntity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/spirit_bizarro_bane/spirit_bizarro_bane.png");
    }
}

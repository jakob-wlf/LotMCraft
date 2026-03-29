package de.jakob.lotm.entity.client.spirits.blue_wizard;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.bubbles.SpiritBubblesModel;
import de.jakob.lotm.entity.custom.spirits.SpiritBlueWizardEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBubblesEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpiritBlueWizardRenderer extends MobRenderer<SpiritBlueWizardEntity, SpiritBlueWizardModel<SpiritBlueWizardEntity>> {
    public SpiritBlueWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritBlueWizardModel<>(context.bakeLayer(SpiritBlueWizardModel.LAYER_LOCATION)), .3f);
    }
    @Override
    public void render(SpiritBlueWizardEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, -.2D, 0.0D);
        poseStack.scale(4, 4, 4);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritBlueWizardEntity spiritBlueWizardEntity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/spirit_blue_wizard/spirit_blue_wizard.png");
    }
}

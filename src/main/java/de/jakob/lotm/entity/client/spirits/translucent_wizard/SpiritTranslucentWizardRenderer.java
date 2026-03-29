package de.jakob.lotm.entity.client.spirits.translucent_wizard;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.spirits.SpiritBlueWizardEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritTranslucentWizardEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SpiritTranslucentWizardRenderer extends MobRenderer<SpiritTranslucentWizardEntity, SpiritTranslucentWizardModel<SpiritTranslucentWizardEntity>> {
    public SpiritTranslucentWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritTranslucentWizardModel<>(context.bakeLayer(SpiritTranslucentWizardModel.LAYER_LOCATION)), .3f);
    }
    @Override
    public void render(SpiritTranslucentWizardEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, -.2D, 0.0D);
        poseStack.scale(2.5f, 2.5f, 2.5f);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritTranslucentWizardEntity spiritBlueWizardEntity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/spirit_blue_wizard/spirit_blue_wizard.png");
    }

    @Nullable
    @Override
    protected RenderType getRenderType(SpiritTranslucentWizardEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.crumbling(this.getTextureLocation(livingEntity));
    }
}

package de.jakob.lotm.entity.client.spirits.bubbles;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.dervish.SpiritDervishModel;
import de.jakob.lotm.entity.custom.spirits.SpiritBubblesEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritDervishEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpiritBubblesRenderer extends MobRenderer<SpiritBubblesEntity, SpiritBubblesModel<SpiritBubblesEntity>> {
    public SpiritBubblesRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritBubblesModel<>(context.bakeLayer(SpiritBubblesModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritBubblesEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/spirit_bubbles/spirit_bubbles.png");
    }

    @Override
    public void render(SpiritBubblesEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, -.2D, 0.0D);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

package de.jakob.lotm.entity.client.spirits.grim_reaper;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.abscessed_hand.AbscessedHandModel;
import de.jakob.lotm.entity.custom.spirits.AbscessedHandEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritGrimReaperEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GrimReaperRenderer extends MobRenderer<SpiritGrimReaperEntity, GrimReaperModel<SpiritGrimReaperEntity>> {
    public GrimReaperRenderer(EntityRendererProvider.Context context) {
        super(context, new GrimReaperModel<>(context.bakeLayer(GrimReaperModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritGrimReaperEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/grim_reaper/grim_reaper.png");
    }

    @Override
    public void render(SpiritGrimReaperEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0, 0, 0);

        poseStack.scale(1.5f, 1.5f, 1.5f);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

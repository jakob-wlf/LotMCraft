package de.jakob.lotm.entity.client.spirits.abscessed_hand;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.dervish.SpiritDervishModel;
import de.jakob.lotm.entity.custom.spirits.AbscessedHandEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritDervishEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class AbscessedHandRenderer extends MobRenderer<AbscessedHandEntity, AbscessedHandModel<AbscessedHandEntity>> {
    public AbscessedHandRenderer(EntityRendererProvider.Context context) {
        super(context, new AbscessedHandModel<>(context.bakeLayer(AbscessedHandModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(AbscessedHandEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/abscessed_hand/abscessed_hand.png");
    }

    @Override
    public void render(AbscessedHandEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0, 0, 0);

        poseStack.scale(1.5f, 1.5f, 1.5f);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

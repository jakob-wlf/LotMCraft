package de.jakob.lotm.entity.client.stone_golem;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.abscessed_hand.AbscessedHandModel;
import de.jakob.lotm.entity.custom.StoneGolemEntity;
import de.jakob.lotm.entity.custom.spirits.AbscessedHandEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StoneGolemRenderer extends MobRenderer<StoneGolemEntity, StoneGolemModel<StoneGolemEntity>> {
    public StoneGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new StoneGolemModel<>(context.bakeLayer(StoneGolemModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(StoneGolemEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/stone_golem/stone_golem.png");
    }

    @Override
    public void render(StoneGolemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1.25f, 1.25f, 1.25f);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

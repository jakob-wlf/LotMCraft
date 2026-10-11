package de.jakob.lotm.entity.client.spirits.headless_bride;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.stone_golem.StoneGolemModel;
import de.jakob.lotm.entity.custom.StoneGolemEntity;
import de.jakob.lotm.entity.custom.spirits.HeadlessBrideEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HeadlessBrideRenderer extends MobRenderer<HeadlessBrideEntity, HeadlessBrideModel<HeadlessBrideEntity>> {
    public HeadlessBrideRenderer(EntityRendererProvider.Context context) {
        super(context, new HeadlessBrideModel<>(context.bakeLayer(HeadlessBrideModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(HeadlessBrideEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/headless_bride/headless_bride.png");
    }

    @Override
    public void render(HeadlessBrideEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1.25f, 1.25f, 1.25f);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

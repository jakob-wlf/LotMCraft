package de.jakob.lotm.entity.client.murloc;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.abscessed_hand.AbscessedHandModel;
import de.jakob.lotm.entity.custom.MurlocEntity;
import de.jakob.lotm.entity.custom.spirits.AbscessedHandEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MurlocRenderer extends MobRenderer<MurlocEntity, MurlocModel<MurlocEntity>> {
    public MurlocRenderer(EntityRendererProvider.Context context) {
        super(context, new MurlocModel<>(context.bakeLayer(MurlocModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(MurlocEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/murloc/murloc.png");
    }

    @Override
    public void render(MurlocEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0, 0, 0);

        poseStack.scale(1, 1, 1);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

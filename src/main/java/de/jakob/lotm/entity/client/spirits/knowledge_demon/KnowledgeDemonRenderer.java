package de.jakob.lotm.entity.client.spirits.knowledge_demon;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.asmann.AsmannModel;
import de.jakob.lotm.entity.custom.spirits.AsmannEntity;
import de.jakob.lotm.entity.custom.spirits.KnowledgeDemonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KnowledgeDemonRenderer extends MobRenderer<KnowledgeDemonEntity, KnowledgeDemonModel<KnowledgeDemonEntity>> {
    public KnowledgeDemonRenderer(EntityRendererProvider.Context context) {
        super(context, new KnowledgeDemonModel<>(context.bakeLayer(KnowledgeDemonModel.LAYER_LOCATION)), .3f);
    }
    @Override
    public void render(KnowledgeDemonEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1, 1, 1);
        poseStack.translate(0, -.5, 0);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(KnowledgeDemonEntity spirit) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/knowledge_demon/knowledge_demon.png");
    }
}

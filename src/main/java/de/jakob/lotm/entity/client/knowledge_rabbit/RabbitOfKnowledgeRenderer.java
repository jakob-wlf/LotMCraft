package de.jakob.lotm.entity.client.knowledge_rabbit;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.murloc.MurlocModel;
import de.jakob.lotm.entity.custom.MurlocEntity;
import de.jakob.lotm.entity.custom.spirits.RabbitOfKnowledgeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RabbitOfKnowledgeRenderer extends MobRenderer<RabbitOfKnowledgeEntity, RabbitOfKnowledgeModel<RabbitOfKnowledgeEntity>> {
    public RabbitOfKnowledgeRenderer(EntityRendererProvider.Context context) {
        super(context, new RabbitOfKnowledgeModel<>(context.bakeLayer(RabbitOfKnowledgeModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(RabbitOfKnowledgeEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/knowledge_rabbit/knowledge_rabbit.png");
    }

    @Override
    public void render(RabbitOfKnowledgeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0, 0, 0);

        poseStack.scale(1, 1, 1);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}

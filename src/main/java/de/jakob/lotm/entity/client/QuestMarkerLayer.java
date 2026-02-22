package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class QuestMarkerLayer extends RenderLayer<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> {
    private static QuestMarkerModel<Entity> model;
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/npc/quest_marker.png");

    public QuestMarkerLayer(RenderLayerParent<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       BeyonderNPCEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.getQuestId().isEmpty()) {
            return;
        }

        if (model == null) {
            model = new QuestMarkerModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(QuestMarkerModel.LAYER_LOCATION)
            );
        }

        poseStack.pushPose();
        poseStack.scale(2, 2, 2);
        poseStack.translate(0, -2, 0);

        // Get the vertex consumer with your texture
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));



        model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
    }

}

package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class QuestMarkerLayer extends RenderLayer<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> {
    private static final ResourceLocation QUEST_MARKER = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/npc/quest_marker.png");

    public QuestMarkerLayer(RenderLayerParent<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       BeyonderNPCEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.hasQuest()) {
            return;
        }

        poseStack.pushPose();

        // Scale the marker (make it bigger for testing)
        poseStack.scale(1.25F, -1.25F, 1.25F);
        poseStack.translate(0, entity.getEyeHeight() - .65, 0);

        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot()));

        Player player = Minecraft.getInstance().player;
        double dx = player.getX() - entity.getX();
        double dz = player.getZ() - entity.getZ();

        float yaw = (float)(Math.atan2(dz, dx) * (180.0F / Math.PI)) - 90.0F;

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0F));

        // Render the texture with bright light
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(QUEST_MARKER));
        PoseStack.Pose pose = poseStack.last();

        // Draw a quad (corrected vertex order for proper facing)
        vertex(vertexConsumer, pose, 240, -0.5F, 0.5F, 0, 0, 0);
        vertex(vertexConsumer, pose, 240, 0.5F, 0.5F, 0, 1, 0);
        vertex(vertexConsumer, pose, 240, 0.5F, -0.5F, 0, 1, 1);
        vertex(vertexConsumer, pose, 240, -0.5F, -0.5F, 0, 0, 1);

        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               int light, float x, float y, float z, float u, float v) {
        consumer.addVertex(pose.pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}

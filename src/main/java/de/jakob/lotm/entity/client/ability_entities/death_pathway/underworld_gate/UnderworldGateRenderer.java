package de.jakob.lotm.entity.client.ability_entities.death_pathway.underworld_gate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.travelers_door.TravelersDoorModel;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.UnderworldGateEntity;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.TravelersDoorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class UnderworldGateRenderer extends EntityRenderer<UnderworldGateEntity> {

    private final UnderworldGateModel<UnderworldGateEntity> model;
    private static final float SCALE_FACTOR = 3.5f;

    public UnderworldGateRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new UnderworldGateModel<>(context.bakeLayer(UnderworldGateModel.LAYER_LOCATION));
    }

    @Override
    public void render(UnderworldGateEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        float ageInTicks = entity.tickCount + partialTicks;

        model.setupAnim(entity, 0, 0, ageInTicks, 0, 0);

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));

        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));

        poseStack.scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
        poseStack.translate(0, -1.35f, 0);

        RenderType renderType = RenderType.entityTranslucent(this.getTextureLocation(entity));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        int color = 0xFFFFFFFF;

        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(UnderworldGateEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull UnderworldGateEntity entity) {
        if(entity.hasTentacles() && entity.tickCount >= 21)
            return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/underworld_gate/underworld_gate.png");
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/underworld_gate/underworld_gate_no_tentacles.png");
    }
}

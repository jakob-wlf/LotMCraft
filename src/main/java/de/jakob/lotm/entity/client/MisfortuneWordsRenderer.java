package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.MisfortuneWordsEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MisfortuneWordsRenderer extends EntityRenderer<MisfortuneWordsEntity> {
    private final MisfortuneWordsModel<MisfortuneWordsEntity> model;
    private static final float ROTATION_SPEED = 2.0F;

    public MisfortuneWordsRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MisfortuneWordsModel<>(context.bakeLayer(MisfortuneWordsModel.LAYER_LOCATION));
    }

    @Override
    public void render(MisfortuneWordsEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Scale model
        poseStack.scale(3, -3, 3);
        poseStack.translate(0, -1.5, 0);

        float timeSinceAnimationEnd = (entity.tickCount - 10) + partialTicks;
        float rotationDegrees = timeSinceAnimationEnd * ROTATION_SPEED;

        // Rotate around Y axis (horizontal rotation)
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));

        // Render the model
        this.model.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MisfortuneWordsEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/misfortune_words/misfortune_words.png");
    }
}
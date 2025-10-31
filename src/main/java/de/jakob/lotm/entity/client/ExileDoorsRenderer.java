package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ExileDoorsEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ExileDoorsRenderer extends EntityRenderer<ExileDoorsEntity> {



    private ExileDoorsModel model;

    public ExileDoorsRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ExileDoorsModel(context.bakeLayer(ExileDoorsModel.LAYER_LOCATION));
    }

    @Override
    public void render(ExileDoorsEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float time = entity.tickCount + partialTicks;

        // Gentle motion
        poseStack.translate(0.0D, 0.05 * Mth.sin(time * 0.1F) + 1.75, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(.75F * Mth.sin(time * 0.2F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(.25F * Mth.cos(time * 0.15F)));

        // Scale model
        poseStack.scale(1.5F, -1.5F, 1.5F);

        float flicker = 0.8F + 0.2F * Mth.sin(time * 0.3F + entity.getId() % 10);
        float alpha = Math.clamp(0.5F + 0.5F * flicker, 0.0F, 1.0F);

        RenderType renderType = RenderType.entityTranslucent(this.getTextureLocation(entity));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        int color = ((int)(alpha * 255) << 24) | 0xFFFFFF; // ARGB format

        // Render the model
        this.model.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }


    protected int getBlockLightLevel(ExileDoorsEntity entity, BlockPos blockpos) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ExileDoorsEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/exile_doors/exile_doors.png");
    }


}

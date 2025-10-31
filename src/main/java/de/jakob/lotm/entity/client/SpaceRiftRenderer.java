package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.SpaceRiftEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class SpaceRiftRenderer extends EntityRenderer<SpaceRiftEntity> {

    private SpaceRiftModel model;

    public SpaceRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SpaceRiftModel(context.bakeLayer(SpaceRiftModel.LAYER_LOCATION));
    }

    @Override
    public void render(SpaceRiftEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1.5f, -2f, 1.5f);
        poseStack.translate(0, 1.5, 2);

        // Continue rendering
        float time = entity.tickCount + partialTicks;
        float flicker = 0.8F + 0.2F * Mth.sin(time * 0.3F + entity.getId() % 10);
        float alpha = Math.clamp(0.6F + 0.5F * flicker, 0.0F, 1.0F);
        RenderType renderType = RenderType.entityTranslucent(this.getTextureLocation(entity));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        int color = ((int)(alpha * 255) << 24) | 0xFFFFFF;
        this.model.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }


    protected int getBlockLightLevel(SpaceRiftEntity entity, BlockPos blockpos) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SpaceRiftEntity entity) {
        int animState = 3;
        int lifetime = entity.getLifetime();
        if(lifetime < 20) {
            animState = 0;
        }
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/space_rift/space_rift_" + animState + ".png");
    }


}

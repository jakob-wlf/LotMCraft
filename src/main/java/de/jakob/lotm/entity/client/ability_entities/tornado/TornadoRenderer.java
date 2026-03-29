package de.jakob.lotm.entity.client.ability_entities.tornado;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.TornadoEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TornadoRenderer extends EntityRenderer<TornadoEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/tornado/tornado.png");
    private final TornadoModel<TornadoEntity> model;
    private final BlockRenderDispatcher blockRenderer;

    public TornadoRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TornadoModel<>(context.bakeLayer(TornadoModel.LAYER_LOCATION));
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(TornadoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        boolean petrified = entity.getTags().contains("petrified");

        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;

        poseStack.scale(1.85f, -2.1f, 1.85f);
        poseStack.translate(0, -1.5, 0);

        RenderType renderType = petrified ? RenderType.entityTranslucent(LOTMCraft.STONE_TEXTURE) :
                RenderType.entityTranslucent(this.getTextureLocation(entity));

        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        int color = petrified ? 0xFF808080 : 0xFFFFFFFF;
        model.setupAnim(entity, 0, 0, ageInTicks, 0, 0);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();

        renderCirclingBlocks(entity, partialTicks, poseStack, buffer, packedLight);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderCirclingBlocks(TornadoEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        for (TornadoEntity.CirclingBlock circlingBlock : entity.getCirclingBlocks()) {
            poseStack.pushPose();

            float angle = circlingBlock.angle + partialTicks * 5.0f;
            float radians = (float) Math.toRadians(angle);

            float x = (float) (Math.cos(radians) * circlingBlock.radius);
            float z = (float) (Math.sin(radians) * circlingBlock.radius);
            float y = circlingBlock.height + Mth.sin((entity.tickCount + partialTicks) * 0.1f + circlingBlock.height) * 0.5f;

            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle * 2));
            poseStack.mulPose(Axis.XP.rotationDegrees(angle));
            poseStack.scale(0.5f, 0.5f, 0.5f);

            blockRenderer.renderSingleBlock(circlingBlock.state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(TornadoEntity entity) {
        return TEXTURE;
    }
}
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

    public TornadoRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TornadoModel<>(context.bakeLayer(TornadoModel.LAYER_LOCATION));
    }

    @Override
    public void render(TornadoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        boolean petrified = entity.getTags().contains("petrified");

        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;

        float size = entity.getSize();
        poseStack.scale(1.85f * size, -2.1f * size, 1.85f * size);
        poseStack.translate(0, -1.5, 0);

        RenderType renderType = petrified ? RenderType.entityTranslucent(LOTMCraft.STONE_TEXTURE) :
                RenderType.entityTranslucent(this.getTextureLocation(entity));

        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        int color = petrified ? 0xFF808080 : 0xFFFFFFFF;
        model.setupAnim(entity, 0, 0, ageInTicks, 0, 0);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TornadoEntity entity) {
        return TEXTURE;
    }
}
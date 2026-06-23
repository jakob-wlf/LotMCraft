package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


public class DiscernmentRendererLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public static final RenderType DISCERNMENT = RenderType.create(
            "discernment_outline",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(true)
    );

    public DiscernmentRendererLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        Player player = Minecraft.getInstance().player;

        if(!DiscernmentRenderer.activeDiscernment.containsKey(player.getUUID())) return;

        float r = 1f, g = 1f, b = 1f;

        if(entity instanceof Player){
            r = 1.0F; g = 0.0F; b = 1.0F;
        }
        else if (entity.getType().getCategory().isFriendly()) {
            r = 0.0F; g = 1F; b = 0.0F;
        } else if (ClientBeyonderCache.isBeyonder(entity.getUUID())) {
            r = 1.0F; g = 1F; b = 0.0F;
        }
        else {
            r = 1.0F; g = 0.0F; b = 0.0F;
        }

        VertexConsumer vc = buffer.getBuffer(DISCERNMENT);

        int grayColor = packColor(r, g, b, 1);

        poseStack.pushPose();

        poseStack.scale(1.15F, 1.15F, 1.15F);

        this.getParentModel().renderToBuffer(
                poseStack,
                vc,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                grayColor
        );

        poseStack.popPose();
    }

    private int packColor(float r, float g, float b, float a) {
        int red = (int)(r * 255.0F);
        int green = (int)(g * 255.0F);
        int blue = (int)(b * 255.0F);
        int alpha = (int)(a * 255.0F);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}

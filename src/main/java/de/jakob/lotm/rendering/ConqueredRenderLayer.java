package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ConqueredRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public ConqueredRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        if (!entity.hasEffect(ModEffects.CONQUERED)) {
            return;
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/misc/conquered_overlay.png")
        ));

        int grayColor = packColor(1, 0, 0, 1.0f);

        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                grayColor
        );
    }

    private int packColor(float r, float g, float b, float a) {
        int red = (int)(r * 255.0F);
        int green = (int)(g * 255.0F);
        int blue = (int)(b * 255.0F);
        int alpha = (int)(a * 255.0F);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
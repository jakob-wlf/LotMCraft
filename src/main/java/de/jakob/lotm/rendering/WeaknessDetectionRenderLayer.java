package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class WeaknessDetectionRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public static final HashMap<Integer, Integer> activeWeaknessDetection = new HashMap<>();

    public WeaknessDetectionRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        Integer tier = activeWeaknessDetection.get(entity.getId());
        if (tier == null) {
            return;
        }

        float[] c = colorForTier(tier);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.outline(
                ResourceLocation.withDefaultNamespace("textures/misc/white.png")
        ));

        int packedColor = packColor(c[0], c[1], c[2], 1.0f);

        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                packedColor
        );
    }

    private static float[] colorForTier(int tier) {
        return switch (tier) {
            case 1 -> new float[] { 1.0F, 1.0F, 0.15F };
            case 2 -> new float[] { 1.0F, 0.55F, 0.0F };
            case 3 -> new float[] { 1.0F, 0.0F, 0.0F };
            default -> new float[] { 1.0F, 1.0F, 1.0F };
        };
    }

    private static int packColor(float r, float g, float b, float a) {
        int red = (int) (r * 255.0F);
        int green = (int) (g * 255.0F);
        int blue = (int) (b * 255.0F);
        int alpha = (int) (a * 255.0F);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
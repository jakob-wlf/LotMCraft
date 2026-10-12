package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

public class IllusoryScalesLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final int FULL_BRIGHT = 0xF000F0;

    private static final float HUE_GREEN = 0.38F;
    private static final float HUE_BLUE = 0.56F;

    private final Predicate<T> shouldRender;

    public IllusoryScalesLayer(RenderLayerParent<T, M> renderer) {
        this(renderer, entity -> true);
    }

    public IllusoryScalesLayer(RenderLayerParent<T, M> renderer, Predicate<T> shouldRender) {
        super(renderer);
        this.shouldRender = shouldRender;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!this.shouldRender.test(entity)) {
            return;
        }

        boolean invisible = entity.isInvisible();
        VertexConsumer consumer = bufferSource.getBuffer(
                invisible ? IllusoryScalesRenderTypes.SCALES_GHOST : IllusoryScalesRenderTypes.SCALES);

        float phase = (entity.getId() * 0.73F) % Mth.TWO_PI;

        float hueMix = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.04F + phase);
        int rgb = Mth.hsvToRgb(Mth.lerp(hueMix, HUE_GREEN, HUE_BLUE), 0.60F, 1.0F);

        float speedBoost = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float pulse = 0.5F + 0.5F * Mth.sin(ageInTicks * (0.10F + 0.15F * speedBoost) + phase * 2.0F);
        float intensity = Mth.lerp(pulse, 0.45F, 0.85F) + 0.15F * speedBoost;
        if (invisible) {
            intensity *= 0.8F;
        }
        intensity = Mth.clamp(intensity, 0.0F, 1.0F);

        int r = (int) (((rgb >> 16) & 0xFF) * intensity);
        int g = (int) (((rgb >> 8) & 0xFF) * intensity);
        int b = (int) ((rgb & 0xFF) * intensity);
        int color = 0xFF000000 | (r << 16) | (g << 8) | b;   // ARGB

        this.getParentModel().renderToBuffer(poseStack, consumer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
    }
}
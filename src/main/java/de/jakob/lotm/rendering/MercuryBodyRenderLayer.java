package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.MercuryBodyAbility;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class MercuryBodyRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public MercuryBodyRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(PassiveAbilityHandler.MERCURY_BODY.get() instanceof MercuryBodyAbility mercuryBody)
                || !mercuryBody.shouldApplyTo(entity)) {
            return;
        }

        float shimmer = (Mth.sin(ageInTicks * 0.08f) + 1) * 0.5f;
        float red = 0.68f + shimmer * 0.08f;
        float green = 0.74f + shimmer * 0.08f;
        float blue = 0.80f + shimmer * 0.10f;
        float alpha = 0.20f + shimmer * 0.08f;

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                packColor(red, green, blue, alpha));
    }

    private static int packColor(float red, float green, float blue, float alpha) {
        return ((int) (alpha * 255) << 24)
                | ((int) (red * 255) << 16)
                | ((int) (green * 255) << 8)
                | (int) (blue * 255);
    }
}
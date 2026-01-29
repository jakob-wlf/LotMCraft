package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class DecryptionRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public static final HashSet<UUID> activeDecryption = new HashSet<>();

    public DecryptionRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        Player player = Minecraft.getInstance().player;
        if(!activeDecryption.contains(player.getUUID())) {
            return;
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.outline(
                ResourceLocation.withDefaultNamespace("textures/misc/white.png")
        ));

        int grayColor = packColor(.05f, .875f, .3f, 1);

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
package de.jakob.lotm.entity.client.projectiles.wind_blade;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.WindBladeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class WindBladeRenderer extends EntityRenderer<WindBladeEntity> {

    private WindBladeModel model;

    public WindBladeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WindBladeModel<>(context.bakeLayer(WindBladeModel.LAYER_LOCATION));
    }

    @Override
    public void render(WindBladeEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        boolean petrified = entity.getTags().contains("petrified");

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));

        RenderType renderType = petrified ? this.model.renderType(LOTMCraft.STONE_TEXTURE) :
                this.model.renderType(this.getTextureLocation(entity));

        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(
                buffer, renderType, false, false
        );
        int color = petrified ? 0xFF808080 : 0xFFFFFFFF;
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    protected int getBlockLightLevel(WindBladeEntity projectileEntity, BlockPos blockpos) { return 15; }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull WindBladeEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/wind_blade/wind_blade.png");
    }
}
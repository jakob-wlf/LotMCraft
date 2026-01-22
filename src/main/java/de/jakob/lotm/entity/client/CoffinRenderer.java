package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.CoffinEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CoffinRenderer extends EntityRenderer<CoffinEntity> {
    private CoffinModel model;
    private static final float ROTATION_SPEED = 1.0F; // Adjust this value to change rotation speed (degrees per tick)

    public CoffinRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CoffinModel(context.bakeLayer(CoffinModel.LAYER_LOCATION));
    }

    @Override
    public void render(CoffinEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Scale model
        poseStack.scale(5, -5, 5);
        poseStack.translate(0, -1.5, 0);

        // Add rotation after the closing animation has played (after 10 ticks)
        if (entity.hasPlayedAnimation()) {
            // Calculate rotation based on time since animation finished
            // The animation finishes at tick 10, so we subtract that
            float timeSinceAnimationEnd = (entity.tickCount - 10) + partialTicks;
            float rotationDegrees = timeSinceAnimationEnd * ROTATION_SPEED;

            // Rotate around Y axis (horizontal rotation)
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));
        }

        // Setup animation - this will call animate() internally if needed
        float ageInTicks = entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0, 0, ageInTicks, 0, 0);

        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));

        // Render the model
        this.model.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CoffinEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/coffin/coffin.png");
    }
}
package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ApprenticeBookEntity;
import de.jakob.lotm.entity.custom.ApprenticeDoorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ApprenticeBookRenderer extends EntityRenderer<ApprenticeBookEntity> {

    private final ApprenticeBookModel<ApprenticeBookEntity> model;

    public ApprenticeBookRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ApprenticeBookModel<>(context.bakeLayer(ApprenticeBookModel.LAYER_LOCATION));
    }

    @Override
    public void render(ApprenticeBookEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Get interpolated rotations
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        // Apply rotations - try these one at a time to see which works
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw + 180.0F)); // Try with/without +180
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        // Get the render type
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(this.getTextureLocation(entity)));

        // Render the model
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(ApprenticeBookEntity entity, BlockPos blockPos) {
        return 15; // Always fully lit for ethereal effect
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ApprenticeBookEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/apprentice_book/apprentice_book.png");
    }
}
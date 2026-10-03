package de.jakob.lotm.entity.client.projectiles.spirit_block;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.entity.custom.projectiles.SpiritBlockProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Renders the SpiritBlockProjectileEntity as a block, similar to how FallingBlockEntity is rendered.
 */
public class SpiritBlockProjectileRenderer extends EntityRenderer<SpiritBlockProjectileEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public SpiritBlockProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(SpiritBlockProjectileEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState blockState = entity.getBlockState();
        if (blockState.getRenderShape() == RenderShape.INVISIBLE) return;

        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);

        this.blockRenderer.renderSingleBlock(
                blockState, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SpiritBlockProjectileEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}

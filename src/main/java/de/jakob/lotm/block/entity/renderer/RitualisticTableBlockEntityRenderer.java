package de.jakob.lotm.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.jakob.lotm.block.custom.RitualisticTableBlock;
import de.jakob.lotm.block.entity.RitualisticTableBlockEntity;
import de.jakob.lotm.gui.custom.ritualistic_table.RitualMenu;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RitualisticTableBlockEntityRenderer implements BlockEntityRenderer<RitualisticTableBlockEntity> {

    public RitualisticTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RitualisticTableBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = ClientHandler.getMinecraftInstance().getItemRenderer();
        ItemStack stack1 = be.itemHandler.getStackInSlot(RitualMenu.SACRIFICE_SLOT_1);
        ItemStack stack2 = be.itemHandler.getStackInSlot(RitualMenu.SACRIFICE_SLOT_2);
        ItemStack stack3 = be.itemHandler.getStackInSlot(RitualMenu.SACRIFICE_SLOT_3);
        ItemStack candleStack = be.itemHandler.getStackInSlot(RitualMenu.CANDLE_SLOT);

        Direction facing = be.getBlockState().getValue(RitualisticTableBlock.FACING);
        float rotY = getRotationForFacing(facing);

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.translate(-0.5, 0, -0.5);

        renderSlot(stack1, .375f, 1.035f, .535f, be, itemRenderer, poseStack, multiBufferSource);
        renderSlot(stack2, .375f, 1.035f, 1.335f, be, itemRenderer, poseStack, multiBufferSource);
        renderSlot(stack3, .375f, 1.035f, -.300f, be, itemRenderer, poseStack, multiBufferSource);

        renderCandleSlot(candleStack, .5f, 1.0f, .5f, be, poseStack, multiBufferSource);

        poseStack.popPose();
    }

    private void renderSlot(ItemStack stack, float x, float y, float z, RitualisticTableBlockEntity be,
                            ItemRenderer itemRenderer, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(0.35f, 2, 0.35f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, getLightLevel(), OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, be.getLevel(), 1);
        poseStack.popPose();
    }

    private void renderCandleSlot(ItemStack stack, float x, float y, float z, RitualisticTableBlockEntity be,
                                  PoseStack poseStack, MultiBufferSource multiBufferSource) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof CandleBlock candleBlock)) {
            return;
        }

        BlockState state = candleBlock.defaultBlockState()
                .setValue(CandleBlock.CANDLES, 3);

        poseStack.pushPose();
        poseStack.translate(x - .15, y, z - 0.5);

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        blockRenderer.renderSingleBlock(state, poseStack, multiBufferSource,
                getLightLevel(), OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    private float getRotationForFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> -90f;
            case EAST  -> 180f;
            case SOUTH -> 90f;
            case WEST  -> 0f;
            default -> 0f;
        };
    }

    private int getLightLevel() {
        return LightTexture.pack(15, 15);
    }
}

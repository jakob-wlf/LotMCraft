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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class RitualisticTableBlockEntityRenderer implements BlockEntityRenderer<RitualisticTableBlockEntity> {

    public RitualisticTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RitualisticTableBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = ClientHandler.getMinecraftInstance().getItemRenderer();
        ItemStack stack1 = be.itemHandler.getStackInSlot(RitualMenu.SACRIFICE_SLOT_1);
        ItemStack stack2 = be.itemHandler.getStackInSlot(RitualMenu.SACRIFICE_SLOT_2);
        ItemStack stack3 = be.itemHandler.getStackInSlot(RitualMenu.SACRIFICE_SLOT_3);

        poseStack.pushPose();
        poseStack.translate(.375f, 1.035f, .535f); // x kleiner --> näher, z kleiner --> links
        poseStack.scale(0.35f, 0.35f, 0.35f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90));


        itemRenderer.renderStatic(stack1, ItemDisplayContext.FIXED, getLightLevel(be.getLevel(),
                be.getBlockPos()), OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, be.getLevel(), 1);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(.375f, 1.035f, 1.335f); // x kleiner --> näher, z kleiner --> links
        poseStack.scale(0.35f, 0.35f, 0.35f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90));


        itemRenderer.renderStatic(stack2, ItemDisplayContext.FIXED, getLightLevel(be.getLevel(),
                be.getBlockPos()), OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, be.getLevel(), 1);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(.375f, 1.035f, (-.235f)); // x kleiner --> näher, z kleiner --> links
        poseStack.scale(0.35f, 0.35f, 0.35f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90));

        itemRenderer.renderStatic(stack3, ItemDisplayContext.FIXED, getLightLevel(be.getLevel(),
                be.getBlockPos()), OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, be.getLevel(), 1);
        poseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}

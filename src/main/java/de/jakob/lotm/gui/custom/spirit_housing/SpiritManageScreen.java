package de.jakob.lotm.gui.custom.spirit_housing;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SpiritManageScreen extends net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<SpiritManageMenu> {
    public SpiritManageScreen(SpiritManageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 150;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0101018);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF6a4a78);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.head"), leftPos + 8, topPos + 22, 0xFFC8B4D4, false);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.torso"), leftPos + 8, topPos + 42, 0xFFC8B4D4, false);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.arms"), leftPos + 8, topPos + 62, 0xFFC8B4D4, false);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.legs"), leftPos + 8, topPos + 82, 0xFFC8B4D4, false);
        graphics.drawString(font, Component.translatable("ability.lotmcraft.spirit_commanding.bin"), leftPos + 8, topPos + 102, 0xFF886666, false);
    }
}

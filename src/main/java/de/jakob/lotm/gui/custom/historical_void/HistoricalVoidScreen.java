package de.jakob.lotm.gui.custom.historical_void;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class HistoricalVoidScreen extends AbstractContainerScreen<de.jakob.lotm.gui.custom.historical_void.HistoricalVoidMenu> {

    private static final int PANEL_TOP    = 0xE6EAF0F2;
    private static final int PANEL_BOTTOM = 0xE6C6CDD6;

    private static final int GLOW         = 0x50D8E4EA;
    private static final int GLOW_SOFT    = 0x20D8E4EA;

    private static final int FOG_LIGHT    = 0x30FFFFFF;
    private static final int FOG_DARK     = 0x1A9AA6AE;

    private static final int SLOT_FILL       = 0x2CFFFFFF;
    private static final int SLOT_FILL_HOVER = 0x55E8F4F8;
    private static final int SLOT_BORDER     = 0x40FFFFFF;
    private static final int SLOT_BORDER_HOVER = 0x90D8ECF4;

    public HistoricalVoidScreen(HistoricalVoidMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 8 + HistoricalVoidMenu.COLS * 18 + 8;
        this.imageHeight = 18 + HistoricalVoidMenu.ROWS * 18 + 14;
        this.inventoryLabelY = -1000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        graphics.fillGradient(x, y, x + w, y + h, PANEL_TOP, PANEL_BOTTOM);

        renderFogLayer(graphics, x, y, w, h, partialTick);

        graphics.fillGradient(x - 5, y - 5, x + w + 5, y, 0x00FFFFFF, GLOW);
        graphics.fillGradient(x - 5, y + h, x + w + 5, y + h + 5, GLOW, 0x00FFFFFF);
        graphics.fillGradient(x - 5, y, x, y + h, 0x00FFFFFF, GLOW_SOFT);
        graphics.fillGradient(x + w, y, x + w + 5, y + h, GLOW_SOFT, 0x00FFFFFF);

        for (int row = 0; row < HistoricalVoidMenu.ROWS; row++) {
            for (int col = 0; col < HistoricalVoidMenu.COLS; col++) {
                int slotX = x + 8 + col * 18;
                int slotY = y + 18 + row * 18;

                boolean hovered = mouseX >= slotX && mouseX < slotX + 16
                        && mouseY >= slotY && mouseY < slotY + 16;

                int fill = hovered ? SLOT_FILL_HOVER : SLOT_FILL;
                int border = hovered ? SLOT_BORDER_HOVER : SLOT_BORDER;

                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, fill);
                graphics.fill(slotX, slotY, slotX + 16, slotY + 1, border);
                graphics.fill(slotX, slotY, slotX + 1, slotY + 16, border);
                graphics.fill(slotX, slotY + 15, slotX + 16, slotY + 16, 0x20000000);
                graphics.fill(slotX + 15, slotY, slotX + 16, slotY + 16, 0x20000000);

                if (hovered) {
                    graphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY, 0x30D8ECF4);
                    graphics.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, 0x30D8ECF4);
                    graphics.fill(slotX - 1, slotY - 1, slotX, slotY + 17, 0x30D8ECF4);
                    graphics.fill(slotX + 16, slotY - 1, slotX + 17, slotY + 17, 0x30D8ECF4);
                }
            }
        }
    }

    private void renderFogLayer(GuiGraphics graphics, int x, int y, int w, int h, float partialTick) {
        long time = System.currentTimeMillis();
        int bandHeight = Math.max(6, h / 5);

        for (int i = 0; i < 5; i++) {
            double speed = 4000 + i * 900;
            double phase = (time + i * 1500L) % (long) speed;
            double t = phase / speed;
            double sway = Math.sin(t * Math.PI * 2) * (w * 0.15);

            int bandY = y + i * bandHeight;
            int bandBottom = Math.min(bandY + bandHeight, y + h);
            if (bandY >= y + h) continue;

            int offset = (int) sway;
            int color = (i % 2 == 0) ? FOG_LIGHT : FOG_DARK;

            graphics.fillGradient(
                    x - 10 + offset, bandY,
                    x + w + 10 + offset, bandBottom,
                    color, fadeAlpha(color)
            );
        }
    }

    private int fadeAlpha(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int fadedA = Math.max(0, a - (a / 2));
        return (fadedA << 24) | (argb & 0x00FFFFFF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX + 1, this.titleLabelY + 1, 0x20FFFFFF, false);
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x4A5560, false);
    }
}
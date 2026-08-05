package de.jakob.lotm.gui.custom.WheelSelection;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestDailySpinPacket;
import de.jakob.lotm.network.packets.toServer.RequestSellYourSoulInfoPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Small popup that lets the player choose which wheel/exchange to open.
 * Replaces the individual Daily Spin / Sell Soul / Char Exchange buttons
 * that previously cluttered the Introspect menu.
 */
@OnlyIn(Dist.CLIENT)
public class WheelSelectionScreen extends Screen {

    private static final int PANEL_W = 200;
    private static final int PANEL_H = 182;

    private static final int COL_BG      = 0xF0060010;
    private static final int COL_OUTLINE = 0xFF440088;
    private static final int COL_TITLE   = 0xFFCC88FF;

    /** Screen to return to when this popup is closed (the Introspect screen). */
    private final Screen parent;

    public WheelSelectionScreen(Screen parent) {
        super(Component.literal("Wheel Selection"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        int btnX = cx + 10;
        int btnW = PANEL_W - 20;

        // Daily Spin
        addRenderableWidget(Button.builder(
                Component.literal("✦ Daily Spin").withStyle(ChatFormatting.GOLD),
                b -> {
                    PacketHandler.sendToServer(new RequestDailySpinPacket());
                    onClose();
                })
                .bounds(btnX, cy + 22, btnW, 20)
                .build());

        // Sell Your Soul
        addRenderableWidget(Button.builder(
                Component.literal("☠ Sell Your Soul").withStyle(ChatFormatting.DARK_RED),
                b -> {
                    PacketHandler.sendToServer(new RequestSellYourSoulInfoPacket());
                    onClose();
                })
                .bounds(btnX, cy + 46, btnW, 20)
                .build());

        // Characteristics Exchange
        addRenderableWidget(Button.builder(
                Component.literal("✧ Characteristics Exchange").withStyle(ChatFormatting.LIGHT_PURPLE),
                b -> {
                    onClose();
                    Minecraft.getInstance().setScreen(
                            new de.jakob.lotm.gui.custom.CharExchange.CharExchangeSelectScreen());
                })
                .bounds(btnX, cy + 70, btnW, 20)
                .build());
        // Char Path Exchange
        addRenderableWidget(Button.builder(
                Component.literal("\u2604 Char Path Exchange").withStyle(ChatFormatting.AQUA),
                b -> {
                    onClose();
                    Minecraft.getInstance().setScreen(
                            new de.jakob.lotm.gui.custom.CharExchange.CharExchangeSelectScreen(
                                    de.jakob.lotm.gui.custom.CharExchange.CharExchangeSelectScreen.Mode.PATH_EXCHANGE));
                })
                .bounds(btnX, cy + 94, btnW, 20)
                .build());

        // Recipe Exchange
        addRenderableWidget(Button.builder(
                Component.literal("\u2318 Recipe Exchange").withStyle(ChatFormatting.YELLOW),
                b -> {
                    onClose();
                    Minecraft.getInstance().setScreen(
                            new de.jakob.lotm.gui.custom.CharExchange.RecipeExchangeSelectScreen(
                                    de.jakob.lotm.gui.custom.CharExchange.RecipeExchangeSelectScreen.Mode.EXCHANGE));
                })
                .bounds(btnX, cy + 118, btnW, 20)
                .build());

        // Recipe Path Exchange
        addRenderableWidget(Button.builder(
                Component.literal("\u2318 Recipe Path Exchange").withStyle(ChatFormatting.GREEN),
                b -> {
                    onClose();
                    Minecraft.getInstance().setScreen(
                            new de.jakob.lotm.gui.custom.CharExchange.RecipeExchangeSelectScreen(
                                    de.jakob.lotm.gui.custom.CharExchange.RecipeExchangeSelectScreen.Mode.PATH_EXCHANGE));
                })
                .bounds(btnX, cy + 142, btnW, 20)
                .build());    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Darken but keep the parent visible behind
        g.fill(0, 0, width, height, 0x88000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, COL_BG);
        drawBorder(g, cx, cy, PANEL_W, PANEL_H, COL_OUTLINE);

        g.drawCenteredString(font, "Choose a Wheel", cx + PANEL_W / 2, cy + 7, COL_TITLE);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        // Return to parent (Introspect) rather than closing everything
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,         x + w,     y + 1,     color);
        g.fill(x,         y + h - 1, x + w,     y + h,     color);
        g.fill(x,         y,         x + 1,     y + h,     color);
        g.fill(x + w - 1, y,         x + w,     y + h,     color);
    }
}

package de.jakob.lotm.gui.custom.CharExchange;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestCharExchangePacket;
import de.jakob.lotm.network.packets.toServer.RequestCharPathExchangePacket;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 1 of the Characteristics Exchange UI.
 *
 * Displays the flavour text and lists every characteristic currently in the
 * player's inventory (slots 0-35). The player clicks one to sacrifice it.
 */
@OnlyIn(Dist.CLIENT)
public class CharExchangeSelectScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W = 400;
    private static final int PANEL_H = 300;
    private static final int LIST_X_PAD = 20;
    private static final int ROW_H     = 22;

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final int COL_BG       = 0xF0080014;
    private static final int COL_OUTLINE  = 0xFF4400AA;
    private static final int COL_TITLE    = 0xFFCC88FF;
    private static final int COL_FLAVOUR  = 0xFFAA88CC;
    private static final int COL_DIVIDER  = 0xFF330088;
    private static final int COL_LABEL    = 0xFFFFFFFF;
    private static final int COL_SUBLABEL = 0xFF888888;

    public enum Mode { EXCHANGE, PATH_EXCHANGE }

    // ── Discovered characteristics ────────────────────────────────────────────
    private final List<CharEntry> entries = new ArrayList<>();
    private final Mode mode;

    private Button cancelButton;

    private record CharEntry(int slot, ItemStack stack, String displayName) {}

    public CharExchangeSelectScreen() {
        this(Mode.EXCHANGE);
    }

    public CharExchangeSelectScreen(Mode mode) {
        super(Component.literal(mode == Mode.PATH_EXCHANGE ? "Char Path Exchange" : "Characteristics Exchange"));
        this.mode = mode;
    }

    @Override
    protected void init() {
        super.init();

        // Scan player inventory for characteristics
        entries.clear();
        var inventory = Minecraft.getInstance().player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BeyonderCharacteristicItem) {
                entries.add(new CharEntry(i, stack.copy(), stack.getHoverName().getString()));
            }
        }

        int cx  = (width  - PANEL_W) / 2;
        int cy  = (height - PANEL_H) / 2;

        // Characteristic row buttons
        int listTop = cy + 80;
        for (int idx = 0; idx < entries.size(); idx++) {
            CharEntry entry = entries.get(idx);
            int rowY = listTop + idx * ROW_H;
            final int finalSlot = entry.slot();
            addRenderableWidget(Button.builder(
                    Component.literal(entry.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE),
                    btn -> onCharSelected(finalSlot))
                    .bounds(cx + LIST_X_PAD, rowY, PANEL_W - LIST_X_PAD * 2, ROW_H - 2)
                    .build());
        }

        // Cancel button
        cancelButton = addRenderableWidget(Button.builder(
                Component.literal("Cancel").withStyle(ChatFormatting.GRAY),
                btn -> onClose())
                .bounds(cx + PANEL_W / 2 - 40, cy + PANEL_H - 28, 80, 20)
                .build());
    }

    private void onCharSelected(int slot) {
        // Remove the characteristic from the client inventory immediately for instant feedback
        var inv = Minecraft.getInstance().player.getInventory();
        inv.setItem(slot, ItemStack.EMPTY);

        if (mode == Mode.PATH_EXCHANGE) {
            PacketHandler.sendToServer(new RequestCharPathExchangePacket(slot));
        } else {
            PacketHandler.sendToServer(new RequestCharExchangePacket(slot));
        }
        // Close — server will open the wheel screen once processing is done
        onClose();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xBB000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        // Background panel
        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, COL_BG);
        drawBorder(g, cx, cy, PANEL_W, PANEL_H, COL_OUTLINE);

        // Title
        String title = mode == Mode.PATH_EXCHANGE ? "Char Path Exchange" : "Characteristics Exchange";
        g.drawCenteredString(font, title, cx + PANEL_W / 2, cy + 10, COL_TITLE);

        // Flavour text (word-wrapped)
        String flavour = mode == Mode.PATH_EXCHANGE
                ? "Fate cares not for your pathway. What will it return to you?"
                : "A price is always exacted for what fate bestows, isn't it.";
        drawWrappedText(g, flavour, cx + LIST_X_PAD, cy + 28, PANEL_W - LIST_X_PAD * 2, COL_FLAVOUR);

        // Section divider
        g.fill(cx + LIST_X_PAD, cy + 70, cx + PANEL_W - LIST_X_PAD, cy + 71, COL_DIVIDER);

        // Instruction
        if (entries.isEmpty()) {
            g.drawCenteredString(font, "No characteristics found in inventory.",
                    cx + PANEL_W / 2, cy + 82, COL_SUBLABEL);
        } else {
            g.drawString(font, "Select a characteristic to sacrifice:", cx + LIST_X_PAD, cy + 75, COL_SUBLABEL, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,         x + w,     y + 1,     color);
        g.fill(x,         y + h - 1, x + w,     y + h,     color);
        g.fill(x,         y,         x + 1,     y + h,     color);
        g.fill(x + w - 1, y,         x + w,     y + h,     color);
    }

    private void drawWrappedText(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        List<net.minecraft.util.FormattedCharSequence> lines =
                font.split(Component.literal(text), maxWidth);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(font, lines.get(i), x, y + i * (font.lineHeight + 1), color, false);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

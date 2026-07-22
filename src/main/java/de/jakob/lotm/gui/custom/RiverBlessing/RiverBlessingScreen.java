package de.jakob.lotm.gui.custom.RiverBlessing;

import com.mojang.authlib.GameProfile;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RiverAudienceActionPacket;
import de.jakob.lotm.network.packets.toServer.RiverBlessingActionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GUI for the River of Eternal Darkness Blessings feature.
 *
 * Shows the list of players who have prayed to the River owner (left panel).
 * The owner can select a player and bless or remove their blessing (right panel).
 *
 * Blessing limits:
 *   Seq 4 or 3 → 1 slot
 *   Seq 2 or better → 2 slots
 *
 * Blessed players receive:
 *   • Immunity to the ASLEEP mob effect
 *   • Passive concealment: divination blocked for diviners ≤ 3 seqs above the blessed player
 */
public class RiverBlessingScreen extends AbstractContainerScreen<RiverBlessingMenu> {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_WIDTH  = 280;
    private static final int PANEL_HEIGHT = 220;

    private static final int LIST_X    = 8;
    private static final int LIST_Y    = 44;
    private static final int HEAD_SIZE = 16;
    private static final int HEAD_GAP  = 2;
    private static final int ROW_H     = HEAD_SIZE + HEAD_GAP;

    private static final int DETAIL_X  = 158;
    private static final int DETAIL_W  = PANEL_WIDTH - DETAIL_X - 8;

    // ── River colour palette ──────────────────────────────────────────────────
    private static final int COLOR_BG      = 0xDD000A0F;
    private static final int COLOR_OUTLINE = 0xFF004466;
    private static final int COLOR_TITLE   = 0xFF66DDFF;
    private static final int COLOR_DIVIDER = 0xFF003355;
    private static final int COLOR_BLESSED = 0xFF00CCAA;

    // ── State ─────────────────────────────────────────────────────────────────
    private UUID   selectedUUID = null;
    private String selectedName = "";
    private final List<ItemStack> headStacks = new ArrayList<>();

    private Button blessButton;
    private Button unblessButton;
    private Button summonButton;
    private Button dismissButton;

    public RiverBlessingScreen(RiverBlessingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width  - imageWidth)  / 2;
        this.topPos  = (this.height - imageHeight) / 2;

        // Build player-head stacks for the prayer list
        headStacks.clear();
        for (RiverBlessingMenu.BlessingEntry e : menu.getPrayers()) {
            ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
            skull.set(DataComponents.PROFILE, new ResolvableProfile(new GameProfile(e.uuid(), e.name())));
            headStacks.add(skull);
        }

        int bx = leftPos + DETAIL_X;

        blessButton = addRenderableWidget(Button.builder(
                Component.literal("✦ Bestow Blessing").withStyle(ChatFormatting.AQUA),
                b -> performAction(RiverBlessingActionPacket.BLESS, selectedUUID))
                .bounds(bx, topPos + PANEL_HEIGHT - 70, DETAIL_W, 18).build());

        unblessButton = addRenderableWidget(Button.builder(
                Component.literal("✧ Remove Blessing").withStyle(ChatFormatting.RED),
                b -> performAction(RiverBlessingActionPacket.UNBLESS, selectedUUID))
                .bounds(bx, topPos + PANEL_HEIGHT - 48, DETAIL_W, 18).build());

        // ── Audience buttons (bottom full-width strip) ────────────────────────
        int halfW = (PANEL_WIDTH - 12) / 2; // ~134 px each side
        summonButton = addRenderableWidget(Button.builder(
                Component.literal("▶ Summon All").withStyle(ChatFormatting.DARK_AQUA),
                b -> { PacketHandler.sendToServer(new RiverAudienceActionPacket(RiverAudienceActionPacket.SUMMON)); this.onClose(); })
                .bounds(leftPos + 4, topPos + PANEL_HEIGHT - 22, halfW, 16).build());

        dismissButton = addRenderableWidget(Button.builder(
                Component.literal("◀ Dismiss All").withStyle(ChatFormatting.GRAY),
                b -> { PacketHandler.sendToServer(new RiverAudienceActionPacket(RiverAudienceActionPacket.DISMISS)); this.onClose(); })
                .bounds(leftPos + 4 + halfW + 4, topPos + PANEL_HEIGHT - 22, halfW, 16).build());

        updateButtonStates();
    }

    private void performAction(int action, UUID target) {
        if (target == null) return;
        PacketHandler.sendToServer(new RiverBlessingActionPacket(action, target));
        this.onClose();
    }

    private void updateButtonStates() {
        boolean hasTarget  = selectedUUID != null;
        boolean isBlessed  = hasTarget && menu.getBlessed().contains(selectedUUID);
        int     usedSlots  = menu.getBlessed().size();
        int     maxSlots   = menu.getMaxBlessings();

        blessButton.active   = hasTarget && !isBlessed && usedSlots < maxSlots;
        unblessButton.active = hasTarget && isBlessed;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + PANEL_WIDTH, topPos + PANEL_HEIGHT, COLOR_BG);
        g.renderOutline(leftPos, topPos, PANEL_WIDTH, PANEL_HEIGHT, COLOR_OUTLINE);

        // Title
        Component title = Component.literal("River — Blessings").withStyle(ChatFormatting.BOLD);
        g.drawString(font, title,
                leftPos + PANEL_WIDTH / 2 - font.width(title) / 2,
                topPos + 7, COLOR_TITLE, true);

        // Dividers
        g.fill(leftPos + 4, topPos + 18, leftPos + PANEL_WIDTH - 4, topPos + 19, COLOR_DIVIDER);
        g.fill(leftPos + 4, topPos + 40, leftPos + PANEL_WIDTH - 4, topPos + 41, COLOR_DIVIDER);
        g.fill(leftPos + DETAIL_X - 6, topPos + 20,
               leftPos + DETAIL_X - 5, topPos + PANEL_HEIGHT - 8, 0xFF333355);

        // Slot counter (left) | Prayers Received header (right of divider) — on same sub-header row
        int used = menu.getBlessed().size();
        int max  = menu.getMaxBlessings();
        Component slotText = Component.literal("Blessing Slots: " + used + " / " + max)
                .withStyle(used >= max ? ChatFormatting.RED : ChatFormatting.GRAY);
        g.drawString(font, slotText, leftPos + DETAIL_X, topPos + 22, 0xFFAAAAAA, false);

        g.drawString(font, Component.literal("Prayers Received").withStyle(ChatFormatting.GRAY),
                leftPos + LIST_X, topPos + 22, 0xFFAAAAAA, false);

        renderPrayerList(g, mouseX, mouseY);
        renderDetailPanel(g);
    }

    private void renderPrayerList(GuiGraphics g, int mouseX, int mouseY) {
        List<RiverBlessingMenu.BlessingEntry> prayers = menu.getPrayers();
        int visibleRows = (PANEL_HEIGHT - LIST_Y - 10) / ROW_H;

        for (int i = 0; i < Math.min(prayers.size(), visibleRows); i++) {
            RiverBlessingMenu.BlessingEntry entry = prayers.get(i);
            int rx = leftPos + LIST_X;
            int ry = topPos + LIST_Y + i * ROW_H;

            boolean isBlessed  = menu.getBlessed().contains(entry.uuid());
            boolean isSelected = entry.uuid().equals(selectedUUID);

            if (isSelected) g.fill(rx - 2, ry - 1, rx + 144, ry + HEAD_SIZE + 1, 0x55004466);
            if (isBlessed)  g.fill(rx - 2, ry - 1, rx + 3,   ry + HEAD_SIZE + 1, 0xFF00CCAA);

            if (i < headStacks.size()) g.renderItem(headStacks.get(i), rx, ry);

            int nameColor = isBlessed ? COLOR_BLESSED : 0xFFCCCCCC;
            g.drawString(font, entry.name(), rx + HEAD_SIZE + 4, ry + 4, nameColor, false);

            // Hover highlight
            if (mouseX >= rx && mouseX < rx + 144 && mouseY >= ry && mouseY < ry + HEAD_SIZE) {
                g.fill(rx - 2, ry - 1, rx + 144, ry + HEAD_SIZE + 1, 0x22FFFFFF);
            }
        }
    }

    private void renderDetailPanel(GuiGraphics g) {
        int px = leftPos + DETAIL_X;
        int py = topPos + 48;

        if (selectedUUID == null) {
            g.drawString(font, Component.literal("Select a player").withStyle(ChatFormatting.DARK_GRAY),
                    px, py, 0xFF888888, false);
            return;
        }

        boolean isBlessed = menu.getBlessed().contains(selectedUUID);

        g.drawString(font, selectedName, px, py, 0xFFFFFFFF, false);
        g.drawString(font, Component.literal(isBlessed ? "★ Blessed" : "Not blessed")
                .withStyle(isBlessed ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY),
                px, py + 12, 0xFFCCCCCC, false);

        if (isBlessed) {
            g.drawString(font, Component.literal("• Sleep immune").withStyle(ChatFormatting.GREEN),
                    px, py + 28, 0xFF88FFCC, false);
            g.drawString(font, Component.literal("• Divination shield").withStyle(ChatFormatting.GREEN),
                    px, py + 40, 0xFF88FFCC, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<RiverBlessingMenu.BlessingEntry> prayers = menu.getPrayers();
        int visibleRows = (PANEL_HEIGHT - LIST_Y - 10) / ROW_H;

        for (int i = 0; i < Math.min(prayers.size(), visibleRows); i++) {
            RiverBlessingMenu.BlessingEntry entry = prayers.get(i);
            int rx = leftPos + LIST_X;
            int ry = topPos + LIST_Y + i * ROW_H;

            if (mouseX >= rx - 2 && mouseX < rx + 144 && mouseY >= ry - 1 && mouseY < ry + HEAD_SIZE + 1) {
                selectedUUID = entry.uuid();
                selectedName = entry.name();
                updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Suppress default inventory labels
    }
}

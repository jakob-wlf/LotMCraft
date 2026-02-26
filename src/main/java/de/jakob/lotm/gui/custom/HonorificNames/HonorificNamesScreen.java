package de.jakob.lotm.gui.custom.HonorificNames;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.HonorificNamesRespondPacket;
import de.jakob.lotm.util.beyonderMap.HonorificName;
import de.jakob.lotm.util.beyonderMap.PendingPrayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class HonorificNamesScreen extends AbstractContainerScreen<HonorificNamesMenu> {

    // Colours used throughout the screen
    private static final int COL_BG_OUTER   = 0xFF0D0D1A; // very dark blue-black
    private static final int COL_BG_INNER   = 0xFF141422; // slightly lighter
    private static final int COL_BORDER_LO  = 0xFF2A1A4A; // deep purple
    private static final int COL_BORDER_HI  = 0xFF6A3A9A; // bright purple
    private static final int COL_TITLE      = 0xFFD4AF37; // antique gold
    private static final int COL_LABEL      = 0xFF9090D0; // soft blue-purple
    private static final int COL_TEXT       = 0xFFCCCCCC; // light grey text
    private static final int COL_DIVIDER    = 0xFF3A2A6A; // muted purple divider
    private static final int COL_PRAYER_BG  = 0xFF1C1C30; // prayer card background
    private static final int COL_PRAYER_BRD = 0xFF4A3A7A; // prayer card border

    // Layout
    private static final int W = 280;
    private static final int H = 220;
    private static final int PADDING = 10;
    private static final int TITLE_H = 24;
    private static final int OWN_NAME_H = 80;
    private static final int DIVIDER_H = 6;
    private static final int PRAYER_ENTRY_H = 44;

    // Scroll state for the pending prayers list
    private int prayerScrollOffset = 0;

    public HonorificNamesScreen(HonorificNamesMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    /** Re-create all buttons based on current scroll position. */
    private void rebuildButtons() {
        this.clearWidgets();

        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int listTop = listTop();
        int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
        int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);

        for (int i = 0; i < visibleCount; i++) {
            int idx = i + prayerScrollOffset;
            if (idx >= prayers.size()) break;

            PendingPrayer prayer = prayers.get(idx);
            int entryY = listTop + i * PRAYER_ENTRY_H;

            // "Send Message" button
            int btnIdx = idx;
            this.addRenderableWidget(
                    Button.builder(Component.literal("Send Message"),
                                    btn -> respondToPrayer(btnIdx, false))
                            .bounds(leftPos + PADDING, entryY + 22, 120, 16)
                            .build()
            );

            // "Teleport" button
            this.addRenderableWidget(
                    Button.builder(Component.literal("Teleport"),
                                    btn -> respondToPrayer(btnIdx, true))
                            .bounds(leftPos + PADDING + 126, entryY + 22, 120, 16)
                            .build()
            );
        }

        // Scroll buttons (up / down) if needed
        if (prayers.size() > visibleCount) {
            int scrollBtnX = leftPos + W - PADDING - 16;
            int scrollBtnY = listTop;

            this.addRenderableWidget(
                    Button.builder(Component.literal("▲"),
                                    btn -> {
                                        if (prayerScrollOffset > 0) {
                                            prayerScrollOffset--;
                                            rebuildButtons();
                                        }
                                    })
                            .bounds(scrollBtnX, scrollBtnY, 16, 16)
                            .build()
            );

            this.addRenderableWidget(
                    Button.builder(Component.literal("▼"),
                                    btn -> {
                                        int maxScroll = Math.max(0, prayers.size() - visibleCount);
                                        if (prayerScrollOffset < maxScroll) {
                                            prayerScrollOffset++;
                                            rebuildButtons();
                                        }
                                    })
                            .bounds(scrollBtnX, scrollBtnY + listHeight - 16, 16, 16)
                            .build()
            );
        }
    }

    private void respondToPrayer(int index, boolean teleport) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        if (index >= 0 && index < prayers.size()) {
            PendingPrayer prayer = prayers.get(index);
            PacketHandler.sendToServer(new HonorificNamesRespondPacket(prayer.senderUUID(), teleport));
            // Remove immediately client-side so the UI updates without waiting for a server round-trip
            prayers.remove(index);
            // Clamp scroll offset so it stays within valid bounds
            int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
            int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);
            prayerScrollOffset = Math.min(prayerScrollOffset, Math.max(0, prayers.size() - visibleCount));
            rebuildButtons();
        }
    }

    // ── Rendering ──────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // ── Outer shadow / glow border ──
        gfx.fill(x - 2, y - 2, x + W + 2, y + H + 2, COL_BORDER_LO);

        // ── Main background ──
        gfx.fillGradient(x, y, x + W, y + H, COL_BG_OUTER, COL_BG_INNER);

        // ── Inner decorative border (1px inset, bright purple) ──
        // Top edge
        gfx.fill(x + 2, y + 2,     x + W - 2, y + 3,     COL_BORDER_HI);
        // Bottom edge
        gfx.fill(x + 2, y + H - 3, x + W - 2, y + H - 2, COL_BORDER_HI);
        // Left edge
        gfx.fill(x + 2, y + 2,     x + 3,     y + H - 2, COL_BORDER_HI);
        // Right edge
        gfx.fill(x + W - 3, y + 2, x + W - 2, y + H - 2, COL_BORDER_HI);

        // ── Title bar gradient ──
        gfx.fillGradient(x + 3, y + 3, x + W - 3, y + TITLE_H,
                0xFF1E0A3C, 0xFF0D0D1A);

        // ── Title underline ──
        gfx.fill(x + PADDING, y + TITLE_H, x + W - PADDING, y + TITLE_H + 1, COL_BORDER_HI);

        // ── Own-name section background ──
        gfx.fill(x + PADDING - 2, y + TITLE_H + 4,
                 x + W - PADDING + 2, y + TITLE_H + OWN_NAME_H + 2,
                 0xFF1A1A2E);
        // Own-name section top border
        gfx.fill(x + PADDING - 2, y + TITLE_H + 4,
                 x + W - PADDING + 2, y + TITLE_H + 5,
                 COL_DIVIDER);

        // ── Divider between own-name and prayer list ──
        int dividerY = y + TITLE_H + OWN_NAME_H + DIVIDER_H / 2;
        gfx.fillGradient(x + PADDING, dividerY, x + W - PADDING, dividerY + 1,
                COL_BORDER_LO, COL_BORDER_HI);
        gfx.fillGradient(x + PADDING, dividerY + 1, x + W - PADDING, dividerY + 2,
                COL_BORDER_HI, COL_BORDER_LO);

        // ── Prayer cards ──
        renderPrayerCards(gfx, x, y);
    }

    private void renderPrayerCards(GuiGraphics gfx, int x, int y) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int listTop = listTop();
        int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
        int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);

        for (int i = 0; i < visibleCount; i++) {
            int idx = i + prayerScrollOffset;
            if (idx >= prayers.size()) break;

            int cardY = listTop + i * PRAYER_ENTRY_H;

            // Card background
            gfx.fill(x + PADDING - 2, cardY - 2,
                     x + W - PADDING + 2, cardY + PRAYER_ENTRY_H - 4,
                     COL_PRAYER_BRD);
            gfx.fill(x + PADDING - 1, cardY - 1,
                     x + W - PADDING + 1, cardY + PRAYER_ENTRY_H - 5,
                     COL_PRAYER_BG);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // NOTE: coordinates here are relative to (leftPos, topPos)

        // ── Title ──
        String titleStr = "✦  Honorific Names  ✦";
        int titleX = (W - font.width(titleStr)) / 2;
        gfx.drawString(font, titleStr, titleX, 7, COL_TITLE, false);

        // ── Own name section ──
        renderOwnNameSection(gfx);

        // ── Pending prayers section ──
        renderPendingPrayersSection(gfx);
    }

    private void renderOwnNameSection(GuiGraphics gfx) {
        int sectionY = TITLE_H + 6;
        gfx.drawString(font, "Your Honorific Name:", PADDING, sectionY, COL_LABEL, false);

        HonorificName name = menu.getOwnName();
        if (name.isEmpty()) {
            String hint = "Not set — use /honorificname set {...}";
            gfx.drawString(font, hint, PADDING + 2, sectionY + 12, 0xFF666688, false);
        } else {
            int lineY = sectionY + 13;
            List<String> lines = name.lines();
            for (int i = 0; i < lines.size(); i++) {
                String lineLabel = "  Line " + (i + 1) + ":  ";
                gfx.drawString(font, lineLabel, PADDING + 2, lineY, COL_LABEL, false);
                gfx.drawString(font, lines.get(i), PADDING + 2 + font.width(lineLabel), lineY, COL_TEXT, false);
                lineY += 11;
            }
        }

        // Pathway / sequence badge at the right of the section
        String badge = menu.getPathway() + "  •  Seq " + menu.getSequence();
        int badgeX = W - PADDING - font.width(badge) - 2;
        gfx.drawString(font, badge, badgeX, TITLE_H + 6, 0xFF7777AA, false);
    }

    private void renderPendingPrayersSection(GuiGraphics gfx) {
        int sectionLabelY = TITLE_H + OWN_NAME_H + DIVIDER_H;
        gfx.drawString(font, "Pending Prayers:", PADDING, sectionLabelY, COL_LABEL, false);

        List<PendingPrayer> prayers = menu.getPendingPrayers();

        if (prayers.isEmpty()) {
            gfx.drawString(font, "No pending prayers.", PADDING + 4, sectionLabelY + 12, 0xFF555577, false);
            return;
        }

        int listTop = listTop() - topPos; // relative Y
        int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
        int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);

        for (int i = 0; i < visibleCount; i++) {
            int idx = i + prayerScrollOffset;
            if (idx >= prayers.size()) break;

            PendingPrayer prayer = prayers.get(idx);
            int entryRelY = listTop + i * PRAYER_ENTRY_H;

            // Name & info
            String nameLine = prayer.senderName()
                    + "  [" + prayer.senderPathway() + " / Seq " + prayer.senderSequence() + "]";
            gfx.drawString(font, nameLine, PADDING, entryRelY + 2, 0xFFFFCC55, false);

            // Coordinates
            String coordLine = String.format("@ %.0f, %.0f, %.0f",
                    prayer.x(), prayer.y(), prayer.z());
            gfx.drawString(font, coordLine, PADDING, entryRelY + 12, 0xFF8888AA, false);
            // (the two buttons are rendered by rebuildButtons)
        }

        // Scroll indicator
        if (prayers.size() > visibleCount) {
            String indicator = (prayerScrollOffset + 1) + " / " + prayers.size();
            gfx.drawString(font, indicator,
                    W - PADDING - 16 - font.width(indicator) - 2,
                    sectionLabelY, 0xFF666688, false);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /** Absolute Y-coordinate of the top of the prayer list. */
    private int listTop() {
        return topPos + TITLE_H + OWN_NAME_H + DIVIDER_H + 11;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
        int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);
        int maxScroll = Math.max(0, prayers.size() - visibleCount);

        if (scrollY < 0 && prayerScrollOffset < maxScroll) {
            prayerScrollOffset++;
            rebuildButtons();
            return true;
        } else if (scrollY > 0 && prayerScrollOffset > 0) {
            prayerScrollOffset--;
            rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}

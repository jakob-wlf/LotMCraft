package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncTargetEnvisionStatusPacket;
import de.jakob.lotm.network.packets.toServer.RequestTargetStatusActionPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Envisioning > Target > Status
 * Shows 2 target snapshot slots. Save captures an online player's state.
 * Restore applies it to them for 5 min (1 min if Sefirot/GOO). 3h restore cooldown.
 */
@OnlyIn(Dist.CLIENT)
public class TargetStatusScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W  = 270;
    private static final int SLOT_H   = 90;  // tall enough to keep info and controls in separate rows
    private static final int SLOT_GAP = 6;
    private static final int HEADER_H = 36;
    private static final int FOOTER_H = 20;
    private static final int PAD      = 10;

    // ── Dropdown ──────────────────────────────────────────────────────────────
    private static final int DROP_ROW_H   = 12;
    private static final int DROP_ROW_GAP = 1;
    private static final int DROP_MAX_VIS = 5;

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_BG          = 0xFF1A1E2A;
    private static final int C_OUTLINE     = 0xFFAA8833;
    private static final int C_TITLE       = 0xFFFFCC55;
    private static final int C_LABEL       = 0xFFCCDDFF;
    private static final int C_HINT        = 0xFF99AACC;
    private static final int C_EMPTY       = 0xFF445566;
    private static final int C_SLOT_BG     = 0xFF141824;
    private static final int C_ACTIVE_BG   = 0xFF1A3028;
    private static final int C_SEL_BG      = 0xFF22263A;
    private static final int C_SEL_BD      = 0xFF5060A0;
    private static final int C_DROP_BG     = 0xFF14172A;
    private static final int C_DROP_BD     = 0xFF6070B0;
    private static final int C_DROP_HOVER  = 0x33FFFFFF;
    private static final int C_DROP_SEL    = 0xFF2A3060;
    private static final int C_PLAYER_TEXT = 0xFFEEEEFF;

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<String> players = new ArrayList<>();
    private String selectedPlayer = null;
    private boolean dropdownOpen = false;
    private int dropScrollOffset = 0;

    // Active save-target dropdown (which slot is it open for?)
    private int activeDropdownSlot = -1;

    // Dropdown selector rects per slot (set in init)
    private int[] selX = new int[2], selY = new int[2], selW = new int[2], selH = new int[2];

    // Cancel button — shown only when an active restore exists
    private Button cancelBtn;

    public TargetStatusScreen() {
        super(Component.literal("Envisioning – Target Status"));
    }

    @Override
    protected void init() {
        super.init();
        refreshPlayers();
        PacketHandler.sendToServer(new RequestTargetStatusActionPacket("SYNC", -1, ""));

        int lx = lx(), ty = ty();
        int slotX = lx + PAD;
        int slotW = PANEL_W - PAD * 2;

        for (int i = 0; i < 2; i++) {
            final int slotIdx = i;
            int sy = ty + HEADER_H + i * (SLOT_H + SLOT_GAP);

            // Control row is in the bottom portion of the slot (after the info area)
            // selY = sy + 68 → "Save for:" label at sy + 60, buttons alongside selector
            selX[i] = slotX + 4;
            selY[i] = sy + SLOT_H - 18;  // = sy + 72 for SLOT_H=90
            selW[i] = slotW - 96;
            selH[i] = 12;

            // Save button
            addRenderableWidget(Button.builder(Component.literal("Save"), btn -> onSave(slotIdx))
                    .bounds(slotX + slotW - 90, sy + SLOT_H - 18, 40, 12)
                    .build());

            // Restore button
            addRenderableWidget(Button.builder(Component.literal("Restore"), btn -> onRestore(slotIdx))
                    .bounds(slotX + slotW - 46, sy + SLOT_H - 18, 42, 12)
                    .build());
        }

        // Cancel button — hidden until an active restore is detected
        cancelBtn = Button.builder(Component.literal("Cancel Restore"),
                        btn -> PacketHandler.sendToServer(new RequestTargetStatusActionPacket("CANCEL", -1, "")))
                .bounds(lx + PAD + 110, ty + panelH() - FOOTER_H + 2, 80, 12)
                .build();
        cancelBtn.visible = false;
        addRenderableWidget(cancelBtn);

        addRenderableWidget(Button.builder(Component.literal("< Back"), btn -> onClose())
                .bounds(lx + 6, ty + 6, 40, 10)
                .build());
    }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x55000000);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        int lx = lx(), ty = ty();

        g.fill(lx, ty, lx + PANEL_W, ty + panelH(), C_BG);
        drawOutline(g, lx, ty, PANEL_W, panelH(), C_OUTLINE);
        g.drawCenteredString(font, "Target Status", lx + PANEL_W / 2, ty + 8, C_TITLE);
        g.drawCenteredString(font, "Store & impose target state (5 min / 1 min for GOO/Sefirot)",
                lx + PANEL_W / 2, ty + 19, C_HINT);
        g.fill(lx + PAD, ty + HEADER_H - 4, lx + PANEL_W - PAD, ty + HEADER_H - 3, C_OUTLINE);

        SyncTargetEnvisionStatusPacket cache = SyncTargetEnvisionStatusPacket.CLIENT_CACHE;

        // Update cancel button visibility
        if (cancelBtn != null) {
            boolean anyActive = cache != null && cache.slots().stream()
                    .anyMatch(s -> !s.isEmpty() && s.targetIsBeingRestored());
            cancelBtn.visible = anyActive;
        }

        int slotX = lx + PAD;
        int slotW = PANEL_W - PAD * 2;
        for (int i = 0; i < 2; i++) {
            int sy = ty + HEADER_H + i * (SLOT_H + SLOT_GAP);
            SyncTargetEnvisionStatusPacket.TargetSlotInfo info =
                    (cache != null && i < cache.slots().size()) ? cache.slots().get(i) : null;
            renderSlot(g, slotX, sy, slotW, i, info);
        }

        if (cache != null && cache.restoreCooldownRemainingMs() > 0) {
            long secs = cache.restoreCooldownRemainingMs() / 1000;
            g.drawString(font, "§cRestore cooldown: " + formatTime(secs),
                    lx + PAD, ty + panelH() - FOOTER_H + 4, 0xFFFF9999, false);
        }

        super.render(g, mx, my, pt);

        // Render dropdowns last (float above buttons)
        for (int i = 0; i < 2; i++) {
            if (activeDropdownSlot == i) renderDropdown(g, i, mx, my);
            renderSelectorBox(g, i, mx, my);
        }
    }

    private void renderSlot(GuiGraphics g, int sx, int sy, int sw, int idx,
                             SyncTargetEnvisionStatusPacket.TargetSlotInfo info) {
        boolean active = info != null && !info.isEmpty() && info.targetIsBeingRestored();
        g.fill(sx, sy, sx + sw, sy + SLOT_H, active ? C_ACTIVE_BG : C_SLOT_BG);
        drawOutline(g, sx, sy, sw, SLOT_H, active ? 0xFF227744 : 0xFF334466);
        g.drawString(font, "§eSlot " + (idx + 1), sx + 4, sy + 4, C_LABEL, false);

        if (info == null || info.isEmpty()) {
            g.drawString(font, "§8— empty —", sx + 4, sy + 16, C_EMPTY, false);
        } else {
            String seqName = BeyonderData.getSequenceName(info.pathway(), info.sequence());
            String pw = capitalize(info.pathway().replace('_', ' '));
            g.drawString(font, "§7Target: §f" + info.targetName(), sx + 4, sy + 16, 0xFFFFFFFF, false);
            g.drawString(font, "§f" + pw + " §7" + seqName,         sx + 4, sy + 26, 0xFFFFFFFF, false);
            if (info.hasUniqueness()) {
                g.drawString(font, "§aUniq: §f" + capitalize(info.uniquenessPathway().replace('_', ' ')),
                        sx + 4, sy + 36, 0xFFFFFFFF, false);
            }
            g.drawString(font, "§7Fx: " + info.effectCount() + "  •  " + timeAgo(info.captureTimeMs()),
                    sx + 4, sy + 46, C_HINT, false);
            if (active) {
                g.drawString(font, "§a● Active restore", sx + 4, sy + 56, 0xFF55FF55, false);
            } else if (info.cooldownRemainingMs() > 0) {
                g.drawString(font, "§7Save locked: " + formatTime(info.cooldownRemainingMs() / 1000),
                        sx + 4, sy + 56, C_HINT, false);
            }
        }
        // "Save for:" label — placed just above the control row, well below the info area
        g.drawString(font, "§7Save for:", sx + 4, selY[idx] - 10, C_HINT, false);
    }

    private void renderSelectorBox(GuiGraphics g, int i, int mx, int my) {
        boolean hov  = isOverSel(i, mx, my);
        boolean open = activeDropdownSlot == i;
        int bd = open ? C_OUTLINE : (hov ? 0xFF8090C0 : C_SEL_BD);
        g.fill(selX[i], selY[i], selX[i] + selW[i], selY[i] + selH[i], C_SEL_BG);
        drawOutline(g, selX[i], selY[i], selW[i], selH[i], bd);
        String disp = selectedPlayer != null && activeDropdownSlot == i
                ? selectedPlayer
                : (selectedPlayer != null ? selectedPlayer : "— player —");
        g.drawString(font, disp, selX[i] + 3, selY[i] + 2, C_PLAYER_TEXT, false);
        g.drawString(font, open ? "▲" : "▼", selX[i] + selW[i] - 10, selY[i] + 2, C_HINT, false);
    }

    private void renderDropdown(GuiGraphics g, int i, int mx, int my) {
        if (players.isEmpty()) return;
        int vis   = Math.min(DROP_MAX_VIS, players.size());
        int dropH = vis * (DROP_ROW_H + DROP_ROW_GAP);
        int dropY = selY[i] + selH[i] + 1;

        g.fill(selX[i], dropY, selX[i] + selW[i], dropY + dropH, C_DROP_BG);
        drawOutline(g, selX[i], dropY, selW[i], dropH, C_DROP_BD);

        for (int j = 0; j < vis; j++) {
            int idx = dropScrollOffset + j;
            if (idx >= players.size()) break;
            String name = players.get(idx);
            int rowY = dropY + j * (DROP_ROW_H + DROP_ROW_GAP);
            boolean rowHov = mx >= selX[i] && mx < selX[i] + selW[i]
                          && my >= rowY && my < rowY + DROP_ROW_H;
            if (name.equals(selectedPlayer)) g.fill(selX[i], rowY, selX[i] + selW[i], rowY + DROP_ROW_H, C_DROP_SEL);
            if (rowHov) g.fill(selX[i], rowY, selX[i] + selW[i], rowY + DROP_ROW_H, C_DROP_HOVER);
            g.drawString(font, name, selX[i] + 4, rowY + 2, C_PLAYER_TEXT, false);
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) {
            if (activeDropdownSlot >= 0) { activeDropdownSlot = -1; return true; }
            return super.mouseClicked(mx, my, btn);
        }

        for (int i = 0; i < 2; i++) {
            if (isOverSel(i, (int) mx, (int) my)) {
                activeDropdownSlot = (activeDropdownSlot == i) ? -1 : i;
                if (activeDropdownSlot >= 0) refreshPlayers();
                return true;
            }
        }

        if (activeDropdownSlot >= 0) {
            int i = activeDropdownSlot;
            int vis   = Math.min(DROP_MAX_VIS, players.size());
            int dropY = selY[i] + selH[i] + 1;
            int bot   = dropY + vis * (DROP_ROW_H + DROP_ROW_GAP);
            if (mx >= selX[i] && mx < selX[i] + selW[i] && my >= dropY && my < bot) {
                int row = (int) (my - dropY) / (DROP_ROW_H + DROP_ROW_GAP);
                int idx = dropScrollOffset + row;
                if (idx >= 0 && idx < players.size()) selectedPlayer = players.get(idx);
            }
            activeDropdownSlot = -1;
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (activeDropdownSlot >= 0) {
            int max = Math.max(0, players.size() - DROP_MAX_VIS);
            dropScrollOffset = (int) Math.max(0, Math.min(max, dropScrollOffset - dy));
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void onSave(int slot) {
        if (selectedPlayer == null) return;
        PacketHandler.sendToServer(new RequestTargetStatusActionPacket("SAVE", slot, selectedPlayer));
    }

    private void onRestore(int slot) {
        PacketHandler.sendToServer(new RequestTargetStatusActionPacket("RESTORE", slot, ""));
    }

    @Override
    public void onClose() { minecraft.setScreen(new EnvisioningScreen()); }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private int lx()     { return (width  - PANEL_W) / 2; }
    private int ty()     { return (height - panelH()) / 2; }
    private int panelH() { return HEADER_H + 2 * SLOT_H + 1 * SLOT_GAP + FOOTER_H + PAD; }

    private boolean isOverSel(int i, int mx, int my) {
        return mx >= selX[i] && mx < selX[i] + selW[i] && my >= selY[i] && my < selY[i] + selH[i];
    }

    private void refreshPlayers() {
        players.clear();
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            conn.getOnlinePlayers().stream()
                    .map(p -> p.getProfile().getName())
                    .filter(n -> n != null && !n.isEmpty())
                    .sorted(Comparator.naturalOrder())
                    .forEach(players::add);
        }
        dropScrollOffset = 0;
    }

    private static void drawOutline(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,         y,         x + w,     y + 1,     c);
        g.fill(x,         y + h - 1, x + w,     y + h,     c);
        g.fill(x,         y,         x + 1,     y + h,     c);
        g.fill(x + w - 1, y,         x + w,     y + h,     c);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        for (String w : s.split(" ")) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private static String formatTime(long secs) {
        long h = secs / 3600, m = (secs % 3600) / 60, sec = secs % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + sec + "s";
        return sec + "s";
    }

    private static String timeAgo(long epochMs) {
        long secs = (System.currentTimeMillis() - epochMs) / 1000;
        if (secs < 60) return secs + "s ago";
        long mins = secs / 60;
        if (mins < 60) return mins + "m ago";
        return (mins / 60) + "h ago";
    }
}

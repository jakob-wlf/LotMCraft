package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEnvisionStatusPacket;
import de.jakob.lotm.network.packets.toServer.RequestSelfStatusActionPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Envisioning > Self > Status
 * Shows 3 snapshot slots. Each slot can save the player's current state
 * and restore it (active for 30 min, then reverts). Overwrite cooldown: 3h.
 */
@OnlyIn(Dist.CLIENT)
public class SelfStatusScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W  = 270;
    private static final int SLOT_H   = 58;
    private static final int SLOT_GAP = 6;
    private static final int HEADER_H = 36;
    private static final int FOOTER_H = 20;
    private static final int PAD      = 10;
    private static final int COMPUTED_PANEL_H =
            HEADER_H + 3 * SLOT_H + 2 * SLOT_GAP + FOOTER_H + PAD * 2;

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_BG       = 0xFF1A1E2A;
    private static final int C_OUTLINE  = 0xFFAA8833;
    private static final int C_TITLE    = 0xFFFFCC55;
    private static final int C_LABEL    = 0xFFCCDDFF;
    private static final int C_HINT     = 0xFF99AACC;
    private static final int C_EMPTY    = 0xFF445566;
    private static final int C_SLOT_BG  = 0xFF141824;
    private static final int C_ACTIVE   = 0xFF1A3028;
    private static final int C_BLOCKED  = 0xFF301818;

    public SelfStatusScreen() {
        super(Component.literal("Envisioning – Self Status"));
    }

    // Cancel button reference — visibility toggled each frame
    private Button cancelBtn;

    @Override
    protected void init() {
        super.init();
        // Request a sync immediately when the screen opens
        PacketHandler.sendToServer(new RequestSelfStatusActionPacket("SYNC", -1));

        int lx = lx(), ty = ty();
        int slotX = lx + PAD;
        int slotW = PANEL_W - PAD * 2;

        for (int i = 0; i < 3; i++) {
            final int slotIdx = i;
            int sy = ty + HEADER_H + i * (SLOT_H + SLOT_GAP);

            // Save button (right side of slot)
            addRenderableWidget(Button.builder(Component.literal("Save"), btn -> onSave(slotIdx))
                    .bounds(slotX + slotW - 90, sy + SLOT_H - 16, 40, 12)
                    .build());

            // Restore button
            addRenderableWidget(Button.builder(Component.literal("Restore"), btn -> onRestore(slotIdx))
                    .bounds(slotX + slotW - 46, sy + SLOT_H - 16, 42, 12)
                    .build());
        }

        // Cancel Restore button — shown only when a restore is active
        cancelBtn = Button.builder(Component.literal("Cancel Restore"),
                        btn -> PacketHandler.sendToServer(new RequestSelfStatusActionPacket("CANCEL", -1)))
                .bounds(lx + PAD + 130, ty + panelH() - FOOTER_H + 2, 80, 12)
                .build();
        cancelBtn.visible = false;
        addRenderableWidget(cancelBtn);

        // Back button
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

        g.drawCenteredString(font, "Self Status", lx + PANEL_W / 2, ty + 8, C_TITLE);
        g.drawCenteredString(font, "Store & restore your beyonder state (30 min)",
                lx + PANEL_W / 2, ty + 19, C_HINT);
        g.fill(lx + PAD, ty + HEADER_H - 4, lx + PANEL_W - PAD, ty + HEADER_H - 3, C_OUTLINE);

        SyncEnvisionStatusPacket cache = SyncEnvisionStatusPacket.CLIENT_CACHE;
        List<SyncEnvisionStatusPacket.SlotInfo> slots =
                cache != null ? cache.slots() : null;

        // Update cancel button visibility
        if (cancelBtn != null) cancelBtn.visible = cache != null && cache.hasActiveRestore();

        int slotX = lx + PAD;
        int slotW = PANEL_W - PAD * 2;

        for (int i = 0; i < 3; i++) {
            int sy = ty + HEADER_H + i * (SLOT_H + SLOT_GAP);
            SyncEnvisionStatusPacket.SlotInfo info =
                    (slots != null && i < slots.size()) ? slots.get(i) : null;
            renderSlot(g, slotX, sy, slotW, i, info, cache);
        }

        // Active restore banner
        if (cache != null && cache.hasActiveRestore()) {
            long secsLeft = Math.max(0, (cache.restoreExpiryMs() - System.currentTimeMillis()) / 1000);
            String msg = "§bActive restore — reverts in " + formatTime(secsLeft);
            g.drawString(font, msg, lx + PAD, ty + panelH() - FOOTER_H + 4, 0xFFFFFFFF, false);
        }

        super.render(g, mx, my, pt);
    }

    private void renderSlot(GuiGraphics g, int sx, int sy, int sw, int idx,
                             SyncEnvisionStatusPacket.SlotInfo info,
                             SyncEnvisionStatusPacket cache) {
        boolean active   = cache != null && cache.hasActiveRestore() && !isEmptyInfo(info);
        boolean blocked  = info != null && !info.isEmpty() && info.blockedByDeath();
        int bg = blocked ? C_BLOCKED : (active && idx == activeSlotIndex(cache) ? C_ACTIVE : C_SLOT_BG);

        g.fill(sx, sy, sx + sw, sy + SLOT_H, bg);
        drawOutline(g, sx, sy, sw, SLOT_H, blocked ? 0xFF662222 : 0xFF334466);

        g.drawString(font, "§eSlot " + (idx + 1), sx + 4, sy + 4, C_LABEL, false);

        if (info == null || info.isEmpty()) {
            g.drawString(font, "§8— empty —", sx + 4, sy + 16, C_EMPTY, false);
        } else {
            String seqName = BeyonderData.getSequenceName(info.pathway(), info.sequence());
            String pw = capitalize(info.pathway().replace('_', ' '));
            g.drawString(font, "§f" + pw + " §7" + seqName,   sx + 4, sy + 16, 0xFFFFFFFF, false);
            if (info.hasUniqueness()) {
                String un = capitalize(info.uniquenessPathway().replace('_', ' '));
                g.drawString(font, "§aUniqueness: §f" + un, sx + 4, sy + 26, 0xFFFFFFFF, false);
            }
            g.drawString(font, "§7Effects: " + info.effectCount() +
                    "  •  Saved: " + timeAgo(info.captureTimeMs()),
                    sx + 4, sy + 36, C_HINT, false);

            if (blocked) {
                g.drawString(font, "§cBlocked — died after save", sx + 4, sy + 46, 0xFFFF5555, false);
            } else if (info.cooldownRemainingMs() > 0) {
                g.drawString(font, "§7Save locked: " + formatTime(info.cooldownRemainingMs() / 1000),
                        sx + 4, sy + 46, C_HINT, false);
            }
        }
    }

    private boolean isEmptyInfo(SyncEnvisionStatusPacket.SlotInfo info) {
        return info == null || info.isEmpty();
    }

    private int activeSlotIndex(SyncEnvisionStatusPacket cache) {
        // We don't track which slot is active server-side in the sync packet,
        // so just highlight all non-empty slots if restore is active.
        return -1;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void onSave(int slot) {
        PacketHandler.sendToServer(new RequestSelfStatusActionPacket("SAVE", slot));
    }

    private void onRestore(int slot) {
        PacketHandler.sendToServer(new RequestSelfStatusActionPacket("RESTORE", slot));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new EnvisioningScreen());
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private int lx()     { return (width  - PANEL_W) / 2; }
    private int ty()     { return (height - panelH()) / 2; }
    private int panelH() { return COMPUTED_PANEL_H; }

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
        long h = secs / 3600, m = (secs % 3600) / 60, s = secs % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    private static String timeAgo(long epochMs) {
        long secs = (System.currentTimeMillis() - epochMs) / 1000;
        if (secs < 60) return secs + "s ago";
        long mins = secs / 60;
        if (mins < 60) return mins + "m ago";
        return (mins / 60) + "h ago";
    }
}

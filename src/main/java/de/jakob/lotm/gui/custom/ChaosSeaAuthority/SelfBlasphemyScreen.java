package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.attachments.SummonedBlasphemyData;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSummonedBlasphemyPacket;
import de.jakob.lotm.network.packets.toServer.RequestSummonBlasphemyPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;

/**
 * Envisioning > Self > Blasphemy card picker.
 * Shows all 16 pathways in a 4 × 4 grid.
 *
 * State colour legend:
 *   Gold   – card is currently summoned (shows uses remaining)
 *   Red    – card is not summoned AND the 3-card limit is already reached
 *   Silver – card is available to summon
 *
 * Clicking a summoned card dismisses it.
 * Clicking an available card summons it (if limit not yet reached).
 * Clicking when at limit shows the limit message from the server.
 */
@OnlyIn(Dist.CLIENT)
public class SelfBlasphemyScreen extends Screen {

    // ── Pathway list (matches BlasphemyCommand order) ─────────────────────────
    private static final List<String> PATHWAYS = List.of(
            "fool", "door", "error", "sun",
            "tyrant", "visionary", "darkness", "death",
            "twilight_giant", "demoness", "red_priest", "mother",
            "abyss", "wheel_of_fortune", "black_emperor", "justiciar"
    );

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int COLS      = 2;
    private static final int ROWS      = 8;  // 16 pathways / 2 cols
    private static final int CELL_W    = 110; // wide enough for "Wheel Of Fortune"
    private static final int CELL_H    = 28;
    private static final int CELL_GAP  = 3;
    private static final int HEADER_H  = 30;
    private static final int PAD       = 10;

    private static final int PANEL_W =
            PAD * 2 + COLS * CELL_W + (COLS - 1) * CELL_GAP;
    private static final int PANEL_H =
            HEADER_H + ROWS * CELL_H + (ROWS - 1) * CELL_GAP + PAD + 14; // +14 for back btn

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_BG            = 0xFF1A1E2A;
    private static final int C_OUTLINE       = 0xFFAA8833;
    private static final int C_TITLE         = 0xFFFFCC55;
    private static final int C_HINT          = 0xFF99AACC;
    private static final int C_CELL_IDLE     = 0xFF2A2E3C;
    private static final int C_CELL_SUMMONED = 0xFF3A3010;
    private static final int C_CELL_LOCKED   = 0xFF2A1A1A;
    private static final int C_BORDER_SUMMON = 0xFFFFCC44;
    private static final int C_BORDER_IDLE   = 0xFF5060A0;
    private static final int C_BORDER_LOCKED = 0xFF602020;
    private static final int C_TEXT_SUMMON   = 0xFFFFDD66;
    private static final int C_TEXT_IDLE     = 0xFFCCDDFF;
    private static final int C_TEXT_LOCKED   = 0xFF996666;
    private static final int C_USES          = 0xFF88FFAA;

    // ── Local optimistic state ────────────────────────────────────────────────
    /** Local copy of the server state, updated when sync arrives or optimistically on click. */
    private final java.util.LinkedHashMap<String, Integer> localState = new java.util.LinkedHashMap<>();

    public SelfBlasphemyScreen() {
        super(Component.literal("Envisioning – Blasphemy"));
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        // Copy latest server-synced state
        localState.clear();
        localState.putAll(SyncSummonedBlasphemyPacket.CLIENT_CACHE);

        // Request fresh sync from server
        PacketHandler.sendToServer(new RequestSummonBlasphemyPacket(""));

        int lx = lx();
        int ty = ty();
        addRenderableWidget(Button.builder(Component.literal("< Back"), btn -> onClose())
                .bounds(lx + 6, ty + 6, 40, 10)
                .build());
    }

    // ── Rendering ─────────────────────────────────────────────────────────────
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x55000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Refresh local state from cache each frame so sync updates are visible
        Map<String, Integer> cache = SyncSummonedBlasphemyPacket.CLIENT_CACHE;
        if (!cache.equals(localState)) {
            localState.clear();
            localState.putAll(cache);
        }

        renderBackground(g, mouseX, mouseY, partialTick);

        int lx = lx();
        int ty = ty();

        // Panel
        g.fill(lx, ty, lx + PANEL_W, ty + PANEL_H, C_BG);
        drawOutline(g, lx, ty, PANEL_W, PANEL_H, C_OUTLINE);

        // Title
        g.drawCenteredString(font, "Blasphemy Cards", lx + PANEL_W / 2, ty + 8, C_TITLE);

        // Summoned count
        int active = localState.size();
        String countText = active + " / " + SummonedBlasphemyData.MAX_CARDS + " summoned";
        int countColor = active >= SummonedBlasphemyData.MAX_CARDS ? 0xFFFF8866 : C_HINT;
        g.drawCenteredString(font, countText, lx + PANEL_W / 2, ty + 19, countColor);

        // Divider
        g.fill(lx + PAD, ty + HEADER_H - 2, lx + PANEL_W - PAD, ty + HEADER_H - 1, C_OUTLINE);

        // Card grid
        boolean limitReached = active >= SummonedBlasphemyData.MAX_CARDS;
        for (int i = 0; i < PATHWAYS.size(); i++) {
            String pathway = PATHWAYS.get(i);
            int col = i % COLS;
            int row = i / COLS;
            int cx = lx + PAD + col * (CELL_W + CELL_GAP);
            int cy = ty + HEADER_H + row * (CELL_H + CELL_GAP);

            boolean summoned = localState.containsKey(pathway);
            boolean locked   = !summoned && limitReached;
            boolean hovered  = mouseX >= cx && mouseX < cx + CELL_W
                            && mouseY >= cy && mouseY < cy + CELL_H;

            int bgColor     = summoned ? C_CELL_SUMMONED : locked ? C_CELL_LOCKED : C_CELL_IDLE;
            int borderColor = summoned ? C_BORDER_SUMMON : locked ? C_BORDER_LOCKED : C_BORDER_IDLE;
            int textColor   = summoned ? C_TEXT_SUMMON   : locked ? C_TEXT_LOCKED   : C_TEXT_IDLE;

            if (hovered && !locked) bgColor = brighten(bgColor);

            g.fill(cx, cy, cx + CELL_W, cy + CELL_H, bgColor);
            drawOutline(g, cx, cy, CELL_W, CELL_H, borderColor);

            String label = prettify(pathway);

            int textX = cx + CELL_W / 2;
            if (summoned) {
                // Name on top half, uses on bottom half
                int uses = localState.get(pathway);
                String usesStr = uses + " use" + (uses != 1 ? "s" : "") + " left";
                g.drawCenteredString(font, label,   textX, cy + 5,           textColor);
                g.drawCenteredString(font, usesStr, textX, cy + CELL_H - 13, C_USES);
            } else {
                // Single centred name — always fits (CELL_W=110 > longest label)
                g.drawCenteredString(font, label, textX, cy + (CELL_H - 8) / 2, textColor);
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);

        int lx = lx();
        int ty = ty();
        boolean limitReached = localState.size() >= SummonedBlasphemyData.MAX_CARDS;

        for (int i = 0; i < PATHWAYS.size(); i++) {
            String pathway = PATHWAYS.get(i);
            int col = i % COLS;
            int row = i / COLS;
            int cx = lx + PAD + col * (CELL_W + CELL_GAP);
            int cy = ty + HEADER_H + row * (CELL_H + CELL_GAP);

            if (mx >= cx && mx < cx + CELL_W && my >= cy && my < cy + CELL_H) {
                boolean summoned = localState.containsKey(pathway);

                if (summoned) {
                    // Dismiss optimistically
                    localState.remove(pathway);
                } else if (!limitReached) {
                    // Summon optimistically
                    localState.put(pathway, SummonedBlasphemyData.MAX_USES);
                }
                // Send to server (server will correct if needed)
                PacketHandler.sendToServer(new RequestSummonBlasphemyPacket(pathway));
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new EnvisioningScreen());
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private int lx() { return (width  - PANEL_W) / 2; }
    private int ty() { return (height - PANEL_H) / 2; }

    private static String prettify(String pathway) {
        String[] words = pathway.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private static int brighten(int color) {
        int a =  (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + 25);
        int g = Math.min(255, ((color >>  8) & 0xFF) + 25);
        int b = Math.min(255,  (color        & 0xFF) + 25);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void drawOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,         x + w,     y + 1,     color);
        g.fill(x,         y + h - 1, x + w,     y + h,     color);
        g.fill(x,         y,         x + 1,     y + h,     color);
        g.fill(x + w - 1, y,         x + w,     y + h,     color);
    }
}

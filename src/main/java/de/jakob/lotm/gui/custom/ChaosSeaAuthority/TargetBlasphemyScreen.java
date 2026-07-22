package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEnvisionTriggerPacket;
import de.jakob.lotm.network.packets.toServer.RequestEnvisionBlasphemyPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Envisioning > Target > Blasphemy.
 *
 * Choose a target player from a dropdown, then either:
 *  - "Invoke Now"  – fires the LEODERO effect on that player immediately.
 *  - "Set Trigger" – stores a chat trigger word; when you say it the effect fires.
 */
@OnlyIn(Dist.CLIENT)
public class TargetBlasphemyScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W      = 250;
    private static final int PANEL_H      = 170;
    private static final int DROP_ROW_H   = 12;
    private static final int DROP_ROW_GAP = 1;
    private static final int DROP_MAX_VIS = 5;

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_BG          = 0xFF1A1E2A;
    private static final int C_OUTLINE     = 0xFFAA8833;
    private static final int C_TITLE       = 0xFFFFCC55;
    private static final int C_LABEL       = 0xFFCCDDFF;
    private static final int C_HINT        = 0xFF99AACC;
    private static final int C_SEL_BG      = 0xFF22263A;
    private static final int C_SEL_BD      = 0xFF5060A0;
    private static final int C_DROP_BG     = 0xFF14172A;
    private static final int C_DROP_BD     = 0xFF6070B0;
    private static final int C_DROP_HOVER  = 0x33FFFFFF;
    private static final int C_DROP_SEL    = 0xFF2A3060;
    private static final int C_PLAYER_TEXT = 0xFFEEEEFF;
    private static final int C_EMPTY       = 0xFF6677AA;
    private static final int C_INVOKE_BG   = 0xFF2A1010;
    private static final int C_INVOKE_BD   = 0xFF882222;
    private static final int C_INVOKE_TXT  = 0xFFFF6655;

    // ── State ─────────────────────────────────────────────────────────────────
    private List<String> players    = new ArrayList<>();
    private String selectedPlayer   = null;
    private boolean dropdownOpen    = false;
    private int dropScrollOffset    = 0;

    // Selector rect (set in init)
    private int selX, selY, selW, selH;

    private EditBox triggerBox;

    public TargetBlasphemyScreen() {
        super(Component.literal("Envisioning \u2013 Target Blasphemy"));
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        refreshPlayers();

        int lx = lx();
        int ty = ty();

        // Selector (player dropdown)
        selX = lx + 14;
        selY = ty + 44;
        selW = PANEL_W - 28;
        selH = 12;

        // Trigger word input — pre-filled with the currently active word (synced from server on login)
        triggerBox = new EditBox(font, lx + 14, ty + 88, PANEL_W - 28, 12, Component.literal("Trigger word"));
        triggerBox.setMaxLength(32);
        String currentWord = SyncEnvisionTriggerPacket.CLIENT_WORD;
        if (!currentWord.isEmpty()) {
            triggerBox.setValue(currentWord);
        } else {
            triggerBox.setHint(Component.literal("e.g. LEODERO!"));
        }
        addRenderableWidget(triggerBox);

        // "Invoke Now" button — fires immediately, no trigger
        addRenderableWidget(Button.builder(Component.literal("Invoke Now"), btn -> onInvoke())
                .bounds(lx + 14, ty + PANEL_H - 30, (PANEL_W - 34) / 2, 14)
                .build());

        // "Set Trigger" button — stores the word
        addRenderableWidget(Button.builder(Component.literal("Set Trigger"), btn -> onSetTrigger())
                .bounds(lx + 14 + (PANEL_W - 34) / 2 + 6, ty + PANEL_H - 30, (PANEL_W - 34) / 2, 14)
                .build());

        // Back
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
        renderBackground(g, mouseX, mouseY, partialTick);

        int lx = lx();
        int ty = ty();
        int labelX = lx + 14;

        // Panel
        g.fill(lx, ty, lx + PANEL_W, ty + PANEL_H, C_BG);
        drawOutline(g, lx, ty, PANEL_W, PANEL_H, C_OUTLINE);

        // Title + divider
        g.drawCenteredString(font, "Target Blasphemy", lx + PANEL_W / 2, ty + 10, C_TITLE);
        g.drawCenteredString(font, "LEODERO invocation", lx + PANEL_W / 2, ty + 21, C_HINT);
        g.fill(lx + 10, ty + 32, lx + PANEL_W - 10, ty + 33, C_OUTLINE);

        // Labels
        g.drawString(font, "Target Player", labelX, ty + 36, C_LABEL, false);
        g.drawString(font, "Trigger Word",  labelX, ty + 80, C_LABEL, false);
        g.drawString(font, "Fires on speaker every time – 60s cooldown", labelX, ty + 103, C_HINT, false);

        // Divider above buttons
        g.fill(lx + 10, ty + PANEL_H - 38, lx + PANEL_W - 10, ty + PANEL_H - 37, C_OUTLINE);

        // Widgets (EditBox + buttons)
        super.render(g, mouseX, mouseY, partialTick);

        // Dropdown drawn last so it floats above widgets
        renderSelector(g, mouseX, mouseY);
    }

    private void renderSelector(GuiGraphics g, int mouseX, int mouseY) {
        boolean hov = isOverSelector(mouseX, mouseY);
        int bd = dropdownOpen ? C_OUTLINE : (hov ? 0xFF8090C0 : C_SEL_BD);

        g.fill(selX, selY, selX + selW, selY + selH, C_SEL_BG);
        drawOutline(g, selX, selY, selW, selH, bd);

        String display = selectedPlayer != null ? selectedPlayer : "\u2014 select player \u2014";
        g.drawString(font, display, selX + 3, selY + (selH - 8) / 2,
                selectedPlayer != null ? C_PLAYER_TEXT : C_EMPTY, false);
        g.drawString(font, dropdownOpen ? "\u25b2" : "\u25bc",
                selX + selW - 10, selY + (selH - 8) / 2, C_HINT, false);

        if (!dropdownOpen || players.isEmpty()) return;

        int vis   = Math.min(DROP_MAX_VIS, players.size());
        int dropH = vis * (DROP_ROW_H + DROP_ROW_GAP);
        int dropY = selY + selH + 1;

        g.fill(selX, dropY, selX + selW, dropY + dropH, C_DROP_BG);
        drawOutline(g, selX, dropY, selW, dropH, C_DROP_BD);

        for (int i = 0; i < vis; i++) {
            int idx  = dropScrollOffset + i;
            if (idx >= players.size()) break;
            String name = players.get(idx);
            int rowY = dropY + i * (DROP_ROW_H + DROP_ROW_GAP);
            boolean rowHov = mouseX >= selX && mouseX < selX + selW
                          && mouseY >= rowY && mouseY < rowY + DROP_ROW_H;
            if (name.equals(selectedPlayer)) g.fill(selX, rowY, selX + selW, rowY + DROP_ROW_H, C_DROP_SEL);
            if (rowHov)                      g.fill(selX, rowY, selX + selW, rowY + DROP_ROW_H, C_DROP_HOVER);
            g.drawString(font, name, selX + 4, rowY + (DROP_ROW_H - 8) / 2, C_PLAYER_TEXT, false);
        }

        if (players.size() > DROP_MAX_VIS) {
            String hint = (dropScrollOffset + vis) + "/" + players.size();
            g.drawString(font, hint, selX + selW - font.width(hint) - 4,
                    dropY + dropH - 8, C_HINT, false);
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) {
            if (dropdownOpen) { dropdownOpen = false; return true; }
            return super.mouseClicked(mx, my, btn);
        }
        if (isOverSelector((int) mx, (int) my)) {
            dropdownOpen = !dropdownOpen;
            if (dropdownOpen) refreshPlayers();
            return true;
        }
        if (dropdownOpen) {
            int vis    = Math.min(DROP_MAX_VIS, players.size());
            int dropY  = selY + selH + 1;
            int bottom = dropY + vis * (DROP_ROW_H + DROP_ROW_GAP);
            if (mx >= selX && mx < selX + selW && my >= dropY && my < bottom) {
                int row = (int) (my - dropY) / (DROP_ROW_H + DROP_ROW_GAP);
                int idx = dropScrollOffset + row;
                if (idx >= 0 && idx < players.size()) selectedPlayer = players.get(idx);
            }
            dropdownOpen = false;
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (dropdownOpen) {
            int max = Math.max(0, players.size() - DROP_MAX_VIS);
            dropScrollOffset = (int) Math.max(0, Math.min(max, dropScrollOffset - dy));
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void onInvoke() {
        if (selectedPlayer == null) return;
        PacketHandler.sendToServer(new RequestEnvisionBlasphemyPacket(selectedPlayer, "", true));
        onClose();
    }

    private void onSetTrigger() {
        String word = triggerBox.getValue().trim();
        if (word.isEmpty()) return;
        // targetName is unused for trigger mode — fires on whoever speaks the word
        PacketHandler.sendToServer(new RequestEnvisionBlasphemyPacket("", word, false));
        onClose();
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
        if (selectedPlayer != null && !players.contains(selectedPlayer)) selectedPlayer = null;
        dropScrollOffset = 0;
    }

    private boolean isOverSelector(int mx, int my) {
        return mx >= selX && mx < selX + selW && my >= selY && my < selY + selH;
    }

    private static void drawOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,         x + w,     y + 1,     color);
        g.fill(x,         y + h - 1, x + w,     y + h,     color);
        g.fill(x,         y,         x + 1,     y + h,     color);
        g.fill(x + w - 1, y,         x + w,     y + h,     color);
    }
}

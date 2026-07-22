package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.EnvisionTargetTeleportPacket;
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
 * Sub-screen for the "Target > Position" Envisioning entry.
 * Lets the player enter a target player name, X/Y/Z coordinates, and a dimension
 * (with alias support), then teleports the target via {@link EnvisionTargetTeleportPacket}.
 */
@OnlyIn(Dist.CLIENT)
public class TargetPositionScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W      = 240;
    private static final int PANEL_H      = 202;
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

    // ── State ─────────────────────────────────────────────────────────────────
    private List<String> players  = new ArrayList<>();
    private String selectedPlayer = null;
    private boolean dropdownOpen  = false;
    private int dropScrollOffset  = 0;

    // Selector rect (set in init)
    private int selX, selY, selW, selH;

    // ── Coord / dim inputs ────────────────────────────────────────────────────
    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;
    private EditBox dimBox;

    public TargetPositionScreen() {
        super(Component.literal("Envisioning \u2013 Target Position"));
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        refreshPlayers();

        int lx = lx();
        int ty = ty();
        int startY  = ty + 44;
        int rowStep = 22;

        // Selector rect (row 0 = Player)
        selX = lx + 14;
        selY = startY;
        selW = PANEL_W - 28;
        selH = 12;

        // X / Y / Z — small fields on the right
        int fieldW = 60;
        int fieldH = 12;
        int fieldX = lx + PANEL_W - 14 - fieldW;

        xBox = new EditBox(font, fieldX, startY + rowStep, fieldW, fieldH, Component.literal("X"));
        xBox.setMaxLength(16);
        xBox.setFilter(s -> s.isEmpty() || s.equals("-") || isParsableDouble(s));
        xBox.setHint(Component.literal("0.0"));
        addRenderableWidget(xBox);

        yBox = new EditBox(font, fieldX, startY + rowStep * 2, fieldW, fieldH, Component.literal("Y"));
        yBox.setMaxLength(16);
        yBox.setFilter(s -> s.isEmpty() || s.equals("-") || isParsableDouble(s));
        yBox.setHint(Component.literal("64.0"));
        addRenderableWidget(yBox);

        zBox = new EditBox(font, fieldX, startY + rowStep * 3, fieldW, fieldH, Component.literal("Z"));
        zBox.setMaxLength(16);
        zBox.setFilter(s -> s.isEmpty() || s.equals("-") || isParsableDouble(s));
        zBox.setHint(Component.literal("0.0"));
        addRenderableWidget(zBox);

        // Dimension — medium field on the right
        int dimFieldW = 120;
        int dimFieldX = lx + PANEL_W - 14 - dimFieldW;
        dimBox = new EditBox(font, dimFieldX, startY + rowStep * 4, dimFieldW, fieldH, Component.literal("Dimension"));
        dimBox.setMaxLength(64);
        dimBox.setHint(Component.literal("overworld"));
        addRenderableWidget(dimBox);

        // Teleport button
        int btnW = 80;
        addRenderableWidget(Button.builder(Component.literal("Teleport"), btn -> onTeleport())
                .bounds(lx + (PANEL_W - btnW) / 2, ty + PANEL_H - 22, btnW, 14)
                .build());

        // Back button
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
        int startY  = ty + 44;
        int rowStep = 22;

        // Panel
        g.fill(lx, ty, lx + PANEL_W, ty + PANEL_H, C_BG);
        drawOutline(g, lx, ty, PANEL_W, PANEL_H, C_OUTLINE);

        // Title + divider
        g.drawCenteredString(font, "Target Position", lx + PANEL_W / 2, ty + 16, C_TITLE);
        g.fill(lx + 10, ty + 26, lx + PANEL_W - 10, ty + 27, C_OUTLINE);

        // Row labels
        int labelY = startY + 2;
        g.drawString(font, "Player",    labelX, labelY,               C_LABEL, false);
        g.drawString(font, "X",         labelX, labelY + rowStep,     C_LABEL, false);
        g.drawString(font, "Y",         labelX, labelY + rowStep * 2, C_LABEL, false);
        g.drawString(font, "Z",         labelX, labelY + rowStep * 3, C_LABEL, false);
        g.drawString(font, "Dimension", labelX, labelY + rowStep * 4, C_LABEL, false);

        // Dimension hint lines
        int dimHintY = startY + rowStep * 4 + 14;
        g.drawString(font, "e.g. overworld, nether, end",            labelX, dimHintY,     C_HINT, false);
        g.drawString(font, "chaos_sea, river, dream, spirit",        labelX, dimHintY + 9, C_HINT, false);

        // Render EditBoxes and buttons first
        super.render(g, mouseX, mouseY, partialTick);

        // Player selector rendered last so dropdown floats over everything
        renderSelector(g, mouseX, mouseY);
    }

    private void renderSelector(GuiGraphics g, int mouseX, int mouseY) {
        boolean hovered = isOverSelector(mouseX, mouseY);
        int bdColor = dropdownOpen ? C_OUTLINE : (hovered ? 0xFF8090C0 : C_SEL_BD);

        g.fill(selX, selY, selX + selW, selY + selH, C_SEL_BG);
        drawOutline(g, selX, selY, selW, selH, bdColor);

        String display = selectedPlayer != null ? selectedPlayer : "— select player —";
        int textColor  = selectedPlayer != null ? C_PLAYER_TEXT : C_EMPTY;
        g.drawString(font, display, selX + 3, selY + (selH - 8) / 2, textColor, false);
        g.drawString(font, dropdownOpen ? "▲" : "▼", selX + selW - 10, selY + (selH - 8) / 2, C_HINT, false);

        if (!dropdownOpen || players.isEmpty()) return;

        int visCount = Math.min(DROP_MAX_VIS, players.size());
        int dropH    = visCount * (DROP_ROW_H + DROP_ROW_GAP);
        int dropY    = selY + selH + 1;

        g.fill(selX, dropY, selX + selW, dropY + dropH, C_DROP_BG);
        drawOutline(g, selX, dropY, selW, dropH, C_DROP_BD);

        for (int i = 0; i < visCount; i++) {
            int idx = dropScrollOffset + i;
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
            String hint = (dropScrollOffset + visCount) + "/" + players.size();
            g.drawString(font, hint, selX + selW - font.width(hint) - 4, dropY + dropH - 8, C_HINT, false);
        }
    }

    // ── Input ────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) {
            if (dropdownOpen) { dropdownOpen = false; return true; }
            return super.mouseClicked(mx, my, btn);
        }
        // Click the selector box
        if (isOverSelector((int) mx, (int) my)) {
            dropdownOpen = !dropdownOpen;
            if (dropdownOpen) refreshPlayers();
            return true;
        }
        // Click inside open dropdown
        if (dropdownOpen) {
            int visCount = Math.min(DROP_MAX_VIS, players.size());
            int dropY = selY + selH + 1;
            int dropBottom = dropY + visCount * (DROP_ROW_H + DROP_ROW_GAP);
            if (mx >= selX && mx < selX + selW && my >= dropY && my < dropBottom) {
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

    // ── Actions ────────────────────────────────────────────────────────────────
    private void onTeleport() {
        if (selectedPlayer == null || selectedPlayer.isEmpty()) return;
        String player = selectedPlayer;

        double x = parseOrZero(xBox.getValue());
        double y = parseOrZero(yBox.getValue());
        double z = parseOrZero(zBox.getValue());
        String dim = dimBox.getValue().trim();
        if (dim.isEmpty()) dim = "overworld";

        PacketHandler.sendToServer(new EnvisionTargetTeleportPacket(player, x, y, z, dim));
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
    private static boolean isParsableDouble(String s) {
        try { Double.parseDouble(s); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private static double parseOrZero(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private static void drawOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,         x + w,     y + 1,     color); // top
        g.fill(x,         y + h - 1, x + w,     y + h,     color); // bottom
        g.fill(x,         y,         x + 1,     y + h,     color); // left
        g.fill(x + w - 1, y,         x + w,     y + h,     color); // right
    }
}

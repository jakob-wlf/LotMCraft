package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Envisioning screen – two-column layout.
 * Both Self and Target expose the same 8 categories.
 */
@OnlyIn(Dist.CLIENT)
public class EnvisioningScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W   = 360;
    private static final int PAD       = 14;   // left/right inner padding
    private static final int ROW_H     = 16;
    private static final int ROW_GAP   = 4;
    private static final int HEADER_H  = 56;   // space before first row

    // 8 entries shared by both columns
    private static final String[] ENTRY_LABELS = {
            "Position", "Blasphemy", "Status",
            "Characteristics", "Sequence", "Abilities",
            "Life", "Death"
    };
    private static final int[] ENTRY_COLORS = {
            0xFFFFDD66, // Position     – gold
            0xFFFFAA55, // Blasphemy    – amber
            0xFF55EEFF, // Status       – teal
            0xFFDDDDDD, // Characteristics – light grey
            0xFFCC88FF, // Sequence     – violet
            0xFF66FF99, // Abilities    – green
            0xFFFF6688, // Life         – rose
            0xFF8899CC, // Death        – slate blue
    };

    private static final int PANEL_H = HEADER_H + ENTRY_LABELS.length * (ROW_H + ROW_GAP) + PAD;

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_BG          = 0xFF1A1E2A;
    private static final int C_OUTLINE     = 0xFFAA8833;
    private static final int C_COL_SEP     = 0xFF3A4060;
    private static final int C_TITLE       = 0xFFFFCC55;
    private static final int C_HINT        = 0xFF99AACC;
    private static final int C_DIVIDER     = 0xFF3A4A66;
    private static final int C_SECTION_HDR = 0xFFCCDDFF;
    private static final int C_ROW_HOVER   = 0x33FFFFFF;

    // ── Constructor ───────────────────────────────────────────────────────────
    public EnvisioningScreen() {
        super(Component.literal("Envisioning"));
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        // no widgets – rows are drawn and hit-tested manually
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private int lx() { return (width  - PANEL_W) / 2; }
    private int ty() { return (height - PANEL_H) / 2; }

    /** Column width = half the inner content area minus a small gap. */
    private int colW() { return (PANEL_W - PAD * 2 - 8) / 2; }

    private int selfX()   { return lx() + PAD; }
    private int targetX() { return lx() + PAD + colW() + 8; }
    private int rowStartY() { return ty() + HEADER_H; }
    private int rowY(int i) { return rowStartY() + i * (ROW_H + ROW_GAP); }

    // ── Rendering ─────────────────────────────────────────────────────────────
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x55000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int lx  = lx(), ty = ty();
        int cw  = colW();
        int sx  = selfX(), tx = targetX();
        int rsy = rowStartY();

        // Panel
        g.fill(lx, ty, lx + PANEL_W, ty + PANEL_H, C_BG);
        g.renderOutline(lx, ty, PANEL_W, PANEL_H, C_OUTLINE);

        // Header
        g.drawString(font, Component.literal("Envisioning").withStyle(ChatFormatting.BOLD),        lx + PAD, ty + 8,  C_TITLE, true);
        g.drawString(font, Component.literal("Observe a target").withStyle(ChatFormatting.ITALIC), lx + PAD, ty + 20, C_HINT,  false);
        // Divider below subtitle
        g.fill(lx + PAD, ty + 31, lx + PANEL_W - PAD, ty + 32, C_DIVIDER);

        // Column headers — sit in their own row between the two dividers
        g.drawString(font, Component.literal("Self").withStyle(ChatFormatting.BOLD),   sx, ty + 36, C_SECTION_HDR, false);
        g.drawString(font, Component.literal("Target").withStyle(ChatFormatting.BOLD), tx, ty + 36, C_SECTION_HDR, false);
        // Divider below column headers
        g.fill(lx + PAD, ty + 48, lx + PANEL_W - PAD, ty + 49, C_DIVIDER);

        // Column separator (full height, starts at top divider)
        g.fill(lx + PAD + cw + 3, ty + 31, lx + PAD + cw + 4, ty + PANEL_H - PAD, C_COL_SEP);

        // Entry rows
        for (int i = 0; i < ENTRY_LABELS.length; i++) {
            int ry    = rowY(i);
            int color = ENTRY_COLORS[i];

            boolean hSelf = mouseX >= sx && mouseX < sx + cw && mouseY >= ry && mouseY < ry + ROW_H;
            boolean hTgt  = mouseX >= tx && mouseX < tx + cw && mouseY >= ry && mouseY < ry + ROW_H;

            if (hSelf) g.fill(sx, ry, sx + cw, ry + ROW_H, C_ROW_HOVER);
            if (hTgt)  g.fill(tx, ry, tx + cw, ry + ROW_H, C_ROW_HOVER);

            int textY = ry + (ROW_H - 8) / 2;
            g.drawString(font, " › " + ENTRY_LABELS[i], sx + 2, textY, color, false);
            g.drawString(font, " › " + ENTRY_LABELS[i], tx + 2, textY, color, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);
        int cw = colW(), sx = selfX(), tx = targetX();
        for (int i = 0; i < ENTRY_LABELS.length; i++) {
            int ry = rowY(i);
            if (mx >= sx && mx < sx + cw && my >= ry && my < ry + ROW_H) { onSelfClicked(i);   return true; }
            if (mx >= tx && mx < tx + cw && my >= ry && my < ry + ROW_H) { onTargetClicked(i); return true; }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private void onSelfClicked(int index) {
        if (index == 0) {
            Minecraft.getInstance().setScreen(new SelfPositionScreen());
        } else if (index == 1) {
            Minecraft.getInstance().setScreen(new SelfBlasphemyScreen());
        } else if (index == 2) {
            Minecraft.getInstance().setScreen(new SelfStatusScreen());
        } else if (index == 3) {
            Minecraft.getInstance().setScreen(new SelfCharacteristicsScreen());
        }
        // other indices – TODO
    }

    private void onTargetClicked(int index) {
        if (index == 0) {
            Minecraft.getInstance().setScreen(new TargetPositionScreen());
        } else if (index == 1) {
            Minecraft.getInstance().setScreen(new TargetBlasphemyScreen());
        } else if (index == 2) {
            Minecraft.getInstance().setScreen(new TargetStatusScreen());
        } else if (index == 3) {
            Minecraft.getInstance().setScreen(new TargetCharacteristicsScreen());
        }
        // other indices – TODO
    }

    @Override
    public boolean isPauseScreen() { return false; }
}


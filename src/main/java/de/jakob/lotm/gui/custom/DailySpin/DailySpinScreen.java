package de.jakob.lotm.gui.custom.DailySpin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Client-side slot-machine screen for the daily spin.
 * The actual reward is already in the player's inventory — this is purely cosmetic.
 */
@OnlyIn(Dist.CLIENT)
public class DailySpinScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W  = 390;
    private static final int PANEL_H  = 270;
    private static final int REEL_W   = 358;
    private static final int VISIBLE  = 5;
    private static final int ROW_H    = 28;

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final int COL_BG          = 0xEE050018;
    private static final int COL_OUTLINE     = 0xFF4400CC;
    private static final int COL_TITLE       = 0xFFDDB8FF;
    private static final int COL_REEL_BG     = 0xFF0A001A;
    private static final int COL_REEL_BORDER = 0xFF7700FF;
    private static final int COL_HIGHLIGHT   = 0x660044FF;
    private static final int COL_SEL_BORDER  = 0xFFFFCC00;
    private static final int COL_JACKPOT     = 0xFFFFD700;

    // ── Spin parameters ───────────────────────────────────────────────────────
    private static final int SPIN_TICKS_TOTAL = 120;
    private static final int SPIN_DECEL_START = 70;

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<String> reelNames;
    private final int landingIndex;
    private final boolean canSpin;

    private float scrollPos   = 0f;
    private float targetScroll = 0f;
    private int   spinTicks   = 0;
    private boolean spinning  = false;

    private Button closeButton;

    public DailySpinScreen(List<String> reelNames, int landingIndex, boolean canSpin) {
        super(Component.literal("Daily Spin"));
        this.reelNames    = reelNames;
        this.landingIndex = landingIndex;
        this.canSpin      = canSpin;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xAA000000);
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;
        int btnY = cy + PANEL_H - 30;

        closeButton = addRenderableWidget(Button.builder(
                Component.literal("Close").withStyle(ChatFormatting.WHITE),
                b -> onClose())
                .bounds(cx + PANEL_W / 2 - 40, btnY, 80, 20).build());

        if (canSpin) {
            closeButton.active = false; // disable until spin finishes
            startSpin();
        }
    }

    private void startSpin() {
        spinning = true;
        spinTicks = 0;

        int loopSize = reelNames.size();
        float fullLaps = loopSize * 2.5f;
        targetScroll = fullLaps + landingIndex;
        scrollPos = 0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (!spinning) return;

        spinTicks++;
        if (spinTicks >= SPIN_TICKS_TOTAL) {
            scrollPos = targetScroll;
            spinning  = false;
            if (closeButton != null) closeButton.active = true;
            return;
        }

        float eased;
        if (spinTicks < SPIN_DECEL_START) {
            eased = (float) spinTicks / SPIN_DECEL_START * 0.8f;
        } else {
            float decelT = (float)(spinTicks - SPIN_DECEL_START) / (SPIN_TICKS_TOTAL - SPIN_DECEL_START);
            eased = 0.8f + (1f - (float)Math.pow(1f - decelT, 3)) * 0.2f;
        }
        scrollPos = eased * targetScroll;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        // Panel
        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, COL_BG);
        g.renderOutline(cx, cy, PANEL_W, PANEL_H, COL_OUTLINE);

        // Title
        g.drawCenteredString(font,
                Component.literal("✦ Daily Spin ✦").withStyle(ChatFormatting.BOLD),
                cx + PANEL_W / 2, cy + 10, COL_TITLE);

        if (!canSpin) {
            // Already spun today
            g.drawCenteredString(font,
                    Component.literal("You have already claimed your spin today!")
                            .withStyle(ChatFormatting.YELLOW),
                    cx + PANEL_W / 2, cy + PANEL_H / 2 - 6, 0xFFFFCC00);
            g.drawCenteredString(font,
                    Component.literal("Come back tomorrow for a new spin.").withStyle(ChatFormatting.GRAY),
                    cx + PANEL_W / 2, cy + PANEL_H / 2 + 10, 0xFF888888);
        } else {
            g.drawCenteredString(font,
                    Component.literal("Fate decides your reward...").withStyle(ChatFormatting.ITALIC),
                    cx + PANEL_W / 2, cy + 24, 0xFF9966CC);
            g.fill(cx + 8, cy + 35, cx + PANEL_W - 8, cy + 36, COL_REEL_BORDER);

            renderReel(g, cx, cy);

            // After spin: show result
            if (!spinning) {
                String landed = reelNames.get(Math.floorMod(landingIndex, reelNames.size()));
                int col = landed.contains("JACKPOT") ? COL_JACKPOT : 0xFFFFFFFF;
                g.drawCenteredString(font,
                        Component.literal("Received: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(landed).withStyle(ChatFormatting.WHITE)),
                        cx + PANEL_W / 2, cy + PANEL_H - 58, col);
                if (landed.contains("JACKPOT")) {
                    g.drawCenteredString(font,
                            Component.literal("★ A Uniqueness manifests! ★").withStyle(ChatFormatting.GOLD),
                            cx + PANEL_W / 2, cy + PANEL_H - 44, COL_JACKPOT);
                } else {
                    g.drawCenteredString(font,
                            Component.literal("Check your inventory!").withStyle(ChatFormatting.GREEN),
                            cx + PANEL_W / 2, cy + PANEL_H - 44, 0xFF88FF88);
                }
            } else {
                g.drawCenteredString(font,
                        Component.literal("Rolling...").withStyle(ChatFormatting.DARK_PURPLE),
                        cx + PANEL_W / 2, cy + PANEL_H - 58, 0xFFAA66FF);
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderReel(GuiGraphics g, int panelX, int panelY) {
        int reelX = panelX + (PANEL_W - REEL_W) / 2;
        int reelY = panelY + 44;
        int reelWindowH = ROW_H * VISIBLE;

        g.fill(reelX, reelY, reelX + REEL_W, reelY + reelWindowH, COL_REEL_BG);
        g.renderOutline(reelX, reelY, REEL_W, reelWindowH, COL_REEL_BORDER);
        g.enableScissor(reelX, reelY, reelX + REEL_W, reelY + reelWindowH);

        int loopSize = reelNames.size();
        int centerRow = VISIBLE / 2;
        float startEntry = scrollPos - centerRow;

        for (int row = 0; row < VISIBLE + 2; row++) {
            float entryFloat  = startEntry + row;
            int entryIndex    = Math.floorMod((int) Math.floor(entryFloat), loopSize);
            if (loopSize == 0) continue;

            float frac = entryFloat - (float) Math.floor(entryFloat);
            int rowY   = reelY + (int) ((row - frac) * ROW_H);

            boolean isCentre = (row == centerRow) && !spinning;
            String name      = reelNames.get(entryIndex);
            boolean isJackpot = name.contains("JACKPOT");

            int textColor = isJackpot ? COL_JACKPOT : (isCentre ? 0xFFFFDD00 : 0xFFCCCCCC);
            int bgColor   = isCentre ? COL_HIGHLIGHT : 0;

            if (bgColor != 0) g.fill(reelX + 2, rowY, reelX + REEL_W - 2, rowY + ROW_H - 2, bgColor);
            if (isCentre)     g.renderOutline(reelX + 2, rowY, REEL_W - 4, ROW_H - 2, COL_SEL_BORDER);

            g.drawCenteredString(font,
                    Component.literal(name).withStyle(isCentre ? ChatFormatting.BOLD : ChatFormatting.RESET),
                    reelX + REEL_W / 2, rowY + (ROW_H - font.lineHeight) / 2, textColor);
        }

        // Fade strips
        g.fillGradient(reelX, reelY, reelX + REEL_W, reelY + ROW_H, 0xDD000000, 0x00000000);
        g.fillGradient(reelX, reelY + reelWindowH - ROW_H, reelX + REEL_W, reelY + reelWindowH, 0x00000000, 0xDD000000);

        g.disableScissor();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return !spinning; }
}

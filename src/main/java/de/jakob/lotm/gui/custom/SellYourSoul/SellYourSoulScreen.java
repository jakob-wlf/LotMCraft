package de.jakob.lotm.gui.custom.SellYourSoul;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import de.jakob.lotm.sound.ModSounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Client-side reel screen for the Sell Your Soul mechanic.
 * Server effects are already applied when this opens — screen is cosmetic.
 *
 * Outcomes:
 *   0 = Sanity Drain (sanity → 50%)
 *   1 = Digestion Wipe (digestion → 0)
 *   2 = Watch an Ad
 *   3 = Mysterious Gift (same-seq characteristic)
 */
@OnlyIn(Dist.CLIENT)
public class SellYourSoulScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W  = 420;
    private static final int PANEL_H  = 280;
    private static final int REEL_W   = 388;
    private static final int VISIBLE  = 5;
    private static final int ROW_H    = 30;

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final int COL_BG          = 0xF0030009;
    private static final int COL_OUTLINE     = 0xFF880022;
    private static final int COL_TITLE       = 0xFFFF4466;
    private static final int COL_REEL_BG     = 0xFF0A0008;
    private static final int COL_REEL_BORDER = 0xFFCC0033;
    private static final int COL_HIGHLIGHT   = 0x66880011;
    private static final int COL_SEL_BORDER  = 0xFFFF2244;

    // ── Outcome definitions ───────────────────────────────────────────────────
    private static final String[] OUTCOME_LABELS = {
            "\u2620 Sanity Drain",
            "\u2620 Digestion Wipe",
            "\uD83D\uDCFA Watch an Ad",
            "\u2726 Mysterious Gift",
            "\u26A0 SEQUENCE REVERSION",
            "\u2728 Potion Reward"
    };
    private static final int[] OUTCOME_COLORS = {
            0xFFFF6688, 0xFFFF6688, 0xFFFF9900, 0xFFFFD700, 0xFFFF2222, 0xFF44FFAA
    };

    // ── Spin parameters ───────────────────────────────────────────────────────
    private static final int SPIN_TICKS_TOTAL = 60;
    private static final int SPIN_DECEL_START = 35;

    // ── State ─────────────────────────────────────────────────────────────────
    private final int outcome;
    private final String rewardName;
    private final List<String> reel;
    private final int landingIndex;

    private float scrollPos    = 0f;
    private float targetScroll = 0f;
    private int   spinTicks    = 0;
    private boolean spinning   = false;

    private Button closeButton;
    private SimpleSoundInstance spinSound;

    public SellYourSoulScreen(int outcome, String rewardName) {
        super(Component.literal("Sell Your Soul"));
        this.outcome    = outcome;
        this.rewardName = rewardName;

        // Build reel: 5 sanity, 5 digestion, 7 ad, 1 gift, 1 reversion, 2 potion
        List<String> raw = new ArrayList<>();
        for (int i = 0; i < 5; i++) raw.add(OUTCOME_LABELS[0]);
        for (int i = 0; i < 5; i++) raw.add(OUTCOME_LABELS[1]);
        for (int i = 0; i < 7; i++) raw.add(OUTCOME_LABELS[2]);
        raw.add(OUTCOME_LABELS[3]);
        raw.add(OUTCOME_LABELS[4]); // reversion — visually present but astronomically rare
        for (int i = 0; i < 2; i++) raw.add(OUTCOME_LABELS[5]);
        Collections.shuffle(raw, new Random());
        this.reel = raw;

        // Place the actual outcome at a random slot of the correct type
        int idx = 0;
        for (int i = 0; i < reel.size(); i++) {
            if (reel.get(i).equals(OUTCOME_LABELS[outcome])) { idx = i; break; }
        }
        reel.set(idx, OUTCOME_LABELS[outcome]); // ensure it's there
        this.landingIndex = idx;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xCC000000);
    }

    @Override
    protected void init() {
        super.init();
        int cx  = (width  - PANEL_W) / 2;
        int cy  = (height - PANEL_H) / 2;
        int btnY = cy + PANEL_H - 30;

        closeButton = addRenderableWidget(Button.builder(
                Component.literal("Accept Fate").withStyle(ChatFormatting.RED),
                b -> {
                    if (outcome == 2) {
                        // Defer to next tick so the current screen fully closes first
                        Minecraft.getInstance().execute(() ->
                                Minecraft.getInstance().setScreen(new AdScreen()));
                    } else {
                        onClose();
                    }
                })
                .bounds(cx + PANEL_W / 2 - 50, btnY, 100, 20).build());

        closeButton.active = false; // enabled when spin finishes
        startSpin();
    }

    private void startSpin() {
        spinning   = true;
        spinTicks  = 0;
        float fullLaps = reel.size() * 3f; // must be an integer multiple so the reel lands on landingIndex
        targetScroll = fullLaps + landingIndex;
        scrollPos    = 0f;

        spinSound = SimpleSoundInstance.forUI(ModSounds.GAMBLING_WHEEL_SPIN.get(), 1.0f);
        Minecraft.getInstance().getSoundManager().play(spinSound);
    }

    @Override
    public void tick() {
        super.tick();
        if (!spinning) return;

        spinTicks++;
        if (spinTicks >= SPIN_TICKS_TOTAL) {
            scrollPos = targetScroll;
            spinning  = false;
            if (spinSound != null) { Minecraft.getInstance().getSoundManager().stop(spinSound); spinSound = null; }
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

        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, COL_BG);
        g.renderOutline(cx, cy, PANEL_W, PANEL_H, COL_OUTLINE);

        // Title
        g.drawCenteredString(font,
                Component.literal("⚠ SELL YOUR SOUL ⚠").withStyle(ChatFormatting.BOLD),
                cx + PANEL_W / 2, cy + 10, COL_TITLE);
        g.drawCenteredString(font,
                Component.literal("What price will fate demand of you?").withStyle(ChatFormatting.ITALIC),
                cx + PANEL_W / 2, cy + 24, 0xFF994455);

        g.fill(cx + 8, cy + 35, cx + PANEL_W - 8, cy + 36, COL_REEL_BORDER);

        renderReel(g, cx, cy);

        if (!spinning) {
            int outcomeColor = OUTCOME_COLORS[outcome];
            String label = OUTCOME_LABELS[outcome];
            g.drawCenteredString(font,
                    Component.literal("Result: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(label).withStyle(ChatFormatting.BOLD)),
                    cx + PANEL_W / 2, cy + PANEL_H - 68, outcomeColor);

            // Flavour text per outcome
            String flavour = switch (outcome) {
                case 0 -> "Your sanity has been set to 50%.";
                case 1 -> "All digestion progress has been wiped.";
                case 2 -> "Fate demands your attention...";
                case 3 -> "You received: " + rewardName;
                case 4 -> "You have been reverted to Sequence 9.";
                case 5 -> "You received: " + rewardName;
                default -> "";
            };
            g.drawCenteredString(font,
                    Component.literal(flavour).withStyle(ChatFormatting.DARK_RED),
                    cx + PANEL_W / 2, cy + PANEL_H - 52, 0xFFCC4444);
        } else {
            g.drawCenteredString(font,
                    Component.literal("Fate is rolling...").withStyle(ChatFormatting.DARK_RED),
                    cx + PANEL_W / 2, cy + PANEL_H - 60, 0xFFCC2244);
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

        int loopSize   = reel.size();
        int centerRow  = VISIBLE / 2;
        float startEntry = scrollPos - centerRow;

        for (int row = 0; row < VISIBLE + 2; row++) {
            float entryFloat = startEntry + row;
            int entryIndex   = Math.floorMod((int) Math.floor(entryFloat), loopSize);
            if (loopSize == 0) continue;

            float frac = entryFloat - (float) Math.floor(entryFloat);
            int rowY   = reelY + (int) ((row - frac) * ROW_H);

            boolean isCentre = (row == centerRow) && !spinning;
            String name      = reel.get(entryIndex);
            int outcomeIdx   = getOutcomeIndex(name);
            int textColor    = isCentre ? OUTCOME_COLORS[outcomeIdx] : 0xFFAA8899;
            int bgColor      = isCentre ? COL_HIGHLIGHT : 0;

            if (bgColor != 0) g.fill(reelX + 2, rowY, reelX + REEL_W - 2, rowY + ROW_H - 2, bgColor);
            if (isCentre)     g.renderOutline(reelX + 2, rowY, REEL_W - 4, ROW_H - 2, COL_SEL_BORDER);

            g.drawCenteredString(font,
                    Component.literal(name).withStyle(isCentre ? ChatFormatting.BOLD : ChatFormatting.RESET),
                    reelX + REEL_W / 2, rowY + (ROW_H - font.lineHeight) / 2, textColor);
        }

        // Fade strips
        g.fillGradient(reelX, reelY, reelX + REEL_W, reelY + ROW_H, 0xEE000000, 0x00000000);
        g.fillGradient(reelX, reelY + reelWindowH - ROW_H, reelX + REEL_W, reelY + reelWindowH, 0x00000000, 0xEE000000);

        g.disableScissor();
    }

    private int getOutcomeIndex(String label) {
        for (int i = 0; i < OUTCOME_LABELS.length; i++) {
            if (OUTCOME_LABELS[i].equals(label)) return i;
        }
        return 0;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}

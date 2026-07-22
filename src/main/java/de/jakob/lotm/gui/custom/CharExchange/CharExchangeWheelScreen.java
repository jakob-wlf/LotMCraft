package de.jakob.lotm.gui.custom.CharExchange;

import de.jakob.lotm.sound.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Step 2 of the Characteristics Exchange UI — the spinning reel.
 *
 * Server effects already applied before this screen opens.
 *
 * Outcomes:
 *   0 = Garbage        (85%) — you received Garbage locked to a slot
 *   1 = GarbageCollect (10%) — all Garbage cleared from your inventory
 *   2 = Upgrade         (5%) — you received a characteristic one rank higher
 */
@OnlyIn(Dist.CLIENT)
public class CharExchangeWheelScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W = 420;
    private static final int PANEL_H = 310;
    private static final int REEL_W  = 388;
    private static final int VISIBLE = 5;
    private static final int ROW_H   = 30;

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final int COL_BG          = 0xF00A0014;
    private static final int COL_OUTLINE     = 0xFF440088;
    private static final int COL_TITLE       = 0xFFBB66FF;
    private static final int COL_REEL_BG     = 0xFF080012;
    private static final int COL_REEL_BORDER = 0xFF6600BB;
    private static final int COL_HIGHLIGHT   = 0x44440088;
    private static final int COL_SEL_BORDER  = 0xFFBB44FF;

    // ── Outcome labels & colors ───────────────────────────────────────────────
    private static final String[] OUTCOME_LABELS = {
            "\uD83D\uDDD1 Garbage",
            "\uD83E\uDDF9 Garbage Collector",
            "\u2605 Fate's Favour"
    };
    private static final int[] OUTCOME_COLORS = {
            0xFFAA6644,   // garbage — brownish
            0xFF44AAFF,   // garbage collect — blue (cleaning water!)
            0xFFFFDD00    // upgrade — gold
    };

    // ── Garbage Collector funny messages ─────────────────────────────────────
    private static final String[] GC_MESSAGES = {
            "The cosmic janitor has arrived. Your filth has been expunged.",
            "Fate has dispatched its intern to clean up your mess. Again.",
            "The universe looked at your inventory and cringed. All Garbage: gone.",
            "\"Those characteristics were WORTHLESS anyway.\" - Fate, probably.",
            "Congratulations! Fate has assigned you a free cleaning service. Enjoy it.",
    };

    // ── Spin parameters ───────────────────────────────────────────────────────
    private static final int SPIN_TICKS_TOTAL = 60;
    private static final int SPIN_DECEL_START = 35;

    // ── State ─────────────────────────────────────────────────────────────────
    private final int outcome;
    private final String rewardName;
    private final List<String> reel;
    private final int landingIndex;
    private final String gcMessage;
    private final String screenTitle;

    private float scrollPos    = 0f;
    private float targetScroll = 0f;
    private int   spinTicks    = 0;
    private boolean spinning   = false;

    private Button closeButton;
    private SimpleSoundInstance spinSound;

    public CharExchangeWheelScreen(List<String> reel, int landingIndex, int outcome, String rewardName, String title) {
        super(Component.literal(title));
        this.reel         = reel;
        this.landingIndex = landingIndex;
        this.outcome      = outcome;
        this.rewardName   = rewardName;
        this.screenTitle  = title;

        // Pick a random funny message for garbage collector outcome
        this.gcMessage = GC_MESSAGES[(int)(System.currentTimeMillis() % GC_MESSAGES.length)];
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xCC000000);
    }

    @Override
    protected void init() {
        super.init();
        int cx   = (width  - PANEL_W) / 2;
        int cy   = (height - PANEL_H) / 2;
        int btnY = cy + PANEL_H - 28;

        closeButton = addRenderableWidget(Button.builder(
                Component.literal("Accept Fate").withStyle(ChatFormatting.LIGHT_PURPLE),
                b -> onClose())
                .bounds(cx + PANEL_W / 2 - 50, btnY, 100, 20).build());
        closeButton.active = false; // enabled when spin finishes

        startSpin();
    }

    private void startSpin() {
        spinning   = true;
        spinTicks  = 0;
        float fullLaps = reel.size() * 3f; // integer multiple so center == landingIndex
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

        // Panel
        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, COL_BG);
        g.renderOutline(cx, cy, PANEL_W, PANEL_H, COL_OUTLINE);

        // Title
        g.drawCenteredString(font,
                Component.literal("\u2746 " + screenTitle.toUpperCase() + " \u2746").withStyle(ChatFormatting.BOLD),
                cx + PANEL_W / 2, cy + 10, COL_TITLE);
        g.drawCenteredString(font,
                Component.literal("A price is always exacted for what fate bestows, isn't it.").withStyle(ChatFormatting.ITALIC),
                cx + PANEL_W / 2, cy + 24, 0xFF886699);

        g.fill(cx + 8, cy + 35, cx + PANEL_W - 8, cy + 36, COL_REEL_BORDER);

        renderReel(g, cx, cy);

        if (!spinning) {
            int outcomeColor = OUTCOME_COLORS[outcome];

            // Result label
            g.drawCenteredString(font,
                    Component.literal("Result: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(OUTCOME_LABELS[outcome]).withStyle(ChatFormatting.BOLD)),
                    cx + PANEL_W / 2, cy + PANEL_H - 80, outcomeColor);

            // Flavour line
            String flavour = switch (outcome) {
                case CharExchangeHandler.OUTCOME_GARBAGE ->
                        "Fate is displeased. Garbage has entered your possession.";
                case CharExchangeHandler.OUTCOME_GARBAGE_COLLECT ->
                        gcMessage;
                case CharExchangeHandler.OUTCOME_UPGRADE ->
                        "Fate smiles. You received: " + rewardName;
                default -> "";
            };

            // Word-wrap the flavour text
            var lines = font.split(Component.literal(flavour), PANEL_W - 40);
            int linesY = cy + PANEL_H - 62;
            for (var line : lines) {
                g.drawCenteredString(font, line, cx + PANEL_W / 2, linesY, 0xFFBB88CC);
                linesY += font.lineHeight + 2;
            }
        } else {
            g.drawCenteredString(font,
                    Component.literal("Fate is deliberating...").withStyle(ChatFormatting.DARK_PURPLE),
                    cx + PANEL_W / 2, cy + PANEL_H - 72, 0xFF8844AA);
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

        int loopSize  = reel.size();
        int centerRow = VISIBLE / 2;
        float startEntry = scrollPos - centerRow;

        for (int row = 0; row < VISIBLE + 2; row++) {
            float entryFloat = startEntry + row;
            int entryIndex   = Math.floorMod((int) Math.floor(entryFloat), loopSize);
            if (loopSize == 0) continue;

            float frac = entryFloat - (float) Math.floor(entryFloat);
            int rowY   = reelY + (int) ((row - frac) * ROW_H);

            boolean isCentre = (row == centerRow) && !spinning;
            String name      = reel.get(entryIndex);
            int outcomeIdx   = resolveOutcomeIndex(name);
            int textColor    = isCentre ? OUTCOME_COLORS[outcomeIdx] : 0xFF997799;
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

    /** Maps a reel label string back to an outcome index for coloring. */
    private int resolveOutcomeIndex(String label) {
        if (label.contains("Garbage Collector") || label.contains("\uD83E\uDDF9")) return CharExchangeHandler.OUTCOME_GARBAGE_COLLECT;
        if (label.contains("Fate's Favour")     || label.contains("\u2605"))       return CharExchangeHandler.OUTCOME_UPGRADE;
        return CharExchangeHandler.OUTCOME_GARBAGE; // default
    }

    @Override
    public boolean isPauseScreen()      { return false; }

    @Override
    public boolean shouldCloseOnEsc()   { return false; }
}

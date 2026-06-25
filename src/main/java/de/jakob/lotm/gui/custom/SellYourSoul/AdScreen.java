package de.jakob.lotm.gui.custom.SellYourSoul;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Random;

/**
 * An unskippable ad overlay. The "Skip Ad" button moves to a random position
 * each time it is clicked. After 5 successful clicks the screen closes.
 */
@OnlyIn(Dist.CLIENT)
public class AdScreen extends Screen {

    private static final int CLICKS_REQUIRED = 5;

    // ── Ad templates ──────────────────────────────────────────────────────────
    private record AdTemplate(String header, int headerBg, int headerFg, String[] lines, String footer) {}

    private static final AdTemplate[] AD_TEMPLATES = {
        new AdTemplate(
            "A D V E R T I S E M E N T",
            0xFFFFAA00, 0xFF000000,
            new String[]{
                "★ BEYOND SEQUENCE SUBSCRIPTION ★",
                "Tired of being merely Sequence 1?",
                "Upgrade to Beyond Sequence PREMIUM",
                "and unlock the power of... nothing.",
                "",
                "Only 999 Characteristics per month!",
                "(Sanity not included)",
            },
            "Sponsored by Fate's Wheel™"
        ),
        new AdTemplate(
            "A D V E R T I S E M E N T",
            0xFF8800CC, 0xFFFFFFFF,
            new String[]{
                "★ NIGHTMARE CHURCH RECRUITMENT ★",
                "Are you feeling... different lately?",
                "Do your dreams involve tentacles?",
                "JOIN US. Free robes, free rituals.",
                "",
                "Side effects: loss of self, madness,",
                "and/or transformation into a monster.",
            },
            "The Nightmare Church — We found you already."
        ),
        new AdTemplate(
            "A D V E R T I S E M E N T",
            0xFF005599, 0xFFFFFFFF,
            new String[]{
                "★ MACHINERY HIVEMIND UPGRADES ★",
                "Biological components getting you down?",
                "Replace up to 90% of your body!",
                "Steel never goes mad. Steel never sins.",
                "",
                "Special offer: Buy 2 limbs, get 1 free!",
                "(Soul upgrade sold separately)",
            },
            "Machinery Hivemind Corp — Progress is Inevitable™"
        ),
        new AdTemplate(
            "A D V E R T I S E M E N T",
            0xFF006600, 0xFFFFFFFF,
            new String[]{
                "★ SEER SERVICES INC. ★",
                "We already knew you'd click this ad.",
                "\"Your future holds... a subscription.\"",
                "- Our diviners, probably",
                "",
                "Tarot readings, fate nudging & more!",
                "Results may vary. Paradoxes extra.",
            },
            "Seer Services Inc. — We Knew You'd Come™"
        ),
        new AdTemplate(
            "A D V E R T I S E M E N T",
            0xFFAA0000, 0xFFFFFFFF,
            new String[]{
                "★ DEATH'S OFFICIAL INSURANCE ★",
                "Did your soul just get sold? Uh oh.",
                "Death's Official Insurance has you covered!",
                "\"We pay out even after full corruption.\"",
                "",
                "Premiums: One memory per month.",
                "Fine print: Death reserves the right to you.",
            },
            "Sponsored by the Grim Reaper™ — Terms apply."
        ),
        new AdTemplate(
            "A D V E R T I S E M E N T",
            0xFF884400, 0xFFFFFFFF,
            new String[]{
                "★ MR. FOOL'S WEIGHT-LOSS PLAN ★",
                "Struggling to lose those extra pounds?",
                "The Fool pathway guarantees results!",
                "Step 1: Sell your soul.",
                "Step 2: The weight concerns you no longer.",
                "",
                "\"It works because you cease to care.\"",
            },
            "Sponsored by the Tarot Club™ — Not liable for madness."
        ),
        new AdTemplate(
            "A D V E R T I S E M E N T",
            0xFF335533, 0xFFFFFFFF,
            new String[]{
                "★ EVERNIGHT DATING SERVICES ★",
                "Single? Perhaps your True Name can help.",
                "Match with other Beyonders near you!",
                "\"Compatibility guaranteed by the Mystery Pryer.\"",
                "",
                "Warning: Some matches may be ancient evils.",
                "Swipe with caution.",
            },
            "Evernight Dating™ — Love Beyond the Sequence."
        ),
    };

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final int COL_BG       = 0xF5000000;
    private static final int COL_OUTLINE  = 0xFFFFAA00;
    private static final int COL_TEXT     = 0xFFCCCCCC;
    private static final int COL_BTN_AREA = 0xFF111111;

    // ── State ─────────────────────────────────────────────────────────────────
    private int clicksLeft = CLICKS_REQUIRED;
    private int btnX, btnY;
    private final Random rand = new Random();
    private Button skipButton;
    private AdTemplate currentAd;

    private static final int BTN_W = 90;
    private static final int BTN_H = 20;

    public AdScreen() {
        super(Component.literal("Advertisement"));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xCC000000);
    }

    @Override
    protected void init() {
        super.init();
        if (currentAd == null) {
            currentAd = AD_TEMPLATES[rand.nextInt(AD_TEMPLATES.length)];
        }
        repositionButton();
    }

    private void repositionButton() {
        // Place the button at a random position anywhere on screen,
        // with a margin so it can't go off-screen
        int margin = 10;
        btnX = margin + rand.nextInt(Math.max(1, width  - BTN_W - margin * 2));
        btnY = margin + rand.nextInt(Math.max(1, height - BTN_H - margin * 2));

        // Rebuild widget at new position
        if (skipButton != null) removeWidget(skipButton);

        int remaining = clicksLeft;
        skipButton = addRenderableWidget(Button.builder(
                Component.literal("Skip Ad (" + remaining + ")")
                        .withStyle(ChatFormatting.YELLOW),
                b -> onSkipClicked())
                .bounds(btnX, btnY, BTN_W, BTN_H).build());
    }

    private void onSkipClicked() {
        clicksLeft--;
        if (clicksLeft <= 0) {
            onClose();
        } else {
            repositionButton();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int cx = width / 2;

        // Ad panel (centered, 360×220)
        int pw = 360;
        int ph = 220;
        int px = cx - pw / 2;
        int py = height / 2 - ph / 2;

        g.fill(px, py, px + pw, py + ph, COL_BG);
        g.renderOutline(px, py, pw, ph, COL_OUTLINE);

        // Header bar (color per ad)
        g.fill(px, py, px + pw, py + 20, currentAd.headerBg());
        g.drawCenteredString(font,
                Component.literal(currentAd.header()).withStyle(ChatFormatting.BOLD),
                cx, py + 6, currentAd.headerFg());

        // Ad body text
        int lineY = py + 28;
        for (String line : currentAd.lines()) {
            if (!line.isEmpty()) {
                g.drawCenteredString(font, Component.literal(line), cx, lineY, COL_TEXT);
            }
            lineY += font.lineHeight + 4;
        }

        // Footer
        g.fill(px, py + ph - 22, px + pw, py + ph, COL_BTN_AREA);
        g.drawCenteredString(font,
                Component.literal(currentAd.footer()).withStyle(ChatFormatting.DARK_GRAY),
                cx, py + ph - 14, 0xFF555555);

        // Skip counter below button
        g.drawString(font,
                Component.literal("Clicks remaining: " + clicksLeft).withStyle(ChatFormatting.GRAY),
                btnX, btnY + BTN_H + 3, COL_TEXT, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}

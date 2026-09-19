package de.jakob.lotm.gui.custom.SellYourSoul;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestSellYourSoulPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Gate screen shown when the player clicks "Sell Soul".
 * Displays the possible outcomes, and either a live countdown timer
 * (if on cooldown) or a "SPIN THE WHEEL" button (if ready).
 */
@OnlyIn(Dist.CLIENT)
public class SellYourSoulGateScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W = 400;
    private static final int PANEL_H = 300;

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final int COL_BG         = 0xF2010008;
    private static final int COL_OUTLINE    = 0xFF880022;
    private static final int COL_TITLE      = 0xFFFF4466;
    private static final int COL_DIVIDER    = 0xFF550011;
    private static final int COL_LABEL      = 0xFFCCAABB;
    private static final int COL_HEADER     = 0xFF994455;
    private static final int COL_TIMER      = 0xFFFF8800;
    private static final int COL_TIMER_DONE = 0xFF44FF44;

    // ── Outcome display table ──────────────────────────────────────────────────
    private static final String[] OUTCOME_NAMES = {
            "\u2620 Sanity Drain",
            "\u2620 Digestion Wipe",
            "\uD83D\uDCFA Watch an Ad",
            "\u2726 Mysterious Gift",
            "\u2726 Mysterious Gift",
    };
    private static final int[] OUTCOME_COLORS = {
            0xFFFF6688, 0xFFFF6688, 0xFFFF9900, 0xFFFFD700, 0xFFFFD700
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private final long cooldownEndMillis;
    private Button spinButton;
    private Button backButton;

    public SellYourSoulGateScreen(long cooldownEndMillis) {
        super(Component.literal("Sell Your Soul"));
        this.cooldownEndMillis = cooldownEndMillis;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xCC000000);
    }

    @Override
    protected void init() {
        super.init();
        int cx  = width  / 2;
        int cy  = height / 2;
        int py  = cy - PANEL_H / 2;

        // Spin button — enabled only when cooldown is over
        spinButton = addRenderableWidget(Button.builder(
                Component.literal("⚰ SPIN THE WHEEL ⚰").withStyle(ChatFormatting.BOLD),
                b -> {
                    PacketHandler.sendToServer(new RequestSellYourSoulPacket());
                    onClose();
                })
                .bounds(cx - 80, py + PANEL_H - 52, 160, 20).build());

        backButton = addRenderableWidget(Button.builder(
                Component.literal("Back").withStyle(ChatFormatting.GRAY),
                b -> onClose())
                .bounds(cx - 30, py + PANEL_H - 28, 60, 20).build());

        updateButtonState();
    }

    private void updateButtonState() {
        if (spinButton != null) {
            boolean ready = System.currentTimeMillis() >= cooldownEndMillis;
            spinButton.active = ready;
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateButtonState();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int cx = width  / 2;
        int cy = height / 2;
        int px = cx - PANEL_W / 2;
        int py = cy - PANEL_H / 2;

        // Panel
        g.fill(px, py, px + PANEL_W, py + PANEL_H, COL_BG);
        g.renderOutline(px, py, PANEL_W, PANEL_H, COL_OUTLINE);

        // Title bar
        g.fill(px, py, px + PANEL_W, py + 22, 0xFF1A0005);
        g.drawCenteredString(font,
                Component.literal("⚠  SELL YOUR SOUL  ⚠").withStyle(ChatFormatting.BOLD),
                cx, py + 7, COL_TITLE);

        g.fill(px + 6, py + 23, px + PANEL_W - 6, py + 24, COL_DIVIDER);

        // Subtitle
        g.drawCenteredString(font,
                Component.literal("Fate will determine your punishment — or reward.").withStyle(ChatFormatting.ITALIC),
                cx, py + 30, COL_HEADER);

        // ── Outcomes list ─────────────────────────────────────────────────────
        int tableY = py + 48;
        g.fill(px + 10, tableY - 2, px + PANEL_W - 10, tableY + OUTCOME_NAMES.length * 18 + 2, 0x22FF0033);
        g.renderOutline(px + 10, tableY - 2, PANEL_W - 20, OUTCOME_NAMES.length * 18 + 4, 0x55880022);

        for (int i = 0; i < OUTCOME_NAMES.length; i++) {
            g.drawCenteredString(font, Component.literal(OUTCOME_NAMES[i]),
                    cx, tableY + 4, OUTCOME_COLORS[i]);
            tableY += 18;
        }

        // ── Cooldown / ready area ─────────────────────────────────────────────
        int timerY = py + PANEL_H - 100;
        long remaining = cooldownEndMillis - System.currentTimeMillis();

        if (remaining > 0) {
            long mins = remaining / 60000L;
            long secs = (remaining % 60000L) / 1000L;

            g.drawCenteredString(font,
                    Component.literal("Next spin available in:").withStyle(ChatFormatting.GRAY),
                    cx, timerY, COL_LABEL);

            // Big timer
            String timerStr = String.format("%02d:%02d", mins, secs);
            // Scale up the timer text via pose stack
            g.pose().pushPose();
            g.pose().translate(cx, timerY + 14, 0);
            g.pose().scale(2f, 2f, 1f);
            g.drawCenteredString(font, Component.literal(timerStr).withStyle(ChatFormatting.BOLD),
                    0, 0, COL_TIMER);
            g.pose().popPose();
        } else {
            g.drawCenteredString(font,
                    Component.literal("✦ Ready to spin! ✦").withStyle(ChatFormatting.BOLD),
                    cx, timerY + 10, COL_TIMER_DONE);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

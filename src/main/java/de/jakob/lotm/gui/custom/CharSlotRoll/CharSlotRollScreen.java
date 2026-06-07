package de.jakob.lotm.gui.custom.CharSlotRoll;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.CharSlotRollResultPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Non-closable slot-machine screen shown to new players when the
 * {@code doCharacteristicsSlots} gamerule is active.
 *
 * The reel scrolls through all seq-9 characteristic names. When it stops,
 * the player may Accept (sends the result) or Reroll (if rerolls remain).
 */
@OnlyIn(Dist.CLIENT)
public class CharSlotRollScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W    = 390;
    private static final int PANEL_H    = 270;
    private static final int REEL_W     = 358;
    private static final int REEL_H     = 32;   // height of the highlighted center slot
    private static final int VISIBLE    = 5;     // number of visible rows in the reel window
    private static final int ROW_H      = 28;    // height per row

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final int COL_BG         = 0xEE0A0010;
    private static final int COL_OUTLINE     = 0xFF3300AA;
    private static final int COL_TITLE       = 0xFFCC88FF;
    private static final int COL_REEL_BG     = 0xFF0D0020;
    private static final int COL_REEL_BORDER = 0xFF6600FF;
    private static final int COL_HIGHLIGHT   = 0x550055FF;
    private static final int COL_SEL_BORDER  = 0xFFFFCC00;
    private static final int COL_STRIP_TOP   = 0xBB000000;
    private static final int COL_STRIP_BOT   = 0xBB000000;

    // ── Spin parameters ───────────────────────────────────────────────────────
    /** Total ticks for one spin. */
    private static final int SPIN_TICKS_TOTAL = 120;
    /** Ticks after which speed starts decelerating. */
    private static final int SPIN_DECEL_START = 70;

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<String> pathways;
    private final List<String> charNames;
    private int rerollsLeft;

    /** The looped display list used for the reel (repeated 3× for wrap-around). */
    private final List<String> reelLoop = new ArrayList<>();
    private final List<String> reelPathwayLoop = new ArrayList<>();

    /** Current fractional scroll position (in rows). */
    private float scrollPos   = 0f;
    /** Target scroll position. */
    private float targetScroll = 0f;
    /** Ticks remaining in current spin. */
    private int  spinTicks    = 0;
    /** Whether a spin is currently running. */
    private boolean spinning  = false;
    /** Index (into reelLoop) of the currently selected entry. */
    private int selectedIndex = 0;

    private Button acceptButton;
    private Button rerollButton;

    private final Random rand = new Random();

    // ── Konami code easter egg ────────────────────────────────────────────────
    // Raw GLFW key codes: UP=265, DOWN=264, LEFT=263, RIGHT=262
    private static final int[] KONAMI = { 265, 265, 264, 264, 263, 262, 263, 262 };
    private int konamiIndex   = 0;
    private int konamiUsed    = 0;   // max 2 activations
    /** Ticks to show the easter-egg flash message. */
    private int konamiFlashTicks = 0;

    // ── Key input ─────────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Konami sequence: UP UP DOWN DOWN LEFT RIGHT LEFT RIGHT
        if (keyCode == KONAMI[konamiIndex]) {
            konamiIndex++;
            if (konamiIndex == KONAMI.length) {
                konamiIndex = 0;
                if (konamiUsed < 2) {
                    konamiUsed++;
                    rerollsLeft++;
                    konamiFlashTicks = 80; // ~4 seconds at 20 tps
                    updateButtonState();
                }
            }
        } else {
            // Wrong key — reset sequence but still check if this key starts a new one
            konamiIndex = (keyCode == KONAMI[0]) ? 1 : 0;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public CharSlotRollScreen(List<String> pathways, List<String> charNames, int rerollsLeft) {
        super(Component.literal("Fate's Wheel"));
        this.pathways    = pathways;
        this.charNames   = charNames;
        this.rerollsLeft = rerollsLeft;

        buildReelLoop();
    }

    /**
     * Called when the server acknowledges a reroll (instead of opening a new screen).
     * Takes the maximum of the server's count and our current count so that Konami
     * bonus rerolls added client-side are never overwritten by the server response.
     */
    public void serverAcknowledgedReroll(int serverRerollsLeft) {
        rerollsLeft = Math.max(rerollsLeft, serverRerollsLeft);
        updateButtonState();
    }

    private void buildReelLoop() {
        reelLoop.clear();
        reelPathwayLoop.clear();
        // Repeat the list 3× so the visual reel never shows an empty gap
        for (int r = 0; r < 3; r++) {
            reelLoop.addAll(charNames);
            reelPathwayLoop.addAll(pathways);
        }
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        int btnY  = cy + PANEL_H - 30;
        int btnW  = 110;
        int btnGap = 12;
        int totalBtns = btnW * 2 + btnGap;
        int btnX = cx + (PANEL_W - totalBtns) / 2;

        acceptButton = addRenderableWidget(Button.builder(
                Component.literal("✔ Accept").withStyle(s -> s.withColor(0xFFFFCC00).withBold(true)),
                b -> onAccept())
                .bounds(btnX, btnY, btnW, 20).build());

        rerollButton = addRenderableWidget(Button.builder(
                Component.literal("↺ Reroll (" + rerollsLeft + " left)").withStyle(s -> s.withColor(0xFFCC88FF)),
                b -> onReroll())
                .bounds(btnX + btnW + btnGap, btnY, btnW, 20).build());

        updateButtonState();
        startSpin();
    }

    /** Non-closable — override all close paths. */
    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public void onClose() {
        // Do nothing — player cannot close this screen until they accept
    }

    // ── Spin logic ────────────────────────────────────────────────────────────

    private void startSpin() {
        spinning = true;
        spinTicks = 0;

        // Pick a random landing index from the middle repeat so we never clip the edges
        int landingIndex = charNames.size() + rand.nextInt(charNames.size());
        // We want the reel to do several full rotations before landing
        // Target = several full laps + landing position
        float fullLaps = charNames.size() * (2 + rand.nextInt(3));
        targetScroll = fullLaps + landingIndex;
        scrollPos = (float) (charNames.size()); // start in the middle loop

        selectedIndex = landingIndex;
        updateButtonState();
    }

    @Override
    public void tick() {
        super.tick();
        if (konamiFlashTicks > 0) konamiFlashTicks--;
        if (!spinning) return;

        spinTicks++;
        float t = (float) spinTicks / SPIN_TICKS_TOTAL;

        if (spinTicks >= SPIN_TICKS_TOTAL) {
            // Snap to final position
            scrollPos = targetScroll;
            spinning = false;
            updateButtonState();
            return;
        }

        // Ease-out interpolation: fast at first, decelerates near the end
        float eased;
        if (spinTicks < SPIN_DECEL_START) {
            // Accelerating / constant phase — linear progress scaled to cover most of the distance
            eased = (float) spinTicks / SPIN_DECEL_START * 0.8f;
        } else {
            // Deceleration phase — ease-out cubic
            float decelT = (float)(spinTicks - SPIN_DECEL_START) / (SPIN_TICKS_TOTAL - SPIN_DECEL_START);
            eased = 0.8f + (1f - (float)Math.pow(1f - decelT, 3)) * 0.2f;
        }

        float startScroll = charNames.size(); // starting position
        scrollPos = startScroll + eased * (targetScroll - startScroll);
    }

    private void updateButtonState() {
        if (acceptButton == null || rerollButton == null) return;
        boolean canInteract = !spinning;
        acceptButton.active = canInteract;
        rerollButton.active = canInteract && rerollsLeft > 0;
        // Update reroll label
        rerollButton.setMessage(rerollsLeft > 0
                ? Component.literal("↺ Reroll (" + rerollsLeft + " left)").withStyle(s -> s.withColor(0xFFCC88FF))
                : Component.literal("↺ Reroll (0 left)").withStyle(ChatFormatting.DARK_GRAY));
    }

    // ── Button actions ────────────────────────────────────────────────────────

    private void onAccept() {
        if (spinning) return;
        int idx = selectedIndex % pathways.size();
        String chosenPathway = pathways.get(idx);
        PacketHandler.sendToServer(new CharSlotRollResultPacket(CharSlotRollResultPacket.ACCEPT, chosenPathway));
        // Screen will be closed by the server after confirming (or just remove it client-side)
        minecraft.setScreen(null);
    }

    private void onReroll() {
        if (spinning || rerollsLeft <= 0) return;
        PacketHandler.sendToServer(new CharSlotRollResultPacket(CharSlotRollResultPacket.REROLL, ""));
        // The server will respond with a new OpenCharSlotRollPacket which re-opens this screen.
        // Pre-emptively decrement locally so the label is correct before the server responds.
        rerollsLeft--;
        updateButtonState();
        startSpin();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    /** Suppress the vanilla background blur — draw a plain dark overlay instead. */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xAA000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        // Panel background + outline
        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, COL_BG);
        g.renderOutline(cx, cy, PANEL_W, PANEL_H, COL_OUTLINE);

        // Title
        Component title = Component.literal("⬡ Fate's Wheel ⬡").withStyle(ChatFormatting.BOLD);
        g.drawCenteredString(font, title, cx + PANEL_W / 2, cy + 10, COL_TITLE);

        Component subtitle = Component.literal("Your characteristic is being decided by fate...")
                .withStyle(ChatFormatting.ITALIC);
        g.drawCenteredString(font, subtitle, cx + PANEL_W / 2, cy + 24, 0xFF9966CC);

        // Divider
        g.fill(cx + 8, cy + 35, cx + PANEL_W - 8, cy + 36, COL_REEL_BORDER);

        renderReel(g, cx, cy, partialTick);

        // Rerolls info
        if (!spinning) {
            int idx = selectedIndex % charNames.size();
            Component landed = Component.literal("Landed on: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(charNames.get(idx)).withStyle(ChatFormatting.WHITE));
            g.drawCenteredString(font, landed, cx + PANEL_W / 2, cy + PANEL_H - 72, 0xFFFFFFFF);
        } else {
            g.drawCenteredString(font,
                    Component.literal("Rolling...").withStyle(ChatFormatting.DARK_PURPLE),
                    cx + PANEL_W / 2, cy + PANEL_H - 72, 0xFFAA66FF);
        }

        // Rerolls remaining info
        Component rerollInfo = Component.literal("Rerolls remaining: " + rerollsLeft)
                .withStyle(rerollsLeft > 0 ? ChatFormatting.YELLOW : ChatFormatting.RED);
        g.drawCenteredString(font, rerollInfo, cx + PANEL_W / 2, cy + PANEL_H - 56, 0xFFFFFFFF);

        // Konami easter-egg flash
        if (konamiFlashTicks > 0) {
            int alpha = Math.min(255, konamiFlashTicks * 6);
            int color = (alpha << 24) | 0x00FFCC00;
            String msg = konamiUsed < 2 ? "★ Fate smiles upon you. +1 Reroll ★" : "★ Fate has no more gifts for you. ★";
            g.drawCenteredString(font, Component.literal(msg).withStyle(ChatFormatting.BOLD),
                    cx + PANEL_W / 2, cy + PANEL_H - 42, color);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderReel(GuiGraphics g, int panelX, int panelY, float partialTick) {
        int reelX = panelX + (PANEL_W - REEL_W) / 2;
        int reelY = panelY + 44;
        int reelWindowH = ROW_H * VISIBLE;

        // Background of reel window
        g.fill(reelX, reelY, reelX + REEL_W, reelY + reelWindowH, COL_REEL_BG);
        g.renderOutline(reelX, reelY, REEL_W, reelWindowH, COL_REEL_BORDER);

        // Enable scissor (clip) so items outside the window are hidden
        g.enableScissor(reelX, reelY, reelX + REEL_W, reelY + reelWindowH);

        // Smooth partial-tick interpolation
        float displayScroll = scrollPos; // tick() already advances scrollPos smoothly

        // Centre row is at VISIBLE/2 (index 2 for 5 rows)
        int centerRow = VISIBLE / 2;
        float startEntry = displayScroll - centerRow;

        for (int row = 0; row < VISIBLE + 2; row++) {
            float entryFloat = startEntry + row;
            int entryIndex = Math.floorMod((int) Math.floor(entryFloat), reelLoop.size());
            if (entryIndex < 0 || entryIndex >= reelLoop.size()) continue;

            float frac = entryFloat - (float) Math.floor(entryFloat);
            int rowY = reelY + (int) ((row - frac) * ROW_H);

            boolean isCentre = (row == centerRow) && !spinning;
            int textColor = isCentre ? 0xFFFFDD00 : 0xFFCCCCCC;
            int bgColor   = isCentre ? COL_HIGHLIGHT : 0x00000000;

            if (bgColor != 0) {
                g.fill(reelX + 2, rowY, reelX + REEL_W - 2, rowY + ROW_H - 2, bgColor);
            }

            if (isCentre) {
                g.renderOutline(reelX + 2, rowY, REEL_W - 4, ROW_H - 2, COL_SEL_BORDER);
            }

            String name = reelLoop.get(entryIndex);
            g.drawCenteredString(font, Component.literal(name).withStyle(isCentre ? ChatFormatting.BOLD : ChatFormatting.RESET),
                    reelX + REEL_W / 2, rowY + (ROW_H - font.lineHeight) / 2, textColor);
        }

        g.disableScissor();

        // Fade strips at top and bottom to create depth
        g.fillGradient(reelX, reelY, reelX + REEL_W, reelY + ROW_H, 0xDD000010, 0x00000010);
        g.fillGradient(reelX, reelY + reelWindowH - ROW_H, reelX + REEL_W, reelY + reelWindowH, 0x00000010, 0xDD000010);

        // Centre selection indicator lines
        int midY = reelY + (reelWindowH / 2) - (ROW_H / 2);
        g.fill(reelX, midY,           reelX + REEL_W, midY + 1,        COL_SEL_BORDER);
        g.fill(reelX, midY + ROW_H - 1, reelX + REEL_W, midY + ROW_H, COL_SEL_BORDER);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

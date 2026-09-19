package de.jakob.lotm.gui.custom.AbilitySeal;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.SetAbilitySealPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Ability-seal selector screen opened for the River owner when clicking
 * "Seal Abilities" on an imprinted player (tier ≥ 2).
 *
 * The owner can toggle up to 2 of the target player's abilities.
 * Clicking Confirm sends {@link SetAbilitySealPacket} and closes the screen.
 */
@OnlyIn(Dist.CLIENT)
public class AbilitySealScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 260;
    private static final int ROW_H   = 20;
    private static final int PADDING = 10;
    private static final int TITLE_H = 28;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final int COL_BG       = 0xEE0A0010;
    private static final int COL_OUTLINE  = 0xFF880022;
    private static final int COL_TITLE    = 0xFFFF4444;
    private static final int COL_ROW_NORM = 0xFF1A1A2E;
    private static final int COL_ROW_SEL  = 0xFF3A0000;
    private static final int COL_ROW_BRD  = 0xFF660011;
    private static final int COL_ROW_SHAD = 0xFF440011;

    // ── Data ──────────────────────────────────────────────────────────────────
    private final String targetUUIDStr;
    private final String targetName;
    private final List<String> abilityIds;
    private final List<String> abilityNames;
    /** Indices of the currently-selected abilities (max 2). */
    private final List<Integer> selected = new ArrayList<>(2);

    // ── Scroll ────────────────────────────────────────────────────────────────
    private int scrollOffset = 0;

    // ── Buttons ───────────────────────────────────────────────────────────────
    private Button confirmButton;
    private Button clearButton;

    public AbilitySealScreen(
            String targetUUIDStr,
            String targetName,
            List<String> abilityIds,
            List<String> abilityNames,
            List<String> currentlySealed) {
        super(Component.literal("Seal Abilities"));
        this.targetUUIDStr = targetUUIDStr;
        this.targetName    = targetName;
        this.abilityIds    = abilityIds;
        this.abilityNames  = abilityNames;

        // Pre-select any abilities that are already sealed
        for (String sealedId : currentlySealed) {
            int idx = abilityIds.indexOf(sealedId);
            if (idx >= 0 && !selected.contains(idx)) selected.add(idx);
        }
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        int btnY = cy + PANEL_H - 24;
        int btnW = 100;

        confirmButton = addRenderableWidget(Button.builder(
                Component.literal("✔ Confirm").withStyle(s -> s.withColor(0xFFFFCC00).withBold(true)),
                b -> doConfirm())
                .bounds(cx + PANEL_W / 2 - btnW - 4, btnY, btnW, 18)
                .build());

        clearButton = addRenderableWidget(Button.builder(
                Component.literal("✗ Clear Seals").withStyle(ChatFormatting.GRAY),
                b -> doClear())
                .bounds(cx + PANEL_W / 2 + 4, btnY, btnW, 18)
                .build());
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;
        int listTop  = cy + TITLE_H;
        int listH    = PANEL_H - TITLE_H - 30;
        int visible  = listH / ROW_H;

        if (button == 0
                && mouseX >= cx + PADDING - 2 && mouseX <= cx + PANEL_W - PADDING + 2
                && mouseY >= listTop && mouseY <= listTop + visible * ROW_H) {

            int row = (int)((mouseY - listTop) / ROW_H);
            int idx = row + scrollOffset;
            if (idx >= 0 && idx < abilityIds.size()) {
                if (selected.contains(idx)) {
                    selected.remove(Integer.valueOf(idx));
                } else if (selected.size() < 2) {
                    selected.add(idx);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listH   = PANEL_H - TITLE_H - 30;
        int visible = listH / ROW_H;
        int maxScroll = Math.max(0, abilityIds.size() - visible);
        if (scrollY < 0 && scrollOffset < maxScroll) {
            scrollOffset++;
            return true;
        } else if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void doConfirm() {
        List<String> ids = new ArrayList<>();
        for (int idx : selected) ids.add(abilityIds.get(idx));
        PacketHandler.sendToServer(new SetAbilitySealPacket(targetUUIDStr, ids));
        onClose();
    }

    private void doClear() {
        selected.clear();
        PacketHandler.sendToServer(new SetAbilitySealPacket(targetUUIDStr, List.of()));
        onClose();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xAA000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        int cx = (width  - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        // ── Panel ──
        g.fill(cx - 2, cy - 2, cx + PANEL_W + 2, cy + PANEL_H + 2, COL_OUTLINE);
        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, COL_BG);

        // ── Title ──
        Component titleLine = Component.literal("Seal Abilities — ").withStyle(s -> s.withColor(COL_TITLE).withBold(true))
                .append(Component.literal(targetName).withStyle(ChatFormatting.WHITE));
        g.drawCenteredString(font, titleLine, cx + PANEL_W / 2, cy + 8, 0xFFFFFFFF);

        // Subtitle
        int selCount = selected.size();
        String hint = selCount == 0 ? "Click up to 2 abilities to seal"
                : selCount == 1     ? "1 / 2 selected — click another or Confirm"
                                    : "2 / 2 selected";
        g.drawCenteredString(font, Component.literal(hint).withStyle(ChatFormatting.GRAY),
                cx + PANEL_W / 2, cy + 18, 0xFFAAAAAA);

        // ── Ability list ──
        int listTop = cy + TITLE_H;
        int listH   = PANEL_H - TITLE_H - 30;
        int visible = listH / ROW_H;

        // Scissor the list area
        g.enableScissor(cx + PADDING - 2, listTop, cx + PANEL_W - PADDING + 2, listTop + visible * ROW_H);

        for (int i = 0; i < visible; i++) {
            int idx = i + scrollOffset;
            if (idx >= abilityIds.size()) break;

            int rowY = listTop + i * ROW_H;
            boolean sel = selected.contains(idx);

            // Row background
            int rowBg  = sel ? COL_ROW_SEL : COL_ROW_NORM;
            int rowBrd = sel ? COL_OUTLINE  : COL_ROW_SHAD;
            g.fill(cx + PADDING - 2, rowY, cx + PANEL_W - PADDING + 2, rowY + ROW_H - 1, rowBrd);
            g.fill(cx + PADDING - 1, rowY + 1, cx + PANEL_W - PADDING + 1, rowY + ROW_H - 2, rowBg);

            // Checkbox
            String check = sel ? "§c[■]" : "§8[ ]";
            g.drawString(font, check, cx + PADDING + 1, rowY + 6, 0xFFFFFFFF, false);

            // Ability name
            int textColor = sel ? 0xFFFF6666 : 0xFFCCCCCC;
            String name = abilityNames.get(idx);
            g.drawString(font, name, cx + PADDING + 22, rowY + 6, textColor, false);

            // Hover glow
            if (mouseX >= cx + PADDING - 2 && mouseX <= cx + PANEL_W - PADDING + 2
                    && mouseY >= rowY && mouseY < rowY + ROW_H) {
                g.fill(cx + PADDING - 2, rowY, cx + PANEL_W - PADDING + 2, rowY + ROW_H - 1, 0x22FFFFFF);
            }
        }

        g.disableScissor();

        // Scroll arrows if needed
        if (abilityIds.size() > visible) {
            int arrowX = cx + PANEL_W - PADDING - 8;
            if (scrollOffset > 0) {
                g.drawString(font, "▲", arrowX, listTop, 0xFFAAAAAA, false);
            }
            int maxScroll = abilityIds.size() - visible;
            if (scrollOffset < maxScroll) {
                g.drawString(font, "▼", arrowX, listTop + visible * ROW_H - 10, 0xFFAAAAAA, false);
            }
        }

        // Divider above buttons
        g.fill(cx + PADDING, cy + PANEL_H - 28, cx + PANEL_W - PADDING, cy + PANEL_H - 27, COL_ROW_BRD);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

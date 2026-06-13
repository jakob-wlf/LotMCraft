package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.ToggleSefirotAuthorityAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

/**
 * GUI for the Chaos Sea sefirot authority ability.
 *
 * Shows cross-path abilities from the Sun, Tyrant and Visionary pathways,
 * grouped by path with a coloured label above each row.
 *
 * Available sequence depth scales with the owner's sequence (same rule as
 * all other authority screens):
 *   Seq 2  →  seq-4 abilities and weaker (higher seq numbers)
 *   Seq 1  →  seq-2 abilities and weaker
 *   Seq 0  →  all abilities (seq 1+)
 */
@OnlyIn(Dist.CLIENT)
public class ChaosSeaAuthorityScreen extends AbstractContainerScreen<ChaosSeaAuthorityMenu> {

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int PANEL_WIDTH   = 248;
    private static final int PANEL_HEIGHT  = 220;
    private static final int ICON_SIZE     = 16;
    private static final int ICON_GAP      = 2;
    private static final int ICON_STEP     = ICON_SIZE + ICON_GAP;
    private static final int CONTENT_LEFT  = 8;
    private static final int ICONS_AREA_W  = PANEL_WIDTH - CONTENT_LEFT - 8; // 232 px
    private static final int ICONS_PER_ROW = ICONS_AREA_W / ICON_STEP;       // 12

    // ── Sun / Tyrant / Visionary colour palette ───────────────────────────────
    /** Panel background – deep space navy */
    private static final int COLOR_BG            = 0xDD000810;
    /** Sun gold outline */
    private static final int COLOR_OUTLINE       = 0xFF886622;
    /** Title text – sun gold */
    private static final int COLOR_TITLE         = 0xFFFFAD33;
    /** Hint text – visionary ice */
    private static final int COLOR_HINT          = 0xFF88AABB;
    /** Divider – dark gold */
    private static final int COLOR_DIVIDER       = 0xFF442200;
    /** Ability slot: active border – tyrant blue */
    private static final int COLOR_ACTIVE_BORDER = 0xFF336DFF;
    /** Ability slot: active background – dark navy */
    private static final int COLOR_ACTIVE_BG     = 0xFF001133;
    /** Ability slot: inactive border */
    private static final int COLOR_INACT_BORDER  = 0xFF223355;
    /** Height of the imprint bar section (only shown when imprint > 0). */
    private static final int IMPRINT_BAR_HEIGHT  = 12;

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<PathSection> sections = new ArrayList<>();
    private final List<String> unlocked = new ArrayList<>();

    private int scrollOffset       = 0;
    private int totalContentHeight = 0;

    private record PathSection(String pathId, String displayName, int color, List<Ability> abilities) {}

    // ── Constructor ───────────────────────────────────────────────────────────

    public ChaosSeaAuthorityScreen(ChaosSeaAuthorityMenu menu,
                                    Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width  - imageWidth)  / 2;
        this.topPos  = (this.height - imageHeight) / 2;

        // Build path sections in the order the server provided them
        sections.clear();
        List<String> neighborPaths = menu.getNeighborPaths();
        Map<String, List<Ability>> byPath = new LinkedHashMap<>();
        for (String np : neighborPaths) byPath.put(np, new ArrayList<>());

        for (String id : menu.getAvailableIds()) {
            Ability a = LOTMCraft.abilityHandler.getById(id);
            if (a == null) continue;
            String path = null;
            for (String np : neighborPaths) {
                if (a.getRequirements().containsKey(np)) { path = np; break; }
            }
            if (path == null) continue;
            byPath.get(path).add(a);
        }

        // Sort: lowest sequence number first (strongest to the left)
        for (String np : neighborPaths) {
            List<Ability> sorted = byPath.get(np);
            if (sorted.isEmpty()) continue;
            sorted.sort((a, b) -> Integer.compare(
                    a.getRequirements().getOrDefault(np, 0),
                    b.getRequirements().getOrDefault(np, 0)));
            PathwayInfos info = BeyonderData.pathwayInfos.get(np);
            String display = (info != null) ? info.getName() : capitalize(np);
            int color      = (info != null) ? info.color()   : 0xFFFFFFFF;
            sections.add(new PathSection(np, display, color, sorted));
        }

        totalContentHeight = computeContentHeight();
        int visibleH  = PANEL_HEIGHT - 35 - (menu.getImprintPercent() > 0 ? IMPRINT_BAR_HEIGHT : 0);
        int maxScroll = Math.max(0, totalContentHeight - visibleH);

        clearWidgets();

        addRenderableWidget(Button.builder(
                Component.literal("▲"),
                b -> { if (scrollOffset > 0) scrollOffset = Math.max(0, scrollOffset - 10); })
                .bounds(leftPos + PANEL_WIDTH - 14, topPos + 30, 12, 12).build());
        addRenderableWidget(Button.builder(
                Component.literal("▼"),
                b -> { if (scrollOffset < maxScroll) scrollOffset = Math.min(maxScroll, scrollOffset + 10); })
                .bounds(leftPos + PANEL_WIDTH - 14, topPos + PANEL_HEIGHT - 22, 12, 12).build());

        // Envisioning button – top right
        addRenderableWidget(Button.builder(
                Component.literal("Envisioning").withStyle(ChatFormatting.GOLD),
                b -> Minecraft.getInstance().setScreen(new EnvisioningScreen()))
                .bounds(leftPos + PANEL_WIDTH - 90, topPos + 4, 86, 14).build());

        unlocked.clear();
        unlocked.addAll(menu.getUnlockedIds());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private int computeContentHeight() {
        int h = 0;
        for (PathSection sec : sections) {
            h += 11;
            int rows = (int) Math.ceil((double) sec.abilities().size() / ICONS_PER_ROW);
            h += rows * ICON_STEP;
            h += 5;
        }
        return h;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("_", " ");
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + PANEL_WIDTH, topPos + PANEL_HEIGHT, COLOR_BG);
        g.renderOutline(leftPos, topPos, PANEL_WIDTH, PANEL_HEIGHT, COLOR_OUTLINE);

        // Tricolor accent strip across the top edge (Sun | Tyrant | Visionary)
        int third = PANEL_WIDTH / 3;
        g.fill(leftPos,           topPos, leftPos + third,         topPos + 3, 0xFFFFAD33);
        g.fill(leftPos + third,   topPos, leftPos + third * 2,     topPos + 3, 0xFF336DFF);
        g.fill(leftPos + third*2, topPos, leftPos + PANEL_WIDTH,   topPos + 3, 0xFFD0F0FF);

        // Title
        Component titleText = Component.literal("Chaos Sea Authority").withStyle(ChatFormatting.BOLD);
        g.drawString(this.font, titleText, leftPos + 6, topPos + 7, COLOR_TITLE, true);

        // Hint
        Component hint = Component.literal("Enable abilities to add them to Introspect").withStyle(ChatFormatting.GRAY);
        g.drawString(this.font, hint, leftPos + CONTENT_LEFT, topPos + 18, COLOR_HINT, false);

        // Divider
        g.fill(leftPos + 4, topPos + 27, leftPos + PANEL_WIDTH - 4, topPos + 28, COLOR_DIVIDER);

        // Mental Imprint bar
        renderImprintBar(g);

        renderSections(g, mouseX, mouseY);
    }

    private void renderImprintBar(GuiGraphics g) {
        int imprint = menu.getImprintPercent();
        if (imprint <= 0) return;

        int barY = topPos + 29;
        int barX = leftPos + CONTENT_LEFT;
        int barW = PANEL_WIDTH - CONTENT_LEFT - 22;

        Component label = Component.literal("Mental Imprint: " + imprint + "%").withStyle(ChatFormatting.DARK_PURPLE);
        g.drawString(this.font, label, barX, barY, 0xFFAA55FF, false);

        int trackY = barY + 9;
        g.fill(barX, trackY, barX + barW, trackY + 3, 0xFF330033);
        int fillW = (int) (barW * (imprint / 100.0f));
        g.fill(barX, trackY, barX + fillW, trackY + 3, 0xFF9933CC);
        g.renderOutline(barX, trackY, barW, 3, 0xFF660066);
    }

    private int getImprintBarOffset() {
        return menu.getImprintPercent() > 0 ? IMPRINT_BAR_HEIGHT : 0;
    }

    private void renderSections(GuiGraphics g, int mouseX, int mouseY) {
        int clipTop    = topPos + 29 + getImprintBarOffset();
        int clipBottom = topPos + PANEL_HEIGHT - 10;
        int curY = clipTop - scrollOffset;

        if (sections.isEmpty()) {
            g.drawString(this.font,
                    Component.literal("No abilities available at your current sequence.").withStyle(ChatFormatting.GRAY),
                    leftPos + CONTENT_LEFT, clipTop + 6, COLOR_HINT, false);
            return;
        }

        for (PathSection sec : sections) {
            if (curY + 9 >= clipTop && curY < clipBottom) {
                g.drawString(this.font,
                        Component.literal(sec.displayName()).withStyle(ChatFormatting.BOLD),
                        leftPos + CONTENT_LEFT, curY, sec.color(), true);
            }
            curY += 11;

            int col = 0;
            for (Ability ability : sec.abilities()) {
                int x = leftPos + CONTENT_LEFT + col * ICON_STEP;
                int y = curY;

                if (y + ICON_SIZE > clipTop && y < clipBottom) {
                    renderAbilitySlot(g, ability, x, y, mouseX, mouseY);
                }

                col++;
                if (col >= ICONS_PER_ROW) { col = 0; curY += ICON_STEP; }
            }
            if (col > 0) curY += ICON_STEP;
            curY += 5;
        }
    }

    private void renderAbilitySlot(GuiGraphics g, Ability ability,
                                   int x, int y, int mouseX, int mouseY) {
        boolean isUnlocked = unlocked.contains(ability.getId());

        g.fill(x, y, x + ICON_SIZE, y + ICON_SIZE,
                isUnlocked ? COLOR_ACTIVE_BG : 0xFF001020);
        g.renderOutline(x, y, ICON_SIZE, ICON_SIZE,
                isUnlocked ? COLOR_ACTIVE_BORDER : COLOR_INACT_BORDER);

        if (ability.getTextureLocation() != null) {
            g.blit(ability.getTextureLocation(), x, y, 0, 0,
                    ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        } else {
            g.drawString(this.font, "?", x + 4, y + 4, 0xFFFFFFFF, false);
        }

        if (mouseX >= x && mouseX < x + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE) {
            g.renderOutline(x, y, ICON_SIZE, ICON_SIZE, 0xFFFFCC66);
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(ability.getName().withStyle(
                    isUnlocked ? ChatFormatting.GOLD : ChatFormatting.WHITE, ChatFormatting.BOLD));
            tooltip.add(Component.literal(isUnlocked
                    ? "§6Enabled \u2014 visible in Introspect"
                    : "§7Left-click to enable"));
            if (isUnlocked) tooltip.add(Component.literal("§cRight-click to disable"));
            g.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    // ── Input ──────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            int clipTop    = topPos + 29 + getImprintBarOffset();
            int clipBottom = topPos + PANEL_HEIGHT - 10;
            int curY = clipTop - scrollOffset;

            for (PathSection sec : sections) {
                curY += 11;
                int col = 0;
                for (Ability ability : sec.abilities()) {
                    int x = leftPos + CONTENT_LEFT + col * ICON_STEP;
                    int y = curY;

                    if (y + ICON_SIZE > clipTop && y < clipBottom) {
                        if (mouseX >= x && mouseX < x + ICON_SIZE
                                && mouseY >= y && mouseY < y + ICON_SIZE) {
                            boolean isUnlocked = unlocked.contains(ability.getId());
                            if (button == 1 && isUnlocked) {
                                unlocked.remove(ability.getId());
                                PacketHandler.sendToServer(
                                        new ToggleSefirotAuthorityAbilityPacket(ability.getId(), false));
                            } else if (button == 0 && !isUnlocked) {
                                unlocked.add(ability.getId());
                                PacketHandler.sendToServer(
                                        new ToggleSefirotAuthorityAbilityPacket(ability.getId(), true));
                            }
                            return true;
                        }
                    }

                    col++;
                    if (col >= ICONS_PER_ROW) { col = 0; curY += ICON_STEP; }
                }
                if (col > 0) curY += ICON_STEP;
                curY += 5;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double scrollX, double scrollY) {
        int visibleH  = PANEL_HEIGHT - 35 - getImprintBarOffset();
        int maxScroll = Math.max(0, totalContentHeight - visibleH);
        scrollOffset  = (int) Math.max(0,
                Math.min(maxScroll, scrollOffset - scrollY * 10));
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // suppress default labels
    }
}

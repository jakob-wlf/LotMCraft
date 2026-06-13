package de.jakob.lotm.gui.custom.SefirotAuthority;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestGatheringScreenPacket;
import de.jakob.lotm.network.packets.toServer.ToggleSefirotAuthorityAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

/**
 * GUI for the Sefirot Authority ability.
 *
 * Shows cross-path abilities grouped by path, with a coloured path-name label
 * above each row. Abilities are sorted highest-sequence first (left) to
 * lowest (right), wrapping to the next line if needed.
 *
 * Left-click an ability to enable it; right-click to disable it.
 * Enabled abilities appear in the Introspect available-abilities panel.
 */
@OnlyIn(Dist.CLIENT)
public class SefirotAuthorityScreen extends AbstractContainerScreen<SefirotAuthorityMenu> {

    private static final int PANEL_WIDTH  = 248;
    private static final int PANEL_HEIGHT = 220;
    private static final int ICON_SIZE    = 16;
    private static final int ICON_GAP     = 2;
    private static final int ICON_STEP    = ICON_SIZE + ICON_GAP;
    private static final int CONTENT_LEFT = 8;
    private static final int ICONS_AREA_W = PANEL_WIDTH - CONTENT_LEFT - 8; // 232px → fits ~12 icons
    private static final int ICONS_PER_ROW = ICONS_AREA_W / ICON_STEP;      // 12

    /** One entry per neighbouring path: ordered list of abilities (high seq → low seq). */
    private final List<PathSection> sections = new ArrayList<>();
    /** Currently unlocked IDs — updated optimistically on click. */
    private final List<String> unlocked = new ArrayList<>();

    private int scrollOffset = 0;
    private int totalContentHeight = 0;

    /** Pre-computed layout unit for one neighbouring path. */
    private record PathSection(String pathId, String displayName, int color, List<Ability> abilities) {}

    public SefirotAuthorityScreen(SefirotAuthorityMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width  - imageWidth)  / 2;
        this.topPos  = (this.height - imageHeight) / 2;

        // Build path sections in neighbor-path order so we never get spurious sections
        // from abilities that have multiple-path requirements (e.g. common abilities).
        sections.clear();
        List<String> neighborPaths = menu.getNeighborPaths();
        Map<String, List<Ability>> byPath = new LinkedHashMap<>();
        for (String np : neighborPaths) byPath.put(np, new ArrayList<>());

        for (String id : menu.getAvailableIds()) {
            Ability a = LOTMCraft.abilityHandler.getById(id);
            if (a == null) continue;
            // Find the first neighbor path this ability belongs to
            String path = null;
            for (String np : neighborPaths) {
                if (a.getRequirements().containsKey(np)) { path = np; break; }
            }
            if (path == null) continue; // ability has no matching neighbor path; skip
            byPath.get(path).add(a);
        }

        // Sort abilities in each section: highest sequence number first (weakest → leftmost isn't
        // what we want; the user said highest seq left, so seq 9 left and seq 1 right).
        // In LotM higher number = weaker, so "highest seq number first" = weakest first.
        // Re-reading user: "highest seq to the left lowest seq abilitys on the right" —
        // i.e. seq 9 on left, seq 4 on right for a seq-2 player. So sort descending by req number.
        for (String np : neighborPaths) {
            List<Ability> sorted = byPath.get(np);
            if (sorted.isEmpty()) continue;
            sorted.sort((a, b) -> Integer.compare(
                    b.getRequirements().getOrDefault(np, 0),
                    a.getRequirements().getOrDefault(np, 0)));
            PathwayInfos info = BeyonderData.pathwayInfos.get(np);
            String display = (info != null) ? info.getName() : capitalize(np);
            int color = (info != null) ? info.color() : 0xFFFFFFFF;
            sections.add(new PathSection(np, display, color, sorted));
        }

        // Calculate total content height for scrolling
        totalContentHeight = computeContentHeight();
        int visibleH = PANEL_HEIGHT - 35;
        int maxScroll = Math.max(0, totalContentHeight - visibleH);

        clearWidgets();
        addRenderableWidget(Button.builder(Component.literal("▲"), b -> { if (scrollOffset > 0) scrollOffset = Math.max(0, scrollOffset - 10); })
                .bounds(leftPos + PANEL_WIDTH - 14, topPos + 30, 12, 12).build());
        addRenderableWidget(Button.builder(Component.literal("▼"), b -> { if (scrollOffset < maxScroll) scrollOffset = Math.min(maxScroll, scrollOffset + 10); })
                .bounds(leftPos + PANEL_WIDTH - 14, topPos + PANEL_HEIGHT - 22, 12, 12).build());

        // Gatherings button — only shown for sefirah_castle owner
        if ("sefirah_castle".equals(menu.getSefirotName())) {
            addRenderableWidget(Button.builder(
                    Component.literal("Gatherings").withStyle(ChatFormatting.LIGHT_PURPLE),
                    b -> { PacketHandler.sendToServer(new RequestGatheringScreenPacket()); this.onClose(); }
            ).bounds(leftPos + 4, topPos + PANEL_HEIGHT - 20, 80, 16).build());

            addRenderableWidget(Button.builder(
                    Component.literal("Call upon the Grey Fog").withStyle(ChatFormatting.GRAY),
                    b -> net.minecraft.client.Minecraft.getInstance().setScreen(new GreyFogScreen(this))
            ).bounds(leftPos + 88, topPos + PANEL_HEIGHT - 20, 120, 16).build());
        }

        unlocked.clear();
        unlocked.addAll(menu.getUnlockedIds());
    }

    private int computeContentHeight() {
        int h = 0;
        for (PathSection sec : sections) {
            h += 11; // label line
            int rows = (int) Math.ceil((double) sec.abilities().size() / ICONS_PER_ROW);
            h += rows * ICON_STEP;
            h += 5;  // section gap
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
        // Panel background
        g.fill(leftPos, topPos, leftPos + PANEL_WIDTH, topPos + PANEL_HEIGHT, 0xDD000000);
        g.renderOutline(leftPos, topPos, PANEL_WIDTH, PANEL_HEIGHT, 0xFF8A6A2E);

        // Title
        Component titleText = Component.literal("Sefirot Authority").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        g.drawString(this.font, titleText,
                leftPos + PANEL_WIDTH / 2 - font.width(titleText) / 2, topPos + 6, 0xFFFFDD88, true);

        // Hint
        Component hint = Component.literal("Enable abilities to add them to Introspect").withStyle(ChatFormatting.GRAY);
        g.drawString(this.font, hint, leftPos + CONTENT_LEFT, topPos + 18, 0xFFAAAAAA, false);

        // Divider
        g.fill(leftPos + 4, topPos + 27, leftPos + PANEL_WIDTH - 4, topPos + 28, 0xFF8A6A2E);

        renderSections(g, mouseX, mouseY);
    }

    private void renderSections(GuiGraphics g, int mouseX, int mouseY) {
        int clipTop    = topPos + 29;
        int clipBottom = topPos + PANEL_HEIGHT - 10;
        int curY = clipTop - scrollOffset;

        if (sections.isEmpty()) {
            g.drawString(this.font,
                    Component.literal("No abilities available at your current sequence.").withStyle(ChatFormatting.GRAY),
                    leftPos + CONTENT_LEFT, clipTop + 6, 0xFF888888, false);
            return;
        }

        for (PathSection sec : sections) {
            // Path label
            if (curY + 9 >= clipTop && curY < clipBottom) {
                // Coloured path name
                g.drawString(this.font,
                        Component.literal(sec.displayName()).withStyle(ChatFormatting.BOLD),
                        leftPos + CONTENT_LEFT, curY, sec.color(), true);
            }
            curY += 11;

            // Ability icons in this section
            int col = 0;
            for (Ability ability : sec.abilities()) {
                int x = leftPos + CONTENT_LEFT + col * ICON_STEP;
                int y = curY;

                if (y + ICON_SIZE > clipTop && y < clipBottom) {
                    renderAbilitySlot(g, ability, x, y, mouseX, mouseY);
                }

                col++;
                if (col >= ICONS_PER_ROW) {
                    col = 0;
                    curY += ICON_STEP;
                }
            }
            // Advance past the final partial row
            if (col > 0) curY += ICON_STEP;
            curY += 5; // section gap
        }
    }

    private void renderAbilitySlot(GuiGraphics g, Ability ability, int x, int y, int mouseX, int mouseY) {
        boolean isUnlocked = unlocked.contains(ability.getId());

        // Background + border
        g.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, isUnlocked ? 0xFF3A2A00 : 0xFF222222);
        g.renderOutline(x, y, ICON_SIZE, ICON_SIZE, isUnlocked ? 0xFFD4A017 : 0xFF555555);

        // Icon
        if (ability.getTextureLocation() != null) {
            g.blit(ability.getTextureLocation(), x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        } else {
            g.drawString(this.font, "?", x + 4, y + 4, 0xFFFFFFFF, false);
        }

        // Hover highlight + tooltip
        if (mouseX >= x && mouseX < x + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE) {
            g.renderOutline(x, y, ICON_SIZE, ICON_SIZE, 0xFFFFFFFF);

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(ability.getName().withStyle(isUnlocked ? ChatFormatting.GOLD : ChatFormatting.WHITE, ChatFormatting.BOLD));
            tooltip.add(Component.literal(isUnlocked
                    ? "§aEnabled \u2014 visible in Introspect"
                    : "§7Left-click to enable"));
            if (isUnlocked) tooltip.add(Component.literal("§cRight-click to disable"));
            g.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    // ── Input ──────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            int clipTop    = topPos + 29;
            int clipBottom = topPos + PANEL_HEIGHT - 10;
            int curY = clipTop - scrollOffset;

            for (PathSection sec : sections) {
                curY += 11; // skip label

                int col = 0;
                for (Ability ability : sec.abilities()) {
                    int x = leftPos + CONTENT_LEFT + col * ICON_STEP;
                    int y = curY;

                    if (y + ICON_SIZE > clipTop && y < clipBottom) {
                        if (mouseX >= x && mouseX < x + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE) {
                            boolean isUnlocked = unlocked.contains(ability.getId());
                            if (button == 1 && isUnlocked) {
                                unlocked.remove(ability.getId());
                                PacketHandler.sendToServer(new ToggleSefirotAuthorityAbilityPacket(ability.getId(), false));
                            } else if (button == 0 && !isUnlocked) {
                                unlocked.add(ability.getId());
                                PacketHandler.sendToServer(new ToggleSefirotAuthorityAbilityPacket(ability.getId(), true));
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleH = PANEL_HEIGHT - 35;
        int maxScroll = Math.max(0, totalContentHeight - visibleH);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 10));
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


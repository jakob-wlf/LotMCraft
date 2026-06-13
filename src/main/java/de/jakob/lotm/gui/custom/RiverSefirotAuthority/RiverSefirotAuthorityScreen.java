package de.jakob.lotm.gui.custom.RiverSefirotAuthority;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestRiverBlessingScreenPacket;
import de.jakob.lotm.network.packets.toServer.RequestRiverImprintScreenPacket;
import de.jakob.lotm.network.packets.toServer.RequestRiverVaultScreenPacket;
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
 * GUI for the River of Eternal Darkness sefirot authority ability.
 *
 * Mirrors {@code SefirotAuthorityScreen} but uses river-themed colours and adds
 * a "Death Imprints" tab at the top that opens the RiverAuthorityMenu (imprint
 * list screen).
 *
 * Shows Death, Darkness, and Twilight Giant path abilities.
 * Available sequence depth scales with the owner's sequence:
 *   Seq 2  →  seq-4 abilities and weaker (higher numbers)
 *   Seq 1  →  seq-2 abilities and weaker
 *   Seq 0  →  all abilities (seq 1+)
 */
@OnlyIn(Dist.CLIENT)
public class RiverSefirotAuthorityScreen extends AbstractContainerScreen<RiverSefirotAuthorityMenu> {

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int PANEL_WIDTH   = 248;
    private static final int PANEL_HEIGHT  = 220;
    private static final int ICON_SIZE     = 16;
    private static final int ICON_GAP      = 2;
    private static final int ICON_STEP     = ICON_SIZE + ICON_GAP;
    private static final int CONTENT_LEFT  = 8;
    private static final int ICONS_AREA_W  = PANEL_WIDTH - CONTENT_LEFT - 8; // 232 px
    private static final int ICONS_PER_ROW = ICONS_AREA_W / ICON_STEP;       // 12

    // ── River colour palette ──────────────────────────────────────────────────
    /** Panel outline – deep teal-blue */
    private static final int COLOR_OUTLINE       = 0xFF004466;
    /** Title text – bright aqua */
    private static final int COLOR_TITLE         = 0xFF66DDFF;
    /** Divider / hint lines */
    private static final int COLOR_DIVIDER       = 0xFF003355;
    /** Ability slot: active border */
    private static final int COLOR_ACTIVE_BORDER = 0xFF0099CC;
    /** Ability slot: active background */
    private static final int COLOR_ACTIVE_BG     = 0xFF001A33;
    /** Ability slot: inactive border */
    private static final int COLOR_INACT_BORDER  = 0xFF334455;
    /** "Death Imprints" tab inactive fill */
    private static final int COLOR_TAB_BG        = 0xFF001122;
    /** "Death Imprints" tab active fill */
    private static final int COLOR_TAB_ACTIVE_BG = 0xFF003355;

    // ── State ─────────────────────────────────────────────────────────────────
    /** One entry per neighbouring path: path label + ordered list of abilities. */
    private final List<PathSection> sections = new ArrayList<>();
    /** Currently-unlocked ability IDs (optimistically updated on click). */
    private final List<String> unlocked = new ArrayList<>();

    private int scrollOffset     = 0;
    private int totalContentHeight = 0;

    /** Pre-computed layout unit for one neighbouring path. */
    private record PathSection(String pathId, String displayName, int color, List<Ability> abilities) {}

    // ── Constructor ───────────────────────────────────────────────────────────

    public RiverSefirotAuthorityScreen(RiverSefirotAuthorityMenu menu,
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

        // Sort: lowest sequence number first (strongest left)
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
        int visibleH  = PANEL_HEIGHT - 40; // content area (below tabs + hint)
        int maxScroll = Math.max(0, totalContentHeight - visibleH);

        clearWidgets();

        // Scroll buttons
        addRenderableWidget(Button.builder(
                Component.literal("▲"),
                b -> { if (scrollOffset > 0) scrollOffset = Math.max(0, scrollOffset - 10); })
                .bounds(leftPos + PANEL_WIDTH - 14, topPos + 36, 12, 12).build());
        addRenderableWidget(Button.builder(
                Component.literal("▼"),
                b -> { if (scrollOffset < maxScroll) scrollOffset = Math.min(maxScroll, scrollOffset + 10); })
                .bounds(leftPos + PANEL_WIDTH - 14, topPos + PANEL_HEIGHT - 22, 12, 12).build());

        // "Death Imprints" tab button at the top-right of the panel
        addRenderableWidget(Button.builder(
                Component.literal("☠ Death Imprints").withStyle(ChatFormatting.DARK_PURPLE),
                b -> openImprintScreen())
                .bounds(leftPos + PANEL_WIDTH - 98, topPos + 4, 94, 14).build());

        // "Blessings" button at the bottom-left of the panel
        addRenderableWidget(Button.builder(
                Component.literal("✦ Blessings").withStyle(ChatFormatting.AQUA),
                b -> openBlessingScreen())
                .bounds(leftPos + 4, topPos + PANEL_HEIGHT - 20, 74, 16).build());

        // "Soul Vault" button to the right of Blessings
        addRenderableWidget(Button.builder(
                Component.literal("\u26B1 Soul Vault").withStyle(ChatFormatting.DARK_PURPLE),
                b -> openVaultScreen())
                .bounds(leftPos + 82, topPos + PANEL_HEIGHT - 20, 74, 16).build());

        unlocked.clear();
        unlocked.addAll(menu.getUnlockedIds());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void openImprintScreen() {
        PacketHandler.sendToServer(new RequestRiverImprintScreenPacket());
        this.onClose();
    }

    private void openBlessingScreen() {
        PacketHandler.sendToServer(new RequestRiverBlessingScreenPacket());
        this.onClose();
    }

    private void openVaultScreen() {
        PacketHandler.sendToServer(new RequestRiverVaultScreenPacket());
        this.onClose();
    }

    private int computeContentHeight() {
        int h = 0;
        for (PathSection sec : sections) {
            h += 11; // label
            int rows = (int) Math.ceil((double) sec.abilities().size() / ICONS_PER_ROW);
            h += rows * ICON_STEP;
            h += 5;  // gap
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
        g.fill(leftPos, topPos, leftPos + PANEL_WIDTH, topPos + PANEL_HEIGHT, 0xDD000C1A);
        g.renderOutline(leftPos, topPos, PANEL_WIDTH, PANEL_HEIGHT, COLOR_OUTLINE);

        // Title (left of tab button)
        Component titleText = Component.literal("River Authority").withStyle(ChatFormatting.BOLD);
        g.drawString(this.font, titleText, leftPos + 6, topPos + 7, COLOR_TITLE, true);

        // Hint below title
        Component hint = Component.literal("Enable abilities to add them to Introspect").withStyle(ChatFormatting.GRAY);
        g.drawString(this.font, hint, leftPos + CONTENT_LEFT, topPos + 21, 0xFF557799, false);

        // Divider
        g.fill(leftPos + 4, topPos + 30, leftPos + PANEL_WIDTH - 4, topPos + 31, COLOR_DIVIDER);

        renderSections(g, mouseX, mouseY);
    }

    private void renderSections(GuiGraphics g, int mouseX, int mouseY) {
        int clipTop    = topPos + 32;
        int clipBottom = topPos + PANEL_HEIGHT - 10;
        int curY = clipTop - scrollOffset;

        if (sections.isEmpty()) {
            g.drawString(this.font,
                    Component.literal("No abilities available at your current sequence.").withStyle(ChatFormatting.GRAY),
                    leftPos + CONTENT_LEFT, clipTop + 6, 0xFF557799, false);
            return;
        }

        for (PathSection sec : sections) {
            // Path label
            if (curY + 9 >= clipTop && curY < clipBottom) {
                g.drawString(this.font,
                        Component.literal(sec.displayName()).withStyle(ChatFormatting.BOLD),
                        leftPos + CONTENT_LEFT, curY, sec.color(), true);
            }
            curY += 11;

            // Ability icons
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
                isUnlocked ? COLOR_ACTIVE_BG : 0xFF0A1A2A);
        g.renderOutline(x, y, ICON_SIZE, ICON_SIZE,
                isUnlocked ? COLOR_ACTIVE_BORDER : COLOR_INACT_BORDER);

        if (ability.getTextureLocation() != null) {
            g.blit(ability.getTextureLocation(), x, y, 0, 0,
                    ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        } else {
            g.drawString(this.font, "?", x + 4, y + 4, 0xFFFFFFFF, false);
        }

        // Hover
        if (mouseX >= x && mouseX < x + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE) {
            g.renderOutline(x, y, ICON_SIZE, ICON_SIZE, 0xFF88EEFF);
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(ability.getName().withStyle(
                    isUnlocked ? ChatFormatting.AQUA : ChatFormatting.WHITE, ChatFormatting.BOLD));
            tooltip.add(Component.literal(isUnlocked
                    ? "§bEnabled \u2014 visible in Introspect"
                    : "§7Left-click to enable"));
            if (isUnlocked) tooltip.add(Component.literal("§cRight-click to disable"));
            g.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    // ── Input ──────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            int clipTop    = topPos + 32;
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
        int visibleH  = PANEL_HEIGHT - 40;
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

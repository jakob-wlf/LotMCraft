package de.jakob.lotm.gui.custom.RiverAuthority;

import com.mojang.authlib.GameProfile;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestAbilitySealScreenPacket;
import de.jakob.lotm.network.packets.toServer.RiverAuthorityActionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.*;

public class RiverAuthorityScreen extends AbstractContainerScreen<RiverAuthorityMenu> {

    private static final int PANEL_WIDTH  = 280;
    private static final int PANEL_HEIGHT = 260;

    // Left panel: imprint list
    private static final int LIST_X         = 8;
    private static final int LIST_Y         = 30;
    private static final int HEAD_SIZE      = 16;
    private static final int HEAD_GAP       = 4;
    private static final int HEAD_STEP      = HEAD_SIZE + HEAD_GAP;
    private static final int SECTION_HEADER_H = 10;

    // Right panel: action area
    private static final int DETAIL_X    = 154;
    private static final int DETAIL_Y    = 30;
    private static final int DETAIL_W    = PANEL_WIDTH - DETAIL_X - 8;

    private UUID selectedUUID   = null;
    private int  selectedTier   = 0;
    private String selectedName = "";
    private boolean selectedLeakageExempt = false;
    private boolean localGlobalLeakageOff = false;

    /** Pre-computed skull items keyed by player UUID. */
    private final Map<UUID, ItemStack> headStacks = new HashMap<>();

    private Button riversCallButton;
    private Button locateButton;
    private Button sealButton;
    private Button leakageButton;
    private Button globalLeakageButton;

    public RiverAuthorityScreen(RiverAuthorityMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width  - imageWidth)  / 2;
        this.topPos  = (this.height - imageHeight) / 2;

        localGlobalLeakageOff = menu.isGlobalLeakageOff();

        headStacks.clear();
        for (RiverAuthorityMenu.ImprintEntry e : menu.getEntries()) {
            ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
            GameProfile profile = new GameProfile(e.uuid(), e.name());
            skull.set(DataComponents.PROFILE, new ResolvableProfile(profile));
            headStacks.put(e.uuid(), skull);
        }

        // Action buttons: stack 4 buttons (18px each, 4px gap) with 8px bottom margin
        int bx = leftPos + DETAIL_X;
        int by = topPos + PANEL_HEIGHT - 110;  // 4*(18+4)-4+8 = 92px from bottom

        locateButton = addRenderableWidget(Button.builder(
                Component.literal("Locate / Teleport").withStyle(ChatFormatting.AQUA),
                b -> performAction(1)
        ).bounds(bx, by, DETAIL_W, 18).build());

        riversCallButton = addRenderableWidget(Button.builder(
                Component.literal("River's Call").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                b -> performAction(0)
        ).bounds(bx, by + 22, DETAIL_W, 18).build());

        sealButton = addRenderableWidget(Button.builder(
                Component.literal("\u2728 Seal Abilities").withStyle(s -> s.withColor(0xFFAA0000).withBold(true)),
                b -> openSealScreen()
        ).bounds(bx, by + 44, DETAIL_W, 18).build());

        leakageButton = addRenderableWidget(Button.builder(
                Component.literal("Leakage: ON"),
                b -> togglePlayerLeakage()
        ).bounds(bx, by + 66, DETAIL_W, 18).build());

        // Global leakage toggle — spans full panel width, pinned near the bottom
        int gbY = topPos + PANEL_HEIGHT - 24;
        globalLeakageButton = addRenderableWidget(Button.builder(
                Component.literal("Global Leakage: ON"),
                b -> toggleGlobalLeakage()
        ).bounds(leftPos + 4, gbY, PANEL_WIDTH - 8, 18).build());

        updateButtonStates();
    }

    private void openSealScreen() {
        if (selectedUUID == null) return;
        PacketHandler.sendToServer(new RequestAbilitySealScreenPacket(selectedUUID.toString()));
        // The server will respond with OpenAbilitySealScreenPacket which opens the screen
    }

    private void togglePlayerLeakage() {
        if (selectedUUID == null) return;
        PacketHandler.sendToServer(new RiverAuthorityActionPacket(2, selectedUUID));
        // Optimistically flip local state so the button label updates immediately
        selectedLeakageExempt = !selectedLeakageExempt;
        // Also update the menu entry so that re-selecting this player reflects the new state
        menu.setLeakageExempt(selectedUUID, selectedLeakageExempt);
        updateButtonStates();
    }

    private void toggleGlobalLeakage() {
        // Use a zero UUID as placeholder — server ignores targetUUID for action 3
        PacketHandler.sendToServer(new RiverAuthorityActionPacket(3, new java.util.UUID(0, 0)));
        // Optimistically flip local state so the button label updates immediately
        localGlobalLeakageOff = !localGlobalLeakageOff;
        updateButtonStates();
    }

    private void performAction(int actionType) {
        if (selectedUUID == null) return;
        PacketHandler.sendToServer(new RiverAuthorityActionPacket(actionType, selectedUUID));
        this.onClose();
    }

    private void updateButtonStates() {
        locateButton.active     = selectedUUID != null && selectedTier >= 2;
        riversCallButton.active = selectedUUID != null && selectedTier >= 3;
        sealButton.active       = selectedUUID != null && selectedTier >= 2;
        leakageButton.active    = selectedUUID != null;
        leakageButton.setMessage(Component.literal(
                selectedLeakageExempt ? "\u00a7cLeakage: OFF" : "\u00a7aLeakage: ON"));

        boolean globalOff = localGlobalLeakageOff;
        globalLeakageButton.setMessage(Component.literal(
                globalOff ? "\u00a7cGlobal Leakage: OFF" : "\u00a7aGlobal Leakage: ON"));
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Main panel
        g.fill(leftPos, topPos, leftPos + PANEL_WIDTH, topPos + PANEL_HEIGHT, 0xDD000000);
        g.renderOutline(leftPos, topPos, PANEL_WIDTH, PANEL_HEIGHT, 0xFF330033);

        // Title
        Component title = Component.literal("River of Eternal Darkness")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        g.drawString(font, title, leftPos + PANEL_WIDTH / 2 - font.width(title) / 2, topPos + 7, 0xFFCC88FF, true);

        // Divider
        g.fill(leftPos + 4, topPos + 18, leftPos + PANEL_WIDTH - 4, topPos + 19, 0xFF330033);
        // Vertical divider between list and detail
        g.fill(leftPos + DETAIL_X - 6, topPos + 20, leftPos + DETAIL_X - 5, topPos + PANEL_HEIGHT - 30, 0xFF333333);

        // Bottom divider above global button
        g.fill(leftPos + 4, topPos + PANEL_HEIGHT - 28, leftPos + PANEL_WIDTH - 4, topPos + PANEL_HEIGHT - 27, 0xFF330033);

        // Section header: Death Imprints
        g.drawString(font, Component.literal("Death Imprints").withStyle(ChatFormatting.GRAY),
                leftPos + LIST_X, topPos + 21, 0xFFAAAAAA, false);

        renderImprintList(g, mouseX, mouseY);
        renderDetailPanel(g, mouseX, mouseY);
    }

    private void renderImprintList(GuiGraphics g, int mouseX, int mouseY) {
        List<RiverAuthorityMenu.ImprintEntry> entries = menu.getEntries();
        if (entries.isEmpty()) {
            g.drawString(font, Component.literal("No imprints yet.").withStyle(ChatFormatting.DARK_GRAY),
                    leftPos + LIST_X, topPos + LIST_Y, 0xFF555555, false);
            return;
        }

        int maxCols = Math.max(1, (DETAIL_X - LIST_X - 8) / HEAD_STEP);
        int[][] positions = getHeadPositions();
        long onlineCount = entries.stream().filter(RiverAuthorityMenu.ImprintEntry::online).count();
        boolean hasOffline = entries.stream().anyMatch(e -> !e.online());

        // "Online" section header
        g.drawString(font, Component.literal("\u25cf Online").withStyle(ChatFormatting.GREEN),
                leftPos + LIST_X, topPos + LIST_Y, 0xFF55FF55, false);

        // "Offline" section header (below the online block)
        if (hasOffline) {
            int onlineRows = onlineCount == 0 ? 0 : (int)((onlineCount - 1) / maxCols + 1);
            int offlineHeaderY = topPos + LIST_Y + SECTION_HEADER_H + onlineRows * HEAD_STEP + 4;
            g.drawString(font, Component.literal("\u25cf Offline").withStyle(ChatFormatting.DARK_GRAY),
                    leftPos + LIST_X, offlineHeaderY, 0xFF888888, false);
        }

        for (int idx = 0; idx < entries.size(); idx++) {
            RiverAuthorityMenu.ImprintEntry entry = entries.get(idx);
            int x = positions[idx][0];
            int y = positions[idx][1];

            ItemStack skull = headStacks.get(entry.uuid());
            if (skull != null) g.renderFakeItem(skull, x, y);

            // Tier indicator dot (top-right corner)
            g.fill(x + HEAD_SIZE - 4, y, x + HEAD_SIZE, y + 4, tierColor(entry.imprintTier()));

            // Online indicator dot (bottom-left corner)
            if (entry.online()) {
                g.fill(x, y + HEAD_SIZE - 4, x + 4, y + HEAD_SIZE, 0xFF55FF55);
            }

            // Selection highlight
            if (entry.uuid().equals(selectedUUID)) {
                g.renderOutline(x - 1, y - 1, HEAD_SIZE + 2, HEAD_SIZE + 2, 0xFFFF88FF);
            }

            // Hover tooltip
            if (mouseX >= x && mouseX < x + HEAD_SIZE && mouseY >= y && mouseY < y + HEAD_SIZE) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.literal(entry.name()).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
                tooltip.add(Component.literal(entry.online() ? "\u00a7aOnline" : "\u00a78Offline"));
                tooltip.add(Component.literal("Path: " + capitalize(entry.pathway())).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Sequence: " + entry.sequence()).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Imprint Tier: " + entry.imprintTier()).withStyle(tierFormatting(entry.imprintTier())));
                tooltip.add(Component.literal(entry.leakageExempt() ? "\u00a7cLeakage: OFF" : "\u00a7aLeakage: ON"));
                if (!entry.sealedAbilityIds().isEmpty()) {
                    tooltip.add(Component.literal("Sealed:").withStyle(ChatFormatting.DARK_RED));
                    for (String abilityId : entry.sealedAbilityIds()) {
                        de.jakob.lotm.beyonders.abilities.core.Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
                        String abilityName = ability != null
                                ? ability.getName().getString()
                                : capitalize(abilityId.replace("_ability", ""));
                        tooltip.add(Component.literal("  \u2746 " + abilityName).withStyle(ChatFormatting.RED));
                    }
                }
                g.renderTooltip(font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private void renderDetailPanel(GuiGraphics g, int mouseX, int mouseY) {
        int px = leftPos + DETAIL_X;
        int py = topPos + DETAIL_Y;

        if (selectedUUID == null) {
            g.drawString(font, Component.literal("Select a soul").withStyle(ChatFormatting.DARK_GRAY),
                    px, py, 0xFF555555, false);
            return;
        }

        g.drawString(font, Component.literal(selectedName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
                px, py, 0xFFFFFFFF, true);
        g.drawString(font, Component.literal("Tier " + selectedTier).withStyle(tierFormatting(selectedTier)),
                px, py + 12, 0xFFFFFFFF, false);

        // Tier descriptions (kept short to fit DETAIL_W)
        if (selectedTier >= 1) {
            g.drawString(font, Component.literal("§8✓ §7Divination succeeds"),
                    px, py + 26, 0xFFAAAAAA, false);
        }
        if (selectedTier >= 2) {
            g.drawString(font, Component.literal("§8✓ §7Teleport available"),
                    px, py + 36, 0xFFAAAAAA, false);
        }
        if (selectedTier >= 3) {
            g.drawString(font, Component.literal("§8✓ §4River's Call"),
                    px, py + 46, 0xFFAAAAAA, false);
        }

        String leakStr = selectedLeakageExempt ? "§cLeakage: OFF" : "§aLeakage: ON";
        g.drawString(font, Component.literal(leakStr), px, py + 60, 0xFFAAAAAA, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // suppress default labels
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<RiverAuthorityMenu.ImprintEntry> entries = menu.getEntries();
        int[][] positions = getHeadPositions();
        for (int idx = 0; idx < entries.size(); idx++) {
            int x = positions[idx][0];
            int y = positions[idx][1];
            if (mouseX >= x && mouseX < x + HEAD_SIZE && mouseY >= y && mouseY < y + HEAD_SIZE) {
                RiverAuthorityMenu.ImprintEntry e = entries.get(idx);
                selectedUUID = e.uuid();
                selectedTier = e.imprintTier();
                selectedName = e.name();
                selectedLeakageExempt = e.leakageExempt();
                updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Computes absolute screen {x, y} for each entry in menu.getEntries() order.
     * Online entries are grouped first, offline second, each block preceded by a section header.
     */
    private int[][] getHeadPositions() {
        List<RiverAuthorityMenu.ImprintEntry> entries = menu.getEntries();
        int maxCols = Math.max(1, (DETAIL_X - LIST_X - 8) / HEAD_STEP);
        int[][] result = new int[entries.size()][2];

        List<Integer> onlineIdxs  = new ArrayList<>();
        List<Integer> offlineIdxs = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            (entries.get(i).online() ? onlineIdxs : offlineIdxs).add(i);
        }

        int currentY = topPos + LIST_Y + SECTION_HEADER_H;
        for (int pos = 0; pos < onlineIdxs.size(); pos++) {
            int idx = onlineIdxs.get(pos);
            result[idx][0] = leftPos + LIST_X + (pos % maxCols) * HEAD_STEP;
            result[idx][1] = currentY + (pos / maxCols) * HEAD_STEP;
        }
        int onlineRows = onlineIdxs.isEmpty() ? 0 : (onlineIdxs.size() - 1) / maxCols + 1;
        currentY += onlineRows * HEAD_STEP + 4 + SECTION_HEADER_H;
        for (int pos = 0; pos < offlineIdxs.size(); pos++) {
            int idx = offlineIdxs.get(pos);
            result[idx][0] = leftPos + LIST_X + (pos % maxCols) * HEAD_STEP;
            result[idx][1] = currentY + (pos / maxCols) * HEAD_STEP;
        }
        return result;
    }

    private static int tierColor(int tier) {
        return switch (tier) {
            case 1 -> 0xFF886600;
            case 2 -> 0xFF4488FF;
            case 3 -> 0xFFCC0000;
            default -> 0xFF444444;
        };
    }

    private static ChatFormatting tierFormatting(int tier) {
        return switch (tier) {
            case 1 -> ChatFormatting.YELLOW;
            case 2 -> ChatFormatting.AQUA;
            case 3 -> ChatFormatting.DARK_RED;
            default -> ChatFormatting.DARK_GRAY;
        };
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("_", " ");
    }
}


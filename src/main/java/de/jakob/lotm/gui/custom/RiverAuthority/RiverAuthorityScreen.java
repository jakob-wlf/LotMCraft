package de.jakob.lotm.gui.custom.RiverAuthority;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RiverAuthorityActionPacket;
import de.jakob.lotm.network.packets.toServer.RequestAbilitySealScreenPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.core.component.DataComponents;
import com.mojang.authlib.GameProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RiverAuthorityScreen extends AbstractContainerScreen<RiverAuthorityMenu> {

    private static final int PANEL_WIDTH  = 280;
    private static final int PANEL_HEIGHT = 220;

    // Left panel: imprint list
    private static final int LIST_X      = 8;
    private static final int LIST_Y      = 30;
    private static final int HEAD_SIZE   = 16;
    private static final int HEAD_GAP    = 4;
    private static final int HEAD_STEP   = HEAD_SIZE + HEAD_GAP;

    // Right panel: action area
    private static final int DETAIL_X    = 154;
    private static final int DETAIL_Y    = 30;
    private static final int DETAIL_W    = PANEL_WIDTH - DETAIL_X - 8;

    private UUID selectedUUID   = null;
    private int  selectedTier   = 0;
    private String selectedName = "";

    /** Pre-computed skull items per entry index. */
    private final List<ItemStack> headStacks = new ArrayList<>();

    private Button riversCallButton;
    private Button locateButton;
    private Button sealButton;

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

        headStacks.clear();
        for (RiverAuthorityMenu.ImprintEntry e : menu.getEntries()) {
            ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
            GameProfile profile = new GameProfile(e.uuid(), e.name());
            skull.set(DataComponents.PROFILE, new ResolvableProfile(profile));
            headStacks.add(skull);
        }

        // Action buttons: stack 3 buttons (18px each, 4px gap) with 8px bottom margin
        int bx = leftPos + DETAIL_X;
        int by = topPos + PANEL_HEIGHT - 70;  // 3*(18+4)-4+8 = 70px from bottom

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

        updateButtonStates();
    }

    private void openSealScreen() {
        if (selectedUUID == null) return;
        PacketHandler.sendToServer(new RequestAbilitySealScreenPacket(selectedUUID.toString()));
        // The server will respond with OpenAbilitySealScreenPacket which opens the screen
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
        g.fill(leftPos + DETAIL_X - 6, topPos + 20, leftPos + DETAIL_X - 5, topPos + PANEL_HEIGHT - 8, 0xFF333333);

        // Section header: Death Imprints
        g.drawString(font, Component.literal("Death Imprints").withStyle(ChatFormatting.GRAY),
                leftPos + LIST_X, topPos + 21, 0xFFAAAAAA, false);

        renderImprintList(g, mouseX, mouseY);
        renderDetailPanel(g, mouseX, mouseY);
    }

    private void renderImprintList(GuiGraphics g, int mouseX, int mouseY) {
        List<RiverAuthorityMenu.ImprintEntry> entries = menu.getEntries();
        int maxCols = (DETAIL_X - LIST_X - 8) / HEAD_STEP;
        int idx = 0;

        for (RiverAuthorityMenu.ImprintEntry entry : entries) {
            int col = idx % maxCols;
            int row = idx / maxCols;
            int x = leftPos + LIST_X + col * HEAD_STEP;
            int y = topPos + LIST_Y + row * HEAD_STEP;

            if (idx < headStacks.size()) {
                g.renderFakeItem(headStacks.get(idx), x, y);
            }

            // Tier indicator dot
            int dotColor = tierColor(entry.imprintTier());
            g.fill(x + HEAD_SIZE - 4, y, x + HEAD_SIZE, y + 4, dotColor);

            // Selection highlight
            if (entry.uuid().equals(selectedUUID)) {
                g.renderOutline(x - 1, y - 1, HEAD_SIZE + 2, HEAD_SIZE + 2, 0xFFFF88FF);
            }

            // Hover tooltip
            if (mouseX >= x && mouseX < x + HEAD_SIZE && mouseY >= y && mouseY < y + HEAD_SIZE) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.literal(entry.name()).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
                tooltip.add(Component.literal("Path: " + capitalize(entry.pathway())).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Sequence: " + entry.sequence()).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Imprint Tier: " + entry.imprintTier()).withStyle(tierFormatting(entry.imprintTier())));
                g.renderTooltip(font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
            }

            idx++;
        }

        if (entries.isEmpty()) {
            g.drawString(font, Component.literal("No imprints yet.").withStyle(ChatFormatting.DARK_GRAY),
                    leftPos + LIST_X, topPos + LIST_Y, 0xFF555555, false);
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
        // Check head clicks
        List<RiverAuthorityMenu.ImprintEntry> entries = menu.getEntries();
        int maxCols = (DETAIL_X - LIST_X - 8) / HEAD_STEP;
        for (int idx = 0; idx < entries.size(); idx++) {
            int col = idx % maxCols;
            int row = idx / maxCols;
            int x = leftPos + LIST_X + col * HEAD_STEP;
            int y = topPos + LIST_Y + row * HEAD_STEP;
            if (mouseX >= x && mouseX < x + HEAD_SIZE && mouseY >= y && mouseY < y + HEAD_SIZE) {
                RiverAuthorityMenu.ImprintEntry e = entries.get(idx);
                selectedUUID = e.uuid();
                selectedTier = e.imprintTier();
                selectedName = e.name();
                updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

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


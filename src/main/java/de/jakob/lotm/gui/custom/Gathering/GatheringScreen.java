package de.jakob.lotm.gui.custom.Gathering;

import de.jakob.lotm.attachments.GatheringData;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.GatheringActionPacket;
import de.jakob.lotm.network.packets.toServer.GatheringMessagePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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

public class GatheringScreen extends AbstractContainerScreen<GatheringMenu> {

    private static final int PANEL_WIDTH  = 280;
    private static final int PANEL_HEIGHT = 220;

    // Left panel: prayer / member list
    private static final int LIST_X    = 8;
    private static final int LIST_Y    = 30;
    private static final int HEAD_SIZE = 16;
    private static final int HEAD_GAP  = 2;
    private static final int ROW_H     = HEAD_SIZE + HEAD_GAP;

    // Right panel: detail + actions
    private static final int DETAIL_X  = 154;
    private static final int DETAIL_W  = PANEL_WIDTH - DETAIL_X - 8;

    private UUID   selectedUUID = null;
    private String selectedName = "";
    private boolean selectedIsMember = false;

    private final List<ItemStack> headStacks = new ArrayList<>();

    private Button markButton;
    private Button callButton;
    private Button endButton;
    private EditBox messageBox;

    public GatheringScreen(GatheringMenu menu, Inventory playerInventory, Component title) {
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
        for (GatheringMenu.GatheringEntry e : menu.getPrayers()) {
            ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
            skull.set(DataComponents.PROFILE, new ResolvableProfile(new GameProfile(e.uuid(), e.name())));
            headStacks.add(skull);
        }

        int bx = leftPos + DETAIL_X;

        markButton = addRenderableWidget(Button.builder(
                Component.literal("Mark as Member").withStyle(ChatFormatting.GREEN),
                b -> performAction(selectedIsMember ? GatheringActionPacket.UNMARK : GatheringActionPacket.MARK, selectedUUID)
        ).bounds(bx, topPos + PANEL_HEIGHT - 78, DETAIL_W, 18).build());

        callButton = addRenderableWidget(Button.builder(
                Component.literal("Call Gathering").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                b -> performAction(GatheringActionPacket.CALL, null)
        ).bounds(bx, topPos + PANEL_HEIGHT - 56, DETAIL_W, 18).build());

        endButton = addRenderableWidget(Button.builder(
                Component.literal("End Gathering").withStyle(ChatFormatting.RED),
                b -> performAction(GatheringActionPacket.END, null)
        ).bounds(bx, topPos + PANEL_HEIGHT - 34, DETAIL_W, 18).build());

        // Message box + send button spanning full bottom of panel
        int msgBoxWidth = PANEL_WIDTH - 50;
        messageBox = new EditBox(this.font,
                leftPos + 4, topPos + PANEL_HEIGHT + 4,
                msgBoxWidth, 16,
                Component.literal("Message members..."));
        messageBox.setMaxLength(256);
        messageBox.setHint(Component.literal("Message all members...").withStyle(ChatFormatting.DARK_GRAY));
        addRenderableWidget(messageBox);

        addRenderableWidget(Button.builder(
                Component.literal("Send").withStyle(ChatFormatting.AQUA),
                b -> sendMessage()
        ).bounds(leftPos + 4 + msgBoxWidth + 2, topPos + PANEL_HEIGHT + 3, 42, 18).build());

        updateButtonStates();
    }

    private void performAction(int action, UUID target) {
        String uuidStr = (target != null) ? target.toString() : "";
        PacketHandler.sendToServer(new GatheringActionPacket(action, uuidStr));
        this.onClose();
    }

    private void updateButtonStates() {
        markButton.active = selectedUUID != null;
        markButton.setMessage(selectedIsMember
                ? Component.literal("Remove Member").withStyle(ChatFormatting.RED)
                : Component.literal("Mark as Member").withStyle(ChatFormatting.GREEN));
        callButton.active = !menu.getMembers().isEmpty();
        endButton.active  = menu.isActive();
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + PANEL_WIDTH, topPos + PANEL_HEIGHT, 0xDD000000);
        g.renderOutline(leftPos, topPos, PANEL_WIDTH, PANEL_HEIGHT, 0xFF4a2070);

        // Title — varies by sefirah
        String titleText = "chaos_sea".equals(menu.getSefirahType())
                ? "Chaos Sea — Blessed"
                : "Sefirah Castle — Gatherings";
        Component title = Component.literal(titleText)
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
        g.drawString(font, title, leftPos + PANEL_WIDTH / 2 - font.width(title) / 2, topPos + 7, 0xFFCC88FF, true);

        // Dividers
        g.fill(leftPos + 4, topPos + 18, leftPos + PANEL_WIDTH - 4, topPos + 19, 0xFF4a2070);
        g.fill(leftPos + DETAIL_X - 6, topPos + 20, leftPos + DETAIL_X - 5, topPos + PANEL_HEIGHT - 8, 0xFF333355);

        // Section header
        g.drawString(font, Component.literal("Prayers Received").withStyle(ChatFormatting.GRAY),
                leftPos + LIST_X, topPos + 21, 0xFFAAAAAA, false);

        renderPrayerList(g, mouseX, mouseY);
        renderDetailPanel(g);
    }

    private void renderPrayerList(GuiGraphics g, int mouseX, int mouseY) {
        List<GatheringMenu.GatheringEntry> prayers = menu.getPrayers();
        int visibleRows = (PANEL_HEIGHT - LIST_Y - 10) / ROW_H;

        for (int i = 0; i < Math.min(prayers.size(), visibleRows); i++) {
            GatheringMenu.GatheringEntry entry = prayers.get(i);
            int rx = leftPos + LIST_X;
            int ry = topPos + LIST_Y + i * ROW_H;

            boolean isMember = menu.getMembers().contains(entry.uuid());
            boolean isSelected = entry.uuid().equals(selectedUUID);

            if (isSelected)   g.fill(rx - 2, ry - 1, rx + 140, ry + HEAD_SIZE + 1, 0x554a2070);
            if (isMember)     g.fill(rx - 2, ry - 1, rx + 3,   ry + HEAD_SIZE + 1, 0xFF44FF88);

            if (i < headStacks.size()) {
                g.renderItem(headStacks.get(i), rx, ry);
            }

            int nameColor = isMember ? 0xFF88FF88 : 0xFFCCCCCC;
            g.drawString(font, entry.name(), rx + HEAD_SIZE + 4, ry + 4, nameColor, false);

            // Hover highlight
            if (mouseX >= rx && mouseX < rx + 140 && mouseY >= ry && mouseY < ry + HEAD_SIZE) {
                g.fill(rx - 2, ry - 1, rx + 140, ry + HEAD_SIZE + 1, 0x22FFFFFF);
            }
        }
    }

    private void renderDetailPanel(GuiGraphics g) {
        int px = leftPos + DETAIL_X;
        int py = topPos + 25;

        if (selectedUUID == null) {
            g.drawString(font, Component.literal("Select a player").withStyle(ChatFormatting.GRAY),
                    px, py, 0xFF888888, false);
            return;
        }

        g.drawString(font, Component.literal(selectedName).withStyle(ChatFormatting.WHITE), px, py, 0xFFFFFFFF, false);
        g.drawString(font, Component.literal(selectedIsMember ? "★ Member" : "Not a member")
                .withStyle(selectedIsMember ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY), px, py + 12, 0xFFCCCCCC, false);

        // Active gathering status
        int activeCount = (int) menu.getMembers().stream()
                .filter(uuid -> GatheringData.isGathered(uuid)).count();
        if (menu.isActive()) {
            g.drawString(font, Component.literal("Gathering Active (" + activeCount + " present)")
                    .withStyle(ChatFormatting.GOLD), px, py + 46, 0xFFFFAA00, false);
        } else {
            g.drawString(font, Component.literal("No active gathering")
                    .withStyle(ChatFormatting.DARK_GRAY), px, py + 46, 0xFF666666, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<GatheringMenu.GatheringEntry> prayers = menu.getPrayers();
        int visibleRows = (PANEL_HEIGHT - LIST_Y - 10) / ROW_H;

        for (int i = 0; i < Math.min(prayers.size(), visibleRows); i++) {
            GatheringMenu.GatheringEntry entry = prayers.get(i);
            int rx = leftPos + LIST_X;
            int ry = topPos + LIST_Y + i * ROW_H;

            if (mouseX >= rx && mouseX < rx + 140 && mouseY >= ry && mouseY < ry + HEAD_SIZE) {
                selectedUUID     = entry.uuid();
                selectedName     = entry.name();
                selectedIsMember = menu.getMembers().contains(entry.uuid());
                updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // suppress default title/inventory rendering
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter sends the message
        if ((keyCode == 257 || keyCode == 335) && messageBox != null && messageBox.isFocused()) {
            sendMessage();
            return true;
        }
        // While the EditBox is focused, pass control keys to it and block everything
        // else (including E/inventory) from reaching super. Only Escape is allowed through.
        if (messageBox != null && messageBox.isFocused() && keyCode != 256) {
            messageBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void sendMessage() {
        if (messageBox == null) return;
        String text = messageBox.getValue().trim();
        if (text.isEmpty()) return;
        PacketHandler.sendToServer(new GatheringMessagePacket(text));
        messageBox.setValue("");
    }
}

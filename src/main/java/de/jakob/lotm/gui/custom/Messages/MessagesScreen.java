package de.jakob.lotm.gui.custom.Messages;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.OpenMessagePacket;
import de.jakob.lotm.util.beyonderMap.MessageType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MessagesScreen extends AbstractContainerScreen<MessagesMenu> {
    private int scrollOffset = 0;

    private static ResourceLocation BG = ResourceLocation.
            fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/introspect.png");

    public MessagesScreen(MessagesMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();

        // Example: open message button
        this.addRenderableWidget(
                Button.builder(Component.literal("Open"), btn -> openSelected())
                        .bounds(leftPos + 10, topPos + 160, 50, 20)
                        .build()
        );
    }

    private void openSelected() {
        int index = menu.getSelectedMessageIndex();
        if (index >= 0) {
            PacketHandler.sendToServer(
                    new OpenMessagePacket(index)
            );
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        gfx.blit(
                BG,
                x, y,
                0, 0,
                imageWidth,
                imageHeight
        );

        int selected = menu.getSelectedMessageIndex();
        if (selected >= 0) {
            int inboxX = x + 8;
            int inboxY = y + 20 + (selected * 12);

            gfx.fill(
                    inboxX,
                    inboxY,
                    inboxX + 80,
                    inboxY + 12,
                    0x80FFFFFF   // translucent white
            );
        }

        // 3️⃣ Unread message markers
        var messages = menu.getMessages();
        for (int i = 0; i < messages.size(); i++) {
            if (!messages.get(i).read()) {
                int dotX = x + 90;
                int dotY = y + 22 + i * 12;

                gfx.fill(
                        dotX,
                        dotY,
                        dotX + 4,
                        dotY + 4,
                        0xFFFFAA00   // orange dot
                );
            }
        }

        // 4️⃣ Divider line (inbox → message)
        gfx.fill(
                x + 96,
                y + 12,
                x + 97,
                y + imageHeight - 12,
                0xFF404040
        );
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        drawInbox(gfx);
        drawMessage(gfx);
    }

    private void drawInbox(GuiGraphics gfx) {
        var messages = menu.getMessages();

        for (int i = 0; i < 6; i++) {
            int idx = i + scrollOffset;
            if (idx >= messages.size()) break;

            var msg = messages.get(idx);
            gfx.drawString(
                    font,
                    msg.from() == null ? "Unknown" : msg.from(),
                    10,
                    20 + i * 12,
                    0xFFFFFF
            );
        }
    }

    private void drawMessage(GuiGraphics gfx) {
        int index = menu.getSelectedMessageIndex();
        if (index < 0) return;

        MessageType msg = menu.getMessages().get(index);
        gfx.drawString(font, msg.title(), 100, 20, 0xFFFFFF);
        gfx.drawWordWrap(font, Component.literal(msg.desc()), 100, 40, 150, 0xCCCCCC);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = leftPos + 10;
        int listY = topPos + 20;

        for (int i = 0; i < 6; i++) {
            int y = listY + i * 12;
            if (mouseX >= listX && mouseX <= listX + 80 &&
                    mouseY >= y && mouseY <= y + 12) {

                menu.selectMessage(i + scrollOffset);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}


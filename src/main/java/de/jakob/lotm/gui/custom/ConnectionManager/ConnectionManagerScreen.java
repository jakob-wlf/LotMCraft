package de.jakob.lotm.gui.custom.ConnectionManager;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenConnectionManagerPacket;
import de.jakob.lotm.network.packets.toServer.ClearConnectionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ConnectionManagerScreen extends Screen {
    private static final int panelWidth = 360;
    private static final int panelHeight = 188;
    private static final int rowHeight = 42;
    private static final int panelColor = 0xF014111B;
    private static final int borderColor = 0xFFB9A65B;
    private static final int rowColor = 0xFF211E2A;
    private static final int hoverColor = 0xFF332E40;

    private final List<OpenConnectionManagerPacket.ConnectionInfo> connections;

    public ConnectionManagerScreen(List<OpenConnectionManagerPacket.ConnectionInfo> connections) {
        super(Component.literal("Manage Connections"));
        this.connections = List.copyOf(connections);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xB0000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        graphics.fill(left - 1, top - 1, left + panelWidth + 1, top + panelHeight + 1, borderColor);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, panelColor);
        graphics.drawCenteredString(font, title.copy().withStyle(ChatFormatting.GOLD),
            left + panelWidth / 2, top + 10, 0xFFFFFFFF);
        graphics.drawCenteredString(font, "Right-click a connection to clear it",
            left + panelWidth / 2, top + 23, 0xFFAAA6B0);

        int listTop = top + 40;
        for (int slot = 0; slot < 3; slot++) {
            int rowTop = listTop + slot * rowHeight;
            boolean hovered = mouseX >= left + 10 && mouseX < left + panelWidth - 10
                && mouseY >= rowTop && mouseY < rowTop + rowHeight - 4;
            graphics.fill(left + 10, rowTop, left + panelWidth - 10, rowTop + rowHeight - 4,
                hovered ? hoverColor : rowColor);

            if (slot >= connections.size()) {
                graphics.drawString(font, "Empty connection slot", left + 24, rowTop + 14,
                    0xFF77727D, false);
                continue;
            }

            OpenConnectionManagerPacket.ConnectionInfo info = connections.get(slot);
            ItemStack icon = createIcon(info.itemId());
            if (!icon.isEmpty()) graphics.renderItem(icon, left + 18, rowTop + 10);
            graphics.drawString(font, trim(info.itemName(), 190), left + 42, rowTop + 7,
                0xFFF0E8CF, false);
            graphics.drawString(font, "Holder: " + trim(info.holderName(), 170), left + 42, rowTop + 21,
                0xFFBDB7C5, false);
            String identity = info.sequence() < 0 ? "Pathway: None"
                : "Pathway: " + info.pathway() + " | Sequence " + info.sequence();
            graphics.drawString(font, trim(identity, 140), left + 210, rowTop + 21,
                0xFF9EC7D5, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            int left = (width - panelWidth) / 2;
            int top = (height - panelHeight) / 2 + 40;
            if (mouseX >= left + 10 && mouseX < left + panelWidth - 10 && mouseY >= top) {
                int slot = (int) ((mouseY - top) / rowHeight);
                if (slot >= 0 && slot < connections.size()
                        && mouseY < top + slot * rowHeight + rowHeight - 4) {
                    PacketHandler.sendToServer(new ClearConnectionPacket(connections.get(slot).connectionId()));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private ItemStack createIcon(String itemId) {
        try {
            return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId)));
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private String trim(String text, int maximumWidth) {
        return font.width(text) <= maximumWidth ? text : font.plainSubstrByWidth(text, maximumWidth - 8) + "...";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

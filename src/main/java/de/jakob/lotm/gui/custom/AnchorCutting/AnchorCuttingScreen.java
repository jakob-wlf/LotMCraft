package de.jakob.lotm.gui.custom.AnchorCutting;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenAnchorCuttingScreenPacket;
import de.jakob.lotm.network.packets.toServer.CutAnchorPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AnchorCuttingScreen extends Screen {
    private static final int panelWidth = 300;
    private static final int panelHeight = 214;
    private static final int rowHeight = 28;
    private static final int visibleRows = 5;

    private final List<OpenAnchorCuttingScreenPacket.AnchorInfo> anchors;
    private int scrollOffset;

    public AnchorCuttingScreen(List<OpenAnchorCuttingScreenPacket.AnchorInfo> anchors) {
        super(Component.literal("Cut Connections"));
        this.anchors = List.copyOf(anchors);
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
        graphics.fill(left - 1, top - 1, left + panelWidth + 1, top + panelHeight + 1, 0xFFB9A65B);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xF014111B);
        graphics.drawCenteredString(font, title.copy().withStyle(ChatFormatting.GOLD),
                left + panelWidth / 2, top + 12, 0xFFFFFFFF);
        graphics.drawCenteredString(font, "Right-click an anchor to sever it",
                left + panelWidth / 2, top + 28, 0xFFAAA6B0);

        if (anchors.isEmpty()) {
            graphics.drawCenteredString(font, "You have no anchors.",
                    left + panelWidth / 2, top + 94, 0xFF77727D);
        }

        int listTop = top + 50;
        int end = Math.min(anchors.size(), scrollOffset + visibleRows);
        for (int index = scrollOffset; index < end; index++) {
            int rowTop = listTop + (index - scrollOffset) * rowHeight;
            boolean hovered = isInside(mouseX, mouseY, left + 12, rowTop, panelWidth - 24, rowHeight - 4);
            graphics.fill(left + 12, rowTop, left + panelWidth - 12, rowTop + rowHeight - 4,
                    hovered ? 0xFF332E40 : 0xFF211E2A);
            OpenAnchorCuttingScreenPacket.AnchorInfo anchor = anchors.get(index);
            graphics.drawString(font, anchor.name(), left + 22, rowTop + 8, 0xFFF0E8CF, false);
            String strength = Math.round(anchor.strength() * 100) + "% strength";
            graphics.drawString(font, strength, left + panelWidth - 22 - font.width(strength),
                    rowTop + 8, 0xFF9EC7D5, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            int left = (width - panelWidth) / 2;
            int listTop = (height - panelHeight) / 2 + 50;
            if (isInside(mouseX, mouseY, left + 12, listTop, panelWidth - 24, visibleRows * rowHeight)) {
                int visibleIndex = (int) ((mouseY - listTop) / rowHeight);
                int index = scrollOffset + visibleIndex;
                if (index < anchors.size() && mouseY < listTop + visibleIndex * rowHeight + rowHeight - 4) {
                    PacketHandler.sendToServer(new CutAnchorPacket(anchors.get(index).anchorId()));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = Math.max(0, Math.min(Math.max(0, anchors.size() - visibleRows),
                scrollOffset - (int) Math.signum(scrollY)));
        return true;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int areaWidth, int areaHeight) {
        return mouseX >= x && mouseX < x + areaWidth && mouseY >= y && mouseY < y + areaHeight;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
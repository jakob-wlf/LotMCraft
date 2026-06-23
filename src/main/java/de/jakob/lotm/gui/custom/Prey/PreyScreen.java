package de.jakob.lotm.gui.custom.Prey;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.PerformPreyPacket;
import de.jakob.lotm.util.playerMap.HonorificName;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PreyScreen extends AbstractContainerScreen<PreyMenu> {

    private static final int CARD_HEIGHT = 40;
    private static final int CARD_WIDTH = 180;
    
    private static final int COLOR_BG = 0xDD000810;
    private static final int COLOR_OUTLINE = 0xFF886622;
    private static final int COLOR_TITLE = 0xFFFFAD33;
    
    private double scrollAmount = 0;
    private final List<Map.Entry<UUID, HonorificName>> entries;

    public PreyScreen(PreyMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 220;
        this.entries = new ArrayList<>(menu.getHonorificNames().entrySet());
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // Simple colored background
        gfx.fill(x, y, x + imageWidth, y + imageHeight, COLOR_BG);
        gfx.renderOutline(x, y, imageWidth, imageHeight, COLOR_OUTLINE);

        renderList(gfx, x + (imageWidth - CARD_WIDTH) / 2, y + 25, mouseX, mouseY);
    }

    private void renderList(GuiGraphics gfx, int x, int y, int mouseX, int mouseY) {
        int visibleHeight = 180;
        gfx.enableScissor(x, y, x + CARD_WIDTH + 10, y + visibleHeight);
        
        int currentY = y - (int) scrollAmount;
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<UUID, HonorificName> entry = entries.get(i);
            renderEntry(gfx, entry, x, currentY, mouseX, mouseY);
            currentY += CARD_HEIGHT + 5;
        }
        
        gfx.disableScissor();
    }

    private void renderEntry(GuiGraphics gfx, Map.Entry<UUID, HonorificName> entry, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + CARD_WIDTH && mouseY >= y && mouseY <= y + CARD_HEIGHT;
        
        gfx.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, hovered ? 0x44FFFFFF : 0x22FFFFFF);
        gfx.renderOutline(x, y, CARD_WIDTH, CARD_HEIGHT, hovered ? 0xFFFFFFFF : 0xFFAAAAAA);

        HonorificName name = entry.getValue();
        int lineY = y + 5;
        for (int i = 0; i < Math.min(name.lines().size(), 3); i++) {
            gfx.drawString(font, name.lines().get(i), x + 5, lineY, 0xFFE0E0E0);
            lineY += 10;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - imageWidth) / 2 + (imageWidth - CARD_WIDTH) / 2;
        int y = (height - imageHeight) / 2 + 25;
        
        int currentY = y - (int) scrollAmount;
        for (int i = 0; i < entries.size(); i++) {
            if (mouseX >= x && mouseX <= x + CARD_WIDTH && mouseY >= currentY && mouseY <= currentY + CARD_HEIGHT) {
                // Perform prey
                PacketHandler.sendToServer(new PerformPreyPacket(entries.get(i).getKey()));
                this.onClose();
                return true;
            }
            currentY += CARD_HEIGHT + 5;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scrollAmount = Math.max(0, this.scrollAmount - scrollY * 10);
        return true;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int i1) {
        guiGraphics.drawString(this.font, this.title, (this.imageWidth - this.font.width(this.title)) / 2, this.titleLabelY, COLOR_TITLE, false);
    }
}

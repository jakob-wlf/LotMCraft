package de.jakob.lotm.gui.custom.honorific_names;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.HonorificNamesRespondPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.HonorificName;
import de.jakob.lotm.util.playerMap.PendingPrayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class HonorificNamesScreen extends AbstractContainerScreen<HonorificNamesMenu> {

    
    private static final int COL_BG_OUTER   = 0xFF0D0D1A; 
    private static final int COL_BG_INNER   = 0xFF141422; 
    private static final int COL_BORDER_LO  = 0xFF2A1A4A; 
    private static final int COL_BORDER_HI  = 0xFF6A3A9A; 
    private static final int COL_TITLE      = 0xFFFFFFFF; 
    private static final int COL_LABEL      = 0xFF9090D0; 
    private static final int COL_TEXT       = 0xFFCCCCCC; 
    private static final int COL_DIVIDER    = 0xFF3A2A6A; 
    private static final int COL_PRAYER_BG  = 0xFF1C1C30; 
    private static final int COL_PRAYER_BRD = 0xFF4A3A7A; 

    
    private static final int W = 280;
    private static final int H = 240;
    private static final int PADDING = 10;
    private static final int TITLE_H = 24;
    private static final int OWN_NAME_H = 100;
    private static final int DIVIDER_H = 6;
    private static final int PRAYER_ENTRY_H = 44;

    
    private int prayerScrollOffset = 0;

    public HonorificNamesScreen(HonorificNamesMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    
    private void rebuildButtons() {
        this.clearWidgets();

        
        if (menu.getSequence() < 4) {
            this.addRenderableWidget(
                    Button.builder(Component.literal("Set Name"),
                                    btn -> Minecraft.getInstance().setScreen(
                                            new SetHonorificNameScreen(menu.getPathway(), menu.getSequence())))
                            .bounds(leftPos + W - PADDING - 80, topPos + TITLE_H + OWN_NAME_H - 20, 80, 16)
                            .build()
            );
        }

        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int listTop = listTop();
        int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
        int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);

        for (int i = 0; i < visibleCount; i++) {
            int idx = i + prayerScrollOffset;
            if (idx >= prayers.size()) break;

            PendingPrayer prayer = prayers.get(idx);
            int entryY = listTop + i * PRAYER_ENTRY_H;

            
            int btnIdx = idx;
            this.addRenderableWidget(
                    Button.builder(Component.literal("Send Message"),
                                    btn -> respondToPrayer(btnIdx, false))
                            .bounds(leftPos + PADDING, entryY + 22, 120, 16)
                            .build()
            );

            
            this.addRenderableWidget(
                    Button.builder(Component.literal("Teleport"),
                                    btn -> respondToPrayer(btnIdx, true))
                            .bounds(leftPos + PADDING + 126, entryY + 22, 120, 16)
                            .build()
            );
        }

        
        if (prayers.size() > visibleCount) {
            int scrollBtnX = leftPos + W - PADDING - 16;
            int scrollBtnY = listTop;

            this.addRenderableWidget(
                    Button.builder(Component.literal("▲"),
                                    btn -> {
                                        if (prayerScrollOffset > 0) {
                                            prayerScrollOffset--;
                                            rebuildButtons();
                                        }
                                    })
                            .bounds(scrollBtnX, scrollBtnY, 16, 16)
                            .build()
            );

            this.addRenderableWidget(
                    Button.builder(Component.literal("▼"),
                                    btn -> {
                                        int maxScroll = Math.max(0, prayers.size() - visibleCount);
                                        if (prayerScrollOffset < maxScroll) {
                                            prayerScrollOffset++;
                                            rebuildButtons();
                                        }
                                    })
                            .bounds(scrollBtnX, scrollBtnY + listHeight - 16, 16, 16)
                            .build()
            );
        }
    }

    private void respondToPrayer(int index, boolean teleport) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        if (index >= 0 && index < prayers.size()) {
            PendingPrayer prayer = prayers.get(index);
            PacketHandler.sendToServer(new HonorificNamesRespondPacket(prayer.senderUUID(), teleport));
            prayers.remove(index);
            int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
            int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);
            prayerScrollOffset = Math.min(prayerScrollOffset, Math.max(0, prayers.size() - visibleCount));
            rebuildButtons();
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        gfx.fill(x - 2, y - 2, x + W + 2, y + H + 2, COL_BORDER_LO);

        gfx.fillGradient(x, y, x + W, y + H, COL_BG_OUTER, COL_BG_INNER);

        gfx.fill(x + 2, y + 2,     x + W - 2, y + 3,     COL_BORDER_HI);
        gfx.fill(x + 2, y + H - 3, x + W - 2, y + H - 2, COL_BORDER_HI);
        gfx.fill(x + 2, y + 2,     x + 3,     y + H - 2, COL_BORDER_HI);
        gfx.fill(x + W - 3, y + 2, x + W - 2, y + H - 2, COL_BORDER_HI);

        gfx.fillGradient(x + 3, y + 3, x + W - 3, y + TITLE_H,
                0xFF1E0A3C, 0xFF0D0D1A);

        gfx.fill(x + PADDING, y + TITLE_H, x + W - PADDING, y + TITLE_H + 1, COL_BORDER_HI);

        
        gfx.fill(x + PADDING - 2, y + TITLE_H + 4,
                 x + W - PADDING + 2, y + TITLE_H + OWN_NAME_H + 2,
                 0xFF1A1A2E);
        
        gfx.fill(x + PADDING - 2, y + TITLE_H + 4,
                 x + W - PADDING + 2, y + TITLE_H + 5,
                 COL_DIVIDER);

        
        int dividerY = y + TITLE_H + OWN_NAME_H + DIVIDER_H / 2;
        gfx.fillGradient(x + PADDING, dividerY, x + W - PADDING, dividerY + 1,
                COL_BORDER_LO, COL_BORDER_HI);
        gfx.fillGradient(x + PADDING, dividerY + 1, x + W - PADDING, dividerY + 2,
                COL_BORDER_HI, COL_BORDER_LO);

        
        renderPrayerCards(gfx, x, y);
    }

    private void renderPrayerCards(GuiGraphics gfx, int x, int y) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int listTop = listTop();
        int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
        int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);

        for (int i = 0; i < visibleCount; i++) {
            int idx = i + prayerScrollOffset;
            if (idx >= prayers.size()) break;

            int cardY = listTop + i * PRAYER_ENTRY_H;

            
            gfx.fill(x + PADDING - 2, cardY - 2,
                     x + W - PADDING + 2, cardY + PRAYER_ENTRY_H - 4,
                     COL_PRAYER_BRD);
            gfx.fill(x + PADDING - 1, cardY - 1,
                     x + W - PADDING + 1, cardY + PRAYER_ENTRY_H - 5,
                     COL_PRAYER_BG);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        

        
        String titleStr = "Honorific Name";
        int titleX = (W - font.width(titleStr)) / 2;
        gfx.drawString(font, titleStr, titleX, 7, COL_TITLE, false);

        
        renderOwnNameSection(gfx);

        
        renderPendingPrayersSection(gfx);
    }

    private void renderOwnNameSection(GuiGraphics gfx) {
        int sectionY = TITLE_H + 6;
        gfx.drawString(font, "Your Honorific Name:", PADDING, sectionY, COL_LABEL, false);

        HonorificName name = menu.getOwnName();
        if (name.isEmpty()) {
            String hint = "Not set — click \"Set Name\" to set it";
            gfx.drawString(font, hint, PADDING + 2, sectionY + 12, 0xFF666688, false);
        } else {
            int lineY = sectionY + 13;
            List<String> lines = name.lines();
            for (int i = 0; i < lines.size(); i++) {
                String lineLabel = "  Line " + (i + 1) + ":  ";
                gfx.drawString(font, lineLabel, PADDING + 2, lineY, COL_LABEL, false);
                gfx.drawString(font, lines.get(i), PADDING + 2 + font.width(lineLabel), lineY, COL_TEXT, false);
                lineY += 11;
            }
        }

        String badge = BeyonderData.pathwayInfos.containsKey(menu.getPathway()) ? BeyonderData.pathwayInfos.get(menu.getPathway()).getSequenceName(menu.getSequence()) : "";
        int badgeX = W - PADDING - font.width(badge) - 2;
        gfx.drawString(font, badge, badgeX, TITLE_H + 6, 0xFF7777AA, false);
    }

    private void renderPendingPrayersSection(GuiGraphics gfx) {
        int sectionLabelY = TITLE_H + OWN_NAME_H + DIVIDER_H;
        gfx.drawString(font, "Pending Prayers:", PADDING, sectionLabelY, COL_LABEL, false);

        List<PendingPrayer> prayers = menu.getPendingPrayers();

        if (prayers.isEmpty()) {
            gfx.drawString(font, "No pending prayers.", PADDING + 4, sectionLabelY + 12, 0xFF555577, false);
            return;
        }

        int listTop = listTop() - topPos; 
        int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
        int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);

        for (int i = 0; i < visibleCount; i++) {
            int idx = i + prayerScrollOffset;
            if (idx >= prayers.size()) break;

            PendingPrayer prayer = prayers.get(idx);
            int entryRelY = listTop + i * PRAYER_ENTRY_H;

            
            String nameLine = prayer.senderName()
                    + "  [" + prayer.senderPathway() + " / Seq " + prayer.senderSequence() + "]";
            gfx.drawString(font, nameLine, PADDING, entryRelY + 2, 0xFFFFCC55, false);

            
            String coordLine = String.format("@ %.0f, %.0f, %.0f",
                    prayer.x(), prayer.y(), prayer.z());
            gfx.drawString(font, coordLine, PADDING, entryRelY + 12, 0xFF8888AA, false);
            
        }

        
        if (prayers.size() > visibleCount) {
            String indicator = (prayerScrollOffset + 1) + " / " + prayers.size();
            gfx.drawString(font, indicator,
                    W - PADDING - 16 - font.width(indicator) - 2,
                    sectionLabelY, 0xFF666688, false);
        }
    }

    

    
    private int listTop() {
        return topPos + TITLE_H + OWN_NAME_H + DIVIDER_H + 11;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<PendingPrayer> prayers = menu.getPendingPrayers();
        int listHeight = H - TITLE_H - OWN_NAME_H - DIVIDER_H - PADDING;
        int visibleCount = Math.max(1, listHeight / PRAYER_ENTRY_H);
        int maxScroll = Math.max(0, prayers.size() - visibleCount);

        if (scrollY < 0 && prayerScrollOffset < maxScroll) {
            prayerScrollOffset++;
            rebuildButtons();
            return true;
        } else if (scrollY > 0 && prayerScrollOffset > 0) {
            prayerScrollOffset--;
            rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}

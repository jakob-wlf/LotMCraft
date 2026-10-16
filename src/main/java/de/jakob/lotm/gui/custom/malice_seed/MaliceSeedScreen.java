package de.jakob.lotm.gui.custom.malice_seed;

import de.jakob.lotm.beyonders.abilities.abyss.MaliceSeed.MaliceSeedAbility.SeedInfo;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestMaliceSeedSyncPacket;
import de.jakob.lotm.network.packets.toServer.SetMaliceChannelPacket;
import de.jakob.lotm.network.packets.toServer.TakeOverMaliceHostPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MaliceSeedScreen extends AbstractContainerScreen<MaliceSeedMenu> {
    private static final int PANEL_MARGIN = 24;
    private static final int ROW_HEIGHT = 48;
    private static final int ROW_GAP = 5;
    private static final int SYNC_INTERVAL_TICKS = 20;
    private int syncCounter;
    private int scrollOffset;
    private int listTop;
    private int listBottom;
    private int listLeft;
    private int listRight;

    private final List<Button> rowButtons = new ArrayList<>();
    public MaliceSeedScreen(MaliceSeedMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 360;
        this.imageHeight = 250;
    }
    @Override
    protected void init() {
        super.init();
        listLeft = leftPos + 12;
        listRight = leftPos + imageWidth - 12;
        listTop = topPos + 38;
        listBottom = topPos + imageHeight - 35;
        addRenderableWidget(Button.builder(Component.translatable("gui.lotm.malice_seed.close"), b -> onClose()).bounds(leftPos + imageWidth / 2 - 45, topPos + imageHeight - 25, 90, 20).build());
        rebuildRowButtons();
    }
    private void clearRowButtons() {
        for (Button button : rowButtons) {removeWidget(button);}
        rowButtons.clear();
    }
    private int maxScroll() {
        int contentHeight = menu.getSeeds().size() * (ROW_HEIGHT + ROW_GAP);
        return Math.max(0, contentHeight - (listBottom - listTop));
    }
    private void rebuildRowButtons() {
        clearRowButtons();
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll());
        List<SeedInfo> seeds = menu.getSeeds();
        for (int i = 0; i < seeds.size(); i++) {
            SeedInfo seed = seeds.get(i);
            int rowY = listTop + i * (ROW_HEIGHT + ROW_GAP) - scrollOffset;
            if (rowY + ROW_HEIGHT <= listTop || rowY >= listBottom) {continue;}
            int buttonWidth = 66;
            int buttonGap = 3;
            int buttonY = rowY + 25;
            int startX = listRight - (buttonWidth * 3 + buttonGap * 2);
            // 1. Whisper: Phase 2+, player hosts only.
            Button whisper = Button.builder(Component.translatable("gui.lotm.malice_seed.whisper"), b -> sendChannel(seed.hostUUID(), 1)).bounds(startX, buttonY, buttonWidth, 18).build();
            whisper.active = seed.phase() >= 2 && seed.isPlayer();
            addRenderableWidget(whisper);
            rowButtons.add(whisper);
            // 2. Speak Through
            Button speak = Button.builder(Component.translatable("gui.lotm.malice_seed.speak_as"), b -> sendChannel(seed.hostUUID(), 2)).bounds(startX + buttonWidth + buttonGap, buttonY, buttonWidth, 18).build();
            speak.active = seed.phase() >= 3;
            addRenderableWidget(speak);
            rowButtons.add(speak);
            //3. Take over
            Button takeOver = Button.builder(Component.translatable("gui.lotm.malice_seed.take_over"), b -> PacketHandler.sendToServer(new TakeOverMaliceHostPacket(seed.hostUUID()))).bounds(startX + (buttonWidth + buttonGap) * 2, buttonY, buttonWidth, 18).build();
            takeOver.active = seed.phase() >= 4;
            addRenderableWidget(takeOver);
            rowButtons.add(takeOver);
        }
    }
    private void sendChannel(java.util.UUID hostUUID, int mode) {PacketHandler.sendToServer(new SetMaliceChannelPacket(hostUUID, mode));}

    @Override
    protected void containerTick() {
        super.containerTick();
        if (++syncCounter >= SYNC_INTERVAL_TICKS) {
            syncCounter = 0;
            PacketHandler.sendToServer(new RequestMaliceSeedSyncPacket());
            rebuildRowButtons();
        }
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= listLeft && mouseX <= listRight && mouseY >= listTop && mouseY <= listBottom) {
            int previousOffset = scrollOffset;
            scrollOffset -= (int) Math.signum(scrollY) * (ROW_HEIGHT + ROW_GAP) * 2;
            scrollOffset = Math.clamp(scrollOffset, 0, maxScroll());
            if (previousOffset != scrollOffset) {rebuildRowButtons();}
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF014141C);
        guiGraphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF655078);
        guiGraphics.drawCenteredString(font, title, leftPos + imageWidth / 2, topPos + 10, 0xFFE4C8FF);
        guiGraphics.drawString(font, "Seeded targets: " + menu.getSeeds().size(), listLeft, topPos + 26, 0xFFAAAAAA, false);
        guiGraphics.enableScissor(listLeft, listTop, listRight, listBottom);
        List<SeedInfo> seeds = menu.getSeeds();
        if (seeds.isEmpty()) {guiGraphics.drawCenteredString(font, Component.translatable("gui.lotm.malice_seed.none"), leftPos + imageWidth / 2, listTop + 30, 0xFFAAAAAA);}
        else {
            for (int i = 0; i < seeds.size(); i++) {
                SeedInfo seed = seeds.get(i);
                int rowY = listTop + i * (ROW_HEIGHT + ROW_GAP) - scrollOffset;
                if (rowY + ROW_HEIGHT <= listTop || rowY >= listBottom) {continue;}
                guiGraphics.fill(listLeft, rowY, listRight, rowY + ROW_HEIGHT, i % 2 == 0 ? 0xFF252530 : 0xFF1D1D26);
                guiGraphics.renderOutline(listLeft, rowY, listRight - listLeft, ROW_HEIGHT, 0xFF403747);
                String name = seed.hostName();
                int maxNameWidth = listRight - listLeft - 12;
                while (font.width(name) > maxNameWidth
                        && !name.isEmpty()) {
                    name = name.substring(0, name.length() - 1);
                }
                if (!name.equals(seed.hostName()) && name.length() > 3) {name = name.substring(0, name.length() - 3) + "...";}
                guiGraphics.drawString(font, name, listLeft + 6, rowY + 5, 0xFFFFFFFF, false);
                guiGraphics.drawString(font, "Phase " + seed.phase(), listLeft + 6, rowY + 27, 0xFFD6B8FF, false);
                // Progress toward the next phase.
                int barX = listLeft + 65;
                int barY = rowY + 29;
                int barWidth = 75;
                int progress = Math.clamp(seed.progressPercent(), 0, 100);
                guiGraphics.fill(barX, barY, barX + barWidth, barY + 5, 0xFF393943);
                guiGraphics.fill(barX, barY, barX + barWidth * progress / 100, barY + 5, 0xFFAD70DF);
                guiGraphics.drawString(font, progress + "%", barX + barWidth + 5, rowY + 27, 0xFFBBBBBB, false);
            }
        }
        guiGraphics.disableScissor();
        // Scrollbar
        int contentHeight = seeds.size() * (ROW_HEIGHT + ROW_GAP);
        int visibleHeight = listBottom - listTop;
        if (contentHeight > visibleHeight) {
            int trackX = listRight - 3;
            int thumbHeight = Math.max(12, visibleHeight * visibleHeight / contentHeight);
            int thumbY = listTop + (visibleHeight - thumbHeight) * scrollOffset / Math.max(1, maxScroll());
            guiGraphics.fill(trackX, listTop, trackX + 3, listBottom, 0xFF303039);
            guiGraphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, 0xFFAD70DF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Everything is positioned explicitly.
    }
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Custom background is drawn in render().
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
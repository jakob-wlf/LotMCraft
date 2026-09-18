package de.jakob.lotm.gui.custom.flaming_jump;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.TeleportPlayerToLocationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class FlamingJumpScreen extends AbstractContainerScreen<FlamingJumpMenu> {

    private static final int PANEL_MARGIN = 40;
    private static final int ROW_HEIGHT = 40;

    private FireList list;

    public FlamingJumpScreen(FlamingJumpMenu container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void init() {
        int listTop = PANEL_MARGIN + 24;
        int listBottom = this.height - 40;

        this.list = new FireList(this, this.minecraft, this.width - PANEL_MARGIN * 2,
                listBottom - listTop, listTop, ROW_HEIGHT);
        this.list.setX(PANEL_MARGIN);

        for (BlockPos pos : menu.getFireLocations()) {
            this.list.addFireLocation(pos);
        }
        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.lotm.flaming_jump.close"), b -> onClose())
                .bounds(this.width / 2 - 60, this.height - 30, 120, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // panel backdrop
        guiGraphics.fillGradient(PANEL_MARGIN - 6, PANEL_MARGIN - 6, this.width - PANEL_MARGIN + 6, this.height - PANEL_MARGIN + 6,
                0xCC1B1B22, 0xCC101014);
        guiGraphics.renderOutline(PANEL_MARGIN - 6, PANEL_MARGIN - 6,
                this.width - PANEL_MARGIN * 2 + 12, this.height - PANEL_MARGIN * 2 + 12, 0xFF4A4A55);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, PANEL_MARGIN + 4, 0xFFFFFF);

        if (menu.getFireLocations().isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.lotm.flaming_jump.none"),
                    this.width / 2, this.height / 2, 0xAAAAAA);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    public static class FireList extends ContainerObjectSelectionList<FireList.Entry> {

        private final FlamingJumpScreen screen;

        public FireList(FlamingJumpScreen screen, Minecraft mc, int width, int height, int y0, int itemHeight) {
            super(mc, width, height, y0, itemHeight);
            this.screen = screen;
            this.setRenderHeader(false, 0);
        }

        public void addFireLocation(BlockPos pos) {
            this.addEntry(new Entry(screen, pos));
        }

        @Override
        public int getRowWidth() {
            return this.width - 12;
        }

        public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

            private final FlamingJumpScreen screen;
            private final BlockPos pos;
            private final Button teleportButton;

            Entry(FlamingJumpScreen screen, BlockPos pos) {
                this.screen = screen;
                this.pos = pos;

                this.teleportButton = Button.builder(
                                Component.translatable("gui.lotm.flaming_jump.teleport"),
                                b -> teleportToFire())
                        .bounds(0, 0, 70, 18)
                        .build();
            }

            private void teleportToFire() {
                if (Minecraft.getInstance().player == null) return;
                PacketHandler.sendToServer(new TeleportPlayerToLocationPacket(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, Minecraft.getInstance().player.getId()));

                screen.onClose();
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                Font font = Minecraft.getInstance().font;

                // row background
                int bg = hovering ? 0x30FFFFFF : (index % 2 == 0 ? 0x1AFFFFFF : 0x10FFFFFF);
                guiGraphics.fill(left, top, left + width, top + height - 2, bg);

                String coordsText = String.format("X: %d  Y: %d  Z: %d", pos.getX(), pos.getY(), pos.getZ());
                guiGraphics.drawString(font, coordsText, left + 8, top + (height - 8) / 2 - 1, 0xFFFFFF, false);

                int buttonX = left + width - 76;
                int buttonY = top + (height - 18) / 2 - 1;

                teleportButton.setX(buttonX);
                teleportButton.setY(buttonY);
                teleportButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(teleportButton);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(teleportButton);
            }
        }
    }
}
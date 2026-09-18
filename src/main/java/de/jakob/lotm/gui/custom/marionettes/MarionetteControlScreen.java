package de.jakob.lotm.gui.custom.marionettes;

import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.gui.custom.introspect.IntrospectMenu;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toServer.ReleaseMarionettePacket;
import de.jakob.lotm.network.packets.toServer.RequestMarionetteSyncPacket;
import de.jakob.lotm.network.packets.toServer.SyncMarionetteToServerPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class MarionetteControlScreen extends AbstractContainerScreen<MarionetteMenu> {

    private static final int PANEL_MARGIN = 40;
    private static final int ROW_HEIGHT = 40;

    private MarionetteList list;

    public MarionetteControlScreen(MarionetteMenu container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void init() {
        int listTop = PANEL_MARGIN + 24;
        int listBottom = this.height - 40;

        this.list = new MarionetteList(this, this.minecraft, this.width - PANEL_MARGIN * 2,
                listBottom - listTop, listTop, ROW_HEIGHT);
        this.list.setX(PANEL_MARGIN);

        for (LivingEntity entity : menu.getMarionettes()) {
            this.list.addEntity(entity);
        }
        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.lotm.marionette_control.close"), b -> onClose())
                .bounds(this.width / 2 - 60, this.height - 30, 120, 20)
                .build());

        PacketHandler.sendToServer(new RequestMarionetteSyncPacket(menu.getEntityIds()));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // panel backdrop
        guiGraphics.fillGradient(PANEL_MARGIN - 6, PANEL_MARGIN - 6, this.width - PANEL_MARGIN + 6, this.height - PANEL_MARGIN + 6,
                0xCC1B1B22, 0xCC101014);
        guiGraphics.renderOutline(PANEL_MARGIN - 6, PANEL_MARGIN - 6,
                this.width - PANEL_MARGIN * 2 + 12, this.height - PANEL_MARGIN * 2 + 12, 0xFF4A4A55);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, PANEL_MARGIN + 4, 0xFFFFFF);

        if (menu.getMarionettes().isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.lotm.marionette_control.none"),
                    this.width / 2, this.height / 2, 0xAAAAAA);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void applySync(List<RequestMarionetteSyncPacket.MarionetteEntry> entries) {
        if (this.list == null) return;
        for (RequestMarionetteSyncPacket.MarionetteEntry data : entries) {
            for (MarionetteList.Entry entry : this.list.children()) {
                if (entry.entityId == data.entityId()) {
                    entry.applySync(data);
                    break;
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    public static class MarionetteList extends ContainerObjectSelectionList<MarionetteList.Entry> {

        private final MarionetteControlScreen screen;

        public MarionetteList(MarionetteControlScreen screen, Minecraft mc, int width, int height, int y0, int itemHeight) {
            super(mc, width, height, y0, itemHeight);
            this.screen = screen;
            this.setRenderHeader(false, 0);
        }

        public void addEntity(LivingEntity entity) {
            this.addEntry(new Entry(screen, entity));
        }

        @Override
        public int getRowWidth() {
            return this.width - 12;
        }

        public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

            private final MarionetteControlScreen screen;
            private final LivingEntity entity;
            private final int entityId;

            private final boolean isBeyonder;
            private final String pathway;
            private final int sequence;

            private MarionetteComponent.MarionetteMode mode = MarionetteComponent.MarionetteMode.FOLLOW;
            private boolean shouldAttack = true;
            private boolean suppressUpdates = true;

            private final Button modeButton;
            private Checkbox attackCheckbox;
            private final Button releaseButton;

            Entry(MarionetteControlScreen screen, LivingEntity entity) {
                this.screen = screen;
                this.entity = entity;
                this.entityId = entity.getId();

                this.isBeyonder = BeyonderData.isBeyonder(entity);
                this.pathway = isBeyonder ? BeyonderData.getPathway(entity) : "";
                this.sequence = isBeyonder ? BeyonderData.getSequence(entity) : -1;

                this.modeButton = Button.builder(modeLabel(), b -> cycleMode(1))
                        .bounds(0, 0, 92, 18)
                        .build();

                this.attackCheckbox = buildCheckbox(shouldAttack);
                this.releaseButton = Button.builder(
                                Component.literal("✖").withStyle(ChatFormatting.RED),
                                b -> release())
                        .bounds(0, 0, 18, 18)
                        .build();

                this.suppressUpdates = false;
            }

            private Component modeLabel() {
                return Component.literal(niceName(mode.name()));
            }

            private void cycleMode(int direction) {
                MarionetteComponent.MarionetteMode[] values = MarionetteComponent.MarionetteMode.values();
                int next = Math.floorMod(mode.ordinal() + direction, values.length);
                mode = values[next];
                modeButton.setMessage(modeLabel());
                pushUpdate();
            }

            private void pushUpdate() {
                if (suppressUpdates) return;
                PacketDistributor.sendToServer(new SyncMarionetteToServerPacket(entityId, mode.ordinal(), shouldAttack));
            }

            private void release() {
                PacketDistributor.sendToServer(new ReleaseMarionettePacket(entityId));
            }

            public void applySync(RequestMarionetteSyncPacket.MarionetteEntry data) {
                suppressUpdates = true;
                MarionetteComponent.MarionetteMode[] values = MarionetteComponent.MarionetteMode.values();
                int ordinal = Math.max(0, Math.min(values.length - 1, data.modeOrdinal()));
                this.mode = values[ordinal];
                this.shouldAttack = data.shouldAttack();

                this.modeButton.setMessage(modeLabel());
                if (this.attackCheckbox.selected() != this.shouldAttack) {
                    this.attackCheckbox = buildCheckbox(this.shouldAttack);
                }
                suppressUpdates = false;
            }

            private Checkbox buildCheckbox(boolean selected) {
                return Checkbox.builder(Component.translatable("gui.lotm.marionette.attack"), ClientHandler.getMinecraftInstance().font)
                        .pos(0, 0)
                        .selected(selected)
                        .onValueChange((cb, value) -> {
                            shouldAttack = value;
                            pushUpdate();
                        })
                        .build();
            }

            private static String cropName(String name) {
                if (name.length() <= 12) return name;
                return name.substring(0, 11) + "\u2026";
            }

            private static String niceName(String raw) {
                String[] parts = raw.toLowerCase().split("_");
                StringBuilder sb = new StringBuilder();
                for (String part : parts) {
                    if (part.isEmpty()) continue;
                    if (!sb.isEmpty()) sb.append(' ');
                    sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
                }
                return sb.toString();
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                                int mouseX, int mouseY, boolean hovering, float partialTick) {
                Font font = Minecraft.getInstance().font;

                // row background
                int bg = hovering ? 0x30FFFFFF : (index % 2 == 0 ? 0x1AFFFFFF : 0x10FFFFFF);
                guiGraphics.fill(left, top, left + width, top + height - 2, bg);

                String displayName = cropName(entity.getName().getString());
                guiGraphics.drawString(font, displayName, left + 6, top + 5, 0xFFFFFF, false);

                if (isBeyonder) {
                    String pathwayLine = niceName(pathway) + "  \u00B7  Seq " + sequence;
                    guiGraphics.drawString(font, pathwayLine, left + 6, top + 17, 0x9A9AA5, false);
                }

                int controlsX = left + width - (92 + 6 + 20 + 6 + 62) - 6;
                int controlsY = top + (height - 18) / 2 - 1;

                modeButton.setX(controlsX);
                modeButton.setY(controlsY);
                modeButton.render(guiGraphics, mouseX, mouseY, partialTick);

                attackCheckbox.setX(controlsX + 92 + 8);
                attackCheckbox.setY(controlsY + 1);
                attackCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);

                releaseButton.setX(controlsX + 92 + 8 + 20 + 40);
                releaseButton.setY(controlsY);
                releaseButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(modeButton, attackCheckbox, releaseButton);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(modeButton, attackCheckbox, releaseButton);
            }
        }
    }
}
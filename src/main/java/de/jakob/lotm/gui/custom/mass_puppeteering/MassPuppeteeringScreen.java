package de.jakob.lotm.gui.custom.mass_puppeteering;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toServer.MassPuppeteeringSelectedEntitiesPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

import java.util.*;

public class MassPuppeteeringScreen extends AbstractContainerScreen<MassPuppeteeringMenu> {

    private static final int PANEL_MARGIN = 40;
    private static final int ROW_HEIGHT = 28;

    private PuppeteeringList list;
    private Button confirmButton;

    public MassPuppeteeringScreen(MassPuppeteeringMenu container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void init() {
        int listTop = PANEL_MARGIN + 24;
        int listBottom = this.height - 45;

        this.list = new PuppeteeringList(this.minecraft, this.width - PANEL_MARGIN * 2,
                listBottom - listTop, listTop, ROW_HEIGHT);
        this.list.setX(PANEL_MARGIN);

        // group entities by entity type display name
        Map<String, List<MassPuppeteeringMenu.PuppetTarget>> groupedTargets = new LinkedHashMap<>();
        for (MassPuppeteeringMenu.PuppetTarget target : menu.getTargets()) {
            String typeName = target.entity().getType().getDescription().getString();
            groupedTargets.computeIfAbsent(typeName, k -> new ArrayList<>()).add(target);
        }

        // populate groups into list
        for (Map.Entry<String, List<MassPuppeteeringMenu.PuppetTarget>> entry : groupedTargets.entrySet()) {
            String groupName = entry.getKey();
            List<MassPuppeteeringMenu.PuppetTarget> targets = entry.getValue();

            PuppeteeringList.GroupEntry groupEntry = new PuppeteeringList.GroupEntry(this.list, groupName);
            for (MassPuppeteeringMenu.PuppetTarget target : targets) {
                groupEntry.addChild(new PuppeteeringList.EntityEntry(groupEntry, target.entity(), target.time()));
            }
            this.list.addGroup(groupEntry);
        }

        this.list.refreshList();
        this.addRenderableWidget(this.list);

        // buttons
        int buttonY = this.height - 35;
        this.confirmButton = this.addRenderableWidget(
                Button.builder(Component.translatable("gui.lotm.mass_puppeteering.confirm"), b -> handleConfirm())
                        .bounds(this.width / 2 - 125, buttonY, 120, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.lotm.mass_puppeteering.close"), b -> onClose())
                        .bounds(this.width / 2 + 5, buttonY, 120, 20)
                        .build()
        );
    }

    private void handleConfirm() {
        List<LivingEntity> selectedEntities = this.list.getSelectedEntities();
        Map<Integer, Integer> selectedTargetTimes = this.list.getSelectedTargetTimes();

        PacketHandler.sendToServer(new MassPuppeteeringSelectedEntitiesPacket(selectedTargetTimes));

        if (this.minecraft != null && this.minecraft.player != null) {
            if (selectedEntities.isEmpty()) {
                this.minecraft.player.sendSystemMessage(
                        Component.literal("[Mass Puppeteering] No entities selected!").withStyle(ChatFormatting.RED)
                );
            } else {
                this.minecraft.player.sendSystemMessage(
                        Component.literal("[Mass Puppeteering] Selected Entities (" + selectedEntities.size() + "):")
                                .withStyle(ChatFormatting.GOLD)
                );
                for (LivingEntity target : selectedEntities) {
                    this.minecraft.player.sendSystemMessage(
                            Component.literal(" - " + target.getDisplayName().getString() + " [ID: " + target.getId() + "]")
                                    .withStyle(ChatFormatting.YELLOW)
                    );
                }
            }
        }
        onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // panel backdrop
        guiGraphics.fillGradient(PANEL_MARGIN - 6, PANEL_MARGIN - 6, this.width - PANEL_MARGIN + 6, this.height - PANEL_MARGIN + 6,
                0xCC1B1B22, 0xCC101014);
        guiGraphics.renderOutline(PANEL_MARGIN - 6, PANEL_MARGIN - 6,
                this.width - PANEL_MARGIN * 2 + 12, this.height - PANEL_MARGIN * 2 + 12, 0xFF4A4A55);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, PANEL_MARGIN + 4, 0xFFFFFF);

        if (menu.getTargets().isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.lotm.mass_puppeteering.none"),
                    this.width / 2, this.height / 2, 0xAAAAAA);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // -----------------------------------------------------------------------------------------------

    public static class PuppeteeringList extends ContainerObjectSelectionList<PuppeteeringList.Entry> {

        private final List<GroupEntry> groups = new ArrayList<>();

        public PuppeteeringList(Minecraft mc, int width, int height, int y0, int itemHeight) {
            super(mc, width, height, y0, itemHeight);
            this.setRenderHeader(false, 0);
        }

        public void addGroup(GroupEntry group) {
            this.groups.add(group);
        }

        public void refreshList() {
            this.clearEntries();
            for (GroupEntry group : groups) {
                this.addEntry(group);
                if (group.isExpanded()) {
                    for (EntityEntry child : group.getChildren()) {
                        this.addEntry(child);
                    }
                }
            }
        }

        public List<LivingEntity> getSelectedEntities() {
            List<LivingEntity> selected = new ArrayList<>();
            for (GroupEntry group : groups) {
                for (EntityEntry child : group.getChildren()) {
                    if (child.isSelected()) {
                        selected.add(child.getEntity());
                    }
                }
            }
            return selected;
        }

        public Map<Integer, Integer> getSelectedTargetTimes() {
            Map<Integer, Integer> map = new HashMap<>();
            for (GroupEntry group : groups) {
                for (EntityEntry child : group.getChildren()) {
                    if (child.isSelected()) {
                        map.put(child.getEntity().getId(), child.getManipulationTime());
                    }
                }
            }
            return map;
        }

        @Override
        public int getRowWidth() {
            return this.width - 12;
        }

        public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {}

        public static class GroupEntry extends Entry {
            private final PuppeteeringList list;
            private final String groupName;
            private final List<EntityEntry> children = new ArrayList<>();

            private boolean expanded = true;
            private boolean isSelected = false;

            private final Button expandButton;
            private Checkbox groupCheckbox;

            public GroupEntry(PuppeteeringList list, String groupName) {
                this.list = list;
                this.groupName = groupName;

                this.expandButton = Button.builder(Component.literal("▼"), b -> toggleExpand())
                        .bounds(0, 0, 16, 16)
                        .build();

                this.groupCheckbox = buildCheckbox(false);
            }

            public void addChild(EntityEntry child) {
                this.children.add(child);
            }

            public List<EntityEntry> getChildren() {
                return children;
            }

            public boolean isExpanded() {
                return expanded;
            }

            private void toggleExpand() {
                this.expanded = !this.expanded;
                this.expandButton.setMessage(Component.literal(expanded ? "▼" : "▶"));
                this.list.refreshList();
            }

            public void updateGroupCheckboxState() {
                boolean allSelected = !children.isEmpty() && children.stream().allMatch(EntityEntry::isSelected);
                if (this.isSelected != allSelected) {
                    this.isSelected = allSelected;
                    this.groupCheckbox = buildCheckbox(allSelected);
                }
            }

            private Checkbox buildCheckbox(boolean selected) {
                return Checkbox.builder(Component.literal(""), ClientHandler.getMinecraftInstance().font)
                        .pos(0, 0)
                        .selected(selected)
                        .onValueChange((cb, value) -> {
                            this.isSelected = value;
                            for (EntityEntry child : children) {
                                child.setSelected(value);
                            }
                        })
                        .build();
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                Font font = Minecraft.getInstance().font;

                guiGraphics.fill(left, top, left + width, top + height - 2, 0x40FFFFFF);

                // expand button
                expandButton.setX(left + 4);
                expandButton.setY(top + (height - 16) / 2);
                expandButton.render(guiGraphics, mouseX, mouseY, partialTick);

                // group selection checkbox
                groupCheckbox.setX(left + 24);
                groupCheckbox.setY(top + (height - 20) / 2);
                groupCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);

                // Title & count label
                String title = groupName + " (" + children.size() + ")";
                guiGraphics.drawString(font, title, left + 48, top + (height - 8) / 2, 0xFFFFAA00, false);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(expandButton, groupCheckbox);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(expandButton, groupCheckbox);
            }
        }

        public static class EntityEntry extends Entry {
            private final GroupEntry parentGroup;
            private final LivingEntity entity;
            private final int manipulationTime;

            private boolean isSelected = false;
            private Checkbox entityCheckbox;

            public EntityEntry(GroupEntry parentGroup, LivingEntity entity, int manipulationTime) {
                this.parentGroup = parentGroup;
                this.entity = entity;
                this.manipulationTime = manipulationTime;
                this.entityCheckbox = buildCheckbox(false);
            }

            public LivingEntity getEntity() {
                return entity;
            }

            public int getManipulationTime() {
                return manipulationTime;
            }

            public boolean isSelected() {
                return isSelected;
            }

            public void setSelected(boolean selected) {
                if (this.isSelected != selected) {
                    this.isSelected = selected;
                    this.entityCheckbox = buildCheckbox(selected);
                }
            }

            private Checkbox buildCheckbox(boolean selected) {
                return Checkbox.builder(Component.literal(""), ClientHandler.getMinecraftInstance().font)
                        .pos(0, 0)
                        .selected(selected)
                        .onValueChange((cb, value) -> {
                            this.isSelected = value;
                            this.parentGroup.updateGroupCheckboxState();
                        })
                        .build();
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                Font font = Minecraft.getInstance().font;

                int bg = hovering ? 0x25FFFFFF : (index % 2 == 0 ? 0x15FFFFFF : 0x0DFFFFFF);
                guiGraphics.fill(left + 16, top, left + width, top + height - 2, bg);

                // entity selection checkbox
                entityCheckbox.setX(left + 24);
                entityCheckbox.setY(top + (height - 20) / 2);
                entityCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);

                // entity Name
                String name = entity.getDisplayName().getString();
                guiGraphics.drawString(font, name, left + 48, top + 4, 0xFFFFFF, false);

                // location details
                String posText = String.format("X: %d, Y: %d, Z: %d", entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
                guiGraphics.drawString(font, posText, left + 48, top + 14, 0x888888, false);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(entityCheckbox);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(entityCheckbox);
            }
        }
    }
}
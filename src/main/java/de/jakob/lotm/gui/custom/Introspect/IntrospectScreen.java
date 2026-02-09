// FILE 1: IntrospectScreen.java (updated with proper JEI support indicator)
package de.jakob.lotm.gui.custom.Introspect;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.OpenMessagesMenuPacket;
import de.jakob.lotm.network.packets.toServer.RequestAbilityBarPacket;
import de.jakob.lotm.network.packets.toServer.SyncAbilityBarAbilitiesPacket;
import de.jakob.lotm.network.packets.toServer.SyncAbilityWheelAbilitiesPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IntrospectScreen extends AbstractContainerScreen<IntrospectMenu> {
    private ResourceLocation containerBackground;
    private boolean showAbilities = false;

    private Button toggleButton;
    private Button clearWheelButton;
    private Button messageButton;
    private Button clearBarButton;

    // Tab management
    private enum Tab {
        ABILITY_WHEEL,
        ABILITY_BAR
    }

    private Tab currentTab = Tab.ABILITY_WHEEL;
    private Button wheelTabButton;
    private Button barTabButton;

    // Ability management
    private final List<Ability> availableAbilities = new ArrayList<>();
    private final List<Ability> abilityWheelSlots = new ArrayList<>();
    private final List<Ability> abilityBarSlots = new ArrayList<>();
    private Ability draggedAbility = null;
    private int draggedFromWheelIndex = -1;
    private int draggedFromBarIndex = -1;
    private boolean draggedFromAvailable = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    // UI dimensions for abilities panel
    private static final int ABILITIES_PANEL_WIDTH = 120;
    private static final int ABILITIES_PANEL_HEIGHT = 115;
    private static final int ABILITY_WHEEL_HEIGHT = 80;
    private static final int ABILITY_BAR_HEIGHT = 60;
    private static final int ABILITY_ICON_SIZE = 16;
    private static final int ABILITY_WHEEL_MAX = 14;
    private static final int ABILITY_BAR_MAX = 6;

    // Placeholder keybinds - easy to change later
    private static final String[] KEYBIND_LABELS = {"1", "2", "3", "4", "5", "6"};

    private int abilitiesScrollOffset = 0;
    private int maxAbilitiesScroll = 0;

    public IntrospectScreen(IntrospectMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/introspect.png");

        this.imageHeight = 231;
        this.imageWidth = 192;
    }

    private void initializeAbilities() {
        availableAbilities.addAll(LOTMCraft.abilityHandler.getByPathwayAndSequenceOrderedBySequence(menu.getPathway(), menu.getSequence()));

        // Calculate max scroll
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int rows = (int) Math.ceil((double) availableAbilities.size() / iconsPerRow);
        int visibleRows = (ABILITIES_PANEL_HEIGHT - 20) / (ABILITY_ICON_SIZE + 2);
        maxAbilitiesScroll = Math.max(0, rows - visibleRows);
    }

    public void setAbilityWheelSlots(ArrayList<String> abilityIds) {
        this.abilityWheelSlots.clear();
        for (String id : abilityIds) {
            Ability ability = LOTMCraft.abilityHandler.getById(id);
            if (ability != null) {
                this.abilityWheelSlots.add(ability);
            }
        }
    }

    public void setAbilityBarSlots(ArrayList<String> abilityIds) {
        this.abilityBarSlots.clear();
        for (String id : abilityIds) {
            Ability ability = LOTMCraft.abilityHandler.getById(id);
            if (ability != null) {
                this.abilityBarSlots.add(ability);
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        if(this.minecraft == null) return;

        // Request ability bar data from server
        PacketHandler.sendToServer(new RequestAbilityBarPacket());

        KEYBIND_LABELS[0] = LOTMCraft.useAbilityBarAbility1.getKey().getDisplayName().getString();
        KEYBIND_LABELS[1] = LOTMCraft.useAbilityBarAbility2.getKey().getDisplayName().getString();
        KEYBIND_LABELS[2] = LOTMCraft.useAbilityBarAbility3.getKey().getDisplayName().getString();
        KEYBIND_LABELS[3] = LOTMCraft.useAbilityBarAbility4.getKey().getDisplayName().getString();
        KEYBIND_LABELS[4] = LOTMCraft.useAbilityBarAbility5.getKey().getDisplayName().getString();
        KEYBIND_LABELS[5] = LOTMCraft.useAbilityBarAbility6.getKey().getDisplayName().getString();

        // Update positions when buttons are created
        updateButtonPositions();
    }

    private String abbreviateKeybind(String keybind) {
        // Handle common long keybind names
        if (keybind.startsWith("Button ")) {
            return "B" + keybind.substring(7); // "Button 5" -> "B5"
        }
        if (keybind.equalsIgnoreCase("Not Bound") || keybind.equalsIgnoreCase("Unbound")) {
            return "-";
        }
        if (keybind.equalsIgnoreCase("Middle Mouse Button")) {
            return "MMB";
        }
        if (keybind.equalsIgnoreCase("Left Mouse Button")) {
            return "LMB";
        }
        if (keybind.equalsIgnoreCase("Right Mouse Button")) {
            return "RMB";
        }
        // Truncate if still too long
        if (keybind.length() > 5) {
            return keybind.substring(0, 4) + "…";
        }
        return keybind;
    }

    private void updateButtonPositions() {
        // Clear existing widgets
        this.clearWidgets();

        // Calculate base position (shifted left when abilities are shown)
        int baseLeftPos = this.leftPos;

        // Add toggle button to the left of the main screen with descriptive text
        int buttonX = baseLeftPos - 65;
        int buttonY = this.topPos + 10;

        toggleButton = Button.builder(Component.literal(showAbilities ? "< Hide" : "Abilities >"),
                        button -> {
                            showAbilities = !showAbilities;
                            button.setMessage(Component.literal(showAbilities ? "< Hide" : "Abilities >"));
                            updateButtonPositions(); // Recreate buttons at new positions
                        })
                .bounds(buttonX, buttonY, 60, 20)
                .build();

        this.addRenderableWidget(toggleButton);

//        int messageButtonX = baseLeftPos - 65;
//        int messageButtonY = this.topPos + 35;
//
//        messageButton = Button.builder(Component.literal("Messages"),
//                button -> {
//                    openMessagesMenu();
//                 })
//                .bounds(messageButtonX, messageButtonY, 60, 20)
//                .build();
//
//        this.addRenderableWidget(messageButton);

        if (showAbilities) {
            // Add tab buttons
            int tabButtonY = this.topPos;
            int tabButtonWidth = ABILITIES_PANEL_WIDTH / 2;
            int panelX = baseLeftPos + this.imageWidth + 5;

            wheelTabButton = Button.builder(Component.literal("Wheel"),
                            button -> {
                                currentTab = Tab.ABILITY_WHEEL;
                                updateButtonPositions();
                            })
                    .bounds(panelX, tabButtonY, tabButtonWidth, 15)
                    .build();

            barTabButton = Button.builder(Component.literal("Bar"),
                            button -> {
                                currentTab = Tab.ABILITY_BAR;
                                updateButtonPositions();
                            })
                    .bounds(panelX + tabButtonWidth, tabButtonY, tabButtonWidth, 15)
                    .build();

            this.addRenderableWidget(wheelTabButton);
            this.addRenderableWidget(barTabButton);

            // Highlight active tab
            if (currentTab == Tab.ABILITY_WHEEL) {
                wheelTabButton.active = false;
            } else {
                barTabButton.active = false;
            }

            // Add clear button based on current tab
            int clearButtonX = baseLeftPos + this.imageWidth + 5;
            int clearButtonY;

            if (currentTab == Tab.ABILITY_WHEEL) {
                clearButtonY = this.topPos + 15 + ABILITIES_PANEL_HEIGHT + 5 + ABILITY_WHEEL_HEIGHT + 5;

                clearWheelButton = Button.builder(Component.literal("Clear Wheel").withStyle(ChatFormatting.RED),
                                button -> {
                                    abilityWheelSlots.clear();
                                    PacketHandler.sendToServer(new SyncAbilityWheelAbilitiesPacket(new ArrayList<>()));
                                })
                        .bounds(clearButtonX, clearButtonY, ABILITIES_PANEL_WIDTH, 20)
                        .build();

                this.addRenderableWidget(clearWheelButton);
            } else {
                clearButtonY = this.topPos + 15 + ABILITIES_PANEL_HEIGHT + 5 + ABILITY_BAR_HEIGHT + 5;

                clearBarButton = Button.builder(Component.literal("Clear Bar").withStyle(ChatFormatting.RED),
                                button -> {
                                    abilityBarSlots.clear();
                                    PacketHandler.sendToServer(new SyncAbilityBarAbilitiesPacket(new ArrayList<>()));
                                })
                        .bounds(clearButtonX, clearButtonY, ABILITIES_PANEL_WIDTH, 20)
                        .build();

                this.addRenderableWidget(clearBarButton);
            }
        }
    }

    private void openMessagesMenu() {
        PacketHandler.sendToServer(new OpenMessagesMenuPacket());
    }

    public void updateMenuData(int sequence, String pathway, float digestionProgress, float sanity) {
        this.menu.updateData(sequence, pathway, digestionProgress, sanity);
        initializeAbilities();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render abilities panel if visible
        if (showAbilities) {
            renderAbilitiesPanel(guiGraphics, mouseX, mouseY);
        }

        // Render dragged ability on top
        if (draggedAbility != null) {
            renderAbilityIcon(guiGraphics, draggedAbility, mouseX - dragOffsetX, mouseY - dragOffsetY);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Render ability tooltips if hovering and not dragging
        if (showAbilities && draggedAbility == null) {
            renderAbilityTooltips(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderAbilityTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos + 15;
        int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

        // Check for hover over available abilities
        Ability hoveredAbility = getAbilityAt(mouseX, mouseY, panelX, panelY, availableAbilities, true);

        // If not hovering over available abilities, check current tab's slots
        if (hoveredAbility == null) {
            if (currentTab == Tab.ABILITY_WHEEL) {
                int wheelSlot = getAbilityWheelSlot(mouseX, mouseY, panelX, slotY);
                if (wheelSlot >= 0 && wheelSlot < abilityWheelSlots.size()) {
                    hoveredAbility = abilityWheelSlots.get(wheelSlot);
                }
            } else {
                int barSlot = getAbilityBarSlot(mouseX, mouseY, panelX, slotY);
                if (barSlot >= 0 && barSlot < abilityBarSlots.size()) {
                    hoveredAbility = abilityBarSlots.get(barSlot);
                }
            }
        }

        // Render tooltip if we found a hovered ability
        if (hoveredAbility != null) {
            List<Component> tooltipLines = new ArrayList<>();

            // Add ability name
            int color = BeyonderData.pathwayInfos.get(menu.getPathway()).color();
            tooltipLines.add(hoveredAbility.getName().withStyle(ChatFormatting.BOLD).withColor(color));

            // Add description if available, wrapping long text
            Component description = hoveredAbility.getDescription();
            if (description != null) {
                String descText = description.getString();
                int maxWidth = 100; // Maximum width in pixels for tooltip

                // Split description into multiple lines if needed
                List<String> wrappedLines = wrapText(descText, maxWidth);
                for (String line : wrappedLines) {
                    tooltipLines.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
                }
            }

            guiGraphics.renderTooltip(this.font, tooltipLines, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int lineWidth = this.font.width(testLine);

            if (lineWidth > maxWidth && currentLine.length() > 0) {
                // Current line is full, add it and start a new line
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                // Add word to current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        // Add the last line
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private void renderAbilitiesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos + 15;

        // Render abilities list background
        guiGraphics.fill(panelX, panelY, panelX + ABILITIES_PANEL_WIDTH, panelY + ABILITIES_PANEL_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, panelY, ABILITIES_PANEL_WIDTH, ABILITIES_PANEL_HEIGHT, 0xFFAAAAAA);

        // Render "Abilities" label
        Component abilitiesLabel = Component.literal("Abilities").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, abilitiesLabel, panelX + 5, panelY + 5, 0xFFFFFFFF, true);

        // Render available abilities (scrollable)
        renderAvailableAbilities(guiGraphics, panelX, panelY + 15, mouseX, mouseY);

        // Render current tab's slots
        int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

        if (currentTab == Tab.ABILITY_WHEEL) {
            renderAbilityWheelSection(guiGraphics, panelX, slotY, mouseX, mouseY);
        } else {
            renderAbilityBarSection(guiGraphics, panelX, slotY, mouseX, mouseY);
        }
    }

    private void renderAvailableAbilities(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        int startX = panelX + 5;
        int startY = panelY + abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2);

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        for (int i = 0; i < availableAbilities.size(); i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;

            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            // Only render if within visible area
            if (y >= panelY && y + ABILITY_ICON_SIZE <= panelY + ABILITIES_PANEL_HEIGHT - 15) {
                Ability ability = availableAbilities.get(i);
                // Always render available abilities (they never disappear)
                renderAbilityIcon(guiGraphics, ability, x, y);
            }
        }
    }

    private void renderAbilityWheelSection(GuiGraphics guiGraphics, int panelX, int wheelY, int mouseX, int mouseY) {
        // Render ability wheel background
        guiGraphics.fill(panelX, wheelY, panelX + ABILITIES_PANEL_WIDTH, wheelY + ABILITY_WHEEL_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, wheelY, ABILITIES_PANEL_WIDTH, ABILITY_WHEEL_HEIGHT, 0xFFAAAAAA);

        // Render "Ability Wheel" label
        Component wheelLabel = Component.literal("Ability Wheel").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, wheelLabel, panelX + 5, wheelY + 5, 0xFFFFFFFF, true);

        // Render ability wheel slots
        renderAbilityWheel(guiGraphics, panelX, wheelY + 15, mouseX, mouseY);
    }

    private void renderAbilityBarSection(GuiGraphics guiGraphics, int panelX, int barY, int mouseX, int mouseY) {
        // Render ability bar background
        guiGraphics.fill(panelX, barY, panelX + ABILITIES_PANEL_WIDTH, barY + ABILITY_BAR_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, barY, ABILITIES_PANEL_WIDTH, ABILITY_BAR_HEIGHT, 0xFFAAAAAA);

        // Render "Ability Bar" label
        Component barLabel = Component.literal("Ability Bar").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, barLabel, panelX + 5, barY + 5, 0xFFFFFFFF, true);

        // Render ability bar slots
        renderAbilityBar(guiGraphics, panelX, barY + 15, mouseX, mouseY);
    }

    private void renderAbilityWheel(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        int startX = panelX + 5;
        int startY = panelY;

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        // Render slots
        for (int i = 0; i < ABILITY_WHEEL_MAX; i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;

            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            // Draw slot background
            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFF333333);
            guiGraphics.renderOutline(x, y, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF666666);

            // Render ability if present (skip if this is the one being dragged)
            if (i < abilityWheelSlots.size()) {
                Ability ability = abilityWheelSlots.get(i);
                // Don't render if this is the slot we're dragging from
                if (draggedFromWheelIndex != i) {
                    renderAbilityIcon(guiGraphics, ability, x, y);
                }
            }
        }
    }

    private void renderAbilityBar(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        int startX = panelX + 5;
        int startY = panelY;

        int slotWidth = (ABILITIES_PANEL_WIDTH - 10) / ABILITY_BAR_MAX;
        int iconX = (slotWidth - ABILITY_ICON_SIZE) / 2;

        // Render slots
        for (int i = 0; i < ABILITY_BAR_MAX; i++) {
            int x = startX + i * slotWidth + iconX;
            int y = startY;

            // Draw slot background
            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFF333333);
            guiGraphics.renderOutline(x, y, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF666666);

            // Render ability if present (skip if this is the one being dragged)
            if (i < abilityBarSlots.size()) {
                Ability ability = abilityBarSlots.get(i);
                // Don't render if this is the slot we're dragging from
                if (draggedFromBarIndex != i) {
                    renderAbilityIcon(guiGraphics, ability, x, y);
                }
            }

            // Render keybind label below slot
            String keybind = abbreviateKeybind(KEYBIND_LABELS[i]);
            Component keybindText = Component.literal(keybind).withStyle(ChatFormatting.GRAY);
            int textWidth = this.font.width(keybindText);
            int textX = startX + i * slotWidth + (slotWidth - textWidth) / 2;
            int textY = y + ABILITY_ICON_SIZE + 2;
            guiGraphics.drawString(this.font, keybindText, textX, textY, 0xFFAAAAAA, false);
        }
    }

    private void renderAbilityIcon(GuiGraphics guiGraphics, Ability ability, int x, int y) {
        if (ability.getTextureLocation() != null) {
            // Render texture when available
            guiGraphics.blit(ability.getTextureLocation(), x, y, 0, 0, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);
        } else {
            // Render colored square as placeholder
            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFFFFFFFF);
            guiGraphics.renderOutline(x, y, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF000000);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && showAbilities) { // Left click
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;
            int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

            // Check if clicking on current tab's slots first (higher priority)
            if (currentTab == Tab.ABILITY_WHEEL) {
                int wheelSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, slotY);
                if (wheelSlot >= 0 && wheelSlot < abilityWheelSlots.size()) {
                    draggedAbility = abilityWheelSlots.get(wheelSlot);
                    draggedFromWheelIndex = wheelSlot;
                    draggedFromBarIndex = -1;
                    draggedFromAvailable = false;
                    dragOffsetX = (int) mouseX - getWheelSlotX(wheelSlot, panelX, slotY);
                    dragOffsetY = (int) mouseY - getWheelSlotY(wheelSlot, panelX, slotY);
                    return true;
                }
            } else {
                int barSlot = getAbilityBarSlot((int) mouseX, (int) mouseY, panelX, slotY);
                if (barSlot >= 0 && barSlot < abilityBarSlots.size()) {
                    draggedAbility = abilityBarSlots.get(barSlot);
                    draggedFromBarIndex = barSlot;
                    draggedFromWheelIndex = -1;
                    draggedFromAvailable = false;
                    dragOffsetX = (int) mouseX - getBarSlotX(barSlot, panelX, slotY);
                    dragOffsetY = (int) mouseY - getBarSlotY(barSlot, panelX, slotY);
                    return true;
                }
            }

            // Check if clicking on available abilities
            Ability clicked = getAbilityAt((int) mouseX, (int) mouseY, panelX, panelY, availableAbilities, true);
            if (clicked != null) {
                draggedAbility = clicked;
                draggedFromWheelIndex = -1;
                draggedFromBarIndex = -1;
                draggedFromAvailable = true;
                dragOffsetX = (int) mouseX - getAbilityX(clicked, panelX, panelY, availableAbilities, true);
                dragOffsetY = (int) mouseY - getAbilityY(clicked, panelX, panelY, availableAbilities, true);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggedAbility != null) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;
            int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

            // Handle drops based on current tab
            if (currentTab == Tab.ABILITY_WHEEL) {
                // Check if dropping in ability wheel area
                if (isInAbilityWheelArea((int) mouseX, (int) mouseY, panelX, slotY)) {
                    int targetSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, slotY);

                    if (targetSlot >= 0 && targetSlot < ABILITY_WHEEL_MAX) {
                        if (draggedFromAvailable) {
                            // Dragging from available abilities to wheel
                            if (targetSlot < abilityWheelSlots.size()) {
                                // Replace existing ability at target slot
                                abilityWheelSlots.set(targetSlot, draggedAbility);
                            } else {
                                // Add to end if slot is empty
                                abilityWheelSlots.add(draggedAbility);
                            }
                        } else {
                            // Rearranging within wheel
                            if (draggedFromWheelIndex >= 0) {
                                if (targetSlot < abilityWheelSlots.size() && targetSlot != draggedFromWheelIndex) {
                                    // Swap abilities properly
                                    Ability temp = abilityWheelSlots.get(targetSlot);
                                    abilityWheelSlots.set(targetSlot, draggedAbility);
                                    abilityWheelSlots.set(draggedFromWheelIndex, temp);
                                } else if (targetSlot >= abilityWheelSlots.size()) {
                                    // Move to end
                                    abilityWheelSlots.remove(draggedFromWheelIndex);
                                    abilityWheelSlots.add(draggedAbility);
                                }
                                // If targetSlot == draggedFromWheelIndex, do nothing (dropped on same slot)
                            }
                        }
                    }
                } else {
                    // Dropped outside wheel area
                    if (!draggedFromAvailable && draggedFromWheelIndex >= 0) {
                        // Remove from wheel if dragged from wheel
                        abilityWheelSlots.remove(draggedFromWheelIndex);
                    }
                }

                PacketHandler.sendToServer(new SyncAbilityWheelAbilitiesPacket(abilityWheelSlots.stream().map(Ability::getId).collect(Collectors.toCollection(ArrayList::new))));

            } else {
                // Ability Bar tab
                // Check if dropping in ability bar area
                if (isInAbilityBarArea((int) mouseX, (int) mouseY, panelX, slotY)) {
                    int targetSlot = getAbilityBarSlot((int) mouseX, (int) mouseY, panelX, slotY);

                    if (targetSlot >= 0 && targetSlot < ABILITY_BAR_MAX) {
                        if (draggedFromAvailable) {
                            // Dragging from available abilities to bar
                            if (targetSlot < abilityBarSlots.size()) {
                                // Replace existing ability at target slot
                                abilityBarSlots.set(targetSlot, draggedAbility);
                            } else {
                                // Add to end if slot is empty
                                abilityBarSlots.add(draggedAbility);
                            }
                        } else {
                            // Rearranging within bar
                            if (draggedFromBarIndex >= 0) {
                                if (targetSlot < abilityBarSlots.size() && targetSlot != draggedFromBarIndex) {
                                    // Swap abilities properly
                                    Ability temp = abilityBarSlots.get(targetSlot);
                                    abilityBarSlots.set(targetSlot, draggedAbility);
                                    abilityBarSlots.set(draggedFromBarIndex, temp);
                                } else if (targetSlot >= abilityBarSlots.size()) {
                                    // Move to end
                                    abilityBarSlots.remove(draggedFromBarIndex);
                                    abilityBarSlots.add(draggedAbility);
                                }
                                // If targetSlot == draggedFromBarIndex, do nothing (dropped on same slot)
                            }
                        }
                    }
                } else {
                    // Dropped outside bar area
                    if (!draggedFromAvailable && draggedFromBarIndex >= 0) {
                        // Remove from bar if dragged from bar
                        abilityBarSlots.remove(draggedFromBarIndex);
                    }
                }

                PacketHandler.sendToServer(new SyncAbilityBarAbilitiesPacket(abilityBarSlots.stream().map(Ability::getId).collect(Collectors.toCollection(ArrayList::new))));
            }

            // Reset drag state
            draggedAbility = null;
            draggedFromWheelIndex = -1;
            draggedFromBarIndex = -1;
            draggedFromAvailable = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showAbilities) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;

            // Check if mouse is over abilities panel
            if (mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                    mouseY >= panelY && mouseY <= panelY + ABILITIES_PANEL_HEIGHT - 15) {

                abilitiesScrollOffset = Math.max(0, Math.min(maxAbilitiesScroll,
                        abilitiesScrollOffset - (int) scrollY));
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private Ability getAbilityAt(int mouseX, int mouseY, int panelX, int panelY, List<Ability> abilities, boolean useScroll) {
        mouseY -= 15;
        int startX = panelX + 5;
        int startY = panelY + (useScroll ? abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2) : 0);
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        for (int i = 0; i < abilities.size(); i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;

            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            if (mouseX >= x && mouseX <= x + ABILITY_ICON_SIZE &&
                    mouseY >= y && mouseY <= y + ABILITY_ICON_SIZE) {
                return abilities.get(i);
            }
        }

        return null;
    }

    private int getAbilityX(Ability ability, int panelX, int panelY, List<Ability> abilities, boolean useScroll) {
        int index = abilities.indexOf(ability);
        if (index < 0) return 0;

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int col = index % iconsPerRow;

        return panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
    }

    private int getAbilityY(Ability ability, int panelX, int panelY, List<Ability> abilities, boolean useScroll) {
        int index = abilities.indexOf(ability);
        if (index < 0) return 0;

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int row = index / iconsPerRow;

        return panelY + (useScroll ? abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2) : 0) + row * (ABILITY_ICON_SIZE + 2) + 15;
    }

    private boolean isInAbilityWheelArea(int mouseX, int mouseY, int panelX, int wheelY) {
        return mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                mouseY >= wheelY + 15 && mouseY <= wheelY + ABILITY_WHEEL_HEIGHT;
    }

    private boolean isInAbilityBarArea(int mouseX, int mouseY, int panelX, int barY) {
        return mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                mouseY >= barY + 15 && mouseY <= barY + ABILITY_BAR_HEIGHT;
    }

    private int getAbilityWheelSlot(int mouseX, int mouseY, int panelX, int wheelY) {
        int startX = panelX + 5;
        int startY = wheelY + 15;
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        for (int i = 0; i < ABILITY_WHEEL_MAX; i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;

            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            if (mouseX >= x && mouseX <= x + ABILITY_ICON_SIZE &&
                    mouseY >= y && mouseY <= y + ABILITY_ICON_SIZE) {
                return i;
            }
        }

        return -1;
    }

    private int getAbilityBarSlot(int mouseX, int mouseY, int panelX, int barY) {
        int startX = panelX + 5;
        int startY = barY + 15;
        int slotWidth = (ABILITIES_PANEL_WIDTH - 10) / ABILITY_BAR_MAX;
        int iconX = (slotWidth - ABILITY_ICON_SIZE) / 2;

        for (int i = 0; i < ABILITY_BAR_MAX; i++) {
            int x = startX + i * slotWidth + iconX;
            int y = startY;

            if (mouseX >= x && mouseX <= x + ABILITY_ICON_SIZE &&
                    mouseY >= y && mouseY <= y + ABILITY_ICON_SIZE) {
                return i;
            }
        }

        return -1;
    }

    private int getWheelSlotX(int slot, int panelX, int wheelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int col = slot % iconsPerRow;
        return panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
    }

    private int getWheelSlotY(int slot, int panelX, int wheelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int row = slot / iconsPerRow;
        return wheelY + 15 + row * (ABILITY_ICON_SIZE + 2);
    }

    private int getBarSlotX(int slot, int panelX, int barY) {
        int slotWidth = (ABILITIES_PANEL_WIDTH - 10) / ABILITY_BAR_MAX;
        int iconX = (slotWidth - ABILITY_ICON_SIZE) / 2;
        return panelX + 5 + slot * slotWidth + iconX;
    }

    private int getBarSlotY(int slot, int panelX, int barY) {
        return barY + 15;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Shift menu left when abilities are shown
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(containerBackground, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        renderPathwaySymbol(guiGraphics, x, y);
        renderSequenceNumber(guiGraphics, x, y);
        renderSequenceName(guiGraphics, x, y);
        renderDigestionLabel(guiGraphics, x, y);
        renderDigestionProgress(guiGraphics, x, y);
        renderSanityLabel(guiGraphics, x, y);
        renderSanityProgress(guiGraphics, x, y);
        renderPassiveAbilitiesText(guiGraphics, x, y);
        RenderSystem.disableBlend();
    }

    private void renderPassiveAbilitiesText(GuiGraphics guiGraphics, int x, int y) {
        Component passiveAbilitiesText = Component.translatable("lotm.passive_abilities").withStyle(ChatFormatting.BOLD);

        int color = 0xDDDDDD;

        int textY = 162;
        int textX = 7;

        guiGraphics.drawString(this.font, passiveAbilitiesText, x + textX, y + textY, color, true);
    }

    private void renderSanityLabel(GuiGraphics guiGraphics, int x, int y) {
        Component digestionText = Component.translatable("lotm.sanity").withStyle(ChatFormatting.BOLD);

        int color = 0xDDDDDD;

        int textY = 115;
        int textX = 7;

        guiGraphics.drawString(this.font, digestionText, x + textX, y + textY, color, true);
    }

    private void renderSanityProgress(GuiGraphics guiGraphics, int x, int y) {
        int barStartY = 132;
        int barEndY = 143;

        int barStartX = 3;
        int barEndX = (int) (115 * menu.getSanity()) + barStartX;

        int color = 0xFFe8bb68;
        int color2 = 0xFFF5ad2a;

        guiGraphics.fillGradient(barStartX + x, barStartY + y, barEndX + x, barEndY + y, color, color2);
    }

    private void renderDigestionLabel(GuiGraphics guiGraphics, int x, int y) {
        Component digestionText = Component.translatable("lotm.digestion").withStyle(ChatFormatting.BOLD);

        int color = 0xDDDDDD;

        int textY = 76;
        int textX = 7;

        guiGraphics.drawString(this.font, digestionText, x + textX, y + textY, color, true);
    }

    private void renderDigestionProgress(GuiGraphics guiGraphics, int x, int y) {
        int barStartY = 93;
        int barEndY = 104;

        int barStartX = 3;
        int barEndX = (int) (115 * menu.getDigestionProgress()) + barStartX;

        int color = 0xFFe36c54;
        int color2 = 0xFFa8422d;

        guiGraphics.fillGradient(barStartX + x, barStartY + y, barEndX + x, barEndY + y, color, color2);
    }

    private void renderSequenceNumber(GuiGraphics guiGraphics, int x, int y) {
        int color = 0xDDDDDD;

        Component sequenceText = Component.translatable("lotm.sequence").append(": ").append(Component.literal(menu.getSequence() + "")).withStyle(ChatFormatting.BOLD);

        int textX = 7;
        int textY = 7;

        guiGraphics.drawString(this.font, sequenceText, x + textX, y + textY, color, true);
    }

    private void renderSequenceName(GuiGraphics guiGraphics, int x, int y) {
        int color = BeyonderData.pathwayInfos.get(menu.getPathway()).color();

        Component sequenceNameText = Component.literal(BeyonderData.getSequenceName(menu.getPathway(), menu.getSequence()));

        int textX = 7;
        int textY = 28;

        guiGraphics.drawString(this.font, sequenceNameText, x + textX, y + textY, color, true);
    }

    private void renderPathwaySymbol(GuiGraphics guiGraphics, int x, int y) {
        ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/gui/icons/" + menu.getPathway() + "_icon.png"
        );

        int iconX = 126;
        int iconY = 3;
        int iconWidth = 62;
        int iconHeight = 62;

        int screenX = x + iconX;
        int screenY = y + iconY;

        guiGraphics.blit(iconTexture, screenX, screenY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title.getString(), this.titleLabelX, this.titleLabelY, 0xCCCCCC, true);
    }
}
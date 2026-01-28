package de.jakob.lotm.gui.custom.Introspect;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.OpenMessagesMenuPacket;
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

public class IntrospectScreen extends AbstractContainerScreen<IntrospectMenu> {
    private ResourceLocation containerBackground;
    private boolean showAbilities = false;
    private Button toggleButton;

    // Ability management
    private List<AbilityIcon> availableAbilities = new ArrayList<>();
    private List<AbilityIcon> abilityWheelSlots = new ArrayList<>();
    private AbilityIcon draggedAbility = null;
    private int draggedFromWheelIndex = -1; // Track where we dragged from in the wheel
    private boolean draggedFromAvailable = false; // Track if dragged from available abilities
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    // UI dimensions for abilities panel
    private static final int ABILITIES_PANEL_WIDTH = 120;
    private static final int ABILITIES_PANEL_HEIGHT = 140;
    private static final int ABILITY_WHEEL_HEIGHT = 80;
    private static final int ABILITY_ICON_SIZE = 16;
    private static final int ABILITY_WHEEL_MAX = 18;

    private int abilitiesScrollOffset = 0;
    private int maxAbilitiesScroll = 0;

    public IntrospectScreen(IntrospectMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/introspect.png");

        this.imageHeight = 231;
        this.imageWidth = 192;

        // Initialize some test abilities (colored squares)
        initializeTestAbilities();
    }

    private void initializeTestAbilities() {
        // Add some test abilities with different colors
        availableAbilities.add(new AbilityIcon("ability_1", 0xFFFF0000, null)); // Red
        availableAbilities.add(new AbilityIcon("ability_2", 0xFF00FF00, null)); // Green
        availableAbilities.add(new AbilityIcon("ability_3", 0xFF0000FF, null)); // Blue
        availableAbilities.add(new AbilityIcon("ability_4", 0xFFFFFF00, null)); // Yellow
        availableAbilities.add(new AbilityIcon("ability_5", 0xFFFF00FF, null)); // Magenta
        availableAbilities.add(new AbilityIcon("ability_6", 0xFF00FFFF, null)); // Cyan
        availableAbilities.add(new AbilityIcon("ability_7", 0xFFFF8800, null)); // Orange
        availableAbilities.add(new AbilityIcon("ability_8", 0xFF8800FF, null)); // Purple

        // Calculate max scroll
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int rows = (int) Math.ceil((double) availableAbilities.size() / iconsPerRow);
        int visibleRows = (ABILITIES_PANEL_HEIGHT - 20) / (ABILITY_ICON_SIZE + 2);
        maxAbilitiesScroll = Math.max(0, rows - visibleRows);
    }

    public void updateScreen(String pathway, int sequence) {
        this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/introspect.png");
    }

    @Override
    protected void init() {
        super.init();

        if(this.minecraft == null) return;

        updateScreen(menu.getPathway(), menu.getSequence());

        // Add toggle button to the left of the main screen
        int buttonX = this.leftPos - 25;
        int buttonY = this.topPos + 10;

        toggleButton = Button.builder(Component.literal(showAbilities ? "<" : ">"),
                        button -> {
                            showAbilities = !showAbilities;
                            button.setMessage(Component.literal(showAbilities ? "<" : ">"));
                        })
                .bounds(buttonX, buttonY, 20, 20)
                .build();

        this.addRenderableWidget(toggleButton);
    }

    private void openMessagesMenu() {
        PacketHandler.sendToServer(new OpenMessagesMenuPacket());
    }

    public void updateMenuData(int sequence, String pathway, float digestionProgress, float sanity) {
        this.menu.updateData(sequence, pathway, digestionProgress, sanity);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateScreen(menu.getPathway(), menu.getSequence());

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
    }

    private void renderAbilitiesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int panelX = this.leftPos + this.imageWidth + 5;
        int panelY = this.topPos;

        // Render abilities list background
        guiGraphics.fill(panelX, panelY, panelX + ABILITIES_PANEL_WIDTH, panelY + ABILITIES_PANEL_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, panelY, ABILITIES_PANEL_WIDTH, ABILITIES_PANEL_HEIGHT, 0xFFAAAAAA);

        // Render "Abilities" label
        Component abilitiesLabel = Component.literal("Abilities").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, abilitiesLabel, panelX + 5, panelY + 5, 0xFFFFFFFF, true);

        // Render available abilities (scrollable)
        renderAvailableAbilities(guiGraphics, panelX, panelY + 15, mouseX, mouseY);

        // Render ability wheel background
        int wheelY = panelY + ABILITIES_PANEL_HEIGHT + 5;
        guiGraphics.fill(panelX, wheelY, panelX + ABILITIES_PANEL_WIDTH, wheelY + ABILITY_WHEEL_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, wheelY, ABILITIES_PANEL_WIDTH, ABILITY_WHEEL_HEIGHT, 0xFFAAAAAA);

        // Render "Ability Wheel" label
        Component wheelLabel = Component.literal("Ability Wheel").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, wheelLabel, panelX + 5, wheelY + 5, 0xFFFFFFFF, true);

        // Render ability wheel slots
        renderAbilityWheel(guiGraphics, panelX, wheelY + 15, mouseX, mouseY);
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
                AbilityIcon ability = availableAbilities.get(i);
                // Always render available abilities (they never disappear)
                renderAbilityIcon(guiGraphics, ability, x, y);
            }
        }
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
                AbilityIcon ability = abilityWheelSlots.get(i);
                // Don't render if this is the slot we're dragging from
                if (draggedFromWheelIndex != i) {
                    renderAbilityIcon(guiGraphics, ability, x, y);
                }
            }
        }
    }

    private void renderAbilityIcon(GuiGraphics guiGraphics, AbilityIcon ability, int x, int y) {
        if (ability.texture != null) {
            // Render texture when available
            guiGraphics.blit(ability.texture, x, y, 0, 0, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);
        } else {
            // Render colored square as placeholder
            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, ability.color);
            guiGraphics.renderOutline(x, y, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF000000);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && showAbilities) { // Left click
            int panelX = this.leftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;
            int wheelY = panelY + ABILITIES_PANEL_HEIGHT - 10;

            // Check if clicking on ability wheel first (higher priority)
            int wheelSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, wheelY);
            if (wheelSlot >= 0 && wheelSlot < abilityWheelSlots.size()) {
                draggedAbility = abilityWheelSlots.get(wheelSlot);
                draggedFromWheelIndex = wheelSlot;
                draggedFromAvailable = false;
                dragOffsetX = (int) mouseX - getWheelSlotX(wheelSlot, panelX, wheelY);
                dragOffsetY = (int) mouseY - getWheelSlotY(wheelSlot, panelX, wheelY);
                return true;
            }

            // Check if clicking on available abilities
            AbilityIcon clicked = getAbilityAt((int) mouseX, (int) mouseY, panelX, panelY, availableAbilities, true);
            if (clicked != null) {
                draggedAbility = clicked;
                draggedFromWheelIndex = -1;
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
            int panelX = this.leftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;
            int wheelY = panelY + ABILITIES_PANEL_HEIGHT - 10;

            // Check if dropping in ability wheel area
            if (isInAbilityWheelArea((int) mouseX, (int) mouseY, panelX, wheelY)) {
                int targetSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, wheelY);

                if (targetSlot >= 0 && targetSlot < ABILITY_WHEEL_MAX) {
                    if (draggedFromAvailable) {
                        // Dragging from available abilities to wheel
                        if (targetSlot < abilityWheelSlots.size()) {
                            // Replace existing ability
                            abilityWheelSlots.set(targetSlot, draggedAbility);
                        } else {
                            // Add to end if slot is empty
                            abilityWheelSlots.add(draggedAbility);
                        }
                    } else {
                        // Rearranging within wheel
                        if (draggedFromWheelIndex >= 0) {
                            if (targetSlot < abilityWheelSlots.size()) {
                                // Swap abilities
                                AbilityIcon temp = abilityWheelSlots.get(targetSlot);
                                abilityWheelSlots.set(targetSlot, draggedAbility);
                                abilityWheelSlots.set(draggedFromWheelIndex, temp);
                            } else {
                                // Move to end
                                abilityWheelSlots.remove(draggedFromWheelIndex);
                                abilityWheelSlots.add(draggedAbility);
                            }
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

            // Reset drag state
            draggedAbility = null;
            draggedFromWheelIndex = -1;
            draggedFromAvailable = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showAbilities) {
            int panelX = this.leftPos + this.imageWidth + 5;
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

    private AbilityIcon getAbilityAt(int mouseX, int mouseY, int panelX, int panelY, List<AbilityIcon> abilities, boolean useScroll) {
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

    private int getAbilityX(AbilityIcon ability, int panelX, int panelY, List<AbilityIcon> abilities, boolean useScroll) {
        int index = abilities.indexOf(ability);
        if (index < 0) return 0;

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int col = index % iconsPerRow;

        return panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
    }

    private int getAbilityY(AbilityIcon ability, int panelX, int panelY, List<AbilityIcon> abilities, boolean useScroll) {
        int index = abilities.indexOf(ability);
        if (index < 0) return 0;

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int row = index / iconsPerRow;

        return panelY + (useScroll ? abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2) : 0) + row * (ABILITY_ICON_SIZE + 2);
    }

    private boolean isInAbilityWheelArea(int mouseX, int mouseY, int panelX, int wheelY) {
        return mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                mouseY >= wheelY && mouseY <= wheelY + ABILITY_WHEEL_HEIGHT;
    }

    private int getAbilityWheelSlot(int mouseX, int mouseY, int panelX, int wheelY) {
        int startX = panelX + 5;
        int startY = wheelY;
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

    private int getWheelSlotX(int slot, int panelX, int wheelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int col = slot % iconsPerRow;
        return panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
    }

    private int getWheelSlotY(int slot, int panelX, int wheelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int row = slot / iconsPerRow;
        return wheelY + row * (ABILITY_ICON_SIZE + 2);
    }

    // Inner class to represent an ability icon
    private static class AbilityIcon {
        String id;
        int color;
        ResourceLocation texture;

        AbilityIcon(String id, int color, ResourceLocation texture) {
            this.id = id;
            this.color = color;
            this.texture = texture;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

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
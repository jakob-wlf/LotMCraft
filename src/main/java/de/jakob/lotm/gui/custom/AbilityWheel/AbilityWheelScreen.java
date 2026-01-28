package de.jakob.lotm.gui.custom.AbilityWheel;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.UpdateSelectedAbilityPacket;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class AbilityWheelScreen extends AbstractContainerScreen<AbilityWheelMenu> {

    private static final ResourceLocation WHEEL_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_wheel_background.png");

    private static final int WHEEL_SIZE = 240;
    private static final int CENTER_X = WHEEL_SIZE / 2;
    private static final int CENTER_Y = WHEEL_SIZE / 2;

    private static final int SLOT_SIZE = 32;
    private static final int SLOT_HOVER_SIZE = 34;

    private int hoveredSlot = -1;

    public AbilityWheelScreen(AbilityWheelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WHEEL_SIZE;
        this.imageHeight = WHEEL_SIZE;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        List<String> abilities = ClientData.getAbilityWheelAbilities();
        if (abilities.isEmpty()) {
            return;
        }

        // Update hovered slot
        hoveredSlot = getSlotAtPosition(mouseX, mouseY, abilities.size());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        List<String> abilities = ClientData.getAbilityWheelAbilities();
        if (abilities.isEmpty()) {
            return;
        }

        int centerX = this.leftPos + CENTER_X;
        int centerY = this.topPos + CENTER_Y;

        // Enable blending for transparency with proper alpha handling
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Render the static background image (contains circles, runes, particles, center core)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WHEEL_BACKGROUND);
        guiGraphics.blit(WHEEL_BACKGROUND, this.leftPos, this.topPos, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);

        // Only render ability slots dynamically
        renderAbilitySlots(guiGraphics, centerX, centerY, abilities);

        // Disable blending after rendering
        RenderSystem.disableBlend();
    }

    private void renderCenterLines(GuiGraphics guiGraphics, int centerX, int centerY, List<String> abilities) {
        int abilityCount = Math.min(abilities.size(), 14);
        int lineColor = 0x998B6914; // Semi-transparent golden color

        for (int i = 0; i < abilityCount; i++) {
            SlotPosition pos = getSlotPosition(i, abilityCount, centerX, centerY);
            drawLine(guiGraphics, centerX, centerY, pos.x, pos.y, lineColor, 2);
        }
    }

    private void renderAbilitySlots(GuiGraphics guiGraphics, int centerX, int centerY, List<String> abilities) {
        int abilityCount = Math.min(abilities.size(), 14);

        for (int i = 0; i < abilityCount; i++) {
            SlotPosition pos = getSlotPosition(i, abilityCount, centerX, centerY);
            boolean isHovered = hoveredSlot == i;
            int selectedIndex = ClientData.getSelectedAbility();
            boolean isSelected = i == selectedIndex;

            renderAbilitySlot(guiGraphics, pos, abilities.get(i), isHovered, isSelected);
        }
    }

    private void renderAbilitySlot(GuiGraphics guiGraphics, SlotPosition pos, String abilityId, boolean isHovered, boolean isSelected) {
        int size = isHovered ? SLOT_HOVER_SIZE : SLOT_SIZE;
        int x = pos.x - size / 2;
        int y = pos.y - size / 2;

        // Background
        guiGraphics.fill(x, y, x + size, y + size, 0xCC000000);

        // Border with glow effect
        int borderColor = isSelected ? 0xFFc4a8e3 : (isHovered ? 0xFFc4a8e3 : 0xFF9989ab);
        int borderWidth = isSelected ? 2 : 1;

        // Glow effect for hovered/selected (render behind)
        if (isHovered || isSelected) {
            int glowSize = 1;
            int glowColor = isSelected ? 0x60c4a8e3 : 0x409989ab;
            guiGraphics.fill(x - glowSize, y - glowSize, x + size + glowSize, y + size + glowSize, glowColor);
        }

        // Draw border
        guiGraphics.fill(x, y, x + size, y + borderWidth, borderColor); // Top
        guiGraphics.fill(x, y + size - borderWidth, x + size, y + size, borderColor); // Bottom
        guiGraphics.fill(x, y, x + borderWidth, y + size, borderColor); // Left
        guiGraphics.fill(x + size - borderWidth, y, x + size, y + size, borderColor); // Right

        // Render ability icon
        try {
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/" + abilityId + ".png");
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);

            int iconPadding = 3;
            int iconSize = size - iconPadding * 2;
            guiGraphics.blit(texture,
                    x + iconPadding,
                    y + iconPadding,
                    0, 0,
                    iconSize,
                    iconSize,
                    iconSize,
                    iconSize);
        } catch (Exception e) {
            // If texture doesn't exist, render placeholder
            int padding = size / 5;
            guiGraphics.fill(x + padding, y + padding, x + size - padding, y + size - padding, 0xFF8B6914);
        }
    }

    private SlotPosition getSlotPosition(int index, int totalSlots, int centerX, int centerY) {
        // Pentagon formation for 5 or fewer slots
        if (totalSlots <= 5) {
            double angle = Math.toRadians(index * (360.0 / totalSlots) - 90);
            int radius = 85; // Reduced from 180
            return new SlotPosition(
                    centerX + (int)(Math.cos(angle) * radius),
                    centerY + (int)(Math.sin(angle) * radius)
            );
        } else {
            // Circular formation for more slots
            double angle = Math.toRadians(index * (360.0 / totalSlots) - 90);
            int radius = 95; // Reduced from 200
            return new SlotPosition(
                    centerX + (int)(Math.cos(angle) * radius),
                    centerY + (int)(Math.sin(angle) * radius)
            );
        }
    }

    private int getSlotAtPosition(int mouseX, int mouseY, int totalSlots) {
        int centerX = this.leftPos + CENTER_X;
        int centerY = this.topPos + CENTER_Y;

        for (int i = 0; i < totalSlots; i++) {
            SlotPosition pos = getSlotPosition(i, totalSlots, centerX, centerY);
            int halfSize = SLOT_HOVER_SIZE / 2;

            if (mouseX >= pos.x - halfSize && mouseX <= pos.x + halfSize &&
                    mouseY >= pos.y - halfSize && mouseY <= pos.y + halfSize) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredSlot != -1) {
            // Update selected ability
            PacketHandler.sendToServer(new UpdateSelectedAbilityPacket(hoveredSlot));
            ClientData.setAbilityWheelData(
                    new java.util.ArrayList<>(ClientData.getAbilityWheelAbilities()),
                    hoveredSlot
            );

            // Close the screen
            this.onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Don't render inventory labels
    }

    // Helper method to draw a line with thickness
    private void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, int thickness) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int halfThickness = thickness / 2;

        while (true) {
            // Draw a small rectangle for thickness
            guiGraphics.fill(x1 - halfThickness, y1 - halfThickness,
                    x1 + halfThickness, y1 + halfThickness, color);

            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private record SlotPosition(int x, int y) {}
}
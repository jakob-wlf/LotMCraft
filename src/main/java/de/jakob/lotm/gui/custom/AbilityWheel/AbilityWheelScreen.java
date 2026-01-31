package de.jakob.lotm.gui.custom.AbilityWheel;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.events.KeyInputHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.UpdateSelectedAbilityPacket;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.security.Key;
import java.util.List;

public class AbilityWheelScreen extends AbstractContainerScreen<AbilityWheelMenu> {

    private static final ResourceLocation WHEEL_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_wheel_background.png");

    private static final int WHEEL_SIZE = 180;
    private static final int CENTER_X = WHEEL_SIZE / 2;
    private static final int CENTER_Y = WHEEL_SIZE / 2;
    private static final int WHEEL_RADIUS = WHEEL_SIZE / 2;

    private static final int SLOT_SIZE = 30;
    private static final int SLOT_HOVER_SIZE = 32;

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

        // Update hovered slot based on section
        hoveredSlot = getSlotAtPosition(mouseX, mouseY, abilities.size());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
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

        // Render section glow for hovered section
        if (hoveredSlot != -1 && abilities.size() > 1) {
            renderSectionGlow(guiGraphics, centerX, centerY, abilities.size(), hoveredSlot);
        }

        // Render section divider lines
        if(abilities.size() > 1) {
            renderSectionLines(guiGraphics, centerX, centerY, abilities);
        }

        // Only render ability slots dynamically
        renderAbilitySlots(guiGraphics, centerX, centerY, abilities);

        // Disable blending after rendering
        RenderSystem.disableBlend();
    }

    private void renderSectionLines(GuiGraphics guiGraphics, int centerX, int centerY, List<String> abilities) {
        int abilityCount = Math.min(abilities.size(), 14);
        int lineColor = 0x4Dc4a8e3; // Semi-transparent golden color (30% opacity)

        for (int i = 0; i < abilityCount; i++) {
            // Calculate angle for the line (between two abilities)
            double angleOffset = 360.0 / abilityCount / 2.0; // Half section to get middle
            double angle = Math.toRadians(i * (360.0 / abilityCount) - 90 - angleOffset);

            // Calculate end point at the wheel's edge
            int endX = centerX + (int)(Math.cos(angle) * WHEEL_RADIUS);
            int endY = centerY + (int)(Math.sin(angle) * WHEEL_RADIUS);

            // Draw smooth line
            drawSmoothLine(guiGraphics, centerX, centerY, endX, endY, lineColor);
        }
    }

    private void renderSectionGlow(GuiGraphics guiGraphics, int centerX, int centerY, int totalSlots, int hoveredSection) {
        double degreesPerSection = 360.0 / totalSlots;

        // Calculate start and end angles for this section (in radians for efficiency)
        double sectionStartAngle = Math.toRadians(hoveredSection * degreesPerSection - 90 - (degreesPerSection / 2.0));
        double sectionEndAngle = Math.toRadians(hoveredSection * degreesPerSection - 90 + (degreesPerSection / 2.0));

        int maxGlowRadius = (int) Math.round(WHEEL_RADIUS / 1.5);

        // OPTIMIZATION 1: Reduce pixel sampling - only check every 2-3 pixels
        int step = 2; // Increase to 3 for even better performance

        // OPTIMIZATION 2: Pre-calculate these once
        double radiusSq = maxGlowRadius * maxGlowRadius;
        double invMaxRadius = 1.0 / maxGlowRadius;

        // Pre-calculate bounds
        int minX = centerX - maxGlowRadius;
        int maxX = centerX + maxGlowRadius;
        int minY = centerY - maxGlowRadius;
        int maxY = centerY + maxGlowRadius;

        // OPTIMIZATION 3: Use PoseStack for batch rendering instead of individual fill calls
        var pose = guiGraphics.pose();
        pose.pushPose();

        // Draw glow with reduced sampling
        for (int y = minY; y <= maxY; y += step) {
            for (int x = minX; x <= maxX; x += step) {
                int dx = x - centerX;
                int dy = y - centerY;

                // OPTIMIZATION 4: Use squared distance to avoid sqrt
                double distanceSq = dx * dx + dy * dy;

                // Skip if outside glow radius (using squared distance)
                if (distanceSq > radiusSq || distanceSq < 1) {
                    continue;
                }

                // Calculate angle of this pixel
                double pixelAngle = Math.atan2(dy, dx);

                // Normalize angles to same range for comparison
                double normalizedPixelAngle = pixelAngle;
                double normalizedStartAngle = sectionStartAngle;
                double normalizedEndAngle = sectionEndAngle;

                // Handle angle wrapping
                if (normalizedEndAngle < normalizedStartAngle) {
                    normalizedEndAngle += 2 * Math.PI;
                }
                if (normalizedPixelAngle < normalizedStartAngle) {
                    normalizedPixelAngle += 2 * Math.PI;
                }

                // Check if pixel is within the section's angular range
                if (normalizedPixelAngle >= normalizedStartAngle && normalizedPixelAngle <= normalizedEndAngle) {
                    // OPTIMIZATION 5: Calculate alpha using squared distance
                    double distance = Math.sqrt(distanceSq); // Only sqrt when needed
                    double alphaMult = 1.0 - (distance * invMaxRadius);
                    int alpha = (int)(0xEE * alphaMult);

                    if (alpha > 0) {
                        int glowColor = (alpha << 24) | 0xc4a8e3;
                        // Draw slightly larger pixels to compensate for step
                        guiGraphics.fill(x, y, x + step, y + step, glowColor);
                    }
                }
            }
        }

        pose.popPose();
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

        // Calculate relative position from center
        int relativeX = mouseX - centerX;
        int relativeY = mouseY - centerY;

        // Calculate distance from center
        double distance = Math.sqrt(relativeX * relativeX + relativeY * relativeY);

        // Check if mouse is outside the wheel
        if (distance > WHEEL_RADIUS) {
            return -1;
        }

        // Calculate angle in radians, then convert to degrees
        // atan2 gives us the angle from the positive X axis (right = 0°)
        double angleRadians = Math.atan2(relativeY, relativeX);
        double angleDegrees = Math.toDegrees(angleRadians);

        // Normalize to 0-360 range (atan2 returns -180 to 180)
        if (angleDegrees < 0) {
            angleDegrees += 360;
        }

        // Calculate degrees per section
        double degreesPerSection = 360.0 / totalSlots;

        // The first ability is at -90 degrees (straight up, or 270 in our 0-360 system)
        // We need to rotate our coordinate system so that:
        // - The first section (index 0) is centered at 270° (up)
        // - Section boundaries are offset by half a section from the ability positions

        // Offset to align: rotate by 90° (to make up = 0°) then by half section (to center sections)
        double adjustedAngle = angleDegrees - 270 + (degreesPerSection / 2.0);

        // Normalize back to 0-360
        while (adjustedAngle < 0) {
            adjustedAngle += 360;
        }
        adjustedAngle = adjustedAngle % 360;

        // Calculate which section this angle falls into
        int section = (int)(adjustedAngle / degreesPerSection);

        // Ensure section is within valid range
        if (section >= totalSlots) {
            section = 0;
        }

        return section;
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
            KeyInputHandler.holdAbilityWheelCooldownTicks = 12;
            this.onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if(KeyInputHandler.wasWheelOpenedWithHold && hoveredSlot != -1) {
            PacketHandler.sendToServer(new UpdateSelectedAbilityPacket(hoveredSlot));
            ClientData.setAbilityWheelData(
                    new java.util.ArrayList<>(ClientData.getAbilityWheelAbilities()),
                    hoveredSlot
            );
        }
        super.onClose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Don't render inventory labels
    }

    // Smooth anti-aliased line drawing using Xiaolin Wu's algorithm (simplified)
    private void drawSmoothLine(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int baseColor) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        if (dx == 0 && dy == 0) {
            guiGraphics.fill(x0, y0, x0 + 1, y0 + 1, baseColor);
            return;
        }

        // Extract ARGB components
        int alpha = (baseColor >> 24) & 0xFF;
        int red = (baseColor >> 16) & 0xFF;
        int green = (baseColor >> 8) & 0xFF;
        int blue = baseColor & 0xFF;

        if (dx > dy) {
            // Line is more horizontal
            if (x0 > x1) {
                int temp = x0; x0 = x1; x1 = temp;
                temp = y0; y0 = y1; y1 = temp;
            }

            float gradient = (float)(y1 - y0) / (x1 - x0);
            float y = y0;

            for (int x = x0; x <= x1; x++) {
                int yInt = (int)y;
                float yFrac = y - yInt;

                // Main pixel
                int alpha1 = (int)(alpha * (1 - yFrac));
                int color1 = (alpha1 << 24) | (red << 16) | (green << 8) | blue;
                guiGraphics.fill(x, yInt, x + 1, yInt + 1, color1);

                // Anti-aliasing pixel
                int alpha2 = (int)(alpha * yFrac);
                int color2 = (alpha2 << 24) | (red << 16) | (green << 8) | blue;
                guiGraphics.fill(x, yInt + 1, x + 1, yInt + 2, color2);

                y += gradient;
            }
        } else {
            // Line is more vertical
            if (y0 > y1) {
                int temp = x0; x0 = x1; x1 = temp;
                temp = y0; y0 = y1; y1 = temp;
            }

            float gradient = (float)(x1 - x0) / (y1 - y0);
            float x = x0;

            for (int y = y0; y <= y1; y++) {
                int xInt = (int)x;
                float xFrac = x - xInt;

                // Main pixel
                int alpha1 = (int)(alpha * (1 - xFrac));
                int color1 = (alpha1 << 24) | (red << 16) | (green << 8) | blue;
                guiGraphics.fill(xInt, y, xInt + 1, y + 1, color1);

                // Anti-aliasing pixel
                int alpha2 = (int)(alpha * xFrac);
                int color2 = (alpha2 << 24) | (red << 16) | (green << 8) | blue;
                guiGraphics.fill(xInt + 1, y, xInt + 2, y + 1, color2);

                x += gradient;
            }
        }
    }

    private record SlotPosition(int x, int y) {}
}
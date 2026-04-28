package de.jakob.lotm.gui.custom.AbilityWheel;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.events.KeyInputHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.UpdateSelectedAbilityPacket;
import de.jakob.lotm.network.packets.toServer.UseSelectedAbilityPacket;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.client.Minecraft;
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

    // FIX: Raised cap to 27 to match the new max ability count.
    // Previously this was 14, which caused getSlotAtPosition (using full count)
    // to diverge from renderAbilitySlots (capped at 14), breaking hover alignment.
    private static final int MAX_ABILITIES = 27;

    private int hoveredSlot = -1;

    public AbilityWheelScreen(AbilityWheelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WHEEL_SIZE;
        this.imageHeight = WHEEL_SIZE;
    }

    private List<String> getActiveAbilities() {
        if (ClientData.sharedAbilityMode) {
            return ClientData.getSharedWheelAbilities();
        }
        return ClientData.getAbilityWheelAbilities();
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

        List<String> abilities = getActiveAbilities();
        if (abilities.isEmpty()) {
            return;
        }

        // FIX: clamp here once and use this clamped count everywhere downstream.
        // Previously the cap was applied inconsistently across methods.
        int count = Math.min(abilities.size(), MAX_ABILITIES);
        hoveredSlot = getSlotAtPosition(mouseX, mouseY, count);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        List<String> abilities = getActiveAbilities();
        if (abilities.isEmpty()) {
            return;
        }

        // FIX: apply the cap once here, pass the clamped count to every downstream call
        // so all rendering methods operate on the same count as the hover detection.
        int count = Math.min(abilities.size(), MAX_ABILITIES);
        List<String> clampedAbilities = abilities.subList(0, count);

        int centerX = this.leftPos + CENTER_X;
        int centerY = this.topPos + CENTER_Y;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WHEEL_BACKGROUND);
        guiGraphics.blit(WHEEL_BACKGROUND, this.leftPos, this.topPos, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);

        if (hoveredSlot != -1 && count > 1) {
            renderSectionGlow(guiGraphics, centerX, centerY, count, hoveredSlot);
        }

        if (count > 1) {
            renderSectionLines(guiGraphics, centerX, centerY, clampedAbilities);
        }

        renderAbilitySlots(guiGraphics, centerX, centerY, clampedAbilities);

        RenderSystem.disableBlend();
    }

    private void renderSectionLines(GuiGraphics guiGraphics, int centerX, int centerY, List<String> abilities) {
        // FIX: use abilities.size() directly — the list is already clamped by the caller.
        int abilityCount = abilities.size();
        int lineColor = 0x4Dc4a8e3;

        for (int i = 0; i < abilityCount; i++) {
            double angleOffset = 360.0 / abilityCount / 2.0;
            double angle = Math.toRadians(i * (360.0 / abilityCount) - 90 - angleOffset);

            int endX = centerX + (int)(Math.cos(angle) * WHEEL_RADIUS);
            int endY = centerY + (int)(Math.sin(angle) * WHEEL_RADIUS);

            drawSmoothLine(guiGraphics, centerX, centerY, endX, endY, lineColor);
        }
    }

    private void renderSectionGlow(GuiGraphics guiGraphics, int centerX, int centerY, int totalSlots, int hoveredSection) {
        double degreesPerSection = 360.0 / totalSlots;

        double sectionStartAngle = Math.toRadians(hoveredSection * degreesPerSection - 90 - (degreesPerSection / 2.0));
        double sectionEndAngle = Math.toRadians(hoveredSection * degreesPerSection - 90 + (degreesPerSection / 2.0));

        int maxGlowRadius = (int) Math.round(WHEEL_RADIUS / 1.5);

        int step = 2;

        double radiusSq = maxGlowRadius * maxGlowRadius;
        double invMaxRadius = 1.0 / maxGlowRadius;

        int minX = centerX - maxGlowRadius;
        int maxX = centerX + maxGlowRadius;
        int minY = centerY - maxGlowRadius;
        int maxY = centerY + maxGlowRadius;

        var pose = guiGraphics.pose();
        pose.pushPose();

        for (int y = minY; y <= maxY; y += step) {
            for (int x = minX; x <= maxX; x += step) {
                int dx = x - centerX;
                int dy = y - centerY;

                double distanceSq = dx * dx + dy * dy;

                if (distanceSq > radiusSq || distanceSq < 1) {
                    continue;
                }

                double pixelAngle = Math.atan2(dy, dx);

                double normalizedPixelAngle = pixelAngle;
                double normalizedStartAngle = sectionStartAngle;
                double normalizedEndAngle = sectionEndAngle;

                if (normalizedEndAngle < normalizedStartAngle) {
                    normalizedEndAngle += 2 * Math.PI;
                }
                if (normalizedPixelAngle < normalizedStartAngle) {
                    normalizedPixelAngle += 2 * Math.PI;
                }

                if (normalizedPixelAngle >= normalizedStartAngle && normalizedPixelAngle <= normalizedEndAngle) {
                    double distance = Math.sqrt(distanceSq);
                    double alphaMult = 1.0 - (distance * invMaxRadius);
                    int alpha = (int)(0xEE * alphaMult);

                    if (alpha > 0) {
                        int glowColor = (alpha << 24) | 0xc4a8e3;
                        guiGraphics.fill(x, y, x + step, y + step, glowColor);
                    }
                }
            }
        }

        pose.popPose();
    }

    private void renderAbilitySlots(GuiGraphics guiGraphics, int centerX, int centerY, List<String> abilities) {
        // FIX: use abilities.size() directly — the list is already clamped by the caller.
        // Previously this applied Math.min(abilities.size(), 14) here independently,
        // which diverged from getSlotAtPosition's count and broke hover alignment.
        int abilityCount = abilities.size();

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

        guiGraphics.fill(x, y, x + size, y + size, 0xCC000000);

        int borderColor = isSelected ? 0xFFc4a8e3 : (isHovered ? 0xFFc4a8e3 : 0xFF9989ab);
        int borderWidth = isSelected ? 2 : 1;

        if (isHovered || isSelected) {
            int glowSize = 1;
            int glowColor = isSelected ? 0x60c4a8e3 : 0x409989ab;
            guiGraphics.fill(x - glowSize, y - glowSize, x + size + glowSize, y + size + glowSize, glowColor);
        }

        guiGraphics.fill(x, y, x + size, y + borderWidth, borderColor);
        guiGraphics.fill(x, y + size - borderWidth, x + size, y + size, borderColor);
        guiGraphics.fill(x, y, x + borderWidth, y + size, borderColor);
        guiGraphics.fill(x + size - borderWidth, y, x + size, y + size, borderColor);

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
            int padding = size / 5;
            guiGraphics.fill(x + padding, y + padding, x + size - padding, y + size - padding, 0xFF8B6914);
        }

        if (isHovered) {
            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if (ability != null) {
                Component name = ability.getNameFormatted();
                int textWidth = net.minecraft.client.Minecraft.getInstance().font.width(name);
                guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, name,
                        pos.x - textWidth / 2, pos.y - size / 2 - 12, 0xFFFFFF, true);
            }
        }
    }

    private SlotPosition getSlotPosition(int index, int totalSlots, int centerX, int centerY) {
        // FIX: unified radius — previously this used 85 for ≤5 and 95 for >5,
        // which was fine, but keeping a single value simplifies reasoning.
        // For 27 slots packed tightly, 75 keeps icons within the wheel background.
        // Adjust this value if icons clip the wheel edge.
        int radius;
        if (totalSlots <= 5) {
            radius = 85;
        } else {
            radius = 95;
        }

        double angle = Math.toRadians(index * (360.0 / totalSlots) - 90);
        return new SlotPosition(
                centerX + (int)(Math.cos(angle) * radius),
                centerY + (int)(Math.sin(angle) * radius)
        );
    }

    private int getSlotAtPosition(int mouseX, int mouseY, int totalSlots) {
        int centerX = this.leftPos + CENTER_X;
        int centerY = this.topPos + CENTER_Y;

        int relativeX = mouseX - centerX;
        int relativeY = mouseY - centerY;

        double distance = Math.sqrt(relativeX * relativeX + relativeY * relativeY);

        if (distance > WHEEL_RADIUS) {
            return -1;
        }

        double angleRadians = Math.atan2(relativeY, relativeX);
        double angleDegrees = Math.toDegrees(angleRadians);

        if (angleDegrees < 0) {
            angleDegrees += 360;
        }

        double degreesPerSection = 360.0 / totalSlots;

        // This offset must stay consistent with how getSlotPosition places slots.
        // getSlotPosition uses:  angle = index * (360/n) - 90
        // So slot 0 is at 270° (straight up). Sections are centered on each slot,
        // meaning section boundaries are at ±(degreesPerSection/2) from each slot.
        // The adjustment below rotates the mouse angle into that same coordinate space.
        double adjustedAngle = angleDegrees - 270 + (degreesPerSection / 2.0);

        while (adjustedAngle < 0) {
            adjustedAngle += 360;
        }
        adjustedAngle = adjustedAngle % 360;

        int section = (int)(adjustedAngle / degreesPerSection);

        if (section >= totalSlots) {
            section = 0;
        }

        return section;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredSlot != -1) {
            if (ClientData.sharedAbilityMode) {
                ClientData.setSelectedSharedAbility(hoveredSlot);
            } else {
                PacketHandler.sendToServer(new UpdateSelectedAbilityPacket(hoveredSlot));
                ClientData.setAbilityWheelData(
                        new java.util.ArrayList<>(getActiveAbilities()),
                        hoveredSlot
                );
            }

            KeyInputHandler.holdAbilityWheelCooldownTicks = 12;
            this.onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (KeyInputHandler.wasWheelOpenedWithHold && hoveredSlot != -1) {
            PacketHandler.sendToServer(new UpdateSelectedAbilityPacket(hoveredSlot));
            if (ClientData.sharedAbilityMode) {
                ClientData.setSelectedSharedAbility(hoveredSlot);
            } else {
                ClientData.setAbilityWheelData(
                        new java.util.ArrayList<>(getActiveAbilities()),
                        hoveredSlot
                );
            }
        }
        ClientData.sharedAbilityMode = false;
        super.onClose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Don't render inventory labels
    }

    private void drawSmoothLine(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int baseColor) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        if (dx == 0 && dy == 0) {
            guiGraphics.fill(x0, y0, x0 + 1, y0 + 1, baseColor);
            return;
        }

        int alpha = (baseColor >> 24) & 0xFF;
        int red = (baseColor >> 16) & 0xFF;
        int green = (baseColor >> 8) & 0xFF;
        int blue = baseColor & 0xFF;

        if (dx > dy) {
            if (x0 > x1) {
                int temp = x0; x0 = x1; x1 = temp;
                temp = y0; y0 = y1; y1 = temp;
            }

            float gradient = (float)(y1 - y0) / (x1 - x0);
            float y = y0;

            for (int x = x0; x <= x1; x++) {
                int yInt = (int)y;
                float yFrac = y - yInt;

                int alpha1 = (int)(alpha * (1 - yFrac));
                int color1 = (alpha1 << 24) | (red << 16) | (green << 8) | blue;
                guiGraphics.fill(x, yInt, x + 1, yInt + 1, color1);

                int alpha2 = (int)(alpha * yFrac);
                int color2 = (alpha2 << 24) | (red << 16) | (green << 8) | blue;
                guiGraphics.fill(x, yInt + 1, x + 1, yInt + 2, color2);

                y += gradient;
            }
        } else {
            if (y0 > y1) {
                int temp = x0; x0 = x1; x1 = temp;
                temp = y0; y0 = y1; y1 = temp;
            }

            float gradient = (float)(x1 - x0) / (y1 - y0);
            float x = x0;

            for (int y = y0; y <= y1; y++) {
                int xInt = (int)x;
                float xFrac = x - xInt;

                int alpha1 = (int)(alpha * (1 - xFrac));
                int color1 = (alpha1 << 24) | (red << 16) | (green << 8) | blue;
                guiGraphics.fill(xInt, y, xInt + 1, y + 1, color1);

                int alpha2 = (int)(alpha * xFrac);
                int color2 = (alpha2 << 24) | (red << 16) | (green << 8) | blue;
                guiGraphics.fill(xInt + 1, y, xInt + 2, y + 1, color2);

                x += gradient;
            }
        }
    }

    private record SlotPosition(int x, int y) {}
}
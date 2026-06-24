package de.jakob.lotm.gui.custom.AbilityWheel;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

/**
 * Reusable base class for any ability-wheel GUI screen.
 *
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@link #getAbilities()} – the ordered list of ability IDs to display</li>
 *   <li>{@link #getMaxAbilities()} – hard cap on how many slots the wheel shows</li>
 *   <li>{@link #getLineColor()} – ARGB colour for the separator lines</li>
 *   <li>{@link #getGlowColor()} – RGB colour (no alpha) used for the hover-glow effect</li>
 *   <li>{@link #renderSlot} – slot-level rendering (icon, badge, tooltip, …)</li>
 * </ul>
 *
 * <p>Subclasses can also override {@link #mouseClicked} and {@link #onClose} to
 * attach their own selection / packet logic.
 */
public abstract class BaseAbilityWheelScreen<T extends AbstractContainerMenu>
        extends AbstractContainerScreen<T> {

    protected static final ResourceLocation WHEEL_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_wheel_background.png");

    protected static final int WHEEL_SIZE = 180;
    protected static final int CENTER_X = WHEEL_SIZE / 2;
    protected static final int CENTER_Y = WHEEL_SIZE / 2;
    protected static final int WHEEL_RADIUS = WHEEL_SIZE / 2;

    protected static final int SLOT_SIZE = 30;
    protected static final int SLOT_HOVER_SIZE = 32;

    protected int hoveredSlot = -1;

    protected BaseAbilityWheelScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WHEEL_SIZE;
        this.imageHeight = WHEEL_SIZE;
    }

    // -------------------------------------------------------------------------
    // Abstract API – subclasses define the data and per-slot rendering
    // -------------------------------------------------------------------------

    /** Returns the ordered list of ability IDs (or id:subIndex entries) to display. */
    protected abstract List<String> getAbilities();

    /** Maximum number of slots shown in the wheel. */
    protected abstract int getMaxAbilities();

    /**
     * ARGB colour for the divider lines between wheel sections.
     * Examples: {@code 0x4Dc4a8e3} (ability wheel), {@code 0x4Da8c4e3} (copied ability wheel).
     */
    protected abstract int getLineColor();

    /**
     * RGB colour (no alpha – alpha is computed per-pixel) for the hover glow.
     * Examples: {@code 0xc4a8e3} (ability wheel), {@code 0xa8c4e3} (copied ability wheel).
     */
    protected abstract int getGlowColor();

    /**
     * Renders a single slot.  Called once per ability in the wheel.
     *
     * @param guiGraphics the graphics context
     * @param pos         centre position of the slot
     * @param abilityId   the ability ID for this slot (may include {@code :subIndex})
     * @param isHovered   whether the mouse is over this slot
     * @param index       zero-based index of the slot in the current wheel
     */
    protected abstract void renderSlot(GuiGraphics guiGraphics, SlotPosition pos,
                                       String abilityId, boolean isHovered, int index);

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        List<String> abilities = getAbilities();
        if (abilities.isEmpty()) return;

        int count = Math.min(abilities.size(), getMaxAbilities());
        hoveredSlot = getSlotAtPosition(mouseX, mouseY, count);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        List<String> abilities = getAbilities();
        if (abilities.isEmpty()) return;

        int count = Math.min(abilities.size(), getMaxAbilities());

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
            renderSectionLines(guiGraphics, centerX, centerY, count);
        }

        for (int i = 0; i < count; i++) {
            SlotPosition pos = getSlotPosition(i, count, centerX, centerY);
            renderSlot(guiGraphics, pos, abilities.get(i), hoveredSlot == i, i);
        }

        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // intentionally empty – no inventory labels
    }

    // -------------------------------------------------------------------------
    // Wheel geometry helpers
    // -------------------------------------------------------------------------

    protected SlotPosition getSlotPosition(int index, int totalSlots, int centerX, int centerY) {
        int radius = (totalSlots <= 5) ? 85 : 95;
        double angle = Math.toRadians(index * (360.0 / totalSlots) - 90);
        return new SlotPosition(
                centerX + (int) (Math.cos(angle) * radius),
                centerY + (int) (Math.sin(angle) * radius)
        );
    }

    protected int getSlotAtPosition(int mouseX, int mouseY, int totalSlots) {
        int centerX = this.leftPos + CENTER_X;
        int centerY = this.topPos + CENTER_Y;

        int relativeX = mouseX - centerX;
        int relativeY = mouseY - centerY;

        double distance = Math.sqrt(relativeX * relativeX + relativeY * relativeY);
        if (distance > WHEEL_RADIUS) return -1;

        double angleDegrees = Math.toDegrees(Math.atan2(relativeY, relativeX));
        if (angleDegrees < 0) angleDegrees += 360;

        double degreesPerSection = 360.0 / totalSlots;
        double adjustedAngle = ((angleDegrees - 270 + (degreesPerSection / 2.0)) % 360 + 360) % 360;

        int section = (int) (adjustedAngle / degreesPerSection);
        return (section >= totalSlots) ? 0 : section;
    }

    // -------------------------------------------------------------------------
    // Section rendering helpers
    // -------------------------------------------------------------------------

    private void renderSectionLines(GuiGraphics guiGraphics, int centerX, int centerY, int count) {
        int lineColor = getLineColor();
        for (int i = 0; i < count; i++) {
            double angleOffset = 360.0 / count / 2.0;
            double angle = Math.toRadians(i * (360.0 / count) - 90 - angleOffset);
            int endX = centerX + (int) (Math.cos(angle) * WHEEL_RADIUS);
            int endY = centerY + (int) (Math.sin(angle) * WHEEL_RADIUS);
            drawSmoothLine(guiGraphics, centerX, centerY, endX, endY, lineColor);
        }
    }

    private void renderSectionGlow(GuiGraphics guiGraphics, int centerX, int centerY,
                                   int totalSlots, int hoveredSection) {
        double degreesPerSection = 360.0 / totalSlots;
        double sectionStartAngle = Math.toRadians(hoveredSection * degreesPerSection - 90 - (degreesPerSection / 2.0));
        double sectionEndAngle = Math.toRadians(hoveredSection * degreesPerSection - 90 + (degreesPerSection / 2.0));

        int maxGlowRadius = (int) Math.round(WHEEL_RADIUS / 1.5);
        int step = 2;

        double radiusSq = (double) maxGlowRadius * maxGlowRadius;
        double invMaxRadius = 1.0 / maxGlowRadius;

        int minX = centerX - maxGlowRadius;
        int maxX = centerX + maxGlowRadius;
        int minY = centerY - maxGlowRadius;
        int maxY = centerY + maxGlowRadius;

        int rgbGlow = getGlowColor();

        var pose = guiGraphics.pose();
        pose.pushPose();

        for (int y = minY; y <= maxY; y += step) {
            for (int x = minX; x <= maxX; x += step) {
                int dx = x - centerX;
                int dy = y - centerY;

                double distanceSq = (double) dx * dx + (double) dy * dy;
                if (distanceSq > radiusSq || distanceSq < 1) continue;

                double pixelAngle = Math.atan2(dy, dx);
                double normPixel = pixelAngle;
                double normStart = sectionStartAngle;
                double normEnd = sectionEndAngle;

                if (normEnd < normStart) normEnd += 2 * Math.PI;
                if (normPixel < normStart) normPixel += 2 * Math.PI;

                if (normPixel >= normStart && normPixel <= normEnd) {
                    double distance = Math.sqrt(distanceSq);
                    int alpha = (int) (0xEE * (1.0 - distance * invMaxRadius));
                    if (alpha > 0) {
                        guiGraphics.fill(x, y, x + step, y + step, (alpha << 24) | rgbGlow);
                    }
                }
            }
        }

        pose.popPose();
    }

    // -------------------------------------------------------------------------
    // Line drawing
    // -------------------------------------------------------------------------

    protected void drawSmoothLine(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int baseColor) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        if (dx == 0 && dy == 0) {
            guiGraphics.fill(x0, y0, x0 + 1, y0 + 1, baseColor);
            return;
        }

        int alpha = (baseColor >> 24) & 0xFF;
        int red   = (baseColor >> 16) & 0xFF;
        int green = (baseColor >> 8)  & 0xFF;
        int blue  = baseColor         & 0xFF;

        if (dx > dy) {
            if (x0 > x1) {
                int tmp = x0; x0 = x1; x1 = tmp;
                tmp = y0; y0 = y1; y1 = tmp;
            }
            float gradient = (float) (y1 - y0) / (x1 - x0);
            float y = y0;
            for (int x = x0; x <= x1; x++) {
                int yInt = (int) y;
                float yFrac = y - yInt;
                guiGraphics.fill(x, yInt,     x + 1, yInt + 1, ((int)(alpha * (1 - yFrac)) << 24) | (red << 16) | (green << 8) | blue);
                guiGraphics.fill(x, yInt + 1, x + 1, yInt + 2, ((int)(alpha * yFrac)       << 24) | (red << 16) | (green << 8) | blue);
                y += gradient;
            }
        } else {
            if (y0 > y1) {
                int tmp = x0; x0 = x1; x1 = tmp;
                tmp = y0; y0 = y1; y1 = tmp;
            }
            float gradient = (float) (x1 - x0) / (y1 - y0);
            float x = x0;
            for (int y = y0; y <= y1; y++) {
                int xInt = (int) x;
                float xFrac = x - xInt;
                guiGraphics.fill(xInt,     y, xInt + 1, y + 1, ((int)(alpha * (1 - xFrac)) << 24) | (red << 16) | (green << 8) | blue);
                guiGraphics.fill(xInt + 1, y, xInt + 2, y + 1, ((int)(alpha * xFrac)       << 24) | (red << 16) | (green << 8) | blue);
                x += gradient;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Shared types
    // -------------------------------------------------------------------------

    protected record SlotPosition(int x, int y) {}
}

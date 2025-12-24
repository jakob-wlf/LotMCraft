package de.jakob.lotm.gui.custom.AbilityWheel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class AbilityWheelScreen extends Screen {
    private final SelectableAbilityItem abilityItem;
    private final ItemStack itemStack;
    private final Player player;
    private final String[] abilityNames;
    private int hoveredIndex = -1;
    private final int currentSelection;

    private static final int WHEEL_RADIUS = 80;
    private static final int CENTER_RADIUS = 20;
    private static final int SEGMENT_HOVER_RADIUS = 90;
    private static final int SEGMENTS_DETAIL = 32;

    public AbilityWheelScreen(SelectableAbilityItem abilityItem, ItemStack itemStack, Player player) {
        super(Component.literal("Ability Wheel"));
        this.abilityItem = abilityItem;
        this.itemStack = itemStack;
        this.player = player;
        this.abilityNames = abilityItem.getAbilityNamesCopy();
        this.currentSelection = getSelectedAbilityIndex();
    }

    @Override
    public void init() {
        super.init();
        if (this.minecraft != null) {
            this.minecraft.options.hideGui = false;
        }
    }

    private int getSelectedAbilityIndex() {
        String selected = abilityItem.getSelectedAbility(player);
        for (int i = 0; i < abilityNames.length; i++) {
            if (abilityNames[i].equals(selected)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x80000000);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        hoveredIndex = getHoveredSegment(mouseX, mouseY, centerX, centerY);

        drawWheelSegments(graphics, centerX, centerY);
        drawCenterCircle(graphics, centerX, centerY);
        drawAbilityNames(graphics, centerX, centerY);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawWheelSegments(GuiGraphics graphics, int centerX, int centerY) {
        if (abilityNames.length == 0) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float anglePerSegment = 360.0f / abilityNames.length;

        for (int i = 0; i < abilityNames.length; i++) {
            float startAngle = (float) Math.toRadians(i * anglePerSegment - 90);
            float endAngle = (float) Math.toRadians((i + 1) * anglePerSegment - 90);

            int color = getSegmentColor(i);
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (color >> 24) & 0xFF;

            drawSegmentOptimized(buffer, matrix, centerX, centerY, startAngle, endAngle, r, g, b, a);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private int getSegmentColor(int index) {
        if (index == currentSelection) {
            return 0xFF4A90E2;
        } else if (index == hoveredIndex) {
            return 0xFF5FA3D8;
        } else {
            return 0xFF2C3E50;
        }
    }

    private void drawSegmentOptimized(BufferBuilder buffer, Matrix4f matrix, int centerX, int centerY,
                                      float startAngle, float endAngle, int r, int g, int b, int a) {
        float angleStep = (endAngle - startAngle) / SEGMENTS_DETAIL;

        for (int i = 0; i < SEGMENTS_DETAIL; i++) {
            float angle1 = startAngle + i * angleStep;
            float angle2 = startAngle + (i + 1) * angleStep;

            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);
            float cos2 = (float) Math.cos(angle2);
            float sin2 = (float) Math.sin(angle2);

            // Inner points
            float x1 = centerX + cos1 * CENTER_RADIUS;
            float y1 = centerY + sin1 * CENTER_RADIUS;
            float x2 = centerX + cos2 * CENTER_RADIUS;
            float y2 = centerY + sin2 * CENTER_RADIUS;

            // Outer points
            float x3 = centerX + cos1 * WHEEL_RADIUS;
            float y3 = centerY + sin1 * WHEEL_RADIUS;
            float x4 = centerX + cos2 * WHEEL_RADIUS;
            float y4 = centerY + sin2 * WHEEL_RADIUS;

            // Draw quad (4 vertices)
            buffer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x4, y4, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x3, y3, 0).setColor(r, g, b, a);
        }
    }

    private void drawCenterCircle(GuiGraphics graphics, int centerX, int centerY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Center point
        buffer.addVertex(matrix, centerX, centerY, 0).setColor(51, 51, 51, 255);

        // Circle vertices
        int circleSegments = 32;
        for (int i = 0; i <= circleSegments; i++) {
            float angle = (float) (i * 2 * Math.PI / circleSegments);
            float x = centerX + (float) Math.cos(angle) * CENTER_RADIUS;
            float y = centerY + (float) Math.sin(angle) * CENTER_RADIUS;
            buffer.addVertex(matrix, x, y, 0).setColor(51, 51, 51, 255);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawAbilityNames(GuiGraphics graphics, int centerX, int centerY) {
        if (abilityNames.length == 0) return;

        float anglePerSegment = 360.0f / abilityNames.length;

        for (int i = 0; i < abilityNames.length; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90 + anglePerSegment / 2);
            int textRadius = WHEEL_RADIUS + 20;
            int x = centerX + (int)(Math.cos(angle) * textRadius);
            int y = centerY + (int)(Math.sin(angle) * textRadius);

            Component text = Component.translatable(abilityNames[i]);
            int textWidth = this.font.width(text);

            int textColor = (i == hoveredIndex || i == currentSelection) ? 0xFFFFFF : 0xAAAAAA;
            graphics.drawString(this.font, text, x - textWidth / 2, y - 4, textColor, false);
        }
    }

    private int getHoveredSegment(int mouseX, int mouseY, int centerX, int centerY) {
        if (abilityNames.length == 0) return -1;

        int dx = mouseX - centerX;
        int dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < CENTER_RADIUS || distance > SEGMENT_HOVER_RADIUS) {
            return -1;
        }

        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;

        float anglePerSegment = 360.0f / abilityNames.length;
        int segment = (int)(angle / anglePerSegment);

        return segment % abilityNames.length;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Don't close on click, wait for release
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (hoveredIndex >= 0) {
                PacketHandler.sendToServer(new AbilitySelectionPacket(abilityItem, hoveredIndex));
            }
            this.onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Allow all keys to pass through without closing the screen
        // This enables movement while the wheel is open
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // Still allow ESC to close the wheel
        return true;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
}
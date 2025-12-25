package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class AbilityWheelOverlay {
    private static AbilityWheelOverlay instance;
    private static boolean registered = false;

    private SelectableAbilityItem abilityItem;
    private Player player;
    private String[] abilityNames;
    private int hoveredIndex = -1;
    private int currentSelection;
    private boolean isOpen = false;

    // Mouse offset for centering
    private double mouseOffsetX = 0;
    private double mouseOffsetY = 0;

    private static final int WHEEL_RADIUS = 80;
    private static final int CENTER_RADIUS = 20;
    private static final int SEGMENT_HOVER_RADIUS = 90;
    private static final int SEGMENTS_DETAIL = 32;

    public static AbilityWheelOverlay getInstance() {
        if (instance == null) {
            instance = new AbilityWheelOverlay();
            // Register to event bus when instance is created
            if (!registered) {
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(instance);
                registered = true;
            }
        }
        return instance;
    }

    public void open(SelectableAbilityItem abilityItem, Player player) {
        this.abilityItem = abilityItem;
        this.player = player;
        this.abilityNames = abilityItem.getAbilityNamesCopy();
        this.currentSelection = getSelectedAbilityIndex();
        this.isOpen = true;

        // Calculate offset to center the cursor when opening
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        double mouseX = mc.mouseHandler.xpos() * screenWidth / mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * screenHeight / mc.getWindow().getScreenHeight();

        this.mouseOffsetX = centerX - mouseX;
        this.mouseOffsetY = centerY - mouseY;
    }

    public void close() {
        this.isOpen = false;
        this.mouseOffsetX = 0;
        this.mouseOffsetY = 0;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public SelectableAbilityItem getAbilityItem() {
        return abilityItem;
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

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (!isOpen) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Get mouse position with offset applied
        double mouseX = mc.mouseHandler.xpos() * screenWidth / mc.getWindow().getScreenWidth() + mouseOffsetX;
        double mouseY = mc.mouseHandler.ypos() * screenHeight / mc.getWindow().getScreenHeight() + mouseOffsetY;

        hoveredIndex = getHoveredSegment((int)mouseX, (int)mouseY, centerX, centerY);

        // Draw semi-transparent background
        graphics.fill(0, 0, screenWidth, screenHeight, 0x66000000);

        drawWheelSegments(graphics, centerX, centerY);
        drawCenterCircle(graphics, centerX, centerY);
        drawAbilityNames(graphics, centerX, centerY, mc);
        drawCursor(graphics, (int)mouseX, (int)mouseY);
    }

    private void drawCursor(GuiGraphics graphics, int mouseX, int mouseY) {
        // Draw a crosshair cursor
        int size = 5;
        int thickness = 2;
        int color = 0xFFFFFFFF; // White

        // Horizontal line
        graphics.fill(mouseX - size, mouseY - thickness / 2, mouseX + size, mouseY + thickness / 2, color);
        // Vertical line
        graphics.fill(mouseX - thickness / 2, mouseY - size, mouseX + thickness / 2, mouseY + size, color);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Center point
        buffer.addVertex(matrix, mouseX, mouseY, 0).setColor(255, 255, 255, 255);

        // Circle vertices (small center dot)
        int circleSegments = 16;
        int dotRadius = 2;
        for (int i = 0; i <= circleSegments; i++) {
            float angle = (float) (i * 2 * Math.PI / circleSegments);
            float x = mouseX + (float) Math.cos(angle) * dotRadius;
            float y = mouseY + (float) Math.sin(angle) * dotRadius;
            buffer.addVertex(matrix, x, y, 0).setColor(255, 255, 255, 255);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
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

            float x1 = centerX + cos1 * CENTER_RADIUS;
            float y1 = centerY + sin1 * CENTER_RADIUS;
            float x2 = centerX + cos2 * CENTER_RADIUS;
            float y2 = centerY + sin2 * CENTER_RADIUS;

            float x3 = centerX + cos1 * WHEEL_RADIUS;
            float y3 = centerY + sin1 * WHEEL_RADIUS;
            float x4 = centerX + cos2 * WHEEL_RADIUS;
            float y4 = centerY + sin2 * WHEEL_RADIUS;

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

        buffer.addVertex(matrix, centerX, centerY, 0).setColor(51, 51, 51, 255);

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

    private void drawAbilityNames(GuiGraphics graphics, int centerX, int centerY, Minecraft mc) {
        if (abilityNames.length == 0) return;

        float anglePerSegment = 360.0f / abilityNames.length;

        for (int i = 0; i < abilityNames.length; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90 + anglePerSegment / 2);
            int textRadius = WHEEL_RADIUS + 20;
            int x = centerX + (int)(Math.cos(angle) * textRadius);
            int y = centerY + (int)(Math.sin(angle) * textRadius);

            Component text = Component.translatable(abilityNames[i]);
            int textWidth = mc.font.width(text);

            int textColor = (i == hoveredIndex || i == currentSelection) ? 0xFFFFFF : 0xAAAAAA;
            graphics.drawString(mc.font, text, x - textWidth / 2, y - 4, textColor, false);
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

    public void handleMouseRelease() {
        if (isOpen && hoveredIndex >= 0) {
            PacketHandler.sendToServer(new AbilitySelectionPacket(abilityItem, hoveredIndex));
        }
        close();
    }
}
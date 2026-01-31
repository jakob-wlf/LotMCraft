package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.PerformMiraclePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class MiracleWheelOverlay {
    private static MiracleWheelOverlay instance;
    private static boolean registered = false;

    public Long lastClosedMs = 0L;

    private Player player;
    private String[] abilityNames;
    private int hoveredIndex = -1;
    private boolean isOpen = false;

    private double mouseOffsetX = 0;
    private double mouseOffsetY = 0;

    private static final int WHEEL_RADIUS = 85;
    private static final int CENTER_RADIUS = 22;
    private static final int SEGMENT_HOVER_RADIUS = 95;
    private static final int SEGMENTS_DETAIL = 40;

    public static MiracleWheelOverlay getInstance() {
        if (instance == null) {
            instance = new MiracleWheelOverlay();
            if (!registered) {
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(instance);
                registered = true;
            }
        }
        return instance;
    }

    public void open(Player player, String... abilityNames) {
        this.player = player;
        this.abilityNames = abilityNames;
        this.isOpen = true;

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

        lastClosedMs = System.currentTimeMillis();
    }

    public boolean isOpen() {
        return isOpen;
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Pre event) {
        if (!isOpen) return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player != player){
            close();
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        double mouseX = mc.mouseHandler.xpos() * screenWidth / mc.getWindow().getScreenWidth() + mouseOffsetX;
        double mouseY = mc.mouseHandler.ypos() * screenHeight / mc.getWindow().getScreenHeight() + mouseOffsetY;

        hoveredIndex = getHoveredSegment((int)mouseX, (int)mouseY, centerX, centerY);

        // Draw background glow
        drawBackgroundGlow(graphics, centerX, centerY);

        // Draw wheel segments with gradients
        drawWheelSegments(graphics, centerX, centerY);

        // Draw outer ring
        drawOuterRing(graphics, centerX, centerY);

        // Draw segment borders
        drawSegmentBorders(graphics, centerX, centerY);

        // Draw center circle with gradient
        drawCenterCircle(graphics, centerX, centerY);

        // Draw ability names
        drawAbilityNames(graphics, centerX, centerY, mc);

        // Draw cursor
        drawCursor(graphics, (int)mouseX, (int)mouseY);
    }

    private void drawBackgroundGlow(GuiGraphics graphics, int centerX, int centerY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Center point - fully transparent
        buffer.addVertex(matrix, centerX, centerY, 0).setColor(80, 60, 100, 0);

        int glowRadius = WHEEL_RADIUS + 35;
        int circleSegments = 60;
        for (int i = 0; i <= circleSegments; i++) {
            float angle = (float) (i * 2 * Math.PI / circleSegments);
            float x = centerX + (float) Math.cos(angle) * glowRadius;
            float y = centerY + (float) Math.sin(angle) * glowRadius;
            buffer.addVertex(matrix, x, y, 0).setColor(60, 40, 80, 40);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawOuterRing(GuiGraphics graphics, int centerX, int centerY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        int innerRadius = WHEEL_RADIUS;
        int outerRadius = WHEEL_RADIUS + 3;
        int circleSegments = 60;

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < circleSegments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / circleSegments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / circleSegments);

            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);
            float cos2 = (float) Math.cos(angle2);
            float sin2 = (float) Math.sin(angle2);

            float x1 = centerX + cos1 * innerRadius;
            float y1 = centerY + sin1 * innerRadius;
            float x2 = centerX + cos2 * innerRadius;
            float y2 = centerY + sin2 * innerRadius;
            float x3 = centerX + cos1 * outerRadius;
            float y3 = centerY + sin1 * outerRadius;
            float x4 = centerX + cos2 * outerRadius;
            float y4 = centerY + sin2 * outerRadius;

            // Gradient from purple to lighter purple
            buffer.addVertex(matrix, x1, y1, 0).setColor(160, 120, 180, 220);
            buffer.addVertex(matrix, x2, y2, 0).setColor(160, 120, 180, 220);
            buffer.addVertex(matrix, x4, y4, 0).setColor(200, 160, 220, 180);
            buffer.addVertex(matrix, x3, y3, 0).setColor(200, 160, 220, 180);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawCursor(GuiGraphics graphics, int mouseX, int mouseY) {
        int size = 6;
        int thickness = 2;
        int gap = 3;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Brighter cursor with a subtle glow
        addQuad(buffer, matrix, mouseX - size, mouseY - thickness / 2, mouseX - gap, mouseY + thickness / 2, 255, 240, 200, 250);
        addQuad(buffer, matrix, mouseX + gap, mouseY - thickness / 2, mouseX + size, mouseY + thickness / 2, 255, 240, 200, 250);
        addQuad(buffer, matrix, mouseX - thickness / 2, mouseY - size, mouseX + thickness / 2, mouseY - gap, 255, 240, 200, 250);
        addQuad(buffer, matrix, mouseX - thickness / 2, mouseY + gap, mouseX + thickness / 2, mouseY + size, 255, 240, 200, 250);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, mouseX, mouseY, 0).setColor(255, 240, 200, 250);

        int circleSegments = 12;
        int dotRadius = 2;
        for (int i = 0; i <= circleSegments; i++) {
            float angle = (float) (i * 2 * Math.PI / circleSegments);
            float x = mouseX + (float) Math.cos(angle) * dotRadius;
            float y = mouseY + (float) Math.sin(angle) * dotRadius;
            buffer.addVertex(matrix, x, y, 0).setColor(255, 240, 200, 250);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void addQuad(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, int r, int g, int b, int a) {
        buffer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y1, 0).setColor(r, g, b, a);
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

            boolean isHovered = (i == hoveredIndex);
            drawSegmentWithGradient(buffer, matrix, centerX, centerY, startAngle, endAngle, isHovered);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawSegmentWithGradient(BufferBuilder buffer, Matrix4f matrix, int centerX, int centerY,
                                         float startAngle, float endAngle, boolean isHovered) {
        float angleStep = (endAngle - startAngle) / SEGMENTS_DETAIL;

        // Color values for gradient
        int innerR, innerG, innerB, innerA;
        int outerR, outerG, outerB, outerA;

        if (isHovered) {
            // Hovered: Brighter purple/magenta gradient
            innerR = 140; innerG = 100; innerB = 160; innerA = 240;
            outerR = 180; outerG = 130; outerB = 200; outerA = 220;
        } else {
            // Normal: Deeper purple gradient
            innerR = 80; innerG = 50; innerB = 100; innerA = 200;
            outerR = 120; outerG = 80; outerB = 140; outerA = 180;
        }

        for (int i = 0; i < SEGMENTS_DETAIL; i++) {
            float angle1 = startAngle + i * angleStep;
            float angle2 = startAngle + (i + 1) * angleStep;

            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);
            float cos2 = (float) Math.cos(angle2);
            float sin2 = (float) Math.sin(angle2);

            // Inner vertices (near center)
            float x1 = centerX + cos1 * CENTER_RADIUS;
            float y1 = centerY + sin1 * CENTER_RADIUS;
            float x2 = centerX + cos2 * CENTER_RADIUS;
            float y2 = centerY + sin2 * CENTER_RADIUS;

            // Outer vertices (edge of wheel)
            float x3 = centerX + cos1 * WHEEL_RADIUS;
            float y3 = centerY + sin1 * WHEEL_RADIUS;
            float x4 = centerX + cos2 * WHEEL_RADIUS;
            float y4 = centerY + sin2 * WHEEL_RADIUS;

            // Create gradient from inner (darker) to outer (lighter)
            buffer.addVertex(matrix, x1, y1, 0).setColor(innerR, innerG, innerB, innerA);
            buffer.addVertex(matrix, x2, y2, 0).setColor(innerR, innerG, innerB, innerA);
            buffer.addVertex(matrix, x4, y4, 0).setColor(outerR, outerG, outerB, outerA);
            buffer.addVertex(matrix, x3, y3, 0).setColor(outerR, outerG, outerB, outerA);
        }
    }

    private void drawSegmentBorders(GuiGraphics graphics, int centerX, int centerY) {
        if (abilityNames.length == 0) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.5f);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float anglePerSegment = 360.0f / abilityNames.length;

        for (int i = 0; i < abilityNames.length; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float x1 = centerX + cos * CENTER_RADIUS;
            float y1 = centerY + sin * CENTER_RADIUS;
            float x2 = centerX + cos * WHEEL_RADIUS;
            float y2 = centerY + sin * WHEEL_RADIUS;

            // Slightly brighter borders
            buffer.addVertex(matrix, x1, y1, 0).setColor(90, 80, 110, 180);
            buffer.addVertex(matrix, x2, y2, 0).setColor(90, 80, 110, 180);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
    }

    private void drawCenterCircle(GuiGraphics graphics, int centerX, int centerY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        // Draw filled circle with gradient
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Center is lighter
        buffer.addVertex(matrix, centerX, centerY, 0).setColor(100, 80, 120, 240);

        int circleSegments = 40;
        for (int i = 0; i <= circleSegments; i++) {
            float angle = (float) (i * 2 * Math.PI / circleSegments);
            float x = centerX + (float) Math.cos(angle) * CENTER_RADIUS;
            float y = centerY + (float) Math.sin(angle) * CENTER_RADIUS;
            // Edges are darker
            buffer.addVertex(matrix, x, y, 0).setColor(70, 55, 90, 240);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        // Draw border ring
        buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= circleSegments; i++) {
            float angle = (float) (i * 2 * Math.PI / circleSegments);
            float x = centerX + (float) Math.cos(angle) * CENTER_RADIUS;
            float y = centerY + (float) Math.sin(angle) * CENTER_RADIUS;
            buffer.addVertex(matrix, x, y, 0).setColor(130, 110, 150, 200);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawAbilityNames(GuiGraphics graphics, int centerX, int centerY, Minecraft mc) {
        if (abilityNames.length == 0) return;

        float anglePerSegment = 360.0f / abilityNames.length;

        for (int i = 0; i < abilityNames.length; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90 + anglePerSegment / 2);
            int textRadius = WHEEL_RADIUS + 25;
            int x = centerX + (int)(Math.cos(angle) * textRadius);
            int y = centerY + (int)(Math.sin(angle) * textRadius);

            Component text = Component.translatable("ability.lotmcraft.miracle_creation.miracle." + abilityNames[i]);
            int textWidth = mc.font.width(text);
            int textX = x - textWidth / 2;
            int textY = y - 4;

            boolean isActive = (i == hoveredIndex);

            // Enhanced shadow
            graphics.drawString(mc.font, text, textX + 1, textY + 1, 0xA0000000, false);

            // More vibrant colors
            int textColor = isActive ? 0xFFFFE0A0 : 0xFFE8D0E8;
            graphics.drawString(mc.font, text, textX, textY, textColor, false);
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
        if (isOpen && hoveredIndex >= 0 && hoveredIndex < abilityNames.length) {
            PacketHandler.sendToServer(new PerformMiraclePacket(abilityNames[hoveredIndex]));
        }
        close();
    }
}
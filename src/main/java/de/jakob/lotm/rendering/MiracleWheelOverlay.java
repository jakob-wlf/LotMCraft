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
    private static final int SEGMENTS_DETAIL = 48;

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

        double mouseX = mc.mouseHandler.xpos() * screenWidth / mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * screenHeight / mc.getWindow().getScreenHeight();

        this.mouseOffsetX = screenWidth / 2.0 - mouseX;
        this.mouseOffsetY = screenHeight / 2.0 - mouseY;
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
        if (mc.player != player) {
            close();
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        double rawMouseX = mc.mouseHandler.xpos() * screenWidth / mc.getWindow().getScreenWidth() + mouseOffsetX;
        double rawMouseY = mc.mouseHandler.ypos() * screenHeight / mc.getWindow().getScreenHeight() + mouseOffsetY;
        double mouseX = centerX + (rawMouseX - centerX) * 2.0;
        double mouseY = centerY + (rawMouseY - centerY) * 2.0;

        hoveredIndex = getHoveredSegment((int) mouseX, (int) mouseY, centerX, centerY);

        drawBackgroundGlow(graphics, centerX, centerY);
        drawWheelSegments(graphics, centerX, centerY);
        if (hoveredIndex >= 0) drawHoverGlow(graphics, centerX, centerY);
        drawOuterRing(graphics, centerX, centerY);
        drawSegmentBorders(graphics, centerX, centerY);
        drawCenterCircle(graphics, centerX, centerY);
        drawAbilityNames(graphics, centerX, centerY, mc);
        drawCursor(graphics, (int) mouseX, (int) mouseY);
    }

    private void drawBackgroundGlow(GuiGraphics graphics, int centerX, int centerY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        buffer.addVertex(matrix, centerX, centerY, 0).setColor(20, 10, 45, 30);

        int glowRadius = WHEEL_RADIUS + 50;
        for (int i = 0; i <= 64; i++) {
            float angle = (float) (i * 2 * Math.PI / 64);
            buffer.addVertex(matrix, centerX + (float) Math.cos(angle) * glowRadius,
                            centerY + (float) Math.sin(angle) * glowRadius, 0)
                    .setColor(15, 8, 40, 0);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawHoverGlow(GuiGraphics graphics, int centerX, int centerY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float anglePerSegment = 360.0f / abilityNames.length;
        float startAngle = (float) Math.toRadians(hoveredIndex * anglePerSegment - 90);
        float endAngle = (float) Math.toRadians((hoveredIndex + 1) * anglePerSegment - 90);
        float angleStep = (endAngle - startAngle) / SEGMENTS_DETAIL;

        int glowOuter = WHEEL_RADIUS + 14;

        for (int i = 0; i < SEGMENTS_DETAIL; i++) {
            float a1 = startAngle + i * angleStep;
            float a2 = startAngle + (i + 1) * angleStep;

            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            buffer.addVertex(matrix, centerX + cos1 * WHEEL_RADIUS, centerY + sin1 * WHEEL_RADIUS, 0).setColor(160, 110, 230, 60);
            buffer.addVertex(matrix, centerX + cos2 * WHEEL_RADIUS, centerY + sin2 * WHEEL_RADIUS, 0).setColor(160, 110, 230, 60);
            buffer.addVertex(matrix, centerX + cos2 * glowOuter, centerY + sin2 * glowOuter, 0).setColor(140, 90, 210, 0);
            buffer.addVertex(matrix, centerX + cos1 * glowOuter, centerY + sin1 * glowOuter, 0).setColor(140, 90, 210, 0);
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
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int innerRadius = WHEEL_RADIUS;
        int outerRadius = WHEEL_RADIUS + 2;

        for (int i = 0; i < 64; i++) {
            float a1 = (float) (i * 2 * Math.PI / 64);
            float a2 = (float) ((i + 1) * 2 * Math.PI / 64);

            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            buffer.addVertex(matrix, centerX + cos1 * innerRadius, centerY + sin1 * innerRadius, 0).setColor(120, 90, 180, 200);
            buffer.addVertex(matrix, centerX + cos2 * innerRadius, centerY + sin2 * innerRadius, 0).setColor(120, 90, 180, 200);
            buffer.addVertex(matrix, centerX + cos2 * outerRadius, centerY + sin2 * outerRadius, 0).setColor(180, 150, 230, 120);
            buffer.addVertex(matrix, centerX + cos1 * outerRadius, centerY + sin1 * outerRadius, 0).setColor(180, 150, 230, 120);
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
            drawSegmentWithGradient(buffer, matrix, centerX, centerY, startAngle, endAngle, i == hoveredIndex);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawSegmentWithGradient(BufferBuilder buffer, Matrix4f matrix, int centerX, int centerY,
                                         float startAngle, float endAngle, boolean isHovered) {
        float angleStep = (endAngle - startAngle) / SEGMENTS_DETAIL;

        int innerR, innerG, innerB, innerA;
        int outerR, outerG, outerB, outerA;

        if (isHovered) {
            innerR = 85;  innerG = 55;  innerB = 150; innerA = 230;
            outerR = 130; outerG = 90;  outerB = 200; outerA = 210;
        } else {
            innerR = 28;  innerG = 18;  innerB = 65;  innerA = 210;
            outerR = 55;  outerG = 38;  outerB = 105; outerA = 190;
        }

        for (int i = 0; i < SEGMENTS_DETAIL; i++) {
            float a1 = startAngle + i * angleStep;
            float a2 = startAngle + (i + 1) * angleStep;

            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            buffer.addVertex(matrix, centerX + cos1 * CENTER_RADIUS, centerY + sin1 * CENTER_RADIUS, 0).setColor(innerR, innerG, innerB, innerA);
            buffer.addVertex(matrix, centerX + cos2 * CENTER_RADIUS, centerY + sin2 * CENTER_RADIUS, 0).setColor(innerR, innerG, innerB, innerA);
            buffer.addVertex(matrix, centerX + cos2 * WHEEL_RADIUS,  centerY + sin2 * WHEEL_RADIUS,  0).setColor(outerR, outerG, outerB, outerA);
            buffer.addVertex(matrix, centerX + cos1 * WHEEL_RADIUS,  centerY + sin1 * WHEEL_RADIUS,  0).setColor(outerR, outerG, outerB, outerA);
        }

        if (isHovered) {
            float highlightWidth = (endAngle - startAngle) * 0.15f;
            float midAngle = (startAngle + endAngle) / 2;
            float hA = midAngle - highlightWidth / 2;
            float hB = midAngle + highlightWidth / 2;
            float hStep = (hB - hA) / 8;

            for (int i = 0; i < 8; i++) {
                float a1 = hA + i * hStep;
                float a2 = hA + (i + 1) * hStep;
                float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
                float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

                int hiR = 200, hiG = 170, hiB = 255, hiA = 30;
                buffer.addVertex(matrix, centerX + cos1 * CENTER_RADIUS, centerY + sin1 * CENTER_RADIUS, 0).setColor(hiR, hiG, hiB, hiA);
                buffer.addVertex(matrix, centerX + cos2 * CENTER_RADIUS, centerY + sin2 * CENTER_RADIUS, 0).setColor(hiR, hiG, hiB, hiA);
                buffer.addVertex(matrix, centerX + cos2 * WHEEL_RADIUS,  centerY + sin2 * WHEEL_RADIUS,  0).setColor(hiR, hiG, hiB, 0);
                buffer.addVertex(matrix, centerX + cos1 * WHEEL_RADIUS,  centerY + sin1 * WHEEL_RADIUS,  0).setColor(hiR, hiG, hiB, 0);
            }
        }
    }

    private void drawSegmentBorders(GuiGraphics graphics, int centerX, int centerY) {
        if (abilityNames.length == 0) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(1.0f);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float anglePerSegment = 360.0f / abilityNames.length;

        for (int i = 0; i < abilityNames.length; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90);
            float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);

            buffer.addVertex(matrix, centerX + cos * CENTER_RADIUS, centerY + sin * CENTER_RADIUS, 0).setColor(70, 55, 110, 100);
            buffer.addVertex(matrix, centerX + cos * WHEEL_RADIUS,  centerY + sin * WHEEL_RADIUS,  0).setColor(70, 55, 110, 100);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawCenterCircle(GuiGraphics graphics, int centerX, int centerY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, centerX, centerY, 0).setColor(140, 110, 200, 255);

        for (int i = 0; i <= 48; i++) {
            float angle = (float) (i * 2 * Math.PI / 48);
            buffer.addVertex(matrix,
                            centerX + (float) Math.cos(angle) * CENTER_RADIUS,
                            centerY + (float) Math.sin(angle) * CENTER_RADIUS, 0)
                    .setColor(40, 28, 80, 250);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int ringInner = CENTER_RADIUS;
        int ringOuter = CENTER_RADIUS + 2;

        for (int i = 0; i < 48; i++) {
            float a1 = (float) (i * 2 * Math.PI / 48);
            float a2 = (float) ((i + 1) * 2 * Math.PI / 48);
            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            buffer.addVertex(matrix, centerX + cos1 * ringInner, centerY + sin1 * ringInner, 0).setColor(160, 130, 220, 220);
            buffer.addVertex(matrix, centerX + cos2 * ringInner, centerY + sin2 * ringInner, 0).setColor(160, 130, 220, 220);
            buffer.addVertex(matrix, centerX + cos2 * ringOuter, centerY + sin2 * ringOuter, 0).setColor(100, 75, 160, 80);
            buffer.addVertex(matrix, centerX + cos1 * ringOuter, centerY + sin1 * ringOuter, 0).setColor(100, 75, 160, 80);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawAbilityNames(GuiGraphics graphics, int centerX, int centerY, Minecraft mc) {
        if (abilityNames.length == 0) return;

        float anglePerSegment = 360.0f / abilityNames.length;

        for (int i = 0; i < abilityNames.length; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90 + anglePerSegment / 2);
            int textRadius = WHEEL_RADIUS + 22;
            int x = centerX + (int) (Math.cos(angle) * textRadius);
            int y = centerY + (int) (Math.sin(angle) * textRadius);

            Component text = Component.translatable("ability.lotmcraft.miracle_creation.miracle." + abilityNames[i]);
            int textWidth = mc.font.width(text);
            int textX = x - textWidth / 2;
            int textY = y - 4;

            boolean isHovered = (i == hoveredIndex);

            graphics.drawString(mc.font, text, textX + 1, textY + 1, 0x55000000, false);
            graphics.drawString(mc.font, text, textX, textY, isHovered ? 0xFFFFD87A : 0xFFCCBBEE, false);
        }
    }

    private void drawCursor(GuiGraphics graphics, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int size = 5, gap = 3, thickness = 1;
        addQuad(buffer, matrix, mouseX - size, mouseY - thickness, mouseX - gap, mouseY + thickness, 255, 245, 210, 230);
        addQuad(buffer, matrix, mouseX + gap, mouseY - thickness, mouseX + size, mouseY + thickness, 255, 245, 210, 230);
        addQuad(buffer, matrix, mouseX - thickness, mouseY - size, mouseX + thickness, mouseY - gap, 255, 245, 210, 230);
        addQuad(buffer, matrix, mouseX - thickness, mouseY + gap, mouseX + thickness, mouseY + size, 255, 245, 210, 230);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, mouseX, mouseY, 0).setColor(255, 245, 210, 240);
        for (int i = 0; i <= 12; i++) {
            float angle = (float) (i * 2 * Math.PI / 12);
            buffer.addVertex(matrix, mouseX + (float) Math.cos(angle) * 2, mouseY + (float) Math.sin(angle) * 2, 0)
                    .setColor(255, 245, 210, 200);
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

    private int getHoveredSegment(int mouseX, int mouseY, int centerX, int centerY) {
        if (abilityNames.length == 0) return -1;

        int dx = mouseX - centerX;
        int dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < CENTER_RADIUS || distance > SEGMENT_HOVER_RADIUS) return -1;

        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;

        return (int) (angle / (360.0f / abilityNames.length)) % abilityNames.length;
    }

    public void handleMouseRelease() {
        if (isOpen && hoveredIndex >= 0 && hoveredIndex < abilityNames.length) {
            PacketHandler.sendToServer(new PerformMiraclePacket(abilityNames[hoveredIndex]));
        }
        close();
    }
}
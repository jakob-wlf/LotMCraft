package de.jakob.lotm.gui.custom.TextDisplay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public class ColoredTextDisplayScreen extends Screen {
    private final Screen previousScreen;
    private final List<ColoredTextLine> textLines;
    private final String title;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int LINE_HEIGHT = 10;
    private static final int PADDING = 15;
    private static final int TEXT_COLOR_DEFAULT = 0xFFFFFF;
    private static final int BACKGROUND_COLOR = 0xAA1a1a1a;
    private static final int BORDER_COLOR = 0xFF444444;

    public ColoredTextDisplayScreen(String title, Screen previousScreen) {
        super(Component.literal(title));
        this.title = title;
        this.previousScreen = previousScreen;
        this.textLines = new ArrayList<>();
    }

    public void addLine(String text) {
        addLine(text, TEXT_COLOR_DEFAULT);
    }

    public void addLine(String text, int color) {
        textLines.add(new ColoredTextLine(text, color));
    }

    public void addLines(List<String> texts, int color) {
        for (String text : texts) {
            addLine(text, color);
        }
    }

    public void addBlankLine() {
        textLines.add(new ColoredTextLine("", TEXT_COLOR_DEFAULT));
    }

    public void addSeparator(int color) {
        addLine("§m                                                                  §r", color);
    }

    public void addHeader(String headerText, int color) {
        addBlankLine();
        addLine("§n" + headerText + "§r", color);
        addBlankLine();
    }

    @Override
    protected void init() {
        super.init();
        int totalHeight = textLines.size() * LINE_HEIGHT;
        int displayHeight = this.height - (PADDING * 2) - 30;
        maxScroll = Math.max(0, totalHeight - displayHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int screenWidth = this.width;
        int screenHeight = this.height;
        int windowWidth = Math.min(600, screenWidth - 40);
        int windowHeight = Math.min(400, screenHeight - 40);
        int windowX = (screenWidth - windowWidth) / 2;
        int windowY = (screenHeight - windowHeight) / 2;

        guiGraphics.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, BACKGROUND_COLOR);
        guiGraphics.fill(windowX - 1, windowY - 1, windowX + windowWidth + 1, windowY + 1, BORDER_COLOR);
        guiGraphics.fill(windowX - 1, windowY + windowHeight - 1, windowX + windowWidth + 1, windowY + windowHeight + 1, BORDER_COLOR);
        guiGraphics.fill(windowX - 1, windowY, windowX, windowY + windowHeight, BORDER_COLOR);
        guiGraphics.fill(windowX + windowWidth, windowY, windowX + windowWidth + 1, windowY + windowHeight, BORDER_COLOR);

        guiGraphics.drawCenteredString(this.font, this.title, screenWidth / 2, windowY + PADDING - 5, 0xFFFFFF);

        guiGraphics.enableScissor(
                windowX + PADDING,
                windowY + PADDING + 15,
                windowX + windowWidth - PADDING,
                windowY + windowHeight - PADDING
        );

        int yOffset = windowY + PADDING + 15 - scrollOffset;
        for (ColoredTextLine line : textLines) {
            if (yOffset + LINE_HEIGHT > windowY + PADDING + 15 && yOffset < windowY + windowHeight - PADDING) {
                guiGraphics.drawString(this.font, line.text, windowX + PADDING + 5, yOffset, line.color, false);
            }
            yOffset += LINE_HEIGHT;
        }

        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int scrollAmount = (int) (scrollY * 3);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollAmount));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(previousScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class ColoredTextLine {
        String text;
        int color;

        ColoredTextLine(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }
}
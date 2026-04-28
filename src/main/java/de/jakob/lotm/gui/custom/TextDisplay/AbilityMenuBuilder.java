package de.jakob.lotm.gui.custom.TextDisplay;

import net.minecraft.client.gui.screens.Screen;
import java.util.ArrayList;
import java.util.List;

public class AbilityMenuBuilder {
    private final String title;
    private final Screen previousScreen;
    private final ColoredTextDisplayScreen screen;
    private final List<String> currentParagraph;

    public AbilityMenuBuilder(String title, Screen previousScreen) {
        this.title = title;
        this.previousScreen = previousScreen;
        this.screen = new ColoredTextDisplayScreen(title, previousScreen);
        this.currentParagraph = new ArrayList<>();
    }

    public AbilityMenuBuilder line(String text) {
        return line(text, TextColorHelper.WHITE);
    }

    public AbilityMenuBuilder line(String text, int color) {
        screen.addLine(text, color);
        return this;
    }

    public AbilityMenuBuilder header(String headerText) {
        return header(headerText, TextColorHelper.GOLD);
    }

    public AbilityMenuBuilder header(String headerText, int color) {
        screen.addHeader(headerText, color);
        return this;
    }

    public AbilityMenuBuilder spacing() {
        screen.addBlankLine();
        return this;
    }

    public AbilityMenuBuilder separator() {
        return separator(TextColorHelper.DARK_GRAY);
    }

    public AbilityMenuBuilder separator(int color) {
        screen.addSeparator(color);
        return this;
    }

    public AbilityMenuBuilder ability(String abilityName, String description) {
        return ability(abilityName, description, TextColorHelper.GOLD, TextColorHelper.WHITE);
    }

    public AbilityMenuBuilder ability(String abilityName, String description, int nameColor, int descColor) {
        screen.addLine("► " + abilityName, nameColor);
        screen.addLine("  " + description, descColor);
        return this;
    }

    public AbilityMenuBuilder bullet(String text) {
        return bullet(text, TextColorHelper.WHITE);
    }

    public AbilityMenuBuilder bullet(String text, int color) {
        screen.addLine("  • " + text, color);
        return this;
    }

    public AbilityMenuBuilder bullets(List<String> items) {
        return bullets(items, TextColorHelper.WHITE);
    }

    public AbilityMenuBuilder bullets(List<String> items, int color) {
        for (String item : items) {
            bullet(item, color);
        }
        return this;
    }

    public AbilityMenuBuilder warning(String text) {
        return line("⚠ " + text, TextColorHelper.RED);
    }

    public AbilityMenuBuilder tip(String text) {
        return line("💡 " + text, TextColorHelper.YELLOW);
    }

    public ColoredTextDisplayScreen build() {
        return screen;
    }
}
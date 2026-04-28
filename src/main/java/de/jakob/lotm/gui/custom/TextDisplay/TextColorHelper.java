package de.jakob.lotm.gui.custom.TextDisplay;

public class TextColorHelper {
    public static final int RED = 0xFF0000;
    public static final int DARK_RED = 0xAA0000;
    public static final int GREEN = 0x00FF00;
    public static final int DARK_GREEN = 0x00AA00;
    public static final int BLUE = 0x0000FF;
    public static final int DARK_BLUE = 0x0000AA;
    public static final int CYAN = 0x00FFFF;
    public static final int DARK_CYAN = 0x00AAAA;
    public static final int MAGENTA = 0xFF00FF;
    public static final int DARK_MAGENTA = 0xAA00AA;
    public static final int YELLOW = 0xFFFF00;
    public static final int DARK_YELLOW = 0xAA8800;
    public static final int WHITE = 0xFFFFFF;
    public static final int LIGHT_GRAY = 0xAAAAAA;
    public static final int DARK_GRAY = 0x555555;
    public static final int BLACK = 0x000000;

    public static final int GOLD = 0xFFD700;
    public static final int SILVER = 0xC0C0C0;
    public static final int COPPER = 0xB87333;
    public static final int PURPLE = 0x800080;
    public static final int ORANGE = 0xFFA500;
    public static final int BROWN = 0x8B4513;
    public static final int LIGHT_BLUE = 0x87CEEB;
    public static final int LIME = 0x32CD32;

    public static int rgb(int red, int green, int blue) {
        return (red << 16) | (green << 8) | blue;
    }

    public static int fromHex(String hex) {
        hex = hex.replace("#", "");
        return Integer.parseInt(hex, 16);
    }

    public static String toHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    public static int lighten(int color, float factor) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        red = Math.min(255, (int) (red * (1 + factor)));
        green = Math.min(255, (int) (green * (1 + factor)));
        blue = Math.min(255, (int) (blue * (1 + factor)));

        return rgb(red, green, blue);
    }

    public static int darken(int color, float factor) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        red = Math.max(0, (int) (red * (1 - factor)));
        green = Math.max(0, (int) (green * (1 - factor)));
        blue = Math.max(0, (int) (blue * (1 - factor)));

        return rgb(red, green, blue);
    }
}
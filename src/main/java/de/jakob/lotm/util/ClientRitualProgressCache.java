package de.jakob.lotm.util;

public final class ClientRitualProgressCache {
    private static int current;
    private static int total;
    private static String label = "Ritual";
    private static boolean timed;
    private static boolean active;

    private ClientRitualProgressCache() {
    }

    public static void update(int currentValue, int totalValue, String progressLabel,
                              boolean timedProgress, boolean isActive) {
        current = currentValue;
        total = totalValue;
        label = progressLabel;
        timed = timedProgress;
        active = isActive;
    }

    public static int getCurrent() {
        return current;
    }

    public static int getTotal() {
        return total;
    }

    public static String getLabel() {
        return label;
    }

    public static boolean isTimed() {
        return timed;
    }

    public static boolean isActive() {
        return active && total > 0;
    }

    public static void clear() {
        update(0, 0, "Ritual", false, false);
    }
}
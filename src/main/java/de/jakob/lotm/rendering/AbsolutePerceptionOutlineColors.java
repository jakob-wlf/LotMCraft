package de.jakob.lotm.rendering;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AbsolutePerceptionOutlineColors {
    private static final Map<Integer, Integer> COLORS = new ConcurrentHashMap<>();

    private AbsolutePerceptionOutlineColors() {
    }

    public static Integer get(int entityId) {
        return COLORS.get(entityId);
    }

    public static void replace(Map<Integer, Integer> colors) {
        COLORS.clear();
        COLORS.putAll(colors);
    }
}
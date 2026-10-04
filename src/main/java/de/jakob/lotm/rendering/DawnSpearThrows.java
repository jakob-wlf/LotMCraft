package de.jakob.lotm.rendering;

import java.util.HashSet;
import java.util.Set;

public final class DawnSpearThrows {
    private static final Set<Integer> ids = new HashSet<>();

    private DawnSpearThrows() {
    }

    public static void mark(int entityId) {
        ids.add(entityId);
    }

    public static void clear(int entityId) {
        ids.remove(entityId);
    }

    public static boolean contains(int entityId) {
        return ids.contains(entityId);
    }
}

package de.jakob.lotm.beyonders.abilities.fool.marionettes;

import java.util.HashMap;
import java.util.Map;

public final class ClientPuppetTimerCache {

    private ClientPuppetTimerCache() {}

    private static final class Entry {
        int total;
        float remaining;
    }

    private static final Map<Integer, Entry> ENTRIES = new HashMap<>();

    public static void update(int targetId, int total, int remaining) {
        if (remaining <= 0 || total <= 0) {
            ENTRIES.remove(targetId);
            return;
        }
        Entry e = ENTRIES.computeIfAbsent(targetId, k -> new Entry());
        e.total = total;
        e.remaining = remaining;
    }

    public static void tickDown() {
        if (ENTRIES.isEmpty()) return;
        ENTRIES.values().removeIf(e -> (e.remaining -= 1f) <= 0f);
    }

    public static boolean isActive() {
        return !ENTRIES.isEmpty();
    }

    public static int count() {
        return ENTRIES.size();
    }

    public static float[] getLongest() {
        Entry best = null;
        for (Entry e : ENTRIES.values()) {
            if (best == null || e.remaining > best.remaining) best = e;
        }
        return best == null ? null : new float[]{best.total, best.remaining};
    }

    public static void clear() {
        ENTRIES.clear();
    }
}
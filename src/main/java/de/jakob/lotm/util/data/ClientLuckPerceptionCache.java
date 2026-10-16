package de.jakob.lotm.util.data;

import de.jakob.lotm.network.packets.toClient.LuckPerceptionPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientLuckPerceptionCache {

    private ClientLuckPerceptionCache() {}

    private static final long TIMEOUT_MS = 1500;

    private static final Map<Integer, Double> mobLuck = new HashMap<>();
    private static double selfLuck = 0;
    private static boolean active = false;
    private static long lastUpdate = 0;

    public static void update(double self, List<LuckPerceptionPacket.Entry> entries) {
        selfLuck = self;
        mobLuck.clear();
        for (LuckPerceptionPacket.Entry e : entries) mobLuck.put(e.entityId(), e.luck());
        active = true;
        lastUpdate = System.currentTimeMillis();
    }

    public static void clear() {
        active = false;
        mobLuck.clear();
    }

    public static boolean isActive() {
        if (active && System.currentTimeMillis() - lastUpdate > TIMEOUT_MS) clear();
        return active;
    }

    public static double getSelfLuck() {
        return selfLuck;
    }

    public static Double getLuck(int entityId) {
        return isActive() ? mobLuck.get(entityId) : null;
    }
}
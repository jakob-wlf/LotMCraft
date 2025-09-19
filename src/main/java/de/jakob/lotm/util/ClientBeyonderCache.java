package de.jakob.lotm.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientBeyonderCache {
    private static final Map<UUID, BeyonderClientData> playerDataCache = new ConcurrentHashMap<>();

    public static void updatePlayerData(UUID playerUUID, String pathway, int sequence, float spirituality, boolean griefingEnabled) {
        playerDataCache.put(playerUUID, new BeyonderClientData(pathway, sequence, spirituality, griefingEnabled));
    }

    public static String getPathway(UUID playerUUID) {
        BeyonderClientData data = playerDataCache.get(playerUUID);
        return data != null ? data.pathway() : "none";
    }

    public static int getSequence(UUID playerUUID) {
        BeyonderClientData data = playerDataCache.get(playerUUID);
        return data != null ? data.sequence() : -1;
    }

    public static float getSpirituality(UUID playerUUID) {
        BeyonderClientData data = playerDataCache.get(playerUUID);
        return data != null ? Math.max(0, data.spirituality()) : 0.0f;
    }

    public static boolean isGriefingEnabled(UUID playerUUID) {
        BeyonderClientData data = playerDataCache.get(playerUUID);
        return data != null && data.griefingEnabled();
    }

    public static boolean isBeyonder(UUID playerUUID) {
        BeyonderClientData data = playerDataCache.get(playerUUID);
        return data != null && !data.pathway().equals("none") && data.sequence() >= 0;
    }

    public static void clearCache() {
        playerDataCache.clear();
    }

    public static void removePlayer(UUID playerUUID) {
        playerDataCache.remove(playerUUID);
    }

    // Inner record to store client-side beyonder data
    private record BeyonderClientData(String pathway, int sequence, float spirituality, boolean griefingEnabled) {}
}
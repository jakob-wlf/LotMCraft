package de.jakob.lotm.util;

import de.jakob.lotm.gamerule.ClientGameruleCache;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientBeyonderCache {
    private static final Map<UUID, BeyonderClientData> dataCache = new ConcurrentHashMap<>();

    public static void updateData(UUID playerUUID, String pathway, int sequence, float spirituality, boolean griefingEnabled, boolean isPlayer) {
        dataCache.put(playerUUID, new BeyonderClientData(pathway, sequence, spirituality, griefingEnabled));

        if(isPlayer) {
            float progress = spirituality / BeyonderData.getMaxSpirituality(sequence);
            SpiritualityProgressTracker.setProgress(playerUUID, progress);
        }
    }

    public static String getPathway(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.pathway() : "none";
    }

    public static int getSequence(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.sequence() : -1;
    }

    public static float getSpirituality(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? Math.max(0, data.spirituality()) : 0.0f;
    }

    public static boolean isGriefingEnabled(UUID playerUUID) {
        if(!ClientGameruleCache.isGlobalGriefingEnabled) {
            return false;
        }
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null && data.griefingEnabled();
    }

    public static boolean isBeyonder(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null && !data.pathway().equals("none") && data.sequence() >= 0;
    }

    public static void clearCache() {
        dataCache.clear();
    }

    public static void removePlayer(UUID playerUUID) {
        dataCache.remove(playerUUID);
    }

    // Inner record to store client-side beyonder data
    private record BeyonderClientData(String pathway, int sequence, float spirituality, boolean griefingEnabled) {}
}
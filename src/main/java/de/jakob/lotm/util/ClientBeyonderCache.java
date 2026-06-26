package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gamerule.ClientGameruleCache;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientBeyonderCache {
    private static final Map<UUID, BeyonderClientData> dataCache = new ConcurrentHashMap<>();

    public static void updateData(UUID playerUUID, String pathway, int sequence, float spirituality, boolean griefingEnabled, boolean isPlayer, float digestionProgress, int cowardWormAmount) {
        updateData(playerUUID, pathway, sequence, spirituality, griefingEnabled, isPlayer, digestionProgress, new String[10], new int[10], cowardWormAmount);
    }

    public static void updateData(UUID playerUUID, String pathway, int sequence, float spirituality, boolean griefingEnabled, boolean isPlayer, float digestionProgress, String[] pathwayHistory, int[] charStacks, int cowardWormAmount) {
        dataCache.put(playerUUID, new BeyonderClientData(pathway, sequence, spirituality, griefingEnabled, digestionProgress, pathwayHistory, charStacks, cowardWormAmount));
    }

    public static String getPathway(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.pathway() : "none";
    }

    public static int getSequence(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.sequence() : LOTMCraft.NON_BEYONDER_SEQ;
    }

    public static int getCharStack(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        if (data == null) return 0;
        int seq = data.sequence();
        return (seq >= 0 && seq < 10) ? data.charStacks()[seq] : 0;
    }

    public static int[] getCharStacks(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.charStacks() : new int[10];
    }

    public static void setCharStack(UUID playerUUID, int charStack) {
        BeyonderClientData data = dataCache.get(playerUUID);
        if (data != null) {
            int[] stacks = java.util.Arrays.copyOf(data.charStacks(), 10);
            int seq = data.sequence();
            if (seq >= 0 && seq < 10) stacks[seq] = charStack;
            dataCache.put(playerUUID, new BeyonderClientData(data.pathway(), data.sequence(), data.spirituality(), data.griefingEnabled(), data.digestionProgress(), data.pathwayHistory(), stacks, data.cowardWormAmount));
        }
    }

    public static int getCowardWormAmount(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.cowardWormAmount : 0;
    }

    public static float getDigestionProgress(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.digestionProgress() : 0.0f;
    }

    public static float getSpirituality(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);

        boolean hasData = data != null;

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

        return data != null && !data.pathway().equals("none") && data.sequence() != LOTMCraft.NON_BEYONDER_SEQ;
    }

    public static String[] getPathwayHistory(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.pathwayHistory() : new String[10];
    }

    public static void clearCache() {
        dataCache.clear();
    }

    public static void removePlayer(UUID playerUUID) {
        dataCache.remove(playerUUID);
    }

    // Inner record to store client-side beyonder data
    private record BeyonderClientData(String pathway, int sequence, float spirituality, boolean griefingEnabled, float digestionProgress, String[] pathwayHistory, int[] charStacks, int cowardWormAmount) {}
}
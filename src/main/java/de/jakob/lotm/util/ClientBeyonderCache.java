package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gamerule.ClientGameruleCache;
import de.jakob.lotm.util.playerMap.Characteristic;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientBeyonderCache {
    private static final Map<UUID, BeyonderClientData> dataCache = new ConcurrentHashMap<>();

    public static void updateData(UUID playerUUID, String pathway, int sequence, float spirituality, boolean griefingEnabled, boolean isPlayer, float digestionProgress) {
        updateData(playerUUID, pathway, sequence, spirituality, griefingEnabled, isPlayer, digestionProgress, new String[10], new ArrayList<>());
    }

    public static void updateData(UUID playerUUID, String pathway, int sequence, float spirituality, boolean griefingEnabled, boolean isPlayer, float digestionProgress, String[] pathwayHistory, ArrayList<Characteristic> charList) {
        dataCache.put(playerUUID, new BeyonderClientData(pathway, sequence, spirituality, griefingEnabled, digestionProgress, pathwayHistory, charList));
    }

    public static String getPathway(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.pathway() : "none";
    }

    public static int getSequence(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.sequence() : LOTMCraft.NON_BEYONDER_SEQ;
    }

    public static int getCharacteristicCount(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        if (data == null) return 0;
        return getCharacteristicCount(playerUUID, data.pathway());
    }

    public static int getCharacteristicCount(UUID playerUUID, String pathway) {
        BeyonderClientData data = dataCache.get(playerUUID);
        if (data == null) return 0;
        int seq = data.sequence();
        if (seq < 0 || seq >= 11) return 0;

        return data.charList().stream()
                .filter(c -> c.sequence() == seq && c.pathway().equals(pathway))
                .mapToInt(Characteristic::stack)
                .sum();
    }


    public static void setCharacteristicCount(UUID playerUUID, int charStack) {
        BeyonderClientData data = dataCache.get(playerUUID);
        if (data != null) {
            int seq = data.sequence();
            String pathway = data.pathway();
            ArrayList<Characteristic> newList = new ArrayList<>(data.charList());
            newList.removeIf(c -> c.sequence() == seq && c.pathway().equals(pathway));
            if (charStack > 0) {
                newList.add(new Characteristic(pathway, charStack, seq));
            }
            dataCache.put(playerUUID, new BeyonderClientData(data.pathway(), data.sequence(), data.spirituality(), data.griefingEnabled(), data.digestionProgress(), data.pathwayHistory(), newList));
        }
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

    public static int getHighestSequence(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        if (data == null) return LOTMCraft.NON_BEYONDER_SEQ;
        int min = data.sequence();
        for (Characteristic c : data.charList()) {
            if (c.sequence() < min) min = c.sequence();
        }
        return min;
    }

    public static String getHighestPathway(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        if (data == null) return "none";
        int min = data.sequence();
        String bestPathway = data.pathway();
        for (Characteristic c : data.charList()) {
            if (c.sequence() < min) {
                min = c.sequence();
                bestPathway = c.pathway();
            }
        }
        return bestPathway;
    }

    public static boolean isBeyonder(UUID playerUUID) {
        return getHighestSequence(playerUUID) < LOTMCraft.NON_BEYONDER_SEQ;
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

    public static ArrayList<Characteristic> getCharList(UUID playerUUID) {
        BeyonderClientData data = dataCache.get(playerUUID);
        return data != null ? data.charList() : new ArrayList<>();
    }

    // Inner record to store client-side beyonder data
    private record BeyonderClientData(String pathway, int sequence, float spirituality, boolean griefingEnabled, float digestionProgress, String[] pathwayHistory, ArrayList<Characteristic> charList) {}
}
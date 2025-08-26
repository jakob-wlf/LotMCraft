package de.jakob.lotm.util;

import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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

    public static boolean canPlayerUse(Player player, Map<String, Integer> requirements, float spiritualityCost) {
        UUID playerUUID = player.getUUID();
        String pathway = getPathway(playerUUID);
        int sequence = getSequence(playerUUID);
        float spirituality = getSpirituality(playerUUID);

        // Debug pathway always works
        if (pathway.equalsIgnoreCase("debug")) {
            return true;
        }

        // Check if pathway has requirements
        Integer minSeq = requirements.get(pathway);
        if (minSeq == null) {
            return false;
        }

        // Check sequence and spirituality requirements
        return sequence <= minSeq && spirituality >= spiritualityCost;
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
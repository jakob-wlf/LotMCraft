package de.jakob.lotm.network.packets.handlers;

import de.jakob.lotm.abilities.door.PlayerTeleportationAbility.PlayerInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPlayerListHandler {
    private static final Map<UUID, List<PlayerInfo>> clientPlayerCache = new ConcurrentHashMap<>();

    public static void updatePlayerList(UUID playerUUID, List<PlayerInfo> players) {
        clientPlayerCache.put(playerUUID, new ArrayList<>(players));
    }

    public static List<PlayerInfo> getPlayerList(UUID playerUUID) {
        return clientPlayerCache.getOrDefault(playerUUID, Collections.emptyList());
    }

    public static void clear(UUID playerUUID) {
        clientPlayerCache.remove(playerUUID);
    }
}
package de.jakob.lotm.beyonders.abilities.abyss.contract.registry;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SoulOwnershipRegistry {
    private static final Set<UUID> boundSouls = ConcurrentHashMap.newKeySet();

    public static boolean isSoulBound(UUID uuid) {
        return uuid != null && boundSouls.contains(uuid);
    }

    public static void bind(UUID uuid) {
        if (uuid != null) boundSouls.add(uuid);
    }

    public static void release(UUID uuid) {
        if (uuid != null) boundSouls.remove(uuid);
    }
}
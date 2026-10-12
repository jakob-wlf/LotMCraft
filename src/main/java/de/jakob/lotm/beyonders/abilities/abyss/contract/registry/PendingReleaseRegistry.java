package de.jakob.lotm.beyonders.abilities.abyss.contract.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PendingReleaseRegistry {
    public record PendingRelease(UUID releaseContractCasterUUID, long createdAtTick) {}
    // soul owner's UUID -> list of pending release contracts, oldest first
    private static final Map<UUID, List<PendingRelease>> pending = new ConcurrentHashMap<>();
    public static void register(UUID soulOwnerUUID, UUID releaseContractCasterUUID, long now) {
        pending.computeIfAbsent(soulOwnerUUID, k -> new ArrayList<>()).add(new PendingRelease(releaseContractCasterUUID, now));
    }
    //Pops and returns the oldest pending release for this soul owner, or null if none exist.
    public static PendingRelease claimOldest(UUID soulOwnerUUID) {
        List<PendingRelease> list = pending.get(soulOwnerUUID);
        if (list == null || list.isEmpty()) return null;
        list.sort(Comparator.comparingLong(PendingRelease::createdAtTick));
        PendingRelease oldest = list.removeFirst();
        if (list.isEmpty()) pending.remove(soulOwnerUUID);
        return oldest;
    }
    public static void remove(UUID soulOwnerUUID, UUID releaseContractCasterUUID) {
        List<PendingRelease> list = pending.get(soulOwnerUUID);
        if (list == null) return;
        list.removeIf(p -> p.releaseContractCasterUUID().equals(releaseContractCasterUUID));
        if (list.isEmpty()) pending.remove(soulOwnerUUID);
    }
}
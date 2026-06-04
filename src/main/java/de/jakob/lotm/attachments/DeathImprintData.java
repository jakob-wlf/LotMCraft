package de.jakob.lotm.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

/**
 * Server-persistent data for the River of Eternal Darkness Death Imprint system.
 *
 * Imprint tiers:
 *   0 = none, 1 = marked (always-succeed divination + river NPC ghost), 2 = can teleport + NPC less transparent,
 *   3 = River's Call (force into river), permanent (died in river) = always-visible NPC
 */
public class DeathImprintData extends SavedData {

    private static final String DATA_NAME = "deathImprintData";

    /** How many times each player has been imprinted (capped at 3). */
    private final Map<UUID, Integer> imprintCounts = new HashMap<>();

    /** Snapshot data for GUI display and NPC (name, pathway, sequence). */
    private final Map<UUID, CompoundTag> playerSnapshots = new HashMap<>();

    /** Players whose souls are permanently in the river (died there). */
    private final Set<UUID> permanentRiverSouls = new HashSet<>();

    /** Players currently trapped in the river (rivers-call active). Maps UUID → game tick when trap expires. */
    private final Map<UUID, Long> riverTrapped = new HashMap<>();

    /** Position each trapped player is locked to (set when River's Call teleports them). */
    private final Map<UUID, BlockPos> trapPositions = new HashMap<>();

    // ── Static access ──────────────────────────────────────────────────────────

    public static DeathImprintData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                DeathImprintData::new,
                DeathImprintData::load
        ), DATA_NAME);
    }

    // ── Imprint counts ─────────────────────────────────────────────────────────

    public int getImprintCount(UUID uuid) {
        return imprintCounts.getOrDefault(uuid, 0);
    }

    /**
     * Adds one imprint to the target, capped at 3.
     * @return the new imprint tier (1–3)
     */
    public int addImprint(UUID uuid) {
        int current = imprintCounts.getOrDefault(uuid, 0);
        int next = Math.min(current + 1, 3);
        imprintCounts.put(uuid, next);
        setDirty();
        return next;
    }

    public void setImprintCount(UUID uuid, int count) {
        if (count <= 0) {
            imprintCounts.remove(uuid);
        } else {
            imprintCounts.put(uuid, Math.min(count, 3));
        }
        setDirty();
    }

    /** @return all UUIDs that have at least one imprint or a permanent soul */
    public Set<UUID> getAllImprintedPlayers() {
        Set<UUID> all = new HashSet<>(imprintCounts.keySet());
        all.addAll(permanentRiverSouls);
        return all;
    }

    // ── Player snapshots ───────────────────────────────────────────────────────

    public void saveSnapshot(UUID uuid, String name, String pathway, int sequence) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("pathway", pathway);
        tag.putInt("sequence", sequence);
        playerSnapshots.put(uuid, tag);
        setDirty();
    }

    public CompoundTag getSnapshot(UUID uuid) {
        return playerSnapshots.getOrDefault(uuid, new CompoundTag());
    }

    public String getSnapshotName(UUID uuid) {
        return playerSnapshots.containsKey(uuid) ? playerSnapshots.get(uuid).getString("name") : uuid.toString().substring(0, 8);
    }

    public String getSnapshotPathway(UUID uuid) {
        return playerSnapshots.containsKey(uuid) ? playerSnapshots.get(uuid).getString("pathway") : "unknown";
    }

    public int getSnapshotSequence(UUID uuid) {
        return playerSnapshots.containsKey(uuid) ? playerSnapshots.get(uuid).getInt("sequence") : 9;
    }

    // ── Permanent river souls ──────────────────────────────────────────────────

    public void addPermanentRiverSoul(UUID uuid) {
        permanentRiverSouls.add(uuid);
        setDirty();
    }

    public boolean isPermanentRiverSoul(UUID uuid) {
        return permanentRiverSouls.contains(uuid);
    }

    public Set<UUID> getPermanentRiverSouls() {
        return Collections.unmodifiableSet(permanentRiverSouls);
    }

    // ── River trap ─────────────────────────────────────────────────────────────

    public void trapInRiver(UUID uuid, long expiryTick) {
        riverTrapped.put(uuid, expiryTick);
        setDirty();
    }

    public void releaseFromRiver(UUID uuid) {
        riverTrapped.remove(uuid);
        trapPositions.remove(uuid);
        setDirty();
    }

    public boolean isTrappedInRiver(UUID uuid) {
        return riverTrapped.containsKey(uuid);
    }

    public long getTrapExpiryTick(UUID uuid) {
        return riverTrapped.getOrDefault(uuid, 0L);
    }

    public Map<UUID, Long> getAllTrapped() {
        return Collections.unmodifiableMap(riverTrapped);
    }

    public void setTrapPosition(UUID uuid, BlockPos pos) {
        trapPositions.put(uuid, pos);
        setDirty();
    }

    public BlockPos getTrapPosition(UUID uuid) {
        return trapPositions.get(uuid);
    }

    // ── Save / Load ────────────────────────────────────────────────────────────

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // Imprint counts
        ListTag imprintList = new ListTag();
        for (Map.Entry<UUID, Integer> entry : imprintCounts.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("UUID", entry.getKey());
            e.putInt("count", entry.getValue());
            imprintList.add(e);
        }
        tag.put("imprintCounts", imprintList);

        // Snapshots
        ListTag snapshotList = new ListTag();
        for (Map.Entry<UUID, CompoundTag> entry : playerSnapshots.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("UUID", entry.getKey());
            e.put("data", entry.getValue().copy());
            snapshotList.add(e);
        }
        tag.put("playerSnapshots", snapshotList);

        // Permanent river souls
        ListTag permanentList = new ListTag();
        for (UUID uuid : permanentRiverSouls) {
            CompoundTag e = new CompoundTag();
            e.putUUID("UUID", uuid);
            permanentList.add(e);
        }
        tag.put("permanentRiverSouls", permanentList);

        // River trapped
        ListTag trappedList = new ListTag();
        for (Map.Entry<UUID, Long> entry : riverTrapped.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("UUID", entry.getKey());
            e.putLong("expiry", entry.getValue());
            trappedList.add(e);
        }
        tag.put("riverTrapped", trappedList);

        return tag;
    }

    public static DeathImprintData load(CompoundTag tag, HolderLookup.Provider provider) {
        DeathImprintData data = new DeathImprintData();

        ListTag imprintList = tag.getList("imprintCounts", Tag.TAG_COMPOUND);
        for (Tag t : imprintList) {
            CompoundTag e = (CompoundTag) t;
            data.imprintCounts.put(e.getUUID("UUID"), e.getInt("count"));
        }

        ListTag snapshotList = tag.getList("playerSnapshots", Tag.TAG_COMPOUND);
        for (Tag t : snapshotList) {
            CompoundTag e = (CompoundTag) t;
            data.playerSnapshots.put(e.getUUID("UUID"), e.getCompound("data"));
        }

        ListTag permanentList = tag.getList("permanentRiverSouls", Tag.TAG_COMPOUND);
        for (Tag t : permanentList) {
            data.permanentRiverSouls.add(((CompoundTag) t).getUUID("UUID"));
        }

        ListTag trappedList = tag.getList("riverTrapped", Tag.TAG_COMPOUND);
        for (Tag t : trappedList) {
            CompoundTag e = (CompoundTag) t;
            data.riverTrapped.put(e.getUUID("UUID"), e.getLong("expiry"));
        }

        return data;
    }
}

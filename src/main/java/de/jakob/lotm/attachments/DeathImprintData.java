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
import java.util.ArrayList;

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

    /** Abilities sealed by the River owner per player (max 2). Maps target UUID → list of ability IDs. */
    private final Map<UUID, List<String>> sealedAbilities = new HashMap<>();

    /** Souls stored in the River vault — persistent even through owner death. Max 27 slots. */
    private final List<CompoundTag> riverVault = new ArrayList<>();
    public static final int RIVER_VAULT_CAPACITY = 30;

    /** Real-time epoch (ms) at which each player's imprint count will decay by 1. */
    private final Map<UUID, Long> imprintDecayTick = new HashMap<>();

    /** Real-time milliseconds between imprint decay events: 12 hours. */
    public static final long IMPRINT_DECAY_INTERVAL_MS = 12L * 60L * 60L * 1_000L;

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
     * Resets the decay timer so the countdown always starts from the most recent imprint.
     * @return the new imprint tier (1–3)
     */
    public int addImprint(UUID uuid) {
        int current = imprintCounts.getOrDefault(uuid, 0);
        int next = Math.min(current + 1, 3);
        imprintCounts.put(uuid, next);
        // Decay timer is managed externally via scheduleDecay once the server is available.
        setDirty();
        return next;
    }

    /**
     * (Re-)schedules the decay timer for {@code uuid} to fire {@link #IMPRINT_DECAY_INTERVAL_MS}
     * after the current wall-clock time. Call this from the death event.
     */
    public void scheduleDecay(UUID uuid) {
        imprintDecayTick.put(uuid, System.currentTimeMillis() + IMPRINT_DECAY_INTERVAL_MS);
        setDirty();
    }

    public void setImprintCount(UUID uuid, int count) {
        int previous = imprintCounts.getOrDefault(uuid, 0);
        if (count <= 0) {
            imprintCounts.remove(uuid);
            imprintDecayTick.remove(uuid);
        } else {
            imprintCounts.put(uuid, Math.min(count, 3));
        }
        // If the new count drops below 2, clear any ability seals placed on this player
        int effective = count <= 0 ? 0 : Math.min(count, 3);
        if (effective < 2 && previous >= 2) {
            clearSealedAbilities(uuid);
        }
        setDirty();
    }

    /**
     * Checks all players whose 12-hour real-time decay timer has elapsed and decrements their
     * imprint count by 1. Should be called periodically from the server tick.
     * Returns the UUIDs whose count was changed so the caller can notify them.
     */
    public List<UUID> tickDecay(net.minecraft.server.MinecraftServer server) {
        long now = System.currentTimeMillis();
        List<UUID> decayed = new ArrayList<>();
        Iterator<Map.Entry<UUID, Long>> it = imprintDecayTick.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (now >= entry.getValue()) {
                UUID uuid = entry.getKey();
                int current = imprintCounts.getOrDefault(uuid, 0);
                if (current <= 1) {
                    imprintCounts.remove(uuid);
                    // Also clear seals if they had tier 2 before decay knocked them below
                    clearSealedAbilitiesAndUnapply(uuid, server);
                    it.remove();
                } else {
                    int next = current - 1;
                    imprintCounts.put(uuid, next);
                    // If dropped below tier 2, remove seals
                    if (next < 2) {
                        clearSealedAbilitiesAndUnapply(uuid, server);
                    }
                    // Schedule the next 12-hour decay from now
                    entry.setValue(now + IMPRINT_DECAY_INTERVAL_MS);
                }
                decayed.add(uuid);
                setDirty();
            }
        }
        return decayed;
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

    // ── Ability seals ──────────────────────────────────────────────────────────

    /** Set (or overwrite) the sealed abilities for a player (up to 2). */
    public void setSealedAbilities(UUID target, List<String> abilityIds) {
        if (abilityIds == null || abilityIds.isEmpty()) {
            sealedAbilities.remove(target);
        } else {
            sealedAbilities.put(target, new ArrayList<>(abilityIds.subList(0, Math.min(2, abilityIds.size()))));
        }
        setDirty();
    }

    /** Returns the sealed ability IDs for a player, or an empty list. */
    public List<String> getSealedAbilities(UUID target) {
        return sealedAbilities.getOrDefault(target, Collections.emptyList());
    }

    /** Returns true if the given ability is sealed for the player. */
    public boolean isAbilitySealed(UUID target, String abilityId) {
        List<String> seals = sealedAbilities.get(target);
        return seals != null && seals.contains(abilityId);
    }

    /** Clear all seals for a player (called when imprints drop below 2 or sefirot is lost). */
    public void clearSealedAbilities(UUID target) {
        if (sealedAbilities.remove(target) != null) setDirty();
    }

    /** Clear seals for ALL players (called when sefirot is unclaimed). */
    public void clearAllSealedAbilities() {
        if (!sealedAbilities.isEmpty()) {
            sealedAbilities.clear();
            setDirty();
        }
    }

    /**
     * Clear seals for ALL players and immediately remove the seal enforcement from
     * every online player. Call this when the sefirot is unclaimed.
     */
    public void clearAllSealedAbilitiesAndUnapply(net.minecraft.server.MinecraftServer server) {
        if (sealedAbilities.isEmpty()) return;
        // Collect all sealed UUIDs before clearing
        List<UUID> sealedUUIDs = new ArrayList<>(sealedAbilities.keySet());
        sealedAbilities.clear();
        setDirty();
        for (UUID uuid : sealedUUIDs) {
            net.minecraft.server.level.ServerPlayer online = server.getPlayerList().getPlayer(uuid);
            if (online != null) {
                online.getData(de.jakob.lotm.attachments.ModAttachments.DISABLED_ABILITIES_COMPONENT)
                        .clearCause(SEAL_CAUSE);
            }
        }
    }

    // ── River Soul Vault ───────────────────────────────────────────────────────

    public List<CompoundTag> getRiverVault() {
        return Collections.unmodifiableList(riverVault);
    }

    public int getRiverVaultSize() {
        return riverVault.size();
    }

    public boolean isRiverVaultFull() {
        return riverVault.size() >= RIVER_VAULT_CAPACITY;
    }

    public boolean addToRiverVault(CompoundTag soul) {
        if (soul == null || isRiverVaultFull()) return false;
        riverVault.add(soul.copy());
        setDirty();
        return true;
    }

    /** Removes and returns the vault soul with the given SoulKey, or null if not found. */
    public CompoundTag removeFromRiverVaultByKey(String soulKey) {
        if (soulKey == null || soulKey.isEmpty()) return null;
        for (java.util.Iterator<CompoundTag> it = riverVault.iterator(); it.hasNext();) {
            CompoundTag soul = it.next();
            if (soulKey.equals(soul.getString("SoulKey"))) {
                it.remove();
                setDirty();
                return soul;
            }
        }
        return null;
    }

    // ── Live-player enforcement helpers ────────────────────────────────────────

    /**
     * The cause key used when disabling abilities via death-imprint seals.
     * Kept as a constant so it can be referenced consistently everywhere.
     */
    public static final String SEAL_CAUSE = "death_imprint_seal";

    /**
     * Re-applies any persisted ability seals for {@code player} into their
     * {@link de.jakob.lotm.attachments.DisabledAbilitiesComponent}.
     * Call this on player login so seals survive log-out / log-in cycles.
     */
    public void reapplySealedAbilities(net.minecraft.server.level.ServerPlayer player) {
        de.jakob.lotm.attachments.DisabledAbilitiesComponent comp =
                player.getData(de.jakob.lotm.attachments.ModAttachments.DISABLED_ABILITIES_COMPONENT);
        // Clear any stale entries for this cause, then re-apply the current sealed list.
        comp.clearCause(SEAL_CAUSE);
        for (String id : getSealedAbilities(player.getUUID())) {
            comp.disableSpecificAbility(id, SEAL_CAUSE);
        }
    }

    /**
     * Removes the seal enforcement for all abilities that were sealed for {@code uuid}
     * from the live player (if online) and clears the stored seals.
     */
    public void clearSealedAbilitiesAndUnapply(UUID uuid, net.minecraft.server.MinecraftServer server) {
        List<String> seals = new ArrayList<>(getSealedAbilities(uuid));
        clearSealedAbilities(uuid);
        net.minecraft.server.level.ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null && !seals.isEmpty()) {
            de.jakob.lotm.attachments.DisabledAbilitiesComponent comp =
                    online.getData(de.jakob.lotm.attachments.ModAttachments.DISABLED_ABILITIES_COMPONENT);
            for (String id : seals) {
                comp.enableSpecificAbility(id, SEAL_CAUSE);
            }
        }
    }

    /** Returns all individual ability IDs currently sealed for this player. */
    private List<String> getAllSealedAbilityIds(UUID uuid) {
        return new ArrayList<>(getSealedAbilities(uuid));
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

        // Sealed abilities
        ListTag sealedList = new ListTag();
        for (Map.Entry<UUID, List<String>> entry : sealedAbilities.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("UUID", entry.getKey());
            ListTag ids = new ListTag();
            for (String id : entry.getValue()) {
                net.minecraft.nbt.StringTag st = net.minecraft.nbt.StringTag.valueOf(id);
                ids.add(st);
            }
            e.put("abilities", ids);
            sealedList.add(e);
        }
        tag.put("sealedAbilities", sealedList);

        // Imprint decay timers (real-time epoch ms)
        ListTag decayList = new ListTag();
        for (Map.Entry<UUID, Long> entry : imprintDecayTick.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("UUID", entry.getKey());
            e.putLong("decayTimeMs", entry.getValue());
            decayList.add(e);
        }
        tag.put("imprintDecayTick", decayList);

        // River vault
        ListTag vaultList = new ListTag();
        for (CompoundTag soul : riverVault) vaultList.add(soul.copy());
        tag.put("riverVault", vaultList);

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

        // Sealed abilities
        ListTag sealedList = tag.getList("sealedAbilities", Tag.TAG_COMPOUND);
        for (Tag t : sealedList) {
            CompoundTag e = (CompoundTag) t;
            UUID uuid = e.getUUID("UUID");
            ListTag ids = e.getList("abilities", Tag.TAG_STRING);
            List<String> abilityIds = new ArrayList<>();
            for (Tag idTag : ids) abilityIds.add(idTag.getAsString());
            data.sealedAbilities.put(uuid, abilityIds);
        }

        // Imprint decay timers (real-time epoch ms)
        ListTag decayList = tag.getList("imprintDecayTick", Tag.TAG_COMPOUND);
        for (Tag t : decayList) {
            CompoundTag e = (CompoundTag) t;
            // "decayTimeMs" is the new real-time key; skip legacy game-tick entries ("decayTick")
            if (e.contains("decayTimeMs")) {
                data.imprintDecayTick.put(e.getUUID("UUID"), e.getLong("decayTimeMs"));
            }
        }

        // River vault
        if (tag.contains("riverVault", Tag.TAG_LIST)) {
            ListTag vaultList = tag.getList("riverVault", Tag.TAG_COMPOUND);
            for (Tag t : vaultList) data.riverVault.add(((CompoundTag) t).copy());
        }

        return data;
    }
}

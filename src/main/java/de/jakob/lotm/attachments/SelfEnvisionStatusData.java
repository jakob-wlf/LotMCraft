package de.jakob.lotm.attachments;

import de.jakob.lotm.status.StatusSnapshot;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

/**
 * Stores the Chaos Sea authority's self-status Envisioning data:
 * <ul>
 *   <li>3 snapshot slots — each can be overwritten only once every 3 real hours.</li>
 *   <li>Pressing Restore applies the snapshot for 30 minutes, then reverts.</li>
 *   <li>If the player died after the snapshot was saved, the slot is blocked.</li>
 *   <li>If the player dies during the 30-min restore window, the revert is cancelled.</li>
 * </ul>
 */
public class SelfEnvisionStatusData extends SavedData {

    private static final String DATA_NAME = "self_envision_status_data";

    public static final int    MAX_SLOTS           = 3;
    public static final long   OVERWRITE_COOLDOWN  = 3L * 60 * 60 * 1000; // 3 h in ms
    public static final long   RESTORE_DURATION_MS = 30L * 60 * 1000;     // 30 min in ms

    // ── Inner types ───────────────────────────────────────────────────────────

    public static final class Slot {
        public StatusSnapshot snapshot;    // null = empty
        public long           writeTimeMs; // when last written (0 if empty)

        public Slot() {}
        public Slot(StatusSnapshot snapshot, long writeTimeMs) {
            this.snapshot    = snapshot;
            this.writeTimeMs = writeTimeMs;
        }

        public boolean isEmpty()             { return snapshot == null; }
        public long    cooldownRemainingMs() {
            if (isEmpty()) return 0L;
            long r = OVERWRITE_COOLDOWN - (System.currentTimeMillis() - writeTimeMs);
            return Math.max(0L, r);
        }
        public boolean canOverwrite() { return cooldownRemainingMs() == 0; }
    }

    public static final class ActiveRestore {
        public StatusSnapshot preSnapshot;  // state before restore was applied
        public long           expiryMs;     // epoch ms when revert should fire
        public boolean        cancelled;    // true if player died during restore window

        public ActiveRestore(StatusSnapshot pre, long expiryMs) {
            this.preSnapshot = pre;
            this.expiryMs    = expiryMs;
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────

    // casterUUID → Slot[MAX_SLOTS]
    private final Map<UUID, Slot[]> slotMap          = new HashMap<>();
    // casterUUID → epoch ms of last death
    private final Map<UUID, Long>   lastDeathTimes   = new HashMap<>();
    // casterUUID → active restore
    private final Map<UUID, ActiveRestore> activeRestores = new HashMap<>();

    // ── Factory ───────────────────────────────────────────────────────────────

    public static SelfEnvisionStatusData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(SelfEnvisionStatusData::new, SelfEnvisionStatusData::load),
                DATA_NAME);
    }

    // ── Slot access ───────────────────────────────────────────────────────────

    public Slot[] getSlots(UUID caster) {
        return slotMap.computeIfAbsent(caster, k -> {
            Slot[] s = new Slot[MAX_SLOTS];
            for (int i = 0; i < MAX_SLOTS; i++) s[i] = new Slot();
            return s;
        });
    }

    public Slot getSlot(UUID caster, int index) {
        return getSlots(caster)[index];
    }

    // ── Save slot ─────────────────────────────────────────────────────────────

    /** Returns an error message if the slot cannot be saved right now, or null on success. */
    public String trySaveSlot(UUID caster, int slotIndex, StatusSnapshot snapshot) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return "Invalid slot index.";
        Slot slot = getSlot(caster, slotIndex);
        if (!slot.canOverwrite()) {
            long secs = slot.cooldownRemainingMs() / 1000;
            return "Slot " + (slotIndex + 1) + " is locked for " + formatTime(secs) + ".";
        }
        slot.snapshot    = snapshot;
        slot.writeTimeMs = System.currentTimeMillis();
        setDirty();
        return null; // success
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    public boolean hasActiveRestore(UUID caster) { return activeRestores.containsKey(caster); }

    public ActiveRestore getActiveRestore(UUID caster) { return activeRestores.get(caster); }

    /**
     * Cancel the active restore and return the pre-restore snapshot so the caller can revert the player.
     * Returns null if no restore was active.
     */
    public StatusSnapshot cancelRestore(UUID caster) {
        ActiveRestore ar = activeRestores.remove(caster);
        setDirty();
        return ar == null ? null : ar.preSnapshot;
    }

    /**
     * Returns an error message if the slot cannot be restored, or null on success.
     * On success, the pre-restore snapshot is stored and the expiry is set.
     * Caller is responsible for calling {@link StatusSnapshot#applyTo(net.minecraft.server.level.ServerPlayer)}.
     */
    public String tryStartRestore(UUID caster, int slotIndex, StatusSnapshot preSnapshot) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return "Invalid slot index.";
        Slot slot = getSlot(caster, slotIndex);
        if (slot.isEmpty()) return "Slot " + (slotIndex + 1) + " is empty.";
        if (hasActiveRestore(caster)) return "A restore is already active.";

        // Death check: if the player died after this snapshot was saved, block restore
        Long lastDeath = lastDeathTimes.get(caster);
        if (lastDeath != null && lastDeath > slot.snapshot.captureTimeMs) {
            return "State invalidated — you died after this snapshot.";
        }

        activeRestores.put(caster, new ActiveRestore(preSnapshot,
                System.currentTimeMillis() + RESTORE_DURATION_MS));
        setDirty();
        return null;
    }

    /** Apply the revert for an expired or cancelled restore. Returns the pre-snapshot, or null if none. */
    public StatusSnapshot concludeRestore(UUID caster) {
        ActiveRestore ar = activeRestores.remove(caster);
        setDirty();
        if (ar == null || ar.cancelled) return null;
        return ar.preSnapshot;
    }

    // ── Death tracking ────────────────────────────────────────────────────────

    /** Returns true if the player died after the given snapshot capture time. */
    public boolean isBlockedByDeath(UUID uuid, long captureTimeMs) {
        Long last = lastDeathTimes.get(uuid);
        return last != null && last > captureTimeMs;
    }

    public void onPlayerDied(UUID uuid) {
        lastDeathTimes.put(uuid, System.currentTimeMillis());
        // Cancel any active restore so the revert doesn't fire on respawn
        ActiveRestore ar = activeRestores.get(uuid);
        if (ar != null) ar.cancelled = true;
        setDirty();
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    /**
     * Called every server tick (throttled externally).
     * Finds expired restores and reverts them.
     */
    public void tickRestores(MinecraftServer server) {
        long now = System.currentTimeMillis();
        List<UUID> toRevert = new ArrayList<>();
        for (Map.Entry<UUID, ActiveRestore> e : activeRestores.entrySet()) {
            if (now >= e.getValue().expiryMs) toRevert.add(e.getKey());
        }
        for (UUID uuid : toRevert) {
            StatusSnapshot pre = concludeRestore(uuid);
            if (pre == null) continue; // cancelled or missing
            net.minecraft.server.level.ServerPlayer player =
                    server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                pre.applyTo(player);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7[Envisioning] Status restore expired — reverting to previous state."));
            }
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag slotsTag = new CompoundTag();
        for (Map.Entry<UUID, Slot[]> e : slotMap.entrySet()) {
            ListTag list = new ListTag();
            for (Slot s : e.getValue()) {
                CompoundTag st = new CompoundTag();
                st.putBoolean("empty", s.isEmpty());
                if (!s.isEmpty()) {
                    st.put("snapshot", s.snapshot.save());
                    st.putLong("writeTimeMs", s.writeTimeMs);
                }
                list.add(st);
            }
            slotsTag.put(e.getKey().toString(), list);
        }
        tag.put("slots", slotsTag);

        CompoundTag deathTag = new CompoundTag();
        lastDeathTimes.forEach((k, v) -> deathTag.putLong(k.toString(), v));
        tag.put("deaths", deathTag);

        CompoundTag restoreTag = new CompoundTag();
        for (Map.Entry<UUID, ActiveRestore> e : activeRestores.entrySet()) {
            ActiveRestore ar = e.getValue();
            CompoundTag r = new CompoundTag();
            r.put("pre",        ar.preSnapshot.save());
            r.putLong("expiry", ar.expiryMs);
            r.putBoolean("cancelled", ar.cancelled);
            restoreTag.put(e.getKey().toString(), r);
        }
        tag.put("restores", restoreTag);
        return tag;
    }

    public static SelfEnvisionStatusData load(CompoundTag tag, HolderLookup.Provider registries) {
        SelfEnvisionStatusData data = new SelfEnvisionStatusData();

        CompoundTag slotsTag = tag.getCompound("slots");
        for (String key : slotsTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                ListTag list = slotsTag.getList(key, net.minecraft.nbt.Tag.TAG_COMPOUND);
                Slot[] slots = new Slot[MAX_SLOTS];
                for (int i = 0; i < MAX_SLOTS; i++) slots[i] = new Slot();
                for (int i = 0; i < Math.min(list.size(), MAX_SLOTS); i++) {
                    CompoundTag st = list.getCompound(i);
                    if (!st.getBoolean("empty")) {
                        slots[i] = new Slot(
                                StatusSnapshot.load(st.getCompound("snapshot")),
                                st.getLong("writeTimeMs"));
                    }
                }
                data.slotMap.put(uuid, slots);
            } catch (IllegalArgumentException ignored) {}
        }

        CompoundTag deathTag = tag.getCompound("deaths");
        for (String key : deathTag.getAllKeys()) {
            try { data.lastDeathTimes.put(UUID.fromString(key), deathTag.getLong(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        CompoundTag restoreTag = tag.getCompound("restores");
        for (String key : restoreTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                CompoundTag r = restoreTag.getCompound(key);
                ActiveRestore ar = new ActiveRestore(
                        StatusSnapshot.load(r.getCompound("pre")),
                        r.getLong("expiry"));
                ar.cancelled = r.getBoolean("cancelled");
                data.activeRestores.put(uuid, ar);
            } catch (IllegalArgumentException ignored) {}
        }
        return data;
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private static String formatTime(long totalSecs) {
        long h = totalSecs / 3600, m = (totalSecs % 3600) / 60, s = totalSecs % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }
}

package de.jakob.lotm.attachments;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.status.StatusSnapshot;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

/**
 * Stores the Chaos Sea authority's target-status Envisioning data:
 * <ul>
 *   <li>2 slots total per caster — each can target any player.</li>
 *   <li>Either 1 player × 2 slots, or 2 players × 1 slot each.</li>
 *   <li>Each slot overwrite has a 3-hour cooldown.</li>
 *   <li>Restore lasts 5 min normally; 1 min if target has a Sefirot or is a Great Old One.</li>
 *   <li>Restore has a global 3-hour cooldown (only one restore every 3 hours).</li>
 * </ul>
 */
public class TargetEnvisionStatusData extends SavedData {

    private static final String DATA_NAME = "target_envision_status_data";

    public static final int  MAX_SLOTS             = 2;
    public static final long OVERWRITE_COOLDOWN    = 3L * 60 * 60 * 1000; // 3 h
    public static final long RESTORE_COOLDOWN      = 3L * 60 * 60 * 1000; // 3 h
    public static final long RESTORE_NORMAL_MS     = 5L * 60 * 1000;      // 5 min
    public static final long RESTORE_SEFIROT_MS    = 1L * 60 * 1000;      // 1 min

    // ── Inner types ───────────────────────────────────────────────────────────

    public static final class TargetSlot {
        public StatusSnapshot snapshot;
        public String         targetName;  // name of the player whose state was snapped
        public long           writeTimeMs;

        public TargetSlot() {}
        public TargetSlot(StatusSnapshot snap, String targetName, long writeTimeMs) {
            this.snapshot    = snap;
            this.targetName  = targetName;
            this.writeTimeMs = writeTimeMs;
        }

        public boolean isEmpty() { return snapshot == null; }
        public long cooldownRemainingMs() {
            if (isEmpty()) return 0L;
            return Math.max(0L, OVERWRITE_COOLDOWN - (System.currentTimeMillis() - writeTimeMs));
        }
        public boolean canOverwrite() { return cooldownRemainingMs() == 0; }
    }

    public static final class ActiveTargetRestore {
        public UUID           casterUUID;
        public UUID           targetUUID;
        public StatusSnapshot preSnapshot;
        public long           expiryMs;

        public ActiveTargetRestore(UUID casterUUID, UUID targetUUID, StatusSnapshot pre, long expiry) {
            this.casterUUID  = casterUUID;
            this.targetUUID  = targetUUID;
            this.preSnapshot = pre;
            this.expiryMs    = expiry;
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────

    // casterUUID → TargetSlot[MAX_SLOTS]
    private final Map<UUID, TargetSlot[]> slotMap        = new HashMap<>();
    // casterUUID → last time a restore was applied (ms)
    private final Map<UUID, Long>         lastRestoreTime = new HashMap<>();
    // targetUUID → active restore
    private final Map<UUID, ActiveTargetRestore> activeRestores = new HashMap<>();

    // ── Factory ───────────────────────────────────────────────────────────────

    public static TargetEnvisionStatusData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(TargetEnvisionStatusData::new, TargetEnvisionStatusData::load),
                DATA_NAME);
    }

    // ── Slot access ───────────────────────────────────────────────────────────

    public TargetSlot[] getSlots(UUID caster) {
        return slotMap.computeIfAbsent(caster, k -> {
            TargetSlot[] s = new TargetSlot[MAX_SLOTS];
            for (int i = 0; i < MAX_SLOTS; i++) s[i] = new TargetSlot();
            return s;
        });
    }

    public TargetSlot getSlot(UUID caster, int index) {
        return getSlots(caster)[index];
    }

    // ── Save slot ─────────────────────────────────────────────────────────────

    public String trySaveSlot(UUID caster, int slotIndex, StatusSnapshot snap, String targetName) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return "Invalid slot.";
        TargetSlot slot = getSlot(caster, slotIndex);
        if (!slot.canOverwrite()) {
            return "Slot " + (slotIndex + 1) + " locked for " + formatTime(slot.cooldownRemainingMs() / 1000) + ".";
        }
        slot.snapshot    = snap;
        slot.targetName  = targetName;
        slot.writeTimeMs = System.currentTimeMillis();
        setDirty();
        return null;
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    public long restoreCooldownRemainingMs(UUID caster) {
        Long last = lastRestoreTime.get(caster);
        if (last == null) return 0L;
        return Math.max(0L, RESTORE_COOLDOWN - (System.currentTimeMillis() - last));
    }

    public boolean isTargetBeingRestored(UUID targetUUID) {
        return activeRestores.containsKey(targetUUID);
    }

    /** Read-only view of the active restores map, for searching by caster. */
    public Map<UUID, ActiveTargetRestore> getActiveRestoreMap() {
        return java.util.Collections.unmodifiableMap(activeRestores);
    }

    /**
     * Start a restore of the snapshot in {@code slotIndex} onto {@code target}.
     * @param durationMs  pass {@link #RESTORE_NORMAL_MS} or {@link #RESTORE_SEFIROT_MS}.
     * @param preSnapshot current state of target (to revert to when restore expires).
     * @return error message or null on success.
     */
    public String tryStartRestore(UUID caster, int slotIndex,
                                  UUID targetUUID, StatusSnapshot preSnapshot,
                                  long durationMs) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return "Invalid slot.";
        TargetSlot slot = getSlot(caster, slotIndex);
        if (slot.isEmpty()) return "Slot " + (slotIndex + 1) + " is empty.";

        long cd = restoreCooldownRemainingMs(caster);
        if (cd > 0) return "Restore on cooldown for " + formatTime(cd / 1000) + ".";
        if (isTargetBeingRestored(targetUUID)) return "Target already has an active status restore.";

        lastRestoreTime.put(caster, System.currentTimeMillis());
        activeRestores.put(targetUUID, new ActiveTargetRestore(
                caster, targetUUID, preSnapshot, System.currentTimeMillis() + durationMs));
        setDirty();
        return null;
    }

    public StatusSnapshot concludeRestore(UUID targetUUID) {
        ActiveTargetRestore ar = activeRestores.remove(targetUUID);
        setDirty();
        return ar == null ? null : ar.preSnapshot;
    }

    /**
     * Cancel the active restore started by {@code caster}.
     * Returns the {@link ActiveTargetRestore} record (containing preSnapshot and targetUUID)
     * so the caller can revert the target player, or null if none was found.
     */
    public ActiveTargetRestore cancelActiveRestoreForCaster(UUID caster) {
        UUID targetUUID = null;
        for (Map.Entry<UUID, ActiveTargetRestore> e : activeRestores.entrySet()) {
            if (caster.equals(e.getValue().casterUUID)) {
                targetUUID = e.getKey();
                break;
            }
        }
        if (targetUUID != null) {
            ActiveTargetRestore ar = activeRestores.remove(targetUUID);
            setDirty();
            return ar;
        }
        return null;
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    public void tickRestores(MinecraftServer server) {
        long now = System.currentTimeMillis();
        List<UUID> toRevert = new ArrayList<>();
        for (Map.Entry<UUID, ActiveTargetRestore> e : activeRestores.entrySet()) {
            if (now >= e.getValue().expiryMs) toRevert.add(e.getKey());
        }
        for (UUID uuid : toRevert) {
            StatusSnapshot pre = concludeRestore(uuid);
            if (pre == null) continue;
            net.minecraft.server.level.ServerPlayer player =
                    server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                pre.applyTo(player);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7[Envisioning] Imposed status restore expired — reverting."));
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Check if a target is a Great Old One or has claimed a Sefirot. */
    public static boolean targetHasSefirotOrGOO(net.minecraft.server.level.ServerPlayer target) {
        // GOO check
        if (de.jakob.lotm.util.BeyonderData.getSequence(target) == LOTMCraft.GREAT_OLD_ONE_SEQ) return true;
        // Sefirot check
        String claimed = SefirotData.get(target.getServer()).getClaimedSefirot(target.getUUID());
        return claimed != null && !claimed.isEmpty();
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag slotsTag = new CompoundTag();
        for (Map.Entry<UUID, TargetSlot[]> e : slotMap.entrySet()) {
            ListTag list = new ListTag();
            for (TargetSlot s : e.getValue()) {
                CompoundTag st = new CompoundTag();
                st.putBoolean("empty", s.isEmpty());
                if (!s.isEmpty()) {
                    st.put("snapshot", s.snapshot.save());
                    st.putString("targetName", s.targetName != null ? s.targetName : "");
                    st.putLong("writeTimeMs", s.writeTimeMs);
                }
                list.add(st);
            }
            slotsTag.put(e.getKey().toString(), list);
        }
        tag.put("slots", slotsTag);

        CompoundTag cdTag = new CompoundTag();
        lastRestoreTime.forEach((k, v) -> cdTag.putLong(k.toString(), v));
        tag.put("restoreCd", cdTag);

        CompoundTag restoreTag = new CompoundTag();
        for (Map.Entry<UUID, ActiveTargetRestore> e : activeRestores.entrySet()) {
            ActiveTargetRestore ar = e.getValue();
            CompoundTag r = new CompoundTag();
            if (ar.casterUUID != null) r.putString("casterUUID", ar.casterUUID.toString());
            r.putString("targetUUID", ar.targetUUID.toString());
            r.put("pre",       ar.preSnapshot.save());
            r.putLong("expiry", ar.expiryMs);
            restoreTag.put(e.getKey().toString(), r);
        }
        tag.put("restores", restoreTag);
        return tag;
    }

    public static TargetEnvisionStatusData load(CompoundTag tag, HolderLookup.Provider registries) {
        TargetEnvisionStatusData data = new TargetEnvisionStatusData();

        CompoundTag slotsTag = tag.getCompound("slots");
        for (String key : slotsTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                ListTag list = slotsTag.getList(key, net.minecraft.nbt.Tag.TAG_COMPOUND);
                TargetSlot[] slots = new TargetSlot[MAX_SLOTS];
                for (int i = 0; i < MAX_SLOTS; i++) slots[i] = new TargetSlot();
                for (int i = 0; i < Math.min(list.size(), MAX_SLOTS); i++) {
                    CompoundTag st = list.getCompound(i);
                    if (!st.getBoolean("empty")) {
                        slots[i] = new TargetSlot(
                                StatusSnapshot.load(st.getCompound("snapshot")),
                                st.getString("targetName"),
                                st.getLong("writeTimeMs"));
                    }
                }
                data.slotMap.put(uuid, slots);
            } catch (IllegalArgumentException ignored) {}
        }

        CompoundTag cdTag = tag.getCompound("restoreCd");
        for (String key : cdTag.getAllKeys()) {
            try { data.lastRestoreTime.put(UUID.fromString(key), cdTag.getLong(key)); }
            catch (IllegalArgumentException ignored) {}
        }

        CompoundTag restoreTag = tag.getCompound("restores");
        for (String key : restoreTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                CompoundTag r = restoreTag.getCompound(key);
                UUID casterUUID = r.contains("casterUUID") ? UUID.fromString(r.getString("casterUUID")) : uuid;
                data.activeRestores.put(uuid, new ActiveTargetRestore(
                        casterUUID,
                        UUID.fromString(r.getString("targetUUID")),
                        StatusSnapshot.load(r.getCompound("pre")),
                        r.getLong("expiry")));
            } catch (IllegalArgumentException ignored) {}
        }
        return data;
    }

    private static String formatTime(long totalSecs) {
        long h = totalSecs / 3600, m = (totalSecs % 3600) / 60, s = totalSecs % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }
}

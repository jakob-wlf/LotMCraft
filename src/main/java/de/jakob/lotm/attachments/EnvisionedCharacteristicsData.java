package de.jakob.lotm.attachments;

import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

/**
 * Stores up to 3 temporarily envisioned characteristics per player.
 *
 * When a slot is activated the corresponding characteristic is added to the
 * player's {@link de.jakob.lotm.attachments.BeyonderComponent} charList
 * (stack = 1). On expiry or manual release it is removed again.
 *
 * Duration: {@link #SLOT_DURATION_MS} (30 minutes).
 * Max slots: {@link #MAX_SLOTS} (3).
 */
public class EnvisionedCharacteristicsData extends SavedData {

    private static final String DATA_NAME       = "envisioned_characteristics_data";
    public  static final int    MAX_SLOTS        = 3;
    /** Maximum number of target-envisioned slots the Chaos Sea owner can place on one player. */
    public  static final int    TARGET_MAX_SLOTS = 1;
    public  static final long   SLOT_DURATION_MS = 30L * 60 * 1000; // 30 min (self)
    /** Duration when envisioning onto a target who is NOT a Great Old One and has no sefirot. */
    public  static final long   TARGET_SLOT_DURATION_MS       = 5L * 60 * 1000;  // 5 min
    /** Reduced duration when the target IS a Great Old One or claims a sefirot. */
    public  static final long   TARGET_SLOT_SHORT_DURATION_MS = 1L * 60 * 1000;  // 1 min
    public  static final long   COOLDOWN_MS      = 3L * 60 * 60 * 1000; // 3 hours

    // ── Inner type ────────────────────────────────────────────────────────────

    public static final class Slot {
        public String pathway  = "";
        public int    sequence = -1;
        public long   expiryMs = 0L;

        public Slot() {}

        public Slot(String pathway, int sequence, long expiryMs) {
            this.pathway  = pathway;
            this.sequence = sequence;
            this.expiryMs = expiryMs;
        }

        public boolean isEmpty() { return pathway == null || pathway.isEmpty() || sequence < 0; }

        public long remainingMs() {
            if (isEmpty()) return 0L;
            return Math.max(0L, expiryMs - System.currentTimeMillis());
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────

    /** casterUUID → Slot[MAX_SLOTS] */
    private final Map<UUID, Slot[]> slotMap = new HashMap<>();
    /** casterUUID → { "pathway:seq" → cooldownUntilMs } */
    private final Map<UUID, Map<String, Long>> cooldownMap = new HashMap<>();

    // ── Factory ───────────────────────────────────────────────────────────────

    public static EnvisionedCharacteristicsData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(EnvisionedCharacteristicsData::new, EnvisionedCharacteristicsData::load),
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

    /** How many slots are currently occupied. */
    public int activeCount(UUID caster) {
        int n = 0;
        for (Slot s : getSlots(caster)) if (!s.isEmpty()) n++;
        return n;
    }

    // ── Envision ──────────────────────────────────────────────────────────────

    /**
     * Activates an envisioned characteristic slot.
     * The caller is responsible for actually adding the characteristic to the
     * player's BeyonderComponent before calling this.
     *
     * Returns the slot index used (0–2), or -1 if all slots are occupied and
     * the request should be denied.
     */
    public int addSlot(UUID caster, String pathway, int sequence) {
        return addSlot(caster, pathway, sequence, SLOT_DURATION_MS);
    }

    /**
     * Activates an envisioned characteristic slot with an explicit duration.
     * Use this variant for target-envisioning where the duration differs from the self default.
     */
    public int addSlot(UUID caster, String pathway, int sequence, long durationMs) {
        Slot[] slots = getSlots(caster);

        // Find first empty slot
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i].isEmpty()) {
                slots[i] = new Slot(pathway, sequence,
                        System.currentTimeMillis() + durationMs);
                setDirty();
                return i;
            }
        }
        return -1; // no free slot
    }

    /**
     * Releases a slot by index. Returns the released slot (or an empty one if
     * the index was already empty).
     */
    public Slot releaseSlot(UUID caster, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) return new Slot();
        Slot[] slots = getSlots(caster);
        Slot released = slots[slotIndex];
        slots[slotIndex] = new Slot();
        setDirty();
        return released;
    }

    /**
     * Checks whether a particular (pathway, sequence) pair is already envisioned.
     */
    public boolean isEnvisioned(UUID caster, String pathway, int sequence) {
        for (Slot s : getSlots(caster)) {
            if (!s.isEmpty() && s.pathway.equals(pathway) && s.sequence == sequence) return true;
        }
        return false;
    }

    // ── Cooldown ─────────────────────────────────────────────────────────────────

    private static String cdKey(String pathway, int seq) { return pathway + ":" + seq; }

    public void addCooldown(UUID caster, String pathway, int sequence) {
        cooldownMap.computeIfAbsent(caster, k -> new HashMap<>())
                   .put(cdKey(pathway, sequence), System.currentTimeMillis() + COOLDOWN_MS);
        setDirty();
    }

    public boolean isOnCooldown(UUID caster, String pathway, int sequence) {
        Map<String, Long> m = cooldownMap.get(caster);
        if (m == null) return false;
        Long until = m.get(cdKey(pathway, sequence));
        if (until == null) return false;
        if (System.currentTimeMillis() >= until) { m.remove(cdKey(pathway, sequence)); return false; }
        return true;
    }

    public long getCooldownRemainingMs(UUID caster, String pathway, int sequence) {
        Map<String, Long> m = cooldownMap.get(caster);
        if (m == null) return 0L;
        Long until = m.get(cdKey(pathway, sequence));
        return until == null ? 0L : Math.max(0L, until - System.currentTimeMillis());
    }

    /** Returns all active (non-expired) cooldowns for this player, ready for the sync packet. */
    public List<de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.CooldownInfo> getCooldownsForSync(UUID caster) {
        var result = new java.util.ArrayList<de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.CooldownInfo>();
        Map<String, Long> m = cooldownMap.get(caster);
        if (m == null) return result;
        long now = System.currentTimeMillis();
        m.entrySet().removeIf(e -> e.getValue() <= now);
        for (var e : m.entrySet()) {
            int colon = e.getKey().lastIndexOf(':');
            if (colon <= 0) continue;
            try {
                result.add(new de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.CooldownInfo(
                        e.getKey().substring(0, colon),
                        Integer.parseInt(e.getKey().substring(colon + 1)),
                        e.getValue()));
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    /**
     * Called every server second. Expires any slots whose time has elapsed and
     * removes the corresponding characteristic from the player's component.
     */
    public void tickSlots(MinecraftServer server) {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Slot[]> entry : slotMap.entrySet()) {
            UUID uuid = entry.getKey();
            Slot[] slots = entry.getValue();
            for (int i = 0; i < MAX_SLOTS; i++) {
                Slot s = slots[i];
                if (s.isEmpty()) continue;
                if (now >= s.expiryMs) {
                    String expiredPath = s.pathway;
                    int    expiredSeq  = s.sequence;
                    slots[i] = new Slot(); // clear slot
                    addCooldown(uuid, expiredPath, expiredSeq); // start 3-hour cooldown
                    setDirty();

                    ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                    if (player != null) {
                        removeCharacteristicFromPlayer(player, expiredPath, expiredSeq);
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§7[Chaos Sea] Envisioned characteristic expired: §e"
                                + prettify(expiredPath) + " Seq " + expiredSeq + " §7(3h cooldown)"));
                        // Sync updated envisioned state back to client
                        sendSync(player, this);
                    }
                }
            }
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag playerList = new ListTag();
        for (Map.Entry<UUID, Slot[]> entry : slotMap.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", entry.getKey());
            ListTag slotsTag = new ListTag();
            for (Slot s : entry.getValue()) {
                CompoundTag st = new CompoundTag();
                st.putString("pathway", s.pathway == null ? "" : s.pathway);
                st.putInt("sequence", s.sequence);
                st.putLong("expiry", s.expiryMs);
                slotsTag.add(st);
            }
            playerTag.put("slots", slotsTag);
            playerList.add(playerTag);
        }
        tag.put("players", playerList);
        // Save cooldowns
        ListTag cdList = new ListTag();
        for (Map.Entry<UUID, Map<String, Long>> cde : cooldownMap.entrySet()) {
            CompoundTag ct = new CompoundTag();
            ct.putUUID("uuid", cde.getKey());
            ListTag cdEntries = new ListTag();
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> e : cde.getValue().entrySet()) {
                if (e.getValue() <= now) continue;
                CompoundTag ced = new CompoundTag();
                ced.putString("key", e.getKey());
                ced.putLong("until", e.getValue());
                cdEntries.add(ced);
            }
            if (!cdEntries.isEmpty()) {
                ct.put("entries", cdEntries);
                cdList.add(ct);
            }
        }
        tag.put("cooldowns", cdList);
        return tag;
    }

    public static EnvisionedCharacteristicsData load(CompoundTag tag, HolderLookup.Provider provider) {
        EnvisionedCharacteristicsData data = new EnvisionedCharacteristicsData();
        if (!tag.contains("players")) return data;
        ListTag playerList = tag.getList("players", 10 /* COMPOUND */);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag pt = playerList.getCompound(i);
            UUID uuid = pt.getUUID("uuid");
            ListTag slotsTag = pt.getList("slots", 10);
            Slot[] slots = new Slot[MAX_SLOTS];
            for (int j = 0; j < MAX_SLOTS; j++) {
                slots[j] = new Slot();
            }
            for (int j = 0; j < Math.min(slotsTag.size(), MAX_SLOTS); j++) {
                CompoundTag st = slotsTag.getCompound(j);
                slots[j] = new Slot(
                        st.getString("pathway"),
                        st.getInt("sequence"),
                        st.getLong("expiry")
                );
            }
            data.slotMap.put(uuid, slots);
        }
        // Load cooldowns
        if (tag.contains("cooldowns")) {
            ListTag cdList = tag.getList("cooldowns", 10);
            for (int i = 0; i < cdList.size(); i++) {
                CompoundTag ct = cdList.getCompound(i);
                UUID cdUuid = ct.getUUID("uuid");
                Map<String, Long> m = new HashMap<>();
                if (ct.contains("entries")) {
                    ListTag cdEntries = ct.getList("entries", 10);
                    for (int j = 0; j < cdEntries.size(); j++) {
                        CompoundTag ced = cdEntries.getCompound(j);
                        m.put(ced.getString("key"), ced.getLong("until"));
                    }
                }
                if (!m.isEmpty()) data.cooldownMap.put(cdUuid, m);
            }
        }
        return data;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Removes one stack of the given characteristic from the player's component
     * if the current stack is exactly 1 (i.e. only the envisioned copy exists).
     * If the player has already consumed the real characteristic (stack ≥ 2),
     * the real copy is left untouched.
     */
    public static void removeCharacteristicFromPlayer(ServerPlayer player, String pathway, int sequence) {
        de.jakob.lotm.attachments.BeyonderComponent component =
                player.getData(ModAttachments.BEYONDER_COMPONENT);

        int current = component.getCharacteristicList().stream()
                .filter(c -> c.pathway().equals(pathway) && c.sequence() == sequence)
                .mapToInt(de.jakob.lotm.util.playerMap.Characteristic::stack)
                .findFirst().orElse(0);

        if (current > 0) {
            // Decrement by 1 — leaves any real consumed copies untouched
            component.setCharacteristic(current - 1, sequence, pathway);
            de.jakob.lotm.events.BeyonderDataTickHandler.invalidateCache(player);
            de.jakob.lotm.network.PacketHandler.syncBeyonderDataToPlayer(player);
        }
    }

    /**
     * Sends a sync of this player's envisioned slots to the client.
     * Import note: this is separate from the BeyonderData sync.
     */
    public static void sendSync(ServerPlayer player, EnvisionedCharacteristicsData data) {
        Slot[] slots = data.getSlots(player.getUUID());
        java.util.List<de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.SlotInfo> infos
                = new java.util.ArrayList<>();
        for (Slot s : slots) {
            if (s.isEmpty()) {
                infos.add(de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.SlotInfo.emptySlot());
            } else {
                infos.add(new de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.SlotInfo(
                        false, s.pathway, s.sequence, s.expiryMs));
            }
        }
        de.jakob.lotm.network.PacketHandler.sendToPlayer(player,
                new de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket(
                        infos, data.getCooldownsForSync(player.getUUID()), false, "", -1));
    }

    /**
     * Sends the TARGET's envisioned-slot state back to the OWNER's client (target-mode sync).
     */
    public static void sendTargetSync(ServerPlayer owner, ServerPlayer target, EnvisionedCharacteristicsData data) {
        Slot[] slots = data.getSlots(target.getUUID());
        java.util.List<de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.SlotInfo> infos
                = new java.util.ArrayList<>();
        for (Slot s : slots) {
            if (s.isEmpty()) {
                infos.add(de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.SlotInfo.emptySlot());
            } else {
                infos.add(new de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.SlotInfo(
                        false, s.pathway, s.sequence, s.expiryMs));
            }
        }
        String targetPath = de.jakob.lotm.util.BeyonderData.getPathway(target);
        int    targetSeq  = de.jakob.lotm.util.BeyonderData.getSequence(target);
        de.jakob.lotm.network.PacketHandler.sendToPlayer(owner,
                new de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket(
                        infos, data.getCooldownsForSync(target.getUUID()), true, targetPath, targetSeq));
    }

    private static String prettify(String pathway) {
        if (pathway == null || pathway.isEmpty()) return "Unknown";
        String[] words = pathway.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}

package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks unique existence of Blasphemy Cards, the two Blasphemy Slate halves,
 * and the assembled Blasphemy Slate. At most one of each can exist in the world.
 *
 * Locking rules:
 *  - If the Left Half exists, left-half pathway cards cannot exist.
 *  - If the Right Half exists, right-half pathway cards cannot exist.
 *  - If the Slate exists, neither half can exist (they were consumed).
 */
public class BlasphemySlateData extends SavedData {

    // Pathways whose cards make up the Left Half of the Blasphemy Slate.
    public static final Set<String> LEFT_HALF_PATHWAYS  = Set.of(
            "sun", "tyrant", "visionary", "justiciar", "twilight_giant", "death", "abyss", "wheel_of_fortune");

    // Pathways whose cards make up the Right Half of the Blasphemy Slate.
    public static final Set<String> RIGHT_HALF_PATHWAYS = Set.of(
            "red_priest", "demoness", "error", "door", "fool", "mother", "black_emperor", "darkness");

    private static final String DATA_NAME = "blasphemy_slate_data";

    // Cards: pathway -> UUID
    private final Map<String, UUID> cardIds      = new HashMap<>();
    private final Map<String, Long> cardLastSeen = new HashMap<>();

    // Left half
    private UUID leftHalfId       = null;
    private long leftHalfLastSeen = 0L;

    // Right half
    private UUID rightHalfId       = null;
    private long rightHalfLastSeen = 0L;

    // Full slate
    private UUID slateId       = null;
    private long slateLastSeen = 0L;

    // ─── Factory ────────────────────────────────────────────────────────────

    public static BlasphemySlateData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                BlasphemySlateData::new,
                BlasphemySlateData::load
        ), DATA_NAME);
    }

    // ─── Card helpers ────────────────────────────────────────────────────────

    public boolean canSpawnCard(String pathway) {
        if (cardIds.containsKey(pathway)) return false;
        if (slateId != null) return false;
        if (LEFT_HALF_PATHWAYS.contains(pathway)  && leftHalfId  != null) return false;
        if (RIGHT_HALF_PATHWAYS.contains(pathway) && rightHalfId != null) return false;
        return true;
    }

    public boolean hasCard(String pathway)          { return cardIds.containsKey(pathway); }
    public UUID getCardId(String pathway)            { return cardIds.get(pathway); }
    public long getCardLastSeen(String pathway)      { return cardLastSeen.getOrDefault(pathway, 0L); }
    public boolean isCardOwner(String pathway, UUID id) { return id != null && id.equals(cardIds.get(pathway)); }

    public void markCardExists(String pathway, UUID id, long tick) {
        if (pathway == null || id == null) return;
        cardIds.put(pathway, id);
        cardLastSeen.put(pathway, tick);
        setDirty();
    }

    public void markCardSeen(String pathway, UUID id, long tick) {
        if (!isCardOwner(pathway, id)) return;
        cardLastSeen.put(pathway, tick);
        setDirty();
    }

    public void clearCard(String pathway) {
        cardIds.remove(pathway);
        cardLastSeen.remove(pathway);
        setDirty();
    }

    // ─── Left half helpers ────────────────────────────────────────────────────

    public boolean canSpawnLeftHalf()  { return leftHalfId  == null && slateId == null; }
    public boolean hasLeftHalf()       { return leftHalfId  != null; }
    public UUID   getLeftHalfId()      { return leftHalfId; }
    public long   getLeftHalfLastSeen(){ return leftHalfLastSeen; }
    public boolean isLeftHalfOwner(UUID id) { return id != null && id.equals(leftHalfId); }

    public void markLeftHalfExists(UUID id, long tick) {
        if (id == null) return;
        leftHalfId = id;
        leftHalfLastSeen = tick;
        setDirty();
    }

    public void markLeftHalfSeen(UUID id, long tick) {
        if (!isLeftHalfOwner(id)) return;
        leftHalfLastSeen = tick;
        setDirty();
    }

    public void clearLeftHalf() {
        leftHalfId = null;
        leftHalfLastSeen = 0L;
        setDirty();
    }

    // ─── Right half helpers ───────────────────────────────────────────────────

    public boolean canSpawnRightHalf()  { return rightHalfId  == null && slateId == null; }
    public boolean hasRightHalf()       { return rightHalfId  != null; }
    public UUID   getRightHalfId()      { return rightHalfId; }
    public long   getRightHalfLastSeen(){ return rightHalfLastSeen; }
    public boolean isRightHalfOwner(UUID id) { return id != null && id.equals(rightHalfId); }

    public void markRightHalfExists(UUID id, long tick) {
        if (id == null) return;
        rightHalfId = id;
        rightHalfLastSeen = tick;
        setDirty();
    }

    public void markRightHalfSeen(UUID id, long tick) {
        if (!isRightHalfOwner(id)) return;
        rightHalfLastSeen = tick;
        setDirty();
    }

    public void clearRightHalf() {
        rightHalfId = null;
        rightHalfLastSeen = 0L;
        setDirty();
    }

    // ─── Slate helpers ────────────────────────────────────────────────────────

    public boolean canSpawnSlate()  { return slateId == null; }
    public boolean hasSlate()       { return slateId != null; }
    public UUID   getSlateId()      { return slateId; }
    public long   getSlateLastSeen(){ return slateLastSeen; }
    public boolean isSlateOwner(UUID id) { return id != null && id.equals(slateId); }

    public void markSlateExists(UUID id, long tick) {
        if (id == null) return;
        slateId = id;
        slateLastSeen = tick;
        setDirty();
    }

    public void markSlateSeen(UUID id, long tick) {
        if (!isSlateOwner(id)) return;
        slateLastSeen = tick;
        setDirty();
    }

    public void clearSlate() {
        slateId = null;
        slateLastSeen = 0L;
        setDirty();
    }

    // ─── NBT serialization ────────────────────────────────────────────────────

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag cards = new CompoundTag();
        for (Map.Entry<String, UUID> entry : cardIds.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putUUID("id", entry.getValue());
            c.putLong("seen", cardLastSeen.getOrDefault(entry.getKey(), 0L));
            cards.put(entry.getKey(), c);
        }
        tag.put("cards", cards);

        if (leftHalfId != null) {
            tag.putUUID("leftHalfId", leftHalfId);
            tag.putLong("leftHalfSeen", leftHalfLastSeen);
        }
        if (rightHalfId != null) {
            tag.putUUID("rightHalfId", rightHalfId);
            tag.putLong("rightHalfSeen", rightHalfLastSeen);
        }
        if (slateId != null) {
            tag.putUUID("slateId", slateId);
            tag.putLong("slateSeen", slateLastSeen);
        }
        return tag;
    }

    public static BlasphemySlateData load(CompoundTag tag, HolderLookup.Provider registries) {
        BlasphemySlateData data = new BlasphemySlateData();
        if (tag.contains("cards")) {
            CompoundTag cards = tag.getCompound("cards");
            for (String key : cards.getAllKeys()) {
                CompoundTag c = cards.getCompound(key);
                if (c.hasUUID("id")) {
                    data.cardIds.put(key, c.getUUID("id"));
                    data.cardLastSeen.put(key, c.getLong("seen"));
                }
            }
        }
        if (tag.hasUUID("leftHalfId")) {
            data.leftHalfId = tag.getUUID("leftHalfId");
            data.leftHalfLastSeen = tag.getLong("leftHalfSeen");
        }
        if (tag.hasUUID("rightHalfId")) {
            data.rightHalfId = tag.getUUID("rightHalfId");
            data.rightHalfLastSeen = tag.getLong("rightHalfSeen");
        }
        if (tag.hasUUID("slateId")) {
            data.slateId = tag.getUUID("slateId");
            data.slateLastSeen = tag.getLong("slateSeen");
        }
        return data;
    }
}

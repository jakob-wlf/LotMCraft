package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

/**
 * Tracks blasphemy cards "summoned" via the Envisioning > Self > Blasphemy ability.
 * Rules:
 *  - At most {@link #MAX_CARDS} cards active per player at once.
 *  - Each card starts with {@link #MAX_USES} uses.
 *  - Uses are decremented externally (by the card-use handler).
 *  - When uses reach 0 the card is automatically dismissed.
 */
public class SummonedBlasphemyData extends SavedData {

    public static final int MAX_CARDS = 3;
    public static final int MAX_USES  = 5;

    private static final String DATA_NAME = "summoned_blasphemy_data";

    // playerUUID -> (pathway -> usesRemaining), insertion-ordered
    private final Map<UUID, LinkedHashMap<String, Integer>> playerCards = new HashMap<>();
    /** playerUUID -> set of pathways that are locked (cannot be dismissed via GUI) */
    private final Map<UUID, Set<String>> lockedCards = new HashMap<>();
    /** playerUUID -> (pathway -> expiry epoch ms): pathway cannot be summoned again until that time. */
    private final Map<UUID, Map<String, Long>> cardCooldowns = new HashMap<>();

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static SummonedBlasphemyData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                SummonedBlasphemyData::new,
                SummonedBlasphemyData::load
        ), DATA_NAME);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    /** Returns an unmodifiable snapshot of this player's active cards. */
    public Map<String, Integer> getCards(UUID player) {
        LinkedHashMap<String, Integer> map = playerCards.get(player);
        return map == null ? Collections.emptyMap() : Collections.unmodifiableMap(map);
    }

    public boolean hasSummoned(UUID player, String pathway) {
        LinkedHashMap<String, Integer> map = playerCards.get(player);
        return map != null && map.containsKey(pathway);
    }

    public int activeCount(UUID player) {
        LinkedHashMap<String, Integer> map = playerCards.get(player);
        return map == null ? 0 : map.size();
    }

    /** Returns the number of slots occupied by either a summoned card or an active cooldown. */
    public int occupiedSlots(UUID player) {
        int summoned = activeCount(player);
        Map<String, Long> cds = cardCooldowns.get(player);
        if (cds == null) return summoned;
        long now = System.currentTimeMillis();
        // Count cooldown entries that haven't expired and aren't already in playerCards
        LinkedHashMap<String, Integer> active = playerCards.get(player);
        long cooldownCount = cds.entrySet().stream()
                .filter(e -> e.getValue() > now)
                .filter(e -> active == null || !active.containsKey(e.getKey()))
                .count();
        return summoned + (int) cooldownCount;
    }

    // ─── Mutations ────────────────────────────────────────────────────────────

    /**
     * Summon a card for the given player.
     * @return {@code true} if successful, {@code false} if limit reached, already summoned, or on cooldown.
     */
    public boolean summon(UUID player, String pathway) {
        if (isOnCooldown(player, pathway)) return false;
        LinkedHashMap<String, Integer> cards = playerCards.computeIfAbsent(player, k -> new LinkedHashMap<>());
        if (cards.containsKey(pathway)) return false;
        if (cards.size() >= MAX_CARDS)  return false;
        cards.put(pathway, MAX_USES);
        setDirty();
        return true;
    }

    /**
     * Dismiss a summoned card voluntarily.
     * Returns {@code false} if the card is locked — locked cards can only be removed
     * when their uses run out or the physical item is destroyed.
     */
    public boolean dismiss(UUID player, String pathway) {
        if (isLocked(player, pathway)) return false;
        return dismissInternal(player, pathway);
    }

    /**
     * Force-remove a summoned card regardless of lock status.
     * Use this when the physical item has been confirmed destroyed or gone from the world.
     */
    public boolean forceDismiss(UUID player, String pathway) {
        Set<String> locked = lockedCards.get(player);
        if (locked != null) {
            locked.remove(pathway);
            if (locked.isEmpty()) lockedCards.remove(player);
        }
        return dismissInternal(player, pathway);
    }

    private boolean dismissInternal(UUID player, String pathway) {
        LinkedHashMap<String, Integer> cards = playerCards.get(player);
        if (cards == null || !cards.containsKey(pathway)) return false;
        cards.remove(pathway);
        if (cards.isEmpty()) playerCards.remove(player);
        setDirty();
        return true;
    }

    /**
     * Decrement the use count for an active card.
     * Automatically dismisses the card (and clears its lock) when uses reach 0.
     * @return remaining uses after decrement (-1 if card not found).
     */
    public int consumeUse(UUID player, String pathway) {
        LinkedHashMap<String, Integer> cards = playerCards.get(player);
        if (cards == null || !cards.containsKey(pathway)) return -1;
        int remaining = cards.get(pathway) - 1;
        if (remaining <= 0) {
            cards.remove(pathway);
            if (cards.isEmpty()) playerCards.remove(player);
            // Also clear the lock when all uses are consumed
            Set<String> locked = lockedCards.get(player);
            if (locked != null) {
                locked.remove(pathway);
                if (locked.isEmpty()) lockedCards.remove(player);
            }
        } else {
            cards.put(pathway, remaining);
        }
        setDirty();
        return Math.max(0, remaining);
    }

    // ─── Locking ──────────────────────────────────────────────────────────────

    /**
     * Lock all currently summoned cards for this player.
     * Called when the Blasphemy GUI is closed — locked cards cannot be dismissed
     * via the GUI and must be used up or have their physical item destroyed.
     */
    public void lockAll(UUID player) {
        LinkedHashMap<String, Integer> cards = playerCards.get(player);
        if (cards == null || cards.isEmpty()) return;
        lockedCards.computeIfAbsent(player, k -> new HashSet<>()).addAll(cards.keySet());
        setDirty();
    }

    public boolean isLocked(UUID player, String pathway) {
        Set<String> locked = lockedCards.get(player);
        return locked != null && locked.contains(pathway);
    }

    public Set<String> getLockedCards(UUID player) {
        Set<String> locked = lockedCards.get(player);
        return locked == null ? Collections.emptySet() : Collections.unmodifiableSet(locked);
    }

    // ─── Slot cooldowns ───────────────────────────────────────────────────────

    public static final long DESTROYED_COOLDOWN_MS = 5L * 60 * 60 * 1000; // 5 hours

    /** Put a pathway on cooldown for this player for the given duration. */
    public void putOnCooldown(UUID player, String pathway, long durationMs) {
        cardCooldowns.computeIfAbsent(player, k -> new HashMap<>())
                .put(pathway, System.currentTimeMillis() + durationMs);
        setDirty();
    }

    public boolean isOnCooldown(UUID player, String pathway) {
        Map<String, Long> map = cardCooldowns.get(player);
        if (map == null) return false;
        Long expiry = map.get(pathway);
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    /** Returns remaining cooldown in ms, or 0 if not on cooldown. */
    public long getCooldownRemainingMs(UUID player, String pathway) {
        Map<String, Long> map = cardCooldowns.get(player);
        if (map == null) return 0L;
        Long expiry = map.get(pathway);
        if (expiry == null) return 0L;
        return Math.max(0L, expiry - System.currentTimeMillis());
    }

    /**
     * Returns a snapshot of pathway → expiry epoch ms for this player.
     * Only includes entries that are still active (expiry in the future).
     */
    public Map<String, Long> getCooldownExpiryMap(UUID player) {
        Map<String, Long> map = cardCooldowns.get(player);
        if (map == null) return Collections.emptyMap();
        long now = System.currentTimeMillis();
        Map<String, Long> result = new HashMap<>();
        map.forEach((pathway, expiry) -> { if (expiry > now) result.put(pathway, expiry); });
        return result;
    }

    // ─── NBT ─────────────────────────────────────────────────────────────────

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, LinkedHashMap<String, Integer>> entry : playerCards.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            for (Map.Entry<String, Integer> cardEntry : entry.getValue().entrySet()) {
                playerTag.putInt(cardEntry.getKey(), cardEntry.getValue());
            }
            playersTag.put(entry.getKey().toString(), playerTag);
        }
        tag.put("players", playersTag);
        // Save locked cards
        CompoundTag lockedTag = new CompoundTag();
        for (Map.Entry<UUID, Set<String>> entry : lockedCards.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            ListTag list = new ListTag();
            for (String p : entry.getValue()) list.add(StringTag.valueOf(p));
            lockedTag.put(entry.getKey().toString(), list);
        }
        tag.put("locked", lockedTag);
        // Save cooldowns
        CompoundTag cooldownsTag = new CompoundTag();
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Map<String, Long>> entry : cardCooldowns.entrySet()) {
            CompoundTag playerCdTag = new CompoundTag();
            entry.getValue().forEach((pathway, expiry) -> {
                if (expiry > now) playerCdTag.putLong(pathway, expiry);
            });
            if (!playerCdTag.isEmpty()) cooldownsTag.put(entry.getKey().toString(), playerCdTag);
        }
        tag.put("cooldowns", cooldownsTag);
        return tag;
    }

    public static SummonedBlasphemyData load(CompoundTag tag, HolderLookup.Provider registries) {
        SummonedBlasphemyData data = new SummonedBlasphemyData();
        if (!tag.contains("players")) return data;
        CompoundTag playersTag = tag.getCompound("players");
        for (String uuidStr : playersTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag playerTag = playersTag.getCompound(uuidStr);
                LinkedHashMap<String, Integer> cards = new LinkedHashMap<>();
                for (String pathway : playerTag.getAllKeys()) {
                    int uses = playerTag.getInt(pathway);
                    if (uses > 0) cards.put(pathway, uses);
                }
                if (!cards.isEmpty()) data.playerCards.put(uuid, cards);
            } catch (IllegalArgumentException ignored) { /* bad UUID, skip */ }
        }
        // Load locked cards
        if (tag.contains("locked")) {
            CompoundTag lockedTag = tag.getCompound("locked");
            for (String uuidStr : lockedTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ListTag list = lockedTag.getList(uuidStr, 8); // TAG_String = 8
                    Set<String> locked = new HashSet<>();
                    for (int i = 0; i < list.size(); i++) locked.add(list.getString(i));
                    // Only keep locks for cards that are actually still summoned
                    LinkedHashMap<String, Integer> active = data.playerCards.get(uuid);
                    if (active != null) locked.retainAll(active.keySet());
                    if (!locked.isEmpty()) data.lockedCards.put(uuid, locked);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        // Load cooldowns
        if (tag.contains("cooldowns")) {
            long now = System.currentTimeMillis();
            CompoundTag cooldownsTag = tag.getCompound("cooldowns");
            for (String uuidStr : cooldownsTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    CompoundTag playerCdTag = cooldownsTag.getCompound(uuidStr);
                    Map<String, Long> cdMap = new HashMap<>();
                    for (String pathway : playerCdTag.getAllKeys()) {
                        long expiry = playerCdTag.getLong(pathway);
                        if (expiry > now) cdMap.put(pathway, expiry);
                    }
                    if (!cdMap.isEmpty()) data.cardCooldowns.put(uuid, cdMap);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return data;
    }
}

package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.UUID;

/**
 * Tracks the single global Fool Card created via the Grey Fog ability.
 * Only one can exist in the world at a time.
 * If destroyed, the ability to create a new one is locked for 48 hours.
 */
public class FoolCardData extends SavedData {

    /** Real-time cooldown duration: 48 hours in milliseconds. */
    public static final long COOLDOWN_MS = 48L * 60L * 60L * 1_000L;

    /** Game-ticks before the card is considered gone if not seen anywhere. */
    public static final long UNSEEN_TICKS = 40L;

    private static final String DATA_NAME = "fool_card_data";

    private UUID cardId          = null;
    private long cardLastSeenTick = 0L;
    private long cooldownExpiryMs = 0L;   // System.currentTimeMillis() epoch ms

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static FoolCardData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(FoolCardData::new, FoolCardData::load), DATA_NAME);
    }

    // ─── Queries ─────────────────────────────────────────────────────────────

    public boolean hasCard()     { return cardId != null; }
    public UUID   getCardId()    { return cardId; }
    public long   getLastSeen()  { return cardLastSeenTick; }

    public boolean isOnCooldown() {
        return System.currentTimeMillis() < cooldownExpiryMs;
    }

    public long getCooldownRemainingMs() {
        return Math.max(0, cooldownExpiryMs - System.currentTimeMillis());
    }

    /** True when a new card may be created: no card exists and no cooldown. */
    public boolean canCreate() {
        return !hasCard() && !isOnCooldown();
    }

    // ─── Mutations ───────────────────────────────────────────────────────────

    public void registerCard(UUID id, long tick) {
        this.cardId           = id;
        this.cardLastSeenTick = tick;
        setDirty();
    }

    public void markSeen(UUID id, long tick) {
        if (!id.equals(cardId)) return;
        this.cardLastSeenTick = tick;
        setDirty();
    }

    /** Call when the card has been confirmed destroyed — clears it and starts the 48 h cooldown. */
    public void markDestroyed() {
        this.cardId           = null;
        this.cardLastSeenTick = 0L;
        this.cooldownExpiryMs = System.currentTimeMillis() + COOLDOWN_MS;
        setDirty();
    }

    // ─── NBT ─────────────────────────────────────────────────────────────────

    public static FoolCardData load(CompoundTag tag, HolderLookup.Provider registries) {
        FoolCardData data = new FoolCardData();
        if (tag.hasUUID("cardId")) {
            data.cardId           = tag.getUUID("cardId");
            data.cardLastSeenTick = tag.getLong("cardLastSeen");
        }
        data.cooldownExpiryMs = tag.getLong("cooldownExpiryMs");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        if (cardId != null) {
            tag.putUUID("cardId", cardId);
            tag.putLong("cardLastSeen", cardLastSeenTick);
        }
        tag.putLong("cooldownExpiryMs", cooldownExpiryMs);
        return tag;
    }
}

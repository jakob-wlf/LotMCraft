package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores per-Chaos-Sea-authority LEODERO trigger words set via the Envisioning > Target > Blasphemy screen.
 *
 * When ANY player says the registered word in chat, the LEODERO effect fires on THEM (the speaker).
 * Triggers are persistent until the owner explicitly changes or clears them.
 *
 * Map structure: casterUUID → triggerWord
 */
public class EnvisionBlasphemyTriggerData extends SavedData {

    private static final String DATA_NAME = "envision_blasphemy_trigger_data";

    public static final long COOLDOWN_MS = 60_000L; // 60 seconds

    // casterUUID → triggerWord (lower-cased)
    private final Map<UUID, String> triggers = new HashMap<>();
    // casterUUID → last fire timestamp (System.currentTimeMillis())
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public static EnvisionBlasphemyTriggerData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                EnvisionBlasphemyTriggerData::new,
                EnvisionBlasphemyTriggerData::load
        ), DATA_NAME);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    /** Set a trigger word for this caster. Overwrites any existing trigger. Persists indefinitely. */
    public void setTrigger(UUID caster, String word) {
        triggers.put(caster, word.trim().toLowerCase());
        setDirty();
    }

    /** Remove the trigger set by this caster. */
    public void clearTrigger(UUID caster) {
        if (triggers.remove(caster) != null) setDirty();
    }

    /**
     * Check whether {@code message} matches this caster's registered trigger word.
     * Returns {@code true} if it matches. The trigger is NOT removed — it fires every time.
     */
    public boolean check(UUID caster, String message) {
        String word = triggers.get(caster);
        if (word == null) return false;
        return matchesTrigger(message, word);
    }

    /** Iterate all registered trigger words and check {@code message} against each. */
    public java.util.List<UUID> findMatches(String message) {
        java.util.List<UUID> matched = new java.util.ArrayList<>();
        for (Map.Entry<UUID, String> e : triggers.entrySet()) {
            if (matchesTrigger(message, e.getValue())) matched.add(e.getKey());
        }
        return matched;
    }

    private static boolean matchesTrigger(String message, String trigger) {
        if (message == null || trigger == null) return false;
        String normalizedMessage = message.trim();
        String normalizedTrigger = trigger.trim();
        if (normalizedMessage.equalsIgnoreCase(normalizedTrigger)) return true;
        if (normalizedTrigger.isEmpty()) return false;
        return Pattern.compile("(?iu)(^|[^\\p{L}\\p{N}_])" + Pattern.quote(normalizedTrigger) + "($|[^\\p{L}\\p{N}_])")
                .matcher(normalizedMessage)
                .find();
    }

    public boolean hasTrigger(UUID caster) {
        return triggers.containsKey(caster);
    }

    public String getTriggerWord(UUID caster) {
        return triggers.get(caster);
    }

    // ── Cooldown ──────────────────────────────────────────────────────────────

    /** Returns true if the caster's cooldown has expired (or was never set). */
    public boolean canFire(UUID caster) {
        Long last = cooldowns.get(caster);
        if (last == null) return true;
        return System.currentTimeMillis() - last >= COOLDOWN_MS;
    }

    /** Records the current time as the last fire time for this caster. */
    public void recordFire(UUID caster) {
        cooldowns.put(caster, System.currentTimeMillis());
        setDirty();
    }

    /** Returns remaining cooldown in whole seconds, or 0 if ready. */
    public long getCooldownRemainingSeconds(UUID caster) {
        Long last = cooldowns.get(caster);
        if (last == null) return 0L;
        long remaining = COOLDOWN_MS - (System.currentTimeMillis() - last);
        return Math.max(0L, remaining) / 1000L;
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag entries = new CompoundTag();
        for (Map.Entry<UUID, String> e : triggers.entrySet()) {
            entries.putString(e.getKey().toString(), e.getValue());
        }
        tag.put("triggers", entries);

        CompoundTag cds = new CompoundTag();
        for (Map.Entry<UUID, Long> e : cooldowns.entrySet()) {
            cds.putLong(e.getKey().toString(), e.getValue());
        }
        tag.put("cooldowns", cds);
        return tag;
    }

    public static EnvisionBlasphemyTriggerData load(CompoundTag tag, HolderLookup.Provider registries) {
        EnvisionBlasphemyTriggerData data = new EnvisionBlasphemyTriggerData();
        if (tag.contains("triggers")) {
            CompoundTag entries = tag.getCompound("triggers");
            for (String key : entries.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    data.triggers.put(uuid, entries.getString(key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        if (tag.contains("cooldowns")) {
            CompoundTag cds = tag.getCompound("cooldowns");
            for (String key : cds.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    data.cooldowns.put(uuid, cds.getLong(key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return data;
    }
}

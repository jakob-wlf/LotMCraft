package de.jakob.lotm.beyonders.abilities.abyss.MaliceSeed;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public final class MaliceSeedData extends SavedData {
    private static final String DATA_KEY = "lotm_malice_seed_saved_data";
    private static final String IDENTITIES_KEY = "identities";
    private final CompoundTag identities = new CompoundTag();
    public MaliceSeedData() {
    }
    public static MaliceSeedData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        MaliceSeedData data = new MaliceSeedData();
        if (!tag.contains(IDENTITIES_KEY, CompoundTag.TAG_COMPOUND)) return data;
        CompoundTag savedIdentities = tag.getCompound(IDENTITIES_KEY);
        for (String identityKey : new ArrayList<>(savedIdentities.getAllKeys())) {
            try {
                UUID.fromString(identityKey);
                if (!savedIdentities.contains(identityKey, CompoundTag.TAG_COMPOUND)) continue;
                CompoundTag seeds = savedIdentities.getCompound(identityKey);
                CompoundTag validSeeds = new CompoundTag();
                for (String casterKey : new ArrayList<>(seeds.getAllKeys())) {
                    try {
                        UUID.fromString(casterKey);
                        if (!seeds.contains(casterKey, CompoundTag.TAG_COMPOUND)) continue;
                        CompoundTag seed = seeds.getCompound(casterKey);
                        int phase = seed.getInt("phase");
                        int progress = seed.getInt("progress");
                        int casterSeq = seed.getInt("casterSeq");
                        if (phase < 1 || phase > 4 || progress < 0 || casterSeq < 0 || casterSeq > 10) continue;
                        CompoundTag validSeed = seed.copy();
                        validSeeds.put(casterKey, validSeed);
                    } catch (IllegalArgumentException ignored) {
                        // Ignore malformed caster UUIDs.
                    }
                }
                if (!validSeeds.isEmpty()) data.identities.put(identityKey, validSeeds);
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed identity UUIDs.
            }
        }
        return data;
    }
    public static MaliceSeedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(MaliceSeedData::new, MaliceSeedData::load), DATA_KEY);
    }
    public CompoundTag getSeeds(UUID identity) {
        if (identity == null) return new CompoundTag();
        return identities.getCompound(identity.toString()).copy();
    }
    public void setSeeds(UUID identity, CompoundTag seeds) {
        if (identity == null) return;
        String key = identity.toString();
        if (seeds == null || seeds.isEmpty()) {
            if (identities.contains(key)) {
                identities.remove(key);
                setDirty();
            }
            return;
        }
        CompoundTag copy = seeds.copy();
        if (identities.contains(key) && identities.getCompound(key).equals(copy)) return;
        identities.put(key, copy);
        setDirty();
    }
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.put(IDENTITIES_KEY, identities.copy());
        return tag;
    }
}
package de.jakob.lotm.beyonders.advancementrituals;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;

/** Persistent per-world overrides for advancement ritual participant requirements. */
public final class RitualRequirementData extends SavedData {
    private static final String DATA_NAME = "ritual_required_amounts";
    private static final Map<String, Integer> DEFAULT_AMOUNTS = Map.of(
            key("visionary", 0), 10,
            key("visionary", 1), 20,
            key("visionary", 2), 10,
            key("visionary", 3), 10,
            key("visionary", 4), 1
    );

    private final Map<String, Integer> requiredAmounts = new HashMap<>();

    public static RitualRequirementData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                RitualRequirementData::new,
                RitualRequirementData::load
        ), DATA_NAME);
    }

    public int getRequiredAmount(String pathway, int sequence) {
        String key = key(pathway, sequence);
        return requiredAmounts.getOrDefault(key, DEFAULT_AMOUNTS.getOrDefault(key, 1));
    }

    public void setRequiredAmount(String pathway, int sequence, int amount) {
        requiredAmounts.put(key(pathway, sequence), amount);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        for (Map.Entry<String, Integer> entry : requiredAmounts.entrySet()) {
            tag.putInt(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    public static RitualRequirementData load(CompoundTag tag, HolderLookup.Provider provider) {
        RitualRequirementData data = new RitualRequirementData();
        for (String key : tag.getAllKeys()) {
            int amount = tag.getInt(key);
            if (amount > 0) {
                data.requiredAmounts.put(key, amount);
            }
        }
        return data;
    }

    private static String key(String pathway, int sequence) {
        return pathway + "." + sequence;
    }
}
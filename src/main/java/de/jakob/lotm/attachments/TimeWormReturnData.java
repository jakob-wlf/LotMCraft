package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeWormReturnData extends SavedData {
    private static final String DATA_NAME = "time_worm_returns";
    private final Map<UUID, Integer> pendingReturns = new HashMap<>();

    public static TimeWormReturnData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(TimeWormReturnData::new, TimeWormReturnData::load), DATA_NAME);
    }

    public void add(UUID owner, int amount) {
        if (amount <= 0) return;
        pendingReturns.merge(owner, amount, Integer::sum);
        setDirty();
    }

    public int claim(UUID owner) {
        int amount = pendingReturns.getOrDefault(owner, 0);
        if (amount > 0) {
            pendingReturns.remove(owner);
            setDirty();
        }
        return amount;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        pendingReturns.forEach((owner, amount) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("owner", owner);
            entry.putInt("amount", amount);
            entries.add(entry);
        });
        tag.put("pendingReturns", entries);
        return tag;
    }

    public static TimeWormReturnData load(CompoundTag tag, HolderLookup.Provider registries) {
        TimeWormReturnData data = new TimeWormReturnData();
        ListTag entries = tag.getList("pendingReturns", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (entry.hasUUID("owner")) {
                int amount = entry.getInt("amount");
                if (amount > 0) data.pendingReturns.put(entry.getUUID("owner"), amount);
            }
        }
        return data;
    }
}

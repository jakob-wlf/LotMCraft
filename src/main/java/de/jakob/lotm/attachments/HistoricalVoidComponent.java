package de.jakob.lotm.attachments;

import de.jakob.lotm.beyonders.abilities.fool.HistoricalVoidSummoningAbility;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HistoricalVoidComponent implements INBTSerializable<CompoundTag> {
    public int summonedCount = 0;
    public int historicalBorrowingCount = 0;
    public boolean hasSummonedSelf = false;
    public long summonedSelfMillis = 0;
    public final Map<Long, SummonInfo> activeSummonTimes = new ConcurrentHashMap<>();
    public final List<SavedEffect> savedEffects = new ArrayList<>();

    public record SummonInfo(
            long summonTime,
            HistoricalVoidSummoningAbility.SummonType type,
            UUID entityUUID,
            CompoundTag originalBeforeBorrowing
    ) {}

    public record SavedEffect(
            ResourceLocation effectId,
            int amplifier,
            int duration
    ) {}

    public List<SavedEffect> getSavedEffects() {
        return this.savedEffects;
    }

    public void addSavedEffect(ResourceLocation effectId, int amplifier, int duration) {
        this.savedEffects.add(new SavedEffect(effectId, amplifier, duration));
    }

    public void reset() {
        this.summonedCount = 0;
        this.historicalBorrowingCount = 0;
        this.activeSummonTimes.clear();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("SummonedCount", summonedCount);
        tag.putInt("HistoricalBorrowingCount", historicalBorrowingCount);

        ListTag list = new ListTag();
        activeSummonTimes.forEach((time, info) -> {
            CompoundTag anotherTag = new CompoundTag();
            anotherTag.putLong("Time", info.summonTime());
            anotherTag.putString("Type", info.type().name());
            if (info.entityUUID() != null) anotherTag.putUUID("EntityUUID", info.entityUUID());
            anotherTag.put("OriginalTag", info.originalBeforeBorrowing());
            list.add(anotherTag);
        });
        tag.put("ActiveSummons", list);

        ListTag effectsList = new ListTag();
        for (SavedEffect effect : savedEffects) {
            CompoundTag effectTag = new CompoundTag();
            effectTag.putString("Id", effect.effectId().toString());
            effectTag.putInt("Amplifier", effect.amplifier());
            effectTag.putInt("Duration", effect.duration());
            effectsList.add(effectTag);
        }
        tag.put("SavedEffects", effectsList);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.summonedCount = tag.getInt("SummonedCount");
        this.historicalBorrowingCount = tag.getInt("HistoricalBorrowingCount");
        this.activeSummonTimes.clear();

        ListTag list = tag.getList("ActiveSummons", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag anotherTag = list.getCompound(i);
            long time = anotherTag.getLong("Time");
            SummonInfo info = new SummonInfo(
                    time,
                    HistoricalVoidSummoningAbility.SummonType.valueOf(anotherTag.getString("Type")),
                    anotherTag.hasUUID("EntityUUID") ? anotherTag.getUUID("EntityUUID") : null,
                    anotherTag.getCompound("OriginalTag")
            );
            this.activeSummonTimes.put(time, info);
        }

        this.savedEffects.clear();
        ListTag effectsList = tag.getList("SavedEffects", 10);
        for (int i = 0; i < effectsList.size(); i++) {
            CompoundTag effectTag = effectsList.getCompound(i);
            this.savedEffects.add(new SavedEffect(
                    ResourceLocation.parse(effectTag.getString("Id")),
                    effectTag.getInt("Amplifier"),
                    effectTag.getInt("Duration")
            ));
        }
    }
}
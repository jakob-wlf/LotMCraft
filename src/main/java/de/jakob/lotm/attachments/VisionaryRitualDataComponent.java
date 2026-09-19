package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Persists Visionary dream bonds and Sequence 2 ritual manipulation progress across restarts. */
public class VisionaryRitualDataComponent implements INBTSerializable<CompoundTag> {

    private final Set<UUID> dreamBonds = new HashSet<>();
    private final Set<UUID> manipulatedRitualTargets = new HashSet<>();
    private final Set<UUID> storyWritingRitualTargets = new HashSet<>();
    private final Set<UUID> predictedSequenceTargets = new HashSet<>();

    public Set<UUID> getDreamBonds() {
        return dreamBonds;
    }

    public Set<UUID> getManipulatedRitualTargets() {
        return manipulatedRitualTargets;
    }

    public Set<UUID> getStoryWritingRitualTargets() {
        return storyWritingRitualTargets;
    }

    public Set<UUID> getPredictedSequenceTargets() {
        return predictedSequenceTargets;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("dreamBonds", uuidsToList(dreamBonds));
        tag.put("manipulatedRitualTargets", uuidsToList(manipulatedRitualTargets));
        tag.put("storyWritingRitualTargets", uuidsToList(storyWritingRitualTargets));
        tag.put("predictedSequenceTargets", uuidsToList(predictedSequenceTargets));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        dreamBonds.clear();
        dreamBonds.addAll(uuidsFromList(tag.getList("dreamBonds", 10)));
        manipulatedRitualTargets.clear();
        manipulatedRitualTargets.addAll(uuidsFromList(tag.getList("manipulatedRitualTargets", 10)));
        storyWritingRitualTargets.clear();
        storyWritingRitualTargets.addAll(uuidsFromList(tag.getList("storyWritingRitualTargets", 10)));
        predictedSequenceTargets.clear();
        predictedSequenceTargets.addAll(uuidsFromList(tag.getList("predictedSequenceTargets", 10)));
    }

    private static ListTag uuidsToList(Set<UUID> uuids) {
        ListTag list = new ListTag();
        for (UUID uuid : uuids) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            list.add(entry);
        }
        return list;
    }

    private static Set<UUID> uuidsFromList(ListTag list) {
        Set<UUID> uuids = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            uuids.add(list.getCompound(i).getUUID("uuid"));
        }
        return uuids;
    }
}

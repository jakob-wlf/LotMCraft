package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class ReceivedBlessingComponent implements INBTSerializable<CompoundTag> {
    public record ReceivedBlessing(String blessingId, String pathway, int sequence, int ticksLeft) {}

    private final List<ReceivedBlessing> blessings = new ArrayList<>();

    public void addBlessing(String blessingId, String pathway, int sequence, int duration) {
        blessings.add(new ReceivedBlessing(blessingId, pathway, sequence, duration));
    }

    public List<ReceivedBlessing> getBlessings() {
        return blessings;
    }

    public void tick() {
        blessings.replaceAll(b -> new ReceivedBlessing(b.blessingId(), b.pathway(), b.sequence(), b.ticksLeft() - 1));
        blessings.removeIf(b -> b.ticksLeft() <= 0);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ReceivedBlessing b : blessings) {
            CompoundTag bTag = new CompoundTag();
            bTag.putString("blessingId", b.blessingId());
            bTag.putString("pathway", b.pathway());
            bTag.putInt("sequence", b.sequence());
            bTag.putInt("ticksLeft", b.ticksLeft());
            list.add(bTag);
        }
        tag.put("blessings", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        blessings.clear();
        if (tag.contains("blessings", Tag.TAG_LIST)) {
            ListTag list = tag.getList("blessings", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag bTag = list.getCompound(i);
                blessings.add(new ReceivedBlessing(
                        bTag.getString("blessingId"),
                        bTag.getString("pathway"),
                        bTag.getInt("sequence"),
                        bTag.getInt("ticksLeft")
                ));
            }
        }
    }
}

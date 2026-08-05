package de.jakob.lotm.attachments;

import de.jakob.lotm.util.DisabledCharacteristic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class ActiveBlessingComponent implements INBTSerializable<CompoundTag> {
    private final List<DisabledCharacteristic> disabledCharacteristics = new ArrayList<>();

    public void addDisabledCharacteristic(String pathway, int sequence, int duration) {
        disabledCharacteristics.add(new DisabledCharacteristic(pathway, sequence, duration));
    }

    public List<DisabledCharacteristic> getDisabledCharacteristics() {
        return disabledCharacteristics;
    }

    public void tick() {
        disabledCharacteristics.replaceAll(dc -> new DisabledCharacteristic(dc.pathway(), dc.sequence(), dc.ticksLeft() - 20));
        disabledCharacteristics.removeIf(dc -> dc.ticksLeft() <= 0);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (DisabledCharacteristic dc : disabledCharacteristics) {
            CompoundTag dcTag = new CompoundTag();
            dcTag.putString("pathway", dc.pathway());
            dcTag.putInt("sequence", dc.sequence());
            dcTag.putInt("ticksLeft", dc.ticksLeft());
            list.add(dcTag);
        }
        tag.put("disabledCharacteristics", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        disabledCharacteristics.clear();
        if (tag.contains("disabledCharacteristics", Tag.TAG_LIST)) {
            ListTag list = tag.getList("disabledCharacteristics", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag dcTag = list.getCompound(i);
                disabledCharacteristics.add(new DisabledCharacteristic(
                        dcTag.getString("pathway"),
                        dcTag.getInt("sequence"),
                        dcTag.getInt("ticksLeft")
                ));
            }
        }
    }
}

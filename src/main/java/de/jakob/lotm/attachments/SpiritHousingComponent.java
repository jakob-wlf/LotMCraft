package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

public class SpiritHousingComponent implements INBTSerializable<CompoundTag> {
    public int fails;
    public boolean releaseArmed;
    public boolean releaseFromSlots;
    public final List<String> stored = new ArrayList<>();
    public final List<String> released = new ArrayList<>();
    public final String[] slots = new String[]{"", "", "", "", "", ""};

    public int count() {
        int amount = stored.size();
        for (String slot : slots) if (!slot.isEmpty()) amount++;
        return amount;
    }

    public List<String> powers() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            if (!slots[i].isEmpty()) list.add(slots[i] + "|" + i);
        }
        return list;
    }

    public List<String> takeForRelease(boolean fromSlots) {
        List<String> taken = new ArrayList<>();
        if (fromSlots) {
            for (int i = 0; i < slots.length; i++) {
                if (!slots[i].isEmpty()) taken.add(slots[i] + "|" + i);
                slots[i] = "";
            }
        }
        if (taken.isEmpty()) {
            for (String spirit : stored) taken.add(spirit + "|-1");
            stored.clear();
        }
        releaseArmed = false;
        releaseFromSlots = false;
        return taken;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("fails", fails);
        tag.putBoolean("armed", releaseArmed);
        tag.putBoolean("releaseSlots", releaseFromSlots);
        tag.putString("head", slots[0]);
        tag.putString("torso", slots[1]);
        tag.putString("armLeft", slots[2]);
        tag.putString("armRight", slots[3]);
        tag.putString("legLeft", slots[4]);
        tag.putString("legRight", slots[5]);
        ListTag list = new ListTag();
        for (String spirit : stored) list.add(StringTag.valueOf(spirit));
        tag.put("stored", list);
        ListTag out = new ListTag();
        for (String spirit : released) out.add(StringTag.valueOf(spirit));
        tag.put("released", out);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        fails = tag.getInt("fails");
        releaseArmed = tag.getBoolean("armed");
        releaseFromSlots = tag.getBoolean("releaseSlots");
        slots[0] = tag.getString("head");
        slots[1] = tag.getString("torso");
        if (tag.contains("armLeft")) {
            slots[2] = tag.getString("armLeft");
            slots[3] = tag.getString("armRight");
            slots[4] = tag.getString("legLeft");
            slots[5] = tag.getString("legRight");
        } else {
            slots[2] = tag.getString("arms");
            slots[4] = tag.getString("legs");
        }
        stored.clear();
        ListTag list = tag.getList("stored", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) stored.add(list.getString(i));
        released.clear();
        ListTag out = tag.getList("released", Tag.TAG_STRING);
        for (int i = 0; i < out.size(); i++) released.add(out.getString(i));
    }
}

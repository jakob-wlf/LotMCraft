package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;

/**
 * Persists which cross-path abilities this player has chosen to enable via the Sefirot Authority GUI.
 * These ability IDs appear in the Introspect available-abilities panel and can be assigned to any wheel or bar.
 */
public class SefirotUnlockedAbilitiesComponent implements INBTSerializable<CompoundTag> {

    private ArrayList<String> unlockedAbilities = new ArrayList<>();

    public ArrayList<String> getUnlockedAbilities() {
        return unlockedAbilities;
    }

    public boolean hasAbility(String id) {
        return unlockedAbilities.contains(id);
    }

    public void addAbility(String id) {
        if (!unlockedAbilities.contains(id)) {
            unlockedAbilities.add(id);
        }
    }

    public void removeAbility(String id) {
        unlockedAbilities.remove(id);
    }

    public void clear() {
        unlockedAbilities.clear();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (String ability : unlockedAbilities) {
            list.add(StringTag.valueOf(ability));
        }
        tag.put("UnlockedAbilities", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        unlockedAbilities.clear();
        if (tag.contains("UnlockedAbilities", Tag.TAG_LIST)) {
            ListTag list = tag.getList("UnlockedAbilities", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                unlockedAbilities.add(list.getString(i));
            }
        }
    }
}

package de.jakob.lotm.attachments;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AbilityWheelComponent implements INBTSerializable<CompoundTag> {
    private ArrayList<String> abilities = new ArrayList<>();
    private int selectedAbility = 0;

    public ArrayList<String> getAbilities() {
        return abilities;
    }

    public void setAbilities(ArrayList<String> abilities) {
        this.abilities = abilities;
    }

    public int getSelectedAbility() {
        return selectedAbility;
    }

    public void setSelectedAbility(int selectedAbility) {
        this.selectedAbility = selectedAbility;
    }



    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        ListTag list = new ListTag();
        for (String ability : abilities) {
            list.add(StringTag.valueOf(ability));
        }

        tag.put("Abilities", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        abilities.clear();

        if (tag.contains("Abilities", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Abilities", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                abilities.add(list.getString(i));
            }
        }
    }
}

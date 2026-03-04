package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class CopiedAbilityComponent implements INBTSerializable<CompoundTag> {

    public static final int MAX_ABILITIES = 24;

    public record CopiedAbilityData(String abilityId, String copyType, int remainingUses, String originalOwnerUUID) {

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("AbilityId", abilityId);
            tag.putString("CopyType", copyType);
            tag.putInt("RemainingUses", remainingUses);
            tag.putString("OriginalOwnerUUID", originalOwnerUUID != null ? originalOwnerUUID : "");
            return tag;
        }

        public static CopiedAbilityData fromTag(CompoundTag tag) {
            String abilityId = tag.getString("AbilityId");
            String copyType = tag.getString("CopyType");
            int remainingUses = tag.getInt("RemainingUses");
            String ownerUUID = tag.getString("OriginalOwnerUUID");
            return new CopiedAbilityData(abilityId, copyType, remainingUses, ownerUUID.isEmpty() ? null : ownerUUID);
        }

        public CopiedAbilityData withRemainingUses(int uses) {
            return new CopiedAbilityData(abilityId, copyType, uses, originalOwnerUUID);
        }
    }

    private final ArrayList<CopiedAbilityData> abilities = new ArrayList<>();

    public void addAbility(CopiedAbilityData data) {
        if (abilities.size() >= MAX_ABILITIES) {
            abilities.remove(0);
        }
        abilities.add(data);
    }

    public void removeAbility(int index) {
        if (index >= 0 && index < abilities.size()) {
            abilities.remove(index);
        }
    }

    public CopiedAbilityData getAbility(int index) {
        if (index >= 0 && index < abilities.size()) {
            return abilities.get(index);
        }
        return null;
    }

    public ArrayList<CopiedAbilityData> getAbilities() {
        return abilities;
    }

    public void decrementUses(int index) {
        if (index < 0 || index >= abilities.size()) return;
        CopiedAbilityData data = abilities.get(index);
        if (data.remainingUses() == -1) return;
        int newUses = data.remainingUses() - 1;
        if (newUses <= 0) {
            abilities.remove(index);
        } else {
            abilities.set(index, data.withRemainingUses(newUses));
        }
    }

    public int size() {
        return abilities.size();
    }

    public List<String> getAbilityIds() {
        List<String> ids = new ArrayList<>();
        for (CopiedAbilityData data : abilities) {
            ids.add(data.abilityId());
        }
        return ids;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        ListTag list = new ListTag();
        for (CopiedAbilityData data : abilities) {
            list.add(data.toTag());
        }

        tag.put("CopiedAbilities", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        abilities.clear();

        if (tag.contains("CopiedAbilities", Tag.TAG_LIST)) {
            ListTag list = tag.getList("CopiedAbilities", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                abilities.add(CopiedAbilityData.fromTag(list.getCompound(i)));
            }
        }
    }
}

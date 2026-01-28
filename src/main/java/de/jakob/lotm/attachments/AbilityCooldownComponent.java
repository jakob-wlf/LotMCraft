package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class AbilityCooldownComponent implements INBTSerializable<CompoundTag> {
    private final Map<String, Integer> cooldowns = new HashMap<>();
    
    public void setCooldown(String abilityId, int ticks) {
        cooldowns.put(abilityId, ticks);
    }
    
    public boolean isOnCooldown(String abilityId) {
        return cooldowns.getOrDefault(abilityId, 0) > 0;
    }
    
    public int getRemainingCooldown(String abilityId) {
        return cooldowns.getOrDefault(abilityId, 0);
    }
    
    public void tick() {
        cooldowns.replaceAll((id, ticks) -> Math.max(0, ticks - 1));
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag cooldownsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : cooldowns.entrySet()) {
            cooldownsTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("cooldowns", cooldownsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        cooldowns.clear();
        CompoundTag cooldownsTag = tag.getCompound("cooldowns");
        for (String key : cooldownsTag.getAllKeys()) {
            cooldowns.put(key, cooldownsTag.getInt(key));
        }
    }
}
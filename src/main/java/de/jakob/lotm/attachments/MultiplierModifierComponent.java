package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.DisableAbilityUsageForTimePacket;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiplierModifierComponent implements INBTSerializable<CompoundTag> {

    public HashMap<String, MultiplierModifier> modifiers = new HashMap<>();

    public void addMultiplier(String cause, float multiplier) {
        modifiers.computeIfAbsent(cause, k -> new MultiplierModifier(multiplier, 0));
        modifiers.put(cause, new MultiplierModifier(multiplier, modifiers.get(cause).amount + 1));
    }

    public void removeMultiplier(String cause) {
        if(modifiers.containsKey(cause)) {
            MultiplierModifier modifier = modifiers.get(cause);
            if(modifier.amount <= 1) {
                modifiers.remove(cause);
            } else {
                modifiers.put(cause, new MultiplierModifier(modifier.multiplier, modifier.amount - 1));
            }
        }
    }

    public void addMultiplierForTime(String cause, float multiplier, int ticks) {
        addMultiplier(cause, multiplier);

        ServerScheduler.scheduleDelayed(ticks, () -> removeMultiplier(cause));
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag modifiersList = new ListTag();
        for (var entry : modifiers.entrySet()) {
            CompoundTag modifierTag = new CompoundTag();
            modifierTag.putString("cause", entry.getKey());
            modifierTag.putFloat("multiplier", entry.getValue().multiplier);
            modifierTag.putInt("amount", entry.getValue().amount);
            modifiersList.add(modifierTag);
        }
        tag.put("modifiers", modifiersList);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        modifiers.clear();
        ListTag modifiersList = compoundTag.getList("modifiers", Tag.TAG_COMPOUND);
        for (int i = 0; i < modifiersList.size(); i++) {
            CompoundTag modifierTag = modifiersList.getCompound(i);
            String cause = modifierTag.getString("cause");
            float multiplier = modifierTag.getFloat("multiplier");
            int amount = modifierTag.getInt("amount");
            modifiers.put(cause, new MultiplierModifier(multiplier, amount));
        }
    }

    public record MultiplierModifier(float multiplier, int amount) {
    }
}

package de.jakob.lotm.abilities.visionary.prophecy;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record Prophecy(UUID targetID, TriggerBase trigger, TriggerEnum triggerType, UUID casterId) {
    public static final String TARGET_ID = "target_id";
    public static final String TRIGGER = "trigger";
    public static final String TRIGGER_TYPE = "trigger_type";
    public static final String CASTER_ID = "caster_id";

    public boolean checkAndPerform(Level level, LivingEntity entity){
        return trigger.checkTrigger(level, entity, casterId);
    }

    public CompoundTag toNBT(HolderLookup.Provider provider){
        CompoundTag tag = new CompoundTag();

        tag.putUUID(TARGET_ID, targetID);
        tag.put(TRIGGER, trigger.toNBT(provider));
        trigger.getType().toNBT(tag, TRIGGER_TYPE);
        tag.putUUID(CASTER_ID, casterId);

        return tag;
    }

    public static Prophecy fromNBT(CompoundTag tag, HolderLookup.Provider provider){
        UUID id = tag.getUUID(TARGET_ID);
        var trigger = TriggerBase.load(TriggerEnum.fromNBT(tag, TRIGGER_TYPE), tag, provider);
        UUID casterId = tag.getUUID(CASTER_ID);

        return new Prophecy(id, trigger, trigger.getType(), casterId);
    }

}

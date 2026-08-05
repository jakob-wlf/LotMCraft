package de.jakob.lotm.beyonders.abilities.visionary.prophecy;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.MercuryBodyAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
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

    public int checkAndPerform(Level level, LivingEntity entity){
        if (!MercuryBodyAbility.hasMercuryBody(entity)) {
            return trigger.checkTrigger(level, entity, casterId);
        }

        var casterData = BeyonderData.playerMap.get(casterId);
        int casterSequence = casterData.map(data -> data.sequence()).orElse(-1);
        if (MercuryBodyAbility.blocksInquiry(entity, casterSequence)) return -1;

        int result = trigger.checkTrigger(level, entity, casterId);
        if (result == 1) {
            String casterName = casterData.map(data -> data.trueName()).orElse(casterId.toString());
            if (level instanceof ServerLevel serverLevel && serverLevel.getPlayerByUUID(casterId) != null) {
                casterName = serverLevel.getPlayerByUUID(casterId).getGameProfile().getName();
            }
            MercuryBodyAbility.warn(
                    entity,
                    casterName,
                    casterSequence,
                    "Story Writing triggered " + trigger.getType() + " and executed " + trigger.getActionType());
        }
        return result;
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

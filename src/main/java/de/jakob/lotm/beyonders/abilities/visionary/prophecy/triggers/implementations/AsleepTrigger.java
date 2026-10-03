package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class AsleepTrigger extends TriggerBase {
    public AsleepTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.ASLEEP;
    }

    @Override
    public int getRequiredSeq() {
        return 6;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if(entity.isSleeping() || entity.hasEffect(ModEffects.ASLEEP)){
            action.action(level, entity, casterId);
            return 1;
        }

        return 0;
    }

    public static AsleepTrigger load(CompoundTag tag,
                                    ActionsEnum actionType,
                                    TriggerContextEnum contextType,
                                    HolderLookup.Provider provider){
        return new AsleepTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }
}

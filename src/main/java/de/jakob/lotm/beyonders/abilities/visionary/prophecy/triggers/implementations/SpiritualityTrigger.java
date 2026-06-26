package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations.TriggerNumbersContext;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SpiritualityTrigger extends TriggerBase {
    public SpiritualityTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.SPIRITUALITY;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof TriggerNumbersContext numbers)) return -1;

        float value = numbers.isInt ? numbers.intValue : (float) numbers.doubleValue;
        float sp = BeyonderData.getSpirituality(entity);

        if(numbers.checkOperation(value, sp)){
            action.action(level, entity, casterId);
            return 1;
        }

        return 0;
    }

    public static SpiritualityTrigger load(CompoundTag tag,
                                     ActionsEnum actionType,
                                     TriggerContextEnum contextType,
                                     HolderLookup.Provider provider){
        return new SpiritualityTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }
}

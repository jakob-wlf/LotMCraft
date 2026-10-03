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

public class SeqTrigger extends TriggerBase {
    public SeqTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.SEQUENCE;
    }

    @Override
    public int getRequiredSeq() {
        return 5;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof TriggerNumbersContext numbers)) return -1;

        int value = numbers.isInt ? numbers.intValue : (int) numbers.doubleValue;
        int seq = BeyonderData.getSequence(entity);

        if(numbers.checkOperation(value, seq)){
            action.action(level, entity, casterId);
            return 1;
        }

        return 0;
    }

    public static SeqTrigger load(CompoundTag tag,
                                     ActionsEnum actionType,
                                     TriggerContextEnum contextType,
                                     HolderLookup.Provider provider){
        return new SeqTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }
}

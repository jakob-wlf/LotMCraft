package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerHelper;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations.TriggerChainContext;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ChainTrigger extends TriggerBase {
    private TriggerBase trigger = null;

    public ChainTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.CHAIN;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if(!(level instanceof ServerLevel serverLevel)) return -1;
        if(!(context instanceof TriggerChainContext chainContext)) return -1;

        if(trigger == null){

            var casterSeq = BeyonderData.playerMap.get(casterId).get().sequence();
            trigger = TriggerHelper.deduceWithoutAction(chainContext.trigger, casterSeq);

            if(trigger == null) return -1;
        }

        var target = (LivingEntity) serverLevel.getEntity(trigger.getTarget());
        if(target == null) return 0;

        int result = trigger.checkTrigger(level, target, casterId);
        if(result == 1) {
            action.action(level, entity, casterId);
        }

        return result;
    }

    public static ChainTrigger load(CompoundTag tag,
                                     ActionsEnum actionType,
                                     TriggerContextEnum contextType,
                                     HolderLookup.Provider provider){
        return new ChainTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }
}

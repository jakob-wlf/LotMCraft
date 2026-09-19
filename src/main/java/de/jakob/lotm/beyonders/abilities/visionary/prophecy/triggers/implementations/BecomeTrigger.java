package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations.TriggerPathSeqContext;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

/** Fires only when the target's pathway AND sequence both match the prediction in one story. */
public class BecomeTrigger extends TriggerBase {
    public BecomeTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.BECOME;
    }

    @Override
    public int getRequiredSeq() {
        return 1;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if (!(context instanceof TriggerPathSeqContext pathSeq)) return -1;

        boolean pathwayMatches = pathSeq.pathway != null && pathSeq.pathway.equals(BeyonderData.getPathway(entity));
        boolean sequenceMatches = pathSeq.sequence == BeyonderData.getSequence(entity);

        if (pathwayMatches && sequenceMatches) {
            action.action(level, entity, casterId);
            return 1;
        }

        return 0;
    }

    public static BecomeTrigger load(CompoundTag tag,
                                     ActionsEnum actionType,
                                     TriggerContextEnum contextType,
                                     HolderLookup.Provider provider) {
        return new BecomeTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }
}

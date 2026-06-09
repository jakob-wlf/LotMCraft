package de.jakob.lotm.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class InstantTrigger extends TriggerBase {
    public InstantTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.INSTANT;
    }

    @Override
    public int getRequiredSeq() {
        return 5;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        action.action(level, entity, casterId);
        return 1;
    }

    public static InstantTrigger load(CompoundTag tag,
                                      ActionsEnum actionType,
                                      TriggerContextEnum contextType,
                                      HolderLookup.Provider provider){
      return new InstantTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }
}

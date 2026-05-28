package de.jakob.lotm.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.implementations.TriggerNumbersContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class HealthTrigger extends TriggerBase {
    public HealthTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.HEALTH;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public boolean checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof TriggerNumbersContext numbers)) return true;

        float value = numbers.isInt ? numbers.intValue : (float) numbers.doubleValue;
        float health = entity.getHealth();

        if(checkOperation(value, health, numbers.operation)){
            action.action(level, entity, casterId);
            return true;
        }

        return false;
    }

    public static HealthTrigger load(CompoundTag tag,
                                      ActionsEnum actionType,
                                      TriggerContextEnum contextType,
                                      HolderLookup.Provider provider){
        return new HealthTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }

    private boolean checkOperation(float value, float value2, int operation){
        return switch (operation) {
            case -2 -> value2 < value;
            case -1 -> value2 <= value;
            case 0 -> value2 == value;
            case 1 -> value2 >= value;
            case 2 -> value2 > value;
            default -> false;
        };
    }
}

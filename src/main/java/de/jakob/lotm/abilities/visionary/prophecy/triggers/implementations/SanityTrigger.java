package de.jakob.lotm.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.implementations.TriggerNumbersContext;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SanityTrigger extends TriggerBase {
    public SanityTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.SANITY;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof TriggerNumbersContext numbers)) return -1;

        float value = -1;

        if(numbers.isInt){
            value = numbers.intValue < 0? 0.0f : 1.0f;
        }
        else if(numbers.isDouble){
            value = numbers.doubleValue < 0.0 ? 0.0f : numbers.doubleValue > 1 ? 1.0f : (float) numbers.doubleValue;
        }

        if(value == -1) return -1;

        float sanity = entity.getData(ModAttachments.SANITY_COMPONENT.get()).getSanity();

        var operation = numbers.operation;

        if(checkOperation(value, sanity, operation)){
            action.action(level, entity, casterId);
            return 1;
        }

        return 0;
    }

    public static SanityTrigger load(CompoundTag tag,
                                     ActionsEnum actionType,
                                     TriggerContextEnum contextType,
                                     HolderLookup.Provider provider){
        return new SanityTrigger(ActionBase.load(actionType, tag, provider),
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

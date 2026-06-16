package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionNumberContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class HealthAction extends ActionBase {
    public HealthAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.HEALTH;
    }

    @Override
    public int getRequiredSeq() {
        return 0;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof ActionNumberContext numbers)) return;

        float value = numbers.isInt ? numbers.intValue : (float) numbers.doubleValue;

        var max = entity.getMaxHealth();
        var min = max * 0.3f;

        if(min > value){
            entity.setHealth(min);
        }
        else{
            entity.setHealth(Math.min(max, value));
        }
    }

    public static HealthAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new HealthAction(ActionContextBase.load(ActionContextEnum.NUMBER, tag, provider));
    }
}

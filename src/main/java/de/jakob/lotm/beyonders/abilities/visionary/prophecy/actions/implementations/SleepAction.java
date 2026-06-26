package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SleepAction extends ActionBase {
    public SleepAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.SLEEP;
    }

    @Override
    public int getRequiredSeq() {
        return 6;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(entity.hasEffect(ModEffects.ASLEEP)) return;

        entity.addEffect(new MobEffectInstance(ModEffects.ASLEEP, 20 * 10));
    }

    public static SleepAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new SleepAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

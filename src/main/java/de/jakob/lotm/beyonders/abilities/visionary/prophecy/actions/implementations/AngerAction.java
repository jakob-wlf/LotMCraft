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

public class AngerAction extends ActionBase {
    public AngerAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.ANGER;
    }

    @Override
    public int getRequiredSeq() {
        return 6;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        entity.addEffect(new MobEffectInstance(ModEffects.ANGER, 20 * 60 * 15));
    }

    public static AngerAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new AngerAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

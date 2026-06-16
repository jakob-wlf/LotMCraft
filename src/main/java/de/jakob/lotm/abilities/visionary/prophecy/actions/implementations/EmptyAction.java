package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EmptyAction extends ActionBase {
    public EmptyAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.EMPTY;
    }

    @Override
    public int getRequiredSeq() {
        return 10;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        return;
    }

    public static EmptyAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new EmptyAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

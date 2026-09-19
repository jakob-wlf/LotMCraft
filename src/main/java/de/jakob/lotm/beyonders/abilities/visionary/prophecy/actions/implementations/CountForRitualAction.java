package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.beyonders.advancementrituals.visionary.seq0.VisSeq0Ritual;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class CountForRitualAction extends ActionBase {
    public CountForRitualAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.COUNT_FOR_RITUAL;
    }

    @Override
    public int getRequiredSeq() {
        return 1;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        VisSeq0Ritual.onPredictionTriggered(casterId, entity);
    }

    public static CountForRitualAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new CountForRitualAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

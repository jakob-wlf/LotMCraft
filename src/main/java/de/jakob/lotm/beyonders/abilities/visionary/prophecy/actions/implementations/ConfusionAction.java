package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ConfusionAction extends ActionBase {
    public ConfusionAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.CONFUSION;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        AbilityUtil.ignoreAllies.put(entity.getUUID(), true);

        ServerScheduler.scheduleDelayed(20 * 30, () -> {
            AbilityUtil.ignoreAllies.remove(entity.getUUID());
        });
    }

    public static ConfusionAction load(CompoundTag tag, HolderLookup.Provider provider) {
            return new ConfusionAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

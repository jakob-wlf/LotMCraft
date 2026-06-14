package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations.ActionNumberContext;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class DigestionAction extends ActionBase {
    public DigestionAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.DIGESTION;
    }

    @Override
    public int getRequiredSeq() {
        return 1;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof ActionNumberContext numbers)) return;

        if(numbers.isInt){
            float value = numbers.intValue <= 0 ? -1 : 1;
            float has = BeyonderData.getDigestionProgress((ServerPlayer)entity);

            float newValue = Math.max(0.0f, Math.min(1.0f, has + value));

            BeyonderData.digest((ServerPlayer)entity,newValue - has, false);
        }
        else if(numbers.isDouble){
            float value = (float) (numbers.doubleValue > 1.0? 1.0 : numbers.doubleValue < 0 ? 0 : numbers.doubleValue);
            float has = BeyonderData.getDigestionProgress((ServerPlayer)entity);

            float newValue = value - has;

            BeyonderData.digest((ServerPlayer)entity,newValue - has, false);
        }
    }

    public static DigestionAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new DigestionAction(ActionContextBase.load(ActionContextEnum.NUMBER, tag, provider));
    }
}

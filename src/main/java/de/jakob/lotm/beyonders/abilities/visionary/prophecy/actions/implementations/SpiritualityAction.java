package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations.ActionNumberContext;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SpiritualityAction extends ActionBase {
    public SpiritualityAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.SPIRITUALITY;
    }

    @Override
    public int getRequiredSeq() {
        return 0;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof ActionNumberContext numbers)) return;

        float value = numbers.isInt ? numbers.intValue : (float) numbers.doubleValue;

        float max = BeyonderData.getMaxSpirituality(BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));
        if(value < max * 0.3f){
            BeyonderData.setSpirituality(entity, max * 0.3f);
            return;
        }

        BeyonderData.setSpirituality(entity, value);
    }

    public static SpiritualityAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new SpiritualityAction(ActionContextBase.load(ActionContextEnum.NUMBER, tag, provider));
    }
}

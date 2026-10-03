package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations.ActionNumberContext;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SanityAction extends ActionBase {
    public SanityAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.SANITY;
    }

    @Override
    public int getRequiredSeq() {
        return 0;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof ActionNumberContext numbers)) return;

        if(numbers.isInt){
            float value = numbers.intValue <= 0 ? 0.4f : 1.0f;
            var sanity = entity.getData(ModAttachments.SANITY_COMPONENT.get());

            sanity.setSanity(value);
        }
        else if(numbers.isDouble){
            float value = (float) (numbers.doubleValue > 1.0? 1.0 : numbers.doubleValue < 0 ? 0 : numbers.doubleValue);
            var sanity = entity.getData(ModAttachments.SANITY_COMPONENT.get());

            value = Math.max(0.4f, value);
            sanity.setSanity(value);
        }
    }

    public static SanityAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new SanityAction(ActionContextBase.load(ActionContextEnum.NUMBER, tag, provider));
    }
}

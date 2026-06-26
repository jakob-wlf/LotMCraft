package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class UnSealAction extends ActionBase {
    public UnSealAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.UNSEAL;
    }

    @Override
    public int getRequiredSeq() {
        return 1;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT.get()).enableAllAbilities();
    }

    public static UnSealAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new UnSealAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

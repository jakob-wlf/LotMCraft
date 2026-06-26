package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SealAction extends ActionBase {
    public SealAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.SEAL;
    }

    @Override
    public int getRequiredSeq() {
        return 1;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        DisabledAbilitiesComponent comp = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        comp.disableAbilityUsage("sealed_visionary");

        ServerScheduler.scheduleDelayed(20 * 30, ()->{
            comp.enableAbilityUsage("sealed_visionary");
        });
    }

    public static SealAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new SealAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

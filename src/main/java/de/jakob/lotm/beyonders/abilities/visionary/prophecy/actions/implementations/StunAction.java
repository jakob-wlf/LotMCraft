package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class StunAction extends ActionBase {
    public StunAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.STUN;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if (level.isClientSide) return;

        ServerScheduler.scheduleForDuration(0, 1, 20 * 10, () -> {
            var pos = entity.position();
            entity.teleportTo(pos.x, pos.y, pos.z);
            entity.setDeltaMovement(new Vec3(0, 0, 0));
            entity.hurtMarked = true;
        });
    }

    public static StunAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new  StunAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

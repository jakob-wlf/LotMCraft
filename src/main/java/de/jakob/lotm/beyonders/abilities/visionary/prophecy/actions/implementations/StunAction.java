package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.addons.rituals.wheel_of_fortune.Seq1;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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

        Seq1.stunned.add(entity.getUUID());

        ServerScheduler.scheduleForDuration(0, 1, 20 * 7, () -> {
            var pos = entity.position();
            entity.teleportTo(pos.x, pos.y, pos.z);
            entity.setDeltaMovement(new Vec3(0, 0, 0));
            entity.hurtMarked = true;
        }, () -> {Seq1.stunned.remove(entity.getUUID());}, (ServerLevel) level);
    }

    public static StunAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new  StunAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

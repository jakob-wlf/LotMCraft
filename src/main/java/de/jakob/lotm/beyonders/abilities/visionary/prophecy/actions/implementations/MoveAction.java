package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations.ActionPositionContext;
import de.jakob.lotm.util.helper.TeleportationUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MoveAction extends ActionBase {
    public MoveAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.MOVE;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        if(!(context instanceof ActionPositionContext position)) return;

        var loc = TeleportationUtil.clampToBorder((ServerLevel) serverPlayer.level(), position.pos);

        final var finalTarget = entity;
        ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            if (!finalTarget.isAlive()) {
                return;
            }

            Vec3 current = finalTarget.position();

            double dx = loc.x - current.x;
            double dz = loc.z - current.z;

            double distanceSqr = dx * dx + dz * dz;

            if (distanceSqr < 0.05) {
                finalTarget.setDeltaMovement(Vec3.ZERO);
                finalTarget.hurtMarked = true;
                return;
            }

            double distance = Math.sqrt(distanceSqr);

            double dirX = dx / distance;
            double dirZ = dz / distance;

            BlockPos frontPos = BlockPos.containing(
                    current.x + dirX * 0.7,
                    current.y,
                    current.z + dirZ * 0.7
            );

            BlockState frontBlock = finalTarget.level().getBlockState(frontPos);

            boolean blocked = !frontBlock.getCollisionShape(finalTarget.level(), frontPos).isEmpty();

            Vec3 velocity = finalTarget.getDeltaMovement();

            if (blocked && finalTarget.onGround()) {
                finalTarget.setDeltaMovement(
                        dirX * 0.4,
                        0.42,
                        dirZ * 0.4
                );

                finalTarget.hurtMarked = true;
                return;
            }

            finalTarget.setDeltaMovement(
                    dirX * 0.4,
                    velocity.y,
                    dirZ * 0.4
            );

            finalTarget.hurtMarked = true;
        });
    }

    public static MoveAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new MoveAction(ActionContextBase.load(ActionContextEnum.POSITION, tag, provider));
    }
}

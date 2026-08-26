package de.jakob.lotm.beyonders.abilities.fool.marionettes.goals;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.MarionetteComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

public class MarionetteMovementGoal extends Goal {
    private final Mob marionette;
    private Player controller;
    private Vec3 wanderTarget;

    public MarionetteMovementGoal(Mob marionette) {
        this.marionette = marionette;
        // Only control movement
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!getControllerAndCheckValid()) return false;

        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());

        return component.isMarionette() && marionette.getTarget() == null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!getControllerAndCheckValid()) return false;

        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());

        return  component.isMarionette() && marionette.getTarget() == null;
    }

    @Override
    public void tick() {
        if (controller == null) return;

        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());

        switch (component.getCurrentMode()) {
            case FOLLOW -> {
                double distance = marionette.distanceTo(controller);

                if (distance > 6.0) {
                    marionette.getNavigation().moveTo(controller, 1.0);
                }
            }
            case STAY -> {
                marionette.getNavigation().stop();
                marionette.getMoveControl().setWantedPosition(marionette.getX(), marionette.getY(), marionette.getZ(), 0);
            }
            case WANDER -> {
                if(!(marionette instanceof PathfinderMob pathfinderMob)) return;
                if(wanderTarget == null) {
                    wanderTarget = DefaultRandomPos.getPos(pathfinderMob, 12, 7);
                    if(wanderTarget != null)
                        marionette.getNavigation().moveTo(wanderTarget.x, wanderTarget.y, wanderTarget.z, 1.0);
                }

                if(wanderTarget != null && (marionette.distanceToSqr(wanderTarget) < 1.0 || marionette.distanceToSqr(wanderTarget) > 160)) {
                    wanderTarget = null;
                }

                if(marionette.getRandom().nextInt(100) < 1) {
                    wanderTarget = null;
                }
            }
        }
    }

    private boolean getControllerAndCheckValid() {
        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette()) return false;

        try {
            UUID controllerUUID = UUID.fromString(component.getControllerUUID());
            controller = marionette.level().getPlayerByUUID(controllerUUID);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return controller != null && controller.isAlive();
    }
}
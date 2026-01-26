package de.jakob.lotm.entity.custom.goals.avatar;

import de.jakob.lotm.entity.custom.AvatarEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class AvatarTargetGoal extends TargetGoal {
    private final AvatarEntity avatar;
    private Player controller;

    public AvatarTargetGoal(AvatarEntity avatar) {
        super(avatar, false);
        this.avatar = avatar;
        // Only control targeting
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!getControllerAndCheckValid()) return false;

        // Only target when in follow mode AND don't have a target yet
        if (avatar.getTarget() != null) return false;

        // Check if there's someone to target
        return findValidTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        // This goal stops immediately after setting a target
        // Let attack goals take over
        return false;
    }

    @Override
    public void start() {
        LivingEntity target = findValidTarget();
        if (target != null && target != controller && target != avatar) {
            avatar.setTarget(target);
        }
    }

    @Override
    public void tick() {
        // Clear invalid targets (including controller!)
        LivingEntity currentTarget = avatar.getTarget();
        if (currentTarget != null &&
                (!currentTarget.isAlive() || currentTarget.isRemoved() ||
                        currentTarget == controller || currentTarget == avatar)) {
            avatar.setTarget(null);
            avatar.setLastHurtByMob(null); // Clear last hurt by reference
        }

        // Extra safety: if somehow targeting controller, clear immediately
        if (avatar.getTarget() == controller) {
            avatar.setTarget(null);
            avatar.setLastHurtByMob(null);
        }
    }

    private LivingEntity findValidTarget() {
        if (controller == null) return null;

        // Defend controller if attacked (higher priority)
        LivingEntity controllerAttacker = controller.getLastHurtByMob();
        if (controllerAttacker != null && controllerAttacker.isAlive() &&
                controllerAttacker != avatar && controllerAttacker != controller) {
            return controllerAttacker;
        }

        // Fight what the controller fights
        LivingEntity controllerTarget = controller.getLastHurtMob();
        if (controllerTarget != null && controllerTarget.isAlive() &&
                controllerTarget != avatar && controllerTarget != controller) {
            return controllerTarget;
        }

        return null;
    }

    private boolean getControllerAndCheckValid() {

        try {
            UUID controllerUUID = avatar.getOriginalOwner();
            controller = avatar.level().getPlayerByUUID(controllerUUID);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return controller != null && controller.isAlive();
    }
}
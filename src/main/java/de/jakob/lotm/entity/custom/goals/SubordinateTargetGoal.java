package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class SubordinateTargetGoal extends TargetGoal {
    private final Mob marionette;
    private Player controller;

    public SubordinateTargetGoal(Mob marionette) {
        super(marionette, false);
        this.marionette = marionette;
        // Only control targeting
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!getControllerAndCheckValid()) return false;

        SubordinateComponent component = marionette.getData(ModAttachments.SUBORDINATE_COMPONENT.get());

        // Only target when in follow mode AND don't have a target yet
        if (!component.isFollowMode() || marionette.getTarget() != null) return false;

        if(!component.shouldAttack()) return false;

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
        if (target != null && target != controller && target != marionette) {
            marionette.setTarget(target);
        }
    }

    @Override
    public void tick() {
        // Clear invalid targets (including controller!)
        LivingEntity currentTarget = marionette.getTarget();
        if (currentTarget != null &&
                (!currentTarget.isAlive() || currentTarget.isRemoved() ||
                        currentTarget == controller || currentTarget == marionette)) {
            marionette.setTarget(null);
            marionette.setLastHurtByMob(null); // Clear last hurt by reference
        }

        // Extra safety: if somehow targeting controller, clear immediately
        if (marionette.getTarget() == controller) {
            marionette.setTarget(null);
            marionette.setLastHurtByMob(null);
        }
    }

    private LivingEntity findValidTarget() {
        if (controller == null) return null;

        // Defend controller if attacked (higher priority)
        LivingEntity controllerAttacker = controller.getLastHurtByMob();
        if (controllerAttacker != null && controllerAttacker.isAlive() &&
                controllerAttacker != marionette && controllerAttacker != controller) {
            return controllerAttacker;
        }

        // Fight what the controller fights
        LivingEntity controllerTarget = controller.getLastHurtMob();
        if (controllerTarget != null && controllerTarget.isAlive() &&
                controllerTarget != marionette && controllerTarget != controller) {
            return controllerTarget;
        }

        return null;
    }

    private boolean getControllerAndCheckValid() {
        SubordinateComponent component = marionette.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (!component.isSubordinate()) return false;

        try {
            UUID controllerUUID = UUID.fromString(component.getControllerUUID());
            controller = marionette.level().getPlayerByUUID(controllerUUID);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return controller != null && controller.isAlive();
    }
}
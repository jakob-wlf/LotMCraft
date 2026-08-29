package de.jakob.lotm.beyonders.abilities.fool.marionettes.goals;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.MarionetteComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class MarionetteTargetGoal extends TargetGoal {
    private final Mob marionette;
    private Player controller;

    public MarionetteTargetGoal(Mob marionette) {
        super(marionette, false);
        this.marionette = marionette;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!getControllerAndCheckValid()) return false;

        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());

        if (!component.getCurrentMode().shouldAttack) return false;

        if(!component.shouldAttack()) return false;

        return findValidTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
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
        LivingEntity currentTarget = marionette.getTarget();
        if (currentTarget != null &&
                (!currentTarget.isAlive() || currentTarget.isRemoved() ||
                        currentTarget == controller || currentTarget == marionette)) {
            marionette.setTarget(null);
            marionette.setLastHurtByMob(null);
        }

        if (marionette.getTarget() == controller) {
            marionette.setTarget(null);
            marionette.setLastHurtByMob(null);
        }
    }

    private LivingEntity findValidTarget() {
        if (controller == null) return null;

        LivingEntity controllerAttacker = controller.getLastHurtByMob();
        if (controllerAttacker != null && controllerAttacker.isAlive() &&
                controllerAttacker != marionette && controllerAttacker != controller) {
            return controllerAttacker;
        }

        LivingEntity controllerTarget = controller.getLastHurtMob();
        if (controllerTarget != null && controllerTarget.isAlive() &&
                controllerTarget != marionette && controllerTarget != controller) {
            return controllerTarget;
        }

        return null;
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
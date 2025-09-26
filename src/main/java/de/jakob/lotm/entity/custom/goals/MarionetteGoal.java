package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class MarionetteGoal extends Goal {
    private final Mob marionette;
    private Player controller;
    private LivingEntity target;

    public MarionetteGoal(Mob marionette) {
        this.marionette = marionette;
        // Set flags to control movement and targeting
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
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

    @Override
    public void tick() {
        if (controller == null) return;

        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());

        // Handle targeting first
        handleTargeting();

        // Handle movement if not currently fighting
        if (marionette.getTarget() == null && component.isFollowMode()) {
            handleFollowing();
        }
    }

    private void handleTargeting() {
        if(marionette.getTarget() != null && (!marionette.getTarget().isAlive() || marionette.getTarget().isRemoved() || marionette.getTarget() == controller || marionette.getTarget() == marionette)) {
            marionette.setTarget(null);
        }

        // Defend controller if attacked
        LivingEntity controllerAttacker = controller.getLastHurtByMob();
        if (controllerAttacker != null && controllerAttacker.isAlive() && controllerAttacker != marionette) {
            marionette.setTarget(controllerAttacker);
            return;
        }

        // Fight what the controller fights
        LivingEntity controllerTarget = controller.getLastHurtMob();
        if (controllerTarget != null && controllerTarget.isAlive() &&
                controllerTarget != marionette && !controllerTarget.equals(controller)) {
            marionette.setTarget(controllerTarget);
        }
    }

    private void handleFollowing() {
        double distance = marionette.distanceTo(controller);

        if (distance > 3.0) {
            marionette.getNavigation().moveTo(controller, 1.0);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }
}
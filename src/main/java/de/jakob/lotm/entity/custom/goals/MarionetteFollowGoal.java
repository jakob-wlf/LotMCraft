package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class MarionetteFollowGoal extends Goal {
    private final Mob marionette;
    private Player controller;

    public MarionetteFollowGoal(Mob marionette) {
        this.marionette = marionette;
        // Only control movement
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!getControllerAndCheckValid()) return false;

        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());

        // Only follow when in follow mode AND not currently fighting
        return component.isFollowMode() && marionette.getTarget() == null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!getControllerAndCheckValid()) return false;

        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());

        // Stop following if no longer in follow mode OR if we now have a target
        return component.isFollowMode() && marionette.getTarget() == null;
    }

    @Override
    public void tick() {
        if (controller == null) return;

        double distance = marionette.distanceTo(controller);

        if (distance > 6.0) {
            marionette.getNavigation().moveTo(controller, 1.0);
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
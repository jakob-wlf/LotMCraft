package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class SubordinateFollowGoal extends Goal {
    private final Mob subordinate;
    private Player controller;

    public SubordinateFollowGoal(Mob marionette) {
        this.subordinate = marionette;
        // Only control movement
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!getControllerAndCheckValid()) return false;

        SubordinateComponent component = subordinate.getData(ModAttachments.SUBORDINATE_COMPONENT.get());

        // Only follow when in follow mode AND not currently fighting
        return component.isFollowMode() && subordinate.getTarget() == null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!getControllerAndCheckValid()) return false;

        SubordinateComponent component = subordinate.getData(ModAttachments.SUBORDINATE_COMPONENT.get());

        // Stop following if no longer in follow mode OR if we now have a target
        return component.isFollowMode() && subordinate.getTarget() == null;
    }

    @Override
    public void tick() {
        if (controller == null) return;

        double distance = subordinate.distanceTo(controller);

        if (distance > 6.0) {
            subordinate.getNavigation().moveTo(controller, 1.0);
        }
    }

    private boolean getControllerAndCheckValid() {
        SubordinateComponent component = subordinate.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (!component.isSubordinate()) return false;

        try {
            UUID controllerUUID = UUID.fromString(component.getControllerUUID());
            controller = subordinate.level().getPlayerByUUID(controllerUUID);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return controller != null && controller.isAlive();
    }
}
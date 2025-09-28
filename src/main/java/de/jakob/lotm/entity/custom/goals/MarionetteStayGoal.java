package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class MarionetteStayGoal extends Goal {
    private final Mob marionette;

    public MarionetteStayGoal(Mob marionette) {
        this.marionette = marionette;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette()) return false;

        // Run when not in follow mode and not fighting
        return !component.isFollowMode() && marionette.getTarget() == null;
    }

    @Override
    public void tick() {
        // Stop all movement
        marionette.getNavigation().stop();
        marionette.getMoveControl().setWantedPosition(marionette.getX(), marionette.getY(), marionette.getZ(), 0);
    }
}
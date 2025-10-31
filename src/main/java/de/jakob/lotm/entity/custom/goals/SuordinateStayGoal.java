package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SuordinateStayGoal extends Goal {
    private final Mob marionette;

    public SuordinateStayGoal(Mob marionette) {
        this.marionette = marionette;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        SubordinateComponent component = marionette.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (!component.isSubordinate()) return false;

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
package de.jakob.lotm.beyonders.abilities.fool.marionettes.goals;

import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class MarionetteMaxDistanceGoal extends Goal {
    private final Mob marionette;

    public MarionetteMaxDistanceGoal(Mob marionette) {
        this.marionette = marionette;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        return component.isMarionette();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette()) return;

        String controllerUUID = component.getControllerUUID();
        if (controllerUUID.isEmpty()) {
            killMarionette();
            return;
        }

        Player controller = findPlayerAcrossAllLevels(controllerUUID);
        if (controller == null) {
            return;
        }

        if(!marionette.level().equals(controller.level())) {
            return;
        }

        double sqrdDistance = marionette.distanceToSqr(controller);
        if(sqrdDistance > getMaxDistanceSqrd(controller)) {
            killMarionette();
        }
    }

    public double getMaxDistanceSqrd(Player player) {
        return switch (BeyonderData.getSequence(player)) {
            default -> 10000;
            case 4 -> 1000000;
            case 3 -> 2250000;
            case 2 -> 9000000;
            case 1 -> 25000000;
            case 0 -> 100000000;
        };
    }

    private Player findPlayerAcrossAllLevels(String uuidString) {
        try {
            UUID uuid = UUID.fromString(uuidString);

            if (marionette.getServer() != null) {
                for (ServerLevel level : marionette.getServer().getAllLevels()) {
                    Player player = level.getPlayerByUUID(uuid);
                    if (player != null) {
                        return player;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
        }

        return null;
    }

    private void killMarionette() {
        marionette.level().broadcastEntityEvent(marionette, (byte) 3); // Death particles
        
        marionette.hurt(marionette.damageSources().generic(), Float.MAX_VALUE);
    }
}
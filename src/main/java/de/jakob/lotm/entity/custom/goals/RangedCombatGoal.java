package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class RangedCombatGoal extends Goal {
    private final BeyonderNPCEntity entity;
    private final double moveSpeed;
    private final float minDistance;
    private final float maxDistance;
    private final float optimalDistance;

    private int meleeCooldown = 0;
    private int repositionCooldown = 0;

    private static final int MELEE_COOLDOWN_TICKS = 60;  // 3 seconds
    private static final int REPOSITION_COOLDOWN_TICKS = 40;  // 2 seconds

    public RangedCombatGoal(BeyonderNPCEntity entity, double moveSpeed, float minDistance, float maxDistance) {
        this.entity = entity;
        this.moveSpeed = moveSpeed;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.optimalDistance = (minDistance + maxDistance) / 2;  // Sweet spot
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = entity.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = entity.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        // Reset cooldowns on start
        meleeCooldown = 0;
        repositionCooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = entity.getTarget();
        if (target == null) return;

        double distance = entity.distanceTo(target);

        // Decrement cooldowns
        if (meleeCooldown > 0) meleeCooldown--;
        if (repositionCooldown > 0) repositionCooldown--;

        // Always look at target
        entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Handle positioning - only reposition if cooldown is up to avoid constant movement
        if (repositionCooldown <= 0) {
            handlePositioning(target, distance);
        }

        // Opportunistic melee when very close
        if (meleeCooldown <= 0 && distance <= 2.5) {
            // Higher chance to melee when really close
            float meleeChance = distance <= 1.5 ? 0.3f : 0.15f;

            if (entity.getRandom().nextFloat() < meleeChance) {
                entity.doHurtTarget(target);
                meleeCooldown = MELEE_COOLDOWN_TICKS;
            }
        }
    }

    private void handlePositioning(LivingEntity target, double distance) {
        if (distance < minDistance) {
            // Too close - back away to optimal distance
            double retreatX = entity.getX() + (entity.getX() - target.getX()) * 0.3;
            double retreatZ = entity.getZ() + (entity.getZ() - target.getZ()) * 0.3;

            if (entity.getNavigation().moveTo(retreatX, entity.getY(), retreatZ, moveSpeed * 1.2)) {
                repositionCooldown = REPOSITION_COOLDOWN_TICKS;
            }
        } else if (distance > maxDistance) {
            // Too far - move closer to optimal distance
            if (entity.getNavigation().moveTo(target, moveSpeed)) {
                repositionCooldown = REPOSITION_COOLDOWN_TICKS;
            }
        } else if (distance < optimalDistance * 0.8 || distance > optimalDistance * 1.2) {
            // Within max range but not at optimal distance - adjust occasionally
            if (entity.getRandom().nextFloat() < 0.1f) {  // 10% chance per tick
                // Move toward optimal distance
                double ratio = optimalDistance / distance;
                double targetX = entity.getX() + (target.getX() - entity.getX()) * (1 - ratio);
                double targetZ = entity.getZ() + (target.getZ() - entity.getZ()) * (1 - ratio);

                if (entity.getNavigation().moveTo(targetX, entity.getY(), targetZ, moveSpeed * 0.8)) {
                    repositionCooldown = REPOSITION_COOLDOWN_TICKS;
                }
            }
        } else {
            // At good distance - stop moving and let abilities do the work
            entity.getNavigation().stop();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
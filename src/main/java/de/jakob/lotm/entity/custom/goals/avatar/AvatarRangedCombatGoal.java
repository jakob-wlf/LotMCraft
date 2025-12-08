package de.jakob.lotm.entity.custom.goals.avatar;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.entity.custom.ErrorAvatarEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class AvatarRangedCombatGoal extends Goal {
    private final ErrorAvatarEntity entity;
    private final double moveSpeed;
    private final float minDistance;
    private final float maxDistance;
    private int attackCooldown = 0;
    private int meleeCooldown = 0;

    public AvatarRangedCombatGoal(ErrorAvatarEntity entity, double moveSpeed, float minDistance, float maxDistance) {
        this.entity = entity;
        this.moveSpeed = moveSpeed;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = entity.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = entity.getTarget();
        if (target == null) return;

        double distance = entity.distanceTo(target);
        
        if (attackCooldown > 0) attackCooldown--;
        if (meleeCooldown > 0) meleeCooldown--;

        // Look at target
        entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (distance < minDistance) {
            // Too close - back away
            entity.getNavigation().moveTo(
                entity.getX() + (entity.getX() - target.getX()) * 0.5,
                entity.getY(),
                entity.getZ() + (entity.getZ() - target.getZ()) * 0.5,
                moveSpeed
            );
        } else if (distance > maxDistance) {
            // Too far - move closer
            entity.getNavigation().moveTo(target, moveSpeed);
        } else {
            // In range - stop and use abilities
            entity.getNavigation().stop();
            
            // 10% chance to go melee if melee cooldown is up
            if (meleeCooldown <= 0 && entity.getRandom().nextFloat() < 0.1f && distance <= 3.0) {
                entity.doHurtTarget(target);
                meleeCooldown = 60; // 3 seconds before next melee attempt
            }
        }
    }
}
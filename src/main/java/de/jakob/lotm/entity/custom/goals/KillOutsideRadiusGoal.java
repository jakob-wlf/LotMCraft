package de.jakob.lotm.entity.custom.goals;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class KillOutsideRadiusGoal extends Goal {

    private final Mob mob;
    private Vec3 center;
    private double radiusSqr;

    public KillOutsideRadiusGoal(Mob mob, Vec3 center, double radius) {
        this.mob = mob;
        this.center = center;
        this.radiusSqr = radius * radius;

        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        return true; // always active
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public void tick() {
        if (mob.level().isClientSide) return;
        if (!mob.isAlive()) return;

        double dist = mob.position().distanceToSqr(center);

        if (dist > radiusSqr) {
            killMob();
        }
    }

    private void killMob() {
        // Use lethal damage instead of discard/remove
        DamageSource source = new DamageSource(
                mob.level().registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.OUTSIDE_BORDER)
        );

        mob.hurt(source, Float.MAX_VALUE);
    }

    public void setCenter(Vec3 center) {
        this.center = center;
    }

    public void setRadius(double radius) {
        this.radiusSqr = radius * radius;
    }
}

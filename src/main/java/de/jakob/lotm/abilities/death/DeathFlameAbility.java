package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class DeathFlameAbility extends Ability {

    private static final int DURATION = 20 * 7;
    private static final double FLAME_LENGTH = 16.0;
    private static final double FLAME_MAX_RADIUS = 5.0;

    public DeathFlameAbility(String id) {
        super(id, 10);
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 2.0f, 0.4f);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.AMBIENT, 3.0f, 0.5f);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5f, 0.6f);

        ServerScheduler.scheduleForDuration(0, 1, DURATION, () -> {
            if (!entity.isAlive()) return;

            Vec3 origin = entity.getEyePosition();
            Vec3 look = entity.getLookAngle().normalize();
            Vec3 p1 = getPerp(look);
            Vec3 p2 = look.cross(p1).normalize();

            spawnConeParticles(serverLevel, origin, look, p1, p2);
            damageEntitiesInCone(serverLevel, entity, origin, look);

            if (BeyonderData.isGriefingEnabled(entity)) {
                placeFireInCone(serverLevel, origin, look);
            }

            if (serverLevel.getGameTime() % 6 == 0) {
                serverLevel.playSound(null, BlockPos.containing(origin.add(look.scale(8))),
                        SoundEvents.FIRE_AMBIENT, SoundSource.AMBIENT,
                        1.8f, 0.6f + serverLevel.random.nextFloat() * 0.4f);
            }
        }, () -> {
            serverLevel.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 0.8f);
        }, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void spawnConeParticles(ServerLevel level, Vec3 origin, Vec3 look, Vec3 p1, Vec3 p2) {
        for (int step = 2; step <= (int) FLAME_LENGTH; step += 2) {
            double t = step / FLAME_LENGTH;
            double radius = step * (FLAME_MAX_RADIUS / FLAME_LENGTH);
            Vec3 center = origin.add(look.scale(step));

            int ringParticles = Math.max(4, (int) (2 * Math.PI * radius * 1.2));
            for (int i = 0; i < ringParticles; i++) {
                if (level.random.nextFloat() > 0.5f) continue;
                double angle = (2.0 * Math.PI * i) / ringParticles + level.random.nextDouble() * 0.3;
                double r = radius * (0.4 + 0.6 * level.random.nextDouble());
                Vec3 pos = center
                        .add(p1.scale(Math.cos(angle) * r))
                        .add(p2.scale(Math.sin(angle) * r));

                double spread = radius * 0.04;
                double velX = look.x * 0.05 + (level.random.nextDouble() - 0.5) * spread;
                double velY = look.y * 0.05 + level.random.nextDouble() * 0.06;
                double velZ = look.z * 0.05 + (level.random.nextDouble() - 0.5) * spread;

                level.sendParticles(ModParticles.WHITE_FLAME.get(), pos.x, pos.y, pos.z, 1, velX, velY, velZ, 0.02 + t * 0.02);
            }
        }

        for (int i = 0; i < 20; i++) {
            double dist = 1.0 + level.random.nextDouble() * FLAME_LENGTH;
            double t = dist / FLAME_LENGTH;
            double radius = dist * (FLAME_MAX_RADIUS / FLAME_LENGTH);
            double angle = level.random.nextDouble() * 2 * Math.PI;
            double r = level.random.nextDouble() * radius;
            Vec3 pos = origin.add(look.scale(dist))
                    .add(p1.scale(Math.cos(angle) * r))
                    .add(p2.scale(Math.sin(angle) * r));

            level.sendParticles(ModParticles.WHITE_FLAME.get(), pos.x, pos.y, pos.z, 1,
                    look.x * 0.08 + (level.random.nextDouble() - 0.5) * 0.04,
                    look.y * 0.06 + level.random.nextDouble() * 0.06,
                    look.z * 0.08 + (level.random.nextDouble() - 0.5) * 0.04,
                    0.02 + t * 0.02);
        }

        for (int i = 0; i < 6; i++) {
            double dist = level.random.nextDouble() * 3.0;
            Vec3 pos = origin.add(look.scale(dist))
                    .add(p1.scale((level.random.nextDouble() - 0.5) * dist * 0.3))
                    .add(p2.scale((level.random.nextDouble() - 0.5) * dist * 0.3));

            level.sendParticles(ModParticles.WHITE_FLAME.get(), pos.x, pos.y, pos.z, 1,
                    look.x * 0.1, look.y * 0.08 + 0.05, look.z * 0.1, 0.04);
        }
    }

    private void damageEntitiesInCone(ServerLevel level, LivingEntity source, Vec3 origin, Vec3 look) {
        Vec3 coneEnd = origin.add(look.scale(FLAME_LENGTH));
        AABB bounds = new AABB(
                Math.min(origin.x, coneEnd.x) - FLAME_MAX_RADIUS,
                Math.min(origin.y, coneEnd.y) - FLAME_MAX_RADIUS,
                Math.min(origin.z, coneEnd.z) - FLAME_MAX_RADIUS,
                Math.max(origin.x, coneEnd.x) + FLAME_MAX_RADIUS,
                Math.max(origin.y, coneEnd.y) + FLAME_MAX_RADIUS,
                Math.max(origin.z, coneEnd.z) + FLAME_MAX_RADIUS
        );

        level.getEntitiesOfClass(LivingEntity.class, bounds).stream()
                .filter(e -> e != source && AbilityUtil.mayTarget(source, e))
                .filter(e -> isInCone(e.getEyePosition(), origin, look))
                .forEach(e -> {
                    e.invulnerableTime = 0;
                    e.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, source), (float) DamageLookup.lookupDps(2, .8, 1, 30) * multiplier(source));
                    e.setRemainingFireTicks(e.getRemainingFireTicks() + 30);
                });
    }

    private void placeFireInCone(ServerLevel level, Vec3 origin, Vec3 look) {
        Vec3 p1 = getPerp(look);
        Vec3 p2 = look.cross(p1).normalize();

        for (int step = 2; step <= (int) FLAME_LENGTH; step += 2) {
            double radius = step * (FLAME_MAX_RADIUS / FLAME_LENGTH);
            Vec3 center = origin.add(look.scale(step));

            for (int i = 0; i < 4; i++) {
                double angle = level.random.nextDouble() * 2 * Math.PI;
                double r = level.random.nextDouble() * radius;
                Vec3 offset = p1.scale(Math.cos(angle) * r).add(p2.scale(Math.sin(angle) * r));
                Vec3 pos = center.add(offset);

                BlockPos firePos = BlockPos.containing(pos.x, pos.y - 1, pos.z);
                BlockPos abovePos = firePos.above();

                if (!level.getBlockState(firePos).isAir() && level.getBlockState(abovePos).isAir()) {
                    level.setBlockAndUpdate(abovePos, Blocks.FIRE.defaultBlockState());
                }
            }
        }
    }

    private boolean isInCone(Vec3 point, Vec3 origin, Vec3 look) {
        Vec3 toPoint = point.subtract(origin);
        double dot = toPoint.dot(look);
        if (dot < 0 || dot > FLAME_LENGTH) return false;
        double maxRadius = dot * (FLAME_MAX_RADIUS / FLAME_LENGTH);
        double perpDistSq = toPoint.subtract(look.scale(dot)).lengthSqr();
        return perpDistSq <= maxRadius * maxRadius;
    }

    private Vec3 getPerp(Vec3 v) {
        if (Math.abs(v.x) < 0.9) {
            return v.cross(new Vec3(1, 0, 0)).normalize();
        }
        return v.cross(new Vec3(0, 1, 0)).normalize();
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10000;
    }
}
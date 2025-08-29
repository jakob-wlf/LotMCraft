package de.jakob.lotm.util.helper;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbilityUtil {

    public static BlockPos getTargetBlock(LivingEntity entity, int radius) {
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition = playerPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));

            if (!block.isAir()) {
                targetPosition = playerPosition.add(lookDirection.scale(i - 1));
                break;
            }
        }

        return BlockPos.containing(targetPosition);
    }

    public static double distanceToGround(Level level, LivingEntity entity) {
        Vec3 startPos = entity.position();

        BlockPos pos = BlockPos.containing(startPos.x, startPos.y, startPos.z);

        for(int i = 0; i < 500; i++) {
            Vec3 currentPos = startPos.subtract(0, i * .5, 0);
            pos = BlockPos.containing(currentPos.x, currentPos.y, currentPos.z);
            if(!level.getBlockState(pos).isAir())
                break;
        }

        return pos.getCenter().distanceTo(startPos);
    }

    public static BlockPos getTargetBlock(LivingEntity entity, double radius, boolean oneBlockBefore) {
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition = playerPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));

            if (!block.getCollisionShape(entity.level(), BlockPos.containing(targetPosition)).isEmpty()) {
                if(oneBlockBefore)
                    targetPosition = playerPosition.add(lookDirection.scale(i - 1));
                else
                    targetPosition = playerPosition.add(lookDirection.scale(i));

                break;
            }
        }

        return BlockPos.containing(targetPosition);
    }

    public static BlockPos getTargetBlock(LivingEntity entity, double minRadius, double radius, boolean oneBlockBefore) {
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition = playerPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));

            if (!block.isAir() && i >= minRadius) {
                if(oneBlockBefore)
                    targetPosition = playerPosition.add(lookDirection.scale(i - 1));
                else
                    targetPosition = playerPosition.add(lookDirection.scale(i));

                break;
            }
        }

        return BlockPos.containing(targetPosition);
    }

    public static Vec3 getTargetLocation(LivingEntity entity, int radius, float entityDetectionRadius) {
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition = playerPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            // Check for entities at this position
            AABB detectionBox = new AABB(targetPosition.subtract(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius),
                    targetPosition.add(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius));

            List<Entity> nearbyEntities = entity.level().getEntities(entity, detectionBox).stream().filter(
                    e -> e instanceof LivingEntity && e != entity
            ).toList();
            if (!nearbyEntities.isEmpty()) {
                return nearbyEntities.getFirst().getEyePosition().subtract(0, nearbyEntities.getFirst().getEyeHeight() / 2, 0);
            }

            // Check for blocks
            BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));

            if (!block.isAir()) {
                targetPosition = playerPosition.add(lookDirection.scale(i - 1));
                break;
            }
        }

        return targetPosition;
    }

    public static Vec3 getTargetLocation(LivingEntity entity, int radius, float entityDetectionRadius, boolean positionAtEntityFeet) {
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition = playerPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            // Check for entities at this position
            AABB detectionBox = new AABB(targetPosition.subtract(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius),
                    targetPosition.add(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius));

            List<Entity> nearbyEntities = entity.level().getEntities(entity, detectionBox).stream().filter(
                    e -> e instanceof LivingEntity && e != entity
            ).toList();
            if (!nearbyEntities.isEmpty()) {
                return positionAtEntityFeet ? nearbyEntities.getFirst().position() : nearbyEntities.getFirst().getEyePosition().subtract(0, nearbyEntities.getFirst().getEyeHeight() / 2, 0);
            }

            // Check for blocks
            BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));

            if (!block.getCollisionShape(entity.level(), BlockPos.containing(targetPosition)).isEmpty()) {
                targetPosition = playerPosition.add(lookDirection.scale(i - 1));
                break;
            }
        }

        return targetPosition;
    }

    public static Set<BlockPos> getBlocksInCircleOutline(ServerLevel level, Vec3 center,
                                                         double radius, int steps) {
        if (level == null) return Set.of();

        Set<BlockPos> blocks = new HashSet<>();

        for (int i = 0; i < steps; i++) {
            double angle = (2 * Math.PI * i) / steps;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);

            blocks.add(BlockPos.containing(x, center.y, z));
        }

        return blocks;
    }

    public static Set<BlockPos> getBlocksInCircle(ServerLevel level, Vec3 center,
                                                         double radius, int steps) {
        if (level == null) return Set.of();

        Set<BlockPos> blocks = new HashSet<>();

        for(double r = .2; r < radius + .2; r += .2) {
            for (int i = 0; i < steps; i++) {
                double angle = (2 * Math.PI * i) / steps;
                double x = center.x + r * Math.cos(angle);
                double z = center.z + r * Math.sin(angle);

                blocks.add(BlockPos.containing(x, center.y, z));
            }
        }

        return blocks;
    }



    public static @Nullable LivingEntity getTargetEntity(LivingEntity entity, int radius, float entityDetectionRadius) {
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            // Check for entities at this position
            AABB detectionBox = new AABB(targetPosition.subtract(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius),
                    targetPosition.add(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius));

            List<Entity> nearbyEntities = entity.level().getEntities(entity, detectionBox).stream().filter(
                    e -> e instanceof LivingEntity && e != entity
            ).toList();
            if (!nearbyEntities.isEmpty()) {
                return (LivingEntity) nearbyEntities.get(0);
            }

            // Check for blocks
            BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));

            if (!block.isAir()) {
                break;
            }
        }

        return null;
    }

    public static boolean damageNearbyEntities(
            ServerLevel level,
            LivingEntity source,
            double radius,
            double damage,
            Vec3 center,
            boolean ignoreSource,
            boolean distanceFalloff,
            boolean ignoreCooldown,
            int cooldownTicks) {

        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        List<LivingEntity> nearbyEntities = source.level().getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        );

        boolean hitAnyEntity = false;
        double radiusSquared = radius * radius; // Avoid sqrt calculations

        for (LivingEntity entity : nearbyEntities) {
            if (ignoreSource && entity == source) continue;

            // Calculate actual distance for spherical damage
            double distanceSquared = entity.position().distanceToSqr(center);

            // Skip entities outside the actual radius
            if (distanceSquared > radiusSquared) continue;

            // Calculate damage based on distance if enabled
            float finalDamage = (float) damage;
            if (distanceFalloff) {
                double distance = Math.sqrt(distanceSquared);
                double falloffMultiplier = Math.max(0.1, 1.0 - (distance / radius));
                finalDamage *= falloffMultiplier;
            }

            // Apply damage with appropriate damage source
            if (ignoreCooldown || entity.invulnerableTime <= 0) {
                entity.hurt(source.damageSources().explosion(null, source), finalDamage);

                // Set custom invulnerability time if specified
                if (cooldownTicks >= 0) {
                    entity.invulnerableTime = cooldownTicks;
                }

                hitAnyEntity = true;
            }
        }

        return hitAnyEntity;
    }

    public static boolean damageNearbyEntities(
            ServerLevel level,
            LivingEntity source,
            double minRadius,
            double maxRadius,
            double damage,
            Vec3 center,
            boolean ignoreSource,
            boolean distanceFalloff,
            boolean ignoreCooldown,
            int cooldownTicks,
            int fireTicks) {

        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(maxRadius, maxRadius, maxRadius),
                center.add(maxRadius, maxRadius, maxRadius)
        );

        List<LivingEntity> nearbyEntities = source.level().getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        );

        boolean hitAnyEntity = false;
        double radiusSquared = maxRadius * maxRadius; // Avoid sqrt calculations
        double minRadiusSquared = minRadius * minRadius;

        for (LivingEntity entity : nearbyEntities) {
            if (ignoreSource && entity == source) continue;

            // Calculate actual distance for spherical damage
            double distanceSquared = entity.position().distanceToSqr(center);

            // Skip entities outside the actual radius
            if (distanceSquared > radiusSquared) continue;
            if (distanceSquared < minRadiusSquared) continue;

            // Calculate damage based on distance if enabled
            float finalDamage = (float) damage;
            if (distanceFalloff) {
                double distance = Math.sqrt(distanceSquared);
                double falloffMultiplier = Math.max(0.1, 1.0 - (distance / maxRadius));
                finalDamage *= falloffMultiplier;
            }

            // Apply damage with appropriate damage source
            if (ignoreCooldown || entity.invulnerableTime <= 0) {
                entity.hurt(source.damageSources().explosion(null, source), finalDamage);
                entity.setRemainingFireTicks(entity.getRemainingFireTicks() + fireTicks);

                // Set custom invulnerability time if specified
                if (cooldownTicks >= 0) {
                    entity.invulnerableTime = cooldownTicks;
                }

                hitAnyEntity = true;
            }
        }

        return hitAnyEntity;
    }

    public static boolean damageNearbyEntities(
            ServerLevel level,
            LivingEntity source,
            double minRadius,
            double maxRadius,
            double damage,
            Vec3 center,
            boolean ignoreSource,
            boolean distanceFalloff,
            boolean ignoreCooldown,
            int cooldownTicks) {

        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(maxRadius, maxRadius, maxRadius),
                center.add(maxRadius, maxRadius, maxRadius)
        );

        List<LivingEntity> nearbyEntities = source.level().getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        );

        boolean hitAnyEntity = false;
        double radiusSquared = maxRadius * maxRadius; // Avoid sqrt calculations
        double minRadiusSquared = minRadius * minRadius;

        for (LivingEntity entity : nearbyEntities) {
            if (ignoreSource && entity == source) continue;

            // Calculate actual distance for spherical damage
            double distanceSquared = entity.position().distanceToSqr(center);

            // Skip entities outside the actual radius
            if (distanceSquared > radiusSquared) continue;
            if (distanceSquared < minRadiusSquared) continue;

            // Calculate damage based on distance if enabled
            float finalDamage = (float) damage;
            if (distanceFalloff) {
                double distance = Math.sqrt(distanceSquared);
                double falloffMultiplier = Math.max(0.1, 1.0 - (distance / maxRadius));
                finalDamage *= falloffMultiplier;
            }

            // Apply damage with appropriate damage source
            if (ignoreCooldown || entity.invulnerableTime <= 0) {
                entity.hurt(source.damageSources().explosion(null, source), finalDamage);

                // Set custom invulnerability time if specified
                if (cooldownTicks >= 0) {
                    entity.invulnerableTime = cooldownTicks;
                }

                hitAnyEntity = true;
            }
        }

        return hitAnyEntity;
    }

    public static List<LivingEntity> getNearbyEntities(@Nullable LivingEntity exclude,
                                                       ServerLevel level,
                                                       Vec3 center,
                                                       double radius) {
        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        double radiusSquared = radius * radius;

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        ).stream().filter(
                entity -> entity.position().distanceToSqr(center) <= radiusSquared
        ).filter(entity -> entity != exclude).toList();

        return nearbyEntities;
    }

    public static List<LivingEntity> getNearbyEntities(@Nullable LivingEntity exclude,
                                                       ClientLevel level,
                                                       Vec3 center,
                                                       double radius) {
        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        double radiusSquared = radius * radius;

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        ).stream().filter(
                entity -> entity.position().distanceToSqr(center) <= radiusSquared
        ).filter(entity -> entity != exclude).toList();

        return nearbyEntities;
    }

    public static boolean damageNearbyEntities(
            ServerLevel level,
            LivingEntity source,
            double radius,
            double damage,
            Vec3 center,
            boolean ignoreSource,
            boolean distanceFalloff) {

        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        List<LivingEntity> nearbyEntities = source.level().getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        );

        boolean hitAnyEntity = false;
        double radiusSquared = radius * radius; // Avoid sqrt calculations

        for (LivingEntity entity : nearbyEntities) {
            if (ignoreSource && entity == source) continue;

            // Calculate actual distance for spherical damage
            double distanceSquared = entity.position().distanceToSqr(center);

            // Skip entities outside the actual radius
            if (distanceSquared > radiusSquared) continue;

            // Calculate damage based on distance if enabled
            float finalDamage = (float) damage;
            if (distanceFalloff) {
                double distance = Math.sqrt(distanceSquared);
                double falloffMultiplier = Math.max(0.1, 1.0 - (distance / radius));
                finalDamage *= falloffMultiplier;
            }

            // Apply damage with appropriate damage source
            entity.hurt(source.damageSources().mobAttack(source), finalDamage);
            hitAnyEntity = true;
        }

        return hitAnyEntity;
    }

    public static boolean damageNearbyEntities(
            ServerLevel level,
            LivingEntity source,
            double radius,
            double damage,
            Vec3 center,
            boolean ignoreSource,
            boolean distanceFalloff,
            boolean ignoreCooldown,
            int cooldownTicks,
            int fireTicks) {

        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        List<LivingEntity> nearbyEntities = source.level().getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        );

        boolean hitAnyEntity = false;
        double radiusSquared = radius * radius; // Avoid sqrt calculations

        for (LivingEntity entity : nearbyEntities) {
            if (ignoreSource && entity == source) continue;

            // Calculate actual distance for spherical damage
            double distanceSquared = entity.position().distanceToSqr(center);

            // Skip entities outside the actual radius
            if (distanceSquared > radiusSquared) continue;

            // Calculate damage based on distance if enabled
            float finalDamage = (float) damage;
            if (distanceFalloff) {
                double distance = Math.sqrt(distanceSquared);
                double falloffMultiplier = Math.max(0.1, 1.0 - (distance / radius));
                finalDamage *= falloffMultiplier;
            }

            // Apply damage with appropriate damage source
            if (ignoreCooldown || entity.invulnerableTime <= 0) {
                entity.hurt(source.damageSources().mobAttack(source), finalDamage);

                // Set custom invulnerability time if specified
                if (cooldownTicks >= 0) {
                    entity.invulnerableTime = cooldownTicks;
                }

                // Set entity on fire if fireTicks > 0
                if (fireTicks > 0) {
                    entity.setRemainingFireTicks(fireTicks);
                }

                hitAnyEntity = true;
            }
        }

        return hitAnyEntity;
    }

    public static boolean damageNearbyEntities(
            ServerLevel level,
            LivingEntity source,
            double radius,
            double damage,
            Vec3 center,
            boolean ignoreSource,
            boolean distanceFalloff,
            int fireTicks) {

        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        List<LivingEntity> nearbyEntities = source.level().getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        );

        boolean hitAnyEntity = false;
        double radiusSquared = radius * radius; // Avoid sqrt calculations

        for (LivingEntity entity : nearbyEntities) {
            if (ignoreSource && entity == source) continue;

            // Calculate actual distance for spherical damage
            double distanceSquared = entity.position().distanceToSqr(center);

            // Skip entities outside the actual radius
            if (distanceSquared > radiusSquared) continue;

            // Calculate damage based on distance if enabled
            float finalDamage = (float) damage;
            if (distanceFalloff) {
                double distance = Math.sqrt(distanceSquared);
                double falloffMultiplier = Math.max(0.1, 1.0 - (distance / radius));
                finalDamage *= falloffMultiplier;
            }

            // Apply damage with appropriate damage source
            entity.hurt(source.damageSources().mobAttack(source), finalDamage);

            // Set entity on fire if fireTicks > 0
            if (fireTicks > 0) {
                entity.setRemainingFireTicks(fireTicks);
            }

            hitAnyEntity = true;
        }

        return hitAnyEntity;
    }


    public static void addPotionEffectToNearbyEntities(ServerLevel level, LivingEntity entity, double radius, Vec3 pos, MobEffectInstance... mobEffectInstances) {
        List<LivingEntity> nearbyEntities = getNearbyEntities(entity, level, pos, radius);
        for (LivingEntity nearbyEntity : nearbyEntities) {
            if (!nearbyEntity.isAlive() || nearbyEntity.isInvulnerableTo(entity.damageSources().mobAttack(entity))) {
                continue;
            }
            for (MobEffectInstance effect : mobEffectInstances) {
                if (effect != null && !nearbyEntity.hasEffect(effect.getEffect())) {
                    nearbyEntity.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
                }
            }
        }
    }
}

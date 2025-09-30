package de.jakob.lotm.util.helper;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

            if (!block.getCollisionShape(entity.level(), BlockPos.containing(targetPosition)).isEmpty()) {
                targetPosition = playerPosition.add(lookDirection.scale(i - 1));
                break;
            }
        }

        return BlockPos.containing(targetPosition);
    }

    public static double distanceToGround(Level level, Entity entity) {
        Vec3 startPos = entity.position();

        BlockPos pos = BlockPos.containing(startPos.x, startPos.y, startPos.z);

        for(int i = 0; i < 500; i++) {
            Vec3 currentPos = startPos.subtract(0, i * .5, 0);
            pos = BlockPos.containing(currentPos.x, currentPos.y, currentPos.z);
            BlockState block = level.getBlockState(pos);
            if(!block.getCollisionShape(entity.level(), pos).isEmpty())
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

    public static BlockPos getTargetBlock(LivingEntity entity, double radius, boolean oneBlockBefore, boolean includePassableBlocks) {
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition = playerPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));

            if ((!block.getCollisionShape(entity.level(), BlockPos.containing(targetPosition)).isEmpty() && !includePassableBlocks) || !block.isAir()) {
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

            if (!block.getCollisionShape(entity.level(), BlockPos.containing(targetPosition)).isEmpty() && i >= minRadius) {
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
        if(entity instanceof BeyonderNPCEntity customPlayer) {
            if(customPlayer.getCurrentTarget() != null)
                return customPlayer.getCurrentTarget().getEyePosition().subtract(0, customPlayer.getCurrentTarget().getEyeHeight() / 2, 0);
        }

        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition = playerPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            // Check for entities at this position
            AABB detectionBox = new AABB(targetPosition.subtract(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius),
                    targetPosition.add(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius));

            List<Entity> nearbyEntities = entity.level().getEntities(entity, detectionBox).stream().filter(
                    e -> e instanceof LivingEntity && e != entity && mayTarget(entity, (LivingEntity) e)
            ).toList();
            if (!nearbyEntities.isEmpty()) {
                return nearbyEntities.getFirst().getEyePosition().subtract(0, nearbyEntities.getFirst().getEyeHeight() / 2, 0);
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

    public static Vec3 getTargetLocation(LivingEntity entity, int radius, float entityDetectionRadius, boolean positionAtEntityFeet) {
        if(entity instanceof BeyonderNPCEntity customPlayer) {
            if(customPlayer.getCurrentTarget() != null)
                return positionAtEntityFeet ? customPlayer.getCurrentTarget().position() : customPlayer.getCurrentTarget().getEyePosition().subtract(0, customPlayer.getCurrentTarget().getEyeHeight() / 2, 0);
        }

        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition = playerPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            // Check for entities at this position
            AABB detectionBox = new AABB(targetPosition.subtract(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius),
                    targetPosition.add(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius));

            List<Entity> nearbyEntities = entity.level().getEntities(entity, detectionBox).stream().filter(
                    e -> e instanceof LivingEntity && e != entity && mayTarget(entity, (LivingEntity) e)
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

    public static Set<BlockPos> getBlocksInCircle(ServerLevel level, Vec3 center, double radius) {
        if (level == null) return Set.of();

        Set<BlockPos> blocks = new HashSet<>();

        double stepSize = 0.5; // distance between points on circumference, tweak for density

        for (double r = 0.2; r < radius + 0.2; r += 0.2) {
            // Circumference at this radius
            double circumference = 2 * Math.PI * r;

            // Calculate steps so points are spaced ~stepSize apart
            int steps = Math.max(6, (int) Math.ceil(circumference / stepSize));

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
        if(entity instanceof BeyonderNPCEntity customPlayer) {
            if(customPlayer.getCurrentTarget() != null)
                return customPlayer.getCurrentTarget();
        }

        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            // Check for entities at this position
            AABB detectionBox = new AABB(targetPosition.subtract(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius),
                    targetPosition.add(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius));

            List<Entity> nearbyEntities = entity.level().getEntities(entity, detectionBox).stream().filter(
                    e -> e instanceof LivingEntity && e != entity && mayTarget(entity, (LivingEntity) e)
            ).toList();
            if (!nearbyEntities.isEmpty()) {
                return (LivingEntity) nearbyEntities.get(0);
            }

            // Check for blocks
            BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));

            if (!block.getCollisionShape(entity.level(), BlockPos.containing(targetPosition)).isEmpty()) {
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
        ).stream().filter(e -> mayTarget(source, e)).toList();

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
                finalDamage *= (float) falloffMultiplier;
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

    public static boolean mayDamage(LivingEntity source, LivingEntity target) {
        if (source == target) return false;
        if (target instanceof Player player && player.isCreative()) return false;
        if (!source.canAttack(target)) return false;
        MarionetteComponent component = source.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if(component.isMarionette()) {
            if(target.getUUID().toString().equals(component.getControllerUUID()))
                return false;
        }
        return true;
    }

    public static boolean mayTarget(LivingEntity source, LivingEntity target) {
        if(!mayDamage(source, target)) return false;

        MarionetteComponent component = target.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if(component.isMarionette()) {
            if(source.getUUID().toString().equals(component.getControllerUUID()))
                return false;

            MarionetteComponent sourceComponent = source.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if(sourceComponent.isMarionette() && sourceComponent.getControllerUUID().equals(component.getControllerUUID()))
                return false;
        }
        return true;
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
        ).stream().filter(e -> mayTarget(source, e)).toList();

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
        ).stream().filter(e -> mayTarget(source, e)).toList();

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

        return level.getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        ).stream().filter(e -> !(e instanceof Player player) || !player.isCreative())
                .filter(entity -> entity.position().distanceToSqr(center) <= radiusSquared)
                .filter(entity -> entity != exclude)
                .filter(e -> exclude == null || mayTarget(exclude, e)).toList();
    }

    public static List<LivingEntity> getNearbyEntities(@Nullable LivingEntity exclude,
                                                       ServerLevel level,
                                                       Vec3 center,
                                                       double radius, boolean allowCreativeMode) {
        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        double radiusSquared = radius * radius;

        return level.getEntitiesOfClass(
                        LivingEntity.class,
                        detectionBox
                ).stream().filter(e -> !(e instanceof Player player) || (!player.isCreative() || allowCreativeMode))
                .filter(entity -> entity.position().distanceToSqr(center) <= radiusSquared)
                .filter(entity -> entity != exclude)
                .filter(e -> exclude == null || mayTarget(exclude, e)).toList();
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

        return level.getEntitiesOfClass(
                LivingEntity.class,
                detectionBox
        ).stream().filter(e -> !(e instanceof Player player) || !player.isCreative())
                .filter(entity -> entity.position().distanceToSqr(center) <= radiusSquared)
                .filter(entity -> entity != exclude)
                .filter(e -> exclude == null || mayTarget(exclude, e)).toList();
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
        ).stream().filter(e -> mayTarget(source, e)).toList();

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
        ).stream().filter(e -> mayTarget(source, e)).toList();

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
        ).stream().filter(e -> mayTarget(source, e)).toList();

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

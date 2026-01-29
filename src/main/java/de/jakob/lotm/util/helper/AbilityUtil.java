package de.jakob.lotm.util.helper;

import de.jakob.lotm.abilities.error.DeceitAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ParasitationComponent;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    public static int getSequenceDifference(LivingEntity source, LivingEntity target) {
        if(!BeyonderData.isBeyonder(source) && !BeyonderData.isBeyonder(target)) {
            return 0;
        }

        if(BeyonderData.isBeyonder(source) && !BeyonderData.isBeyonder(target)) {
            return 10 - BeyonderData.getSequence(source);
        }
        else if(!BeyonderData.isBeyonder(source) && BeyonderData.isBeyonder(target)) {
            return 10 - BeyonderData.getSequence(target);
        }

        return BeyonderData.getSequence(target) - BeyonderData.getSequence(source);
    }

    public static boolean isTargetSignificantlyWeaker(LivingEntity source, LivingEntity target) {
        if(!BeyonderData.isBeyonder(source)) {
            return false;
        }

        if(!BeyonderData.isBeyonder(target)) {
            return true;
        }

        int sourceSequence = BeyonderData.getSequence(source);
        int targetSequence = BeyonderData.getSequence(target);

        if(sourceSequence <= 4 && targetSequence > 4) {
            return true;
        }

        if(sourceSequence <= 2 && targetSequence > 2) {
            return true;
        }

        if(sourceSequence == 0 && targetSequence > 0) {
            return true;
        }

        return false;
    }

    public static boolean isTargetSignificantlyStronger(LivingEntity source, LivingEntity target) {
        if(!BeyonderData.isBeyonder(target)) {
            return false;
        }

        if(!BeyonderData.isBeyonder(source)) {
            return true;
        }

        int sourceSequence = BeyonderData.getSequence(source);
        int targetSequence = BeyonderData.getSequence(target);

        if(targetSequence <= 4 && sourceSequence > 4) {
            return true;
        }

        if(targetSequence <= 2 && sourceSequence > 2) {
            return true;
        }

        if(targetSequence == 0 && sourceSequence > 0) {
            return true;
        }

        return false;
    }

    public static void sendActionBar(LivingEntity entity, Component message) {
        if(!(entity instanceof ServerPlayer player)) {
            return;
        }
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);
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
            if(customPlayer.getCurrentTarget() != null && customPlayer.getCurrentTarget().distanceTo(entity) <= radius)
                return customPlayer.getCurrentTarget().getEyePosition().subtract(0, customPlayer.getCurrentTarget().getEyeHeight() / 2, 0);
        }

        if(entity instanceof Mob mob) {
            if(mob.getTarget() != null && mob.getTarget().distanceTo(entity) <= radius)
                return mob.getTarget().getEyePosition().subtract(0, mob.getTarget().getEyeHeight() / 2, 0);
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
            if(customPlayer.getCurrentTarget() != null && customPlayer.getCurrentTarget().distanceTo(entity) <= radius)
                return positionAtEntityFeet ? customPlayer.getCurrentTarget().position() : customPlayer.getCurrentTarget().getEyePosition().subtract(0, customPlayer.getCurrentTarget().getEyeHeight() / 2, 0);
        }

        if(entity instanceof Mob mob) {
            if(mob.getTarget() != null && mob.getTarget().distanceTo(entity) <= radius)
                return mob.getTarget().getEyePosition().subtract(0, mob.getTarget().getEyeHeight() / 2, 0);
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

    public static List<BlockPos> getBlocksInEllipsoid(
            ClientLevel level,
            Vec3 center,
            double xzRadius,
            double yRadius,
            boolean filled,
            boolean excludeEmptyBlocks,
            boolean onlyExposed
    ) {
        if (level == null) return List.of();

        List<BlockPos> blocks = new ArrayList<>();

        double maxRadius = Math.max(xzRadius, yRadius);
        int steps = (int) Math.max(20, 4 * Math.PI * maxRadius * maxRadius);

        if (filled) {
            int minX = Mth.floor(center.x - xzRadius);
            int maxX = Mth.ceil(center.x + xzRadius);
            int minY = Mth.floor(center.y - yRadius);
            int maxY = Mth.ceil(center.y + yRadius);
            int minZ = Mth.floor(center.z - xzRadius);
            int maxZ = Mth.ceil(center.z + xzRadius);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double dx = (x + 0.5 - center.x) / xzRadius;
                        double dy = (y + 0.5 - center.y) / yRadius;
                        double dz = (z + 0.5 - center.z) / xzRadius;

                        // Equation for ellipsoid: (x/a)^2 + (y/b)^2 + (z/a)^2 <= 1
                        if (dx * dx + dy * dy + dz * dz <= 1.0) {
                            BlockPos pos = new BlockPos(x, y, z);

                            if (excludeEmptyBlocks) {
                                if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
                                    continue;
                            }

                            if (onlyExposed) {
                                BlockPos above = pos.above();
                                if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty())
                                    continue;
                            }

                            blocks.add(pos);
                        }
                    }
                }
            }
        } else {
            RandomSource random = level.random;

            for (int i = 0; i < steps; i++) {
                double theta = 2 * Math.PI * random.nextDouble();
                double phi = Math.acos(2 * random.nextDouble() - 1);

                // Parametric ellipsoid equation
                double x = center.x + xzRadius * Math.sin(phi) * Math.cos(theta);
                double y = center.y + yRadius * Math.sin(phi) * Math.sin(theta);
                double z = center.z + xzRadius * Math.cos(phi);

                BlockPos pos = BlockPos.containing(x, y, z);

                if (excludeEmptyBlocks) {
                    if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
                        continue;
                }

                if (onlyExposed) {
                    BlockPos above = pos.above();
                    if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty())
                        continue;
                }

                blocks.add(pos);
            }
        }

        return blocks;
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

    public static Set<BlockPos> getBlocksInCircleOutline(ServerLevel level, Vec3 center,
                                                         double radius) {
        if (level == null) return Set.of();

        Set<BlockPos> blocks = new HashSet<>();

        double circumference = 2 * Math.PI * radius;

        // Calculate steps so points are spaced ~stepSize apart
        int steps = Math.max(6, (int) Math.ceil(circumference * 2));

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
            if(customPlayer.getCurrentTarget() != null && customPlayer.getCurrentTarget().distanceTo(entity) <= radius)
                return customPlayer.getCurrentTarget();
        }

        if(entity instanceof Mob mob) {
            if(mob.getTarget() != null && mob.getTarget().distanceTo(entity) <= radius)
                return mob.getTarget();
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


    public static @Nullable LivingEntity getTargetEntity(LivingEntity entity, int radius, float entityDetectionRadius, boolean onlyAllowWithLineOfSight) {
        if(!onlyAllowWithLineOfSight) {
            if(entity instanceof BeyonderNPCEntity customPlayer) {
                if(customPlayer.getCurrentTarget() != null && customPlayer.getCurrentTarget().distanceTo(entity) <= radius)
                    return customPlayer.getCurrentTarget();
            }

            if(entity instanceof Mob mob) {
                if(mob.getTarget() != null && mob.getTarget().distanceTo(entity) <= radius)
                    return mob.getTarget();
            }
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

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
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
                entity.hurt(entity.damageSources().mobAttack(source), finalDamage);

                // Set custom invulnerability time if specified
                if (cooldownTicks >= 0) {
                    entity.invulnerableTime = cooldownTicks;
                }

                hitAnyEntity = true;
            }
        }

        return hitAnyEntity;
    }

    public static List<BlockPos> getBlocksInSphereRadius(ServerLevel level, Vec3 center, double radius, boolean filled) {
        if (level == null) return List.of();

        List<BlockPos> blocks = new ArrayList<>();

        int steps = (int) Math.max(20, 4 * Math.PI * radius * radius);

        if (filled) {
            int minX = Mth.floor(center.x - radius);
            int maxX = Mth.ceil(center.x + radius);
            int minY = Mth.floor(center.y - radius);
            int maxY = Mth.ceil(center.y + radius);
            int minZ = Mth.floor(center.z - radius);
            int maxZ = Mth.ceil(center.z + radius);

            double rSq = radius * radius;

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double dx = x + 0.5 - center.x;
                        double dy = y + 0.5 - center.y;
                        double dz = z + 0.5 - center.z;
                        if (dx * dx + dy * dy + dz * dz <= rSq) {
                            blocks.add(new BlockPos(x, y, z));
                        }
                    }
                }
            }
        } else {
            RandomSource random = level.random;

            for (int i = 0; i < steps; i++) {
                double theta = 2 * Math.PI * random.nextDouble();
                double phi = Math.acos(2 * random.nextDouble() - 1);

                double x = center.x + radius * Math.sin(phi) * Math.cos(theta);
                double y = center.y + radius * Math.sin(phi) * Math.sin(theta);
                double z = center.z + radius * Math.cos(phi);

                blocks.add(BlockPos.containing(x, y, z));
            }
        }

        return blocks;
    }


    public static List<BlockPos> getBlocksInSphereRadius(Level level, Vec3 center, double radius, boolean filled, boolean excludeEmptyBlocks, boolean onlyExposed) {
        if (level == null) return List.of();

        List<BlockPos> blocks = new ArrayList<>();

        int steps = (int) Math.max(20, 4 * Math.PI * radius * radius);

        if (filled) {
            int minX = Mth.floor(center.x - radius);
            int maxX = Mth.ceil(center.x + radius);
            int minY = Mth.floor(center.y - radius);
            int maxY = Mth.ceil(center.y + radius);
            int minZ = Mth.floor(center.z - radius);
            int maxZ = Mth.ceil(center.z + radius);

            double rSq = radius * radius;

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double dx = x + 0.5 - center.x;
                        double dy = y + 0.5 - center.y;
                        double dz = z + 0.5 - center.z;
                        if (dx * dx + dy * dy + dz * dz <= rSq) {
                            if(excludeEmptyBlocks) {
                                BlockPos pos = new BlockPos(x, y, z);
                                if(level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
                                    continue;
                            }
                            if(onlyExposed) {
                                BlockPos pos = new BlockPos(x, y, z);
                                if(!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty())
                                    continue;
                            }
                            blocks.add(new BlockPos(x, y, z));
                        }
                    }
                }
            }
        } else {
            RandomSource random = level.random;

            for (int i = 0; i < steps; i++) {
                double theta = 2 * Math.PI * random.nextDouble();
                double phi = Math.acos(2 * random.nextDouble() - 1);

                double x = center.x + radius * Math.sin(phi) * Math.cos(theta);
                double y = center.y + radius * Math.sin(phi) * Math.sin(theta);
                double z = center.z + radius * Math.cos(phi);

                if(excludeEmptyBlocks) {
                    BlockPos pos = BlockPos.containing(x, y, z);
                    if(level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
                        continue;
                }
                if(onlyExposed) {
                    BlockPos pos = BlockPos.containing(x, y, z);
                    if(!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty())
                        continue;
                }
                blocks.add(BlockPos.containing(x, y, z));
            }
        }

        return blocks;
    }

    public static List<BlockPos> getBlocksInEllipsoid(
            ServerLevel level,
            Vec3 center,
            double xzRadius,
            double yRadius,
            boolean filled,
            boolean excludeEmptyBlocks,
            boolean onlyExposed
    ) {
        if (level == null) return List.of();

        List<BlockPos> blocks = new ArrayList<>();

        double maxRadius = Math.max(xzRadius, yRadius);
        int steps = (int) Math.max(20, 4 * Math.PI * maxRadius * maxRadius);

        if (filled) {
            int minX = Mth.floor(center.x - xzRadius);
            int maxX = Mth.ceil(center.x + xzRadius);
            int minY = Mth.floor(center.y - yRadius);
            int maxY = Mth.ceil(center.y + yRadius);
            int minZ = Mth.floor(center.z - xzRadius);
            int maxZ = Mth.ceil(center.z + xzRadius);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double dx = (x + 0.5 - center.x) / xzRadius;
                        double dy = (y + 0.5 - center.y) / yRadius;
                        double dz = (z + 0.5 - center.z) / xzRadius;

                        // Equation for ellipsoid: (x/a)^2 + (y/b)^2 + (z/a)^2 <= 1
                        if (dx * dx + dy * dy + dz * dz <= 1.0) {
                            BlockPos pos = new BlockPos(x, y, z);

                            if (excludeEmptyBlocks) {
                                if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
                                    continue;
                            }

                            if (onlyExposed) {
                                BlockPos above = pos.above();
                                if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty())
                                    continue;
                            }

                            blocks.add(pos);
                        }
                    }
                }
            }
        } else {
            RandomSource random = level.random;

            for (int i = 0; i < steps; i++) {
                double theta = 2 * Math.PI * random.nextDouble();
                double phi = Math.acos(2 * random.nextDouble() - 1);

                // Parametric ellipsoid equation
                double x = center.x + xzRadius * Math.sin(phi) * Math.cos(theta);
                double y = center.y + yRadius * Math.sin(phi) * Math.sin(theta);
                double z = center.z + xzRadius * Math.cos(phi);

                BlockPos pos = BlockPos.containing(x, y, z);

                if (excludeEmptyBlocks) {
                    if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
                        continue;
                }

                if (onlyExposed) {
                    BlockPos above = pos.above();
                    if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty())
                        continue;
                }

                blocks.add(pos);
            }
        }

        return blocks;
    }



    /**
     * Updated mayDamage method that respects ally relationships
     * Replace your existing mayDamage method with this one
     */
    public static boolean mayDamage(LivingEntity source, LivingEntity target) {
        if(source == null || target == null) return true;
        if (source == target) return false;
        if (target instanceof Player player && player.isCreative()) return false;
        if (!source.canAttack(target)) return false;

        // Check ally relationship - allies cannot damage each other
        if (AllyUtil.areAllies(source, target)) {
            return false;
        }

        if(source instanceof AvatarEntity avatar) {
            if(target.getUUID() == avatar.getOriginalOwner())
                return false;
        }

        ParasitationComponent parasitationComponent = target.getData(ModAttachments.PARASITE_COMPONENT.get());
        if(parasitationComponent.isParasited()) {
            if(parasitationComponent.getParasiteUUID().equals(source.getUUID()))
                return false;
        }

        parasitationComponent = source.getData(ModAttachments.PARASITE_COMPONENT.get());
        if(parasitationComponent.isParasited()) {
            if(parasitationComponent.getParasiteUUID().equals(target.getUUID()))
                return false;
        }

        MarionetteComponent component = source.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if(component.isMarionette()) {
            if(target.getUUID().toString().equals(component.getControllerUUID()))
                return false;

            MarionetteComponent targetComponent = target.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if(targetComponent.isMarionette() && targetComponent.getControllerUUID().equals(component.getControllerUUID()))
                return false;
        }

        SubordinateComponent subordinateComponent = source.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if(subordinateComponent.isSubordinate()) {
            if(target.getUUID().toString().equals(component.getControllerUUID()))
                return false;

            SubordinateComponent targetSubordinateComponent = target.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
            if(targetSubordinateComponent.isSubordinate() && targetSubordinateComponent.getControllerUUID().equals(subordinateComponent.getControllerUUID()))
                return false;
        }
        return true;
    }

    /**
     * Updated mayTarget method that respects ally relationships
     * Replace your existing mayTarget method with this one
     */
    public static boolean mayTarget(LivingEntity source, LivingEntity target) {
        if(!mayDamage(source, target)) return false;

        if(source == null || target == null) return true;

        // Check ally relationship - allies cannot target each other
        if (AllyUtil.areAllies(source, target)) {
            return false;
        }

        if(DeceitAbility.cannotBeTargeted.contains(target.getUUID())) return false;

        MarionetteComponent component = target.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if(component.isMarionette()) {
            if(source.getUUID().toString().equals(component.getControllerUUID()))
                return false;

            MarionetteComponent sourceComponent = source.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if(sourceComponent.isMarionette() && sourceComponent.getControllerUUID().equals(component.getControllerUUID()))
                return false;
        }

        SubordinateComponent subordinateComponent = target.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if(subordinateComponent.isSubordinate()) {
            if(source.getUUID().toString().equals(subordinateComponent.getControllerUUID()))
                return false;

            SubordinateComponent sourceSubordinateComponent = source.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
            if(sourceSubordinateComponent.isSubordinate() && sourceSubordinateComponent.getControllerUUID().equals(subordinateComponent.getControllerUUID()))
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

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
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
                entity.hurt(entity.damageSources().mobAttack(source), finalDamage);
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

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
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
                entity.hurt(entity.damageSources().mobAttack(source), finalDamage);

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

    public static List<Entity> getAllNearbyEntities(@Nullable LivingEntity exclude, ServerLevel level, Vec3 center, double radius) {
        // Create detection box slightly larger than radius for efficiency
        AABB detectionBox = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        double radiusSquared = radius * radius;

        return level.getEntitiesOfClass(
                        Entity.class,
                        detectionBox
                ).stream().filter(e -> !(e instanceof Player player) || !player.isCreative())
                .filter(entity -> entity.position().distanceToSqr(center) <= radiusSquared)
                .filter(entity -> entity != exclude)
                .filter(e -> exclude == null || (!(e instanceof LivingEntity l) || mayTarget(exclude, l))).toList();
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

    public static List<Entity> getAllNearbyEntities(@Nullable LivingEntity exclude,
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
                        Entity.class,
                        detectionBox
                ).stream().filter(e -> !(e instanceof Player player) || (!player.isCreative() || allowCreativeMode))
                .filter(entity -> entity.position().distanceToSqr(center) <= radiusSquared)
                .filter(entity -> entity != exclude)
                .filter(e -> exclude == null || (e instanceof LivingEntity le && mayTarget(exclude, le))).toList();
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

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
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
            if(source != null)
                entity.hurt(source.damageSources().mobAttack(source), finalDamage);
            else
                entity.hurt(entity.damageSources().generic(), finalDamage);
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

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
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
                if(source != null)
                    entity.hurt(source.damageSources().mobAttack(source), finalDamage);
                else
                    entity.hurt(entity.damageSources().generic(), finalDamage);

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

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
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
            if(source != null)
                entity.hurt(source.damageSources().mobAttack(source), finalDamage);
            else
                entity.hurt(entity.damageSources().generic(), finalDamage);

            // Set entity on fire if fireTicks > 0
            if (fireTicks > 0) {
                entity.setRemainingFireTicks(fireTicks);
            }

            hitAnyEntity = true;
        }

        return hitAnyEntity;
    }


    public static void addPotionEffectToNearbyEntities(ServerLevel level, @Nullable LivingEntity entity, double radius, Vec3 pos, MobEffectInstance... mobEffectInstances) {
        List<LivingEntity> nearbyEntities = getNearbyEntities(entity, level, pos, radius);
        for (LivingEntity nearbyEntity : nearbyEntities) {
            if (!nearbyEntity.isAlive() || (entity != null && nearbyEntity.isInvulnerableTo(entity.damageSources().mobAttack(entity)))) {
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

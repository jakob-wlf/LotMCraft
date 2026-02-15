package de.jakob.lotm.util.helper;

import de.jakob.lotm.abilities.error.DeceitAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ParasitationComponent;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.events.custom.TargetEntityEvent;
import de.jakob.lotm.events.custom.TargetLocationEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
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
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbilityUtil {

    // ThreadLocal flag to prevent firing TargetEntityEvent when called from getTargetLocation
    private static final ThreadLocal<Boolean> INSIDE_GET_TARGET_LOCATION = ThreadLocal.withInitial(() -> false);

    // ==================== SEQUENCE UTILITY METHODS ====================

    public static int getSequenceDifference(LivingEntity source, LivingEntity target) {
        boolean sourceIsBeyonder = BeyonderData.isBeyonder(source);
        boolean targetIsBeyonder = BeyonderData.isBeyonder(target);

        if (!sourceIsBeyonder && !targetIsBeyonder) {
            return 0;
        }

        if (sourceIsBeyonder && !targetIsBeyonder) {
            return 10 - BeyonderData.getSequence(source);
        }

        if (!sourceIsBeyonder && targetIsBeyonder) {
            return 10 - BeyonderData.getSequence(target);
        }

        return BeyonderData.getSequence(target) - BeyonderData.getSequence(source);
    }

    public static boolean isTargetSignificantlyWeaker(LivingEntity source, LivingEntity target) {
        if (!BeyonderData.isBeyonder(source)) {
            return false;
        }

        if (!BeyonderData.isBeyonder(target)) {
            return true;
        }

        int sourceSequence = BeyonderData.getSequence(source);
        int targetSequence = BeyonderData.getSequence(target);

        return isSequenceSignificantlyHigher(targetSequence, sourceSequence);
    }

    public static boolean isTargetSignificantlyStronger(LivingEntity source, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) {
            return false;
        }

        if (!BeyonderData.isBeyonder(source)) {
            return true;
        }

        int sourceSequence = BeyonderData.getSequence(source);
        int targetSequence = BeyonderData.getSequence(target);

        return isSequenceSignificantlyHigher(sourceSequence, targetSequence);
    }

    private static boolean isSequenceSignificantlyHigher(int higher, int lower) {
        if (lower <= 4 && higher > 4) return true;
        if (lower <= 2 && higher > 2) return true;
        if (lower == 0 && higher > 0) return true;
        return false;
    }

    // ==================== TARGETING VALIDATION METHODS ====================

    /**
     * Checks if the source entity may damage the target entity.
     * This includes checking for ally relationships, controller relationships, and game mode.
     */
    public static boolean mayDamage(LivingEntity source, LivingEntity target) {
        if (source == null || target == null) return true;
        if (source == target) return false;
        if (target instanceof Player player && player.isCreative()) return false;
        if (!source.canAttack(target)) return false;

        // Check ally relationship - allies cannot damage each other
        if (AllyUtil.areAllies(source, target)) {
            return false;
        }

        // Avatar cannot damage original owner
        if (source instanceof AvatarEntity avatar && target.getUUID() == avatar.getOriginalOwner()) {
            return false;
        }

        // Parasitation checks
        if (!mayDamageParasitation(source, target)) {
            return false;
        }

        // Marionette checks
        if (!mayDamageMarionette(source, target)) {
            return false;
        }

        // Subordinate checks
        if (!mayDamageSubordinate(source, target)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the source entity may target the target entity.
     * This is stricter than mayDamage and includes additional targeting restrictions.
     */
    public static boolean mayTarget(LivingEntity source, LivingEntity target) {
        return mayTarget(source, target, false);
    }

    /**
     * Checks if the source entity may target the target entity.
     * @param allowAllies If true, allows targeting allies (for support abilities)
     */
    public static boolean mayTarget(LivingEntity source, LivingEntity target, boolean allowAllies) {
        if (source == null || target == null) return true;

        // If we're allowing allies for support abilities, skip the mayDamage check
        if (!allowAllies && !mayDamage(source, target)) {
            return false;
        }

        // Still check these even for support abilities
        if (DeceitAbility.cannotBeTargeted.contains(target.getUUID())) {
            return false;
        }

        // When allowing allies, skip controller/subordinate checks as they're allies
        if (allowAllies) {
            return true;
        }

        // Marionette targeting restrictions
        if (!mayTargetMarionette(source, target)) {
            return false;
        }

        // Subordinate targeting restrictions
        if (!mayTargetSubordinate(source, target)) {
            return false;
        }

        return true;
    }

    // ==================== HELPER METHODS FOR TARGETING VALIDATION ====================

    private static boolean mayDamageParasitation(LivingEntity source, LivingEntity target) {
        ParasitationComponent targetParasitation = target.getData(ModAttachments.PARASITE_COMPONENT.get());
        if (targetParasitation.isParasited() && targetParasitation.getParasiteUUID().equals(source.getUUID())) {
            return false;
        }

        ParasitationComponent sourceParasitation = source.getData(ModAttachments.PARASITE_COMPONENT.get());
        if (sourceParasitation.isParasited() && sourceParasitation.getParasiteUUID().equals(target.getUUID())) {
            return false;
        }

        return true;
    }

    private static boolean mayDamageMarionette(LivingEntity source, LivingEntity target) {
        MarionetteComponent sourceComponent = source.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!sourceComponent.isMarionette()) {
            return true;
        }

        if (target.getUUID().toString().equals(sourceComponent.getControllerUUID())) {
            return false;
        }

        MarionetteComponent targetComponent = target.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (targetComponent.isMarionette() &&
                targetComponent.getControllerUUID().equals(sourceComponent.getControllerUUID())) {
            return false;
        }

        return true;
    }

    private static boolean mayDamageSubordinate(LivingEntity source, LivingEntity target) {
        SubordinateComponent sourceComponent = source.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (!sourceComponent.isSubordinate()) {
            return true;
        }

        if (target.getUUID().toString().equals(sourceComponent.getControllerUUID())) {
            return false;
        }

        SubordinateComponent targetComponent = target.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (targetComponent.isSubordinate() &&
                targetComponent.getControllerUUID().equals(sourceComponent.getControllerUUID())) {
            return false;
        }

        return true;
    }

    private static boolean mayTargetMarionette(LivingEntity source, LivingEntity target) {
        MarionetteComponent targetComponent = target.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!targetComponent.isMarionette()) {
            return true;
        }

        if (source.getUUID().toString().equals(targetComponent.getControllerUUID())) {
            return false;
        }

        MarionetteComponent sourceComponent = source.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (sourceComponent.isMarionette() &&
                sourceComponent.getControllerUUID().equals(targetComponent.getControllerUUID())) {
            return false;
        }

        return true;
    }

    private static boolean mayTargetSubordinate(LivingEntity source, LivingEntity target) {
        SubordinateComponent targetComponent = target.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (!targetComponent.isSubordinate()) {
            return true;
        }

        if (source.getUUID().toString().equals(targetComponent.getControllerUUID())) {
            return false;
        }

        SubordinateComponent sourceComponent = source.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (sourceComponent.isSubordinate() &&
                sourceComponent.getControllerUUID().equals(targetComponent.getControllerUUID())) {
            return false;
        }

        return true;
    }

    // ==================== TARGET BLOCK METHODS ====================

    public static BlockPos getTargetBlock(LivingEntity entity, int radius) {
        return getTargetBlock(entity, radius, false);
    }

    public static BlockPos getTargetBlock(LivingEntity entity, double radius, boolean oneBlockBefore) {
        return getTargetBlock(entity, 0, radius, oneBlockBefore, false);
    }

    public static BlockPos getTargetBlock(LivingEntity entity, double radius, boolean oneBlockBefore, boolean includePassableBlocks) {
        return getTargetBlock(entity, 0, radius, oneBlockBefore, includePassableBlocks);
    }

    public static BlockPos getTargetBlock(LivingEntity entity, double minRadius, double maxRadius, boolean oneBlockBefore) {
        return getTargetBlock(entity, minRadius, maxRadius, oneBlockBefore, false);
    }

    /**
     * Core method for finding target blocks with raycasting
     */
    private static BlockPos getTargetBlock(LivingEntity entity, double minRadius, double maxRadius,
                                           boolean oneBlockBefore, boolean includePassableBlocks) {
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 startPosition = entity.position().add(0, entity.getEyeHeight(), 0);
        Vec3 targetPosition = startPosition;

        for (int i = 0; i < maxRadius; i++) {
            targetPosition = startPosition.add(lookDirection.scale(i));
            BlockPos pos = BlockPos.containing(targetPosition);
            BlockState block = entity.level().getBlockState(pos);

            boolean hitBlock = includePassableBlocks
                    ? !block.isAir()
                    : !block.getCollisionShape(entity.level(), pos).isEmpty();

            if (hitBlock && i >= minRadius) {
                int offset = oneBlockBefore ? i - 1 : i;
                return BlockPos.containing(startPosition.add(lookDirection.scale(offset)));
            }
        }

        return BlockPos.containing(targetPosition);
    }

    // ==================== TARGET ENTITY METHODS ====================

    @Nullable
    public static LivingEntity getTargetEntity(LivingEntity entity, int radius, float entityDetectionRadius) {
        return getTargetEntity(entity, radius, entityDetectionRadius, false, false);
    }

    @Nullable
    public static LivingEntity getTargetEntity(LivingEntity entity, int radius, float entityDetectionRadius,
                                               boolean onlyAllowWithLineOfSight) {
        return getTargetEntity(entity, radius, entityDetectionRadius, onlyAllowWithLineOfSight, false);
    }

    /**
     * Core method for finding target entities with raycasting
     * @param entity The source entity
     * @param radius Maximum search distance
     * @param entityDetectionRadius Detection radius for entities along the ray
     * @param onlyAllowWithLineOfSight If true, ignores current target and only uses line of sight
     * @param allowAllies If true, allows targeting allies (for support abilities)
     */
    @Nullable
    public static LivingEntity getTargetEntity(LivingEntity entity, int radius, float entityDetectionRadius,
                                               boolean onlyAllowWithLineOfSight, boolean allowAllies) {
        LivingEntity targetEntity = getTargetEntityInternal(entity, radius, entityDetectionRadius,
                onlyAllowWithLineOfSight, allowAllies);

        // Only fire event if we're not being called from getTargetLocation
        if (!INSIDE_GET_TARGET_LOCATION.get()) {
            return fireTargetEntityEvent(entity, radius, entityDetectionRadius,
                    onlyAllowWithLineOfSight, allowAllies, targetEntity);
        }

        return targetEntity;
    }

    /**
     * Internal implementation of target entity finding without event firing
     */
    @Nullable
    private static LivingEntity getTargetEntityInternal(LivingEntity entity, int radius, float entityDetectionRadius,
                                                        boolean onlyAllowWithLineOfSight, boolean allowAllies) {
        // Check for existing targets first (unless line of sight only)
        if (!onlyAllowWithLineOfSight) {
            LivingEntity currentTarget = getCurrentTarget(entity);
            if (currentTarget != null && currentTarget.distanceTo(entity) <= radius) {
                if (allowAllies || mayTarget(entity, currentTarget)) {
                    return currentTarget;
                }
            }
        }

        // Raycast for entities
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 startPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        for (int i = 0; i < radius; i++) {
            Vec3 currentPosition = startPosition.add(lookDirection.scale(i));

            // Check for entities at this position
            AABB detectionBox = new AABB(
                    currentPosition.subtract(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius),
                    currentPosition.add(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius)
            );

            List<LivingEntity> nearbyEntities = entity.level().getEntities(entity, detectionBox).stream()
                    .filter(e -> e instanceof LivingEntity && e != entity)
                    .map(e -> (LivingEntity) e)
                    .filter(e -> mayTarget(entity, e, allowAllies))
                    .toList();

            if (!nearbyEntities.isEmpty()) {
                return nearbyEntities.get(0);
            }

            // Check for blocks
            BlockState block = entity.level().getBlockState(BlockPos.containing(currentPosition));
            if (!block.getCollisionShape(entity.level(), BlockPos.containing(currentPosition)).isEmpty()) {
                break;
            }
        }

        return null;
    }

    // ==================== TARGET LOCATION METHODS ====================

    public static Vec3 getTargetLocation(LivingEntity entity, int radius, float entityDetectionRadius) {
        return getTargetLocation(entity, radius, entityDetectionRadius, false, false);
    }

    public static Vec3 getTargetLocation(LivingEntity entity, int radius, float entityDetectionRadius,
                                         boolean positionAtEntityFeet) {
        return getTargetLocation(entity, radius, entityDetectionRadius, positionAtEntityFeet, false);
    }

    /**
     * Core method for finding target locations (either entity or block)
     * Fires a TargetLocationEvent that allows modification of the target location
     * @param allowAllies If true, allows targeting allies (for support abilities)
     */
    public static Vec3 getTargetLocation(LivingEntity entity, int radius, float entityDetectionRadius,
                                         boolean positionAtEntityFeet, boolean allowAllies) {
        // Set flag to prevent TargetEntityEvent from firing during this call
        INSIDE_GET_TARGET_LOCATION.set(true);

        try {
            // Check for existing targets first
            LivingEntity currentTarget = getCurrentTarget(entity);
            if (currentTarget != null && currentTarget.distanceTo(entity) <= radius) {
                if (allowAllies || mayTarget(entity, currentTarget)) {
                    Vec3 location = getEntityTargetPosition(currentTarget, positionAtEntityFeet);
                    return fireTargetLocationEvent(entity, radius, entityDetectionRadius, positionAtEntityFeet, allowAllies, location);
                }
            }

            // Raycast for entities or blocks
            Vec3 lookDirection = entity.getLookAngle().normalize();
            Vec3 startPosition = entity.position().add(0, entity.getEyeHeight(), 0);
            Vec3 targetPosition = startPosition;

            for (int i = 0; i < radius; i++) {
                targetPosition = startPosition.add(lookDirection.scale(i));

                // Check for entities at this position
                AABB detectionBox = new AABB(
                        targetPosition.subtract(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius),
                        targetPosition.add(entityDetectionRadius, entityDetectionRadius, entityDetectionRadius)
                );

                List<Entity> nearbyEntities = entity.level().getEntities(entity, detectionBox).stream()
                        .filter(e -> e instanceof LivingEntity && e != entity)
                        .filter(e -> mayTarget(entity, (LivingEntity) e, allowAllies))
                        .toList();

                if (!nearbyEntities.isEmpty()) {
                    Entity target = nearbyEntities.get(0);
                    Vec3 location = getEntityTargetPosition(target, positionAtEntityFeet);
                    return fireTargetLocationEvent(entity, radius, entityDetectionRadius, positionAtEntityFeet, allowAllies, location);
                }

                // Check for blocks
                BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));
                if (!block.getCollisionShape(entity.level(), BlockPos.containing(targetPosition)).isEmpty()) {
                    Vec3 location = startPosition.add(lookDirection.scale(i - 1));
                    return fireTargetLocationEvent(entity, radius, entityDetectionRadius, positionAtEntityFeet, allowAllies, location);
                }
            }

            return fireTargetLocationEvent(entity, radius, entityDetectionRadius, positionAtEntityFeet, allowAllies, targetPosition);
        } finally {
            // Always reset the flag
            INSIDE_GET_TARGET_LOCATION.set(false);
        }
    }

    // ==================== HELPER METHODS FOR TARGET DETECTION ====================

    @Nullable
    private static LivingEntity getCurrentTarget(LivingEntity entity) {
        if (entity instanceof BeyonderNPCEntity npc) {
            return npc.getCurrentTarget();
        }
        if (entity instanceof Mob mob) {
            return mob.getTarget();
        }
        return null;
    }

    private static Vec3 getEntityTargetPosition(Entity entity, boolean atFeet) {
        if (atFeet) {
            return entity.position();
        }
        return entity.getEyePosition().subtract(0, entity.getEyeHeight() / 2, 0);
    }

    /**
     * Fires a TargetLocationEvent to allow modification of the target location
     * @return The potentially modified target location from the event
     */
    private static Vec3 fireTargetLocationEvent(LivingEntity entity, int radius,
                                                float entityDetectionRadius,
                                                boolean positionAtEntityFeet,
                                                boolean allowAllies,
                                                Vec3 targetLocation) {
        TargetLocationEvent event = new TargetLocationEvent(
                entity,
                radius,
                entityDetectionRadius,
                positionAtEntityFeet,
                allowAllies,
                targetLocation
        );
        NeoForge.EVENT_BUS.post(event);
        return event.getTargetLocation();
    }

    /**
     * Fires a TargetEntityEvent to allow modification of the target entity
     * @return The potentially modified target entity from the event
     */
    @Nullable
    private static LivingEntity fireTargetEntityEvent(LivingEntity entity, int radius,
                                                      float entityDetectionRadius,
                                                      boolean onlyAllowWithLineOfSight,
                                                      boolean allowAllies,
                                                      @Nullable LivingEntity targetEntity) {
        TargetEntityEvent event = new TargetEntityEvent(
                entity,
                radius,
                entityDetectionRadius,
                onlyAllowWithLineOfSight,
                allowAllies,
                targetEntity
        );
        NeoForge.EVENT_BUS.post(event);
        return event.getTargetEntity();
    }

    // ==================== DISTANCE UTILITY METHODS ====================

    public static double distanceToGround(Level level, Entity entity) {
        Vec3 startPos = entity.position();
        BlockPos pos = BlockPos.containing(startPos);

        for (int i = 0; i < 500; i++) {
            Vec3 currentPos = startPos.subtract(0, i * 0.5, 0);
            pos = BlockPos.containing(currentPos);
            BlockState block = level.getBlockState(pos);

            if (!block.getCollisionShape(level, pos).isEmpty()) {
                break;
            }
        }

        return pos.getCenter().distanceTo(startPos);
    }

    // ==================== NEARBY ENTITY RETRIEVAL ====================

    public static List<LivingEntity> getNearbyEntities(@Nullable LivingEntity exclude, ServerLevel level,
                                                       Vec3 center, double radius) {
        return getNearbyEntities(exclude, level, center, radius, false);
    }

    public static List<LivingEntity> getNearbyEntities(@Nullable LivingEntity exclude, ServerLevel level,
                                                       Vec3 center, double radius, boolean allowCreativeMode) {
        return getNearbyEntitiesInternal(exclude, level, center, radius, allowCreativeMode, false, false);
    }

    public static List<LivingEntity> getNearbyEntities(@Nullable LivingEntity exclude, ServerLevel level,
                                                       Vec3 center, double radius, boolean allowCreativeMode, Boolean allowAllies) {
        return getNearbyEntitiesInternal(exclude, level, center, radius, allowCreativeMode, false, allowAllies);
    }

    public static List<Entity> getAllNearbyEntities(@Nullable LivingEntity exclude, ServerLevel level,
                                                    Vec3 center, double radius) {
        return getAllNearbyEntities(exclude, level, center, radius, false);
    }

    public static List<Entity> getAllNearbyEntities(@Nullable LivingEntity exclude, ServerLevel level,
                                                    Vec3 center, double radius, boolean allowCreativeMode) {
        return getNearbyEntitiesInternal(exclude, level, center, radius, allowCreativeMode, true, false);
    }

    private static <T extends Entity> List<T> getNearbyEntitiesInternal(@Nullable LivingEntity exclude,
                                                                        ServerLevel level, Vec3 center,
                                                                        double radius, boolean allowCreativeMode,
                                                                        boolean includeAllEntities, boolean allowAllies) {
        AABB detectionBox = createDetectionBox(center, radius);
        double radiusSquared = radius * radius;

        Class<T> entityClass = (Class<T>) (includeAllEntities ? Entity.class : LivingEntity.class);

        return level.getEntitiesOfClass(entityClass, detectionBox).stream()
                .filter(e -> !(e instanceof Player player) || (!player.isCreative() || allowCreativeMode))
                .filter(entity -> entity.position().distanceToSqr(center) <= radiusSquared)
                .filter(entity -> entity != exclude)
                .filter(e -> exclude == null || (!(e instanceof LivingEntity le) || mayTarget(exclude, le, allowAllies)))
                .toList();
    }

    private static AABB createDetectionBox(Vec3 center, double radius) {
        return new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );
    }

    // ==================== DAMAGE METHODS ====================

    public static boolean damageNearbyEntities(ServerLevel level, LivingEntity source, double radius,
                                               double damage, Vec3 center, boolean ignoreSource,
                                               boolean distanceFalloff) {
        return damageNearbyEntities(level, source, 0, radius, damage, center, ignoreSource,
                distanceFalloff, false, -1, 0);
    }

    public static boolean damageNearbyEntities(ServerLevel level, LivingEntity source, double radius,
                                               double damage, Vec3 center, boolean ignoreSource,
                                               boolean distanceFalloff, int fireTicks) {
        return damageNearbyEntities(level, source, 0, radius, damage, center, ignoreSource,
                distanceFalloff, false, -1, fireTicks);
    }

    public static boolean damageNearbyEntities(ServerLevel level, LivingEntity source, double radius,
                                               double damage, Vec3 center, boolean ignoreSource,
                                               boolean distanceFalloff, boolean ignoreCooldown,
                                               int cooldownTicks) {
        return damageNearbyEntities(level, source, 0, radius, damage, center, ignoreSource,
                distanceFalloff, ignoreCooldown, cooldownTicks, 0);
    }

    public static boolean damageNearbyEntities(ServerLevel level, LivingEntity source, double radius,
                                               double damage, Vec3 center, boolean ignoreSource,
                                               boolean distanceFalloff, boolean ignoreCooldown,
                                               int cooldownTicks, int fireTicks) {
        return damageNearbyEntities(level, source, 0, radius, damage, center, ignoreSource,
                distanceFalloff, ignoreCooldown, cooldownTicks, fireTicks);
    }

    public static boolean damageNearbyEntities(ServerLevel level, LivingEntity source, double minRadius,
                                               double maxRadius, double damage, Vec3 center,
                                               boolean ignoreSource, boolean distanceFalloff,
                                               boolean ignoreCooldown, int cooldownTicks) {
        return damageNearbyEntities(level, source, minRadius, maxRadius, damage, center, ignoreSource,
                distanceFalloff, ignoreCooldown, cooldownTicks, 0);
    }

    /**
     * Core method for damaging nearby entities with all options
     */
    public static boolean damageNearbyEntities(ServerLevel level, LivingEntity source, double minRadius,
                                               double maxRadius, double damage, Vec3 center,
                                               boolean ignoreSource, boolean distanceFalloff,
                                               boolean ignoreCooldown, int cooldownTicks, int fireTicks) {
        AABB detectionBox = createDetectionBox(center, maxRadius);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, detectionBox)
                .stream()
                .filter(e -> mayTarget(source, e))
                .toList();

        boolean hitAnyEntity = false;
        double maxRadiusSquared = maxRadius * maxRadius;
        double minRadiusSquared = minRadius * minRadius;

        for (LivingEntity entity : nearbyEntities) {
            if (ignoreSource && entity == source) continue;

            double distanceSquared = entity.position().distanceToSqr(center);

            if (distanceSquared > maxRadiusSquared || distanceSquared < minRadiusSquared) {
                continue;
            }

            float finalDamage = calculateDamageWithFalloff(damage, distanceSquared, maxRadius, distanceFalloff);

            if (ignoreCooldown || entity.invulnerableTime <= 0) {
                applyDamage(entity, source, finalDamage);

                if (cooldownTicks >= 0) {
                    entity.invulnerableTime = cooldownTicks;
                }

                if (fireTicks > 0) {
                    entity.setRemainingFireTicks(entity.getRemainingFireTicks() + fireTicks);
                }

                hitAnyEntity = true;
            }
        }

        return hitAnyEntity;
    }

    private static float calculateDamageWithFalloff(double baseDamage, double distanceSquared,
                                                    double maxRadius, boolean distanceFalloff) {
        if (!distanceFalloff) {
            return (float) baseDamage;
        }

        double distance = Math.sqrt(distanceSquared);
        double falloffMultiplier = Math.max(0.1, 1.0 - (distance / maxRadius));
        return (float) (baseDamage * falloffMultiplier);
    }

    private static void applyDamage(LivingEntity entity, @Nullable LivingEntity source, float damage) {
        if (source != null) {
            entity.hurt(source.damageSources().mobAttack(source), damage);
        } else {
            entity.hurt(entity.damageSources().generic(), damage);
        }
    }

    // ==================== POTION EFFECT METHODS ====================

    public static void addPotionEffectToNearbyEntities(ServerLevel level, @Nullable LivingEntity entity,
                                                       double radius, Vec3 pos,
                                                       MobEffectInstance... mobEffectInstances) {
        List<LivingEntity> nearbyEntities = getNearbyEntities(entity, level, pos, radius);

        for (LivingEntity nearbyEntity : nearbyEntities) {
            if (!nearbyEntity.isAlive()) {
                continue;
            }

            if (entity != null && nearbyEntity.isInvulnerableTo(entity.damageSources().mobAttack(entity))) {
                continue;
            }

            for (MobEffectInstance effect : mobEffectInstances) {
                if (effect != null && !nearbyEntity.hasEffect(effect.getEffect())) {
                    nearbyEntity.addEffect(new MobEffectInstance(
                            effect.getEffect(),
                            effect.getDuration(),
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.isVisible(),
                            effect.showIcon()
                    ));
                }
            }
        }
    }

    // ==================== BLOCK GEOMETRY METHODS ====================

    public static Set<BlockPos> getBlocksInCircleOutline(ServerLevel level, Vec3 center, double radius) {
        double circumference = 2 * Math.PI * radius;
        int steps = Math.max(6, (int) Math.ceil(circumference * 2));
        return getBlocksInCircleOutline(level, center, radius, steps);
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

    public static Set<BlockPos> getBlocksInCircle(ServerLevel level, Vec3 center, double radius) {
        double stepSize = 0.5;
        Set<BlockPos> blocks = new HashSet<>();

        for (double r = 0.2; r < radius + 0.2; r += 0.2) {
            double circumference = 2 * Math.PI * r;
            int steps = Math.max(6, (int) Math.ceil(circumference / stepSize));
            blocks.addAll(getBlocksInCircleOutline(level, center.add(0, 0, 0), r, steps));
        }
        return blocks;
    }

    public static Set<BlockPos> getBlocksInCircle(ServerLevel level, Vec3 center, double radius, int steps) {
        if (level == null) return Set.of();

        Set<BlockPos> blocks = new HashSet<>();
        for (double r = 0.2; r < radius + 0.2; r += 0.2) {
            blocks.addAll(getBlocksInCircleOutline(level, center, r, steps));
        }
        return blocks;
    }

    public static List<BlockPos> getBlocksInSphereRadius(ServerLevel level, Vec3 center,
                                                         double radius, boolean filled) {
        return getBlocksInSphereRadius(level, center, radius, filled, false, false);
    }

    public static List<BlockPos> getBlocksInSphereRadius(Level level, Vec3 center, double radius,
                                                         boolean filled, boolean excludeEmptyBlocks,
                                                         boolean onlyExposed) {
        if (level == null) return List.of();

        List<BlockPos> blocks = new ArrayList<>();
        int steps = (int) Math.max(20, 4 * Math.PI * radius * radius);

        if (filled) {
            blocks.addAll(getFilledSphereBlocks(level, center, radius, excludeEmptyBlocks, onlyExposed));
        } else {
            blocks.addAll(getSphereShellBlocks(level, center, radius, steps, excludeEmptyBlocks, onlyExposed));
        }

        return blocks;
    }

    private static List<BlockPos> getFilledSphereBlocks(Level level, Vec3 center, double radius,
                                                        boolean excludeEmptyBlocks, boolean onlyExposed) {
        List<BlockPos> blocks = new ArrayList<>();
        int minX = Mth.floor(center.x - radius);
        int maxX = Mth.ceil(center.x + radius);
        int minY = Mth.floor(center.y - radius);
        int maxY = Mth.ceil(center.y + radius);
        int minZ = Mth.floor(center.z - radius);
        int maxZ = Mth.ceil(center.z + radius);
        double radiusSquared = radius * radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double dx = x + 0.5 - center.x;
                    double dy = y + 0.5 - center.y;
                    double dz = z + 0.5 - center.z;

                    if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (shouldIncludeBlock(level, pos, excludeEmptyBlocks, onlyExposed)) {
                            blocks.add(pos);
                        }
                    }
                }
            }
        }
        return blocks;
    }

    private static List<BlockPos> getSphereShellBlocks(Level level, Vec3 center, double radius, int steps,
                                                       boolean excludeEmptyBlocks, boolean onlyExposed) {
        List<BlockPos> blocks = new ArrayList<>();
        RandomSource random = level.random;

        for (int i = 0; i < steps; i++) {
            double theta = 2 * Math.PI * random.nextDouble();
            double phi = Math.acos(2 * random.nextDouble() - 1);

            double x = center.x + radius * Math.sin(phi) * Math.cos(theta);
            double y = center.y + radius * Math.sin(phi) * Math.sin(theta);
            double z = center.z + radius * Math.cos(phi);

            BlockPos pos = BlockPos.containing(x, y, z);
            if (shouldIncludeBlock(level, pos, excludeEmptyBlocks, onlyExposed)) {
                blocks.add(pos);
            }
        }
        return blocks;
    }

    public static List<BlockPos> getBlocksInEllipsoid(ServerLevel level, Vec3 center, double xzRadius,
                                                      double yRadius, boolean filled,
                                                      boolean excludeEmptyBlocks, boolean onlyExposed) {
        return getBlocksInEllipsoidInternal(level, center, xzRadius, yRadius, filled,
                excludeEmptyBlocks, onlyExposed);
    }

    private static List<BlockPos> getBlocksInEllipsoidInternal(Level level, Vec3 center, double xzRadius,
                                                               double yRadius, boolean filled,
                                                               boolean excludeEmptyBlocks,
                                                               boolean onlyExposed) {
        if (level == null) return List.of();

        List<BlockPos> blocks = new ArrayList<>();
        double maxRadius = Math.max(xzRadius, yRadius);
        int steps = (int) Math.max(20, 4 * Math.PI * maxRadius * maxRadius);

        if (filled) {
            blocks.addAll(getFilledEllipsoidBlocks(level, center, xzRadius, yRadius,
                    excludeEmptyBlocks, onlyExposed));
        } else {
            blocks.addAll(getEllipsoidShellBlocks(level, center, xzRadius, yRadius, steps,
                    excludeEmptyBlocks, onlyExposed));
        }

        return blocks;
    }

    private static List<BlockPos> getFilledEllipsoidBlocks(Level level, Vec3 center, double xzRadius,
                                                           double yRadius, boolean excludeEmptyBlocks,
                                                           boolean onlyExposed) {
        List<BlockPos> blocks = new ArrayList<>();
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

                    if (dx * dx + dy * dy + dz * dz <= 1.0) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (shouldIncludeBlock(level, pos, excludeEmptyBlocks, onlyExposed)) {
                            blocks.add(pos);
                        }
                    }
                }
            }
        }
        return blocks;
    }

    private static List<BlockPos> getEllipsoidShellBlocks(Level level, Vec3 center, double xzRadius,
                                                          double yRadius, int steps,
                                                          boolean excludeEmptyBlocks, boolean onlyExposed) {
        List<BlockPos> blocks = new ArrayList<>();
        RandomSource random = level.random;

        for (int i = 0; i < steps; i++) {
            double theta = 2 * Math.PI * random.nextDouble();
            double phi = Math.acos(2 * random.nextDouble() - 1);

            double x = center.x + xzRadius * Math.sin(phi) * Math.cos(theta);
            double y = center.y + yRadius * Math.sin(phi) * Math.sin(theta);
            double z = center.z + xzRadius * Math.cos(phi);

            BlockPos pos = BlockPos.containing(x, y, z);
            if (shouldIncludeBlock(level, pos, excludeEmptyBlocks, onlyExposed)) {
                blocks.add(pos);
            }
        }
        return blocks;
    }

    private static boolean shouldIncludeBlock(Level level, BlockPos pos,
                                              boolean excludeEmptyBlocks, boolean onlyExposed) {
        if (excludeEmptyBlocks && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            return false;
        }

        if (onlyExposed) {
            BlockPos above = pos.above();
            if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    // ==================== UI UTILITY METHODS ====================

    public static void sendActionBar(LivingEntity entity, Component message) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);
    }
}
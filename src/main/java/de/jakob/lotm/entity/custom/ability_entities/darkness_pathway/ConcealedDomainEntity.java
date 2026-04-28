package de.jakob.lotm.entity.custom.ability_entities.darkness_pathway;

import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.RegionSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class ConcealedDomainEntity extends Entity {

    public static final int RADIUS = 30;
    private static final int OWNER_MAX_DISTANCE = 50;
    private static final int SHELL_THICKNESS = 1;

    private static final Map<UUID, ConcealedDomainEntity> ACTIVE_BY_OWNER = new HashMap<>();
    public static final Map<ResourceKey<Level>, Set<BlockPos>> DOMAIN_BLOCKS = new HashMap<>();

    /** UUIDs of all living entities currently inside any active domain. Used for nocturnal checks. */
    public static final Set<UUID> ENTITIES_INSIDE_DOMAIN = new HashSet<>();

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(ConcealedDomainEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private BlockPos centerPos;
    private RegionSnapshot snapshot;
    private boolean domainBuilt = false;
    private final Random random = new Random();

    /** UUIDs tracked as inside by this specific domain instance, for cleanup on removal. */
    private final Set<UUID> myTrackedEntities = new HashSet<>();

    public ConcealedDomainEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_UUID, Optional.empty());
    }

    public void setOwner(LivingEntity owner) {
        this.entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
    }

    @Nullable
    public LivingEntity getOwner() {
        Optional<UUID> uuid = this.entityData.get(OWNER_UUID);
        if (uuid.isPresent() && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid.get());
            if (entity instanceof LivingEntity living) return living;
        }
        return null;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    @Nullable
    public static ConcealedDomainEntity getActiveForOwner(UUID ownerUUID) {
        ConcealedDomainEntity entity = ACTIVE_BY_OWNER.get(ownerUUID);
        if (entity != null && !entity.isRemoved()) return entity;
        return null;
    }

    public static void registerForOwner(UUID ownerUUID, ConcealedDomainEntity entity) {
        ConcealedDomainEntity existing = ACTIVE_BY_OWNER.get(ownerUUID);
        if (existing != null && !existing.isRemoved()) {
            existing.discard();
        }
        ACTIVE_BY_OWNER.put(ownerUUID, entity);
    }

    public static boolean isDomainBlock(Level level, BlockPos pos) {
        Set<BlockPos> blocks = DOMAIN_BLOCKS.get(level.dimension());
        return blocks != null && blocks.contains(pos.immutable());
    }

    private void markDomainBlock(ServerLevel level, BlockPos pos) {
        DOMAIN_BLOCKS.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(pos.immutable());
    }

    private void clearDomainBlocks(ServerLevel level) {
        Set<BlockPos> blocks = DOMAIN_BLOCKS.get(level.dimension());
        if (blocks != null && centerPos != null) {
            double clearRadiusSq = (RADIUS + 2.0) * (RADIUS + 2.0);
            Vec3 center = Vec3.atCenterOf(centerPos);
            blocks.removeIf(pos -> pos.getCenter().distanceToSqr(center) <= clearRadiusSq);
        }
    }

    private void buildDomain(ServerLevel level) {
        centerPos = this.blockPosition();
        snapshot = new RegionSnapshot(level, centerPos, RADIUS + 1);

        double outerRadiusSq = RADIUS * RADIUS;
        double innerRadiusSq = (RADIUS - SHELL_THICKNESS) * (RADIUS - SHELL_THICKNESS);

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    double distSq = x * x + y * y + z * z;
                    if (distSq <= outerRadiusSq && distSq > innerRadiusSq) {
                        BlockPos pos = centerPos.offset(x, y, z);
                        snapshot.captureBlock(pos);
                        level.setBlock(pos, Blocks.BLACK_CONCRETE.defaultBlockState(), 3);
                        markDomainBlock(level, pos);
                    }
                }
            }
        }

        domainBuilt = true;
    }

    private void tearDownDomain(ServerLevel level) {
        if (snapshot != null) {
            snapshot.restore(level);
        }
        clearDomainBlocks(level);
        // Remove all entities this domain was tracking from the global set
        ENTITIES_INSIDE_DOMAIN.removeAll(myTrackedEntities);
        myTrackedEntities.clear();
        domainBuilt = false;
    }

    private boolean isAllowed(Entity entity) {
        LivingEntity owner = getOwner();
        if (owner == null) return true;
        if (entity.getUUID().equals(owner.getUUID())) return true;
        if (entity instanceof LivingEntity living) {
            return AllyUtil.areAllies(owner, living);
        }
        return false;
    }

    private Vec3 getTopPoint() {
        return Vec3.atCenterOf(centerPos).add(0, RADIUS - 3, 0);
    }

    private void ejectToOutside(Entity entity) {
        Vec3 center = Vec3.atCenterOf(centerPos);
        Vec3 direction = entity.position().subtract(center);

        if (direction.lengthSqr() < 0.001) {
            direction = new Vec3(0, 1, 0);
        }

        Vec3 ejectionPos = center.add(direction.normalize().scale(RADIUS + 2.5));

        if (this.level() instanceof ServerLevel serverLevel) {
            BlockPos ejectionBlock = BlockPos.containing(ejectionPos);
            int tries = 0;
            while (!serverLevel.getBlockState(ejectionBlock).isAir()
                    && ejectionBlock.getY() < serverLevel.getMaxBuildHeight()
                    && tries++ < 20) {
                ejectionBlock = ejectionBlock.above();
            }
            ejectionPos = new Vec3(ejectionPos.x, ejectionBlock.getY(), ejectionPos.z);
        }

        entity.teleportTo(ejectionPos.x, ejectionPos.y, ejectionPos.z);
        entity.setDeltaMovement(Vec3.ZERO);

        if (entity instanceof ServerPlayer player) {
            player.connection.resetPosition();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (!domainBuilt) {
            buildDomain(serverLevel);
            return;
        }

        centerPos = this.blockPosition();
        LivingEntity owner = getOwner();

        Vec3 center = Vec3.atCenterOf(centerPos);
        double radiusSq = (RADIUS - SHELL_THICKNESS) * (RADIUS - SHELL_THICKNESS);
        Vec3 topPoint = getTopPoint();

        // Discard if owner left the game
        if (owner == null || owner.isRemoved() || !owner.isAlive()) {
            this.discard();
            return;
        }
        // Discard if owner changed dimension
        if (!owner.level().equals(this.level())) {
            this.discard();
            return;
        }
        // Discard if owner exceeded max distance from domain center
        if (owner.distanceToSqr(center) > OWNER_MAX_DISTANCE * OWNER_MAX_DISTANCE) {
            this.discard();
            return;
        }

        // Check for interactions
        if(InteractionHandler.isInteractionPossible(new Location(position(), level()), "destruction", BeyonderData.getSequence(owner))) {
            this.discard();
            return;
        }

        if (this.tickCount % 3 == 0) {
            AABB area = new AABB(centerPos).inflate(RADIUS);
            List<Entity> entities = serverLevel.getEntities(this, area, e -> e instanceof LivingEntity);

            // Collect which UUIDs are inside this tick so we can remove stale ones
            Set<UUID> currentlyInside = new HashSet<>();

            for (Entity entity : entities) {
                double distSq = entity.position().distanceToSqr(center);
                boolean inside = distSq <= radiusSq;

                if (!inside) continue;

                boolean allowed = isAllowed(entity);

                if (allowed) {
                    // Track as inside domain for nocturnal checks
                    currentlyInside.add(entity.getUUID());
                    ENTITIES_INSIDE_DOMAIN.add(entity.getUUID());
                    myTrackedEntities.add(entity.getUUID());

                    // Night vision
                    if (entity instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 60, 0, false, false, false));
                    }

                    // Top exit point — eject out
                    double distToTop = entity.position().distanceToSqr(topPoint);
                    if (distToTop <= 5.0) {
                        ejectToOutside(entity);
                    }
                }
                // Non-allowed: physically blocked by concrete, no action needed
            }

            // Remove entities that were tracked by this domain but are no longer inside
            Set<UUID> leftDomain = new HashSet<>(myTrackedEntities);
            leftDomain.removeAll(currentlyInside);
            ENTITIES_INSIDE_DOMAIN.removeAll(leftDomain);
            myTrackedEntities.removeAll(leftDomain);
        }

        // Red particles at the top exit point every 5 ticks
        if (this.tickCount % 5 == 0) {
            for (int i = 0; i < 8; i++) {
                double theta = random.nextDouble() * 2 * Math.PI;
                double phi = random.nextDouble() * 0.5;
                double r = 1.5;
                double px = topPoint.x + r * Math.sin(phi) * Math.cos(theta);
                double py = topPoint.y + r * Math.cos(phi);
                double pz = topPoint.z + r * Math.sin(phi) * Math.sin(theta);
                serverLevel.sendParticles(
                        new net.minecraft.core.particles.DustParticleOptions(
                                new org.joml.Vector3f(1f, 0f, 0f), 1.5f),
                        px, py, pz, 10, 0, 0, 0, 0.0);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel && domainBuilt) {
            tearDownDomain(serverLevel);
        } else {
            // Even if domain wasn't built, clean up tracked entities
            ENTITIES_INSIDE_DOMAIN.removeAll(myTrackedEntities);
            myTrackedEntities.clear();
        }
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null) {
            ACTIVE_BY_OWNER.remove(ownerUUID, this);
        }
        super.remove(reason);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {}

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
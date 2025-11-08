package de.jakob.lotm.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class ElectromagneticTornadoEntity extends Entity {
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(ElectromagneticTornadoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ElectromagneticTornadoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> CASTER_UUID = SynchedEntityData.defineId(ElectromagneticTornadoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> TARGET_UUID = SynchedEntityData.defineId(ElectromagneticTornadoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(ElectromagneticTornadoEntity.class, EntityDataSerializers.FLOAT);

    private final List<CirclingBlock> circlingBlocks = new ArrayList<>();
    private int blockPickupCooldown = 0;
    private int lifeTicks = 0;
    private int maxLifeTicks = 600; // 30 seconds default

    private Vec3 randomDirection = Vec3.ZERO;
    private int directionChangeCooldown = 0;

    private static final float TARGET_HEIGHT_ABOVE_GROUND = 0.5f;

    public ElectromagneticTornadoEntity(EntityType<?> entityType, Level level) {
        this(entityType, level, 1.0f, 4.0f, null, null);
    }

    public ElectromagneticTornadoEntity(EntityType<?> entityType, Level level, float speed, float damage, @Nullable Entity caster) {
        this(entityType, level, speed, damage, caster, null);
    }

    public ElectromagneticTornadoEntity(EntityType<?> entityType, Level level, float speed, float damage, @Nullable Entity caster, @Nullable Entity target) {
        super(entityType, level);
        this.setSpeed(speed);
        this.setDamage(damage);
        if (caster != null) {
            this.setCasterUUID(caster.getUUID());
        }
        if (target != null) {
            this.setTargetUUID(target.getUUID());
        }
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPEED, 1.0f);
        builder.define(DAMAGE, 4.0f);
        builder.define(CASTER_UUID, Optional.empty());
        builder.define(TARGET_UUID, Optional.empty());
        builder.define(ROTATION, 0.0f);
    }

    public float getSpeed() {
        return this.entityData.get(SPEED);
    }

    public void setSpeed(float speed) {
        this.entityData.set(SPEED, speed);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getRotation() {
        return this.entityData.get(ROTATION);
    }

    private void setRotation(float rotation) {
        this.entityData.set(ROTATION, rotation);
    }

    public void setCasterUUID(@Nullable UUID uuid) {
        this.entityData.set(CASTER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getCasterUUID() {
        return this.entityData.get(CASTER_UUID).orElse(null);
    }

    public void setTargetUUID(@Nullable UUID uuid) {
        this.entityData.set(TARGET_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getTargetUUID() {
        return this.entityData.get(TARGET_UUID).orElse(null);
    }

    @Nullable
    private Entity getCaster() {
        UUID uuid = getCasterUUID();
        if (uuid != null && this.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(uuid);
        }
        return null;
    }

    @Nullable
    private Entity getTarget() {
        UUID uuid = getTargetUUID();
        if (uuid != null && this.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(uuid);
        }
        return null;
    }

    private double findGroundLevel(BlockPos pos) {
        for (int y = 0; y < 10; y++) {
            BlockPos checkPos = pos.below(y);
            BlockState state = this.level().getBlockState(checkPos);
            if (!state.isAir() && state.isSolidRender(this.level(), checkPos)) {
                return checkPos.getY() + 1.0;
            }
        }
        for (int y = 1; y < 10; y++) {
            BlockPos checkPos = pos.above(y);
            BlockState stateBelow = this.level().getBlockState(checkPos.below());
            if (!stateBelow.isAir() && stateBelow.isSolidRender(this.level(), checkPos.below())) {
                return checkPos.getY();
            }
        }
        return pos.getY();
    }

    @Override
    public void tick() {
        super.tick();

        // Update rotation for visual effect
        setRotation((getRotation() + 15.0f) % 360.0f);

        lifeTicks++;
        if (lifeTicks > maxLifeTicks) {
            this.discard();
            return;
        }

        Entity target = getTarget();
        Vec3 horizontalMovement;

        if (target != null && target.isAlive()) {
            Vec3 direction = target.position().subtract(this.position());
            direction = new Vec3(direction.x, 0, direction.z).normalize();
            horizontalMovement = direction.scale(getSpeed() * 0.2);
        } else {
            if (directionChangeCooldown <= 0) {
                randomDirection = new Vec3(
                        this.random.nextGaussian() * 0.5,
                        0,
                        this.random.nextGaussian() * 0.5
                ).normalize();
                directionChangeCooldown = 40 + this.random.nextInt(60);
            } else {
                directionChangeCooldown--;
            }
            horizontalMovement = randomDirection;
        }

        double groundLevel = findGroundLevel(this.blockPosition());
        double targetY = groundLevel + TARGET_HEIGHT_ABOVE_GROUND;
        double currentY = this.getY();

        double verticalSpeed = 0.1;
        double yDiff = targetY - currentY;
        double yMovement = Mth.clamp(yDiff, -verticalSpeed, verticalSpeed);

        Vec3 finalMovement = (new Vec3(horizontalMovement.x, yMovement, horizontalMovement.z)).normalize().scale(getSpeed());
        this.setDeltaMovement(finalMovement);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.hurtMarked = true;

        damageNearbyEntities();

        spawnParticles();

        if (this.tickCount % 20 == 0) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_AMBIENT,
                    SoundSource.HOSTILE, 1.0f, 0.3f + this.random.nextFloat() * 0.2f);
        }

        // Add electric crackling sound
        if (this.tickCount % 15 == 0) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.HOSTILE, 0.3f, 1.5f + this.random.nextFloat() * 0.5f);
        }
    }

    private void damageNearbyEntities() {
        AABB boundingBox = this.getBoundingBox().inflate(5.0);
        List<Entity> entities = this.level().getEntities(this, boundingBox);
        Entity caster = getCaster();

        UUID casterUUID = getCasterUUID();

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && entity != caster) {
                if (casterUUID != null && entity.getUUID().equals(casterUUID)) {
                    continue;
                }
                float distance = this.distanceTo(entity);
                if (distance < 6.0f) {
                    entity.hurt(this.damageSources().lightningBolt(), getDamage());

                    Vec3 direction = this.position().subtract(entity.position()).normalize();
                    entity.push(direction.x * 0.3, 0.3, direction.z * 0.3);
                }
            }
        }
    }

    private void spawnParticles() {
        // Particles removed for renderer-only effects
    }

    public List<CirclingBlock> getCirclingBlocks() {
        return circlingBlocks;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setSpeed(tag.getFloat("Speed"));
        this.setDamage(tag.getFloat("Damage"));
        this.lifeTicks = tag.getInt("LifeTicks");
        this.maxLifeTicks = tag.getInt("MaxLifeTicks");
        this.setRotation(tag.getFloat("Rotation"));

        if (tag.hasUUID("CasterUUID")) {
            this.setCasterUUID(tag.getUUID("CasterUUID"));
        }
        if (tag.hasUUID("TargetUUID")) {
            this.setTargetUUID(tag.getUUID("TargetUUID"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Speed", this.getSpeed());
        tag.putFloat("Damage", this.getDamage());
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putInt("MaxLifeTicks", this.maxLifeTicks);
        tag.putFloat("Rotation", this.getRotation());

        UUID casterUUID = getCasterUUID();
        if (casterUUID != null) {
            tag.putUUID("CasterUUID", casterUUID);
        }
        UUID targetUUID = getTargetUUID();
        if (targetUUID != null) {
            tag.putUUID("TargetUUID", targetUUID);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public void setMaxLifeTicks(int ticks) {
        this.maxLifeTicks = ticks;
    }

    public static class CirclingBlock {
        public BlockState state;
        public float angle;
        public float radius;
        public float height;
        public int lifetime;

        public CirclingBlock(BlockState state, float angle, float radius, float height) {
            this.state = state;
            this.angle = angle;
            this.radius = radius;
            this.height = height;
            this.lifetime = 0;
        }
    }
}
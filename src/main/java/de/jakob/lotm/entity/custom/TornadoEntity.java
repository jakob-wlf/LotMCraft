package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.client.TornadoAnimations;
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
import net.minecraft.world.damagesource.DamageSource;
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

public class TornadoEntity extends Entity {
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> CASTER_UUID = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> TARGET_UUID = SynchedEntityData.defineId(TornadoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    
    private final List<CirclingBlock> circlingBlocks = new ArrayList<>();
    private int blockPickupCooldown = 0;
    private int lifeTicks = 0;
    private int maxLifeTicks = 600; // 30 seconds default
    
    private Vec3 randomDirection = Vec3.ZERO;
    private int directionChangeCooldown = 0;
    
    public TornadoEntity(EntityType<?> entityType, Level level) {
        this(entityType, level, 1.0f, 4.0f, null, null);
    }
    
    public TornadoEntity(EntityType<?> entityType, Level level, float speed, float damage, @Nullable Entity caster) {
        this(entityType, level, speed, damage, caster, null);
    }
    
    public TornadoEntity(EntityType<?> entityType, Level level, float speed, float damage, @Nullable Entity caster, @Nullable Entity target) {
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
    
    @Override
    public void tick() {
        super.tick();
        
        lifeTicks++;
        if (lifeTicks > maxLifeTicks) {
            this.discard();
            return;
        }
        
        // Movement logic
        Entity target = getTarget();
        if (target != null && target.isAlive()) {
            // Chase target
            Vec3 direction = target.position().subtract(this.position()).normalize();
            this.setDeltaMovement(direction.scale(getSpeed() * 0.2));
        } else {
            // Random movement
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
            this.setDeltaMovement(randomDirection.scale(getSpeed() * 0.15));
        }
        
        this.move(MoverType.SELF, this.getDeltaMovement());
        
        // Damage entities
        damageNearbyEntities();
        
        // Pick up blocks
        if (blockPickupCooldown <= 0 && !this.level().isClientSide) {
            pickupBlocks();
            blockPickupCooldown = 20;
        } else {
            blockPickupCooldown--;
        }
        
        // Update circling blocks
        updateCirclingBlocks();
        
        // Spawn particles
        spawnParticles();
        
        // Play sound occasionally
        if (this.tickCount % 20 == 0) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.ELYTRA_FLYING, 
                SoundSource.HOSTILE, 1.0f, 0.5f + this.random.nextFloat() * 0.3f);
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
                    continue; // Skip the caster
                }
                float distance = this.distanceTo(entity);
                if (distance < 6.0f) {
                    float damageAmount = getDamage() * (1.0f - distance / 6.0f);
                    entity.hurt(this.damageSources().magic(), damageAmount);
                    
                    // Pull entities toward center
                    Vec3 direction = this.position().subtract(entity.position()).normalize();
                    entity.push(direction.x * 0.3, 0.3, direction.z * 0.3);
                }
            }
        }
    }
    
    private void pickupBlocks() {
        if (circlingBlocks.size() >= 12) return; // Max 12 blocks
        
        BlockPos centerPos = this.blockPosition();
        int radius = 3;
        
        for (int i = 0; i < 3; i++) { // Try 3 times per pickup cycle
            BlockPos targetPos = centerPos.offset(
                this.random.nextInt(radius * 2) - radius,
                this.random.nextInt(3) - 1,
                this.random.nextInt(radius * 2) - radius
            );
            
            BlockState state = this.level().getBlockState(targetPos);
            if (!state.isAir() && state.getDestroySpeed(this.level(), targetPos) >= 0 && 
                state.getDestroySpeed(this.level(), targetPos) < 50.0f) {
                
                Block block = state.getBlock();
                if (block != Blocks.BEDROCK && block != Blocks.END_PORTAL_FRAME && 
                    block != Blocks.END_PORTAL && block != Blocks.BARRIER) {
                    
                    this.level().destroyBlock(targetPos, false);
                    
                    CirclingBlock circlingBlock = new CirclingBlock(
                        state,
                        this.random.nextFloat() * 360,
                        2.0f + this.random.nextFloat() * 2.0f,
                        this.random.nextFloat() * 10.0f
                    );
                    circlingBlocks.add(circlingBlock);
                    break;
                }
            }
        }
    }
    
    private void updateCirclingBlocks() {
        Iterator<CirclingBlock> iterator = circlingBlocks.iterator();
        while (iterator.hasNext()) {
            CirclingBlock block = iterator.next();
            block.lifetime++;
            block.angle += 5.0f;
            
            if (block.lifetime > 100) { // Remove after 5 seconds
                iterator.remove();
            }
        }
    }
    
    private void spawnParticles() {
        if (this.level().isClientSide) {
            for (int i = 0; i < 5; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2;
                double radius = 2.0 + this.random.nextDouble() * 3.0;
                double x = this.getX() + Math.cos(angle) * radius;
                double y = this.getY() + this.random.nextDouble() * 8.0;
                double z = this.getZ() + Math.sin(angle) * radius;
                
                this.level().addParticle(ParticleTypes.CLOUD, x, y, z, 
                    (this.random.nextDouble() - 0.5) * 0.1,
                    this.random.nextDouble() * 0.2,
                    (this.random.nextDouble() - 0.5) * 0.1);
            }
        }
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
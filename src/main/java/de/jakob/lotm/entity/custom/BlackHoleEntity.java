package de.jakob.lotm.entity.custom;

import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlackHoleEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SUCK_BLOCKS = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.BOOLEAN);
    
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private LivingEntity cachedOwner;
    
    private static final double PULL_STRENGTH = 0.5;
    private static final int duration = 20 * 60;
    private int age = 0;

    private List<BlockPos> blocks;
    
    public BlackHoleEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    // Custom constructor for easy creation
    public BlackHoleEntity(EntityType<?> type, Level level, double x, double y, double z, 
                          float radius, float damage, boolean suckBlocks, @Nullable LivingEntity owner) {
        this(type, level);
        this.setPos(x, y, z);
        this.setRadius(radius);
        this.setDamage(damage);
        this.setSuckBlocks(suckBlocks);
        this.setOwner(owner);

        blocks = AbilityUtil.getBlocksInSphereRadius((ServerLevel) level(), this.position(), getRadius() * 6, true, true, true).stream().filter(b -> level().getBlockState(b).getDestroySpeed(level(), b) >= 0).toList();

    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 3.0F);
        builder.define(DAMAGE, 2.0F);
        builder.define(SUCK_BLOCKS, false);
    }
    
    public void setRadius(float radius) {
        this.entityData.set(RADIUS, radius);
    }
    
    public float getRadius() {
        return this.entityData.get(RADIUS);
    }
    
    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }
    
    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }
    
    public void setSuckBlocks(boolean suckBlocks) {
        this.entityData.set(SUCK_BLOCKS, suckBlocks);
    }
    
    public boolean shouldSuckBlocks() {
        return this.entityData.get(SUCK_BLOCKS);
    }
    
    public void setOwner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        } else {
            this.ownerUUID = null;
            this.cachedOwner = null;
        }
    }
    
    @Nullable
    public LivingEntity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity living) {
                this.cachedOwner = living;
                return this.cachedOwner;
            }
        }
        return null;
    }
    
    @Override
    public void tick() {
        super.tick();

        if(level().isClientSide)
            return;

        if(age == 0) {
            if(shouldSuckBlocks()) {
                AbilityUtil.getBlocksInSphereRadius((ServerLevel) level(), this.position(), getRadius() * 1.5, true, false, false).stream().filter(b -> level().getBlockState(b).getDestroySpeed(level(), b) >= 0).forEach(b -> level().setBlockAndUpdate(b, Blocks.AIR.defaultBlockState()));
            }
        }

        age++;
        if (tickCount >= duration) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        if(age % 20 == 0) {
            blocks = AbilityUtil.getBlocksInSphereRadius((ServerLevel) level(), this.position(), getRadius() * 6, true, true, true).stream().filter(b -> level().getBlockState(b).getDestroySpeed(level(), b) >= 0).toList();
        }

        pullEntities();
        damageEntities();

        suckUpBlocks();
    }
    
    private void pullEntities() {
        float radius = getRadius() * 10;
        
        List<LivingEntity> entities = AbilityUtil.getNearbyEntities(getOwner(), (ServerLevel) level(), position(), radius, false);
        
        Vec3 blackHolePos = this.position();
        
        for (LivingEntity entity : entities) {
            Vec3 entityPos = entity.position();
            Vec3 diff = blackHolePos.subtract(entityPos);
            double distance = diff.length();
            
            if (distance < radius) {
                // Stronger pull the closer you are
                double pullStrength = PULL_STRENGTH * (1 - Math.max(0, distance / radius));
                Vec3 pullVector = diff.normalize().scale(pullStrength);
                
                entity.setDeltaMovement(pullVector);
                entity.hurtMarked = true;
            }
        }
    }
    
    private void damageEntities() {
        float radius = getRadius();
        float damage = getDamage();
        float damageRadius = radius * 0.3F; // Damage only in inner radius

        AbilityUtil.damageNearbyEntities((ServerLevel) level(), getOwner(), damageRadius, damage, this.position(), true, false);
    }
    
    private void suckUpBlocks() {
        if(this.blocks == null || this.blocks.isEmpty())
            return;
        int blocksToSuck = shouldSuckBlocks() ? Math.min(100, blocks.size()) : 2;
        for (int i = 0; i < blocksToSuck; i++) {
            BlockPos blockPos = blocks.get(random.nextInt(blocks.size()));
            BlockState state = level().getBlockState(blockPos);
            if(shouldSuckBlocks())
                level().setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());

            if(random.nextInt(70) != 0 && shouldSuckBlocks())
                continue;

            FallingBlockEntity falling = FallingBlockEntity.fall(
                    level(),
                    blockPos.above().above(),
                    state
            );
            falling.disableDrop();

            AtomicBoolean cancel = new AtomicBoolean(false);

            ServerScheduler.scheduleForDuration(0, 1, 50, () -> {
                if(cancel.get())
                    return;

                Vec3 diff = this.position().subtract(falling.position()).normalize().scale(.5);
                falling.setDeltaMovement(diff);
                falling.hurtMarked = true;

                if(this.distanceTo(falling) < getRadius() - .1) {
                    falling.remove(RemovalReason.DISCARDED);
                    cancel.set(true);
                }
            }, falling::discard, (ServerLevel) level());

            falling.setNoGravity(true);
        }
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
        }
        this.setRadius(compound.getFloat("Radius"));
        this.setDamage(compound.getFloat("Damage"));
        this.setSuckBlocks(compound.getBoolean("SuckBlocks"));
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
        compound.putFloat("Radius", this.getRadius());
        compound.putFloat("Damage", this.getDamage());
        compound.putBoolean("SuckBlocks", this.shouldSuckBlocks());
    }
}
package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.client.TsunamiRenderer;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TsunamiEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(TsunamiEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_GRIEFING = SynchedEntityData.defineId(TsunamiEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_DIRECTION_X = SynchedEntityData.defineId(TsunamiEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DIRECTION_Z = SynchedEntityData.defineId(TsunamiEntity.class, EntityDataSerializers.FLOAT);

    private LivingEntity shooter;

    private static final double SPEED = 0.8D;
    private static final int MAX_LIFETIME = 200; // 10 seconds at 20 TPS
    private int ticksExisted = 0;
    private boolean hasAdjustedPosition = false;
    private float speed = .65f;

    public TsunamiEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        // Set bounding box based on your model dimensions
        // Adjust these values based on your model's actual size
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        // Ensure direction is set when added to world (for summoned entities)
        if (!this.level().isClientSide && !directionManuallySet) {
            Vec3 currentDirection = getDirectionFacing();
            if (currentDirection.lengthSqr() == 0) {
                // Default to facing north
                setDirection(new Vec3(0, 0, -1));
                float yaw = (float) Math.toDegrees(Math.atan2(-0.0, -(-1.0))) + 90.0F;
                this.setYRot(yaw);
                this.yRotO = yaw;
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isColliding(BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        float scale = TsunamiRenderer.scale;
        float width = 8.0F * scale;
        float height = 6.0F * scale;
        float length = 2.0F * scale;

        return createBoundingBox(width, height, length);
    }

    private AABB createBoundingBox(float width, float height, float length) {
        return new AABB(
                this.getX() - width / 2.0D,
                this.getY(),
                this.getZ() - length / 2.0D,
                this.getX() + width / 2.0D,
                this.getY() + height,
                this.getZ() + length / 2.0D
        );
    }

    private boolean directionManuallySet = false;

    // Constructor for spawning with custom parameters
    public TsunamiEntity(Level level, Vec3 position, Vec3 direction, float damage, boolean griefing, LivingEntity shooter) {
        this(ModEntities.TSUNAMI.get(), level);
        this.setPos(position.x, position.y, position.z);
        this.setDirection(direction);
        this.setDamage(damage);
        this.setGriefing(griefing);
        this.shooter = shooter;

        this.directionManuallySet = true;

        // Set initial rotation to match direction
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z)) + 90.0F;
        this.setYRot(yaw);
        this.yRotO = yaw;
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DAMAGE, 10.0F);
        builder.define(DATA_DIRECTION_X, 0.0F);
        builder.define(DATA_DIRECTION_Z, -1.0F);
        builder.define(DATA_GRIEFING, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Adjust position on first tick if not on solid ground
            if (!hasAdjustedPosition) {
                adjustPositionToGround();
                hasAdjustedPosition = true;
            }

            // Move forward - only horizontal movement, no vertical rotation
            Vec3 direction = getDirectionFacing().normalize().scale(SPEED);
            // Ensure Y component is always 0 for horizontal-only movement
            Vec3 normalizedDirection = direction.normalize().scale(SPEED);
            Vec3 horizontalMovement = new Vec3(normalizedDirection.x * speed, 0, normalizedDirection.z * speed);

            double newX = this.getX() + horizontalMovement.x;
            double newZ = this.getZ() + horizontalMovement.z;
            this.setPos(newX, this.getY(), newZ);

            this.setDeltaMovement(horizontalMovement);

            // Update bounding box after movement
            this.setBoundingBox(this.makeBoundingBox());

            // Set entity rotation to match the direction it's moving
            // This ensures the hitbox marker aligns with the visual model
            float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z)) + 90.0F;
            this.setYRot(yaw);
            this.setXRot(0.0F); // Always keep pitch at 0
            this.yRotO = yaw; // Set old rotation for interpolation


            // Damage entities in path
            damageEntitiesInPath();
            if(getGriefing())
                breakSurroundingBlocks();

            spawnParticles();

            // Check lifetime
            ticksExisted++;
            if (ticksExisted >= MAX_LIFETIME) {
                this.discard();
            }
        }
    }

    private void spawnParticles() {
        if(level().isClientSide)
            return;

        Vec3 direction = getDirectionFacing().normalize();
        for(int forward = 0; forward < 2; forward++) {
            for(int up = 0; up < 2; up++) {
                for(int side = -16; side < 17; side+=2) {
                    Vec3 pos = VectorUtil.getRelativePosition(position(), direction, forward, side, up);
                    ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.CLOUD, pos, 4, .4, .4, .4, 0);
                }
            }
        }
    }

    private void breakSurroundingBlocks() {
        if(level().isClientSide)
            return;
        Vec3 direction = getDirectionFacing().normalize();
        for(int forward = -2; forward < 0; forward++) {
            for(int up = 0; up < 13; up++) {
                for(int side = -16; side < 17; side++) {
                    Vec3 pos = VectorUtil.getRelativePosition(position(), direction, forward, side, up);
                    BlockPos blockPos = BlockPos.containing(pos);
                    level().setBlockAndUpdate(blockPos, random.nextInt(10) == 0 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private void adjustPositionToGround() {
        BlockPos currentPos = this.blockPosition();

        // Check if current position is on solid block
        if (level().getBlockState(currentPos.below()).isSolid()) {
            return; // Already on solid ground
        }

        // Check the next 3 blocks below
        for (int i = 1; i <= 3; i++) {
            BlockPos checkPos = currentPos.below(i);
            BlockState blockState = level().getBlockState(checkPos);

            if (blockState.isSolid()) {
                // Found solid block, position tsunami on top of it
                this.setPos(this.getX(), checkPos.getY() + 1, this.getZ());
                return;
            }
        }

        // No solid blocks found in range, stay at current position
    }

    private void damageEntitiesInPath() {
        if(level().isClientSide)
            return;

        Vec3 direction = getDirectionFacing().normalize();
        for(int forward = -2; forward < 5; forward++) {
            for(int up = 0; up < 15; up++) {
                for(int side = -8; side < 9; side++) {
                    Vec3 pos = VectorUtil.getRelativePosition(position(), direction, forward, side, up);
                    for (LivingEntity entity : AbilityUtil.getNearbyEntities(this.shooter, (ServerLevel) level(), pos, 3)) {
                        if (entity instanceof LivingEntity livingEntity) {
                            // Create damage source
                            DamageSource damageSource = this.damageSources().generic();
                            livingEntity.hurt(damageSource, this.getDamage());

                            // Add knockback effect in the direction the tsunami is moving
                            Vec3 knockbackDirection = getDirectionFacing().normalize().scale(2.0D);
                            entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackDirection.x, 0.5D, knockbackDirection.z));
                        }
                    }
                }
            }
        }


    }

    // Getters and setters for synched data
    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_DAMAGE, damage);
    }

    public void setGriefing(boolean griefing) {
        this.entityData.set(DATA_GRIEFING, griefing);
    }

    public boolean getGriefing() {
        return this.entityData.get(DATA_GRIEFING);
    }

    public Vec3 getDirectionFacing() {
        return new Vec3(
                this.entityData.get(DATA_DIRECTION_X),
                0.0D,
                this.entityData.get(DATA_DIRECTION_Z)
        );
    }

    public void setDirection(Vec3 direction) {
        Vec3 normalized = direction.normalize();
        this.entityData.set(DATA_DIRECTION_X, (float) normalized.x);
        this.entityData.set(DATA_DIRECTION_Z, (float) normalized.z);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.setDamage(compound.getFloat("Damage"));
        this.entityData.set(DATA_DIRECTION_X, compound.getFloat("DirectionX"));
        this.entityData.set(DATA_DIRECTION_Z, compound.getFloat("DirectionZ"));
        this.entityData.set(DATA_GRIEFING, compound.getBoolean("Griefing"));
        this.ticksExisted = compound.getInt("TicksExisted");
        this.hasAdjustedPosition = compound.getBoolean("HasAdjustedPosition");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("Damage", this.getDamage());
        compound.putFloat("DirectionX", this.entityData.get(DATA_DIRECTION_X));
        compound.putFloat("DirectionZ", this.entityData.get(DATA_DIRECTION_Z));
        compound.putBoolean("Griefing", this.entityData.get(DATA_GRIEFING));
        compound.putInt("TicksExisted", this.ticksExisted);
        compound.putBoolean("HasAdjustedPosition", this.hasAdjustedPosition);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

}
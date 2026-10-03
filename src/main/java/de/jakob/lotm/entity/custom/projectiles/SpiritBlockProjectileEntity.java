package de.jakob.lotm.entity.custom.projectiles;

import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * A custom entity that mimics a falling block visually but acts as a projectile.
 * It does not place blocks, does not drop items, and deals customizable damage
 * when close enough to a target.
 */
public class SpiritBlockProjectileEntity extends Entity {

    private static final EntityDataAccessor<BlockPos> DATA_BLOCK_POS =
            SynchedEntityData.defineId(SpiritBlockProjectileEntity.class, EntityDataSerializers.BLOCK_POS);

    private BlockState blockState = Blocks.STONE.defaultBlockState();
    private float damage;
    private double hitRadius = 1.5;
    private int maxLifetime = 100; // 5 seconds
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private LivingEntity cachedOwner;

    public SpiritBlockProjectileEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.damage = 10;
        this.noPhysics = false;
    }

    public SpiritBlockProjectileEntity(Level level, LivingEntity owner, BlockState blockState,
                                       float damage, Vec3 position, Vec3 velocity) {
        super(ModEntities.SPIRIT_BLOCK_PROJECTILE.get(), level);
        this.blockState = blockState;
        this.damage = damage;
        this.ownerUUID = owner.getUUID();
        this.cachedOwner = owner;
        this.noPhysics = false;

        this.setPos(position);
        this.setDeltaMovement(velocity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_POS, BlockPos.ZERO);
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public void setBlockState(BlockState state) {
        this.blockState = state;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.cachedOwner != null && this.cachedOwner.isAlive()) {
            return this.cachedOwner;
        }
        if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity living) {
                this.cachedOwner = living;
                return living;
            }
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > maxLifetime) {
            this.discard();
            return;
        }

        // Apply gravity
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x * 0.98, motion.y - 0.04, motion.z * 0.98);

        // Move the entity
        this.move(MoverType.SELF, this.getDeltaMovement());

        if (this.level().isClientSide) return;

        // Check for collision with the ground - discard without placing
        if (this.onGround() && this.tickCount > 5) {
            this.discard();
            return;
        }

        // Check for entity hits
        AABB hitBox = this.getBoundingBox().inflate(hitRadius);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, hitBox);

        LivingEntity owner = getOwner();

        for (LivingEntity entity : entities) {
            if (entity == owner) continue;
            if (owner != null && !AbilityUtil.mayTarget(owner, entity)) continue;

            double distSq = this.distanceToSqr(entity);
            if (distSq <= hitRadius * hitRadius) {
                // Deal damage
                if (owner != null) {
                    entity.hurt(ModDamageTypes.source(level(), ModDamageTypes.BEYONDER_GENERIC, owner), damage);
                } else {
                    entity.hurt(ModDamageTypes.source(level(), ModDamageTypes.BEYONDER_GENERIC), damage);
                }
                this.discard();
                return;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(this.blockState));
        tag.putFloat("Damage", this.damage);
        tag.putDouble("HitRadius", this.hitRadius);
        tag.putInt("MaxLifetime", this.maxLifetime);
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BlockState")) {
            this.blockState = NbtUtils.readBlockState(this.level().holderLookup(net.minecraft.core.registries.Registries.BLOCK), tag.getCompound("BlockState"));
        }
        if (tag.contains("Damage")) {
            this.damage = tag.getFloat("Damage");
        }
        if (tag.contains("HitRadius")) {
            this.hitRadius = tag.getDouble("HitRadius");
        }
        if (tag.contains("MaxLifetime")) {
            this.maxLifetime = tag.getInt("MaxLifetime");
        }
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}

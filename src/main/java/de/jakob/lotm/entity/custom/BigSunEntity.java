package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.UUID;

public class BigSunEntity extends Entity {

    private static final EntityDataAccessor<Integer> MAX_LIFETIME = SynchedEntityData.defineId(BigSunEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(BigSunEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> GRIEFING = SynchedEntityData.defineId(BigSunEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(BigSunEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public BigSunEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public BigSunEntity(Level level, float damage, boolean griefing, UUID ownerUUID, int maxLifetime) {
        super(ModEntities.BIG_SUN.get(), level);
        this.setDamage(damage);
        this.setGriefing(griefing);
        this.setOwnerUUID(Optional.ofNullable(ownerUUID));
        this.setMaxLifetime(maxLifetime);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MAX_LIFETIME, 600);
        builder.define(DAMAGE, 10.0F);
        builder.define(GRIEFING, false);
        builder.define(OWNER_UUID, Optional.empty());
    }

    public void setOwnerUUID(Optional<UUID> uuid) {
        this.entityData.set(OWNER_UUID, uuid);
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setGriefing(boolean griefing) {
        this.entityData.set(GRIEFING, griefing);
    }

    public boolean isGriefing() {
        return this.entityData.get(GRIEFING);
    }

    public void setMaxLifetime(int maxLifetime) {
        this.entityData.set(MAX_LIFETIME, maxLifetime);
    }

    public int getMaxLifetime() {
        return this.entityData.get(MAX_LIFETIME);
    }

    public LivingEntity getOwnerEntity() {
        if (this.level().isClientSide) {
            return null;
        }
        Optional<UUID> ownerUUID = this.getOwnerUUID();
        if (ownerUUID.isPresent()) {
            Entity entity = ((ServerLevel) this.level()).getEntity(ownerUUID.get());
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.setMaxLifetime(compoundTag.getInt("MaxLifetime"));
        this.setDamage(compoundTag.getFloat("Damage"));
        this.setGriefing(compoundTag.getBoolean("Griefing"));
        if (compoundTag.hasUUID("OwnerUUID")) {
            this.setOwnerUUID(Optional.of(compoundTag.getUUID("OwnerUUID")));
        } else {
            this.setOwnerUUID(Optional.empty());
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("MaxLifetime", this.getMaxLifetime());
        compoundTag.putFloat("Damage", this.getDamage());
        compoundTag.putBoolean("Griefing", this.isGriefing());
        this.getOwnerUUID().ifPresent(uuid -> compoundTag.putUUID("OwnerUUID", uuid));
    }

    @Override
    public void onAddedToLevel() {
        LivingEntity entity = getOwnerEntity();
        if(BeyonderData.isGriefingEnabled(entity) && level() instanceof ServerLevel serverLevel) {
            AbilityUtil.getBlocksInSphereRadius(serverLevel, position(), 25, true, true, false).forEach(
                    b -> serverLevel.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState())
            );

            AbilityUtil.getBlocksInSphereRadius(serverLevel, position(), 25, true).forEach(
                    b -> serverLevel.setBlockAndUpdate(b, Blocks.FIRE.defaultBlockState())
            );

            AbilityUtil.getBlocksInSphereRadius(serverLevel, position(), 26, true, true, false).forEach(
                    b -> serverLevel.setBlockAndUpdate(b, Blocks.BASALT.defaultBlockState())
            );
        }
        super.onAddedToLevel();
    }

    @Override
    public void tick() {
        super.tick();

        if(!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.FLAME, position(), 12.1f, 200);
        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, position(), 12.1f, 180);

        AbilityUtil.damageNearbyEntities(serverLevel, getOwnerEntity(), 120, getDamage(), position(), true, false, 20 * 2);

        if(tickCount >= 20 * 30) {
            this.remove(RemovalReason.DISCARDED);
        }
    }
}

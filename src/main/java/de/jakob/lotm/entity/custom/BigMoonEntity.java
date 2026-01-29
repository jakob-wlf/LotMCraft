package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

public class BigMoonEntity extends Entity {

    private static final EntityDataAccessor<Integer> MAX_LIFETIME = SynchedEntityData.defineId(BigMoonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(BigMoonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> GRIEFING = SynchedEntityData.defineId(BigMoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(BigMoonEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public BigMoonEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public BigMoonEntity(Level level, float damage, boolean griefing, UUID ownerUUID, int maxLifetime) {
        super(ModEntities.BIG_MOON.get(), level);
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
        super.onAddedToLevel();
    }

    private final DustParticleOptions dustParticleOptions = new DustParticleOptions(new Vector3f(.975f, .015f, .2f), 2.5f);

    @Override
    public void tick() {
        super.tick();

        if(!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ParticleUtil.spawnSphereParticles(serverLevel, dustParticleOptions, position(), 12.1f, 250);

        AbilityUtil.damageNearbyEntities(serverLevel, getOwnerEntity(), 120, getDamage(), position(), true, false);
        AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, getOwnerEntity(), 120, position(), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 3, false, false, false));
        AbilityUtil.getNearbyEntities(getOwnerEntity(), serverLevel, position(), 120).forEach(e ->  BeyonderData.reduceSpirituality(e, 5));

        if(tickCount >= 20 * 30) {
            this.remove(RemovalReason.DISCARDED);
        }
    }
}

package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class MeteorEntity extends Entity {
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> GRIEFING = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> CASTER_UUID = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private int lifeTicks = 0;
    private int maxLifeTicks = 20 * 12;

    public MeteorEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public MeteorEntity(Level level, float speed, float damage, float size, @Nullable Entity caster, boolean griefing) {
        super(ModEntities.Meteor.get(), level);
        this.setSpeed(speed);
        this.setDamage(damage);
        this.setSize(size);
        this.setGriefing(griefing);
        if (caster != null) {
            this.setCasterUUID(caster.getUUID());
        }
        this.noPhysics = false;
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPEED, 1.0f);
        builder.define(DAMAGE, 4.0f);
        builder.define(SIZE, 1f);
        builder.define(GRIEFING, false);
        builder.define(CASTER_UUID, Optional.empty());
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

    public float getSize() {
        return this.entityData.get(SIZE);
    }

    public void setSize(float size) {
        this.entityData.set(SIZE, size);
    }

    public boolean isGriefing() {
        return this.entityData.get(GRIEFING);
    }

    public void setGriefing(boolean griefing) {
        this.entityData.set(GRIEFING, griefing);
    }
    
    public void setCasterUUID(@Nullable UUID uuid) {
        this.entityData.set(CASTER_UUID, Optional.ofNullable(uuid));
    }
    
    @Nullable
    public UUID getCasterUUID() {
        return this.entityData.get(CASTER_UUID).orElse(null);
    }
    
    @Nullable
    private Entity getCaster() {
        UUID uuid = getCasterUUID();
        if (uuid != null && this.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(uuid);
        }
        return null;
    }

    boolean hasMovedToStartingLocation = false;
    Vec3 direction;
    Vec3 targetPos;

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        Vec3 newPos = position().add(16, 35, 5);
        targetPos = position();
        direction = position().subtract(0, 2, 0).subtract(newPos);
        setPos(newPos);
    }

    Vec3 lastPos;

    @Override
    public void tick() {
        super.tick();

        if(!(level() instanceof ServerLevel)) {
            return;
        }

        lifeTicks++;
        if (lifeTicks > maxLifeTicks) {
            this.discard();
            return;
        }

        setPos(position().add(direction.normalize().scale(getSpeed())));
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.FLAME, position(), 200, getSize() / 2.5f, .01);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.EXPLOSION, position(), 10, .2, .01);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.LARGE_SMOKE, position(), 40, getSize() / 2.5f, .01);

        if(position().distanceTo(targetPos) < .5 || (lastPos != null && position().distanceTo(targetPos) > lastPos.distanceTo(targetPos)) || !level().getBlockState(BlockPos.containing(position())).isAir()) {
            AbilityUtil.damageNearbyEntities((ServerLevel) level(), getCaster() instanceof LivingEntity l ? l : null, getSize() * 2, getDamage(), position(), true, false);
            level().explode(this, position().x, position().y, position().z, getSize() * 2.5f, isGriefing(), isGriefing() ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
            discard();
        }

        lastPos = position();
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setSpeed(tag.getFloat("Speed"));
        this.setDamage(tag.getFloat("Damage"));
        this.setGriefing(tag.getBoolean("Griefing"));
        this.setSize(tag.getFloat("Size"));
        this.lifeTicks = tag.getInt("LifeTicks");
        this.maxLifeTicks = tag.getInt("MaxLifeTicks");
        
        if (tag.hasUUID("CasterUUID")) {
            this.setCasterUUID(tag.getUUID("CasterUUID"));
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Speed", this.getSpeed());
        tag.putFloat("Damage", this.getDamage());
        tag.putFloat("Size", this.getSize());
        tag.putBoolean("Griefing", this.isGriefing());
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putInt("MaxLifeTicks", this.maxLifeTicks);
        
        UUID casterUUID = getCasterUUID();
        if (casterUUID != null) {
            tag.putUUID("CasterUUID", casterUUID);
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
}
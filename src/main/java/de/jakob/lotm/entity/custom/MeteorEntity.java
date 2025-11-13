package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
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
    private static final EntityDataAccessor<Float> EXPLOSION_SIZE = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> GRIEFING = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> CASTER_UUID = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private int lifeTicks = 0;
    private int maxLifeTicks = 20 * 12;

    public MeteorEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public MeteorEntity(Level level, float speed, float damage, float size, @Nullable Entity caster, boolean griefing, float explosionSize, float radius) {
        super(ModEntities.Meteor.get(), level);
        this.setSpeed(speed);
        this.setDamage(damage);
        this.setSize(size);
        this.setGriefing(griefing);
        this.setExplosionSize(explosionSize);
        this.setRadius(radius);
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
        builder.define(EXPLOSION_SIZE, 4.0f);
        builder.define(RADIUS, 12.0f);
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

    public float getExplosionSize() {
        return this.entityData.get(EXPLOSION_SIZE);
    }

    public float getRadius() {
        return this.entityData.get(RADIUS);
    }

    public void setExplosionSize(float size) {
        this.entityData.set(EXPLOSION_SIZE, size);
    }

    public void setRadius(float radius) {
        this.entityData.set(RADIUS, radius);
    }

    @Nullable
    private Entity getCaster() {
        UUID uuid = getCasterUUID();
        if (uuid != null && this.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(uuid);
        }
        return null;
    }

    Vec3 direction;
    Vec3 targetPos;

    public void setPosition(Vec3 pos) {
        targetPos = new Vec3(pos.x, pos.y, pos.z);
        Vec3 newPos = targetPos.add((random.nextDouble() - .5) * 80, 60, (random.nextDouble() - .5) * 80);
        direction = targetPos.subtract(newPos);
        this.setPos(newPos);
    }

    Vec3 lastPos;

    public int getLifeTicks() {
        return lifeTicks;
    }

    @Override
    public void tick() {
        super.tick();

        lifeTicks++;

        if(!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (lifeTicks > maxLifeTicks) {
            this.discard();
            return;
        }

        if(direction == null || targetPos == null) {
            return;
        }

        moveTo(position().add(direction.normalize().scale(getSpeed())));

        if(position().distanceTo(targetPos.subtract(0, 1, 0)) < .5 || (lastPos != null && position().distanceTo(targetPos.subtract(0, 1, 0)) > lastPos.distanceTo(targetPos.subtract(0, 1, 0))) || !level().getBlockState(BlockPos.containing(position())).isAir()) {
            AbilityUtil.damageNearbyEntities(serverLevel, getCaster() instanceof LivingEntity l ? l : null, getRadius(), getDamage(), position(), true, false);
            EffectManager.playEffect(EffectManager.Effect.EXPLOSION, position().x, position().y, position().z, serverLevel);
            serverLevel.explode(this, position().x, position().y, position().z, getRadius(), isGriefing(), isGriefing() ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
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
        this.setExplosionSize(tag.getFloat("ExplosionSize"));
        this.setRadius(tag.getFloat("Radius"));
        
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
        tag.putFloat("ExplosionSize", this.getExplosionSize());
        tag.putFloat("Radius", this.getRadius());
        
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
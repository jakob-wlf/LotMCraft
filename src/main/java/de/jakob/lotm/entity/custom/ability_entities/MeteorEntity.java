package de.jakob.lotm.entity.custom.ability_entities;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.PerformantExplosion;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
    private static final EntityDataAccessor<Boolean> CUSTOM_COLOR = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> COLOR_R = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> COLOR_G = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> COLOR_B = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> ABYSS_IMPACT = SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.BOOLEAN);

    private int lifeTicks = 0;
    private int petrifiedTicks = 0;
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
        builder.define(CUSTOM_COLOR, false);
        builder.define(COLOR_R, 1.0f);
        builder.define(COLOR_G, 0.55f);
        builder.define(COLOR_B, 0.0f);
        builder.define(ABYSS_IMPACT, false);
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

    public boolean isCustomColor() { return this.entityData.get(CUSTOM_COLOR); }
    public void setCustomColor(boolean v) { this.entityData.set(CUSTOM_COLOR, v); }
    public float getColorR() { return this.entityData.get(COLOR_R); }
    public float getColorG() { return this.entityData.get(COLOR_G); }
    public float getColorB() { return this.entityData.get(COLOR_B); }
    public void setColor(float r, float g, float b) {
        this.entityData.set(COLOR_R, r);
        this.entityData.set(COLOR_G, g);
        this.entityData.set(COLOR_B, b);
    }
    public boolean isAbyssImpact() { return this.entityData.get(ABYSS_IMPACT); }
    public void setAbyssImpact(boolean v) { this.entityData.set(ABYSS_IMPACT, v); }

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
        // Petrification Logic -- run before super.tick() to stop movement completely
        if(getTags().contains("petrified")) {
            petrifiedTicks++;
            if(petrifiedTicks >= 20 * 5) {
                this.discard();
            }
            return;
        }

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
            PerformantExplosion.create(serverLevel, getCaster(), position(), getExplosionSize(), isGriefing(), isGriefing() ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.KEEP);

            if (isAbyssImpact()) {
                // Spawn a cluster of green pillars around the impact point
                EffectManager.playEffect(EffectManager.Effect.ABYSS_PILLAR, position().x, position().y, position().z, serverLevel);
                for (int i = 0; i < 3; i++) {
                    double ox = (random.nextDouble() - 0.5) * 8;
                    double oz = (random.nextDouble() - 0.5) * 8;
                    EffectManager.playEffect(EffectManager.Effect.ABYSS_PILLAR, position().x + ox, position().y, position().z + oz, serverLevel);
                }
                // Apply abyss debuffs to nearby entities
                double r = getRadius();
                serverLevel.getEntitiesOfClass(LivingEntity.class,
                        new AABB(position().x - r, position().y - r, position().z - r,
                                 position().x + r, position().y + r, position().z + r))
                        .stream()
                        .filter(e -> !e.equals(getCaster()) && position().distanceTo(e.position()) <= r)
                        .forEach(e -> {
                            e.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 15, 3));
                            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 8, 2));
                            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 4, 1));
                        });
            }

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
        this.setCustomColor(tag.getBoolean("CustomColor"));
        this.setColor(tag.getFloat("ColorR"), tag.getFloat("ColorG"), tag.getFloat("ColorB"));
        this.setAbyssImpact(tag.getBoolean("AbyssImpact"));

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
        tag.putBoolean("CustomColor", this.isCustomColor());
        tag.putFloat("ColorR", this.getColorR());
        tag.putFloat("ColorG", this.getColorG());
        tag.putFloat("ColorB", this.getColorB());
        tag.putBoolean("AbyssImpact", this.isAbyssImpact());

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
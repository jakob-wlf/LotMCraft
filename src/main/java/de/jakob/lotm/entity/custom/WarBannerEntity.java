package de.jakob.lotm.entity.custom;

import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

public class WarBannerEntity extends Entity {

    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.defineId(WarBannerEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> RADIUS =
            SynchedEntityData.defineId(WarBannerEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(WarBannerEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public WarBannerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        setDuration(20 * 60 * 2);
        setRadius(25);
        this.noPhysics = true;
        this.noCulling = true;

    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if(level().isClientSide)
            return;

        if(getDuration() <= 0)
            setDuration(20 * 60 * 2);
        if(getRadius() <= 0)
            setRadius(25);

        ParticleUtil.createParticleSpirals(ParticleTypes.FLAME, new Location(position(), level()), 3, 3, 5.5, .35, 5, 20 * 30, 15, 8);
    }

    public WarBannerEntity(EntityType<?> entityType, Level level, int ticks, UUID casterUUID) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setDuration(ticks);
        this.setCasterUUID(casterUUID);
    }

    int lifetime = 0;

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(.45f, .25f, .25f), 1.5f);

    @Override
    public void tick() {
        super.tick();

        if(level().isClientSide)
            return;

        lifetime++;
        if(lifetime >= getDuration()) {
            discard();
            return;
        }

        spawnParticles();

        AbilityUtil.getNearbyEntities(null, (ServerLevel) level(), position(), getRadius()).forEach(e -> {
            ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.FLAME, e.position().add(0, e.getBbHeight() / 2, 0), 1, .4, 1, .4, 0);
            ParticleUtil.spawnParticles((ServerLevel) level(), dust, e.position().add(0, e.getBbHeight() / 2, 0), 7, .4, 1, .4, 0);

            if(e.getUUID().equals(getCasterUUID())) {
                BeyonderData.addModifier(e, "war_song", 1.5f);

                ServerScheduler.scheduleDelayed(20, () -> {
                    if(e.level() != level() || e.isDeadOrDying() || e.distanceTo(this) > getRadius()) {
                        BeyonderData.removeModifier(e, "war_song");
                    }
                });

                e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 7, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 7, false, false, false));
                return;
            }

            BeyonderData.addModifier(e, "war_song", .75f);

            ServerScheduler.scheduleDelayed(20, () -> {
                if(e.level() != level() || e.isDeadOrDying() || e.distanceTo(this) > getRadius()) {
                    BeyonderData.removeModifier(e, "war_song");
                }
            });

            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 4, false, false, false));
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));
        });
    }

    private void spawnParticles() {
        ParticleUtil.spawnCircleParticles((ServerLevel) level(), ParticleTypes.FLAME, this.position(), getRadius(), getRadius() * 8);
    }

    public void setDuration(int duration) {
        this.entityData.set(DURATION, duration);
    }

    public int getDuration() {
        return this.entityData.get(DURATION);
    }

    public void setCasterUUID(UUID uuid) {
        this.entityData.set(OWNER, Optional.ofNullable(uuid));
    }

    public UUID getCasterUUID() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public int getRadius() {
        return this.entityData.get(RADIUS);
    }

    public void setRadius(int radius) {
        this.entityData.set(RADIUS, radius);
    }

    public Entity getCasterEntity() {
        if(level().isClientSide) {
            return null;
        }
        UUID casterUUID = this.getCasterUUID();
        if (casterUUID == null) {
            return null;
        }
        return ((ServerLevel) level()).getEntity(casterUUID);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DURATION, 20 * 60 * 2);
        builder.define(OWNER, Optional.empty());
        builder.define(RADIUS, 25);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setDuration(compoundTag.getInt("duration"));
        setRadius(compoundTag.getInt("radius"));
        if (compoundTag.hasUUID("owner")) {
            setCasterUUID(compoundTag.getUUID("owner"));
        } else {
            setCasterUUID(null);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("duration", getDuration());
        compoundTag.putInt("radius", getRadius());
        if (getCasterUUID() != null) {
            compoundTag.putUUID("owner", getCasterUUID());
        }
    }
}

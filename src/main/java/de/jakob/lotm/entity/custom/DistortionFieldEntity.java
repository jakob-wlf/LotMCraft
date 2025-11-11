package de.jakob.lotm.entity.custom;

import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class DistortionFieldEntity extends Entity {

    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.defineId(DistortionFieldEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> RADIUS =
            SynchedEntityData.defineId(DistortionFieldEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(DistortionFieldEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Boolean> GRIEFING =
            SynchedEntityData.defineId(DistortionFieldEntity.class, EntityDataSerializers.BOOLEAN);

    public DistortionFieldEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        setDuration(20 * 60 * 2);
        setRadius(40);
        setGriefing(false);
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
            setRadius(40);

    }

    public DistortionFieldEntity(EntityType<?> entityType, Level level, int ticks, UUID casterUUID, boolean griefing) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setDuration(ticks);
        this.setCasterUUID(casterUUID);
        this.setGriefing(griefing);
    }

    int lifetime = 0;

    @Override
    public void tick() {
        super.tick();

        if(!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        lifetime++;
        if(lifetime >= getDuration()) {
            discard();
            return;
        }
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
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

    public boolean getGriefing() {
        return this.entityData.get(GRIEFING);
    }

    public void setGriefing(boolean griefing) {
        this.entityData.set(GRIEFING, griefing);
    }

    public LivingEntity getCasterEntity() {
        if(level().isClientSide) {
            return null;
        }
        UUID casterUUID = this.getCasterUUID();
        if (casterUUID == null) {
            return null;
        }
        return ((ServerLevel) level()).getEntity(casterUUID) != null && ((ServerLevel) level()).getEntity(casterUUID) instanceof LivingEntity livingEntity ?
                livingEntity: null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DURATION, 20 * 60 * 2);
        builder.define(OWNER, Optional.empty());
        builder.define(RADIUS, 40);
        builder.define(GRIEFING, true);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setDuration(compoundTag.getInt("duration"));
        setRadius(compoundTag.getInt("radius"));
        setGriefing(compoundTag.getBoolean("griefing"));
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
        compoundTag.putBoolean("griefing", getGriefing());
        if (getCasterUUID() != null) {
            compoundTag.putUUID("owner", getCasterUUID());
        }
    }
}
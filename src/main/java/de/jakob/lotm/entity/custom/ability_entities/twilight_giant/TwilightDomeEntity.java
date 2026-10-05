package de.jakob.lotm.entity.custom.ability_entities.twilight_giant;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class TwilightDomeEntity extends TwilightVisualEntity {
    public static final int SUNSET_TICKS = 200;
    public static final int BLACK_TICKS = 100;
    public static final float LOCAL_RADIUS = 12.0F;
    public static final float SUN_HIGH = 30.0F;
    public static final float SUN_LOW = -20.0F;
    public static final float SUN_HORIZON = SUN_HIGH / (SUN_HIGH - SUN_LOW);

    private static final EntityDataAccessor<Boolean> SUNSET =
            SynchedEntityData.defineId(TwilightDomeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> SUBJECT =
            SynchedEntityData.defineId(TwilightDomeEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> SUN =
            SynchedEntityData.defineId(TwilightDomeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BLACK =
            SynchedEntityData.defineId(TwilightDomeEntity.class, EntityDataSerializers.FLOAT);

    private float sunFrom;
    private float sunTo;
    private boolean sunReady;

    public TwilightDomeEntity(EntityType<? extends TwilightDomeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SUNSET, false);
        builder.define(SUBJECT, Optional.empty());
        builder.define(SUN, 0.0F);
        builder.define(BLACK, 0.0F);
    }

    public void setSunset(boolean sunset) {
        this.entityData.set(SUNSET, sunset);
    }

    public boolean isSunset() {
        return this.entityData.get(SUNSET);
    }

    public void setSubject(UUID subject) {
        this.entityData.set(SUBJECT, Optional.ofNullable(subject));
    }

    public UUID subject() {
        return this.entityData.get(SUBJECT).orElse(null);
    }

    public void setSun(float sun) {
        this.entityData.set(SUN, Mth.clamp(sun, 0.0F, 1.0F));
    }

    public float sun() {
        return this.entityData.get(SUN);
    }

    public void setBlack(float black) {
        this.entityData.set(BLACK, Mth.clamp(black, 0.0F, 1.0F));
    }

    public float black() {
        return this.entityData.get(BLACK);
    }

    public float sunsetProgress(float partialTick) {
        if (this.level().isClientSide()) return Mth.lerp(partialTick, this.sunFrom, this.sunTo);
        return sun();
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            float value = sun();
            if (!this.sunReady) {
                this.sunFrom = value;
                this.sunTo = value;
                this.sunReady = true;
            } else {
                this.sunFrom = this.sunTo;
                this.sunTo = value;
            }
        }
        super.tick();
        if (this.level().isClientSide() || !(this.level() instanceof ServerLevel server)) return;
        UUID id = subject();
        if (id == null || !(server.getEntity(id) instanceof LivingEntity living) || living.level() != this.level()) return;
        this.moveTo(living.getX(), living.getY(), living.getZ(), living.getYRot(), 0.0F);
    }
}

package de.jakob.lotm.entity.custom.ability_entities.twilight_giant;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TwilightSlashEntity extends TwilightVisualEntity {
    public static final int FLIGHT_END = 40;
    public static final int LIFETIME = 58;
    public static final float ARC_HEIGHT = 1.0F;

    private static final EntityDataAccessor<Integer> OWNER =
            SynchedEntityData.defineId(TwilightSlashEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ARC_Y =
            SynchedEntityData.defineId(TwilightSlashEntity.class, EntityDataSerializers.FLOAT);

    public TwilightSlashEntity(EntityType<? extends TwilightSlashEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, -1);
        builder.define(ARC_Y, 0.0F);
    }

    public void setOwner(Entity owner) {
        this.entityData.set(OWNER, owner.getId());
        this.entityData.set(ARC_Y, (float) owner.getY() + ARC_HEIGHT);
    }

    public Entity owner() {
        return this.level().getEntity(this.entityData.get(OWNER));
    }

    public float arcY() {
        float y = this.entityData.get(ARC_Y);
        return y == 0.0F ? (float) getY() + ARC_HEIGHT : y;
    }

    public Vec3 pathStart(float partial) {
        Entity owner = owner();
        if (owner != null) {
            double x = Mth.lerp(partial, owner.xo, owner.getX());
            double y = Mth.lerp(partial, owner.yo, owner.getY());
            double z = Mth.lerp(partial, owner.zo, owner.getZ());
            return new Vec3(x, y + ARC_HEIGHT, z);
        }
        return new Vec3(getX(), getY() + ARC_HEIGHT, getZ());
    }

    public Vec3 pathEnd() {
        return new Vec3(getX(), arcY(), getZ());
    }

    public static float ease(float x) {
        float p = Mth.clamp(x, 0.0F, 1.0F);
        return 0.5F * p + 0.5F * p * p * (3.0F - 2.0F * p);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age >= LIFETIME + (this.level().isClientSide ? 20 : 0)) discard();
    }
}

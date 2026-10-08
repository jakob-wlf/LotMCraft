package de.jakob.lotm.entity.custom.ability_entities.twilight_giant;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ProtectionBarrierEntity extends Entity {
    public static final int GROW_TICKS = 24;
    public static final int FADE_TICKS = 15;

    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(ProtectionBarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CLOSE_AT =
            SynchedEntityData.defineId(ProtectionBarrierEntity.class, EntityDataSerializers.INT);

    public ProtectionBarrierEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setRadius(float radius) {
        this.entityData.set(RADIUS, radius);
    }

    public float radius() {
        return this.entityData.get(RADIUS);
    }

    public int closeAt() {
        return this.entityData.get(CLOSE_AT);
    }

    public void close() {
        if (closeAt() < 0) this.entityData.set(CLOSE_AT, 0);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (closeAt() >= 0) this.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 15.0F);
        builder.define(CLOSE_AT, -1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}

package de.jakob.lotm.entity.custom.ability_entities.twilight_giant;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class TwilightVisualEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(TwilightVisualEntity.class, EntityDataSerializers.FLOAT);

    protected int age;

    protected TwilightVisualEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 1.0F);
    }

    public void setRadius(float radius) {
        this.entityData.set(RADIUS, radius);
    }

    public float radius() {
        return this.entityData.get(RADIUS);
    }

    public float getAge(float partialTick) {
        return this.age + partialTick;
    }

    public float rise(float partialTick) {
        float in = Mth.clamp(getAge(partialTick) / 12.0F, 0.0F, 1.0F);
        return Math.max(0.01F, 1.0F - (1.0F - in) * (1.0F - in));
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}

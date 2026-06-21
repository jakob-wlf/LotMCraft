package de.jakob.lotm.entity.custom.ability_entities.door_pathway;

import de.jakob.lotm.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MysticalDoorEntity extends Entity {

    private static final EntityDataAccessor<Integer> TEXTURE_INDEX =
            SynchedEntityData.defineId(MysticalDoorEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> SIZE =
            SynchedEntityData.defineId(MysticalDoorEntity.class, EntityDataSerializers.FLOAT);

    private final int maxLifeTime;

    public MysticalDoorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.maxLifeTime = -1;
    }

    public MysticalDoorEntity(Level level, Vec3 pos, int textureIndex, int size, int maxLifeTime) {
        super(ModEntities.MYSTICAL_DOOR.get(), level);
        setPos(pos);
        setTextureIndex(textureIndex);
        setSize(size);
        this.maxLifeTime = maxLifeTime;
    }

    @Override
    public void tick() {
        super.tick();

        if(!level().isClientSide && maxLifeTime > 0 && tickCount > maxLifeTime) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TEXTURE_INDEX, 1);
        builder.define(SIZE, 1f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    public int getTextureIndex() {
        return entityData.get(TEXTURE_INDEX);
    }

    public void setTextureIndex(int textureIndex) {
        entityData.set(TEXTURE_INDEX, Math.clamp(textureIndex, 1, 5));
    }

    public float getSize() {
        return entityData.get(SIZE);
    }

    public void setSize(float size) {
        entityData.set(SIZE, Math.clamp(size, 1, 5));
    }


}
package de.jakob.lotm.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class SunEntity extends Entity {
    public SunEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    int lifetime = 0;

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if(lifetime >= 20 * 30) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}

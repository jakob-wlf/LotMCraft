package de.jakob.lotm.entity.custom.ability_entities.door_pathway;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PlanetEntity extends Entity {
    public PlanetEntity(EntityType<?> entityType, Level level) {
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


    @Override
    public void tick() {
        super.tick();

        if(tickCount >= 20 * 20) {
            this.remove(RemovalReason.DISCARDED);
        }
    }
}

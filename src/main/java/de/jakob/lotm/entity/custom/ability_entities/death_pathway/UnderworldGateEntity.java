package de.jakob.lotm.entity.custom.ability_entities.death_pathway;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.BlackHoleEntity;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class UnderworldGateEntity extends Entity {

    public AnimationState openAnimationState = new AnimationState();
    public AnimationState tentacleAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> HAS_TENTACLES = SynchedEntityData.defineId(UnderworldGateEntity.class, EntityDataSerializers.BOOLEAN);


    public UnderworldGateEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    public UnderworldGateEntity(Level level, boolean hasTentacles) {
        super(ModEntities.UNDERWORLD_GATE.get(), level);
        setHasTentacles(hasTentacles);
    }

    @Override
    public void tick() {
        if(level().isClientSide) {
            if (this.tickCount == 1) {
                openAnimationState.start(0);
            }

            if(this.tickCount >= 2 * 20) {
                tentacleAnimationState.startIfStopped(0);
            }
            return;
        }

        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.SOUL, position().add(getLookAngle().normalize().scale(.85)), 12, .8, 0.25, .8, 0);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.ENCHANT, getEyePosition().subtract(0, .2, 0), 8, 1.5, 1.5, 1.5, 0);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.SMOKE, getEyePosition().subtract(0, .2, 0), 8, 1.5, 1.5, 1.5, 0);

        if(this.tickCount > 20 * 60 * 2) {
            this.discard();
        }
    }

    public void setHasTentacles(boolean hasTentacles) {
        entityData.set(HAS_TENTACLES, hasTentacles);
    }

    public boolean hasTentacles() {
        return entityData.get(HAS_TENTACLES);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HAS_TENTACLES, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setHasTentacles(compoundTag.getBoolean("HasTentacles"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("HasTentacles", hasTentacles());
    }
}

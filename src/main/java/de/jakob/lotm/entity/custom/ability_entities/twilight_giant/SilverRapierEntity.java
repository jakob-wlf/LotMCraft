package de.jakob.lotm.entity.custom.ability_entities.twilight_giant;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class SilverRapierEntity extends Mob {
    private static final EntityDataAccessor<Boolean> STRIKING =
            SynchedEntityData.defineId(SilverRapierEntity.class, EntityDataSerializers.BOOLEAN);

    public SilverRapierEntity(EntityType<? extends SilverRapierEntity> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setSilent(true);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setPersistenceRequired();
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50.0D);
        this.setHealth(this.getMaxHealth());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STRIKING, false);
    }

    public void setStriking(boolean striking) {
        this.entityData.set(STRIKING, striking);
    }

    public boolean isStriking() {
        return this.entityData.get(STRIKING);
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        this.setNoGravity(true);
        super.tick();
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}

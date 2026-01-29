package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CoffinEntity extends Entity {

    private static final EntityDataAccessor<Boolean> ANIMATION_PLAYED =
            SynchedEntityData.defineId(CoffinEntity.class, EntityDataSerializers.BOOLEAN);

    public CoffinEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // Disable physics
        this.noCulling = true; // Always render regardless of culling
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ANIMATION_PLAYED, false);
    }

    public CoffinEntity(Level level, Vec3 pos) {
        this(ModEntities.COFFIN.get(), level);
        this.setPos(pos);
        this.setXRot(90);
        this.setYRot(0);
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount == 10 && !this.hasPlayedAnimation()) {
            this.setAnimationPlayed(true);
        }

        if(tickCount > 20 * 60) {
            this.discard();
        }
    }

    public boolean hasPlayedAnimation() {
        return this.entityData.get(ANIMATION_PLAYED);
    }

    public void setAnimationPlayed(boolean played) {
        this.entityData.set(ANIMATION_PLAYED, played);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("AnimationPlayed")) {
            this.setAnimationPlayed(compoundTag.getBoolean("AnimationPlayed"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("AnimationPlayed", this.hasPlayedAnimation());
    }

    private final net.minecraft.world.entity.AnimationState animationState = new net.minecraft.world.entity.AnimationState();

    public net.minecraft.world.entity.AnimationState getAnimationState() {
        if (tickCount == 0) {
            animationState.start(tickCount);
        }
        return animationState;
    }

    @Override
    public boolean isPickable() {
        return false; // Players can't interact with it directly
    }

    @Override
    public boolean isPushable() {
        return false; // Can't be pushed by other entities
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false; // No passengers allowed
    }
}
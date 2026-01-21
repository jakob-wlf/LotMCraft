package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CoffinEntity extends Entity {
    // Required constructors for entity system
    public CoffinEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // Disable physics
        this.noCulling = true; // Always render regardless of culling
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    // Main constructor for placing the door
    public CoffinEntity(Level level, Vec3 pos) {
        this(ModEntities.COFFIN.get(), level);
        this.setPos(pos);
        this.setXRot(90);
        this.setYRot(0);
    }
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        // Render from a reasonable distance
        return distance < 4096.0; // 64 block radius
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

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
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

public class ApprenticeBookEntity extends Entity {
    // Required constructors for entity system
    public ApprenticeBookEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // Disable physics
        this.noCulling = true; // Always render regardless of culling
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    // Main constructor for placing the door
    public ApprenticeBookEntity(Level level, Vec3 pos) {
        this(ModEntities.APPRENTICE_BOOK.get(), level);
        this.setPos(pos);
        this.setXRot(90);
        this.setYRot(0);
    }

    public ApprenticeBookEntity(Level level, Vec3 pos, Vec3 facing) {
        this(ModEntities.APPRENTICE_BOOK.get(), level);
        this.setPos(pos);
        setFacingDirection(facing);
    }

    public void setFacingDirection(Vec3 direction) {
        // Normalize the direction vector
        Vec3 normalizedDir = direction.normalize();

        // Calculate yaw (horizontal rotation)
        float yaw = (float) (Mth.atan2(-normalizedDir.x, normalizedDir.z) * (180.0 / Math.PI));

        // Calculate pitch (vertical rotation) - use Math.asin
        float pitch = (float) (Math.asin(-normalizedDir.y) * (180.0 / Math.PI));

        // Set the entity's rotation
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
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
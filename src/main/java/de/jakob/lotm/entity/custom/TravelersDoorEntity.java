package de.jakob.lotm.entity.custom;

import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TravelersDoorEntity extends Entity {
    private double destX;
    private double destY;
    private double destZ;
    private static final double TELEPORT_RANGE = 1.0; // Distance at which entities teleport

    // Required constructors for entity system
    public TravelersDoorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // Disable physics
        this.noCulling = true; // Always render regardless of culling
        this.destX = 0.0;
        this.destY = 0.0;
        this.destZ = 0.0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // You can add synched data here if needed for animation state
    }

    // Main constructor for placing the door
    public TravelersDoorEntity(EntityType<? extends TravelersDoorEntity> type, Level level, Vec3 facing, Vec3 center) {
        this(type, level);

        // Doors usually don't pitch. Zero-out Y so we only compute yaw.
        Vec3 dir = new Vec3(facing.x, 0.0, facing.z);
        float yaw = yawFromVector(dir);            // compute yaw from vector
        float pitch = 0.0F;                        // keep door upright

        // Sets pos + yaw/pitch + "old" fields in one call (important for first-frame render)
        this.moveTo(center.x, center.y, center.z, yaw, pitch);
    }

    // Constructor with destination coordinates
    public TravelersDoorEntity(EntityType<? extends TravelersDoorEntity> type, Level level, Vec3 facing, Vec3 center, double destX, double destY, double destZ) {
        this(type, level, facing, center);
        this.destX = destX;
        this.destY = destY;
        this.destZ = destZ;
    }

    private static float yawFromVector(Vec3 dir) {
        if (dir.lengthSqr() < 1.0E-6) return 0.0F; // avoid NaN if looking straight up/down
        // Matches vanilla convention used by vehicles: +yaw clockwise; Z forward
        return (float)(Math.toDegrees(Math.atan2(-dir.x, dir.z)));
    }

    @Override
    public void tick() {
        super.tick();

        // Only check for nearby entities on the server
        if (!this.level().isClientSide) {
            // Get all entities within teleport range
            for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(TELEPORT_RANGE), e -> e != this && e.isAlive())) {
                // Teleport the entity to destination coordinates
                entity.teleportTo(destX, destY, destZ);

                // Optional: Reset fall distance to prevent damage
                entity.resetFallDistance();
            }
        }
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
        if (compoundTag.contains("DestX")) {
            this.destX = compoundTag.getDouble("DestX");
        }
        if (compoundTag.contains("DestY")) {
            this.destY = compoundTag.getDouble("DestY");
        }
        if (compoundTag.contains("DestZ")) {
            this.destZ = compoundTag.getDouble("DestZ");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putDouble("DestX", this.destX);
        compoundTag.putDouble("DestY", this.destY);
        compoundTag.putDouble("DestZ", this.destZ);
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

    // Getters and setters for destination coordinates
    public double getDestX() {
        return destX;
    }

    public void setDestX(double destX) {
        this.destX = destX;
    }

    public double getDestY() {
        return destY;
    }

    public void setDestY(double destY) {
        this.destY = destY;
    }

    public double getDestZ() {
        return destZ;
    }

    public void setDestZ(double destZ) {
        this.destZ = destZ;
    }

    public void setDestination(double x, double y, double z) {
        this.destX = x;
        this.destY = y;
        this.destZ = z;
    }
}
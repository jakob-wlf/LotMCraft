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
    private int duration = 20 * 10;

    private static final Set<UUID> haveTeleported = new HashSet<>();

    // Required constructors for entity system
    public TravelersDoorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // Disable physics
        this.noCulling = true; // Always render regardless of culling
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    // Main constructor for placing the door
    public TravelersDoorEntity(EntityType<?> entityType, Level level, Direction facing, Vec3 blockCenter) {
        this(entityType, level);

        // Position the entity based on the block face
        Vec3 doorPosition = calculateDoorPosition(blockCenter, facing);
        this.setPos(doorPosition.x, doorPosition.y, doorPosition.z);
    }

    private Vec3 calculateDoorPosition(Vec3 blockCenter, Direction facing) {
        // Offset from block center to place door on the specified face
        Vec3 offset = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ()).normalize().multiply(.6, 0, .505); // Slightly offset from block face

        return blockCenter.add(offset);
    }

    private final DustParticleOptions blueDust = new DustParticleOptions(
            new Vector3f(99 / 255f, 255 / 255f, 250 / 255f),
            1
    );

    @Override
    public void tick() {
        super.tick();
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
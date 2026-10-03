package de.jakob.lotm.entity.custom.ability_entities.door_pathway;

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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ApprenticeDoorEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_FACING = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> MAX_RADIUS = SynchedEntityData.defineId(ApprenticeDoorEntity.class, EntityDataSerializers.INT);

    private int duration = 20 * 10;
    private Vec3 teleportPos;

    private static final Set<UUID> haveTeleported = new HashSet<>();

    public ApprenticeDoorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        teleportPos = this.position();
    }

    public ApprenticeDoorEntity(EntityType<?> entityType, Level level, Direction facing, Vec3 blockCenter, int duration, int maxRadius) {
        this(entityType, level);
        this.setFacing(facing);
        this.duration = duration;

        Vec3 doorPosition = calculateDoorPosition(blockCenter, facing);
        this.setPos(doorPosition.x, doorPosition.y, doorPosition.z);

        this.setYRot(facing.toYRot());
        this.setXRot(0);
        this.setMaxRadius(maxRadius);

        calculateTeleportPos();
    }

    public void setOnlyVisibleForCertainPlayer(UUID playerUUID) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(playerUUID));
    }

    private void calculateTeleportPos() {
        Vec3 startingPos = this.position();
        Vec3 dir = new Vec3(getFacing().getStepX(), 0, getFacing().getStepZ()).normalize().scale(-1);
        for(int i = 0; i < getMaxRadius(); i++) {
            startingPos = startingPos.add(dir);
            if(level().getBlockState(BlockPos.containing(startingPos.x, startingPos.y, startingPos.z)).isAir())
                break;
        }

        teleportPos = startingPos;
    }

    private Vec3 calculateDoorPosition(Vec3 blockCenter, Direction facing) {
        Vec3 offset = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ()).normalize().multiply(.6, 0, .505);

        return blockCenter.add(offset);
    }

    private final DustParticleOptions blueDust = new DustParticleOptions(
            new Vector3f(99 / 255f, 255 / 255f, 250 / 255f),
            1
    );

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide)
            return;

        // Handle duration and removal
        if (--duration <= 0) {
            this.discard();
            return;
        }

        // Prevent the entity from moving
        this.setDeltaMovement(Vec3.ZERO);

        for(LivingEntity e : AbilityUtil.getNearbyEntities(null, (ServerLevel) level(), this.position(), 1, true)) {
            if(haveTeleported.contains(e.getUUID()))
                continue;

            float newYaw = this.getYRot() + 180.0F;
            float newPitch = this.getXRot();

            e.teleportTo(
                    (ServerLevel) this.level(),
                    teleportPos.x,
                    teleportPos.y,
                    teleportPos.z,
                    Set.of(),
                    newYaw,
                    newPitch
            );
            this.level().playSound(null, teleportPos.x, teleportPos.y, teleportPos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, .1f, 1);
            this.level().playSound(null, teleportPos.x, teleportPos.y, teleportPos.z, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1, 1);

            ParticleUtil.spawnParticles((ServerLevel) this.level(), ParticleTypes.END_ROD, teleportPos.add(0, .5, 0), 35, .35, .75, .35, .05);
            ParticleUtil.spawnParticles((ServerLevel) this.level(), blueDust, teleportPos.add(0, .5, 0), 35, .35, .75, .35, .05);

            haveTeleported.add(e.getUUID());
            ServerScheduler.scheduleDelayed(35, () -> haveTeleported.remove(e.getUUID()), (ServerLevel) this.level());
        }

        if (this.random.nextFloat() < 0.075f) {
            spawnAmbientParticles();
        }
    }

    private void spawnAmbientParticles() {
        if(this.getOwnerUUID() == null)
            ParticleUtil.spawnParticles((ServerLevel) this.level(), ParticleTypes.END_ROD, this.getEyePosition().subtract(0, this.getEyeHeight() / 2, 0), 2, .55, 1, .55, .025);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FACING, Direction.NORTH.get3DDataValue());
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(MAX_RADIUS, 5);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.setFacing(Direction.from3DDataValue(compound.getInt("Facing")));
        if (compound.hasUUID("Owner")) {
            this.setOwnerUUID(compound.getUUID("Owner"));
        }
        this.duration = compound.getInt("Duration");
        this.setMaxRadius(compound.getInt("MaxRadius"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Facing", this.getFacing().get3DDataValue());
        UUID owner = this.getOwnerUUID();
        if (owner != null) {
            compound.putUUID("Owner", owner);
        }
        compound.putInt("Duration", this.duration);
        compound.putInt("MaxRadius", this.getMaxRadius());
    }

    public Direction getFacing() {
        return Direction.from3DDataValue(this.entityData.get(DATA_FACING));
    }

    public void setFacing(Direction facing) {
        this.entityData.set(DATA_FACING, facing.get3DDataValue());
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public int getMaxRadius() {
        return this.entityData.get(MAX_RADIUS);
    }

    public void setMaxRadius(int maxRadius) {
        this.entityData.set(MAX_RADIUS, maxRadius);
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
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
    private int spawnAnimationTicks = 0; // Track animation progress
    private boolean isSpawnAnimationFinished = false;

    // Animation duration in ticks (1 second = 20 ticks)
    private static final float SPAWN_ANIMATION_DURATION = 20 * 0.6667f; // 1 second to match the animation

    private static final Set<UUID> haveTeleported = new HashSet<>();

    // Required constructors for entity system
    public TravelersDoorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // Disable physics
        this.noCulling = true; // Always render regardless of culling
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


    private static float yawFromVector(Vec3 dir) {
        if (dir.lengthSqr() < 1.0E-6) return 0.0F; // avoid NaN if looking straight up/down
        // Matches vanilla convention used by vehicles: +yaw clockwise; Z forward
        return (float)(Math.toDegrees(Math.atan2(-dir.x, dir.z)));
    }


    @Override
    public void tick() {
        super.tick();

        // Handle spawn animation
        if (!isSpawnAnimationFinished) {
            spawnAnimationTicks++;

            if (spawnAnimationTicks >= SPAWN_ANIMATION_DURATION) {
                isSpawnAnimationFinished = true;
            }
        }
    }

    /**
     * Returns the animation progress from 0.0 to 1.0
     */
    public float getSpawnAnimationProgress() {
        if (isSpawnAnimationFinished) {
            return 1.0f;
        }
        return Math.min(spawnAnimationTicks / (float) SPAWN_ANIMATION_DURATION, 1.0f);
    }

    /**
     * Returns whether the spawn animation is currently playing
     */
    public boolean isPlayingSpawnAnimation() {
        return !isSpawnAnimationFinished;
    }

    /**
     * Returns the age in ticks, useful for animations
     */
    public int getAnimationAge() {
        return spawnAnimationTicks;
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
        this.spawnAnimationTicks = compoundTag.getInt("SpawnAnimationTicks");
        this.isSpawnAnimationFinished = compoundTag.getBoolean("SpawnAnimationFinished");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("SpawnAnimationTicks", this.spawnAnimationTicks);
        compoundTag.putBoolean("SpawnAnimationFinished", this.isSpawnAnimationFinished);
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
package de.jakob.lotm.entity.custom;

import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class ExileDoorsEntity extends Entity {
    private int lifetime = 0;

    private final HashSet<UUID> onExileCooldown = new HashSet<>();

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(ExileDoorsEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.defineId(ExileDoorsEntity.class, EntityDataSerializers.INT);

    // Required constructors for entity system
    public ExileDoorsEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    // Main constructor for placing the door
    public ExileDoorsEntity(EntityType<?> entityType, Level level, int duration, LivingEntity source) {
        this(entityType, level);
        this.setDuration(duration);
        if(source != null) {
            this.setCasterUUID(source.getUUID());
        }
    }

    @Override
    public void tick() {
        super.tick();
        lifetime++;
        if (lifetime >= getDuration()) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        if(!level().isClientSide) {
            spawnParticles();

            Set<LivingEntity> entities = new HashSet<>(this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox()));
            UUID ownerUUID = this.entityData.get(OWNER).orElse(null);
            LivingEntity owner = ownerUUID != null ? (LivingEntity) ((ServerLevel)level()).getEntity(ownerUUID) : null;

            // Check if owner is a beyonder, if not skip all exile logic
            boolean ownerIsBeyonder = owner != null && BeyonderData.isBeyonder(owner);
            int ownerSequence = ownerIsBeyonder ? BeyonderData.getSequence(owner) : 9;

            for (LivingEntity entity : entities) {
                if (owner != null && (entity.getUUID().equals(ownerUUID) || !AbilityUtil.mayTarget(owner, entity) || !AbilityUtil.mayDamage(owner, entity)) || onExileCooldown.contains(entity.getUUID())) {
                    continue;
                }

                int exileTicks;

                if (ownerIsBeyonder && BeyonderData.isBeyonder(entity)) {
                    int targetSequence = BeyonderData.getSequence(entity);
                    int sequenceDiff = targetSequence - ownerSequence;

                    // If target is 2+ sequences lower (stronger), exile doesn't work
                    if (sequenceDiff <= -2) {
                        continue;
                    }

                    // Calculate exile time based on sequence difference
                    if (sequenceDiff == -1) {
                        exileTicks = 20; // 1 second
                    } else if (sequenceDiff == 0) {
                        exileTicks = 120; // 6 seconds
                    } else {
                        // For higher sequences (weaker targets), scale up the time
                        // Each sequence difference adds ~3 seconds
                        exileTicks = 120 + (sequenceDiff * 60); // 6s + (diff * 3s)
                    }
                } else {
                    exileTicks = 120;
                }
                final double origX = entity.getX();
                final double origY = entity.getY();
                final double origZ = entity.getZ();
                final ServerLevel origLevel = (ServerLevel) entity.level();

                // Teleport to End
                ServerLevel endLevel = ((ServerLevel)level()).getServer().getLevel(Level.END);
                if (endLevel != null) {
                    double randomX = (level().random.nextDouble() - 0.5) * 200;
                    double randomZ = (level().random.nextDouble() - 0.5) * 200;
                    double y = 64;

                    entity.teleportTo(endLevel, randomX, y, randomZ, Set.of(), entity.getYRot(), entity.getXRot());
                    entity.resetFallDistance();

                    ServerScheduler.scheduleDelayed(exileTicks, () -> {
                        entity.teleportTo(origLevel, origX, origY, origZ, Set.of(), entity.getYRot(), entity.getXRot());
                        entity.resetFallDistance();
                        onExileCooldown.add(entity.getUUID());
                        ServerScheduler.scheduleDelayed(20 * 4, () -> onExileCooldown.remove(entity.getUUID()));
                    });
                }
            }
        }
    }
    private void spawnParticles() {
        ParticleUtil.spawnParticles((ServerLevel) level(), ModParticles.STAR.get(), position(), 1, 2, 2, 2, .05);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, Optional.empty());
        builder.define(DURATION, 200); // default 10 seconds
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // Store synced fields to disk
        this.entityData.get(OWNER).ifPresent(uuid -> tag.putUUID("Owner", uuid));
        tag.putInt("Duration", this.getDuration());
        tag.putInt("Lifetime", this.lifetime);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.setCasterUUID(tag.getUUID("Owner"));
        }
        if (tag.contains("Duration")) {
            this.setDuration(tag.getInt("Duration"));
        }
        if (tag.contains("Lifetime")) {
            this.lifetime = tag.getInt("Lifetime");
        }
    }

    public void setCasterUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getCasterUUID() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public void setDuration(int duration) {
        this.entityData.set(DURATION, duration);
    }

    public int getDuration() {
        return this.entityData.get(DURATION);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
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
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        return false; // No passengers allowed
    }
}
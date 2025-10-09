package de.jakob.lotm.entity.custom;

import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.TemporaryChunkLoader;
import de.jakob.lotm.util.scheduling.ServerScheduler;
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

    // Somewhere in your entity or ability class
    @Override
    public void tick() {
        super.tick();
        lifetime++;
        if (lifetime >= getDuration()) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        if (!level().isClientSide) {
            spawnParticles();

            ServerLevel serverLevel = (ServerLevel) level();
            Set<LivingEntity> entities = new HashSet<>(serverLevel.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox()));
            UUID ownerUUID = this.entityData.get(OWNER).orElse(null);
            LivingEntity owner = ownerUUID != null ? (LivingEntity) serverLevel.getEntity(ownerUUID) : null;

            boolean ownerIsBeyonder = owner != null && BeyonderData.isBeyonder(owner);
            int ownerSequence = ownerIsBeyonder ? BeyonderData.getSequence(owner) : 9;

            for (LivingEntity entity : entities) {
                if ((owner != null && (
                        entity.getUUID().equals(ownerUUID)
                                || !AbilityUtil.mayTarget(owner, entity)
                                || !AbilityUtil.mayDamage(owner, entity)))
                        || onExileCooldown.contains(entity.getUUID())
                ) continue;

                int exileTicks;
                if (ownerIsBeyonder && BeyonderData.isBeyonder(entity)) {
                    int targetSequence = BeyonderData.getSequence(entity);
                    int sequenceDiff = targetSequence - ownerSequence;
                    if (sequenceDiff <= -2) continue;

                    if (sequenceDiff == -1) exileTicks = 20;
                    else if (sequenceDiff == 0) exileTicks = 120;
                    else exileTicks = 200 + (sequenceDiff * 60);
                } else {
                    exileTicks = 10 * 20;
                }

                exileTicks = (int) Math.round(exileTicks * BeyonderData.getMultiplier(entity));

                ServerLevel origLevel = (ServerLevel) entity.level();
                double origX = entity.getX();
                double origY = entity.getY();
                double origZ = entity.getZ();

                ServerLevel endLevel = serverLevel.getServer().getLevel(Level.END);
                if (endLevel != null) {
                    double randomX = (serverLevel.random.nextDouble() - 0.5) * 200;
                    double randomZ = (serverLevel.random.nextDouble() - 0.5) * 200;
                    double y = 64;

                    // Persist exile data
                    CompoundTag tag = entity.getPersistentData();
                    tag.putBoolean("Exiled", true);
                    tag.putLong("ReturnTime", serverLevel.getGameTime() + exileTicks);
                    tag.putString("ReturnLevel", origLevel.dimension().location().toString());
                    tag.putDouble("ReturnX", origX);
                    tag.putDouble("ReturnY", origY);
                    tag.putDouble("ReturnZ", origZ);
                    entity.hurtMarked = true;

                    ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.STAR.get(), entity.position(), 100, 1, 2, 1, .1);
                    ParticleUtil.spawnParticles((ServerLevel) entity.level(), ParticleTypes.PORTAL, entity.position(), 100, 1, 2, 1, .1);
                    level().playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 2.0f, 0.5f + level().random.nextFloat());
                    TemporaryChunkLoader.forceChunksTemporarily(endLevel, randomX, randomZ, 4, exileTicks + 20 * 4);
                    entity.teleportTo(endLevel, randomX, y, randomZ, Set.of(), entity.getYRot(), entity.getXRot());
                    TemporaryChunkLoader.forceChunksTemporarily(endLevel, randomX, randomZ, 4, exileTicks + 20 * 4);
                    entity.resetFallDistance();
                    ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.STAR.get(), entity.position(), 100, 1, 2, 1, .1);
                    ParticleUtil.spawnParticles((ServerLevel) entity.level(), ParticleTypes.PORTAL, entity.position(), 100, 1, 2, 1, .1);
                    entity.level().playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 2.0f, 0.5f + entity.level().random.nextFloat());

                    ServerScheduler.scheduleDelayed(exileTicks - 5, () -> onExileCooldown.add(entity.getUUID()));
                    ServerScheduler.scheduleDelayed(20 * 4 + exileTicks + 5, () -> onExileCooldown.remove(entity.getUUID()));
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
package de.jakob.lotm.entity.custom;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class SpaceRiftEntity extends Entity {
    private int lifetime = 0;

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(SpaceRiftEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.defineId(SpaceRiftEntity.class, EntityDataSerializers.INT);

    // Required constructors for entity system
    public SpaceRiftEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    // Main constructor for placing the door
    public SpaceRiftEntity(EntityType<?> entityType, Level level, int duration, LivingEntity source) {
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

    public int getLifetime() {
        return lifetime;
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
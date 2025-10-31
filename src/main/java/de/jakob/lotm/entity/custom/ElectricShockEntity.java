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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ElectricShockEntity extends Entity {
    private static final EntityDataAccessor<Float> START_X = SynchedEntityData.defineId(ElectricShockEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Y = SynchedEntityData.defineId(ElectricShockEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Z = SynchedEntityData.defineId(ElectricShockEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(ElectricShockEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(ElectricShockEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(ElectricShockEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_DISTANCE = SynchedEntityData.defineId(ElectricShockEntity.class, EntityDataSerializers.FLOAT);

    private Vec3 startPos;
    private Vec3 direction;
    private float maxDistance;
    private float currentDistance = 0f;
    private List<Vec3> lightningPoints = new ArrayList<>();
    private int updateInterval = 1; // Ticks between path updates

    private double damage = 1;
    private LivingEntity source = null;

    public ElectricShockEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true; // Prevent culling for visual effects
    }

    // Constructor for spawning
    public ElectricShockEntity(Level level, LivingEntity source, Vec3 start, Vec3 direction, float maxDistance, double damage) {
        this(ModEntities.ELECTRIC_SHOCK.get(), level);
        this.startPos = start;
        this.direction = direction.normalize();
        this.maxDistance = maxDistance;
        this.damage = damage;
        this.source = source;
        this.currentDistance = 1.0f; // Start with some initial distance
        setPos(start.x, start.y, start.z);

        // Set synced data
        if (!level.isClientSide) {
            entityData.set(START_X, (float) start.x);
            entityData.set(START_Y, (float) start.y);
            entityData.set(START_Z, (float) start.z);
            entityData.set(DIR_X, (float) direction.x);
            entityData.set(DIR_Y, (float) direction.y);
            entityData.set(DIR_Z, (float) direction.z);
            entityData.set(MAX_DISTANCE, maxDistance);
        }

        generateInitialPath();

    }

    @Override
    public void tick() {
        super.tick();

        // Initialize from synced data on client side
        if (level().isClientSide && (startPos == null || direction == null)) {
            startPos = new Vec3(entityData.get(START_X), entityData.get(START_Y), entityData.get(START_Z));
            direction = new Vec3(entityData.get(DIR_X), entityData.get(DIR_Y), entityData.get(DIR_Z));
            maxDistance = entityData.get(MAX_DISTANCE);
            currentDistance = 1.0f;
            generateInitialPath();
        }

        // Safety check - if direction is null, entity wasn't properly initialized yet
        if (direction == null || startPos == null) {
            return; // Wait for NBT data to be synced
        }

        if (level().isClientSide) {
            updateLightningPath();
        }

        // Raycast to find hit point
        Vec3 currentEnd = startPos.add(direction.scale(Math.min(currentDistance, maxDistance)));
        HitResult hit = level().clip(new ClipContext(startPos, currentEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        // Check for entity hits
        List<Entity> entities = level().getEntities(this, new AABB(startPos, currentEnd));
        for (Entity entity : entities) {
            if (entity != this && !entity.isSpectator()) {
                onHitEntity(entity);
                discard();
                return;
            }
        }

        // Check for block hits
        if (hit.getType() != HitResult.Type.MISS) {
            onHitBlock(hit);
            discard();
            return;
        }

        // Advance beam
        currentDistance += 1.25f; // Beam travel speed (blocks per tick)
        if (currentDistance >= maxDistance) {
            discard();
        }

        // Remove after 5 seconds max
        if (tickCount > 100) {
            discard();
        }
    }

    private void updateLightningPath() {
        // Safety check
        if (direction == null || startPos == null) {
            return;
        }

        if (tickCount % updateInterval == 0) {
            generateJaggedPath();
        }
    }

    private void generateInitialPath() {
        if (startPos != null && direction != null) {
            lightningPoints.clear();
            lightningPoints.add(startPos);
            lightningPoints.add(startPos.add(direction.scale(1.0))); // Add a second point
        }
    }

    private void generateJaggedPath() {
        // Safety check
        if (direction == null || startPos == null) {
            return;
        }

        lightningPoints.clear();
        Vec3 current = startPos;
        Vec3 target = startPos.add(direction.scale(Math.min(currentDistance, maxDistance)));

        int segments = Math.max(1, (int)(currentDistance / 0.8f));
        for (int i = 0; i <= segments; i++) {
            float progress = segments > 0 ? (float)i / segments : 0f;
            Vec3 basePoint = startPos.lerp(target, progress);

            // Add random offset for jagged appearance, but keep first and last points stable
            if (i > 0 && i < segments) {
                double offsetX = (random.nextDouble() - 0.5) * 0.4;
                double offsetY = (random.nextDouble() - 0.5) * 0.4;
                double offsetZ = (random.nextDouble() - 0.5) * 0.4;
                basePoint = basePoint.add(offsetX, offsetY, offsetZ);
            }

            lightningPoints.add(basePoint);
        }
    }

    private void onHitEntity(Entity entity) {
        // Handle entity hit - damage, effects, etc.
        if (!level().isClientSide) {
            DamageSource dmg = (source != null)
                    ? level().damageSources().mobAttack(source)
                    : level().damageSources().generic();

            entity.hurt(dmg, (float) damage);
        }
    }

    private void onHitBlock(HitResult hit) {
        // Handle block hit - particles, sound, etc.
        if (!level().isClientSide) {
            // Add your block hit logic here
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Initialize synced data with default values
        builder.define(START_X, 0.0f);
        builder.define(START_Y, 0.0f);
        builder.define(START_Z, 0.0f);
        builder.define(DIR_X, 1.0f);
        builder.define(DIR_Y, 0.0f);
        builder.define(DIR_Z, 0.0f);
        builder.define(MAX_DISTANCE, 10.0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("startX")) {
            startPos = new Vec3(tag.getDouble("startX"), tag.getDouble("startY"), tag.getDouble("startZ"));
            direction = new Vec3(tag.getDouble("dirX"), tag.getDouble("dirY"), tag.getDouble("dirZ"));
            maxDistance = tag.getFloat("maxDistance");
            currentDistance = tag.getFloat("currentDistance");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (startPos != null && direction != null) {
            tag.putDouble("startX", startPos.x);
            tag.putDouble("startY", startPos.y);
            tag.putDouble("startZ", startPos.z);
            tag.putDouble("dirX", direction.x);
            tag.putDouble("dirY", direction.y);
            tag.putDouble("dirZ", direction.z);
            tag.putFloat("maxDistance", maxDistance);
            tag.putFloat("currentDistance", currentDistance);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    // Getters for renderer
    public List<Vec3> getLightningPoints() {
        return lightningPoints;
    }

    public float getCurrentDistance() {
        return currentDistance;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D; // Render up to 128 blocks away
    }
}
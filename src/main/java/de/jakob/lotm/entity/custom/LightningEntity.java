package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.helper.ParticleUtil;
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
import java.util.Random;

public class LightningEntity extends Entity {
    private static final EntityDataAccessor<Float> START_X = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Y = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Z = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_DISTANCE = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STEP = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(LightningEntity.class, EntityDataSerializers.INT);

    private Vec3 startPos;
    private Vec3 direction;
    private float maxDistance;
    private float currentDistance = 0f;
    private final List<Vec3> lightningPoints = new ArrayList<>();
    private final int updateInterval = 1; // Ticks between path updates
    private float step = 4;
    private int color = 0x11A8DD;
    private boolean griefing;
    private float explosionPower;

    boolean hasHit = false;

    private double damage = 1;
    private LivingEntity source = null;

    private final List<LightningEntity> branches = new ArrayList<>();

    public LightningEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    private final List<Float> distancesAtWhichToSpawnNewBranches = new ArrayList<>();

    // Constructor for spawning
    public LightningEntity(Level level, LivingEntity source, Vec3 start, int height, int branches, double damage, boolean griefing, float explosionPower, float maxDistance, int color) {
        this(ModEntities.LIGHTNING.get(), level);
        this.startPos = start.add(0, height, 0);
        this.direction = new Vec3(0, -1, 0);
        this.maxDistance = maxDistance;
        this.damage = damage;
        this.source = source;
        this.explosionPower = explosionPower;
        this.griefing = griefing;
        this.currentDistance = 1.0f; // Start with some initial distance
        this.step = 4f;
        this.color = color;
        setPos(start.x, start.y, start.z);

        // Set synced data
        if (!level.isClientSide) {
            entityData.set(START_X, (float) start.add(0, height, 0).x);
            entityData.set(START_Y, (float) start.add(0, height, 0).y);
            entityData.set(START_Z, (float) start.add(0, height, 0).z);
            entityData.set(DIR_X, (float) 0);
            entityData.set(DIR_Y, (float) -1);
            entityData.set(DIR_Z, (float) 0);
            entityData.set(MAX_DISTANCE, maxDistance);
            entityData.set(STEP, 4f);
            entityData.set(COLOR, color);

            for(int i = 0; i < branches; i++) {
                distancesAtWhichToSpawnNewBranches.add((new Random()).nextInt(8) * step + 1);
            }
        }

        generateInitialPath();

    }

    public LightningEntity(Level level, LivingEntity source, Vec3 start, double damage, int branches, float maxDistance, Vec3 direction, int color) {
        this(ModEntities.LIGHTNING.get(), level);
        this.startPos = start;
        this.direction = direction;
        this.maxDistance = maxDistance;
        this.damage = damage;
        this.source = source;
        this.currentDistance = 1.0f;
        this.step = 1.7f; // Start with some initial distance
        this.color = color;
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
            entityData.set(STEP, 1.7f);
            entityData.set(COLOR, color);

            for(int i = 0; i < branches; i++) {
                distancesAtWhichToSpawnNewBranches.add((new Random()).nextInt(1, 3) * step + 1);
            }
        }

        generateInitialPath();

    }

    Random random = new Random();

    @Override
    public void tick() {
        super.tick();

        // Initialize from synced data on client side
        if (level().isClientSide && (startPos == null || direction == null)) {
            startPos = new Vec3(entityData.get(START_X), entityData.get(START_Y), entityData.get(START_Z));
            direction = new Vec3(entityData.get(DIR_X), entityData.get(DIR_Y), entityData.get(DIR_Z));
            maxDistance = entityData.get(MAX_DISTANCE);
            currentDistance = 1.0f;
            step = entityData.get(STEP);
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
            if (!(entity instanceof LightningEntity) && !entity.isSpectator()) {
                onHitEntity(entity, currentEnd);
                return;
            }
        }

        // Check for block hits
        if (hit.getType() != HitResult.Type.MISS) {
            onHitBlock(hit);
            return;
        }

        if(!level().isClientSide) {
            for(float d : distancesAtWhichToSpawnNewBranches) {
                if(d == currentDistance) {
                    LightningEntity entity = new LightningEntity(
                            level(),
                            null,
                            startPos.add(0, -d, 0),
                            0,
                            random.nextInt(2),
                            random.nextInt(10, 17),
                            (new Vec3(random.nextDouble(-1, 1), random.nextDouble(-3.4, -2), random.nextDouble(-1, 1)).normalize()),
                            color);
                    level().addFreshEntity(entity);
                    branches.add(entity);
                }
            }
        }

        // Advance beam
        if(currentDistance < maxDistance)
            currentDistance += step; // Beam travel speed (blocks per tick)

        if (currentDistance >= maxDistance && !level().isClientSide) {
            ServerScheduler.scheduleDelayed(10, this::discard);
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
                double offsetX = (random.nextDouble() - 0.5) * 0.95;
                double offsetY = (random.nextDouble() - 0.5) * 0.95;
                double offsetZ = (random.nextDouble() - 0.5) * 0.95;
                basePoint = basePoint.add(offsetX, offsetY, offsetZ);
            }

            lightningPoints.add(basePoint);
        }
    }

    private void onHitEntity(Entity entity, Vec3 pos) {
        if(hasHit)
            return;
        hasHit = true;
        // Handle entity hit - damage, effects, etc.
        if (!level().isClientSide && source != null) {
            explode(pos);
            entity.hurt(source.damageSources().mobAttack(source), (float) damage);
            ServerScheduler.scheduleDelayed(12, this::discardEntityAndBranches);
        }
    }

    private void explode(Vec3 pos) {
        level().explode(source, pos.x, pos.y, pos.z, explosionPower, griefing, griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.EXPLOSION, pos, 20, 1.75, 0);
    }

    private void onHitBlock(HitResult hit) {
        if(hasHit)
            return;
        hasHit = true;
        // Handle block hit - particles, sound, etc.
        if (!level().isClientSide) {
            if(source != null)
                explode(hit.getLocation());
            ServerScheduler.scheduleDelayed(12, this::discardEntityAndBranches);
        }
    }

    public void discardEntityAndBranches() {
        for(LightningEntity e : branches) {
            e.discardEntityAndBranches();
        }
        this.discard();
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
        builder.define(STEP, 4f);
        builder.define(COLOR, 0x11A8DD);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("startX")) {
            startPos = new Vec3(tag.getDouble("startX"), tag.getDouble("startY"), tag.getDouble("startZ"));
            direction = new Vec3(tag.getDouble("dirX"), tag.getDouble("dirY"), tag.getDouble("dirZ"));
            maxDistance = tag.getFloat("maxDistance");
            currentDistance = tag.getFloat("currentDistance");
            step = tag.getInt("step");
            color = tag.getInt("color");
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
            tag.putFloat("step", step);
            tag.putInt("color", color);
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

    public int getColor() {
        return entityData.get(COLOR);
    }
}
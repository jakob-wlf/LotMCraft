package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
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

public class GiantLightningEntity extends Entity {
    private static final EntityDataAccessor<Float> START_X = SynchedEntityData.defineId(GiantLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Y = SynchedEntityData.defineId(GiantLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Z = SynchedEntityData.defineId(GiantLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(GiantLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(GiantLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(GiantLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_DISTANCE = SynchedEntityData.defineId(GiantLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STEP = SynchedEntityData.defineId(GiantLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(GiantLightningEntity.class, EntityDataSerializers.INT);

    private Vec3 startPos;
    private Vec3 direction;
    private float maxDistance;
    private float currentDistance = 0f;
    private final List<Vec3> lightningPoints = new ArrayList<>();
    private final int updateInterval = 1;
    private float step = 8; // Larger step for faster growth
    private int color = 0x88DDFF; // Brighter blue
    private boolean griefing;
    private float explosionPower;

    boolean hasHit = false;

    private double damage = 10; // More damage for giant lightning
    private LivingEntity source = null;

    private final List<GiantLightningEntity> branches = new ArrayList<>();

    public GiantLightningEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    private final List<Float> distancesAtWhichToSpawnNewBranches = new ArrayList<>();

    // Constructor for spawning massive lightning from sky
    public GiantLightningEntity(Level level, LivingEntity source, Vec3 start, int height, int branches, double damage, boolean griefing, float explosionPower, float maxDistance, int color) {
        this(ModEntities.GIANT_LIGHTNING.get(), level);
        // Start much higher in the sky (add 80-100 blocks above the point)
        this.startPos = start.add(0, height + 80, 0);
        this.direction = new Vec3(0, -1, 0);
        this.maxDistance = maxDistance + 80; // Extend max distance to account for higher start
        this.damage = damage;
        this.source = source;
        this.explosionPower = explosionPower;
        this.griefing = griefing;
        this.currentDistance = 2.0f;
        this.step = 12f; // Faster advancement
        this.color = color;
        setPos(start.x, start.y, start.z);

        // Set synced data
        if (!level.isClientSide) {
            entityData.set(START_X, (float) start.add(0, height + 80, 0).x);
            entityData.set(START_Y, (float) start.add(0, height + 80, 0).y);
            entityData.set(START_Z, (float) start.add(0, height + 80, 0).z);
            entityData.set(DIR_X, (float) 0);
            entityData.set(DIR_Y, (float) -1);
            entityData.set(DIR_Z, (float) 0);
            entityData.set(MAX_DISTANCE, maxDistance + 80);
            entityData.set(STEP, 12f);
            entityData.set(COLOR, color);

            // More branches for giant lightning, spawn them at varied distances
            for(int i = 0; i < branches; i++) {
                distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(10f, 50f));
            }
            // Add extra branches distributed along the entire path
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(12f, 25f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(20f, 35f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(30f, 45f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(40f, 55f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(50f, 65f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(60f, 75f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(70f, 85f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(80f, 95f));
        }

        generateInitialPath();
    }

    // Constructor for branch lightning
    public GiantLightningEntity(Level level, LivingEntity source, Vec3 start, double damage, int branches, float maxDistance, Vec3 direction, int color) {
        this(ModEntities.GIANT_LIGHTNING.get(), level);
        this.startPos = start;
        this.direction = direction;
        this.maxDistance = maxDistance;
        this.damage = damage;
        this.source = source;
        this.currentDistance = 2.0f;
        this.step = 6f; // Branches advance slightly slower
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
            entityData.set(STEP, 6f);
            entityData.set(COLOR, color);

            // Fewer sub-branches to avoid overwhelming visuals
            for(int i = 0; i < branches; i++) {
                distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(5f, 15f));
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
            currentDistance = 2.0f;
            step = entityData.get(STEP);
            generateInitialPath();
        }

        // Safety check
        if (direction == null || startPos == null) {
            return;
        }

        if (level().isClientSide) {
            updateLightningPath();
        }

        // Raycast to find hit point - use larger collision box
        Vec3 currentEnd = startPos.add(direction.scale(Math.min(currentDistance, maxDistance)));
        HitResult hit = level().clip(new ClipContext(startPos, currentEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        // Check for entity hits in a larger area
        AABB searchBox = new AABB(startPos, currentEnd).inflate(3.0); // Larger hitbox
        List<Entity> entities = level().getEntities(this, searchBox);
        for (Entity entity : entities) {
            if (!(entity instanceof GiantLightningEntity) && !entity.isSpectator()) {
                onHitEntity(entity, currentEnd);
                return;
            }
        }

        // Check for block hits
        if (hit.getType() != HitResult.Type.MISS) {
            onHitBlock(hit);
            return;
        }

        // Spawn branches
        if(!level().isClientSide) {
            // Create a copy to avoid concurrent modification
            List<Float> branchesToSpawn = new ArrayList<>();
            for(float d : distancesAtWhichToSpawnNewBranches) {
                if(Math.abs(d - currentDistance) < step * 0.5f) {
                    branchesToSpawn.add(d);
                }
            }

            for(float d : branchesToSpawn) {
                Vec3 branchStart = startPos.add(direction.scale(d));
                // Branch directions mostly downward with controlled horizontal spread
                Vec3 branchDir = new Vec3(
                        random.nextDouble(-0.95, 0.95),
                        random.nextDouble(-2.8, -1.5), // Strong downward bias
                        random.nextDouble(-0.95, 0.95)
                ).normalize();

                GiantLightningEntity entity = new GiantLightningEntity(
                        level(),
                        source,
                        branchStart,
                        damage * 0.5,
                        random.nextInt(0, 2), // Some branches spawn their own sub-branches
                        random.nextFloat(15f, 35f),
                        branchDir,
                        color);
                level().addFreshEntity(entity);
                branches.add(entity);

                // Remove this branch point
                distancesAtWhichToSpawnNewBranches.remove(d);
            }
        }

        // Advance beam
        if(currentDistance < maxDistance)
            currentDistance += step;

        if (currentDistance >= maxDistance && !level().isClientSide) {
            ServerScheduler.scheduleDelayed(15, this::discard);
        }

        // Remove after 7 seconds max
        if (tickCount > 140) {
            discard();
        }
    }

    private void updateLightningPath() {
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
            lightningPoints.add(startPos.add(direction.scale(2.0)));
        }
    }

    private void generateJaggedPath() {
        if (direction == null || startPos == null) {
            return;
        }

        lightningPoints.clear();
        Vec3 current = startPos;
        Vec3 target = startPos.add(direction.scale(Math.min(currentDistance, maxDistance)));

        // Fewer segments for more angular, dramatic appearance
        int segments = Math.max(1, (int)(currentDistance / 2.5f));
        for (int i = 0; i <= segments; i++) {
            float progress = segments > 0 ? (float)i / segments : 0f;
            Vec3 basePoint = startPos.lerp(target, progress);

            // Larger random offsets for more dramatic jagged appearance
            if (i > 0 && i < segments) {
                double offsetX = (random.nextDouble() - 0.5) * 3.5;
                double offsetY = (random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (random.nextDouble() - 0.5) * 3.5;
                basePoint = basePoint.add(offsetX, offsetY, offsetZ);
            }

            lightningPoints.add(basePoint);
        }
    }

    private void onHitEntity(Entity entity, Vec3 pos) {
        if(hasHit)
            return;
        hasHit = true;

        if (!level().isClientSide && source != null) {
            explode(pos);
            entity.hurt(source.damageSources().mobAttack(source), (float) damage);

            // Damage nearby entities as well
            List<Entity> nearbyEntities = level().getEntities(this,
                    new AABB(pos.add(-5, -5, -5), pos.add(5, 5, 5)));
            for (Entity nearby : nearbyEntities) {
                if (nearby != entity && !(nearby instanceof GiantLightningEntity) && !nearby.isSpectator()) {
                    nearby.hurt(source.damageSources().mobAttack(source), (float) (damage * 0.3));
                }
            }

            ServerScheduler.scheduleDelayed(15, this::discardEntityAndBranches);
        } else {
            ClientHandler.applyCameraShakeToPlayersInRadius(4f, 35, (ClientLevel) level(), pos, 60);
        }
    }

    private void explode(Vec3 pos) {
        // Larger explosion for giant lightning
        level().explode(source, pos.x, pos.y, pos.z, explosionPower * 1.5f, griefing,
                griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.EXPLOSION, pos, 40, 3.0, 0);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.ELECTRIC_SPARK, pos, 60, 4.0, 0);
    }

    private void onHitBlock(HitResult hit) {
        if(hasHit)
            return;
        hasHit = true;

        if (!level().isClientSide) {
            if(source != null)
                explode(hit.getLocation());
            ServerScheduler.scheduleDelayed(15, this::discardEntityAndBranches);
        } else {
            ClientHandler.applyCameraShakeToPlayersInRadius(4f, 35, (ClientLevel) level(), hit.getLocation(), 60);
        }
    }

    public void discardEntityAndBranches() {
        for(GiantLightningEntity e : branches) {
            e.discardEntityAndBranches();
        }
        this.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(START_X, 0.0f);
        builder.define(START_Y, 0.0f);
        builder.define(START_Z, 0.0f);
        builder.define(DIR_X, 1.0f);
        builder.define(DIR_Y, 0.0f);
        builder.define(DIR_Z, 0.0f);
        builder.define(MAX_DISTANCE, 50.0f);
        builder.define(STEP, 12f);
        builder.define(COLOR, 0x88DDFF);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("startX")) {
            startPos = new Vec3(tag.getDouble("startX"), tag.getDouble("startY"), tag.getDouble("startZ"));
            direction = new Vec3(tag.getDouble("dirX"), tag.getDouble("dirY"), tag.getDouble("dirZ"));
            maxDistance = tag.getFloat("maxDistance");
            currentDistance = tag.getFloat("currentDistance");
            step = tag.getFloat("step");
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
        return distance < 65536.0D; // Render up to 256 blocks away - more visible
    }

    public int getColor() {
        return entityData.get(COLOR);
    }
}
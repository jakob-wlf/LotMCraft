package de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway;

import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.tyrant.WaterMasteryAbility;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StrongLightningEntity extends Entity {
    private static final EntityDataAccessor<Float> START_X = SynchedEntityData.defineId(StrongLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Y = SynchedEntityData.defineId(StrongLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Z = SynchedEntityData.defineId(StrongLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(StrongLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(StrongLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(StrongLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_DISTANCE = SynchedEntityData.defineId(StrongLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STEP = SynchedEntityData.defineId(StrongLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(StrongLightningEntity.class, EntityDataSerializers.INT);

    private Vec3 startPos;
    private Vec3 direction;
    private float maxDistance;
    private float currentDistance = 0f;
    private final List<Vec3> lightningPoints = new ArrayList<>();
    private final int updateInterval = 1;
    private float step = 36;
    private int color = 0xFFFFCC;
    private boolean griefing;
    private float explosionPower;

    boolean hasHit = false;

    private double damage = 7;
    private LivingEntity source = null;

    private final List<StrongLightningEntity> branches = new ArrayList<>();

    public StrongLightningEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    private final List<Float> distancesAtWhichToSpawnNewBranches = new ArrayList<>();

    public StrongLightningEntity(Level level, LivingEntity source, Vec3 start, int height, int branches, double damage, boolean griefing, float explosionPower, float maxDistance, int color) {
        this(ModEntities.STRONG_LIGHTNING.get(), level);
        this.startPos = start.add(0, height + 60, 0);
        this.direction = new Vec3(0, -1, 0);
        this.maxDistance = maxDistance + 60;
        this.damage = damage;
        this.source = source;
        this.explosionPower = explosionPower;
        this.griefing = griefing;
        this.currentDistance = 2.0f;
        this.step = 36f;
        this.color = color;
        setPos(start.x, start.y, start.z);

        if (!level.isClientSide) {
            entityData.set(START_X, (float) start.add(0, height + 60, 0).x);
            entityData.set(START_Y, (float) start.add(0, height + 60, 0).y);
            entityData.set(START_Z, (float) start.add(0, height + 60, 0).z);
            entityData.set(DIR_X, (float) 0);
            entityData.set(DIR_Y, (float) -1);
            entityData.set(DIR_Z, (float) 0);
            entityData.set(MAX_DISTANCE, maxDistance + 60);
            entityData.set(STEP, 36f);
            entityData.set(COLOR, color);

            for(int i = 0; i < branches; i++) {
                distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(10f, 50f));
            }
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(12f, 25f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(20f, 35f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(30f, 45f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(40f, 55f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(50f, 65f));
            distancesAtWhichToSpawnNewBranches.add((new Random()).nextFloat(60f, 75f));
        }

        generateInitialPath();
    }

    public StrongLightningEntity(Level level, LivingEntity source, Vec3 start, double damage, int branches, float maxDistance, Vec3 direction, int color) {
        this(ModEntities.STRONG_LIGHTNING.get(), level);
        this.startPos = start;
        this.direction = direction;
        this.maxDistance = maxDistance;
        this.damage = damage;
        this.source = source;
        this.currentDistance = 2.0f;
        this.step = 5f;
        this.color = color;
        setPos(start.x, start.y, start.z);

        if (!level.isClientSide) {
            entityData.set(START_X, (float) start.x);
            entityData.set(START_Y, (float) start.y);
            entityData.set(START_Z, (float) start.z);
            entityData.set(DIR_X, (float) direction.x);
            entityData.set(DIR_Y, (float) direction.y);
            entityData.set(DIR_Z, (float) direction.z);
            entityData.set(MAX_DISTANCE, maxDistance);
            entityData.set(STEP, 5f);
            entityData.set(COLOR, color);

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

        if (level().isClientSide && (startPos == null || direction == null)) {
            startPos = new Vec3(entityData.get(START_X), entityData.get(START_Y), entityData.get(START_Z));
            direction = new Vec3(entityData.get(DIR_X), entityData.get(DIR_Y), entityData.get(DIR_Z));
            maxDistance = entityData.get(MAX_DISTANCE);
            currentDistance = 2.0f;
            step = entityData.get(STEP);
            generateInitialPath();
        }

        if (direction == null || startPos == null) {
            return;
        }

        if (level().isClientSide) {
            updateLightningPath();
        }

        Vec3 currentEnd = startPos.add(direction.scale(Math.min(currentDistance, maxDistance)));
        HitResult hit = level().clip(new ClipContext(startPos, currentEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        AABB searchBox = new AABB(startPos, currentEnd).inflate(2.0);
        List<Entity> entities = level().getEntities(this, searchBox);
        for (Entity entity : entities) {
            if (!(entity instanceof StrongLightningEntity) && !entity.isSpectator()) {
                onHitEntity(entity, currentEnd);
                return;
            }
        }

        if (hit.getType() != HitResult.Type.MISS) {
            onHitBlock(hit);
            return;
        }

        if(!level().isClientSide) {
            List<Float> branchesToSpawn = new ArrayList<>();
            for(float d : distancesAtWhichToSpawnNewBranches) {
                if(Math.abs(d - currentDistance) < step * 0.5f) {
                    branchesToSpawn.add(d);
                }
            }

            for(float d : branchesToSpawn) {
                Vec3 branchStart = startPos.add(direction.scale(d));
                Vec3 branchDir = new Vec3(
                        random.nextDouble(-0.8, 0.8),
                        random.nextDouble(-2.5, -1.5),
                        random.nextDouble(-0.8, 0.8)
                ).normalize();

                StrongLightningEntity entity = new StrongLightningEntity(
                        level(),
                        source,
                        branchStart,
                        damage * 0.5,
                        random.nextInt(0, 2),
                        random.nextFloat(12f, 28f),
                        branchDir,
                        color);
                level().addFreshEntity(entity);
                branches.add(entity);

                distancesAtWhichToSpawnNewBranches.remove(d);
            }
        }

        if(currentDistance < maxDistance)
            currentDistance += step;

        if (currentDistance >= maxDistance && !level().isClientSide) {
            ServerScheduler.scheduleDelayed(15, this::discard);
        }

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
        Vec3 target = startPos.add(direction.scale(Math.min(currentDistance, maxDistance)));

        int segments = Math.max(1, (int)(currentDistance / 2.5f));
        for (int i = 0; i <= segments; i++) {
            float progress = segments > 0 ? (float)i / segments : 0f;
            Vec3 basePoint = startPos.lerp(target, progress);

            if (i > 0 && i < segments) {
                double offsetX = (random.nextDouble() - 0.5) * 2.5;
                double offsetY = (random.nextDouble() - 0.5) * 1.5;
                double offsetZ = (random.nextDouble() - 0.5) * 2.5;
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

            NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level(), pos, source, null, new String[]{"lightning", "explosion"}, explosionPower, 12));

            boolean inWater = isNearWater(pos);
            float waterMultiplier = inWater ? 1.8f : 1.0f;

            entity.hurt(source.damageSources().mobAttack(source), (float) damage * waterMultiplier);

            List<Entity> nearbyEntities = level().getEntities(this,
                    new AABB(pos.add(-4, -4, -4), pos.add(4, 4, 4)));
            for (Entity nearby : nearbyEntities) {
                if (nearby != entity && !(nearby instanceof StrongLightningEntity) && !nearby.isSpectator()) {
                    nearby.hurt(source.damageSources().mobAttack(source), (float) (damage * 0.3));
                }
            }

            if(inWater) {
                dealWaterConductionDamage(pos);
            }

            dealWaterWallDamage(pos);

            ServerScheduler.scheduleDelayed(15, this::discardEntityAndBranches);
        } else if(level().isClientSide) {
            ClientHandler.applyCameraShakeToPlayersInRadius(3f, 25, (ClientLevel) level(), pos, 50);
        }
    }

    private void explode(Vec3 pos) {
        level().explode(source, pos.x, pos.y, pos.z, explosionPower, griefing,
                griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.EXPLOSION, pos, 25, 2.0, 0);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.ELECTRIC_SPARK, pos, 40, 3.0, 0);
    }

    private void onHitBlock(HitResult hit) {
        if(hasHit)
            return;
        hasHit = true;

        if (!level().isClientSide) {
            Vec3 pos = hit.getLocation();
            if(source != null) {
                explode(pos);
                NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level(), pos, source, null, new String[]{"lightning", "explosion"}, explosionPower, 12));
            }

            if(isNearWater(pos)) {
                dealWaterConductionDamage(pos);
            }

            dealWaterWallDamage(pos);

            ServerScheduler.scheduleDelayed(15, this::discardEntityAndBranches);
        } else {
            ClientHandler.applyCameraShakeToPlayersInRadius(3f, 25, (ClientLevel) level(), hit.getLocation(), 50);
        }
    }

    public void discardEntityAndBranches() {
        for(StrongLightningEntity e : branches) {
            e.discardEntityAndBranches();
        }
        this.discard();
    }

    private boolean isNearWater(Vec3 pos) {
        BlockPos center = BlockPos.containing(pos);
        for(int x = -2; x <= 2; x++) {
            for(int y = -2; y <= 2; y++) {
                for(int z = -2; z <= 2; z++) {
                    if(level().getBlockState(center.offset(x, y, z)).is(Blocks.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void dealWaterConductionDamage(Vec3 pos) {
        if(source == null || level().isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level();
        AbilityUtil.getNearbyEntities(source, serverLevel, pos, 15).forEach(e -> {
            if(e.isInWater() || isNearWater(e.position())) {
                e.hurt(source.damageSources().mobAttack(source), (float) (damage * 1.5));
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.ELECTRIC_SPARK, e.position(), 20, .5, 0);
            }
        });
    }

    private void dealWaterWallDamage(Vec3 pos) {
        if(source == null || level().isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level();
        for(WaterMasteryAbility.ActiveWaterWall wall : WaterMasteryAbility.getActiveWaterWalls()) {
            Vec3 wallPos = wall.position();
            Vec3 perp = wall.perpendicular();

            Vec3 toHit = pos.subtract(wallPos);
            double alongWall = toHit.dot(perp);
            double distToWallLine = toHit.subtract(perp.scale(alongWall)).length();

            if(distToWallLine < 5 && Math.abs(alongWall) < wall.halfWidth()) {
                for(int j = -wall.halfWidth(); j <= wall.halfWidth(); j += 3) {
                    Vec3 wallPoint = wallPos.add(perp.scale(j));
                    AbilityUtil.damageNearbyEntities(serverLevel, source, 3, (float) (damage * 1.5), wallPoint, true, false, true, 0);
                    ParticleUtil.spawnParticles(serverLevel, ParticleTypes.ELECTRIC_SPARK, wallPoint, 10, 1, 0);
                }
                break;
            }
        }
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
        builder.define(STEP, 10f);
        builder.define(COLOR, 0xFFFFCC);
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
        return distance < 65536.0D;
    }

    public int getColor() {
        return entityData.get(COLOR);
    }
}
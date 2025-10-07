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
import java.util.Random;

public class LightningBranchEntity extends Entity {
    private static final EntityDataAccessor<Float> START_X = SynchedEntityData.defineId(LightningBranchEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Y = SynchedEntityData.defineId(LightningBranchEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_Z = SynchedEntityData.defineId(LightningBranchEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(LightningBranchEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(LightningBranchEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(LightningBranchEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_DISTANCE = SynchedEntityData.defineId(LightningBranchEntity.class, EntityDataSerializers.FLOAT);

    private Vec3 startPos;
    private Vec3 direction;
    private float maxDistance;
    private float currentDistance = 0f;

    // Branch structure
    private final List<LightningBranch> branches = new ArrayList<>();
    private boolean branchesInitialized = false;

    private double damage = 1;
    private LivingEntity source = null;

    // Branch configuration
    private static final float BRANCH_ANGLE_VARIANCE = 35f; // Max angle deviation for branches
    private static final float BRANCH_PROBABILITY = 0.55f; // Chance to create a branch at each segment
    private static final int MAX_BRANCH_DEPTH = 4; // How many sub-branches can spawn
    private static final float BRANCH_LENGTH_MULTIPLIER = 0.75f; // Child branches are shorter

    public LightningBranchEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public LightningBranchEntity(Level level, LivingEntity source, Vec3 start, Vec3 direction, float maxDistance, double damage) {
        this(ModEntities.LIGHTNING_BRANCH.get(), level);
        this.startPos = start;
        this.direction = direction.normalize();
        this.maxDistance = maxDistance;
        this.damage = damage;
        this.source = source;
        this.currentDistance = 1.0f;
        setPos(start.x, start.y, start.z);

        if (!level.isClientSide) {
            entityData.set(START_X, (float) start.x);
            entityData.set(START_Y, (float) start.y);
            entityData.set(START_Z, (float) start.z);
            entityData.set(DIR_X, (float) direction.x);
            entityData.set(DIR_Y, (float) direction.y);
            entityData.set(DIR_Z, (float) direction.z);
            entityData.set(MAX_DISTANCE, maxDistance);
        }

        initializeBranches();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide && (startPos == null || direction == null)) {
            startPos = new Vec3(entityData.get(START_X), entityData.get(START_Y), entityData.get(START_Z));
            direction = new Vec3(entityData.get(DIR_X), entityData.get(DIR_Y), entityData.get(DIR_Z));
            maxDistance = entityData.get(MAX_DISTANCE);
            currentDistance = 1.0f;
            initializeBranches();
        }

        if (direction == null || startPos == null) {
            return;
        }

        if (level().isClientSide) {
            updateAllBranches();
        }

        // Check collisions for all active branches
        if (!level().isClientSide) {
            checkBranchCollisions();
        }

        // Advance lightning
        currentDistance += 1.25f;
        if (currentDistance >= maxDistance) {
            discard();
        }

        if (tickCount > 100) {
            discard();
        }
    }

    private void initializeBranches() {
        if (branchesInitialized) return;

        branches.clear();
        // Create main branch
        LightningBranch mainBranch = new LightningBranch(startPos, direction, maxDistance, 0);
        branches.add(mainBranch);

        // Generate initial branch structure
        generateBranchStructure();

        branchesInitialized = true;
    }

    private void generateBranchStructure() {
        // Use a queue to process branches recursively
        List<LightningBranch> toProcess = new ArrayList<>();
        toProcess.add(branches.get(0)); // Start with main branch

        while (!toProcess.isEmpty()) {
            LightningBranch branch = toProcess.remove(0);

            if (branch.depth >= MAX_BRANCH_DEPTH) continue;

            // Determine number of segments for this branch
            int segments = Math.max(4, (int)(branch.maxLength / 1.5f));

            // Potentially create branches at various points along the branch
            for (int i = 1; i < segments; i++) {
                if (random.nextFloat() < BRANCH_PROBABILITY) {
                    float progress = (float)i / segments;
                    Vec3 branchPoint = branch.startPos.add(branch.direction.scale(branch.maxLength * progress));

                    // Create 1-3 child branches per branching point
                    int numChildBranches = 1 + random.nextInt(3);
                    for (int b = 0; b < numChildBranches; b++) {
                        Vec3 branchDir = createBranchDirection(branch.direction);
                        float branchLength = branch.maxLength * BRANCH_LENGTH_MULTIPLIER * (0.6f + random.nextFloat() * 0.4f);

                        LightningBranch childBranch = new LightningBranch(
                                branchPoint,
                                branchDir,
                                branchLength,
                                branch.depth + 1,
                                progress // Store at what point along parent this branch spawns
                        );
                        branches.add(childBranch);
                        toProcess.add(childBranch); // Process this branch's children too
                    }
                }
            }
        }
    }

    private Vec3 createBranchDirection(Vec3 parentDir) {
        // Create a random direction that deviates from parent direction
        double angleRad = Math.toRadians(BRANCH_ANGLE_VARIANCE);

        // Random rotation around parent direction
        double theta = random.nextDouble() * Math.PI * 2;
        double phi = random.nextDouble() * angleRad;

        // Create perpendicular vector to parent
        Vec3 perpendicular = Math.abs(parentDir.y) < 0.9 ?
                new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 tangent = parentDir.cross(perpendicular).normalize();
        Vec3 bitangent = parentDir.cross(tangent).normalize();

        // Rotate around parent direction
        double x = Math.cos(theta) * Math.sin(phi);
        double y = Math.sin(theta) * Math.sin(phi);
        double z = Math.cos(phi);

        Vec3 newDir = tangent.scale(x)
                .add(bitangent.scale(y))
                .add(parentDir.scale(z));

        return newDir.normalize();
    }

    private void updateAllBranches() {
        for (LightningBranch branch : branches) {
            branch.generateJaggedPath(new Random(), currentDistance);
        }
    }

    private void checkBranchCollisions() {
        for (LightningBranch branch : branches) {
            float branchProgress = Math.min(currentDistance / maxDistance, 1.0f);
            Vec3 currentEnd = branch.startPos.add(branch.direction.scale(branch.maxLength * branchProgress));

            // Raycast
            HitResult hit = level().clip(new ClipContext(branch.startPos, currentEnd,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

            // Check entities
            List<Entity> entities = level().getEntities(this, new AABB(branch.startPos, currentEnd).inflate(0.5));
            for (Entity entity : entities) {
                if (entity != this && !entity.isSpectator() && entity != source) {
                    onHitEntity(entity, branch.depth);
                }
            }

            // Check blocks
            if (hit.getType() != HitResult.Type.MISS) {
                onHitBlock(hit);
            }
        }
    }

    private void onHitEntity(Entity entity, int branchDepth) {
        if (!level().isClientSide) {
            // Damage reduces with branch depth
            float damageMultiplier = (float)Math.pow(0.7, branchDepth);

            DamageSource dmg = (source != null)
                    ? level().damageSources().mobAttack(source)
                    : level().damageSources().generic();

            entity.hurt(dmg, (float)(damage * damageMultiplier));
        }
    }

    private void onHitBlock(HitResult hit) {
        if (!level().isClientSide) {
            // Add your block hit logic here
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
        builder.define(MAX_DISTANCE, 10.0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("startX")) {
            startPos = new Vec3(tag.getDouble("startX"), tag.getDouble("startY"), tag.getDouble("startZ"));
            direction = new Vec3(tag.getDouble("dirX"), tag.getDouble("dirY"), tag.getDouble("dirZ"));
            maxDistance = tag.getFloat("maxDistance");
            currentDistance = tag.getFloat("currentDistance");
            initializeBranches();
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
    public List<LightningBranch> getBranches() {
        return branches;
    }

    public float getCurrentDistance() {
        return currentDistance;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }

    // Inner class to represent a single lightning branch
    public static class LightningBranch {
        public final Vec3 startPos;
        public final Vec3 direction;
        public final float maxLength;
        public final int depth; // How many branches deep (0 = main branch)
        public final float parentProgress; // At what point (0-1) along parent this branch spawns
        private final List<Vec3> points = new ArrayList<>();

        public LightningBranch(Vec3 startPos, Vec3 direction, float maxLength, int depth) {
            this(startPos, direction, maxLength, depth, 0f);
        }

        public LightningBranch(Vec3 startPos, Vec3 direction, float maxLength, int depth, float parentProgress) {
            this.startPos = startPos;
            this.direction = direction.normalize();
            this.maxLength = maxLength;
            this.depth = depth;
            this.parentProgress = parentProgress;

            // Initialize with start point
            points.add(startPos);
        }

        public void generateJaggedPath(java.util.Random random, float globalProgress) {
            points.clear();

            // Child branches only start growing after their parent has reached their spawn point
            // Calculate effective progress for this branch
            float adjustedProgress = globalProgress;
            if (depth > 0) {
                // This branch should only start appearing after the parent reaches parentProgress
                float parentReachDistance = maxLength / BRANCH_LENGTH_MULTIPLIER; // Approximate parent length
                float startDistance = parentReachDistance * parentProgress;

                // Only start growing after parent has reached this point
                if (globalProgress < startDistance) {
                    points.add(startPos);
                    return; // Branch hasn't started yet
                }

                // Adjust progress relative to when this branch starts
                adjustedProgress = globalProgress - startDistance;
            }

            // Calculate how far this branch should extend based on adjusted progress
            float branchProgress = Math.min(1.0f, adjustedProgress / maxLength);
            if (branchProgress <= 0) {
                points.add(startPos);
                return;
            }

            Vec3 target = startPos.add(direction.scale(maxLength * branchProgress));

            int segments = Math.max(1, (int)(maxLength * branchProgress / 0.8f));

            for (int i = 0; i <= segments; i++) {
                float progress = segments > 0 ? (float)i / segments : 0f;
                Vec3 basePoint = startPos.lerp(target, progress);

                // Add jitter (less jitter for deeper branches)
                if (i > 0 && i < segments) {
                    double jitterAmount = 0.4 * Math.pow(0.8, depth);
                    double offsetX = (random.nextDouble() - 0.5) * jitterAmount;
                    double offsetY = (random.nextDouble() - 0.5) * jitterAmount;
                    double offsetZ = (random.nextDouble() - 0.5) * jitterAmount;
                    basePoint = basePoint.add(offsetX, offsetY, offsetZ);
                }

                points.add(basePoint);
            }
        }

        public List<Vec3> getPoints() {
            return points;
        }

        public float getWidth() {
            // Thinner branches for deeper levels
            return 0.15f * (float)Math.pow(0.7, depth);
        }
    }
}
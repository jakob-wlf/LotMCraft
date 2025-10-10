package de.jakob.lotm.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SpaceCollapseEntityLegacy extends Entity {
    private static final EntityDataAccessor<Integer> GROWTH_STAGE = 
        SynchedEntityData.defineId(SpaceCollapseEntityLegacy.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SIZE = 
        SynchedEntityData.defineId(SpaceCollapseEntityLegacy.class, EntityDataSerializers.FLOAT);
    
    // Growth stages
    private static final int STAGE_CRACK = 0;
    private static final int STAGE_RIFT = 1;
    private static final int STAGE_COLLAPSE = 2;
    private static final int STAGE_DISSIPATE = 3;
    
    // Configuration
    private static final float CRACK_SIZE = 0.5f;
    private static final float RIFT_SIZE = 2.5f;
    private static final float COLLAPSE_SIZE = 6.0f;
    private static final int CRACK_DURATION = 40; // 2 seconds
    private static final int RIFT_DURATION = 60; // 3 seconds
    private static final int COLLAPSE_DURATION = 100; // 5 seconds
    private static final int DISSIPATE_DURATION = 40; // 2 seconds
    
    private int tickCount = 0;
    private int stageTickCount = 0;
    private float targetSize = CRACK_SIZE;

    public SpaceCollapseEntityLegacy(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public int getTickCount() {
        return tickCount;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(GROWTH_STAGE, STAGE_CRACK);
        builder.define(SIZE, CRACK_SIZE);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            tickCount++;
            stageTickCount++;

            updateGrowthStage();
            updateSize();
            applyEffects();

            // Remove entity when dissipation is complete
            if (getGrowthStage() == STAGE_DISSIPATE && stageTickCount > DISSIPATE_DURATION) {
                this.discard();
            }
        } else {
            spawnParticles();
        }

        playAmbientSound();
    }

    private void updateGrowthStage() {
        int currentStage = getGrowthStage();

        switch (currentStage) {
            case STAGE_CRACK:
                if (stageTickCount > CRACK_DURATION) {
                    setGrowthStage(STAGE_RIFT);
                    targetSize = RIFT_SIZE;
                    stageTickCount = 0;
                }
                break;
            case STAGE_RIFT:
                if (stageTickCount > RIFT_DURATION) {
                    setGrowthStage(STAGE_COLLAPSE);
                    targetSize = COLLAPSE_SIZE;
                    stageTickCount = 0;
                }
                break;
            case STAGE_COLLAPSE:
                if (stageTickCount > COLLAPSE_DURATION) {
                    setGrowthStage(STAGE_DISSIPATE);
                    targetSize = 0.1f;
                    stageTickCount = 0;
                }
                break;
        }
    }

    private void updateSize() {
        float currentSize = getSize();
        float growthSpeed = 0.05f;

        if (getGrowthStage() == STAGE_DISSIPATE) {
            growthSpeed = 0.15f; // Faster collapse
        }

        float newSize = Mth.lerp(growthSpeed, currentSize, targetSize);
        setSize(newSize);

        // Update bounding box
        this.setBoundingBox(new AABB(
                getX() - newSize, getY() - newSize, getZ() - newSize,
                getX() + newSize, getY() + newSize, getZ() + newSize
        ));
    }

    private void applyEffects() {
        int stage = getGrowthStage();
        float size = getSize();

        // Damage entities
        if (stage >= STAGE_RIFT) {
            damageNearbyEntities(size);
        }

        // Destroy blocks
        if (stage == STAGE_COLLAPSE && tickCount % 5 == 0) {
            destroyBlocks(size);
        }

        // Pull entities
        if (stage >= STAGE_RIFT) {
            pullNearbyEntities(size);
        }
    }

    private void damageNearbyEntities(float radius) {
        List<Entity> entities = level().getEntities(this,
                new AABB(getX() - radius, getY() - radius, getZ() - radius,
                        getX() + radius, getY() + radius, getZ() + radius));

        float damageAmount = switch (getGrowthStage()) {
            case STAGE_RIFT -> 2.0f;
            case STAGE_COLLAPSE -> 5.0f;
            default -> 0f;
        };

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && entity.distanceTo(this) < radius) {
                entity.hurt(level().damageSources().magic(), damageAmount);
            }
        }
    }

    private void pullNearbyEntities(float radius) {
        List<Entity> entities = level().getEntities(this,
                new AABB(getX() - radius * 2, getY() - radius * 2, getZ() - radius * 2,
                        getX() + radius * 2, getY() + radius * 2, getZ() + radius * 2));

        float pullStrength = getGrowthStage() == STAGE_COLLAPSE ? 0.15f : 0.05f;

        for (Entity entity : entities) {
            if (entity != this && entity.distanceTo(this) < radius * 2) {
                Vec3 direction = this.position().subtract(entity.position()).normalize();
                entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(pullStrength)));
            }
        }
    }

    private void destroyBlocks(float radius) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        BlockPos center = this.blockPosition();
        int r = (int) Math.ceil(radius);

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-r, -r, -r),
                center.offset(r, r, r))) {

            if (center.distSqr(pos) <= radius * radius) {
                BlockState state = level().getBlockState(pos);

                // Don't destroy bedrock or air
                if (!state.isAir() && state.getDestroySpeed(level(), pos) >= 0) {
                    // 30% chance to destroy each block for more gradual effect
                    if (random.nextFloat() < 0.3f) {
                        level().destroyBlock(pos, false);

                        // Spawn particles at block location
                        serverLevel.sendParticles(ParticleTypes.PORTAL,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                5, 0.3, 0.3, 0.3, 0.0);
                    }
                }
            }
        }
    }

    private void spawnParticles() {
        float size = getSize();
        int stage = getGrowthStage();

        // Particle counts scale with size and stage
        int baseCount = switch (stage) {
            case STAGE_CRACK -> 5;
            case STAGE_RIFT -> 15;
            case STAGE_COLLAPSE -> 40;
            case STAGE_DISSIPATE -> 20;
            default -> 0;
        };

        // Particles at the edge being pulled in
        for (int i = 0; i < baseCount; i++) {
            // Spawn at edge
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = size * (1.2 + random.nextDouble() * 0.5);
            double edgeX = Math.cos(angle) * distance;
            double edgeY = (random.nextDouble() - 0.5) * size * 2;
            double edgeZ = Math.sin(angle) * distance;

            // Pull toward center
            double velocityScale = 0.15 + (stage * 0.05);

            // Portal particles being sucked in
            level().addParticle(ParticleTypes.PORTAL,
                    getX() + edgeX, getY() + edgeY, getZ() + edgeZ,
                    -edgeX * velocityScale, -edgeY * velocityScale, -edgeZ * velocityScale);

            // Reverse portal for energy effect
            if (stage >= STAGE_RIFT) {
                level().addParticle(ParticleTypes.REVERSE_PORTAL,
                        getX() + edgeX, getY() + edgeY, getZ() + edgeZ,
                        -edgeX * velocityScale * 0.7, -edgeY * velocityScale * 0.7, -edgeZ * velocityScale * 0.7);
            }
        }

        // Violent particles at the rift edge
        if (stage >= STAGE_RIFT) {
            int edgeCount = stage == STAGE_COLLAPSE ? 30 : 15;
            for (int i = 0; i < edgeCount; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double edgeX = Math.cos(angle) * size;
                double edgeY = (random.nextDouble() - 0.5) * size * 0.5;
                double edgeZ = Math.sin(angle) * size;

                // Dragon breath and soul fire flame for violent tearing effect
                level().addParticle(ParticleTypes.DRAGON_BREATH,
                        getX() + edgeX, getY() + edgeY, getZ() + edgeZ,
                        (random.nextDouble() - 0.5) * 0.3,
                        (random.nextDouble() - 0.5) * 0.3,
                        (random.nextDouble() - 0.5) * 0.3);

                if (stage == STAGE_COLLAPSE) {
                    level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            getX() + edgeX, getY() + edgeY, getZ() + edgeZ,
                            0, 0.1, 0);
                }
            }
        }

        // Void particles inside the rift
        if (stage >= STAGE_CRACK) {
            int innerCount = 10 + (stage * 5);
            for (int i = 0; i < innerCount; i++) {
                double innerX = (random.nextDouble() - 0.5) * size * 0.8;
                double innerY = (random.nextDouble() - 0.5) * size * 0.8;
                double innerZ = (random.nextDouble() - 0.5) * size * 0.8;

                // Smoke and ash particles for the void
                level().addParticle(ParticleTypes.LARGE_SMOKE,
                        getX() + innerX, getY() + innerY, getZ() + innerZ,
                        0, -0.05, 0);
            }
        }
    }

    private void playAmbientSound() {
        if (tickCount % 10 == 0 && !level().isClientSide) {
            float volume = switch (getGrowthStage()) {
                case STAGE_CRACK -> 0.5f;
                case STAGE_RIFT -> 1.0f;
                case STAGE_COLLAPSE -> 1.5f;
                case STAGE_DISSIPATE -> 0.8f;
                default -> 0f;
            };

            // Deep, ominous portal sound
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE,
                    volume, 0.3f + random.nextFloat() * 0.3f);

            // Add wither ambient for extra dread during collapse
            if (getGrowthStage() == STAGE_COLLAPSE && tickCount % 20 == 0) {
                level().playSound(null, getX(), getY(), getZ(),
                        SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE,
                        volume * 0.7f, 0.5f);
            }
        }
    }

    // Getters and setters
    public int getGrowthStage() {
        return this.entityData.get(GROWTH_STAGE);
    }

    public void setGrowthStage(int stage) {
        this.entityData.set(GROWTH_STAGE, stage);
    }

    public float getSize() {
        return this.entityData.get(SIZE);
    }

    public void setSize(float size) {
        this.entityData.set(SIZE, size);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("TickCount");
        this.stageTickCount = tag.getInt("StageTickCount");
        this.targetSize = tag.getFloat("TargetSize");
        setGrowthStage(tag.getInt("GrowthStage"));
        setSize(tag.getFloat("Size"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TickCount", this.tickCount);
        tag.putInt("StageTickCount", this.stageTickCount);
        tag.putFloat("TargetSize", this.targetSize);
        tag.putInt("GrowthStage", getGrowthStage());
        tag.putFloat("Size", getSize());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false; // Cannot be damaged
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
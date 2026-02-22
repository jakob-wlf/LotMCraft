package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class SpaceCollapseEntity extends Entity {
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(SpaceCollapseEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(SpaceCollapseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(SpaceCollapseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> GRIEFING = SynchedEntityData.defineId(SpaceCollapseEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(SpaceCollapseEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final int MAX_AGE = 130;
    private static final float MAX_RADIUS = 15.0F;
    private static final float INITIAL_RADIUS = 0.5F;

    public SpaceCollapseEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SpaceCollapseEntity(Level level, Vec3 position) {
        this(ModEntities.SPACE_COLLAPSE.get(), level);
        this.setPos(position);
    }

    public SpaceCollapseEntity(Level level, Vec3 position, float damage, boolean griefing, LivingEntity owner) {
        this(ModEntities.SPACE_COLLAPSE.get(), level);
        this.setPos(position);
        this.setDamage(damage);
        this.setGriefing(griefing);
        if (owner != null) {
            this.setOwnerUUID(Optional.of(owner.getUUID()));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AGE, 0);
        builder.define(RADIUS, INITIAL_RADIUS);
        builder.define(DAMAGE, 10.0F);
        builder.define(GRIEFING, false);
        builder.define(OWNER_UUID, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();

        int age = this.getAge();
        age++;
        this.setAge(age);

        float radius = getRadius(age);

        this.setRadius(radius);

        if (!this.level().isClientSide) {
            if(age % 4 == 0)
                damageNearbyEntities(radius);
            if(isGriefing())
                breakBlocks(radius);
            spawnParticles(radius);

            if (age % 10 == 0) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE,
                        2.0F, 0.5F + ((age / (float) MAX_AGE) * 0.5F));
            }

            if (age >= MAX_AGE) {
                this.discard();
            }
        }
    }

    private float getRadius(int age) {
        float radius;
        // Collapse phase starts in the last 20 ticks
        if (age >= MAX_AGE - 20) {
            float collapseProgress = (age - (MAX_AGE - 20)) / 20.0F;
            // Rapidly shrink down to zero
            radius = MAX_RADIUS * (1.0F - easeInCubic(collapseProgress));
        } else {
            // Normal expansion phase
            float progress = Math.min(1.0F, age / (float) (MAX_AGE - 20));
            radius = INITIAL_RADIUS + (MAX_RADIUS - INITIAL_RADIUS) * easeOutCubic(progress);
        }
        return radius;
    }

    private void damageNearbyEntities(float radius) {
        AbilityUtil.damageNearbyEntities((ServerLevel) level(), getOwner(), radius, getDamage(), this.position(), true, false, 0);
    }

    private void breakBlocks(float radius) {
        AbilityUtil.getBlocksInSphereRadius((ServerLevel) level(), this.position(), radius, true).forEach(pos -> {
            if(level().getBlockState(pos).getDestroySpeed(level(), pos) < 0)
                return;
            level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        });
    }

    private void spawnParticles(float radius) {
        if (this.level() instanceof ServerLevel serverLevel) {
            int particleCount = (int) (radius * 10);

            for (int i = 0; i < particleCount; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2;
                double distance = this.random.nextDouble() * radius;
                double offsetX = Math.cos(angle) * distance;
                double offsetZ = Math.sin(angle) * distance;
                double offsetY = (this.random.nextDouble() - 0.5) * radius;

                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                );
            }
        }
    }

    private float easeOutCubic(float x) {
        return 1 - (float) Math.pow(1 - x, 3);
    }

    private float easeInCubic(float x) {
        return (float) Math.pow(x, 3);
    }

    public int getAge() {
        return this.entityData.get(AGE);
    }

    public void setAge(int age) {
        this.entityData.set(AGE, age);
    }

    public float getRadius() {
        return this.entityData.get(RADIUS);
    }

    public void setRadius(float radius) {
        this.entityData.set(RADIUS, radius);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public boolean isGriefing() {
        return this.entityData.get(GRIEFING);
    }

    public void setGriefing(boolean griefing) {
        this.entityData.set(GRIEFING, griefing);
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public void setOwnerUUID(Optional<UUID> ownerUUID) {
        this.entityData.set(OWNER_UUID, ownerUUID);
    }

    public LivingEntity getOwner() {
        try {
            UUID ownerUUID = this.getOwnerUUID().orElse(null);
            if(ownerUUID == null) return null;
            if(!(level() instanceof ServerLevel serverLevel)) return null;
            Entity owner = serverLevel.getEntity(ownerUUID);
            return owner instanceof LivingEntity livingOwner ? livingOwner : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setAge(tag.getInt("Age"));
        this.setRadius(tag.getFloat("Radius"));
        this.setDamage(tag.getFloat("Damage"));
        this.setGriefing(tag.getBoolean("Griefing"));
        if (tag.contains("OwnerUUID")) {
            this.setOwnerUUID(Optional.of(tag.getUUID("OwnerUUID")));
        } else {
            this.setOwnerUUID(Optional.empty());
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.getAge());
        tag.putFloat("Radius", this.getRadius());
        tag.putFloat("Damage", this.getDamage());
        tag.putBoolean("Griefing", this.isGriefing());
        this.getOwnerUUID().ifPresent(uuid -> tag.putUUID("OwnerUUID", uuid));
    }
}

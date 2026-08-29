package de.jakob.lotm.entity.custom.ability_entities;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class PortalEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_MAX_AGE =
            SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DEST_X =
            SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DEST_Y =
            SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DEST_Z =
            SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> DATA_DEST_DIM =
            SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);

    public PortalEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setMaxAge(20);
    }

    public PortalEntity(Vec3 pos, Level level, Level destination, Vec3 destinationPos, int maxAge) {
        super(ModEntities.PORTAL.get(), level);

        this.setMaxAge(maxAge);
        this.setDestinationPos(destinationPos);
        this.setDestinationLevel(destination);

        this.setPos(pos);
    }

    public int getMaxAge() {
        return this.entityData.get(DATA_MAX_AGE);
    }

    public void setMaxAge(int maxAge) {
        this.entityData.set(DATA_MAX_AGE, maxAge);
    }

    public Vec3 getDestinationPos() {
        return new Vec3(
                this.entityData.get(DATA_DEST_X),
                this.entityData.get(DATA_DEST_Y),
                this.entityData.get(DATA_DEST_Z)
        );
    }

    public void setDestinationPos(Vec3 destinationPos) {
        this.entityData.set(DATA_DEST_X, (float) destinationPos.x);
        this.entityData.set(DATA_DEST_Y, (float) destinationPos.y);
        this.entityData.set(DATA_DEST_Z, (float) destinationPos.z);
    }

    public void setDestinationLevel(Level destination) {
        this.entityData.set(DATA_DEST_DIM, destination.dimension().location().toString());
    }

    public Level getDestinationLevel() {
        String raw = this.entityData.get(DATA_DEST_DIM);
        if (raw.isEmpty() || this.level().getServer() == null) return null;

        ResourceLocation dimLocation = ResourceLocation.tryParse(raw);
        if (dimLocation == null) return null;

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLocation);
        return this.level().getServer().getLevel(dimKey);
    }

    @Override
    public void tick() {
        super.tick();

        int maxAge = this.getMaxAge();
        if (maxAge > 0 && tickCount % maxAge == 0) {
            this.discard();
            return;
        }

        if(!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 center = this.position();
        double baseRadius = 0.8;
        double helixHeight = 2.0;

        double rotationSpeed = 0.15;
        double angle = tickCount * rotationSpeed;

        for (int strand = 0; strand < 2; strand++) {
            double strandAngle = angle + strand * Math.PI;
            double y = center.y + (Math.sin(tickCount * 0.1 + strand * Math.PI) * 0.5 + 0.5) * helixHeight;
            double x = center.x + baseRadius * Math.cos(strandAngle);
            double z = center.z + baseRadius * Math.sin(strandAngle);

            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.PORTAL,
                    new Vec3(x, y, z), 1, 0.05, 0.0);
        }

        if (tickCount % 2 == 0) {
            ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.REVERSE_PORTAL, center, baseRadius, 10);
        }

        if (tickCount % 4 == 0) {
            double spokeAngle = tickCount * 0.3;
            Vec3 edge = new Vec3(
                    center.x + baseRadius * Math.cos(spokeAngle),
                    center.y + 0.5,
                    center.z + baseRadius * Math.sin(spokeAngle)
            );
            ParticleUtil.drawParticleLine(serverLevel, ParticleTypes.WITCH, edge, center.add(0, 0.5, 0), 0.2, 1);
        }

        if (tickCount % 10 == 0) {
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, center, 0.3, 6, 0.01);
        }

        Level destination = this.getDestinationLevel();
        if(!(destination instanceof ServerLevel serverDestination)) return;

        Vec3 destinationPos = this.getDestinationPos();

        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(.5));
        for (LivingEntity entity : nearbyEntities) {
            if (entity.isAlive() && !entity.isRemoved() && !entity.isOnPortalCooldown()) {
                Vec3 fromPos = entity.position();

                entity.teleportTo(serverDestination, destinationPos.x, destinationPos.y, destinationPos.z, Set.of(), entity.getYRot(), entity.getXRot());
                entity.setDeltaMovement(Vec3.ZERO);
                entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 2, false, false, true));

                ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.PORTAL, fromPos, 0.6, 20, 0.05);
                ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.PORTAL, destinationPos, 0.6, 20, 0.05);

                entity.setPortalCooldown(20 * 5);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_MAX_AGE, 20);
        builder.define(DATA_DEST_X, 0.0f);
        builder.define(DATA_DEST_Y, 0.0f);
        builder.define(DATA_DEST_Z, 0.0f);
        builder.define(DATA_DEST_DIM, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.setMaxAge(compoundTag.getInt("MaxAge"));
        this.setDestinationPos(new Vec3(
                compoundTag.getDouble("DestX"),
                compoundTag.getDouble("DestY"),
                compoundTag.getDouble("DestZ")
        ));
        this.entityData.set(DATA_DEST_DIM, compoundTag.getString("DestDim"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("MaxAge", this.getMaxAge());

        Vec3 destinationPos = this.getDestinationPos();
        compoundTag.putDouble("DestX", destinationPos.x);
        compoundTag.putDouble("DestY", destinationPos.y);
        compoundTag.putDouble("DestZ", destinationPos.z);

        compoundTag.putString("DestDim", this.entityData.get(DATA_DEST_DIM));
    }
}
package de.jakob.lotm.entity.custom.ability_entities.red_priest_pathway;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.entity.custom.projectiles.FireballEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

public class FirePlateEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(FirePlateEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(FirePlateEntity.class, EntityDataSerializers.FLOAT);
    private float orbitAngle = 0.0f;

    // Configuration values
    private static final double ORBIT_RADIUS = 1.0;
    private static final float ORBIT_SPEED = 0.05f; // Radians per tick

    public FirePlateEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setSize(0.3f);
        this.setBoundingBox(this.getBoundingBox().inflate(0.3f));

    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if(level().isClientSide)
            return;
    }

    public FirePlateEntity(EntityType<?> entityType, Level level, UUID casterUUID) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
//        this.setDuration(ticks);
        this.setCasterUUID(casterUUID);
    }


    int lifetime = 0;

//    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(.45f, .25f, .25f), 1.5f);

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            Entity player = getCasterEntity();
            if (player != null && player.isAlive()) {
//                this.orbitAngle += 0.01F;
//
//                double radius = 0.5D;
//

//                this.setDeltaMovement(0, 0, 0);
                // 1. Advance the angle
                orbitAngle += ORBIT_SPEED;
                if (orbitAngle > Math.PI * 2) {
                    orbitAngle -= Math.PI * 2;
                }

                // 2. Calculate the target position
                double targetX = player.getX() + (Math.cos(orbitAngle) * ORBIT_RADIUS);
                double targetY = player.getY() + 2.25; // Eye-ish level
                double targetZ = player.getZ() + (Math.sin(orbitAngle) * ORBIT_RADIUS);
                this.setPos(targetX, targetY, targetZ);
                // 3. Calculate velocity vector required to get there
                // We use a small multiplier (e.g., 0.5) to smoothly pull it into position without snapping
                double motionX = (targetX - this.getX()) * 0.5;
                double motionY = (targetY - this.getY()) * 0.5;
                double motionZ = (targetZ - this.getZ()) * 0.5;

                // 4. Apply velocity directly
                this.setDeltaMovement(motionX, motionY, motionZ);

                // Forces Minecraft to synchronize client-side movement smoother
                this.hasImpulse = true;

                // Inside your tick method, replace the broken lookAt line with this:
                this.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(player.getX(), player.getEyeY(), player.getZ()));
            } else {
                this.discard();
            }
        }
    }


    public void setCasterUUID(UUID uuid) {
        this.entityData.set(OWNER, Optional.ofNullable(uuid));
    }

    public UUID getCasterUUID() {
        return this.entityData.get(OWNER).orElse(null);
    }


    public Entity getCasterEntity() {
        if(level().isClientSide) {
            return null;
        }
        UUID casterUUID = this.getCasterUUID();
        if (casterUUID == null) {
            return null;
        }
        return ((ServerLevel) level()).getEntity(casterUUID);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, Optional.empty());
        builder.define(SIZE, 0.3f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("owner")) {
            setCasterUUID(compoundTag.getUUID("owner"));
        } else {
            setCasterUUID(null);
        }
        this.setSize(compoundTag.getFloat("Size"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (getCasterUUID() != null) {
            compoundTag.putUUID("owner", getCasterUUID());

        }
        compoundTag.putFloat("Size", this.getSize());
    }

    public void setSize(float size) {
        this.entityData.set(SIZE, size);
    }

    public float getSize() {
        return this.entityData.get(SIZE);
    }
}

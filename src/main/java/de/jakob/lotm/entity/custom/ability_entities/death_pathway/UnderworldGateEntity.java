package de.jakob.lotm.entity.custom.ability_entities.death_pathway;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class UnderworldGateEntity extends Entity {

    public AnimationState openAnimationState = new AnimationState();
    public AnimationState tentacleAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> HAS_TENTACLES = SynchedEntityData.defineId(UnderworldGateEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String TELEPORT_COOLDOWN_KEY = "lotm_underworld_gate_cooldown";

    private boolean travelEnabled;
    private String destinationDimension = "";
    private double destinationX;
    private double destinationZ;
    private boolean createReturnGate = true;
    private boolean returnGateCreated;


    public UnderworldGateEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    public UnderworldGateEntity(Level level, boolean hasTentacles) {
        super(ModEntities.UNDERWORLD_GATE.get(), level);
        setHasTentacles(hasTentacles);
    }

    @Override
    public void tick() {
        if(level().isClientSide) {
            if (this.tickCount == 1) {
                openAnimationState.start(0);
            }

            if(this.tickCount >= 22) {
                tentacleAnimationState.startIfStopped(0);
            }
            return;
        }

        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.SOUL, position().add(getLookAngle().normalize().scale(.85)), 12, .8, 0.25, .8, 0);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.ENCHANT, getEyePosition().subtract(0, .2, 0), 8, 1.5, 1.5, 1.5, 0);
        ParticleUtil.spawnParticles((ServerLevel) level(), ParticleTypes.SMOKE, getEyePosition().subtract(0, .2, 0), 8, 1.5, 1.5, 1.5, 0);

        if (travelEnabled && tickCount >= 22) {
            teleportNearbyEntities((ServerLevel) level());
        }

        int lifetime = travelEnabled ? 20 * 20 : 20 * 60 * 2;
        if(this.tickCount > lifetime) {
            this.discard();
        }
    }

    public void setHasTentacles(boolean hasTentacles) {
        entityData.set(HAS_TENTACLES, hasTentacles);
    }

    public boolean hasTentacles() {
        return entityData.get(HAS_TENTACLES);
    }

    public void setTravelDestination(ResourceKey<Level> dimension, double x, double z) {
        setTravelDestination(dimension, x, z, true);
    }

    private void setTravelDestination(ResourceKey<Level> dimension, double x, double z,
                                      boolean shouldCreateReturnGate) {
        travelEnabled = true;
        destinationDimension = dimension.location().toString();
        destinationX = x;
        destinationZ = z;
        createReturnGate = shouldCreateReturnGate;
    }

    private void teleportNearbyEntities(ServerLevel sourceLevel) {
        ResourceLocation destinationId = ResourceLocation.tryParse(destinationDimension);
        if (destinationId == null) return;
        ServerLevel destination = sourceLevel.getServer().getLevel(
                ResourceKey.create(Registries.DIMENSION, destinationId));
        if (destination == null) return;

        long now = sourceLevel.getGameTime();
        for (Entity entity : sourceLevel.getEntities(this, getBoundingBox().inflate(1.0),
                candidate -> candidate != this && candidate.isAlive())) {
            if (entity.getPersistentData().getLong(TELEPORT_COOLDOWN_KEY) > now) continue;

            int y = destination.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (int) Math.floor(destinationX), (int) Math.floor(destinationZ));
            Vec3 arrival = new Vec3(destinationX + 0.5, y + 1.0, destinationZ + 0.5);
            entity.getPersistentData().putLong(TELEPORT_COOLDOWN_KEY, destination.getGameTime() + 60);
            entity.teleportTo(destination, arrival.x, arrival.y, arrival.z,
                    Set.of(), entity.getYRot(), entity.getXRot());

            if (createReturnGate && !returnGateCreated) {
                UnderworldGateEntity returnGate = new UnderworldGateEntity(destination, false);
                returnGate.setPos(arrival.x + 3.0, arrival.y, arrival.z);
                returnGate.setYRot(getYRot() + 180.0f);
                returnGate.setTravelDestination(sourceLevel.dimension(), getX(), getZ(), false);
                destination.addFreshEntity(returnGate);
                returnGateCreated = true;
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HAS_TENTACLES, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setHasTentacles(compoundTag.getBoolean("HasTentacles"));
        travelEnabled = compoundTag.getBoolean("TravelEnabled");
        destinationDimension = compoundTag.getString("DestinationDimension");
        destinationX = compoundTag.getDouble("DestinationX");
        destinationZ = compoundTag.getDouble("DestinationZ");
        createReturnGate = compoundTag.contains("CreateReturnGate")
            && compoundTag.getBoolean("CreateReturnGate");
        returnGateCreated = compoundTag.getBoolean("ReturnGateCreated");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("HasTentacles", hasTentacles());
        compoundTag.putBoolean("TravelEnabled", travelEnabled);
        compoundTag.putString("DestinationDimension", destinationDimension);
        compoundTag.putDouble("DestinationX", destinationX);
        compoundTag.putDouble("DestinationZ", destinationZ);
        compoundTag.putBoolean("CreateReturnGate", createReturnGate);
        compoundTag.putBoolean("ReturnGateCreated", returnGateCreated);
    }
}

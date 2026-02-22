package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class LocationGraftingEntity extends Entity {
    private Vec3 teleportPos;
    private ResourceKey<Level> teleportDimension;

    public LocationGraftingEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public LocationGraftingEntity(Level level, Vec3 spawnPos, Vec3 returnPos, ResourceKey<Level> returnDim) {
        this(ModEntities.GRAFTING_LOCATION_ENTITY.get(), level);
        this.setPos(spawnPos);
        this.teleportPos = returnPos;
        this.teleportDimension = returnDim;
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!level().isClientSide && teleportPos != null && teleportDimension != null) {
            // Check for nearby players
            AABB checkBox = this.getBoundingBox().inflate(1.0);
            List<LivingEntity> nearbyEntities = level().getEntitiesOfClass(
                LivingEntity.class,
                checkBox,
                player -> player.distanceToSqr(this) < 2.0
            );
            
            for (LivingEntity entity : nearbyEntities) {
                teleportEntity(entity);
            }
        }
    }
    
    private void teleportEntity(LivingEntity entity) {
        if(!(entity.level() instanceof ServerLevel serverLevel)) return;

        ServerLevel targetLevel = serverLevel.getServer().getLevel(teleportDimension);

        if (targetLevel != null) {
            entity.teleportTo(targetLevel,
                teleportPos.x,
                teleportPos.y,
                teleportPos.z,
                Set.of(),
                entity.getYRot(),
                entity.getXRot());
        }
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synched data needed
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("TeleportX")) {
            this.teleportPos = new Vec3(
                tag.getDouble("TeleportX"),
                tag.getDouble("TeleportY"),
                tag.getDouble("TeleportZ")
            );
        }
        if (tag.contains("TeleportDimension")) {
            this.teleportDimension = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(tag.getString("TeleportDimension"))
            );
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (teleportPos != null) {
            tag.putDouble("TeleportX", teleportPos.x);
            tag.putDouble("TeleportY", teleportPos.y);
            tag.putDouble("TeleportZ", teleportPos.z);
        }
        if (teleportDimension != null) {
            tag.putString("TeleportDimension", teleportDimension.location().toString());
        }
    }
}
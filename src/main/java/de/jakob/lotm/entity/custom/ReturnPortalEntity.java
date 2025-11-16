package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ReturnPortalEntity extends Entity {
    private Vec3 returnPosition;
    private ResourceKey<Level> returnDimension;
    
    public ReturnPortalEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }
    
    public ReturnPortalEntity(Level level, Vec3 spawnPos, Vec3 returnPos, ResourceKey<Level> returnDim) {
        this(ModEntities.RETURN_PORTAL.get(), level);
        this.setPos(spawnPos);
        this.returnPosition = returnPos;
        this.returnDimension = returnDim;
    }

    int tickCount = 0;

    public int getTickCount() {
        return tickCount;
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!level().isClientSide && returnPosition != null && returnDimension != null) {
            tickCount++;

            if(tickCount < 20 * 5) {
                return;
            }

            // Check for nearby players
            AABB checkBox = this.getBoundingBox().inflate(1.0);
            List<ServerPlayer> nearbyPlayers = level().getEntitiesOfClass(
                ServerPlayer.class, 
                checkBox,
                player -> player.distanceToSqr(this) < 2.0
            );
            
            for (ServerPlayer player : nearbyPlayers) {
                teleportPlayerBack(player);
            }
        }
    }
    
    private void teleportPlayerBack(ServerPlayer player) {
        ServerLevel targetLevel = player.server.getLevel(returnDimension);
        if (targetLevel != null) {
            player.teleportTo(targetLevel,
                returnPosition.x,
                returnPosition.y,
                returnPosition.z,
                player.getYRot(),
                player.getXRot());
            
            // Remove this portal entity
            this.discard();
        }
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synched data needed
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("ReturnX")) {
            this.returnPosition = new Vec3(
                tag.getDouble("ReturnX"),
                tag.getDouble("ReturnY"),
                tag.getDouble("ReturnZ")
            );
        }
        if (tag.contains("ReturnDimension")) {
            this.returnDimension = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(tag.getString("ReturnDimension"))
            );
        }
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (returnPosition != null) {
            tag.putDouble("ReturnX", returnPosition.x);
            tag.putDouble("ReturnY", returnPosition.y);
            tag.putDouble("ReturnZ", returnPosition.z);
        }
        if (returnDimension != null) {
            tag.putString("ReturnDimension", returnDimension.location().toString());
        }
    }
}
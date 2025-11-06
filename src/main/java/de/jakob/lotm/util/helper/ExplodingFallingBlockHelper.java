package de.jakob.lotm.util.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ExplodingFallingBlockHelper {
    private static final Map<FallingBlockEntity, ExplodingBlockData> trackedEntities = new HashMap<>();
    private static final double IMPACT_DISTANCE_THRESHOLD = 3.5; // Distance to ground before explosion


    private static class ExplodingBlockData {
        float explosionPower;
        float damage;
        boolean spawnLava;
        @Nullable
        UUID ownerUUID;

        boolean griefing;

        ExplodingBlockData(float explosionPower, float damage, boolean spawnLava, @Nullable LivingEntity owner, boolean griefing) {
            this.explosionPower = explosionPower;
            this.damage = damage;
            this.spawnLava = spawnLava;
            this.griefing = griefing;
            this.ownerUUID = owner != null ? owner.getUUID() : null;
        }
    }
    
    /**
     * Spawns a falling block that will explode on impact
     *
     * @param level          The level to spawn in
     * @param startPos       Starting position
     * @param state          Block state to spawn
     * @param velocity       Initial velocity
     * @param griefing       Whether griefing is enabled
     * @param explosionPower Power of the explosion (2.0F = creeper)
     * @param damage         Direct damage dealt to entities in radius
     * @param spawnLava      Whether to spawn lava at impact point
     * @param owner          The entity that spawned this block (for damage attribution)
     */
    public static void spawnExplodingFallingBlock(
            Level level, 
            Vec3 startPos, 
            BlockState state, 
            Vec3 velocity, 
            boolean griefing,
            float explosionPower,
            float damage,
            boolean spawnLava,
            @Nullable LivingEntity owner) {
        
        BlockPos blockPos = BlockPos.containing(startPos);
        
        FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, blockPos, state);
        fallingBlock.setDeltaMovement(velocity);
        
        if(!griefing) {
            fallingBlock.disableDrop();
        }
        
        fallingBlock.hurtMarked = true;
        
        // Track this entity for explosion with its data
        trackedEntities.put(
            fallingBlock,
            new ExplodingBlockData(explosionPower, damage, spawnLava, owner, griefing)
        );

    }
    
    /**
     * Ticked
     */
    public static void tickExplodingBlocks(Level level) {
        if (level.isClientSide) return;

        Set<FallingBlockEntity> toRemove = new HashSet<>();

        for (Map.Entry<FallingBlockEntity, ExplodingBlockData> entry : trackedEntities.entrySet()) {
            FallingBlockEntity fallingBlock = entry.getKey();
            ExplodingBlockData data = entry.getValue();

            // Check if entity is removed or dead
            if (!fallingBlock.isAlive()) {
                toRemove.add(fallingBlock);
                continue;
            }

            // Check distance to ground
            double distanceToGround = AbilityUtil.distanceToGround(level, fallingBlock);

            // Explode when close to ground and hasn't exploded yet
            if (distanceToGround <= IMPACT_DISTANCE_THRESHOLD) {
                BlockPos pos = fallingBlock.blockPosition();

                // Get owner entity if exists
                LivingEntity owner = null;
                if (data.ownerUUID != null) {
                    var ownerEntity = ((ServerLevel) level).getEntity(data.ownerUUID);
                    if (ownerEntity instanceof LivingEntity living) {
                        owner = living;
                    }
                }

                // Deal direct damage to nearby entities
                if (data.damage > 0) {
                    AbilityUtil.damageNearbyEntities((ServerLevel) level, owner, 9f, data.damage, pos.getCenter(), true, false);
                }
                // Create explosion
                level.explode(
                        owner,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        data.explosionPower,
                        data.griefing ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE
                );

                // Spawn lava if configured
                if (data.spawnLava) {
                    spawnLavaPool(level, pos, (int)data.explosionPower);
                }

                toRemove.add(fallingBlock);
            }
        }

        // Clean up tracked entities
        toRemove.forEach(trackedEntities::remove);
    }
    
    private static void spawnLavaPool(Level level, BlockPos center, int radius) {
        // Spawn lava in a small area around impact
        int lavaRadius = Math.max(1, radius / 2);
        
        for (int x = -lavaRadius; x <= lavaRadius; x++) {
            for (int z = -lavaRadius; z <= lavaRadius; z++) {
                if (x * x + z * z <= lavaRadius * lavaRadius) {
                    BlockPos lavaPos = center.offset(x, 0, z);
                    BlockState currentState = level.getBlockState(lavaPos);
                    
                    // Only place lava where it's safe (air or replaceable)
                    if (currentState.isAir() || currentState.canBeReplaced()) {
                        level.setBlock(lavaPos, Blocks.LAVA.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
    
    /**
     * Clear all tracked entities (useful for cleanup)
     */
    public static void clearTracking() {
        trackedEntities.clear();
    }
    
    /**
     * Get the number of currently tracked exploding blocks
     */
    public static int getTrackedCount() {
        return trackedEntities.size();
    }
}
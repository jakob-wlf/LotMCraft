package de.jakob.lotm.util.helper;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.CycleOfFateEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * Helper class for spawning and managing Cycle of Fate entities
 */
public class CycleOfFateHelper {

    /**
     * Attempts to spawn a Cycle of Fate entity at the specified position
     * Automatically removes any existing Cycle of Fate owned by the same owner
     *
     * @param level The server level to spawn in
     * @param pos The position to spawn at
     * @param owner The owner of this Cycle of Fate
     * @return The spawned entity, or null if spawn failed (e.g., due to overlap)
     */
    @Nullable
    public static CycleOfFateEntity spawnCycleOfFate(ServerLevel level, BlockPos pos, LivingEntity owner) {
        // Remove any existing Cycle of Fate for this owner
        CycleOfFateEntity existing = findCycleOfFateForOwner(level, owner);
        if (existing != null) {
            existing.discard(); // Remove without triggering
        }

        // Check for overlap
        if (CycleOfFateEntity.wouldOverlap(level, pos)) {
            return null;
        }

        // Create the entity
        CycleOfFateEntity entity = ModEntities.CYCLE_OF_FATE.get().create(level);
        if (entity == null) {
            return null;
        }

        // Set position and owner
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        entity.setOwner(owner);

        // Add to world
        if (level.addFreshEntity(entity)) {
            // Record the area immediately after spawning
            entity.recordArea();
            return entity;
        }

        return null;
    }

    /**
     * Attempts to spawn a Cycle of Fate entity at the owner's current position
     * Automatically removes any existing Cycle of Fate owned by the same owner
     *
     * @param owner The owner of this Cycle of Fate (also determines spawn position)
     * @return The spawned entity, or null if spawn failed
     */
    @Nullable
    public static CycleOfFateEntity spawnCycleOfFateAtOwner(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        return spawnCycleOfFate(serverLevel, owner.blockPosition(), owner);
    }

    /**
     * Finds an existing Cycle of Fate entity owned by the specified entity
     * Returns null if the owner doesn't have a Cycle of Fate active
     *
     * @param level The level to search in
     * @param owner The owner to search for
     * @return The found entity, or null if none exists
     */
    @Nullable
    public static CycleOfFateEntity findCycleOfFateForOwner(ServerLevel level, LivingEntity owner) {
        for (CycleOfFateEntity entity : level.getEntitiesOfClass(CycleOfFateEntity.class,
                owner.getBoundingBox().inflate(200))) {
            if (owner.getUUID().equals(entity.getOwnerUUID())) {
                return entity;
            }
        }
        return null;
    }
}
package de.jakob.lotm.util.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class RegionSnapshot {
    private final Map<BlockPos, BlockState> blockStates = new HashMap<>();
    private final Map<BlockPos, CompoundTag> blockEntityData = new HashMap<>();
    private final Level level;

    public RegionSnapshot(Level level, BlockPos center, int radius) {
        this.level = level;
        // Don't capture anything on creation - we'll capture blocks as they're modified
    }

    /**
     * Capture the current state of a block before modifying it.
     * This should be called BEFORE any block modification.
     */
    public void captureBlock(BlockPos pos) {
        // Only capture if we haven't already captured this position
        if (!blockStates.containsKey(pos)) {
            BlockPos immutablePos = pos.immutable();
            blockStates.put(immutablePos, level.getBlockState(pos));

            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                blockEntityData.put(immutablePos, be.saveWithoutMetadata(level.registryAccess()));
            }
        }
    }

    /**
     * Capture all blocks in a spherical region before modifying them.
     */
    public void captureSphere(BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance <= radius) {
                        BlockPos pos = center.offset(x, y, z);
                        captureBlock(pos);
                    }
                }
            }
        }
    }

    public void restore(Level level) {
        // Remove current block entities first
        blockEntityData.keySet().forEach(pos -> {
            BlockEntity existing = level.getBlockEntity(pos);
            if (existing != null) {
                level.removeBlockEntity(pos);
            }
        });

        // Restore blocks
        blockStates.forEach((pos, state) -> {
            level.setBlock(pos, state, 3); // Flag 3 = update + notify
        });

        // Restore block entities
        blockEntityData.forEach((pos, data) -> {
            BlockState state = level.getBlockState(pos);
            if (state.hasBlockEntity()) {
                BlockEntity be = BlockEntity.loadStatic(pos, state, data, level.registryAccess());
                if (be != null) {
                    level.setBlockEntity(be);
                }
            }
        });
    }
}
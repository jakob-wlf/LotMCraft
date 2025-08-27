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
    private final BlockPos center;
    private final int radius;

    public RegionSnapshot(Level level, BlockPos center, int radius) {
        this.center = center;
        this.radius = radius;
        captureRegion(level);
    }

    private void captureRegion(Level level) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + y*y + z*z <= radius*radius) { // Spherical bounds
                        BlockPos pos = center.offset(x, y, z);
                        blockStates.put(pos, level.getBlockState(pos));

                        BlockEntity be = level.getBlockEntity(pos);
                        if (be != null) {
                            // saveWithoutMetadata requires a HolderLookup.Provider
                            blockEntityData.put(pos, be.saveWithoutMetadata(level.registryAccess()));
                        }
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
                // loadStatic takes 4 parameters: pos, state, tag, and provider
                BlockEntity be = BlockEntity.loadStatic(pos, state, data, level.registryAccess());
                if (be != null) {
                    level.setBlockEntity(be);
                }
            }
        });
    }
}
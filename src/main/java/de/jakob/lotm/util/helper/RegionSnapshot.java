package de.jakob.lotm.util.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.HashMap;
import java.util.Map;

public class RegionSnapshot {
    private final Map<BlockPos, BlockState> blockStates = new HashMap<>();
    private final Map<BlockPos, CompoundTag> blockEntityData = new HashMap<>();
    private final Level level;

    public RegionSnapshot(Level level, BlockPos center, int radius) {
        this.level = level;
    }


    public void captureBlock(BlockPos pos) {
        if (!blockStates.containsKey(pos)) {
            BlockPos immutablePos = pos.immutable();
            blockStates.put(immutablePos, level.getBlockState(pos));

            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                blockEntityData.put(immutablePos, be.saveWithoutMetadata(level.registryAccess()));
            }
        }
    }

    public void captureSphere(Level level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance <= radius) {
                        BlockPos pos = center.offset(x, y, z);
                        if(level.getBlockEntity(pos) != null) continue;
                        captureBlock(pos);
                    }
                }
            }
        }
    }

    public void restore(Level level) {
        blockEntityData.keySet().forEach(pos -> {
            BlockEntity existing = level.getBlockEntity(pos);
            if (existing != null) {
                level.removeBlockEntity(pos);
            }
        });


        blockStates.forEach((pos, state) -> {
            level.setBlock(pos, state, 3);
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
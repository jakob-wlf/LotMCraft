package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.RiverOfEternalDarknessData;
import de.jakob.lotm.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class RiverWaterConversionHandler {
    private RiverWaterConversionHandler() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY)) {
            return;
        }

        ChunkAccess chunk = event.getChunk();
        RiverOfEternalDarknessData data = RiverOfEternalDarknessData.get(level.getServer());
        long chunkPosKey = chunk.getPos().toLong();
        if (data.isWaterChunkConverted(chunkPosKey)) {
            return;
        }

        BlockState riverWater = ModFluids.DROPS_OF_ETERNAL_DARKNESS_SOURCE.get()
                .defaultFluidState().createLegacyBlock();
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = false;

        for (int y = chunk.getMinBuildHeight(); y < chunk.getMaxBuildHeight(); y++) {
            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    pos.set(chunkPos.getMinBlockX() + localX, y, chunkPos.getMinBlockZ() + localZ);
                    if (chunk.getBlockState(pos).is(Blocks.WATER)) {
                        chunk.setBlockState(pos, riverWater, false);
                        changed = true;
                    }
                }
            }
        }

        data.markWaterChunkConverted(chunkPosKey);
        chunk.setUnsaved(true);
        if (changed) {
            level.getChunkSource().getLightEngine().checkBlock(
                    new BlockPos(chunkPos.getMiddleBlockX(), level.getSeaLevel(), chunkPos.getMiddleBlockZ()));
        }
    }
}
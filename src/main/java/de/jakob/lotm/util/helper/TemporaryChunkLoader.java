package de.jakob.lotm.util.helper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class TemporaryChunkLoader {

    private static final String MODID = "lotmcraft";

    /**
     * Keeps the chunks around a position loaded temporarily.
     *
     * @param level         The level (e.g., End)
     * @param centerX       World X position
     * @param centerZ       World Z position
     * @param radius        Radius in chunks (1 = 3×3 area)
     * @param durationTicks How long to keep them loaded (e.g., 6000 = 5 minutes)
     */
    public static void forceChunksTemporarily(ServerLevel level, double centerX, double centerZ, int radius, int durationTicks) {
        ChunkPos centerChunk = new ChunkPos((int) centerX >> 4, (int) centerZ >> 4);

        // ✅ Force-load the area
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos pos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                level.setChunkForced(pos.x, pos.z, true);
            }
        }
    }
}

package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class SpaceTimeLabyrinthChunkGenerator extends ChunkGenerator {

    public static final MapCodec<SpaceTimeLabyrinthChunkGenerator> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source")
                            .forGetter(SpaceTimeLabyrinthChunkGenerator::getBiomeSource)
            ).apply(instance, SpaceTimeLabyrinthChunkGenerator::new));

    private static final int CORRIDOR_WIDTH  = 2;
    private static final int CELL_SIZE       = CORRIDOR_WIDTH + 1; // 3

    private static final int MAZE_FLOOR = 10;
    private static final int MAZE_CEIL  = 246;

    private static final int PORTAL_RARITY = 600;

    private static final long DIMENSION_SALT = 0xC0FFEE_DEADBEAFL;

    public SpaceTimeLabyrinthChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }


    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
                             RandomState random, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() { return 256; }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunk) {

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        long chunkSeed = mixSeed(chunkX, chunkZ);

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int worldX = chunkX * 16 + lx;
                int worldZ = chunkZ * 16 + lz;

                chunk.setBlockState(new BlockPos(lx, MAZE_FLOOR - 1, lz),
                        Blocks.BEDROCK.defaultBlockState(), false);
                chunk.setBlockState(new BlockPos(lx, MAZE_CEIL + 1, lz),
                        Blocks.BEDROCK.defaultBlockState(), false);

                for (int y = MAZE_FLOOR; y <= MAZE_CEIL; y++) {
                    BlockState state = computeBlock(worldX, y, worldZ, chunkSeed ^ (long) y * 0x9E3779B97F4A7C15L);
                    if (state != null) {
                        chunk.setBlockState(new BlockPos(lx, y, lz), state, false);
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 64;
    }

    @Override
    public int getMinY() {
        return 0;
    }


    private BlockState computeBlock(int x, int y, int z, long chunkSeed) {
        if (isMazeWall(x, y, z)) {
            return wallBlock(x, y, z);
        }

        // Passage – maybe place a Reality Portal
        if (isPortalCandidate(x, y, z)) {
            return ModBlocks.REALITY_PORTAL.get().defaultBlockState();
        }

        return null; // air passage
    }

    /**
     * Deterministically decides whether a world position is a wall cell.
     *
     * The maze is built on a regular grid.  Each cell is CELL_SIZE × CELL_SIZE
     * horizontally and CELL_SIZE vertically.  Within a cell:
     *  - The "last slice" in each axis (index == CELL_SIZE-1) is always wall.
     *  - The inner region is passage.
     *
     * This creates a grid of rectangular rooms connected by corridors once we
     * carve openings: we open a wall between two adjacent rooms if a
     * deterministic hash of the shared wall coordinate is below a threshold.
     * The result is a maze-like structure without requiring any flood-fill or
     * graph traversal at generation time.
     *
     * CODE BY CLAUDE!!
     */
    private boolean isMazeWall(int x, int y, int z) {
        // Position within the cell
        int cx = Math.floorMod(x, CELL_SIZE);
        int cy = Math.floorMod(y, CELL_SIZE);
        int cz = Math.floorMod(z, CELL_SIZE);

        // "Border" positions within a cell
        boolean xBorder = (cx == CELL_SIZE - 1);
        boolean yBorder = (cy == CELL_SIZE - 1);
        boolean zBorder = (cz == CELL_SIZE - 1);

        int borders = (xBorder ? 1 : 0) + (yBorder ? 1 : 0) + (zBorder ? 1 : 0);

        // Corners and edges (2+ borders) are always solid
        if (borders >= 2) return true;

        // Single-border faces: carve an opening based on hash
        if (borders == 1) {
            // Which wall are we on?
            int wallX = Math.floorDiv(x, CELL_SIZE) + (xBorder ? 1 : 0);
            int wallY = Math.floorDiv(y, CELL_SIZE) + (yBorder ? 1 : 0);
            int wallZ = Math.floorDiv(z, CELL_SIZE) + (zBorder ? 1 : 0);

            long h = hash3(wallX, wallY, wallZ);
            // ~55 % of walls are kept solid to form the maze structure
            return (h & 0xFFFFL) < 36044; // 36044/65536 ≈ 55 %
        }

        // Interior of a cell is always passage
        return false;
    }

    private BlockState wallBlock(int x, int y, int z) {
        long h = hash3(x, y, z) & 0xFFFFL; // 0–65535
        if (h < 3276)  return Blocks.BARRIER.defaultBlockState();          //  5 %
        if (h < 9830)  return Blocks.BEDROCK.defaultBlockState();          // 10 %
        if (h < 26214) return Blocks.OBSIDIAN.defaultBlockState();         // 25 %
        return ModBlocks.SOLID_VOID.get().defaultBlockState();             // 60 %
    }

    private boolean isPortalCandidate(int x, int y, int z) {
        long h = hash3(x ^ 0xDEAD, y ^ 0xBEEF, z ^ 0xCAFE);
        return (h & 0xFFFFL) < (65536 / PORTAL_RARITY);
    }

    private static long hash3(int x, int y, int z) {
        long h = DIMENSION_SALT;
        h ^= (long) x * 0x9E3779B97F4A7C15L;
        h = Long.rotateLeft(h, 17);
        h ^= (long) y * 0x6C62272E07BB0142L;
        h = Long.rotateLeft(h, 31);
        h ^= (long) z * 0x94D049BB133111EBL;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return h;
    }

    private static long mixSeed(int chunkX, int chunkZ) {
        return hash3(chunkX, 0, chunkZ);
    }


    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type,
                             LevelHeightAccessor level, RandomState random) {
        return MAZE_FLOOR;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level,
                                     RandomState random) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("SpaceTimeLabyrinth Maze Generator");
    }
}

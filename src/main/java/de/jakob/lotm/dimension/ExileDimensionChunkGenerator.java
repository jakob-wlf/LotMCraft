package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
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

public class ExileDimensionChunkGenerator extends ChunkGenerator {

    public static final MapCodec<ExileDimensionChunkGenerator> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, ExileDimensionChunkGenerator::new));

    private static final BlockState[] SPHERE_BLOCKS = {
            Blocks.OBSIDIAN.defaultBlockState(),
            Blocks.END_STONE.defaultBlockState(),
            Blocks.CRYING_OBSIDIAN.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.BASALT.defaultBlockState(),
    };

    private static final BlockState[] ISLAND_SURFACE_BLOCKS = {
            Blocks.END_STONE.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.OBSIDIAN.defaultBlockState(),
    };

    private static final BlockState[] ISLAND_FILL_BLOCKS = {
            Blocks.END_STONE.defaultBlockState(),
            Blocks.STONE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState(),
    };

    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();

    public ExileDimensionChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }


    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunk) {

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        generateSpheresForChunk(chunk, chunkX, chunkZ);
        generateFloatingIslandsForChunk(chunk, chunkX, chunkZ);

        return CompletableFuture.completedFuture(chunk);
    }

    // -----------------------------------------------------------------------
    // Sphere generation
    // -----------------------------------------------------------------------

    private void generateSpheresForChunk(ChunkAccess chunk, int chunkX, int chunkZ) {
        // Check a radius of neighbouring "sphere slots" so spheres near chunk edges are included
        int searchRadius = 3;

        for (int sx = chunkX - searchRadius; sx <= chunkX + searchRadius; sx++) {
            for (int sz = chunkZ - searchRadius; sz <= chunkZ + searchRadius; sz++) {
                long seed = hashCoords(sx, sz, 0x4E6F697365L);
                Random rng = new Random(seed);

                // ~35 % chance a sphere spawns in this "slot"
                if (rng.nextInt(100) >= 35) continue;

                int radius     = 3 + rng.nextInt(7);           // 3–9 blocks
                int centerX    = (sx << 4) + rng.nextInt(16);
                int centerY    = 40 + rng.nextInt(160);        // y 40–199
                int centerZ    = (sz << 4) + rng.nextInt(16);
                BlockState fill = SPHERE_BLOCKS[rng.nextInt(SPHERE_BLOCKS.length)];
                boolean hollow  = rng.nextBoolean();
                boolean hasLava = !hollow && rng.nextInt(5) == 0;

                placeSphere(chunk, centerX, centerY, centerZ, radius, fill, hollow, hasLava, rng);
            }
        }
    }

    private void placeSphere(ChunkAccess chunk, int cx, int cy, int cz,
                             int radius, BlockState fill,
                             boolean hollow, boolean hasLava, Random rng) {
        int chunkBaseX = chunk.getPos().getMinBlockX();
        int chunkBaseZ = chunk.getPos().getMinBlockZ();
        int shellThickness = Math.max(1, radius / 4);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int worldX = cx + dx;
                int worldZ = cz + dz;
                int localX = worldX - chunkBaseX;
                int localZ = worldZ - chunkBaseZ;
                if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16) continue;

                for (int dy = -radius; dy <= radius; dy++) {
                    int worldY = cy + dy;
                    if (worldY < chunk.getMinBuildHeight() || worldY >= chunk.getMaxBuildHeight()) continue;

                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > radius) continue;

                    if (hollow && dist < radius - shellThickness) {
                        // inside hollow sphere — optionally place lava at bottom
                        if (hasLava && dy < -radius + shellThickness + 1 && rng.nextInt(3) != 0) {
                            chunk.setBlockState(new BlockPos(localX, worldY, localZ), LAVA, false);
                        }
                        continue;
                    }

                    chunk.setBlockState(new BlockPos(localX, worldY, localZ), fill, false);
                }
            }
        }
    }


    private void generateFloatingIslandsForChunk(ChunkAccess chunk, int chunkX, int chunkZ) {
        int searchRadius = 4;

        for (int ix = chunkX - searchRadius; ix <= chunkX + searchRadius; ix++) {
            for (int iz = chunkZ - searchRadius; iz <= chunkZ + searchRadius; iz++) {
                long seed = hashCoords(ix, iz, 0xF10A71D5L);
                Random rng = new Random(seed);

                // ~20 % chance an island spawns in this slot
                if (rng.nextInt(100) >= 20) continue;

                int halfW    = 8  + rng.nextInt(20);  // half-width  8–27
                int halfL    = 8  + rng.nextInt(20);  // half-length 8–27
                int depth    = 4  + rng.nextInt(12);  // underbelly depth 4–15
                int centerX  = (ix << 4) + rng.nextInt(16);
                int centerY  = 80 + rng.nextInt(120); // y 80–199
                int centerZ  = (iz << 4) + rng.nextInt(16);
                int surfaceBlkIdx = rng.nextInt(ISLAND_SURFACE_BLOCKS.length);
                int fillBlkIdx    = rng.nextInt(ISLAND_FILL_BLOCKS.length);

                placeFloatingIsland(chunk, centerX, centerY, centerZ,
                        halfW, halfL, depth,
                        ISLAND_SURFACE_BLOCKS[surfaceBlkIdx],
                        ISLAND_FILL_BLOCKS[fillBlkIdx], rng);
            }
        }
    }

    private void placeFloatingIsland(ChunkAccess chunk,
                                     int cx, int cy, int cz,
                                     int halfW, int halfL, int depth,
                                     BlockState surfaceBlock, BlockState fillBlock,
                                     Random rng) {
        int chunkBaseX = chunk.getPos().getMinBlockX();
        int chunkBaseZ = chunk.getPos().getMinBlockZ();

        int islandThickness = 4 + rng.nextInt(5); // top slab thickness 4–8

        for (int dx = -halfW; dx <= halfW; dx++) {
            for (int dz = -halfL; dz <= halfL; dz++) {
                int worldX = cx + dx;
                int worldZ = cz + dz;
                int localX = worldX - chunkBaseX;
                int localZ = worldZ - chunkBaseZ;
                if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16) continue;

                // Horizontal distance normalised to [0,1]
                double hDist = Math.sqrt(
                        ((double) dx * dx) / (halfW * halfW) +
                        ((double) dz * dz) / (halfL * halfL));
                if (hDist > 1.0) continue;

                // Top slab: flat top with slight noise
                int noiseOff = (int) (rng.nextGaussian() * 1.2);
                for (int dy = 0; dy <= islandThickness; dy++) {
                    int worldY = cy + dy + noiseOff;
                    if (worldY < chunk.getMinBuildHeight() || worldY >= chunk.getMaxBuildHeight()) continue;
                    BlockState bs = (dy == islandThickness) ? surfaceBlock : fillBlock;
                    chunk.setBlockState(new BlockPos(localX, worldY, localZ), bs, false);
                }

                // Underbelly: tapers downward based on horizontal distance
                int maxDown = (int) (depth * (1.0 - hDist * hDist));
                for (int dy = -1; dy >= -maxDown; dy--) {
                    int worldY = cy + dy + noiseOff;
                    if (worldY < chunk.getMinBuildHeight() || worldY >= chunk.getMaxBuildHeight()) continue;
                    chunk.setBlockState(new BlockPos(localX, worldY, localZ), fillBlock, false);
                }
            }
        }
    }

    private long hashCoords(int x, int z, long salt) {
        long v = (long) x * 341873128712L + (long) z * 132897987541L + salt;
        v ^= v >>> 33;
        v *= 0xff51afd7ed558ccdL;
        v ^= v >>> 33;
        return v;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState,
                              BiomeManager biomeManager, StructureManager structureManager,
                              ChunkAccess chunk, GenerationStep.Carving step) {}

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
                              RandomState randomState, ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {}

    @Override
    public int getGenDepth() { return 384; }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() { return -63; }

    @Override
    public int getMinY() { return -64; }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type,
                              LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z,
                                      LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {}
}
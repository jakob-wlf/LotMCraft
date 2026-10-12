package de.jakob.lotm.dimension.abyss;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AbyssChunkGenerator extends ChunkGenerator {

    public static final MapCodec<AbyssChunkGenerator> CODEC =RecordCodecBuilder.mapCodec(instance -> instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)).apply(instance, AbyssChunkGenerator::new));

    private static final int OCEAN_LEVEL = 80;
    private static final int MIN_Y = -64;
    private static final int MAX_TERRAIN_HEIGHT = 350;

    private static final double PEAK_SCALE = 0.013;       // bigger = smaller islands
    private static final double BASE_THRESHOLD = 0.16;    // higher = fewer / smaller islands
    private static final double FULL_EXCESS = 0.20;       // noise strength above threshold that counts as "fully peaked"
    private static final double PEAK_HEIGHT = 140.0;      // height of a full peak above the water
    private static final double SLOPE_SHAPE = 1.5;        // higher = flatter shores, steeper peak
    private static final double RIDGE_HEIGHT = 35.0;      // sharp ridgelines running along the mountain
    private static final double CRAG_HEIGHT = 14.0;       // rough bumps on the faces
    private static final double WARP_SCALE = 0.01;        // size of the wobble in island outlines
    private static final double WARP_AMOUNT = 25.0;       // how wobbly the outlines are

    private static final double ROOT_DEPTH = 80.0;        // how far below the water the deepest part reaches (80 -> about Y=0, deepslate level)
    private static final double BOTTOM_ROUGHNESS = 10.0;  // how uneven the underside is
    //NOISE
    private final PerlinSimplexNoise peakNoise;
    private final PerlinSimplexNoise warpXNoise;
    private final PerlinSimplexNoise warpZNoise;
    private final PerlinSimplexNoise detailNoise;
    private final PerlinSimplexNoise ridgeNoise;

    public AbyssChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
        this.peakNoise = new PerlinSimplexNoise(RandomSource.create(918273L), List.of(-4, -3, -2, -1, 0));
        this.warpXNoise = new PerlinSimplexNoise(RandomSource.create(293847L), List.of(-2, -1, 0, 1));
        this.warpZNoise = new PerlinSimplexNoise(RandomSource.create(293848L), List.of(-2, -1, 0, 1));
        this.detailNoise = new PerlinSimplexNoise(RandomSource.create(481923L), List.of(0, 1, 2, 3));
        this.ridgeNoise = new PerlinSimplexNoise(RandomSource.create(731942L), List.of(-1, 0, 1, 2));
    }
    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {return CODEC;}
    //SHAPE
    private int[] rockColumn(int x, int z) {
        double wx = x + warpXNoise.getValue(x * WARP_SCALE, z * WARP_SCALE, false) * WARP_AMOUNT;
        double wz = z + warpZNoise.getValue(x * WARP_SCALE, z * WARP_SCALE, false) * WARP_AMOUNT;
        double n = peakNoise.getValue(wx * PEAK_SCALE, wz * PEAK_SCALE, false);
        double excess = n - BASE_THRESHOLD;
        if (excess <= 0) { return null; }
        // 0 at the shoreline, 1 at the heart of the island.
        double t = Math.min(1.0, excess / FULL_EXCESS);-
        double height = Math.pow(t, SLOPE_SHAPE) * PEAK_HEIGHT;
        double ridge = 1.0 - Math.abs(ridgeNoise.getValue(x * 0.03, z * 0.03, false)) * 2.0;
        height += Math.max(0.0, ridge) * RIDGE_HEIGHT * t;
        double crag = detailNoise.getValue(x * 0.06, z * 0.06, false) * CRAG_HEIGHT + detailNoise.getValue(x * 0.18, z * 0.18, false) * CRAG_HEIGHT * 0.4;
        height += crag * t;
        int top = Math.min(OCEAN_LEVEL + 2 + (int) Math.max(0.0, height), MAX_TERRAIN_HEIGHT);
        double bowl = Math.sqrt(Math.max(0.0, 2.0 * t - t * t));
        double depth = 3.0 + bowl * ROOT_DEPTH;
        depth += detailNoise.getValue(x * 0.08, z * 0.08, false) * BOTTOM_ROUGHNESS * t;
        int bottom = OCEAN_LEVEL - (int) Math.max(3.0, depth);
        return new int[]{bottom, top};
    }
    //TERRAIN GENERATION
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {return CompletableFuture.completedFuture(generateTerrain(chunk));}
    private ChunkAccess generateTerrain(ChunkAccess chunk) {
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = chunk.getPos().x * 16 + localX;
                int worldZ = chunk.getPos().z * 16 + localZ;
                int[] rock = rockColumn(worldX, worldZ);
                for (int y = minY; y < maxY; y++) {
                    BlockState state;
                    if (rock != null && y >= rock[0] && y <= rock[1]) {
                        state = getTowerBlock(worldX, y, worldZ);
                    } else if (y <= OCEAN_LEVEL) {
                        state = water;
                    } else {
                        state = air;
                    }
                    pos.set(worldX, y, worldZ);
                    chunk.setBlockState(pos, state, false);
                }
            }
        }
        return chunk;
    }
    //MATERIAL
    private BlockState getTowerBlock(int x, int y, int z) {
        long h = x * 3129871L ^ z * 116129781L ^ y * 42317861L;
        h = h * h * 42317861L + h * 11L;
        h ^= (h >>> 29);
        h *= 0xBF58476D1CE4E5B9L;
        h ^= (h >>> 32);
        int roll = (int) ((h >>> 1) % 100);
        if (roll < 32) return Blocks.BLACKSTONE.defaultBlockState();
        if (roll < 56) return Blocks.BASALT.defaultBlockState();
        if (roll < 78) return Blocks.DEEPSLATE.defaultBlockState();
        if (roll < 88) return Blocks.OBSIDIAN.defaultBlockState();
        if (roll < 96) return Blocks.POLISHED_BASALT.defaultBlockState();
        return Blocks.CRYING_OBSIDIAN.defaultBlockState();
    }
    //SURFACE / CARVERS / MOBS

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
    }
    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving carving) {
    }
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType, LevelHeightAccessor level, RandomState randomState) {
        int[] rock = rockColumn(x, z);
        return (rock != null && rock[1] > OCEAN_LEVEL) ? rock[1] + 1 : OCEAN_LEVEL + 1;
    }
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        int[] rock = rockColumn(x, z);
        BlockState[] states = new BlockState[maxY - minY];
        for (int y = minY; y < maxY; y++) {
            BlockState state;
            if (rock != null && y >= rock[0] && y <= rock[1]) {
                state = getTowerBlock(x, y, z);
            } else if (y <= OCEAN_LEVEL) {
                state = Blocks.WATER.defaultBlockState();
            } else {
                state = Blocks.AIR.defaultBlockState();
            }
            states[y - minY] = state;
        }
        return new NoiseColumn(minY, states);
    }
    @Override
    public int getMinY() {return MIN_Y;}
    @Override
    public int getGenDepth() {return 384;}
    @Override
    public int getSeaLevel() {return OCEAN_LEVEL;}
}
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
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ConcealmentWorldChunkGenerator extends ChunkGenerator {

    public static final MapCodec<ConcealmentWorldChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, ConcealmentWorldChunkGenerator::new)
    );

    private final PerlinSimplexNoise surfaceNoise;
    private final PerlinSimplexNoise patchNoise;

    public ConcealmentWorldChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
        // Initialize noise generators with fixed seeds for consistency
        RandomSource randomSource1 = RandomSource.create(12345L);
        RandomSource randomSource2 = RandomSource.create(67890L);
        this.surfaceNoise = new PerlinSimplexNoise(randomSource1, List.of(0, 1, 2, 3));
        this.patchNoise = new PerlinSimplexNoise(randomSource2, List.of(0, 1));
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
            ChunkAccess chunk
    ) {
        return CompletableFuture.completedFuture(this.generateTerrain(chunk));
    }

    private ChunkAccess generateTerrain(ChunkAccess chunk) {
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunk.getPos().x * 16 + x;
                int worldZ = chunk.getPos().z * 16 + z;

                // Generate very flat terrain with subtle height variations
                double noise = this.surfaceNoise.getValue(worldX * 0.01, worldZ * 0.01, false) * 2; // Much smaller scale and amplitude

                int baseHeight = 64; // Base terrain height
                int terrainHeight = (int) (baseHeight + noise);

                // Clamp height
                terrainHeight = Math.max(minY, Math.min(maxY - 1, terrainHeight));

                // Determine surface block type using patch noise
                double patchValue = this.patchNoise.getValue(worldX * 0.05, worldZ * 0.05, false);
                BlockState surfaceBlock = patchValue > 0 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.END_STONE.defaultBlockState();

                // Fill blocks
                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState state;

                    if (y < terrainHeight - 3) {
                        // Deep underground - endstone
                        state = Blocks.END_STONE.defaultBlockState();
                    } else if (y < terrainHeight) {
                        // Near surface - still endstone
                        state = Blocks.END_STONE.defaultBlockState();
                    } else if (y == terrainHeight) {
                        // Surface - grass block or end stone patches
                        state = surfaceBlock;
                    } else {
                        // Air above surface
                        state = Blocks.AIR.defaultBlockState();
                    }

                    chunk.setBlockState(pos, state, false);
                }
            }
        }

        return chunk;
    }

    @Override
    public void buildSurface(
            WorldGenRegion region,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunk
    ) {
        // Surface building is already handled in fillFromNoise
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType, LevelHeightAccessor level, RandomState randomState) {
        double noise = this.surfaceNoise.getValue(x * 0.01, z * 0.01, false) * 2;
        int baseHeight = 64;
        return (int) (baseHeight + noise);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        int height = getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        BlockState[] states = new BlockState[maxY - minY];
        for (int y = minY; y < maxY; y++) {
            if (y < height - 3) {
                states[y - minY] = Blocks.END_STONE.defaultBlockState();
            } else if (y < height) {
                states[y - minY] = Blocks.END_STONE.defaultBlockState();
            } else if (y == height) {
                states[y - minY] = Blocks.GRASS_BLOCK.defaultBlockState();
            } else {
                states[y - minY] = Blocks.AIR.defaultBlockState();
            }
        }

        return new NoiseColumn(minY, states);
    }

    @Override
    public void applyCarvers(
            WorldGenRegion level,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving carving
    ) {
        // No caves or carvers in concealment world
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        // No mob spawning
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
        list.add("Concealment World Generator");
        int height = getBaseHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, null, randomState);
        list.add("Terrain Height: " + height);
    }
}
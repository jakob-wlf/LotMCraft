package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.fluid.ModFluids;
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

public final class UnderworldChunkGenerator extends ChunkGenerator {
    public static final MapCodec<UnderworldChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(BiomeSource.CODEC.fieldOf("biome_source")
                    .forGetter(ChunkGenerator::getBiomeSource))
                    .apply(instance, UnderworldChunkGenerator::new));

    private static final int BASE_HEIGHT = 48;
    private static final int WATER_LEVEL = 47;
    private final PerlinSimplexNoise terrainNoise = new PerlinSimplexNoise(
            RandomSource.create(0x554E444552574F52L), List.of(-2, -1, 0, 1));
    private final PerlinSimplexNoise riverNoise = new PerlinSimplexNoise(
            RandomSource.create(0x5354595852495645L), List.of(-1, 0, 1));

    public UnderworldChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        int minY = chunk.getMinBuildHeight();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = chunk.getPos().getMinBlockX() + localX;
                int worldZ = chunk.getPos().getMinBlockZ() + localZ;
                int height = terrainHeight(worldX, worldZ);
                boolean river = isRiver(worldX, worldZ);
                boolean eternalFork = UnderworldBiomeSource.isEternalDarknessFork(worldX, worldZ);
                int groundHeight = river ? Math.min(height, WATER_LEVEL - 3) : height;

                for (int y = minY; y <= Math.max(groundHeight, WATER_LEVEL); y++) {
                    BlockState state;
                    if (y == minY) {
                        state = Blocks.BEDROCK.defaultBlockState();
                    } else if (y < groundHeight - 3) {
                        state = Blocks.DEEPSLATE.defaultBlockState();
                    } else if (y <= groundHeight) {
                        if (eternalFork && isRiverBank(worldX, worldZ)) {
                            state = y == groundHeight
                                ? Blocks.SOUL_SAND.defaultBlockState()
                                : Blocks.SOUL_SOIL.defaultBlockState();
                        } else {
                            state = y == groundHeight
                                ? Blocks.BLACKSTONE.defaultBlockState()
                                : Blocks.BASALT.defaultBlockState();
                        }
                    } else if (river && y <= WATER_LEVEL) {
                        state = (eternalFork
                            ? ModFluids.DROPS_OF_ETERNAL_DARKNESS_SOURCE.get()
                            : ModFluids.WATER_OF_THE_RIVER_STYX_SOURCE.get())
                                .defaultFluidState().createLegacyBlock();
                    } else {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    chunk.setBlockState(new BlockPos(worldX, y, worldZ), state, false);
                }
            }
        }
        Heightmap.primeHeightmaps(chunk, java.util.EnumSet.of(
                Heightmap.Types.MOTION_BLOCKING,
                Heightmap.Types.WORLD_SURFACE,
                Heightmap.Types.OCEAN_FLOOR));
        return CompletableFuture.completedFuture(chunk);
    }

    private int terrainHeight(int x, int z) {
        double broad = terrainNoise.getValue(x * 0.006, z * 0.006, false) * 10.0;
        double detail = terrainNoise.getValue(x * 0.025, z * 0.025, false) * 3.0;
        return BASE_HEIGHT + (int) Math.round(broad + detail);
    }

    private boolean isRiver(int x, int z) {
        return Math.abs(riverNoise.getValue(x * 0.0045, z * 0.0045, false)) < 0.055;
    }

    private boolean isRiverBank(int x, int z) {
        double riverValue = Math.abs(riverNoise.getValue(x * 0.0045, z * 0.0045, false));
        return riverValue >= 0.055 && riverValue < 0.10;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type,
                             LevelHeightAccessor level, RandomState randomState) {
        return isRiver(x, z) ? WATER_LEVEL + 1 : terrainHeight(x, z) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        BlockState[] states = new BlockState[maxY - minY];
        int height = terrainHeight(x, z);
        boolean river = isRiver(x, z);
        boolean eternalFork = UnderworldBiomeSource.isEternalDarknessFork(x, z);
        int groundHeight = river ? Math.min(height, WATER_LEVEL - 3) : height;
        for (int y = minY; y < maxY; y++) {
            if (y == minY) states[y - minY] = Blocks.BEDROCK.defaultBlockState();
                else if (y <= groundHeight) {
                if (eternalFork && isRiverBank(x, z)) {
                    states[y - minY] = y == groundHeight
                        ? Blocks.SOUL_SAND.defaultBlockState() : Blocks.SOUL_SOIL.defaultBlockState();
                } else {
                    states[y - minY] = y == groundHeight
                        ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState();
                }
                }
                else if (river && y <= WATER_LEVEL) states[y - minY] = (eternalFork
                    ? ModFluids.DROPS_OF_ETERNAL_DARKNESS_SOURCE.get()
                    : ModFluids.WATER_OF_THE_RIVER_STYX_SOURCE.get())
                    .defaultFluidState().createLegacyBlock();
            else states[y - minY] = Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk, GenerationStep.Carving carving) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
                             RandomState randomState, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
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
        return WATER_LEVEL;
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Underworld Generator");
    }
}
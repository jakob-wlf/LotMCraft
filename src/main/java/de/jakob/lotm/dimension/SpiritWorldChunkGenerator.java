package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
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
import java.util.concurrent.CompletableFuture;

public class SpiritWorldChunkGenerator extends ChunkGenerator {
    public static final MapCodec<SpiritWorldChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, SpiritWorldChunkGenerator::new)
    );

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();

    // Material patch types
    private enum PatchType {
        // Fantastical/magical blocks (high frequency)
        WOOL(new BlockState[]{
                Blocks.RED_WOOL.defaultBlockState(),
                Blocks.ORANGE_WOOL.defaultBlockState(),
                Blocks.YELLOW_WOOL.defaultBlockState(),
                Blocks.LIME_WOOL.defaultBlockState(),
                Blocks.GREEN_WOOL.defaultBlockState(),
                Blocks.CYAN_WOOL.defaultBlockState(),
                Blocks.LIGHT_BLUE_WOOL.defaultBlockState(),
                Blocks.BLUE_WOOL.defaultBlockState(),
                Blocks.PURPLE_WOOL.defaultBlockState(),
                Blocks.MAGENTA_WOOL.defaultBlockState(),
                Blocks.PINK_WOOL.defaultBlockState(),
                Blocks.WHITE_WOOL.defaultBlockState()
        }, 0.26), // 20% spawn weight
        AMETHYST(new BlockState[]{
                Blocks.AMETHYST_BLOCK.defaultBlockState(),
                Blocks.BUDDING_AMETHYST.defaultBlockState(),
                Blocks.CALCITE.defaultBlockState(),
                Blocks.PURPLE_GLAZED_TERRACOTTA.defaultBlockState()
        }, 0.18),
        PRISMARINE(new BlockState[]{
                Blocks.PRISMARINE.defaultBlockState(),
                Blocks.PRISMARINE_BRICKS.defaultBlockState(),
                Blocks.DARK_PRISMARINE.defaultBlockState(),
                Blocks.SEA_LANTERN.defaultBlockState()
        }, 0.15),
        END_STONE(new BlockState[]{
                Blocks.END_STONE.defaultBlockState(),
                Blocks.END_STONE_BRICKS.defaultBlockState(),
                Blocks.PURPUR_BLOCK.defaultBlockState(),
                Blocks.PURPUR_PILLAR.defaultBlockState()
        }, 0.15),
        QUARTZ(new BlockState[]{
                Blocks.QUARTZ_BLOCK.defaultBlockState(),
                Blocks.QUARTZ_BRICKS.defaultBlockState(),
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),
                Blocks.QUARTZ_PILLAR.defaultBlockState()
        }, 0.12),
        TERRACOTTA(new BlockState[]{
                Blocks.RED_TERRACOTTA.defaultBlockState(),
                Blocks.ORANGE_TERRACOTTA.defaultBlockState(),
                Blocks.YELLOW_TERRACOTTA.defaultBlockState(),
                Blocks.CYAN_TERRACOTTA.defaultBlockState(),
                Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState(),
                Blocks.MAGENTA_TERRACOTTA.defaultBlockState(),
                Blocks.PINK_TERRACOTTA.defaultBlockState()
        }, 0.15),

        // Nether blocks (medium frequency)
        NETHERRACK(new BlockState[]{
                Blocks.NETHERRACK.defaultBlockState(),
                Blocks.NETHER_BRICKS.defaultBlockState(),
                Blocks.RED_NETHER_BRICKS.defaultBlockState(),
                Blocks.CRIMSON_NYLIUM.defaultBlockState(),
                Blocks.WARPED_NYLIUM.defaultBlockState()
        }, 0.075),
        BLACKSTONE(new BlockState[]{
                Blocks.BLACKSTONE.defaultBlockState(),
                Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                Blocks.GILDED_BLACKSTONE.defaultBlockState(),
                Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
        }, 0.05),

        // Grounded blocks (low frequency)
        BASALT(new BlockState[]{
                Blocks.BASALT.defaultBlockState(),
                Blocks.SMOOTH_BASALT.defaultBlockState(),
                Blocks.POLISHED_BASALT.defaultBlockState()
        }, 0.01),
        DEEPSLATE(new BlockState[]{
                Blocks.DEEPSLATE.defaultBlockState(),
                Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                Blocks.DEEPSLATE_TILES.defaultBlockState()
        }, 0.005),
        STONE(new BlockState[]{
                Blocks.STONE.defaultBlockState(),
                Blocks.ANDESITE.defaultBlockState(),
                Blocks.DIORITE.defaultBlockState()
        }, 0.003),
        GRASS(new BlockState[]{
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState()
        }, 0.14);

        private final BlockState[] blocks;
        private final double weight;

        PatchType(BlockState[] blocks, double weight) {
            this.blocks = blocks;
            this.weight = weight;
        }

        public BlockState getBlock(RandomSource random) {
            return blocks[random.nextInt(blocks.length)];
        }

        public BlockState[] getBlocks() {
            return blocks;
        }

        public double getWeight() {
            return weight;
        }
    }

    // Configuration for patch spawn rates - ADJUST THESE VALUES TO CUSTOMIZE!
    // Higher values = more common, lower values = more rare
    // Total doesn't need to equal 1.0, weights are relative
    private static final class PatchConfig {
        // Fantastical blocks (main feature)
        static final double WOOL_WEIGHT = 0.20;        // Colorful wool patches
        static final double AMETHYST_WEIGHT = 0.18;    // Crystal/amethyst patches
        static final double PRISMARINE_WEIGHT = 0.15;  // Ocean temple blocks
        static final double END_STONE_WEIGHT = 0.15;   // End dimension blocks
        static final double QUARTZ_WEIGHT = 0.12;      // White quartz blocks
        static final double TERRACOTTA_WEIGHT = 0.10;  // Colorful terracotta

        // Nether blocks (medium presence)
        static final double NETHERRACK_WEIGHT = 0.05;  // Nether blocks
        static final double BLACKSTONE_WEIGHT = 0.03;  // Dark nether stone

        // Grounded blocks (rare, background)
        static final double BASALT_WEIGHT = 0.01;      // Dark stone
        static final double DEEPSLATE_WEIGHT = 0.005;  // Underground stone
        static final double STONE_WEIGHT = 0.003;      // Regular stone
        static final double GRASS_WEIGHT = 0.002;      // Natural grass/dirt
    }

    // Enhanced island generation parameters
    private static final int MAIN_ISLAND_RADIUS = 100;
    private static final int SMALL_ISLAND_MIN_RADIUS = 10;
    private static final int SMALL_ISLAND_MAX_RADIUS = 45;
    private static final int ISLAND_BASE_HEIGHT = 64;
    private static final double ISLAND_SPAWN_CHANCE = 0.35; // Reduced for more gaps

    public SpiritWorldChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        RandomSource random = RandomSource.create(chunk.getPos().toLong());

        int chunkX = chunkPos.getMinBlockX();
        int chunkZ = chunkPos.getMinBlockZ();

        // Generate terrain
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX + x;
                int worldZ = chunkZ + z;

                IslandData islandData = getIslandData(worldX, worldZ, random);

                if (islandData.density > 0) {
                    int height = islandData.height;
                    int depth = islandData.depth;

                    // Determine biome type for this column based on noise
                    double biomeNoise = improvedNoise(worldX * 0.03, worldZ * 0.03);

                    for (int y = ISLAND_BASE_HEIGHT - depth; y <= height; y++) {
                        BlockState state = chooseBlockState(y, height, worldX, y, worldZ,
                                random, false, false);
                        chunk.setBlockState(new BlockPos(x, y, z), state, false);
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    private BlockState chooseBlockState(int y, int height, int worldX, int worldY, int worldZ,
                                        RandomSource random, boolean isEndStoneArea, boolean isGrassyArea) {
        // Determine which patch this location belongs to using 3D noise
        PatchType patchType = getPatchTypeAt(worldX, worldY, worldZ);

        // Surface layer - use the patch's surface block
        if (y == height) {
            if (patchType == PatchType.GRASS) {
                return Blocks.GRASS_BLOCK.defaultBlockState();
            } else {
                // Most patches show their material on the surface
                return patchType.getBlock(random);
            }
        }

        // Sub-surface layers (top 3-5 blocks below surface)
        if (y > height - 5) {
            // Grass patches have dirt subsurface
            if (patchType == PatchType.GRASS && y > height - 3) {
                return Blocks.DIRT.defaultBlockState();
            }
            // Other patches show their material with some variation
            return patchType.getBlock(random);
        }

        // Deep interior - use patch materials
        return patchType.getBlock(random);
    }

    private PatchType getPatchTypeAt(int x, int y, int z) {
        // Use multiple noise octaves to create varied patch sizes
        double largePatchNoise = improvedNoise3D(x * 0.02, y * 0.02, z * 0.02);
        double mediumPatchNoise = improvedNoise3D(x * 0.05 + 1000, y * 0.05 + 1000, z * 0.05 + 1000);

        // Combine noises for patch selection
        double combinedNoise = largePatchNoise * 0.7 + mediumPatchNoise * 0.3;

        // Normalize noise to 0-1 range
        double normalizedNoise = (combinedNoise + 1.0) / 2.0;

        // Calculate total weight
        double totalWeight = 0;
        for (PatchType type : PatchType.values()) {
            totalWeight += type.getWeight();
        }

        // Select patch type based on weighted distribution
        double selector = normalizedNoise * totalWeight;
        double currentWeight = 0;

        for (PatchType type : PatchType.values()) {
            currentWeight += type.getWeight();
            if (selector <= currentWeight) {
                return type;
            }
        }

        // Fallback (should rarely happen)
        return PatchType.WOOL;
    }

    private IslandData getIslandData(int x, int z, RandomSource random) {
        // Main central island with varied height
        double distanceFromCenter = Math.sqrt(x * x + z * z);
        double mainIslandDensity = 1.0 - (distanceFromCenter / MAIN_ISLAND_RADIUS);
        mainIslandDensity = Mth.clamp(mainIslandDensity, 0, 1);

        // Multiple octaves of noise for organic variation
        double noise1 = improvedNoise(x * 0.02, z * 0.02) * 0.4;
        double noise2 = improvedNoise(x * 0.05, z * 0.05) * 0.2;
        double noise3 = improvedNoise(x * 0.1, z * 0.1) * 0.1;
        mainIslandDensity += noise1 + noise2 + noise3;

        // Height variation for main island
        double mainHeightVariation = improvedNoise(x * 0.015, z * 0.015);
        int mainHeight = (int) (ISLAND_BASE_HEIGHT + mainIslandDensity * 25 + mainHeightVariation * 15);
        int mainDepth = (int) (mainIslandDensity * 35);

        // Generate floating islands with varied parameters
        IslandData floatingIsland = generateFloatingIslands(x, z);

        // Combine densities - keep the strongest
        if (floatingIsland.density > mainIslandDensity) {
            return floatingIsland;
        } else if (mainIslandDensity > 0) {
            return new IslandData(mainIslandDensity, mainHeight, mainDepth);
        }

        return new IslandData(0, 0, 0);
    }

    private IslandData generateFloatingIslands(int x, int z) {
        double maxDensity = 0;
        int finalHeight = 0;
        int finalDepth = 0;

        // Create a grid-based system for island spawning with more variation
        int gridSize = 120;
        int gridX = Math.floorDiv(x, gridSize);
        int gridZ = Math.floorDiv(z, gridSize);

        // Check surrounding grid cells for islands
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                int cellX = gridX + offsetX;
                int cellZ = gridZ + offsetZ;

                // Use grid coordinates as seed
                RandomSource cellRandom = RandomSource.create((long) cellX * 341873128712L + cellZ * 132897987541L);

                // Reduced chance for more gaps between islands
                if (cellRandom.nextFloat() < ISLAND_SPAWN_CHANCE) {
                    // Random position within grid cell
                    int islandX = cellX * gridSize + cellRandom.nextInt(gridSize);
                    int islandZ = cellZ * gridSize + cellRandom.nextInt(gridSize);

                    // More varied island sizes
                    int radius = SMALL_ISLAND_MIN_RADIUS + cellRandom.nextInt(SMALL_ISLAND_MAX_RADIUS - SMALL_ISLAND_MIN_RADIUS);

                    // Varied height offset for floating islands
                    int heightOffset = cellRandom.nextInt(40) - 10; // -10 to +30

                    // Varied shape factor
                    double shapeFactor = 0.7 + cellRandom.nextDouble() * 0.6; // 0.7 to 1.3

                    // Calculate distance to this island
                    double distanceToIsland = Math.sqrt(
                            Math.pow(x - islandX, 2) +
                                    Math.pow(z - islandZ, 2)
                    );

                    double density = 1.0 - (distanceToIsland / (radius * shapeFactor));
                    density = Mth.clamp(density, 0, 1);

                    // Multiple noise layers for organic, varied shapes
                    double islandNoise1 = improvedNoise(x * 0.05, z * 0.05) * 0.25;
                    double islandNoise2 = improvedNoise(x * 0.1 + 100, z * 0.1 + 100) * 0.15;
                    density += islandNoise1 + islandNoise2;
                    density = Mth.clamp(density, 0, 1);

                    // Smooth the edges with varied smoothing
                    density = smoothstep(density);

                    if (density > maxDensity) {
                        maxDensity = density;
                        // Varied height and depth
                        finalHeight = (int) (ISLAND_BASE_HEIGHT + heightOffset + density * (15 + cellRandom.nextInt(20)));
                        finalDepth = (int) (density * (20 + cellRandom.nextInt(25)));
                    }
                }
            }
        }

        return new IslandData(maxDensity, finalHeight, finalDepth);
    }

    private double smoothstep(double x) {
        x = Mth.clamp(x, 0, 1);
        return x * x * (3 - 2 * x);
    }

    private double improvedNoise(double x, double z) {
        // Simple 2D Perlin-like noise
        int xi = (int) Math.floor(x);
        int zi = (int) Math.floor(z);

        double xf = x - xi;
        double zf = z - zi;

        double u = fade(xf);
        double v = fade(zf);

        int a = hash(xi) + zi;
        int b = hash(xi + 1) + zi;

        return lerp(v,
                lerp(u, grad(hash(a), xf, zf), grad(hash(b), xf - 1, zf)),
                lerp(u, grad(hash(a + 1), xf, zf - 1), grad(hash(b + 1), xf - 1, zf - 1))
        );
    }

    private double improvedNoise3D(double x, double y, double z) {
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        int zi = (int) Math.floor(z);

        double xf = x - xi;
        double yf = y - yi;
        double zf = z - zi;

        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);

        int a = hash(xi) + yi;
        int aa = hash(a) + zi;
        int ab = hash(a + 1) + zi;
        int b = hash(xi + 1) + yi;
        int ba = hash(b) + zi;
        int bb = hash(b + 1) + zi;

        return lerp(w,
                lerp(v,
                        lerp(u, grad3D(hash(aa), xf, yf, zf), grad3D(hash(ba), xf - 1, yf, zf)),
                        lerp(u, grad3D(hash(ab), xf, yf - 1, zf), grad3D(hash(bb), xf - 1, yf - 1, zf))),
                lerp(v,
                        lerp(u, grad3D(hash(aa + 1), xf, yf, zf - 1), grad3D(hash(ba + 1), xf - 1, yf, zf - 1)),
                        lerp(u, grad3D(hash(ab + 1), xf, yf - 1, zf - 1), grad3D(hash(bb + 1), xf - 1, yf - 1, zf - 1)))
        );
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : z;
        double v = h < 4 ? z : h == 12 || h == 14 ? x : 0;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private double grad3D(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private int hash(int x) {
        x = ((x >> 16) ^ x) * 0x45d9f3b;
        x = ((x >> 16) ^ x) * 0x45d9f3b;
        x = (x >> 16) ^ x;
        return x;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk, GenerationStep.Carving carving) {
        // No carvers
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
                             RandomState randomState, ChunkAccess chunk) {
        // Surface is already built in fillFromNoise
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        // No mob spawning yet
    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType,
                             LevelHeightAccessor level, RandomState randomState) {
        RandomSource random = RandomSource.create((long) x * 341873128712L + z * 132897987541L);
        IslandData data = getIslandData(x, z, random);

        if (data.density > 0) {
            return data.height;
        }
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        RandomSource random = RandomSource.create((long) x * 341873128712L + z * 132897987541L);
        IslandData data = getIslandData(x, z, random);

        BlockState[] states = new BlockState[level.getHeight()];

        if (data.density > 0) {
            int height = data.height;
            int depth = data.depth;

            for (int y = ISLAND_BASE_HEIGHT - depth; y <= height && y < states.length; y++) {
                // Use simplified patch logic for column generation
                PatchType patchType = getPatchTypeAt(x, y, z);

                if (y == height) {
                    if (patchType == PatchType.GRASS) {
                        states[y] = Blocks.GRASS_BLOCK.defaultBlockState();
                    } else {
                        states[y] = patchType.blocks[0];
                    }
                } else if (y > height - 4) {
                    if (patchType == PatchType.GRASS) {
                        states[y] = Blocks.DIRT.defaultBlockState();
                    } else {
                        states[y] = patchType.blocks[0];
                    }
                } else {
                    states[y] = patchType.blocks[0];
                }
            }
        }

        return new NoiseColumn(level.getMinBuildHeight(), states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Spirit World Generator - Enhanced");
    }

    // Helper class to store island data
    private static class IslandData {
        final double density;
        final int height;
        final int depth;

        IslandData(double density, int height, int depth) {
            this.density = density;
            this.height = height;
            this.depth = depth;
        }
    }
}
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

/**
 * Chunk generator for the Spirit World dimension.
 *
 * Each of the six biomes has its own island geometry driven by a
 * {@link SpiritWorldBiome.GenerationMode}:
 *
 *  ARCHIPELAGO  – dense medium islands → rolling wool/grass sea
 *  SPIRE        – tiny radius, huge height, extreme edgeSharpness → crystal needles
 *  SCATTERED    – tiny islands across an enormous Y range → void garden
 *  CONTINENTAL  – enormous radius, soft edges, thick depth → dark continents
 *  PLATEAU      – huge radius, near-zero height variation, flat-top clamp → white tables
 *  CANYON       – large islands whose surface is then carved by canyon noise → layered mesas
 *
 * Block patches use large-scale 2D noise so each material type covers broad,
 * coherent regions rather than single-block speckle.
 */
public class SpiritWorldChunkGenerator extends ChunkGenerator {

    public static final MapCodec<SpiritWorldChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, SpiritWorldChunkGenerator::new)
    );

    // -------------------------------------------------------------------------
    // PatchType – shared block palette
    // -------------------------------------------------------------------------

    public enum PatchType {
        WOOL(new BlockState[]{
                Blocks.RED_WOOL.defaultBlockState(),        Blocks.ORANGE_WOOL.defaultBlockState(),
                Blocks.YELLOW_WOOL.defaultBlockState(),     Blocks.LIME_WOOL.defaultBlockState(),
                Blocks.GREEN_WOOL.defaultBlockState(),      Blocks.CYAN_WOOL.defaultBlockState(),
                Blocks.LIGHT_BLUE_WOOL.defaultBlockState(), Blocks.BLUE_WOOL.defaultBlockState(),
                Blocks.PURPLE_WOOL.defaultBlockState(),     Blocks.MAGENTA_WOOL.defaultBlockState(),
                Blocks.PINK_WOOL.defaultBlockState(),       Blocks.WHITE_WOOL.defaultBlockState()
        }),
        AMETHYST(new BlockState[]{
                Blocks.AMETHYST_BLOCK.defaultBlockState(),       Blocks.BUDDING_AMETHYST.defaultBlockState(),
                Blocks.CALCITE.defaultBlockState(),              Blocks.PURPLE_GLAZED_TERRACOTTA.defaultBlockState()
        }),
        PRISMARINE(new BlockState[]{
                Blocks.PRISMARINE.defaultBlockState(),      Blocks.PRISMARINE_BRICKS.defaultBlockState(),
                Blocks.DARK_PRISMARINE.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState()
        }),
        END_STONE(new BlockState[]{
                Blocks.END_STONE.defaultBlockState(),       Blocks.END_STONE_BRICKS.defaultBlockState(),
                Blocks.PURPUR_BLOCK.defaultBlockState(),    Blocks.PURPUR_PILLAR.defaultBlockState()
        }),
        QUARTZ(new BlockState[]{
                Blocks.QUARTZ_BLOCK.defaultBlockState(),    Blocks.QUARTZ_BRICKS.defaultBlockState(),
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),   Blocks.QUARTZ_PILLAR.defaultBlockState()
        }),
        TERRACOTTA(new BlockState[]{
                Blocks.RED_TERRACOTTA.defaultBlockState(),        Blocks.ORANGE_TERRACOTTA.defaultBlockState(),
                Blocks.YELLOW_TERRACOTTA.defaultBlockState(),     Blocks.CYAN_TERRACOTTA.defaultBlockState(),
                Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState(), Blocks.MAGENTA_TERRACOTTA.defaultBlockState(),
                Blocks.PINK_TERRACOTTA.defaultBlockState()
        }),
        NETHERRACK(new BlockState[]{
                Blocks.NETHERRACK.defaultBlockState(),        Blocks.NETHER_BRICKS.defaultBlockState(),
                Blocks.RED_NETHER_BRICKS.defaultBlockState(), Blocks.CRIMSON_NYLIUM.defaultBlockState(),
                Blocks.WARPED_NYLIUM.defaultBlockState()
        }),
        BLACKSTONE(new BlockState[]{
                Blocks.BLACKSTONE.defaultBlockState(),             Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                Blocks.GILDED_BLACKSTONE.defaultBlockState(),      Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
        }),
        BASALT(new BlockState[]{
                Blocks.BASALT.defaultBlockState(), Blocks.SMOOTH_BASALT.defaultBlockState(),
                Blocks.POLISHED_BASALT.defaultBlockState()
        }),
        DEEPSLATE(new BlockState[]{
                Blocks.DEEPSLATE.defaultBlockState(),      Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                Blocks.DEEPSLATE_TILES.defaultBlockState()
        }),
        STONE(new BlockState[]{
                Blocks.STONE.defaultBlockState(), Blocks.ANDESITE.defaultBlockState(),
                Blocks.DIORITE.defaultBlockState()
        }),
        GRASS(new BlockState[]{
                Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState()
        });

        public final BlockState[] blocks;
        PatchType(BlockState[] blocks) { this.blocks = blocks; }
        public BlockState getBlock(RandomSource rng) { return blocks[rng.nextInt(blocks.length)]; }
    }

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final int    MAIN_ISLAND_RADIUS  = 100;
    private static final int    ISLAND_BASE_HEIGHT  = 64;

    /**
     * Terracotta colour strata for the CANYON biome, surface → deep.
     * The pattern repeats every CANYON_LAYERS.length blocks of depth.
     */
    private static final BlockState[] CANYON_LAYERS = {
            Blocks.RED_TERRACOTTA.defaultBlockState(),
            Blocks.ORANGE_TERRACOTTA.defaultBlockState(),
            Blocks.YELLOW_TERRACOTTA.defaultBlockState(),
            Blocks.WHITE_TERRACOTTA.defaultBlockState(),
            Blocks.ORANGE_TERRACOTTA.defaultBlockState(),
            Blocks.BROWN_TERRACOTTA.defaultBlockState(),
            Blocks.CYAN_TERRACOTTA.defaultBlockState(),
            Blocks.RED_TERRACOTTA.defaultBlockState(),
            Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState(),
            Blocks.MAGENTA_TERRACOTTA.defaultBlockState(),
    };

    /**
     * Noise frequency used for patch region selection.
     * Lower = larger, more coherent material patches (less "speckle").
     * The secondary octave adds organic boundary variation.
     */
    private static final double PATCH_FREQ_LARGE  = 0.008;
    private static final double PATCH_FREQ_MEDIUM = 0.020;

    // -------------------------------------------------------------------------
    // Constructor / codec
    // -------------------------------------------------------------------------

    public SpiritWorldChunkGenerator(BiomeSource biomeSource) { super(biomeSource); }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }

    // -------------------------------------------------------------------------
    // Primary fill
    // -------------------------------------------------------------------------

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager sm, ChunkAccess chunk) {
        ChunkPos cp     = chunk.getPos();
        RandomSource rng = RandomSource.create(cp.toLong());
        int chunkX = cp.getMinBlockX();
        int chunkZ = cp.getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = chunkX + x;
                int wz = chunkZ + z;

                SpiritWorldBiome biome  = SpiritWorldBiome.getBiomeAt(wx, wz);
                IslandData       island = getIslandData(wx, wz, rng, biome);

                if (island.density > 0) {
                    int bottom = Math.max(0, island.bottom);
                    for (int y = bottom; y <= island.top; y++) {
                        // CANYON: skip if inside a carved groove
                        if (biome.mode == SpiritWorldBiome.GenerationMode.CANYON
                                && isCanyonVoid(wx, y, wz, island.top)) {
                            continue;
                        }
                        BlockState state = chooseBlock(y, island.top, wx, y, wz, rng, biome);
                        chunk.setBlockState(new BlockPos(x, y, z), state, false);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    // -------------------------------------------------------------------------
    // Canyon groove carving
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the given world position falls inside a canyon groove.
     *
     * Two octaves of noise create channel-like trenches. The deeper below the
     * surface, the wider the canyon must be to keep the block as air, producing
     * V-shaped grooves when viewed from the side.
     */
    private boolean isCanyonVoid(int x, int y, int z, int surfaceY) {
        int depth = surfaceY - y;
        if (depth <= 2) return false; // top 2 layers are always solid (colour cap)

        double canyonNoise = improvedNoise(x * 0.012, z * 0.012);
        double detailNoise = improvedNoise(x * 0.035 + 500, z * 0.035 + 500) * 0.35;
        double combined    = canyonNoise + detailNoise;

        // Groove widens with depth — threshold shrinks toward the surface
        double threshold = 0.18 - depth * 0.008;
        return combined > threshold;
    }

    // -------------------------------------------------------------------------
    // Block selection
    // -------------------------------------------------------------------------

    private BlockState chooseBlock(int y, int surfaceY, int wx, int wy, int wz,
                                   RandomSource rng, SpiritWorldBiome biome) {
        // CANYON: depth-based colour strata; no patch system needed
        if (biome.mode == SpiritWorldBiome.GenerationMode.CANYON) {
            int depth = surfaceY - y;
            return CANYON_LAYERS[depth % CANYON_LAYERS.length];
        }

        PatchType patch = getPatchAt(wx, wy, wz, biome);

        if (y == surfaceY) {
            return patch == PatchType.GRASS
                    ? Blocks.GRASS_BLOCK.defaultBlockState()
                    : patch.getBlock(rng);
        }
        if (patch == PatchType.GRASS && y > surfaceY - 3) {
            return Blocks.DIRT.defaultBlockState();
        }
        return patch.getBlock(rng);
    }

    /**
     * Determines which {@link PatchType} applies at a given world position.
     *
     * Uses two octaves of large-scale 2D noise so that material regions are
     * broad and coherent (tens of blocks across) rather than single-block
     * speckle. The noise value is mapped to a PatchType via the biome's
     * weighted palette.
     */
    private PatchType getPatchAt(int x, int y, int z, SpiritWorldBiome biome) {
        // Large primary octave — drives the main patch regions
        double large  = improvedNoise(x * PATCH_FREQ_LARGE,  z * PATCH_FREQ_LARGE);
        // Medium secondary octave — adds organic boundary variation
        double medium = improvedNoise(x * PATCH_FREQ_MEDIUM + 1000, z * PATCH_FREQ_MEDIUM + 1000);

        // Blend heavily toward the large scale so patches stay wide and solid
        double norm = ((large * 0.80 + medium * 0.20) + 1.0) / 2.0; // → [0, 1]

        double[]   weights = biome.getPatchWeights();
        double     total   = 0;
        for (double w : weights) total += w;

        double    cursor = norm * total;
        double    acc    = 0;
        PatchType[] types = PatchType.values();
        for (int i = 0; i < types.length && i < weights.length; i++) {
            acc += weights[i];
            if (cursor <= acc) return types[i];
        }
        return PatchType.WOOL;
    }

    // -------------------------------------------------------------------------
    // Island geometry dispatch
    // -------------------------------------------------------------------------

    private IslandData getIslandData(int x, int z, RandomSource rng, SpiritWorldBiome biome) {
        IslandData main     = centralIsland(x, z);
        IslandData floating = switch (biome.mode) {
            case ARCHIPELAGO -> floatingArchipelago(x, z, biome);
            case SPIRE       -> floatingSpires(x, z, biome);
            case SCATTERED   -> floatingScattered(x, z, biome);
            case CONTINENTAL -> floatingContinental(x, z, biome);
            case PLATEAU     -> floatingPlateau(x, z, biome);
            case CANYON      -> floatingCanyon(x, z, biome);
        };

        if (floating.density > main.density) return floating;
        if (main.density > 0)               return main;
        return IslandData.EMPTY;
    }

    // ── Central spawn island (all biomes) ────────────────────────────────────

    private IslandData centralIsland(int x, int z) {
        double dist    = Math.sqrt((double)(x * x + z * z));
        double density = Mth.clamp(1.0 - dist / MAIN_ISLAND_RADIUS, 0, 1);
        density += improvedNoise(x * 0.020, z * 0.020) * 0.40
                +  improvedNoise(x * 0.050, z * 0.050) * 0.20
                +  improvedNoise(x * 0.100, z * 0.100) * 0.10;
        density = Mth.clamp(density, 0, 1);
        if (density <= 0) return IslandData.EMPTY;

        int top    = (int)(ISLAND_BASE_HEIGHT + density * 25 + improvedNoise(x * 0.015, z * 0.015) * 15);
        int bottom = top - (int)(density * 35);
        return new IslandData(density, top, bottom);
    }

    // ── ARCHIPELAGO ──────────────────────────────────────────────────────────

    private IslandData floatingArchipelago(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0; int bestTop = 0, bestBot = 0;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double sf  = 0.75 + cr.nextDouble() * 0.50;
            double raw = Mth.clamp(1.0 - dist(x, z, ix, iz) / (radius * sf), 0, 1);
            raw += improvedNoise(x * 0.055, z * 0.055) * 0.20
                    +  improvedNoise(x * 0.110 + 200, z * 0.110 + 200) * 0.12;
            raw = Mth.clamp(smoothstep(raw), 0, 1);

            if (raw > best) {
                best    = raw;
                bestTop = p.baseHeight() + yo + (int)(raw * p.heightVariation());
                bestBot = bestTop - (int)(raw * p.heightVariation() * p.depthMultiplier());
            }
        }
        return best > 0 ? new IslandData(best, bestTop, bestBot) : IslandData.EMPTY;
    }

    // ── SPIRE ────────────────────────────────────────────────────────────────

    private IslandData floatingSpires(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0; int bestTop = 0, bestBot = 0;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double raw     = Mth.clamp(1.0 - dist(x, z, ix, iz) / radius, 0, 1);
            double density = Math.pow(raw, p.edgeSharpness());
            density = Mth.clamp(density, 0, 1);

            if (density > best) {
                best    = density;
                bestTop = p.baseHeight() + yo + (int)(density * p.heightVariation());
                bestBot = bestTop - (int)(density * p.heightVariation() * p.depthMultiplier());
            }
        }
        return best > 0 ? new IslandData(best, bestTop, bestBot) : IslandData.EMPTY;
    }

    // ── SCATTERED ────────────────────────────────────────────────────────────

    private IslandData floatingScattered(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0; int bestTop = 0, bestBot = 0;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double raw = Mth.clamp(1.0 - dist(x, z, ix, iz) / radius, 0, 1);
            raw = Math.pow(raw, p.edgeSharpness());
            raw = Mth.clamp(raw, 0, 1);

            if (raw > best) {
                best    = raw;
                bestTop = p.baseHeight() + yo + (int)(raw * p.heightVariation());
                bestBot = bestTop - (int)(raw * p.heightVariation() * p.depthMultiplier());
            }
        }
        return best > 0 ? new IslandData(best, bestTop, bestBot) : IslandData.EMPTY;
    }

    // ── CONTINENTAL ──────────────────────────────────────────────────────────

    private IslandData floatingContinental(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0; int bestTop = 0, bestBot = 0;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double raw = Mth.clamp(1.0 - dist(x, z, ix, iz) / (double) radius, 0, 1);
            raw += improvedNoise(x * 0.008, z * 0.008) * 0.35
                    +  improvedNoise(x * 0.020 + 300, z * 0.020 + 300) * 0.15;
            raw = Mth.clamp(Math.pow(raw, p.edgeSharpness()), 0, 1);

            if (raw > best) {
                best    = raw;
                bestTop = p.baseHeight() + yo + (int)(raw * p.heightVariation());
                bestBot = bestTop - (int)(raw * p.heightVariation() * p.depthMultiplier());
            }
        }
        return best > 0 ? new IslandData(best, bestTop, bestBot) : IslandData.EMPTY;
    }

    // ── PLATEAU ──────────────────────────────────────────────────────────────

    /**
     * Flat-top slabs: surface height is clamped to a very narrow band so the
     * top is nearly perfectly flat. Slab thickness is 8–16 blocks.
     */
    private IslandData floatingPlateau(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0; int bestTop = 0, bestBot = 0;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double raw = Mth.clamp(1.0 - dist(x, z, ix, iz) / (double) radius, 0, 1);
            raw = Math.pow(raw, p.edgeSharpness());
            raw = Mth.clamp(raw, 0, 1);

            if (raw > best) {
                best = raw;
                double surfaceJitter = improvedNoise(x * 0.040, z * 0.040) * p.heightVariation();
                bestTop = p.baseHeight() + yo + (int) surfaceJitter;
                bestBot = bestTop - 8 - (int)(raw * 8);
            }
        }
        return best > 0 ? new IslandData(best, bestTop, bestBot) : IslandData.EMPTY;
    }

    // ── CANYON ───────────────────────────────────────────────────────────────

    private IslandData floatingCanyon(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0; int bestTop = 0, bestBot = 0;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double sf  = 0.75 + cr.nextDouble() * 0.50;
            double raw = Mth.clamp(1.0 - dist(x, z, ix, iz) / (radius * sf), 0, 1);
            raw += improvedNoise(x * 0.015, z * 0.015) * 0.25
                    +  improvedNoise(x * 0.035 + 400, z * 0.035 + 400) * 0.12;
            raw = Mth.clamp(Math.pow(raw, p.edgeSharpness()), 0, 1);

            if (raw > best) {
                best    = raw;
                bestTop = p.baseHeight() + yo + (int)(raw * p.heightVariation());
                bestBot = bestTop - (int)(raw * p.heightVariation() * p.depthMultiplier());
            }
        }
        return best > 0 ? new IslandData(best, bestTop, bestBot) : IslandData.EMPTY;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private double dist(int x, int z, int ix, int iz) {
        return Math.sqrt(Math.pow(x - ix, 2) + Math.pow(z - iz, 2));
    }

    private RandomSource cellRng(int cx, int cz) {
        return RandomSource.create((long) cx * 341_873_128_712L + (long) cz * 132_897_987_541L);
    }

    private double smoothstep(double x) {
        x = Mth.clamp(x, 0, 1);
        return x * x * (3 - 2 * x);
    }

    // -------------------------------------------------------------------------
    // Required overrides
    // -------------------------------------------------------------------------

    @Override
    public void applyCarvers(WorldGenRegion r, long seed, RandomState rs, BiomeManager bm,
                             StructureManager sm, ChunkAccess chunk, GenerationStep.Carving c) {}

    @Override
    public void buildSurface(WorldGenRegion r, StructureManager sm, RandomState rs, ChunkAccess c) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion r) {}

    @Override public int getGenDepth() { return 256; }
    @Override public int getSeaLevel() { return 0;   }
    @Override public int getMinY()     { return 0;   }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level,
                             RandomState rs) {
        SpiritWorldBiome b = SpiritWorldBiome.getBiomeAt(x, z);
        IslandData d = getIslandData(x, z,
                RandomSource.create((long) x * 341_873_128_712L + z * 132_897_987_541L), b);
        return d.density > 0 ? d.top : 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState rs) {
        SpiritWorldBiome b    = SpiritWorldBiome.getBiomeAt(x, z);
        IslandData data       = getIslandData(x, z,
                RandomSource.create((long) x * 341_873_128_712L + z * 132_897_987_541L), b);
        BlockState[] states   = new BlockState[level.getHeight()];

        if (data.density > 0) {
            int bottom = Math.max(0, data.bottom);
            for (int y = bottom; y <= data.top && y < states.length; y++) {
                if (b.mode == SpiritWorldBiome.GenerationMode.CANYON) {
                    states[y] = CANYON_LAYERS[(data.top - y) % CANYON_LAYERS.length];
                } else {
                    PatchType pt = getPatchAt(x, y, z, b);
                    states[y] = (y == data.top && pt == PatchType.GRASS)
                            ? Blocks.GRASS_BLOCK.defaultBlockState()
                            : pt.blocks[0];
                }
            }
        }
        return new NoiseColumn(level.getMinBuildHeight(), states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState rs, BlockPos pos) {
        SpiritWorldBiome b = SpiritWorldBiome.getBiomeAt(pos.getX(), pos.getZ());
        info.add("Spirit World | Biome: " + b.name() + " | Mode: " + b.mode);
    }

    // -------------------------------------------------------------------------
    // Perlin noise
    // -------------------------------------------------------------------------

    private double improvedNoise(double x, double z) {
        int xi = (int) Math.floor(x), zi = (int) Math.floor(z);
        double xf = x - xi, zf = z - zi, u = fade(xf), v = fade(zf);
        int a = hash(xi) + zi, b = hash(xi + 1) + zi;
        return lerp(v, lerp(u, grad(hash(a),     xf,     zf),
                        grad(hash(b),     xf - 1, zf)),
                lerp(u, grad(hash(a + 1), xf,     zf - 1),
                        grad(hash(b + 1), xf - 1, zf - 1)));
    }

    private double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private double lerp(double t, double a, double b) { return a + t * (b - a); }

    private double grad(int hash, double x, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : z;
        double v = h < 4 ? z : (h == 12 || h == 14) ? x : 0;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private int hash(int x) {
        x = ((x >> 16) ^ x) * 0x45d9f3b;
        x = ((x >> 16) ^ x) * 0x45d9f3b;
        return (x >> 16) ^ x;
    }

    // -------------------------------------------------------------------------
    // Island data record
    // -------------------------------------------------------------------------

    private record IslandData(double density, int top, int bottom) {
        static final IslandData EMPTY = new IslandData(0, 0, 0);
    }
}
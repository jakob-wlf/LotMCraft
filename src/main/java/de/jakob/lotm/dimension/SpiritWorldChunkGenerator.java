package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class SpiritWorldChunkGenerator extends ChunkGenerator {

    public static final MapCodec<SpiritWorldChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, SpiritWorldChunkGenerator::new)
    );


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
        }),

        ICE(new BlockState[]{
                Blocks.PACKED_ICE.defaultBlockState(),  Blocks.BLUE_ICE.defaultBlockState(),
                Blocks.ICE.defaultBlockState(),         Blocks.SNOW_BLOCK.defaultBlockState()
        }),

        MUSHROOM(new BlockState[]{
                Blocks.RED_MUSHROOM_BLOCK.defaultBlockState(),  Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState(),
                Blocks.MUSHROOM_STEM.defaultBlockState(),       Blocks.SHROOMLIGHT.defaultBlockState(),
                Blocks.WARPED_NYLIUM.defaultBlockState(),       Blocks.CRIMSON_NYLIUM.defaultBlockState(),
                Blocks.MYCELIUM.defaultBlockState()
        }),

        COPPER(new BlockState[]{
                Blocks.OXIDIZED_COPPER.defaultBlockState(),          Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(),
                Blocks.EXPOSED_COPPER.defaultBlockState(),           Blocks.WEATHERED_COPPER.defaultBlockState(),
                Blocks.COPPER_BLOCK.defaultBlockState(),             Blocks.CUT_COPPER.defaultBlockState()
        }),
        GOLD_BLOCK(new BlockState[]{
                Blocks.GOLD_BLOCK.defaultBlockState(),    Blocks.GILDED_BLACKSTONE.defaultBlockState(),
                Blocks.RAW_GOLD_BLOCK.defaultBlockState()
        }),
        ASH(new BlockState[]{
                Blocks.TUFF.defaultBlockState(),               Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
                Blocks.DEEPSLATE.defaultBlockState(),          Blocks.CHISELED_DEEPSLATE.defaultBlockState(),
                Blocks.POLISHED_DEEPSLATE.defaultBlockState()
        }),
        MIRE(new BlockState[]{
                Blocks.MUD.defaultBlockState(),                Blocks.PACKED_MUD.defaultBlockState(),
                Blocks.MUD_BRICKS.defaultBlockState(),         Blocks.MOSS_BLOCK.defaultBlockState(),
                Blocks.ROOTED_DIRT.defaultBlockState()
        }),
        BONE(new BlockState[]{
                Blocks.BONE_BLOCK.defaultBlockState(),         Blocks.SMOOTH_QUARTZ.defaultBlockState(),
                Blocks.WHITE_TERRACOTTA.defaultBlockState(),   Blocks.DIORITE.defaultBlockState()
        }),
        ORB(new BlockState[]{
                Blocks.CRYING_OBSIDIAN.defaultBlockState(),    Blocks.OBSIDIAN.defaultBlockState(),
                Blocks.AMETHYST_BLOCK.defaultBlockState()
        });

        public final BlockState[] blocks;
        PatchType(BlockState[] blocks) { this.blocks = blocks; }
        public BlockState getBlock(RandomSource rng) { return blocks[rng.nextInt(blocks.length)]; }
    }

    private static final int MAIN_ISLAND_RADIUS = 100;
    private static final int ISLAND_BASE_HEIGHT = 64;

    /** ARCHIPELAGO: Y level of the ambient "wool sea" surface plane. */
    private static final int WOOL_SEA_LEVEL = 48;
    /** ARCHIPELAGO: thickness of the wool sea surface layer. */
    private static final int WOOL_SEA_DEPTH = 4;

    /** SPIRE: how many blocks apart each crystal shelf ring is on the shaft. */
    private static final int SPIRE_SHELF_INTERVAL = 18;
    /** SPIRE: how many blocks a shelf ring protrudes beyond the shaft radius. */
    private static final int SPIRE_SHELF_WIDTH = 4;
    /** SPIRE: thickness of each shelf ring in the Y axis. */
    private static final int SPIRE_SHELF_THICKNESS = 3;

    /**
     * SPIRE: Y height of the crystal cave floor.  Spires grow up from this level;
     * stalactite/stalagmite formations fill the space between floor and spire base.
     */
    private static final int SPIRE_FLOOR_Y = 30;
    /** SPIRE: thickness of the solid bedrock-like base layer beneath the floor. */
    private static final int SPIRE_FLOOR_BASE_THICKNESS = 6;

    /** CONTINENTAL: shelf / cliff zone fractions. */
    private static final double CONTINENTAL_SHELF_FRAC = 0.30;
    private static final double CONTINENTAL_CLIFF_FRAC = 0.20;

    private static final double PLATEAU_EROSION_STRENGTH = 0.55;

    private static final int EMBER_LAVA_LEVEL = 48;

    private static final int   CHAOS_ORB_GRID        = 40;
    private static final int   CHAOS_ORB_MIN_RADIUS   = 3;
    private static final int   CHAOS_ORB_MAX_RADIUS   = 7;
    private static final float CHAOS_ORB_SPAWN_CHANCE = 0.35f;
    private static final int   CHAOS_ORB_Y_MIN         = 40;
    private static final int   CHAOS_ORB_Y_MAX         = 160;

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

    private static final BlockState[] GLACIAL_LAYERS = {
            Blocks.SNOW_BLOCK.defaultBlockState(),
            Blocks.PACKED_ICE.defaultBlockState(),
            Blocks.PACKED_ICE.defaultBlockState(),
            Blocks.BLUE_ICE.defaultBlockState(),
            Blocks.PACKED_ICE.defaultBlockState(),
            Blocks.ICE.defaultBlockState(),
            Blocks.BLUE_ICE.defaultBlockState(),
            Blocks.PACKED_ICE.defaultBlockState(),
    };

    private static final double PATCH_FREQ_LARGE  = 0.008;
    private static final double PATCH_FREQ_MEDIUM = 0.020;


    public SpiritWorldChunkGenerator(BiomeSource biomeSource) { super(biomeSource); }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }


    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                                 StructureManager sm, ChunkAccess chunk) {
        ChunkPos cp  = chunk.getPos();
        RandomSource rng = RandomSource.create(cp.toLong());
        int chunkX = cp.getMinBlockX();
        int chunkZ = cp.getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = chunkX + x;
                int wz = chunkZ + z;

                SpiritWorldBiome.BiomeWeight[] blend = SpiritWorldBiome.getBlendedBiomesAt(wx, wz);
                SpiritWorldBiome               biome = blend[0].biome();   // dominant biome drives mode-specific logic
                IslandData                    island = getBlendedIslandData(wx, wz, rng, blend);

                if (biome.mode == SpiritWorldBiome.GenerationMode.ARCHIPELAGO) {
                    for (int y = WOOL_SEA_LEVEL - WOOL_SEA_DEPTH; y <= WOOL_SEA_LEVEL; y++) {
                        if (island.density <= 0 || y < island.bottom || y > island.top) {
                            BlockState ws = woolSeaBlock(wx, y, wz, rng);
                            chunk.setBlockState(new BlockPos(x, y, z), ws, false);
                        }
                    }
                }

                if (biome.mode == SpiritWorldBiome.GenerationMode.SPIRE) {
                    fillSpireFloor(x, z, wx, wz, chunk, island, rng);
                }

                if (biome.mode == SpiritWorldBiome.GenerationMode.CHAOS) {
                    fillChaosOrbs(x, z, wx, wz, chunk, rng);
                }

                if (island.density > 0) {
                    int bottom = Math.max(0, island.bottom);
                    for (int y = bottom; y <= island.top; y++) {
                        if (biome.mode == SpiritWorldBiome.GenerationMode.CANYON
                                && isCanyonVoid(wx, y, wz, island.top, island.flowAngle)) {
                            continue;
                        }
                        if (biome.mode == SpiritWorldBiome.GenerationMode.SPIRE) {
                            if (!isSpireSolid(wx, y, wz, island)) continue;
                        }
                        BlockState state = chooseBlock(y, island.top, wx, y, wz, rng, biome);
                        chunk.setBlockState(new BlockPos(x, y, z), state, false);
                    }
                }

                if (biome.mode == SpiritWorldBiome.GenerationMode.CONTINENTAL
                        && biome == SpiritWorldBiome.EMBER_WASTES) {
                    fillEmberLava(x, z, wx, wz, chunk, island);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private IslandData getBlendedIslandData(int x, int z, RandomSource rng,
                                            SpiritWorldBiome.BiomeWeight[] blend) {
        if (blend.length == 1) return getIslandData(x, z, rng, blend[0].biome());

        IslandData a  = getIslandData(x, z, rng, blend[0].biome());
        IslandData b  = getIslandData(x, z, rng, blend[1].biome());
        double     wa = blend[0].weight(), wb = blend[1].weight();

        double density = a.density() * wa + b.density() * wb;
        if (density <= 0) return IslandData.EMPTY;

        double ca = a.density() * wa;
        double cb = b.density() * wb;
        double sum = ca + cb;
        if (sum <= 0) return IslandData.EMPTY;

        int top    = (int)((a.top()    * ca + b.top()    * cb) / sum);
        int bottom = (int)((a.bottom() * ca + b.bottom() * cb) / sum);

        return new IslandData(density, top, bottom,
                a.centerX(), a.centerZ(),
                a.shaftTop(), a.shaftRadius(),
                a.flowAngle());
    }

    private BlockState woolSeaBlock(int x, int y, int wz, RandomSource rng) {
        if (y == WOOL_SEA_LEVEL) {
            double n = (improvedNoise(x * PATCH_FREQ_LARGE, wz * PATCH_FREQ_LARGE) + 1.0) * 0.5;
            int idx = (int)(n * (PatchType.WOOL.blocks.length - 1));
            return PatchType.WOOL.blocks[Mth.clamp(idx, 0, PatchType.WOOL.blocks.length - 1)];
        }
        return (y % 2 == 0) ? Blocks.WHITE_WOOL.defaultBlockState()
                : Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
    }

    private void fillSpireFloor(int lx, int lz, int wx, int wz,
                                ChunkAccess chunk, IslandData island,
                                RandomSource rng) {

        int baseTop = SPIRE_FLOOR_Y;
        int baseBot = SPIRE_FLOOR_Y - SPIRE_FLOOR_BASE_THICKNESS;
        for (int y = baseBot; y <= baseTop; y++) {
            BlockState base = (y % 2 == 0)
                    ? Blocks.CALCITE.defaultBlockState()
                    : Blocks.AMETHYST_BLOCK.defaultBlockState();
            chunk.setBlockState(new BlockPos(lx, y, lz), base, false);
        }

        double n1 = improvedNoise(wx * 0.07,       wz * 0.07);
        double n2 = improvedNoise(wx * 0.18 + 300, wz * 0.18 + 300);
        double combined = (n1 * 0.65 + n2 * 0.35 + 1.0) * 0.5; // normalise to [0,1]

        if (combined > 0.52) {
            int stalagHeight = (int)(combined * 18); // up to 18 blocks tall
            int stalagTop    = baseTop + stalagHeight;

            for (int y = baseTop + 1; y <= stalagTop; y++) {
                double progress = (double)(y - baseTop) / stalagHeight; // 0=base,1=tip
                double taperNoise = improvedNoise(wx * 0.25 + 600, wz * 0.25 + 600) * 0.3;
                if (Math.random() < progress * 0.55 + taperNoise) break; // probabilistic taper

                BlockState spire = spireFloorBlock(y, combined);
                chunk.setBlockState(new BlockPos(lx, y, lz), spire, false);
            }
        }

        if (island.density > 0) {
            int islandBottom = island.bottom;
            double n3 = improvedNoise(wx * 0.09 + 900, wz * 0.09 + 900);
            double n4 = improvedNoise(wx * 0.22 + 1200, wz * 0.22 + 1200);
            double stalCombined = (n3 * 0.60 + n4 * 0.40 + 1.0) * 0.5;

            if (stalCombined > 0.50) {
                int stalHeight = (int)(stalCombined * 14); // up to 14 blocks
                int stalBottom = Math.max(baseTop + 1, islandBottom - stalHeight);
                for (int y = islandBottom - 1; y >= stalBottom; y--) {
                    double progress = (double)(islandBottom - y) / stalHeight;
                    if (Math.random() < progress * 0.60) break;
                    BlockState spire = spireFloorBlock(y, stalCombined);
                    // Don't overwrite existing island blocks
                    if (chunk.getBlockState(new BlockPos(lx, y, lz)).isAir()) {
                        chunk.setBlockState(new BlockPos(lx, y, lz), spire, false);
                    }
                }
            }
        }
    }

    private BlockState spireFloorBlock(int y, double noiseVal) {
        if (noiseVal > 0.80) return Blocks.BUDDING_AMETHYST.defaultBlockState();
        if (noiseVal > 0.65) return Blocks.AMETHYST_BLOCK.defaultBlockState();
        if (y % 3 == 0)      return Blocks.CALCITE.defaultBlockState();
        if (y % 3 == 1)      return Blocks.PRISMARINE.defaultBlockState();
        return Blocks.AMETHYST_BLOCK.defaultBlockState();
    }

    private boolean isSpireSolid(int wx, int y, int wz, IslandData island) {
        double dx = wx - island.centerX;
        double dz = wz - island.centerZ;
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        int shaftTop = island.shaftTop;
        int top      = island.top;

        if (y <= shaftTop) {
            double shaftRadius = island.shaftRadius;
            int depthFromShaftTop = shaftTop - y;
            int ringPhase = depthFromShaftTop % SPIRE_SHELF_INTERVAL;
            boolean inShelfBody = (ringPhase < SPIRE_SHELF_THICKNESS);
            double effectiveRadius = inShelfBody
                    ? shaftRadius + SPIRE_SHELF_WIDTH
                    : shaftRadius;
            return horizDist <= effectiveRadius;
        } else {
            double tipProgress = (double)(y - shaftTop) / Math.max(1, top - shaftTop);
            double tipRadius   = Mth.lerp(tipProgress, island.shaftRadius, 0.5);
            return horizDist <= tipRadius;
        }
    }

    private void fillEmberLava(int lx, int lz, int wx, int wz,
                               ChunkAccess chunk, IslandData island) {
        // Determine if this column is open (no island) or shallow (island top below lava level)
        boolean columnOpen = island.density <= 0;
        boolean columnShallow = island.density > 0 && island.top < EMBER_LAVA_LEVEL;

        if (columnOpen || columnShallow) {
            int lavaBottom = columnShallow ? island.top + 1 : 1;
            for (int y = lavaBottom; y <= EMBER_LAVA_LEVEL; y++) {
                if (chunk.getBlockState(new BlockPos(lx, y, lz)).isAir()) {
                    chunk.setBlockState(new BlockPos(lx, y, lz),
                            Blocks.LAVA.defaultBlockState(), false);
                }
            }
        }
    }

    private IslandData floatingChaos(int x, int z, SpiritWorldBiome b) {
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

            double cellSharpness = 0.6 + cr.nextDouble() * 3.2;
            double cellChaos     = 0.3 + cr.nextDouble() * 0.9;

            double d    = dist(x, z, ix, iz);
            double frac = d / radius;
            if (frac >= 1.0) continue;

            double raw = Mth.clamp(1.0 - frac, 0, 1);

            double shapeNoise = improvedNoise(x * 0.020 + cx * 13.7, z * 0.020 + cz * 13.7) * 0.35
                    +            improvedNoise(x * 0.055 + 400,      z * 0.055 + 400)       * 0.20 * cellChaos;
            raw = Mth.clamp(raw + shapeNoise, 0, 1);
            raw = Math.pow(raw, cellSharpness);
            if (raw <= 0) continue;

            double stepNoise  = improvedNoise(x * 0.030 + 900, z * 0.030 + 900);
            int    steps      = 3 + cr.nextInt(3);
            double terraced   = Math.floor(raw * steps) / (double) steps;
            double heightFrac = Mth.lerp(0.5, raw, terraced) + stepNoise * 0.05;
            heightFrac = Mth.clamp(heightFrac, 0, 1);

            if (raw > best) {
                best    = raw;
                bestTop = p.baseHeight() + yo + (int)(heightFrac * p.heightVariation());
                bestBot = bestTop - (int)(heightFrac * p.heightVariation() * p.depthMultiplier())
                        - (int)(cr.nextDouble() * 10); // ragged, uneven undersides
            }
        }
        return best > 0 ? new IslandData(best, bestTop, bestBot, 0, 0, 0, 0, 0) : IslandData.EMPTY;
    }

    private void fillChaosOrbs(int lx, int lz, int wx, int wz, ChunkAccess chunk, RandomSource rng) {
        int gx = Math.floorDiv(wx, CHAOS_ORB_GRID);
        int gz = Math.floorDiv(wz, CHAOS_ORB_GRID);

        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                int cx = gx + ox, cz = gz + oz;
                RandomSource cr = orbCellRng(cx, cz);
                if (cr.nextFloat() >= CHAOS_ORB_SPAWN_CHANCE) continue;

                int ix     = cx * CHAOS_ORB_GRID + cr.nextInt(CHAOS_ORB_GRID);
                int iz     = cz * CHAOS_ORB_GRID + cr.nextInt(CHAOS_ORB_GRID);
                int iy     = CHAOS_ORB_Y_MIN + cr.nextInt(CHAOS_ORB_Y_MAX - CHAOS_ORB_Y_MIN);
                int radius = CHAOS_ORB_MIN_RADIUS + cr.nextInt(CHAOS_ORB_MAX_RADIUS - CHAOS_ORB_MIN_RADIUS);

                double dx = wx - ix, dz = wz - iz;
                double horizDistSq = dx * dx + dz * dz;
                if (horizDistSq > (double) radius * radius) continue;

                int vertRange = (int) Math.sqrt(Math.max(0, radius * radius - horizDistSq));
                int yBottom = iy - vertRange;
                int yTop    = iy + vertRange;
                boolean hollow = radius >= 5;

                for (int y = yBottom; y <= yTop; y++) {
                    double dy = y - iy;
                    double distSq = dx * dx + dz * dz + dy * dy;
                    if (distSq > (double) radius * radius) continue;
                    if (hollow && distSq < (double) (radius - 2) * (radius - 2)) continue; // hollow shell

                    BlockState orbBlock = chaosOrbBlock(cr, distSq, radius);
                    chunk.setBlockState(new BlockPos(lx, y, lz), orbBlock, false);
                }
            }
        }
    }

    private RandomSource orbCellRng(int cx, int cz) {
        return RandomSource.create((long) cx * 998_244_353L + (long) cz * 500_000_003L + 42L);
    }

    private BlockState chaosOrbBlock(RandomSource cr, double distSq, int radius) {
        double edgeFrac = radius > 0 ? distSq / ((double) radius * radius) : 0;
        if (edgeFrac > 0.75) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        if (edgeFrac > 0.45) return cr.nextFloat() < 0.5
                ? Blocks.OBSIDIAN.defaultBlockState()
                : Blocks.AMETHYST_BLOCK.defaultBlockState();
        return Blocks.BUDDING_AMETHYST.defaultBlockState();
    }

    private boolean isCanyonVoid(int wx, int y, int wz, int surfaceY, double flowAngle) {
        int depth = surfaceY - y;
        if (depth <= 2) return false;

        double cosA = Math.cos(flowAngle);
        double sinA = Math.sin(flowAngle);
        double across = wx * cosA - wz * sinA;

        double canyonNoise = improvedNoise(across * 0.045, depth * 0.020);
        double wallNoise   = improvedNoise(wx * 0.060 + 700, wz * 0.060 + 700) * 0.25;

        double combined = canyonNoise + wallNoise;
        double threshold = 0.12 - depth * 0.006;
        return combined > threshold;
    }

    private BlockState chooseBlock(int y, int surfaceY, int wx, int wy, int wz,
                                   RandomSource rng, SpiritWorldBiome biome) {
        if (biome.mode == SpiritWorldBiome.GenerationMode.CANYON) {
            int depth = surfaceY - y;
            return CANYON_LAYERS[depth % CANYON_LAYERS.length];
        }

        if (biome == SpiritWorldBiome.GLACIAL_SHELF) {
            int depth = surfaceY - y;
            return GLACIAL_LAYERS[Math.min(depth, GLACIAL_LAYERS.length - 1)];
        }

        PatchType patch = getPatchAt(wx, wy, wz, biome);

        if (y == surfaceY) {
            if (biome == SpiritWorldBiome.FUNGAL_DEPTHS) {
                double glowNoise = improvedNoise(wx * 0.15 + 500, wz * 0.15 + 500);
                if (glowNoise > 0.65) return Blocks.SHROOMLIGHT.defaultBlockState();
            }
            return patch == PatchType.GRASS
                    ? Blocks.GRASS_BLOCK.defaultBlockState()
                    : patch.getBlock(rng);
        }
        if (patch == PatchType.GRASS && y > surfaceY - 3) {
            return Blocks.DIRT.defaultBlockState();
        }
        return patch.getBlock(rng);
    }

    private PatchType getPatchAt(int x, int y, int z, SpiritWorldBiome biome) {
        double large  = improvedNoise(x * PATCH_FREQ_LARGE,  z * PATCH_FREQ_LARGE);
        double medium = improvedNoise(x * PATCH_FREQ_MEDIUM + 1000, z * PATCH_FREQ_MEDIUM + 1000);
        double norm   = ((large * 0.80 + medium * 0.20) + 1.0) / 2.0;

        double[]    weights = biome.getPatchWeights();
        double      total   = 0;
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


    private IslandData getIslandData(int x, int z, RandomSource rng, SpiritWorldBiome biome) {
        IslandData main     = centralIsland(x, z);
        IslandData floating = switch (biome.mode) {
            case ARCHIPELAGO -> floatingArchipelago(x, z, biome);
            case SPIRE       -> floatingSpires(x, z, biome);
            case SCATTERED   -> floatingScattered(x, z, biome);
            case CONTINENTAL -> floatingContinental(x, z, biome);
            case PLATEAU     -> floatingPlateau(x, z, biome);
            case CANYON      -> floatingCanyon(x, z, biome);
            case CHAOS       -> floatingChaos(x, z, biome);
        };

        if (floating.density > main.density) return floating;
        if (main.density > 0)               return main;
        return IslandData.EMPTY;
    }


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
        return new IslandData(density, top, bottom, 0, 0, 0, 0, 0);
    }

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

            double sf  = 0.75 + cr.nextDouble() * 0.50;
            double raw = Mth.clamp(1.0 - dist(x, z, ix, iz) / (radius * sf), 0, 1);
            raw += improvedNoise(x * 0.055, z * 0.055) * 0.20
                    +  improvedNoise(x * 0.110 + 200, z * 0.110 + 200) * 0.12;
            raw = Mth.clamp(smoothstep(raw), 0, 1);
            if (raw <= 0) continue;

            int height = (int)(raw * p.heightVariation());
            int top    = WOOL_SEA_LEVEL + 2 + height;
            int bottom = WOOL_SEA_LEVEL - (int)(raw * 8);

            if (raw > best) { best = raw; bestTop = top; bestBot = bottom; }
        }
        return best > 0 ? new IslandData(best, bestTop, bestBot, 0, 0, 0, 0, 0) : IslandData.EMPTY;
    }

    private IslandData floatingSpires(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0;
        IslandData bestData = IslandData.EMPTY;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double distToCenter = dist(x, z, ix, iz);
            double raw = Mth.clamp(1.0 - distToCenter / radius, 0, 1);
            double density = Math.pow(raw, p.edgeSharpness());
            density = Mth.clamp(density, 0, 1);
            if (density <= 0) continue;

            int totalHeight = (int)(density * p.heightVariation());
            int bottom      = SPIRE_FLOOR_Y + 1 + yo / 4; // anchor near floor
            int shaftTop    = bottom + (int)(totalHeight * 0.40);
            int top         = bottom + totalHeight;
            double shaftRad = radius * 0.70;

            if (density > best) {
                best = density;
                bestData = new IslandData(density, top, bottom, (int) ix, (int) iz, shaftTop, shaftRad, 0);
            }
        }
        return bestData;
    }

    private IslandData floatingScattered(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0;
        IslandData bestData = IslandData.EMPTY;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double tiltAngle  = cr.nextDouble() * Math.PI * 2;
            int    tiltAmount = 8 + cr.nextInt(22);

            double dx   = x - ix, dz = z - iz;
            double proj = dx * Math.cos(tiltAngle) + dz * Math.sin(tiltAngle);
            double tiltFrac = Mth.clamp(proj / radius, -1.0, 1.0);
            int    tiltDY   = (int)(tiltFrac * tiltAmount);

            double raw = Mth.clamp(1.0 - dist(x, z, ix, iz) / radius, 0, 1);
            raw = Math.pow(raw, p.edgeSharpness());
            raw = Mth.clamp(raw, 0, 1);
            if (raw <= 0) continue;

            int baseTop    = p.baseHeight() + yo + (int)(raw * p.heightVariation());
            int baseBottom = baseTop - (int)(raw * p.heightVariation() * p.depthMultiplier());
            int top    = baseTop    + tiltDY;
            int bottom = baseBottom + tiltDY;

            if (raw > best) {
                best = raw;
                bestData = new IslandData(raw, top, bottom, 0, 0, 0, 0, 0);
            }
        }
        return bestData;
    }

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

            double d    = dist(x, z, ix, iz);
            double frac = d / radius;
            if (frac >= 1.0) continue;

            double shelfEnd = 1.0 - CONTINENTAL_SHELF_FRAC;
            double cliffEnd = shelfEnd - CONTINENTAL_CLIFF_FRAC;

            double heightFrac;
            if (frac >= shelfEnd) {
                double t = (frac - shelfEnd) / CONTINENTAL_SHELF_FRAC;
                heightFrac = 0.05 * (1.0 - t);
            } else if (frac >= cliffEnd) {
                double t = (frac - cliffEnd) / CONTINENTAL_CLIFF_FRAC;
                heightFrac = Mth.lerp(smoothstep(1.0 - t), 0.05, 0.85);
            } else {
                double interiorNoise = improvedNoise(x * 0.012, z * 0.012) * 0.10
                        +              improvedNoise(x * 0.030 + 300, z * 0.030 + 300) * 0.05;
                heightFrac = 0.85 + interiorNoise;
            }
            heightFrac = Mth.clamp(heightFrac, 0, 1);

            if (heightFrac > best) {
                best    = heightFrac;
                bestTop = p.baseHeight() + yo + (int)(heightFrac * p.heightVariation());
                bestBot = bestTop - (int)(heightFrac * p.heightVariation() * p.depthMultiplier());
            }
        }
        return best > 0 ? new IslandData(best, bestTop, bestBot, 0, 0, 0, 0, 0) : IslandData.EMPTY;
    }

    private IslandData floatingPlateau(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0;
        IslandData bestData = IslandData.EMPTY;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double d    = dist(x, z, ix, iz);
            double frac = d / radius;
            if (frac >= 1.0) continue;

            double edgeFrac     = Math.pow(frac, 2.0);
            double erosionNoise = (improvedNoise(x * 0.035, z * 0.035) + 1.0) * 0.5;
            if (erosionNoise < edgeFrac * PLATEAU_EROSION_STRENGTH) continue;

            double raw = Mth.clamp(1.0 - frac, 0, 1);
            raw = Math.pow(raw, p.edgeSharpness());
            raw = Mth.clamp(raw, 0, 1);
            if (raw <= 0) continue;

            if (raw > best) {
                best = raw;
                double surfaceJitter = improvedNoise(x * 0.040, z * 0.040) * p.heightVariation();
                int top    = p.baseHeight() + yo + (int) surfaceJitter;
                int bottom = top - 8 - (int)(raw * 8);
                bestData = new IslandData(raw, top, bottom, 0, 0, 0, 0, 0);
            }
        }
        return bestData;
    }

    private IslandData floatingCanyon(int x, int z, SpiritWorldBiome b) {
        SpiritWorldBiome.TerrainParams p = b.terrain;
        double best = 0;
        IslandData bestData = IslandData.EMPTY;

        int gx = Math.floorDiv(x, p.gridSize()), gz = Math.floorDiv(z, p.gridSize());
        for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
            int cx = gx + ox, cz = gz + oz;
            RandomSource cr = cellRng(cx, cz);
            if (cr.nextFloat() >= p.islandSpawnChance()) continue;

            int ix = cx * p.gridSize() + cr.nextInt(p.gridSize());
            int iz = cz * p.gridSize() + cr.nextInt(p.gridSize());
            int radius = p.minRadius() + cr.nextInt(p.maxRadius() - p.minRadius());
            int yo     = p.yOffsetMin() + cr.nextInt(p.yOffsetMax() - p.yOffsetMin());

            double flowAngle = cr.nextDouble() * Math.PI;

            double sf  = 0.75 + cr.nextDouble() * 0.50;
            double raw = Mth.clamp(1.0 - dist(x, z, ix, iz) / (radius * sf), 0, 1);
            raw += improvedNoise(x * 0.015, z * 0.015) * 0.25
                    +  improvedNoise(x * 0.035 + 400, z * 0.035 + 400) * 0.12;
            raw = Mth.clamp(Math.pow(raw, p.edgeSharpness()), 0, 1);
            if (raw <= 0) continue;

            if (raw > best) {
                best = raw;
                int top    = p.baseHeight() + yo + (int)(raw * p.heightVariation());
                int bottom = top - (int)(raw * p.heightVariation() * p.depthMultiplier());
                bestData = new IslandData(raw, top, bottom, 0, 0, 0, 0, flowAngle);
            }
        }
        return bestData;
    }

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


    @Override
    public void applyCarvers(WorldGenRegion r, long seed, RandomState rs, BiomeManager bm,
                             StructureManager sm, ChunkAccess chunk, GenerationStep.Carving c) {}

    @Override
    public void buildSurface(WorldGenRegion r, StructureManager sm, RandomState rs, ChunkAccess c) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion r) {}

    @Override public int getGenDepth() { return 384; }
    @Override public int getSeaLevel() { return 0;   }
    @Override public int getMinY()     { return 0;   }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState rs) {
        SpiritWorldBiome b = SpiritWorldBiome.getBiomeAt(x, z);
        IslandData d = getIslandData(x, z,
                RandomSource.create((long) x * 341_873_128_712L + z * 132_897_987_541L), b);
        return d.density > 0 ? d.top : level.getMinBuildHeight();
    }

    @Override
    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState,
                                 StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager) {
        super.createStructures(registryAccess, structureState, structureManager, chunk, templateManager);
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
        super.createReferences(level, structureManager, chunk);
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSetLookup,
                                                    RandomState randomState, long seed) {
        return super.createState(structureSetLookup, randomState, seed);
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
                } else if (b == SpiritWorldBiome.GLACIAL_SHELF) {
                    states[y] = GLACIAL_LAYERS[Math.min(data.top - y, GLACIAL_LAYERS.length - 1)];
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

    private record IslandData(
            double density,
            int    top,
            int    bottom,
            int    centerX,
            int    centerZ,
            int    shaftTop,
            double shaftRadius,
            double flowAngle
    ) {
        static final IslandData EMPTY = new IslandData(0, 0, 0, 0, 0, 0, 0, 0);
    }
}
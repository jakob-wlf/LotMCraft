package de.jakob.lotm.dimension.spirit_world;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;

public final class SpiritWorldStructures {

    public static final String MOD_ID = LOTMCraft.MOD_ID;

    private static final int   SPACING        = 7;
    private static final float MAJOR_CHANCE   = 0.45f;
    private static final boolean ENABLE_TRAPS = true;

    private SpiritWorldStructures() {}

    public static void generate(WorldGenLevel level, ChunkAccess chunk) {
        ChunkPos cp = chunk.getPos();
        long seed = level.getSeed();

        boolean majorHere = false;
        int cellX = Math.floorDiv(cp.x, SPACING);
        int cellZ = Math.floorDiv(cp.z, SPACING);
        RandomSource cellRng = RandomSource.create(
                seed ^ ((long) cellX * 341_873_128_712L) ^ ((long) cellZ * 132_897_987_541L) ^ 0x5EED_1234L);

        if (cellRng.nextFloat() < MAJOR_CHANCE) {
            int chosenX = cellX * SPACING + cellRng.nextInt(SPACING);
            int chosenZ = cellZ * SPACING + cellRng.nextInt(SPACING);
            if (chosenX == cp.x && chosenZ == cp.z) {
                majorHere = tryMajor(level, cp, seed);
            }
        }

        RandomSource rng = RandomSource.create(seed ^ (cp.toLong() * 0x9E37_79B9_7F4A_7C15L) ^ 0xABCDEFL);
        if (majorHere) return;

    }

    private static boolean tryMajor(WorldGenLevel level, ChunkPos cp, long seed) {
        RandomSource rng = RandomSource.create(seed ^ (cp.toLong() * 31L) ^ 0x7777_7777L);
        for (int attempt = 0; attempt < 4; attempt++) {
            int x = cp.getMinBlockX() + 4 + rng.nextInt(8);
            int z = cp.getMinBlockZ() + 4 + rng.nextInt(8);
            int y = surfaceY(level, x, z);
            if (y < 2) continue;

            SpiritWorldBiome biome = SpiritWorldBiome.getBiomeAt(x, z);
            if (!isFlatEnough(level, x, y, z, radius(biome))) continue;

            B b = new B(level, rng, cp, new BlockPos(x, y, z));
            build(b, biome);
            return true;
        }
        return false;
    }

    private static void tryObelisk(WorldGenLevel level, ChunkPos cp, RandomSource rng) {
        int x = cp.getMinBlockX() + 3 + rng.nextInt(10);
        int z = cp.getMinBlockZ() + 3 + rng.nextInt(10);
        int y = surfaceY(level, x, z);
        if (y < 2) return;

        Block[] pal = palette(SpiritWorldBiome.getBiomeAt(x, z));
        B b = new B(level, rng, cp, new BlockPos(x, y, z));
        int h = 4 + rng.nextInt(4);
        for (int i = 1; i <= h; i++) b.set(0, i, 0, pal[0]);
        b.set(0, h + 1, 0, pal[1]);
        b.set( 1, 1, 0, pal[0]);
        b.set(-1, 1, 0, pal[0]);
        b.set(0, 1,  1, pal[0]);
        b.set(0, 1, -1, pal[0]);
    }

    private static void tryCache(WorldGenLevel level, ChunkPos cp, RandomSource rng) {
        int x = cp.getMinBlockX() + 3 + rng.nextInt(10);
        int z = cp.getMinBlockZ() + 3 + rng.nextInt(10);
        int y = surfaceY(level, x, z);
        if (y < 6) return;
        if (level.getBlockState(new BlockPos(x, y - 2, z)).isAir()) return;

        Block[] pal = palette(SpiritWorldBiome.getBiomeAt(x, z));
        B b = new B(level, rng, cp, new BlockPos(x, y, z));
        b.set(0, 1, 0, pal[0]);
        b.set(0, 2, 0, pal[1]);
        b.chest(0, -2, 0, Direction.NORTH, "cache");
    }


    private static int surfaceY(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(x, 0, z);
        for (int y = level.getMaxBuildHeight() - 1; y > 1; y--) {
            m.setY(y);
            BlockState s = level.getBlockState(m);
            if (s.isAir()) continue;
            if (!s.getFluidState().isEmpty()) return -1;
            return y;
        }
        return -1;
    }

    private static boolean isFlatEnough(WorldGenLevel level, int x, int y, int z, int r) {
        int good = 0;
        int[][] pts = {{r, 0}, {-r, 0}, {0, r}, {0, -r}};
        for (int[] p : pts) {
            int sy = surfaceY(level, x + p[0], z + p[1]);
            if (sy >= 0 && Math.abs(sy - y) <= 8) good++;
        }
        return good >= 3;
    }

    private static int radius(SpiritWorldBiome biome) {
        return switch (biome) {
            case WOOL_MEADOWS -> 6;
            case CRYSTALLINE_PEAKS -> 6;
            case VOID_GARDENS -> 6;
            case EMBER_WASTES -> 8;
            case QUARTZ_FLATS -> 9;
            case TERRACOTTA_CANYON -> 9;
            case FUNGAL_DEPTHS -> 7;
            case GILDED_RUINS -> 8;
            case GLACIAL_SHELF -> 9;
            case SPIRIT_WORLD_WASTES -> 8;
            case MIRE_HOLLOW -> 6;
            case BONE_STEPPES -> 11;
            case UNDERWORLD_PATCH -> 12;
        };
    }

    private static Block[] palette(SpiritWorldBiome biome) {
        return switch (biome) {
            case WOOL_MEADOWS -> new Block[]{Blocks.MAGENTA_WOOL, Blocks.GLOWSTONE};
            case CRYSTALLINE_PEAKS -> new Block[]{Blocks.AMETHYST_BLOCK, Blocks.AMETHYST_CLUSTER};
            case VOID_GARDENS -> new Block[]{Blocks.OBSIDIAN, Blocks.END_ROD};
            case EMBER_WASTES -> new Block[]{Blocks.BLACKSTONE, Blocks.MAGMA_BLOCK};
            case QUARTZ_FLATS -> new Block[]{Blocks.QUARTZ_PILLAR, Blocks.SEA_LANTERN};
            case TERRACOTTA_CANYON -> new Block[]{Blocks.ORANGE_TERRACOTTA, Blocks.ORANGE_GLAZED_TERRACOTTA};
            case FUNGAL_DEPTHS -> new Block[]{Blocks.MUSHROOM_STEM, Blocks.SHROOMLIGHT};
            case GILDED_RUINS -> new Block[]{Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.GOLD_BLOCK};
            case GLACIAL_SHELF -> new Block[]{Blocks.PACKED_ICE, Blocks.BLUE_ICE};
            case SPIRIT_WORLD_WASTES -> new Block[]{Blocks.DEEPSLATE_BRICKS, Blocks.SOUL_LANTERN};
            case MIRE_HOLLOW -> new Block[]{Blocks.MUD_BRICKS, Blocks.VERDANT_FROGLIGHT};
            case BONE_STEPPES -> new Block[]{Blocks.BONE_BLOCK, Blocks.SKELETON_SKULL};
            case UNDERWORLD_PATCH -> new Block[]{Blocks.BONE_BLOCK, Blocks.SKELETON_SKULL};
        };
    }

    private static void build(B b, SpiritWorldBiome biome) {
        switch (biome) {
            case WOOL_MEADOWS -> woolPavilion(b);
            case CRYSTALLINE_PEAKS -> crystalBeacon(b);
            case VOID_GARDENS -> voidAltar(b);
            case EMBER_WASTES -> emberForge(b);
            case QUARTZ_FLATS -> quartzColonnade(b);
            case TERRACOTTA_CANYON -> ziggurat(b);
            case FUNGAL_DEPTHS -> mushroomHut(b);
            case GILDED_RUINS -> gildedVault(b);
            case GLACIAL_SHELF -> frozenShrine(b);
            case SPIRIT_WORLD_WASTES -> ritualCircle(b);
            case MIRE_HOLLOW -> mireHut(b);
            case BONE_STEPPES -> boneRibcage(b);
            default -> { }
        }
        
        b.finish();
    }

    private static final Block[] WOOLS = {
            Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.CYAN_WOOL,
            Blocks.LIGHT_BLUE_WOOL, Blocks.PURPLE_WOOL, Blocks.MAGENTA_WOOL, Blocks.PINK_WOOL
    };

    private static final Block[] CARPETS = {
            Blocks.RED_CARPET, Blocks.ORANGE_CARPET, Blocks.YELLOW_CARPET, Blocks.LIME_CARPET, Blocks.CYAN_CARPET,
            Blocks.LIGHT_BLUE_CARPET, Blocks.PURPLE_CARPET, Blocks.MAGENTA_CARPET, Blocks.PINK_CARPET
    };


    
    
    
    private static void woolPavilion(B b) {
        b.clear(7, 14);
        b.foundation(6, Blocks.WHITE_WOOL);

        
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++) {
                int m = Math.max(Math.abs(x), Math.abs(z));
                int d = x * x + z * z;
                Block blk;
                if (m == 6) blk = WOOLS[Math.floorMod(x + z, WOOLS.length)];
                else if (m == 5) blk = Blocks.WHITE_WOOL;
                else if (d <= 5) blk = ((x + z) & 1) == 0 ? Blocks.MAGENTA_WOOL : Blocks.PINK_WOOL;
                else if (d >= 10 && d <= 13) blk = Blocks.PINK_WOOL;
                else blk = ((x + z) & 1) == 0 ? Blocks.LIGHT_GRAY_WOOL : Blocks.WHITE_WOOL;
                b.set(x, 0, z, blk);
            }
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2)
                b.set(2 * sx, 0, 2 * sz, Blocks.GLOWSTONE);
        b.set(3, 0, 0, Blocks.GLOWSTONE);
        b.set(-3, 0, 0, Blocks.GLOWSTONE);
        b.set(0, 0, -3, Blocks.GLOWSTONE);
        b.set(0, 0, 3, Blocks.GLOWSTONE);

        
        for (int x = -5; x <= 5; x++)
            for (int z = -5; z <= 5; z++)
                if (Math.max(Math.abs(x), Math.abs(z)) == 5 && !(z == 5 && Math.abs(x) <= 1))
                    b.set(x, 1, z, CARPETS[Math.floorMod(x + z + 3, CARPETS.length)]);
        for (int z = 2; z <= 6; z++) {
            b.set(0, 1, z, Blocks.PINK_CARPET);
            b.set(-1, 1, z, Blocks.WHITE_CARPET);
            b.set(1, 1, z, Blocks.WHITE_CARPET);
        }

        
        for (int[] p : new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}}) {
            Block c = b.pick(WOOLS);
            b.set(p[0], 1, p[1], Blocks.WHITE_WOOL);
            b.set(p[0], 2, p[1], c);
            b.set(p[0], 3, p[1], Blocks.WHITE_WOOL);
            b.set(p[0], 4, p[1], c);
            b.set(p[0], 5, p[1], Blocks.WHITE_WOOL);
        }
        
        for (int[] p : new int[][]{{-4, 0}, {4, 0}, {0, -4}}) {
            Block c = b.pick(WOOLS);
            b.set(p[0], 1, p[1], Blocks.WHITE_WOOL);
            b.set(p[0], 2, p[1], c);
            b.set(p[0], 3, p[1], Blocks.WHITE_WOOL);
            b.set(p[0], 4, p[1], c);
        }
        
        for (int sx = -2; sx <= 2; sx += 4) {
            Block c = b.pick(WOOLS);
            b.set(sx, 1, 4, Blocks.WHITE_WOOL);
            b.set(sx, 2, 4, c);
            b.set(sx, 3, 4, Blocks.WHITE_WOOL);
            b.set(sx, 4, 4, c);
        }
        
        b.box(-3, 5, -4, 3, 5, -4, b.pick(WOOLS));
        b.box(-4, 5, -3, -4, 5, 3, b.pick(WOOLS));
        b.box(4, 5, -3, 4, 5, 3, b.pick(WOOLS));
        b.box(-3, 5, 4, 3, 5, 4, b.pick(WOOLS));
        b.box(-1, 5, 4, 1, 5, 4, Blocks.PINK_WOOL);
        
        b.candle(-4, 5, -4, Blocks.WHITE_CANDLE, 2);
        b.candle(4, 5, -4, Blocks.WHITE_CANDLE, 2);

        
        for (int t = 0; t < 5; t++) {
            int r = 5 - t, y = 6 + t;
            for (int x = -r; x <= r; x++)
                for (int z = -r; z <= r; z++) {
                    int sector = Math.abs(x) >= Math.abs(z) ? (x >= 0 ? 0 : 1) : (z >= 0 ? 2 : 3);
                    int tan = Math.abs(x) >= Math.abs(z) ? z : x;
                    Block blk = Math.floorMod(tan, 2) == 0 ? WOOLS[sector * 2 % WOOLS.length] : Blocks.WHITE_WOOL;
                    b.set(x, y, z, blk);
                }
        }
        
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++)
                if (Math.max(Math.abs(x), Math.abs(z)) == 6) {
                    if (((x + z) & 1) == 0) b.set(x, 6, z, WOOLS[Math.floorMod(x + z, WOOLS.length)]);
                    else b.set(x, 6, z, Blocks.WHITE_CARPET);
                }
        b.set(0, 11, 0, Blocks.SEA_LANTERN);
        b.set(0, 12, 0, Blocks.END_ROD);

        
        b.hang(0, 5, 0, 2, Blocks.LANTERN);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2)
                b.hang(2 * sx, 5, 2 * sz, 1, Blocks.LANTERN);

        
        for (int sx = -6; sx <= 6; sx += 12)
            for (int sz = -6; sz <= 6; sz += 12) {
                b.set(sx, 1, sz, b.pick(WOOLS));
                b.set(sx, 2, sz, Blocks.WHITE_WOOL);
                b.candle(sx, 3, sz, Blocks.YELLOW_CANDLE, 3);
            }

        
        b.candle(-1, 1, -1, Blocks.PINK_CANDLE, 3);
        b.candle(1, 1, -1, Blocks.PINK_CANDLE, 3);
        b.candle(-1, 1, 1, Blocks.PINK_CANDLE, 3);
        b.candle(1, 1, 1, Blocks.PINK_CANDLE, 3);
        b.chest(0, 1, 0, Direction.SOUTH, "wool_pavilion");
    }

    
    
    
    private static void crystalBeacon(B b) {
        b.clear(6, 28);
        b.foundation(5, Blocks.CALCITE);

        
        for (int x = -5; x <= 5; x++)
            for (int z = -5; z <= 5; z++) {
                int d = x * x + z * z;
                if (d > 30) continue;
                Block blk;
                if (d >= 26) blk = Blocks.SMOOTH_BASALT;
                else if (d >= 20) blk = Blocks.AMETHYST_BLOCK;
                else blk = b.rng.nextInt(5) == 0 ? Blocks.AMETHYST_BLOCK : Blocks.CALCITE;
                b.set(x, 0, z, blk);
            }

        
        for (int h = 0; h <= 19; h++) {
            int rad = h < 5 ? 3 : h < 12 ? 2 : 1;
            boolean band = h % 6 == 5;
            for (int x = -rad; x <= rad; x++)
                for (int z = -rad; z <= rad; z++) {
                    if (x * x + z * z > rad * rad + 1) continue;
                    Block blk;
                    if (band) blk = Blocks.CALCITE;
                    else {
                        int roll = b.rng.nextInt(20);
                        blk = roll < 13 ? Blocks.AMETHYST_BLOCK : roll < 16 ? Blocks.BUDDING_AMETHYST
                                : roll < 18 ? Blocks.CALCITE : Blocks.AMETHYST_BLOCK;
                    }
                    b.set(x, h, z, blk);
                }
        }
        
        for (int x = -3; x <= 3; x++)
            for (int z = -3; z <= 3; z++) {
                int d = x * x + z * z;
                if (d > 5 && d <= 10 && b.chance(0.5f)) b.set(x, 5, z, Blocks.AMETHYST_CLUSTER);
                if (d > 2 && d <= 5 && b.chance(0.5f)) b.set(x, 12, z, Blocks.AMETHYST_CLUSTER);
            }

        
        b.box(-1, 1, -1, 1, 4, 1, Blocks.AIR);
        b.box(0, 1, -3, 0, 2, -2, Blocks.AIR);
        b.box(-1, 0, -1, 1, 0, 1, Blocks.AMETHYST_BLOCK);
        b.set(0, 4, 0, Blocks.SEA_LANTERN);
        b.set(-1, 1, -1, Blocks.AMETHYST_CLUSTER);
        b.set(1, 1, -1, Blocks.AMETHYST_CLUSTER);
        b.set(-1, 1, 1, Blocks.AMETHYST_CLUSTER);
        b.set(1, 1, 1, Blocks.AMETHYST_CLUSTER);
        b.candle(-1, 1, 0, Blocks.PURPLE_CANDLE, 3);
        b.candle(1, 1, 0, Blocks.PURPLE_CANDLE, 3);
        
        for (int y = 2; y <= 3; y++) {
            b.set(2, y, 0, Blocks.AIR);  b.set(3, y, 0, Blocks.PURPLE_STAINED_GLASS);
            b.set(-2, y, 0, Blocks.AIR); b.set(-3, y, 0, Blocks.PURPLE_STAINED_GLASS);
            b.set(0, y, 2, Blocks.AIR);  b.set(0, y, 3, Blocks.PURPLE_STAINED_GLASS);
        }

        
        b.box(-1, 5, -1, 1, 5, 1, Blocks.CALCITE);
        b.box(-1, 6, -1, 1, 11, 1, Blocks.AIR);
        b.box(-1, 6, -1, 1, 6, 1, Blocks.BUDDING_AMETHYST);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2)
                b.set(sx, 7, sz, Blocks.AMETHYST_CLUSTER);
        b.set(0, 9, 0, Blocks.SEA_LANTERN);
        for (int y = 8; y <= 9; y++) {
            b.set(2, y, 0, Blocks.PURPLE_STAINED_GLASS);
            b.set(-2, y, 0, Blocks.PURPLE_STAINED_GLASS);
            b.set(0, y, 2, Blocks.PURPLE_STAINED_GLASS);
            b.set(0, y, -2, Blocks.PURPLE_STAINED_GLASS);
        }
        
        b.box(0, 12, 0, 0, 18, 0, Blocks.AIR);
        b.set(0, 16, 0, Blocks.SEA_LANTERN);
        for (int y = 15; y <= 16; y++) {
            b.set(1, y, 0, Blocks.TINTED_GLASS);
            b.set(-1, y, 0, Blocks.TINTED_GLASS);
            b.set(0, y, 1, Blocks.TINTED_GLASS);
            b.set(0, y, -1, Blocks.TINTED_GLASS);
        }

        
        b.set(0, 20, 0, Blocks.SEA_LANTERN);
        b.set(0, 21, 0, Blocks.AMETHYST_CLUSTER);
        b.set(1, 20, 0, Blocks.AMETHYST_CLUSTER);
        b.set(-1, 20, 0, Blocks.AMETHYST_CLUSTER);
        b.set(0, 20, 1, Blocks.AMETHYST_CLUSTER);
        b.set(0, 20, -1, Blocks.AMETHYST_CLUSTER);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2)
                b.set(sx, 20, sz, Blocks.MEDIUM_AMETHYST_BUD);

        
        for (int sx = -1; sx <= 1; sx += 2) b.box(sx, 1, -4, sx, 3, -4, Blocks.CALCITE);
        b.set(0, 3, -4, Blocks.CALCITE);
        b.set(-1, 4, -4, Blocks.AMETHYST_CLUSTER);
        b.set(0, 4, -4, Blocks.AMETHYST_CLUSTER);
        b.set(1, 4, -4, Blocks.AMETHYST_CLUSTER);
        for (int z = -6; z <= -4; z++) b.set(0, 0, z, Blocks.SMOOTH_BASALT);
        b.set(-1, 0, -5, Blocks.AMETHYST_BLOCK);
        b.set(1, 0, -5, Blocks.AMETHYST_BLOCK);

        
        for (int k = 0; k < 6; k++) {
            double a = (k + b.rng.nextDouble() * 0.5) * Math.PI / 3.0;
            int x = (int) Math.round(5 * Math.cos(a));
            int z = (int) Math.round(5 * Math.sin(a));
            if (Math.abs(x) <= 1 && z < 0) continue;
            int h = 3 + b.rng.nextInt(4);
            int sx = Integer.signum(x), sz = Integer.signum(z);
            b.support(x, z, -1, Blocks.AMETHYST_BLOCK);
            for (int i = 0; i <= h; i++) {
                int lean = i / 3;
                b.set(x + (Math.abs(x) >= Math.abs(z) ? sx * lean : 0), i,
                        z + (Math.abs(z) > Math.abs(x) ? sz * lean : 0), Blocks.AMETHYST_BLOCK);
            }
            int lean = h / 3;
            b.set(x + (Math.abs(x) >= Math.abs(z) ? sx * lean : 0), h + 1,
                    z + (Math.abs(z) > Math.abs(x) ? sz * lean : 0), Blocks.AMETHYST_CLUSTER);
            b.box(x + sx, 0, z, x + sx, h / 2, z, Blocks.BUDDING_AMETHYST);
            b.set(x + sx, h / 2 + 1, z, Blocks.LARGE_AMETHYST_BUD);
            b.set(x, 1, z + sz, Blocks.MEDIUM_AMETHYST_BUD);
        }

        
        BlockState down = Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN);
        for (int k = 0; k < 6; k++) {
            double a = k * Math.PI / 3.0 + 0.5;
            int x = (int) Math.round(4.2 * Math.cos(a));
            int z = (int) Math.round(4.2 * Math.sin(a));
            int y = 9 + (k % 3) * 3 + b.rng.nextInt(2);
            b.set(x, y, z, Blocks.AMETHYST_BLOCK);
            b.set(x, y + 1, z, Blocks.AMETHYST_CLUSTER);
            b.set(x, y - 1, z, down);
        }

        for (int i = -4; i <= 4; i++) {
            if (b.rng.nextFloat() < 0.35f) b.set(i, 1, 4, Blocks.SMALL_AMETHYST_BUD);
            if (Math.abs(i) > 1 && b.rng.nextFloat() < 0.35f) b.set(i, 1, -4, Blocks.SMALL_AMETHYST_BUD);
        }
        b.chest(0, 1, 0, Direction.NORTH, "crystal_beacon");
    }

    
    
    
    private static void voidAltar(B b) {
        b.clear(7, 12);
        b.foundationDisc(6, Blocks.OBSIDIAN);

        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++) {
                int d = x * x + z * z;
                if (d > 36) continue;
                boolean axis = x == 0 || z == 0;
                boolean diag = Math.abs(x) == Math.abs(z);
                Block blk;
                if (d <= 4) blk = Blocks.CRYING_OBSIDIAN;
                else if (d >= 30) blk = ((x + z) & 1) == 0 ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_BLACKSTONE;
                else if (diag) blk = Blocks.CRYING_OBSIDIAN;
                else if (axis) blk = Blocks.PURPUR_BLOCK;
                else if (d >= 12 && d <= 20) blk = Blocks.PURPUR_BLOCK;
                else blk = b.rng.nextInt(6) == 0 ? Blocks.SCULK : Blocks.OBSIDIAN;
                b.set(x, 0, z, blk);
            }

        b.set(3, 0, 3, Blocks.SCULK);
        b.set(-3, 0, -3, Blocks.SCULK);
        b.set(3, 1, 3, Blocks.SCULK_CATALYST);
        b.set(-3, 1, -3, Blocks.SCULK_CATALYST);

        
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++) {
                int d = x * x + z * z;
                if (d < 31 || d > 36) continue;
                if (Math.abs(x) <= 1 || Math.abs(z) <= 1) continue;
                if (Math.abs(x) == Math.abs(z)) {
                    b.set(x, 1, z, Blocks.CRYING_OBSIDIAN);
                    b.set(x, 2, z, Blocks.END_ROD);
                } else b.set(x, 1, z, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
            }

        
        for (int[] p : new int[][]{{4, 0}, {-4, 0}, {0, 4}, {0, -4}}) {
            b.flare(p[0], p[1], 1, Blocks.PURPUR_STAIRS);
            b.set(p[0], 1, p[1], Blocks.PURPUR_PILLAR);
            b.set(p[0], 2, p[1], Blocks.OBSIDIAN);
            b.set(p[0], 3, p[1], Blocks.CRYING_OBSIDIAN);
            b.set(p[0], 4, p[1], Blocks.OBSIDIAN);
            b.set(p[0], 5, p[1], Blocks.PURPUR_PILLAR);
            b.set(p[0], 6, p[1], Blocks.END_ROD);
        }

        
        for (int sx = -4; sx <= 4; sx += 8)
            for (int sz = -4; sz <= 4; sz += 8) {
                b.set(sx, 4, sz, Blocks.OBSIDIAN);
                b.set(sx, 5, sz, Blocks.CRYING_OBSIDIAN);
                b.set(sx, 6, sz, Blocks.END_ROD);
                b.set(sx, 3, sz, Blocks.PURPUR_SLAB);
            }

        
        b.box(-1, 1, -1, 1, 1, 1, Blocks.OBSIDIAN);
        b.set(0, 1, 0, Blocks.CRYING_OBSIDIAN);
        b.stair(0, 1, -2, Blocks.PURPUR_STAIRS, Direction.SOUTH, false);
        b.stair(0, 1, 2, Blocks.PURPUR_STAIRS, Direction.NORTH, false);
        b.stair(-2, 1, 0, Blocks.PURPUR_STAIRS, Direction.EAST, false);
        b.stair(2, 1, 0, Blocks.PURPUR_STAIRS, Direction.WEST, false);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                b.stair(sx * 2, 1, sz * 2, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                        sx > 0 ? Direction.WEST : Direction.EAST, false);
                b.set(sx, 2, sz, Blocks.END_ROD);
            }
        b.chest(0, 2, 0, Direction.SOUTH, "void_altar");
    }

    
    
    
    private static void emberForge(B b) {
        b.clear(9, 16);
        b.foundation(7, Blocks.NETHER_BRICKS);
        for (int x = -7; x <= 7; x++)
            for (int z = -7; z <= 7; z++) {
                int r = b.rng.nextInt(10);
                Block blk = r == 0 ? Blocks.MAGMA_BLOCK : r < 3 ? Blocks.RED_NETHER_BRICKS
                        : r < 5 ? Blocks.CRACKED_NETHER_BRICKS : Blocks.NETHER_BRICKS;
                b.set(x, 0, z, blk);
            }
        
        for (int z = -9; z <= -2; z++) {
            b.set(0, 0, z, Blocks.RED_NETHER_BRICKS);
            if ((z & 1) == 0) { b.set(-1, 0, z, Blocks.CHISELED_NETHER_BRICKS); b.set(1, 0, z, Blocks.CHISELED_NETHER_BRICKS); }
        }
        b.box(-1, 0, -1, 1, 0, 1, Blocks.CHISELED_NETHER_BRICKS);
        b.set(0, 0, 0, Blocks.RED_NETHER_BRICKS);

        
        b.ruinedWalls(-7, 1, -7, 7, 5, 7, 0.2f,
                Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.RED_NETHER_BRICKS);
        for (int i = -7; i <= 7; i++) {
            if ((i & 1) == 0) {
                b.set(i, 6, -7, Blocks.NETHER_BRICKS);
                b.set(i, 6, 7, Blocks.NETHER_BRICKS);
                b.set(-7, 6, i, Blocks.NETHER_BRICKS);
                b.set(7, 6, i, Blocks.NETHER_BRICKS);
            } else {
                b.slab(i, 6, -7, Blocks.NETHER_BRICK_SLAB, false);
                b.slab(i, 6, 7, Blocks.NETHER_BRICK_SLAB, false);
                b.slab(-7, 6, i, Blocks.NETHER_BRICK_SLAB, false);
                b.slab(7, 6, i, Blocks.NETHER_BRICK_SLAB, false);
            }
        }
        for (int i = -4; i <= 4; i += 8) {
            b.set(i, 3, -7, Blocks.SHROOMLIGHT);
            b.set(i, 3, 7, Blocks.SHROOMLIGHT);
            b.set(-7, 3, i, Blocks.SHROOMLIGHT);
            b.set(7, 3, i, Blocks.SHROOMLIGHT);
        }

        
        for (int[] p : new int[][]{{-6, -4}, {-6, 4}, {6, -4}, {6, 4}, {0, 6}}) {
            b.box(p[0], 1, p[1], p[0], 3, p[1], Blocks.NETHER_BRICKS);
            b.set(p[0], 4, p[1], Blocks.CHISELED_NETHER_BRICKS);
        }
        
        b.beam(-6, 5, -4, 13, Direction.Axis.X, Blocks.CRIMSON_STEM);
        b.beam(-6, 5, 4, 13, Direction.Axis.X, Blocks.CRIMSON_STEM);
        b.beam(0, 5, -7, 15, Direction.Axis.Z, Blocks.CRIMSON_STEM);
        b.hang(0, 4, 0, 1, Blocks.LANTERN);
        b.hang(-4, 4, -4, 1, Blocks.LANTERN);
        b.hang(4, 4, -4, 1, Blocks.LANTERN);
        b.hang(-4, 4, 4, 1, Blocks.LANTERN);

        
        b.box(-1, 1, -7, 1, 3, -7, Blocks.AIR);
        b.box(-2, 1, -7, -2, 4, -7, Blocks.CHISELED_NETHER_BRICKS);
        b.box(2, 1, -7, 2, 4, -7, Blocks.CHISELED_NETHER_BRICKS);
        b.box(-1, 4, -7, 1, 4, -7, Blocks.RED_NETHER_BRICKS);
        b.stair(-1, 3, -7, Blocks.NETHER_BRICK_STAIRS, Direction.EAST, true);
        b.stair(1, 3, -7, Blocks.NETHER_BRICK_STAIRS, Direction.WEST, true);
        for (int sx = -3; sx <= 3; sx += 6) {
            b.support(sx, -9, -1, Blocks.NETHER_BRICKS);
            b.set(sx, 0, -9, Blocks.CHISELED_NETHER_BRICKS);
            b.set(sx, 1, -9, Blocks.NETHER_BRICK_WALL);
            b.set(sx, 2, -9, Blocks.CAMPFIRE);
        }
        for (int z = -9; z <= -8; z++) b.set(0, 0, z, Blocks.RED_NETHER_BRICKS);

        
        for (int sx = -7; sx <= 7; sx += 14)
            for (int sz = -7; sz <= 7; sz += 14) {
                int ox = Integer.signum(sx), oz = Integer.signum(sz);
                for (int x = sx - 1; x <= sx + 1; x++)
                    for (int z = sz - 1; z <= sz + 1; z++)
                        if (Math.abs(x) > 7 || Math.abs(z) > 7) b.support(x, z, 0, Blocks.RED_NETHER_BRICKS);
                b.walls(sx - 1, 1, sz - 1, sx + 1, 8, sz + 1, Blocks.RED_NETHER_BRICKS);
                b.box(sx, 1, sz, sx, 8, sz, Blocks.AIR);
                b.walls(sx - 1, 4, sz - 1, sx + 1, 4, sz + 1, Blocks.CHISELED_NETHER_BRICKS);
                b.walls(sx - 1, 8, sz - 1, sx + 1, 8, sz + 1, Blocks.CHISELED_NETHER_BRICKS);
                b.set(sx + ox, 6, sz, Blocks.AIR);
                b.set(sx, 6, sz + oz, Blocks.AIR);
                b.box(sx - ox, 1, sz, sx - ox, 2, sz, Blocks.AIR);
                b.set(sx + ox, 2, sz, Blocks.SHROOMLIGHT);
                b.hang(sx, 8, sz, 1, Blocks.LANTERN);
                b.roof(sx, sz, 9, 2, 3, Blocks.NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICKS, 0f);
                b.set(sx, 12, sz, Blocks.SHROOMLIGHT);
            }

        
        b.box(2, 0, 2, 4, 0, 4, Blocks.LAVA);
        for (int x = 1; x <= 5; x++)
            for (int z = 1; z <= 5; z++)
                if (x == 1 || x == 5 || z == 1 || z == 5)
                    b.slab(x, 1, z, Blocks.NETHER_BRICK_SLAB, false);
        for (int sx = 1; sx <= 5; sx += 4)
            for (int sz = 1; sz <= 5; sz += 4) {
                b.box(sx, 1, sz, sx, 2, sz, Blocks.NETHER_BRICK_WALL);
                b.set(sx, 3, sz, Blocks.LANTERN);
            }
        b.set(3, 4, 4, Blocks.CHAIN);
        b.set(3, 3, 4, Blocks.CHAIN);
        b.set(3, 2, 4, Blocks.LAVA_CAULDRON);

        
        b.box(-6, 1, -3, -6, 4, -1, Blocks.NETHER_BRICKS);
        b.box(-6, 5, -2, -6, 9, -2, Blocks.NETHER_BRICKS);
        b.set(-6, 0, -2, Blocks.NETHERRACK);
        b.set(-6, 1, -2, Blocks.FIRE);
        b.set(-6, 2, -2, Blocks.AIR);
        b.set(-6, 10, -2, Blocks.CAMPFIRE);
        b.facing(-5, 1, -3, Blocks.BLAST_FURNACE, Direction.EAST);
        b.facing(-5, 1, -1, Blocks.ANVIL, Direction.NORTH);
        b.set(-5, 1, 0, Blocks.SMITHING_TABLE);
        b.set(-5, 1, 1, Blocks.LAVA_CAULDRON);

        
        for (int[] p : new int[][]{{-3, 4}, {3, -4}, {-3, -4}}) {
            b.set(p[0], 0, p[1], Blocks.NETHERRACK);
            b.set(p[0], 1, p[1], Blocks.FIRE);
            b.stair(p[0] + 1, 1, p[1], Blocks.NETHER_BRICK_STAIRS, Direction.WEST, false);
            b.stair(p[0] - 1, 1, p[1], Blocks.NETHER_BRICK_STAIRS, Direction.EAST, false);
            b.stair(p[0], 1, p[1] + 1, Blocks.NETHER_BRICK_STAIRS, Direction.NORTH, false);
            b.stair(p[0], 1, p[1] - 1, Blocks.NETHER_BRICK_STAIRS, Direction.SOUTH, false);
        }

        
        b.scatter(10, 6, Blocks.BLACKSTONE, Blocks.MAGMA_BLOCK, Blocks.NETHER_BRICK_SLAB, Blocks.BASALT);

        b.chest(-5, 1, 5, Direction.EAST, "ember_forge");
        b.chest(5, 1, -5, Direction.WEST, "ember_forge");
    }

    
    
    
    private static void quartzColonnade(B b) {
        b.clear(9, 12);
        b.foundationDisc(8, Blocks.QUARTZ_BRICKS);
        for (int x = -8; x <= 8; x++)
            for (int z = -8; z <= 8; z++) {
                int d = x * x + z * z;
                if (d > 64) continue;
                Block blk;
                if (d >= 57) blk = ((x + z) & 1) == 0 ? Blocks.QUARTZ_BRICKS : Blocks.SMOOTH_QUARTZ;
                else if (d >= 50) blk = Blocks.SMOOTH_QUARTZ;
                else if (d >= 36) blk = Blocks.QUARTZ_BRICKS;
                else if (d >= 20) blk = Blocks.SMOOTH_QUARTZ;
                else if (d >= 13) blk = Blocks.QUARTZ_BRICKS;
                else blk = Blocks.CHISELED_QUARTZ_BLOCK;
                if ((x == 0 || z == 0) && d >= 13 && d <= 35 && ((x + z) & 1) == 0) blk = Blocks.CHISELED_QUARTZ_BLOCK;
                b.set(x, 0, z, blk);
            }
        b.set(5, 0, 0, Blocks.SEA_LANTERN);
        b.set(-5, 0, 0, Blocks.SEA_LANTERN);
        b.set(0, 0, 5, Blocks.SEA_LANTERN);
        b.set(0, 0, -5, Blocks.SEA_LANTERN);

        
        int n = 8;
        int[] cx = new int[n], cz = new int[n];
        boolean[] broken = new boolean[n];
        for (int k = 0; k < n; k++) {
            double a = k * Math.PI / 4.0;
            cx[k] = (int) Math.round(7 * Math.cos(a));
            cz[k] = (int) Math.round(7 * Math.sin(a));
            broken[k] = b.rng.nextFloat() < 0.3f;
        }
        for (int k = 0; k < n; k++) {
            int px = cx[k], pz = cz[k];
            int h = broken[k] ? 2 + b.rng.nextInt(2) : 5;
            b.set(px, 1, pz, Blocks.CHISELED_QUARTZ_BLOCK);
            b.box(px, 2, pz, px, h, pz, Blocks.QUARTZ_PILLAR);
            if (!broken[k]) {
                b.set(px, h + 1, pz, Blocks.CHISELED_QUARTZ_BLOCK);
                b.slab(px, h + 2, pz, Blocks.QUARTZ_SLAB, false);
            } else {
                b.stair(px, h + 1, pz, Blocks.QUARTZ_STAIRS, b.randomDir(), false);
                for (int i = 0; i < 3; i++) {
                    int rx = px + b.rng.nextInt(3) - 1, rz = pz + b.rng.nextInt(3) - 1;
                    if ((rx != px || rz != pz) && b.get(rx, 1, rz).isAir()) {
                        if (b.rng.nextBoolean()) b.slab(rx, 1, rz, Blocks.QUARTZ_SLAB, false);
                        else b.stair(rx, 1, rz, Blocks.QUARTZ_STAIRS, b.randomDir(), false);
                    }
                }
            }
        }
        for (int k = 0; k < n; k++) {
            int j = (k + 1) % n;
            if (broken[k] || broken[j]) continue;
            int dx = cx[j] - cx[k], dz = cz[j] - cz[k];
            int steps = Math.max(Math.abs(dx), Math.abs(dz));
            for (int t = 1; t < steps; t++) {
                int x = cx[k] + Math.round(dx * (float) t / steps);
                int z = cz[k] + Math.round(dz * (float) t / steps);
                b.set(x, 6, z, Blocks.SMOOTH_QUARTZ);
                b.slab(x, 7, z, Blocks.QUARTZ_SLAB, false);
                if (t == steps / 2) b.hang(x, 5, z, 1, Blocks.LANTERN);
            }
        }

        
        b.box(-3, 1, -3, 3, 1, 3, Blocks.SMOOTH_QUARTZ);
        for (int i = -3; i <= 3; i++) {
            b.set(3, 1, i, Blocks.QUARTZ_BRICKS);
            b.set(-3, 1, i, Blocks.QUARTZ_BRICKS);
            b.set(i, 1, 3, Blocks.QUARTZ_BRICKS);
            b.set(i, 1, -3, Blocks.QUARTZ_BRICKS);
            b.stair(4, 1, i, Blocks.QUARTZ_STAIRS, Direction.WEST, false);
            b.stair(-4, 1, i, Blocks.QUARTZ_STAIRS, Direction.EAST, false);
            b.stair(i, 1, 4, Blocks.QUARTZ_STAIRS, Direction.NORTH, false);
            b.stair(i, 1, -4, Blocks.QUARTZ_STAIRS, Direction.SOUTH, false);
        }
        b.set(0, 1, 0, Blocks.CHISELED_QUARTZ_BLOCK);

        
        for (int sx = -3; sx <= 3; sx += 6)
            for (int sz = -3; sz <= 3; sz += 6) {
                b.set(sx, 2, sz, Blocks.CHISELED_QUARTZ_BLOCK);
                b.box(sx, 3, sz, sx, 5, sz, Blocks.QUARTZ_PILLAR);
                b.set(sx, 6, sz, Blocks.CHISELED_QUARTZ_BLOCK);
            }
        b.roof(0, 0, 7, 3, 4, Blocks.QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ, 0f);
        b.set(0, 10, 0, Blocks.SEA_LANTERN);
        b.set(0, 11, 0, Blocks.END_ROD);
        b.hang(0, 6, 0, 1, Blocks.LANTERN);
        b.candle(-1, 2, 0, Blocks.WHITE_CANDLE, 3);
        b.candle(1, 2, 0, Blocks.WHITE_CANDLE, 3);
        b.candle(0, 2, 1, Blocks.WHITE_CANDLE, 3);

        
        b.scatter(12, 7, Blocks.MOSS_CARPET);
        b.scatter(3, 7, Blocks.FLOWERING_AZALEA);
        b.chest(0, 2, 0, Direction.SOUTH, "quartz_colonnade");
    }

    
    
    
    private static void ziggurat(B b) {
        b.clear(10, 16);
        b.foundation(8, Blocks.RED_TERRACOTTA);

        Block[] tier  = {Blocks.RED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.WHITE_TERRACOTTA};
        Block[] glaze = {Blocks.ORANGE_GLAZED_TERRACOTTA, Blocks.RED_GLAZED_TERRACOTTA,
                Blocks.YELLOW_GLAZED_TERRACOTTA, Blocks.ORANGE_GLAZED_TERRACOTTA};

        for (int t = 0; t < 4; t++) {
            int r = 8 - 2 * t;
            b.box(-r, 2 * t, -r, r, 2 * t + 1, r, tier[t]);
            for (int x = -r; x <= r; x++)
                for (int z = -r; z <= r; z++)
                    if (Math.abs(x) == r || Math.abs(z) == r) {
                        b.set(x, 2 * t, z, Blocks.CUT_RED_SANDSTONE);
                        b.set(x, 2 * t + 1, z, ((x + z) & 1) == 0 ? glaze[t] : Blocks.CUT_RED_SANDSTONE);
                    }
            for (int sx = -r; sx <= r; sx += 2 * r)
                for (int sz = -r; sz <= r; sz += 2 * r) {
                    b.set(sx, 2 * t + 2, sz, Blocks.RED_SANDSTONE_WALL);
                    b.set(sx, 2 * t + 3, sz, Blocks.LANTERN);
                }
        }

        
        for (int i = 1; i <= 7; i++) {
            int z = 9 - i;
            b.box(-1, 0, z, 1, i - 1, z, Blocks.RED_TERRACOTTA);
            for (int x = -1; x <= 1; x++)
                b.stair(x, i, z, Blocks.RED_SANDSTONE_STAIRS, Direction.NORTH, false);
            for (int s = -2; s <= 2; s += 4) {
                b.box(s, 0, z, s, i, z, Blocks.CUT_RED_SANDSTONE);
                b.set(s, i + 1, z, Blocks.RED_SANDSTONE_WALL);
            }
        }

        
        b.box(-2, 1, -3, 2, 3, 0, Blocks.AIR);
        for (int x = -2; x <= 2; x++)
            for (int z = -3; z <= 0; z++)
                b.set(x, 0, z, ((x + z) & 1) == 0 ? Blocks.ORANGE_GLAZED_TERRACOTTA : Blocks.RED_TERRACOTTA);
        for (int sx = -2; sx <= 2; sx += 4)
            for (int sz = -3; sz <= 0; sz += 3) {
                b.box(sx, 1, sz, sx, 2, sz, Blocks.CHISELED_RED_SANDSTONE);
                b.set(sx, 3, sz, Blocks.RED_SANDSTONE_WALL);
            }
        b.set(-1, 1, 0, Blocks.LANTERN);
        b.set(1, 1, 0, Blocks.LANTERN);

        
        b.box(0, 1, -8, 0, 2, -3, Blocks.AIR);
        b.box(-1, 1, -8, -1, 2, -8, Blocks.CHISELED_RED_SANDSTONE);
        b.box(1, 1, -8, 1, 2, -8, Blocks.CHISELED_RED_SANDSTONE);
        b.set(0, 3, -8, Blocks.CHISELED_RED_SANDSTONE);
        if (ENABLE_TRAPS) {
            b.set(0, 0, -5, Blocks.TNT);
            b.set(0, -1, -5, Blocks.TNT);
            b.set(0, 1, -5, Blocks.STONE_PRESSURE_PLATE);
        }

        
        for (int sx = -2; sx <= 2; sx += 4)
            for (int sz = -2; sz <= 2; sz += 4) {
                b.set(sx, 8, sz, Blocks.CUT_RED_SANDSTONE);
                b.set(sx, 9, sz, Blocks.ORANGE_TERRACOTTA);
                b.set(sx, 10, sz, Blocks.ORANGE_GLAZED_TERRACOTTA);
            }
        b.set(0, 8, 0, Blocks.CHISELED_RED_SANDSTONE);
        b.candle(0, 9, 0, Blocks.RED_CANDLE, 3);
        b.roof(0, 0, 11, 2, 3, Blocks.RED_SANDSTONE_STAIRS, Blocks.CUT_RED_SANDSTONE, 0f);
        b.set(0, 13, 0, Blocks.GOLD_BLOCK);
        b.set(0, 14, 0, Blocks.LANTERN);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2)
                b.hangingLantern(sx, 10, sz, Blocks.LANTERN);

        
        for (int i = 0; i < 12; i++) {
            int x = b.rng.nextInt(21) - 10, z = b.rng.nextInt(21) - 10;
            if (Math.max(Math.abs(x), Math.abs(z)) < 9) continue;
            if (b.get(x, 1, z).isAir() && !b.get(x, 0, z).isAir() && b.get(x, 0, z).getFluidState().isEmpty())
                b.set(x, 1, z, b.rng.nextInt(3) == 0 ? Blocks.DEAD_BUSH : Blocks.CUT_RED_SANDSTONE_SLAB);
        }

        b.chest(-1, 1, -2, Direction.SOUTH, "terracotta_ziggurat");
        b.chest(1, 1, -2, Direction.SOUTH, "terracotta_ziggurat");
    }

    
    
    
    private static void mushroomHut(B b) {
        b.clear(8, 16);
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++)
                if (x * x + z * z <= 36) {
                    b.support(x, z, -1, Blocks.MYCELIUM);
                    b.set(x, 0, z, Math.floorMod(x * 7 + z * 3, 5) == 0 ? Blocks.PODZOL : Blocks.MYCELIUM);
                }

        
        b.box(-2, 0, -2, 2, 4, 2, Blocks.MUSHROOM_STEM);
        b.box(-1, 1, -1, 1, 4, 1, Blocks.AIR);
        b.box(-1, 0, -1, 1, 0, 1, Blocks.MOSS_BLOCK);
        b.box(0, 1, -2, 0, 2, -2, Blocks.AIR);
        b.set(0, 1, -3, Blocks.MOSS_CARPET);
        b.set(0, 0, -3, Blocks.MOSS_BLOCK);
        b.set(2, 2, 0, Blocks.ORANGE_STAINED_GLASS_PANE);
        b.set(-2, 2, 0, Blocks.ORANGE_STAINED_GLASS_PANE);
        b.set(0, 2, 2, Blocks.ORANGE_STAINED_GLASS_PANE);
        b.set(2, 3, 0, Blocks.SHROOMLIGHT);
        b.set(-2, 3, 0, Blocks.SHROOMLIGHT);
        b.set(0, 3, 2, Blocks.SHROOMLIGHT);

        
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++) {
                int d = x * x + z * z;
                if (d <= 36 && !(Math.abs(x) <= 2 && Math.abs(z) <= 2))
                    b.set(x, 4, z, (((int) Math.sqrt(d)) & 1) == 0 ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.MUSHROOM_STEM);
            }
        for (int[] p : new int[][]{{5, 0}, {-5, 0}, {0, 5}, {0, -5}, {3, 3}, {-3, 3}, {3, -3}, {-3, -3}})
            b.set(p[0], 4, p[1], Blocks.SHROOMLIGHT);

        
        for (int y = 5; y <= 9; y++)
            for (int x = -6; x <= 6; x++)
                for (int z = -6; z <= 6; z++) {
                    double d = x * x + z * z;
                    double dy = y - 5;
                    boolean outer = d / 36.0 + dy * dy / 16.0 <= 1.0;
                    boolean inner = d / 20.0 + dy * dy / 9.0 <= 1.0;
                    if (inner) b.set(x, y, z, Blocks.AIR);
                    else if (outer) {
                        int h = Math.floorMod(x * 31 + z * 17 + y * 13 + x * z * 5, 8);
                        Block blk = h == 0 ? Blocks.MUSHROOM_STEM
                                : (h == 3 && b.rng.nextInt(3) == 0) ? Blocks.SHROOMLIGHT : Blocks.RED_MUSHROOM_BLOCK;
                        b.set(x, y, z, blk);
                    }
                }
        b.set(0, 9, 0, Blocks.SHROOMLIGHT);
        b.hang(0, 8, 0, 2, Blocks.LANTERN);

        
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
        for (int y = 1; y <= 4; y++) b.set(0, y, 1, ladder);

        
        b.chest(-1, 1, -1, Direction.EAST, "fungal_hut");
        b.set(1, 1, -1, Blocks.BREWING_STAND);
        b.set(-1, 1, 1, Blocks.CAULDRON);
        b.set(1, 1, 1, Blocks.CRAFTING_TABLE);
        b.chest(3, 5, 0, Direction.WEST, "fungal_hut");
        b.box(-3, 5, 0, -3, 6, 0, Blocks.BOOKSHELF);
        b.set(0, 5, -3, Blocks.BARREL);
        b.scatter(6, 3, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM);
        for (int[] p : new int[][]{{3, 2}, {-3, 2}, {2, -3}, {-2, -3}}) {
            if (b.get(p[0], 5, p[1]).isAir()) b.set(p[0], 5, p[1], b.pick(Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM));
        }

        
        for (int i = 0; i < 10; i++) {
            int x = b.rng.nextInt(11) - 5, z = b.rng.nextInt(11) - 5;
            int d = x * x + z * z;
            if (d < 12 || d > 30 || !b.get(x, 3, z).isAir() || b.get(x, 4, z).isAir()) continue;
            b.set(x, 3, z, i % 3 == 0 ? Blocks.SPORE_BLOSSOM : Blocks.HANGING_ROOTS);
        }

        
        for (int i = 0; i < 5; i++) {
            double a = (i * 72 + b.rng.nextInt(40)) * Math.PI / 180.0;
            int x = (int) Math.round(7 * Math.cos(a));
            int z = (int) Math.round(7 * Math.sin(a));
            int h = 1 + b.rng.nextInt(2);
            Block cap = b.rng.nextBoolean() ? Blocks.RED_MUSHROOM_BLOCK : Blocks.BROWN_MUSHROOM_BLOCK;
            b.support(x, z, 0, Blocks.MYCELIUM);
            b.box(x, 1, z, x, h, z, Blocks.MUSHROOM_STEM);
            b.box(x - 1, h + 1, z - 1, x + 1, h + 1, z + 1, cap);
            b.set(x, h + 2, z, cap);
        }
    }

    
    
    
    private static void gildedVault(B b) {
        b.clear(9, 14);
        b.foundation(7, Blocks.POLISHED_BLACKSTONE_BRICKS);
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++) {
                int m = Math.max(Math.abs(x), Math.abs(z));
                int r = b.rng.nextInt(20);
                Block blk = r == 0 ? Blocks.GILDED_BLACKSTONE
                        : r < 6 ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS
                        : Blocks.POLISHED_BLACKSTONE_BRICKS;
                if (m == 5) blk = Blocks.POLISHED_BLACKSTONE;
                if (m == 6 && ((x + z) & 1) == 0) blk = Blocks.CHISELED_POLISHED_BLACKSTONE;
                b.set(x, 0, z, blk);
            }
        for (int sx = -3; sx <= 3; sx += 6)
            for (int sz = -3; sz <= 3; sz += 6)
                b.set(sx, 0, sz, Blocks.GILDED_BLACKSTONE);
        for (int z = 3; z <= 6; z++) {
            b.set(0, 0, z, Blocks.GILDED_BLACKSTONE);
            b.set(-1, 0, z, Blocks.POLISHED_BLACKSTONE);
            b.set(1, 0, z, Blocks.POLISHED_BLACKSTONE);
        }

        
        b.ruinedWalls(-6, 1, -6, 6, 5, 6, 0.9f,
                Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.BLACKSTONE);
        b.box(-1, 1, 6, 1, 3, 6, Blocks.AIR);
        for (int sx = -2; sx <= 2; sx += 4) {
            b.box(sx, 1, 6, sx, 4, 6, Blocks.POLISHED_BLACKSTONE_BRICKS);
            b.set(sx, 2, 6, Blocks.CHISELED_POLISHED_BLACKSTONE);
            b.set(sx, 5, 6, Blocks.GILDED_BLACKSTONE);
        }
        b.stair(-1, 3, 6, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, true);
        b.stair(1, 3, 6, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.WEST, true);
        for (int x = -1; x <= 1; x++) if (b.chance(0.7f)) b.set(x, 4, 6, Blocks.POLISHED_BLACKSTONE_BRICKS);

        
        for (int sx = -6; sx <= 6; sx += 12)
            for (int sz = -6; sz <= 6; sz += 12) {
                int h = 4 + b.rng.nextInt(3);
                b.flare(sx, sz, 1, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
                b.box(sx, 1, sz, sx, h, sz, Blocks.POLISHED_BLACKSTONE_BRICKS);
                b.set(sx, 2, sz, Blocks.CHISELED_POLISHED_BLACKSTONE);
                b.set(sx, h, sz, Blocks.CHISELED_POLISHED_BLACKSTONE);
                if (b.rng.nextFloat() < 0.6f) {
                    b.set(sx, h + 1, sz, Blocks.GILDED_BLACKSTONE);
                    b.set(sx, h + 2, sz, Blocks.SOUL_LANTERN);
                }
            }
        
        for (int sx = -4; sx <= 4; sx += 8)
            for (int sz = -4; sz <= 4; sz += 8) {
                int h = 2 + b.rng.nextInt(3);
                b.box(sx, 1, sz, sx, h, sz, Blocks.POLISHED_BLACKSTONE_BRICKS);
                b.set(sx, h, sz, Blocks.CHISELED_POLISHED_BLACKSTONE);
                b.set(sx, h + 1, sz, Blocks.GILDED_BLACKSTONE);
            }

        
        for (int sx = -2; sx <= 2; sx += 4) {
            b.set(sx, 1, 4, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
            b.set(sx, 2, 4, Blocks.SOUL_CAMPFIRE);
        }

        
        b.walls(-2, 1, -2, 2, 3, 2, Blocks.POLISHED_BLACKSTONE_BRICKS);
        for (int sx = -2; sx <= 2; sx += 4)
            for (int sz = -2; sz <= 2; sz += 4)
                b.box(sx, 1, sz, sx, 3, sz, Blocks.CHISELED_POLISHED_BLACKSTONE);
        b.set(0, 2, -2, Blocks.GILDED_BLACKSTONE);
        b.set(-2, 2, 0, Blocks.GILDED_BLACKSTONE);
        b.set(2, 2, 0, Blocks.GILDED_BLACKSTONE);
        b.roof(0, 0, 4, 3, 3, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICKS, 0.15f);
        b.set(0, 7, 0, Blocks.GOLD_BLOCK);
        b.set(0, 8, 0, Blocks.SOUL_LANTERN);
        b.box(0, 1, 2, 0, 2, 2, Blocks.AIR);
        b.stair(0, 1, 3, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, false);
        b.box(-1, 0, -1, 1, 0, 1, Blocks.GOLD_BLOCK);
        b.set(0, 1, 0, Blocks.GOLD_BLOCK);
        b.hang(0, 3, 0, 1, Blocks.SOUL_LANTERN);

        
        for (int i = 0; i < 7; i++) {
            int x = b.rng.nextInt(11) - 5, z = b.rng.nextInt(11) - 5;
            if (Math.max(Math.abs(x), Math.abs(z)) >= 3 && b.get(x, 1, z).isAir())
                b.set(x, 1, z, b.rng.nextBoolean() ? Blocks.GOLD_BLOCK : Blocks.RAW_GOLD_BLOCK);
        }
        for (int i = 0; i < 12; i++) {
            int x = b.rng.nextInt(11) - 5, z = b.rng.nextInt(11) - 5;
            if (Math.max(Math.abs(x), Math.abs(z)) >= 3 && b.get(x, 1, z).isAir()) {
                if (b.rng.nextBoolean()) b.stair(x, 1, z, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, b.randomDir(), false);
                else b.slab(x, 1, z, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, false);
            }
        }
        for (int i = 0; i < 6; i++) {
            int x = (b.rng.nextBoolean() ? 1 : -1) * (4 + b.rng.nextInt(2));
            int z = (b.rng.nextBoolean() ? 1 : -1) * (4 + b.rng.nextInt(2));
            if (b.get(x, 2, z).isAir()) b.set(x, 2, z, Blocks.COBWEB);
        }
        b.chest(-1, 1, -1, Direction.SOUTH, "gilded_vault");
        b.chest(1, 1, -1, Direction.SOUTH, "gilded_vault");
        b.chest(-5, 1, -5, Direction.SOUTH, "gilded_vault");
    }

    
    
    
    private static void frozenShrine(B b) {
        b.clear(8, 12);
        b.foundationDisc(7, Blocks.PACKED_ICE);

        for (int y = 0; y <= 5; y++)
            for (int x = -5; x <= 5; x++)
                for (int z = -5; z <= 5; z++) {
                    int d = x * x + y * y + z * z;
                    if (d > 30) continue;
                    if (y == 0) {
                        int r2 = x * x + z * z;
                        Block blk;
                        if (r2 <= 4) blk = Blocks.BLUE_ICE;
                        else if (r2 >= 20) blk = b.rng.nextInt(3) == 0 ? Blocks.SNOW_BLOCK : Blocks.PACKED_ICE;
                        else blk = ((x + z) & 1) == 0 ? Blocks.PACKED_ICE : Blocks.BLUE_ICE;
                        b.set(x, y, z, blk);
                        continue;
                    }
                    if (d < 18) continue;
                    boolean rib = x == 0 || z == 0 || Math.abs(x) == Math.abs(z);
                    b.set(x, y, z, rib ? Blocks.BLUE_ICE : b.pick(Blocks.ICE, Blocks.ICE, Blocks.ICE, Blocks.PACKED_ICE));
                }
        
        b.set(0, 5, 0, Blocks.SEA_LANTERN);
        b.set(3, 3, 0, Blocks.SEA_LANTERN);
        b.set(-3, 3, 0, Blocks.SEA_LANTERN);
        b.set(0, 3, 3, Blocks.SEA_LANTERN);
        b.set(0, 3, -3, Blocks.SEA_LANTERN);

        
        b.box(-1, 1, 4, 1, 2, 5, Blocks.AIR);
        for (int z = 4; z <= 5; z++) {
            b.box(-2, 1, z, -2, 2, z, Blocks.BLUE_ICE);
            b.box(2, 1, z, 2, 2, z, Blocks.BLUE_ICE);
            b.box(-1, 3, z, 1, 3, z, Blocks.BLUE_ICE);
        }
        for (int z = 6; z <= 8; z++) {
            b.set(0, 0, z, Blocks.BLUE_ICE);
            if (z != 7) { b.set(-1, 0, z, Blocks.PACKED_ICE); b.set(1, 0, z, Blocks.PACKED_ICE); }
        }
        b.set(-2, 0, 7, Blocks.SEA_LANTERN);
        b.set(2, 0, 7, Blocks.SEA_LANTERN);
        for (int s = -2; s <= 2; s += 4) {
            b.box(s, 1, 6, s, 2, 6, Blocks.PACKED_ICE);
            b.set(s, 3, 6, Blocks.SEA_LANTERN);
        }

        
        for (int sx = -2; sx <= 2; sx += 4)
            for (int sz = -2; sz <= 2; sz += 4)
                b.set(sx, 0, sz, Blocks.SEA_LANTERN);
        b.box(-1, 1, -1, 1, 1, 1, Blocks.PACKED_ICE);
        b.set(0, 1, 0, Blocks.BLUE_ICE);
        b.chest(0, 2, 0, Direction.SOUTH, "frozen_shrine");

        
        for (int i = 0; i < 14; i++) {
            int x = b.rng.nextInt(9) - 4, z = b.rng.nextInt(9) - 4;
            int r2 = x * x + z * z;
            if (r2 < 6 || r2 > 13 || (z >= 3 && Math.abs(x) <= 1)) continue;
            int ymax = (int) Math.floor(Math.sqrt(17 - r2));
            if (ymax < 2) continue;
            if (i % 2 == 0) {
                b.set(x, ymax, z, Blocks.ICE);
                if (b.chance(0.5f)) b.set(x, ymax - 1, z, Blocks.ICE);
            } else {
                int h = Math.min(1 + b.rng.nextInt(2), ymax - 1);
                for (int y = 1; y <= h; y++) b.set(x, y, z, y == h ? Blocks.BLUE_ICE : Blocks.PACKED_ICE);
            }
        }

        
        for (int i = 0; i < 5; i++) {
            double a = b.rng.nextDouble() * Math.PI * 2;
            int dist = 7 + b.rng.nextInt(3);
            int x = (int) Math.round(dist * Math.cos(a));
            int z = (int) Math.round(dist * Math.sin(a));
            if (Math.abs(x) <= 2 && z >= 5) continue;
            int h = 3 + b.rng.nextInt(5);
            b.support(x, z, -1, Blocks.PACKED_ICE);
            b.box(x, 0, z, x, h, z, Blocks.PACKED_ICE);
            b.box(x - 1, 0, z, x + 1, h / 3, z, Blocks.PACKED_ICE);
            b.box(x, 0, z - 1, x, h / 3, z + 1, Blocks.PACKED_ICE);
            b.set(x, h + 1, z, Blocks.BLUE_ICE);
            int sx = b.rng.nextBoolean() ? 1 : -1;
            b.box(x + 2 * sx, 0, z, x + 2 * sx, h / 2, z, Blocks.BLUE_ICE);
            b.set(x + 2 * sx, h / 2 + 1, z, Blocks.ICE);
            b.set(x, 0, z + 1, Blocks.SEA_LANTERN);
        }

        for (int i = 0; i < 30; i++) {
            int x = b.rng.nextInt(17) - 8, z = b.rng.nextInt(17) - 8;
            if (x * x + z * z >= 36 && b.get(x, 1, z).isAir() && !b.get(x, 0, z).isAir())
                b.set(x, 1, z, Blocks.SNOW);
        }
    }

    
    
    
    private static void ritualCircle(B b) {
        b.clear(10, 12);
        b.foundationDisc(8, Blocks.DEEPSLATE_BRICKS);
        for (int x = -8; x <= 8; x++)
            for (int z = -8; z <= 8; z++) {
                int d = x * x + z * z;
                if (d > 64) continue;
                boolean card = x == 0 || z == 0;
                boolean diag = Math.abs(x) == Math.abs(z);
                Block blk;
                if (d >= 57) blk = b.rng.nextInt(3) == 0 ? Blocks.CRACKED_DEEPSLATE_BRICKS : Blocks.DEEPSLATE_BRICKS;
                else if (d >= 50) blk = Blocks.POLISHED_DEEPSLATE;
                else if (d >= 30 && d <= 40) blk = Blocks.CHISELED_DEEPSLATE;
                else if (d >= 16 && d <= 24) blk = Blocks.CHISELED_DEEPSLATE;
                else blk = Blocks.DEEPSLATE_TILES;
                if (diag && d >= 2 && d <= 14) blk = Blocks.SCULK;
                if (card && d >= 4 && d <= 9) blk = Blocks.SCULK;
                b.set(x, 0, z, blk);
            }

        
        b.set(0, 0, 0, Blocks.CRYING_OBSIDIAN);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                b.set(sx, 0, sz, Blocks.SOUL_SOIL);
                b.set(sx, 1, sz, Blocks.SOUL_FIRE);
            }
        b.candle(4, 1, 0, Blocks.BLACK_CANDLE, 3);
        b.candle(-4, 1, 0, Blocks.BLACK_CANDLE, 3);
        b.candle(0, 1, 4, Blocks.BLACK_CANDLE, 3);
        b.candle(0, 1, -4, Blocks.BLACK_CANDLE, 3);

        
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4.0;
            int px = (int) Math.round(6 * Math.cos(a));
            int pz = (int) Math.round(6 * Math.sin(a));
            boolean tall = (k & 1) == 0;
            int h = tall ? 5 + b.rng.nextInt(2) : 2 + b.rng.nextInt(2);
            b.flare(px, pz, 1, Blocks.DEEPSLATE_BRICK_STAIRS);
            b.set(px, 1, pz, Blocks.POLISHED_DEEPSLATE);
            b.box(px, 2, pz, px, h, pz, Blocks.DEEPSLATE_BRICKS);
            b.set(px, 3, pz, Blocks.CHISELED_DEEPSLATE);
            b.set(px, h, pz, Blocks.CHISELED_DEEPSLATE);
            if (tall) {
                b.set(px, h + 1, pz, Blocks.SOUL_LANTERN);
            } else {
                b.stair(px, h + 1, pz, Blocks.DEEPSLATE_BRICK_STAIRS, b.randomDir(), false);
                for (int i = 0; i < 3; i++) {
                    int rx = px + b.rng.nextInt(3) - 1, rz = pz + b.rng.nextInt(3) - 1;
                    if ((rx != px || rz != pz) && b.get(rx, 1, rz).isAir())
                        b.slab(rx, 1, rz, Blocks.DEEPSLATE_BRICK_SLAB, false);
                }
                if (b.chance(0.5f)) b.set(px, h + 2, pz, Blocks.SKELETON_SKULL);
            }
        }

        
        for (int i = 0; i < 8; i++) {
            double a = (i + b.rng.nextDouble() * 0.6) * Math.PI / 4.0 + Math.PI / 8.0;
            int x = (int) Math.round(9 * Math.cos(a));
            int z = (int) Math.round(9 * Math.sin(a));
            b.support(x, z, 0, Blocks.COBBLED_DEEPSLATE);
            b.set(x, 1, z, Blocks.COBBLED_DEEPSLATE);
            b.set(x, 2, z, Blocks.COBBLED_DEEPSLATE_WALL);
            if (b.rng.nextBoolean()) b.set(x, 3, z, Blocks.COBBLED_DEEPSLATE_WALL);
            if (b.chance(0.4f)) b.set(x + b.rng.nextInt(3) - 1, 1, z + b.rng.nextInt(3) - 1, Blocks.COBBLED_DEEPSLATE_SLAB);
            if (b.chance(0.3f)) b.set(x, 2, z, Blocks.COBWEB);
        }

        b.chest(0, 1, 0, Direction.SOUTH, "ritual_circle");
    }

    
    
    
    private static void mireHut(B b) {
        b.clear(7, 12);

        
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++) {
                int m = Math.max(Math.abs(x), Math.abs(z));
                if (m >= 4 && m <= 6 && b.chance(0.35f)) b.set(x, 0, z, Blocks.MUD);
            }
        for (int[] pond : new int[][]{{-5, 4}, {4, 4}}) {
            for (int x = pond[0] - 1; x <= pond[0] + 2; x++)
                for (int z = pond[1] - 1; z <= pond[1] + 2; z++) {
                    boolean inner = x >= pond[0] && x <= pond[0] + 1 && z >= pond[1] && z <= pond[1] + 1;
                    b.support(x, z, -1, Blocks.MUD);
                    b.set(x, 0, z, inner ? Blocks.WATER : Blocks.MUD);
                    if (inner && b.chance(0.5f)) b.set(x, 1, z, Blocks.LILY_PAD);
                }
        }

        
        for (int sx = -3; sx <= 3; sx += 6)
            for (int sz = -3; sz <= 3; sz += 6) {
                b.box(sx, 0, sz, sx, 1, sz, Blocks.MANGROVE_LOG);
                b.support(sx, sz, -1, Blocks.MANGROVE_LOG);
                int[][] nb = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] n : nb)
                    if (b.chance(0.6f)) b.set(sx + n[0], 0, sz + n[1], b.pick(Blocks.MANGROVE_ROOTS, Blocks.MUDDY_MANGROVE_ROOTS));
            }
        for (int s = -3; s <= 3; s += 6) {
            b.beam(-2, 1, s, 5, Direction.Axis.X, Blocks.MANGROVE_LOG);
            b.beam(s, 1, -2, 5, Direction.Axis.Z, Blocks.MANGROVE_LOG);
        }

        
        b.box(-4, 2, -4, 4, 2, 4, Blocks.MANGROVE_PLANKS);
        for (int x = -4; x <= 4; x++)
            for (int z = -4; z <= 4; z++)
                if (Math.max(Math.abs(x), Math.abs(z)) == 4) b.set(x, 2, z, Blocks.MUD_BRICKS);

        
        b.ruinedWalls(-3, 3, -3, 3, 5, 3, 0.25f, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_PLANKS, Blocks.MUD_BRICKS);
        for (int sx = -3; sx <= 3; sx += 6)
            for (int sz = -3; sz <= 3; sz += 6)
                b.box(sx, 3, sz, sx, 5, sz, Blocks.MANGROVE_LOG);
        b.box(0, 3, 3, 0, 4, 3, Blocks.AIR);
        b.set(-3, 4, 0, Blocks.GREEN_STAINED_GLASS_PANE);
        b.set(3, 4, 0, Blocks.GREEN_STAINED_GLASS_PANE);
        b.set(0, 4, -3, Blocks.GREEN_STAINED_GLASS_PANE);

        
        for (int x = -4; x <= 4; x++)
            for (int z = -4; z <= 4; z++)
                if (Math.max(Math.abs(x), Math.abs(z)) == 4 && !(z == 4 && Math.abs(x) <= 1))
                    b.set(x, 3, z, Blocks.MANGROVE_FENCE);
        for (int sx = -4; sx <= 4; sx += 8)
            for (int sz = -4; sz <= 4; sz += 8) {
                b.set(sx, 4, sz, Blocks.MANGROVE_FENCE);
                b.set(sx, 5, sz, Blocks.LANTERN);
            }

        
        for (int x = -5; x <= 5; x++)
            for (int z = -5; z <= 5; z++)
                if (Math.max(Math.abs(x), Math.abs(z)) == 5) {
                    Direction f = Math.abs(x) >= Math.abs(z)
                            ? (x < 0 ? Direction.EAST : Direction.WEST)
                            : (z < 0 ? Direction.SOUTH : Direction.NORTH);
                    b.stair(x, 5, z, Blocks.MANGROVE_STAIRS, f, false);
                }
        b.roof(0, 0, 6, 4, 4, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_PLANKS, 0f);
        for (int i = 0; i < 4; i++) {
            int rr = 4 - i, y = 6 + i;
            for (int x = -rr; x <= rr; x++)
                for (int z = -rr; z <= rr; z++)
                    if ((Math.abs(x) == rr || Math.abs(z) == rr) && rr > 0 && b.chance(0.3f) && b.get(x, y + 1, z).isAir())
                        b.set(x, y + 1, z, Blocks.MOSS_CARPET);
        }
        b.set(0, 10, 0, Blocks.MANGROVE_FENCE);
        b.set(0, 11, 0, Blocks.LANTERN);

        
        b.stair(0, 1, 6, Blocks.MANGROVE_STAIRS, Direction.NORTH, false);
        b.set(0, 1, 5, Blocks.MANGROVE_LOG);
        b.stair(0, 2, 5, Blocks.MANGROVE_STAIRS, Direction.NORTH, false);

        
        b.hang(0, 5, 0, 1, Blocks.LANTERN);
        b.set(2, 3, -2, Blocks.CAULDRON);
        b.set(2, 3, 1, Blocks.BREWING_STAND);
        b.set(2, 3, -1, Blocks.BARREL);
        b.set(-2, 3, 2, Blocks.MOSS_CARPET);
        b.set(-2, 3, 1, Blocks.MOSS_CARPET);
        b.set(-2, 3, 0, Blocks.BOOKSHELF);
        for (int sx = -2; sx <= 2; sx += 4)
            for (int sz = -2; sz <= 2; sz += 4)
                if (b.chance(0.5f) && b.get(sx, 5, sz).isAir()) b.set(sx, 5, sz, Blocks.COBWEB);
        b.chest(-2, 3, -2, Direction.SOUTH, "mire_hut");
    }

    private static void boneRibcage(B b) {
        b.clear(11, 9);
        int[] ribZ = {-6, -3, 0, 3, 6};
        int[] ribR = {3, 4, 5, 4, 3};

        
        for (int x = -5; x <= 5; x++)
            for (int z = -7; z <= 7; z++) {
                if (b.get(x, 0, z).isAir()) continue;
                int r = b.rng.nextInt(10);
                if (r < 2) b.set(x, 0, z, Blocks.SOUL_SOIL);
                else if (r == 2) b.set(x, 0, z, Blocks.BONE_BLOCK);
            }

        
        for (int i = 0; i < 5; i++) {
            int z = ribZ[i], rr = ribR[i];
            for (int deg = 0; deg <= 180; deg += 3) {
                double a = Math.toRadians(deg);
                Direction.Axis ax = (deg >= 40 && deg <= 140) ? Direction.Axis.X : Direction.Axis.Y;
                b.axis((int) Math.round(rr * Math.cos(a)), (int) Math.round(rr * Math.sin(a)), z, Blocks.BONE_BLOCK, ax);
            }
            b.support(-rr, z, -1, Blocks.BONE_BLOCK);
            b.support(rr, z, -1, Blocks.BONE_BLOCK);
            b.set(-rr - 1, 0, z, Blocks.BONE_BLOCK);
            b.set(rr + 1, 0, z, Blocks.BONE_BLOCK);
        }
        
        for (int z = -6; z <= 7; z++) {
            int seg = Math.min((z + 6) / 3, 3);
            double frac = (z + 6 - 3 * seg) / 3.0;
            int y = (int) Math.round(Mth.lerp(frac, ribR[seg], ribR[seg + 1]));
            if ((z & 1) == 0) {
                b.axis(0, y, z, Blocks.BONE_BLOCK, Direction.Axis.Z);
                b.set(0, y + 1, z, Blocks.BONE_BLOCK);
            } else b.set(0, y, z, Blocks.CALCITE);
        }
        
        for (int i = 0; i < 5; i++) {
            int z = -7 - i;
            int y = 3 - i / 2;
            b.axis(0, y, z, i == 4 ? Blocks.CALCITE : Blocks.BONE_BLOCK, Direction.Axis.Z);
            if (i < 3) b.set(0, y + 1, z, Blocks.BONE_BLOCK);
            b.support(0, z, y - 1, Blocks.BONE_BLOCK);
        }

        
        for (int x = -2; x <= 2; x++)
            for (int z = 8; z <= 10; z++)
                b.support(x, z, -1, Blocks.BONE_BLOCK);
        b.box(-2, 0, 8, 2, 3, 10, Blocks.BONE_BLOCK);
        b.box(-1, 4, 8, 1, 4, 10, Blocks.BONE_BLOCK);
        for (int sx = -2; sx <= 2; sx += 4) {
            b.set(sx, 3, 8, Blocks.AIR);
            b.set(sx, 3, 10, Blocks.AIR);
        }
        b.box(-1, 1, 9, 1, 3, 9, Blocks.AIR);
        b.box(0, 1, 8, 0, 2, 8, Blocks.AIR);
        b.set(0, 3, 7, Blocks.BONE_BLOCK);
        b.set(-1, 2, 10, Blocks.SOUL_LANTERN);
        b.set(1, 2, 10, Blocks.SOUL_LANTERN);
        b.set(-1, 3, 10, Blocks.CALCITE);
        b.set(1, 3, 10, Blocks.CALCITE);
        b.box(0, 1, 10, 0, 2, 10, Blocks.AIR);
        b.box(-2, 0, 11, 2, 0, 11, Blocks.BONE_BLOCK);
        for (int x = -2; x <= 2; x += 2) b.set(x, 1, 11, Blocks.CALCITE);
        for (int s = -1; s <= 1; s += 2) {
            b.set(3 * s, 3, 9, Blocks.BONE_BLOCK);
            b.set(3 * s, 4, 9, Blocks.BONE_BLOCK);
            b.set(4 * s, 5, 9, Blocks.BONE_BLOCK);
            b.set(4 * s, 6, 9, Blocks.BONE_BLOCK);
            b.set(5 * s, 7, 9, Blocks.BONE_BLOCK);
            b.set(5 * s, 8, 9, Blocks.CALCITE);
        }

        for (int i = 0; i < 8; i++) {
            int x = b.rng.nextInt(5) - 2, y = 1 + b.rng.nextInt(3), z = b.rng.nextInt(11) - 5;
            if (b.get(x, y, z).isAir() && (x != 0 || z != 0)) b.set(x, y, z, Blocks.COBWEB);
        }
        for (int i = 0; i < 6; i++) {
            int x = (b.rng.nextBoolean() ? 1 : -1) * (7 + b.rng.nextInt(3));
            int z = b.rng.nextInt(13) - 6;
            if (!b.get(x, 1, z).isAir()) continue;
            if (b.rng.nextInt(3) == 0) b.set(x, 1, z, Blocks.SKELETON_SKULL);
            else b.beam(x, 1, z, 2 + b.rng.nextInt(2), b.rng.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z, Blocks.BONE_BLOCK);
        }
        b.set(0, 1, -3, Blocks.SOUL_CAMPFIRE);
        b.set(0, 1, 3, Blocks.SOUL_CAMPFIRE);
        b.candle(-1, 1, 0, Blocks.BLACK_CANDLE, 2);
        b.candle(1, 1, 0, Blocks.BLACK_CANDLE, 2);

        b.chest(0, 1, 0, Direction.NORTH, "bone_ribcage");
        b.chest(0, 1, 9, Direction.NORTH, "bone_ribcage");
    }


    private static ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "chests/spirit_world/" + name));
    }

    private static final class B {
        final WorldGenLevel level;
        final RandomSource rng;
        final ChunkPos center;
        final BlockPos origin;
        final List<BlockPos> shaped = new ArrayList<>();

        B(WorldGenLevel level, RandomSource rng, ChunkPos center, BlockPos origin) {
            this.level = level;
            this.rng = rng;
            this.center = center;
            this.origin = origin;
        }

        boolean ok(BlockPos p) {
            return Math.abs((p.getX() >> 4) - center.x) <= 1
                    && Math.abs((p.getZ() >> 4) - center.z) <= 1
                    && p.getY() >= 1 && p.getY() < level.getMaxBuildHeight() - 1;
        }

        boolean near(BlockPos p) {
            return Math.abs((p.getX() >> 4) - center.x) <= 1
                    && Math.abs((p.getZ() >> 4) - center.z) <= 1
                    && p.getY() >= level.getMinBuildHeight() && p.getY() < level.getMaxBuildHeight();
        }

        BlockState get(int dx, int dy, int dz) {
            BlockPos p = origin.offset(dx, dy, dz);
            return ok(p) ? level.getBlockState(p) : Blocks.AIR.defaultBlockState();
        }

        void set(int dx, int dy, int dz, BlockState s) {
            BlockPos p = origin.offset(dx, dy, dz);
            if (!ok(p)) return;
            level.setBlock(p, s, 2);
            if (needsShape(s.getBlock())) shaped.add(p);
        }

        void set(int dx, int dy, int dz, Block blk) { set(dx, dy, dz, blk.defaultBlockState()); }

        static boolean needsShape(Block k) {
            return k instanceof StairBlock || k instanceof FenceBlock || k instanceof WallBlock || k instanceof IronBarsBlock;
        }

        void finish() {
            for (BlockPos p : shaped) {
                if (!near(p)) continue;
                BlockState cur = level.getBlockState(p);
                if (!needsShape(cur.getBlock())) continue;
                boolean safe = true;
                for (Direction d : Direction.values())
                    if (!near(p.relative(d))) { safe = false; break; }
                if (!safe) continue;
                BlockState next = Block.updateFromNeighbourShapes(cur, level, p);
                if (next != cur) level.setBlock(p, next, 2);
            }
            shaped.clear();
        }

        boolean chance(float p) { return rng.nextFloat() < p; }

        Direction randomDir() { return Direction.from2DDataValue(rng.nextInt(4)); }

        void box(int x1, int y1, int z1, int x2, int y2, int z2, Block blk) {
            BlockState s = blk.defaultBlockState();
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
                for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                    for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                        set(x, y, z, s);
        }


        void walls(int x1, int y1, int z1, int x2, int y2, int z2, Block blk) {
            BlockState s = blk.defaultBlockState();
            for (int y = y1; y <= y2; y++)
                for (int x = x1; x <= x2; x++)
                    for (int z = z1; z <= z2; z++)
                        if (x == x1 || x == x2 || z == z1 || z == z2) set(x, y, z, s);
        }


        void ruinedWalls(int x1, int y1, int z1, int x2, int y2, int z2, float topDecay, Block... mix) {
            for (int y = y1; y <= y2; y++) {
                float keep = 1f - topDecay * (y - y1) / (float) Math.max(1, y2 - y1);
                for (int x = x1; x <= x2; x++)
                    for (int z = z1; z <= z2; z++)
                        if ((x == x1 || x == x2 || z == z1 || z == z2) && rng.nextFloat() < keep)
                            set(x, y, z, pick(mix));
            }
        }


        void clear(int r, int h) {
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int x = -r; x <= r; x++)
                for (int z = -r; z <= r; z++)
                    for (int y = 1; y <= h; y++)
                        if (!get(x, y, z).isAir()) set(x, y, z, air);
        }


        void support(int dx, int dz, int fromDy, Block blk) {
            BlockState s = blk.defaultBlockState();
            for (int dy = fromDy; dy > fromDy - 12; dy--) {
                BlockState cur = get(dx, dy, dz);
                if (!(cur.isAir() || cur.canBeReplaced())) break;
                set(dx, dy, dz, s);
            }
        }

        void foundation(int r, Block blk) {
            for (int x = -r; x <= r; x++)
                for (int z = -r; z <= r; z++)
                    support(x, z, -1, blk);
        }

        void foundationDisc(int r, Block blk) {
            for (int x = -r; x <= r; x++)
                for (int z = -r; z <= r; z++)
                    if (x * x + z * z <= r * r + r) support(x, z, -1, blk);
        }

        Block pick(Block... blocks) { return blocks[rng.nextInt(blocks.length)]; }


        void stair(int dx, int dy, int dz, Block stairBlock, Direction facing, boolean upsideDown) {
            set(dx, dy, dz, stairBlock.defaultBlockState()
                    .setValue(StairBlock.FACING, facing)
                    .setValue(StairBlock.HALF, upsideDown ? Half.TOP : Half.BOTTOM));
        }

        void flare(int px, int pz, int y, Block stairBlock) {
            if (get(px + 1, y, pz).isAir()) stair(px + 1, y, pz, stairBlock, Direction.WEST, false);
            if (get(px - 1, y, pz).isAir()) stair(px - 1, y, pz, stairBlock, Direction.EAST, false);
            if (get(px, y, pz + 1).isAir()) stair(px, y, pz + 1, stairBlock, Direction.NORTH, false);
            if (get(px, y, pz - 1).isAir()) stair(px, y, pz - 1, stairBlock, Direction.SOUTH, false);
        }

        void slab(int dx, int dy, int dz, Block slabBlock, boolean top) {
            set(dx, dy, dz, slabBlock.defaultBlockState()
                    .setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM));
        }

        void facing(int dx, int dy, int dz, Block blk, Direction dir) {
            set(dx, dy, dz, blk.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, dir));
        }

        void hangingLantern(int dx, int dy, int dz, Block lantern) {
            set(dx, dy, dz, lantern.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }

        void hang(int x, int topY, int z, int len, Block lantern) {
            for (int i = 0; i < len; i++) set(x, topY - i, z, Blocks.CHAIN);
            hangingLantern(x, topY - len, z, lantern);
        }

        void candle(int dx, int dy, int dz, Block candle, int count) {
            set(dx, dy, dz, candle.defaultBlockState()
                    .setValue(CandleBlock.CANDLES, count)
                    .setValue(CandleBlock.LIT, true));
        }

        void scatter(int tries, int r, Block... pool) {
            for (int i = 0; i < tries; i++) {
                int x = rng.nextInt(2 * r + 1) - r, z = rng.nextInt(2 * r + 1) - r;
                BlockState floor = get(x, 0, z);
                if (get(x, 1, z).isAir() && !floor.isAir() && floor.getFluidState().isEmpty())
                    set(x, 1, z, pick(pool));
            }
        }


        void beam(int x, int y, int z, int len, Direction.Axis axis, Block blk) {
            BlockState s = blk.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
            for (int i = 0; i < len; i++)
                set(x + (axis == Direction.Axis.X ? i : 0), y, z + (axis == Direction.Axis.Z ? i : 0), s);
        }

        void axis(int dx, int dy, int dz, Block blk, Direction.Axis axis) {
            if(blk.defaultBlockState().hasProperty(RotatedPillarBlock.AXIS)) {
                set(dx, dy, dz, blk.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
            } else {
                set(dx, dy, dz, blk.defaultBlockState());
            }
        }


        void roof(int cx, int cz, int y, int r, int layers, Block stairBlock, Block fill, float decay) {
            for (int i = 0; i < layers && r - i >= 0; i++) {
                int rr = r - i, yy = y + i;
                for (int x = -rr; x <= rr; x++)
                    for (int z = -rr; z <= rr; z++) {
                        boolean edge = Math.abs(x) == rr || Math.abs(z) == rr;
                        if (rr == 0 || !edge) { set(cx + x, yy, cz + z, fill); continue; }
                        if (decay > 0f && rng.nextFloat() < decay) continue;
                        Direction f = Math.abs(x) >= Math.abs(z)
                                ? (x < 0 ? Direction.EAST : Direction.WEST)
                                : (z < 0 ? Direction.SOUTH : Direction.NORTH);
                        stair(cx + x, yy, cz + z, stairBlock, f, false);
                    }
            }
        }

        void chest(int dx, int dy, int dz, Direction facing, String lootName) {
            BlockPos p = origin.offset(dx, dy, dz);
            if (!ok(p)) return;
            level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
            if (level.getBlockEntity(p) instanceof RandomizableContainerBlockEntity chest) {
                chest.setLootTable(loot(lootName), rng.nextLong());
            }
        }
    }
}
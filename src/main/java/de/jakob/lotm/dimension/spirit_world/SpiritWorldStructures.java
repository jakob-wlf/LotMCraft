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
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;

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
        }
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
                Block blk = m == 6 ? b.pick(WOOLS)
                        : m == 5 ? Blocks.WHITE_WOOL
                        : (((x + z) & 1) == 0 ? Blocks.LIGHT_GRAY_WOOL : Blocks.WHITE_WOOL);
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
                    b.set(x, 1, z, CARPETS[b.rng.nextInt(CARPETS.length)]);
        for (int z = 2; z <= 5; z++) b.set(0, 1, z, Blocks.PINK_CARPET);

        
        for (int[] p : new int[][]{{-4, -4}, {4, -4}, {-4, 4}, {4, 4}}) {
            Block c = b.pick(WOOLS);
            b.box(p[0], 1, p[1], p[0], 5, p[1], c);
            b.set(p[0], 1, p[1], Blocks.WHITE_WOOL);
            b.set(p[0], 3, p[1], Blocks.WHITE_WOOL);
        }
        
        for (int[] p : new int[][]{{-4, 0}, {4, 0}, {0, -4}}) {
            b.box(p[0], 1, p[1], p[0], 2, p[1], b.pick(WOOLS));
            b.candle(p[0], 3, p[1], Blocks.WHITE_CANDLE, 3);
        }
        
        Block cN = b.pick(WOOLS), cW = b.pick(WOOLS), cE = b.pick(WOOLS), cS = b.pick(WOOLS);
        b.box(-3, 5, -4, 3, 5, -4, cN);
        b.box(-4, 5, -3, -4, 5, 3, cW);
        b.box(4, 5, -3, 4, 5, 3, cE);
        b.box(-3, 5, 4, -2, 5, 4, cS);
        b.box(2, 5, 4, 3, 5, 4, cS);

        
        b.box(-5, 6, -5, 5, 6, 5, b.pick(WOOLS));
        for (int x = -5; x <= 5; x++)
            for (int z = -5; z <= 5; z++)
                if (Math.max(Math.abs(x), Math.abs(z)) == 5) b.set(x, 7, z, Blocks.WHITE_CARPET);
        b.box(-3, 7, -3, 3, 7, 3, b.pick(WOOLS));
        b.box(-1, 8, -1, 1, 8, 1, b.pick(WOOLS));
        b.set(0, 9, 0, Blocks.SEA_LANTERN);
        b.set(0, 10, 0, Blocks.END_ROD);

        
        b.hangingLantern(0, 5, 0, Blocks.LANTERN);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2)
                b.hangingLantern(2 * sx, 5, 2 * sz, Blocks.LANTERN);

        
        b.box(-1, 0, -1, 1, 0, 1, Blocks.MAGENTA_WOOL);
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
                b.set(x, 0, z, d >= 22 ? Blocks.SMOOTH_BASALT : b.rng.nextInt(4) == 0 ? Blocks.AMETHYST_BLOCK : Blocks.CALCITE);
            }

        
        for (int h = 0; h <= 19; h++) {
            int rad = h < 5 ? 3 : h < 12 ? 2 : 1;
            for (int x = -rad; x <= rad; x++)
                for (int z = -rad; z <= rad; z++)
                    if (x * x + z * z <= rad * rad + 1) {
                        int roll = b.rng.nextInt(20);
                        Block blk = roll < 12 ? Blocks.AMETHYST_BLOCK : roll < 15 ? Blocks.BUDDING_AMETHYST : Blocks.CALCITE;
                        b.set(x, h, z, blk);
                    }
        }
        
        for (int x = -3; x <= 3; x++)
            for (int z = -3; z <= 3; z++) {
                int d = x * x + z * z;
                if (d <= 10) { b.set(x, 5, z, Blocks.CALCITE); }
                if (d <= 5)  { b.set(x, 12, z, Blocks.CALCITE); }
            }
        
        b.set(2, 8, 0, Blocks.TINTED_GLASS);
        b.set(-2, 8, 0, Blocks.TINTED_GLASS);
        b.set(0, 8, 2, Blocks.TINTED_GLASS);
        b.set(0, 8, -2, Blocks.TINTED_GLASS);
        b.set(1, 15, 0, Blocks.TINTED_GLASS);
        b.set(-1, 15, 0, Blocks.TINTED_GLASS);
        b.set(0, 15, 1, Blocks.TINTED_GLASS);
        b.set(0, 15, -1, Blocks.TINTED_GLASS);

        
        b.set(0, 20, 0, Blocks.SEA_LANTERN);
        b.set(0, 21, 0, Blocks.AMETHYST_CLUSTER);
        b.set(1, 20, 0, Blocks.AMETHYST_CLUSTER);
        b.set(-1, 20, 0, Blocks.AMETHYST_CLUSTER);
        b.set(0, 20, 1, Blocks.AMETHYST_CLUSTER);
        b.set(0, 20, -1, Blocks.AMETHYST_CLUSTER);

        
        b.box(-1, 1, -1, 1, 4, 1, Blocks.AIR);
        b.box(0, 1, -3, 0, 2, -2, Blocks.AIR);
        b.set(0, 4, 0, Blocks.SEA_LANTERN);
        b.set(-1, 1, -1, Blocks.AMETHYST_CLUSTER);
        b.set(1, 1, -1, Blocks.AMETHYST_CLUSTER);
        b.set(-1, 1, 1, Blocks.AMETHYST_CLUSTER);
        b.set(1, 1, 1, Blocks.AMETHYST_CLUSTER);

        
        for (int sx = -1; sx <= 1; sx += 2) {
            b.box(sx, 1, -4, sx, 3, -4, Blocks.CALCITE);
            b.set(sx, 4, -4, Blocks.AMETHYST_CLUSTER);
        }
        for (int z = -6; z <= -4; z++) b.set(0, 0, z, Blocks.SMOOTH_BASALT);

        
        for (int k = 0; k < 6; k++) {
            double a = (k + b.rng.nextDouble() * 0.5) * Math.PI / 3.0;
            int x = (int) Math.round(5 * Math.cos(a));
            int z = (int) Math.round(5 * Math.sin(a));
            if (Math.abs(x) <= 1 && z < 0) continue; 
            int h = 2 + b.rng.nextInt(4);
            b.support(x, z, -1, Blocks.AMETHYST_BLOCK);
            b.box(x, 0, z, x, h, z, Blocks.AMETHYST_BLOCK);
            b.set(x, h + 1, z, Blocks.AMETHYST_CLUSTER);
            int sx = Integer.signum(x), sz = Integer.signum(z);
            b.box(x + sx, 0, z, x + sx, h / 2, z, Blocks.BUDDING_AMETHYST);
            b.set(x + sx, h / 2 + 1, z, Blocks.AMETHYST_CLUSTER);
            b.set(x, 1, z + sz, Blocks.AMETHYST_CLUSTER);
        }

        for (int i = -4; i <= 4; i++) {
            if (b.rng.nextFloat() < 0.35f) b.set(i, 1, 4, Blocks.AMETHYST_CLUSTER);
            if (Math.abs(i) > 1 && b.rng.nextFloat() < 0.35f) b.set(i, 1, -4, Blocks.AMETHYST_CLUSTER);
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
                Block blk;
                if (d <= 4) blk = Blocks.CRYING_OBSIDIAN;
                else if (d >= 12 && d <= 20) blk = Blocks.PURPUR_BLOCK;
                else if (d >= 30) blk = Blocks.POLISHED_BLACKSTONE_BRICKS;
                else blk = b.rng.nextInt(6) == 0 ? Blocks.SCULK : Blocks.OBSIDIAN;
                b.set(x, 0, z, blk);
            }

        
        b.set(3, 0, 3, Blocks.SCULK);
        b.set(-3, 0, -3, Blocks.SCULK);
        b.set(3, 1, 3, Blocks.SCULK_CATALYST);
        b.set(-3, 1, -3, Blocks.SCULK_CATALYST);

        
        for (int[] p : new int[][]{{4, 0}, {-4, 0}, {0, 4}, {0, -4}}) {
            b.set(p[0], 1, p[1], Blocks.PURPUR_PILLAR);
            b.box(p[0], 2, p[1], p[0], 3, p[1], Blocks.CRYING_OBSIDIAN);
            b.set(p[0], 4, p[1], Blocks.PURPUR_PILLAR);
            b.set(p[0], 5, p[1], Blocks.END_ROD);
        }

        
        for (int sx = -4; sx <= 4; sx += 8)
            for (int sz = -4; sz <= 4; sz += 8) {
                b.set(sx, 3, sz, Blocks.OBSIDIAN);
                b.set(sx, 4, sz, Blocks.CRYING_OBSIDIAN);
                b.set(sx, 5, sz, Blocks.END_ROD);
            }

        
        b.box(-1, 1, -1, 1, 1, 1, Blocks.OBSIDIAN);
        b.stair(0, 1, -2, Blocks.PURPUR_STAIRS, Direction.SOUTH, false);
        b.stair(0, 1, 2, Blocks.PURPUR_STAIRS, Direction.NORTH, false);
        b.stair(-2, 1, 0, Blocks.PURPUR_STAIRS, Direction.EAST, false);
        b.stair(2, 1, 0, Blocks.PURPUR_STAIRS, Direction.WEST, false);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2)
                b.set(sx, 2, sz, Blocks.END_ROD);
        b.chest(0, 2, 0, Direction.SOUTH, "void_altar");
    }

    
    
    

    
    private static void emberForge(B b) {
        b.clear(9, 16);
        b.foundation(7, Blocks.NETHER_BRICKS);
        b.box(-7, 0, -7, 7, 0, 7, Blocks.NETHER_BRICKS);
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++) {
                int r = b.rng.nextInt(10);
                if (r == 0) b.set(x, 0, z, Blocks.MAGMA_BLOCK);
                else if (r < 3) b.set(x, 0, z, Blocks.RED_NETHER_BRICKS);
                else if (r < 5) b.set(x, 0, z, Blocks.CRACKED_NETHER_BRICKS);
            }
        b.box(-1, 0, -1, 1, 0, 1, Blocks.CHISELED_NETHER_BRICKS);

        
        b.ruinedWalls(-7, 1, -7, 7, 5, 7, 0.2f,
                Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.RED_NETHER_BRICKS);
        for (int i = -7; i <= 7; i++) {
            Block top = (i & 1) == 0 ? Blocks.NETHER_BRICKS : Blocks.AIR;
            if (top != Blocks.AIR) {
                b.set(i, 6, -7, top);
                b.set(i, 6, 7, top);
                b.set(-7, 6, i, top);
                b.set(7, 6, i, top);
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

        
        b.box(-1, 1, -7, 1, 3, -7, Blocks.AIR);
        b.box(-2, 1, -7, -2, 4, -7, Blocks.CHISELED_NETHER_BRICKS);
        b.box(2, 1, -7, 2, 4, -7, Blocks.CHISELED_NETHER_BRICKS);
        b.box(-1, 4, -7, 1, 4, -7, Blocks.RED_NETHER_BRICKS);
        b.stair(-1, 3, -7, Blocks.NETHER_BRICK_STAIRS, Direction.EAST, true);
        b.stair(1, 3, -7, Blocks.NETHER_BRICK_STAIRS, Direction.WEST, true);
        
        for (int sx = -3; sx <= 3; sx += 6) {
            b.set(sx, 0, -9, Blocks.NETHERRACK);
            b.set(sx, 1, -9, Blocks.FIRE);
        }
        for (int z = -9; z <= -8; z++) b.set(0, 0, z, Blocks.RED_NETHER_BRICKS);

        
        for (int sx = -7; sx <= 7; sx += 14)
            for (int sz = -7; sz <= 7; sz += 14) {
                int ox = Integer.signum(sx), oz = Integer.signum(sz);
                b.walls(sx - 1, 1, sz - 1, sx + 1, 8, sz + 1, Blocks.RED_NETHER_BRICKS);
                b.box(sx, 1, sz, sx, 8, sz, Blocks.AIR);
                b.walls(sx - 1, 4, sz - 1, sx + 1, 4, sz + 1, Blocks.CHISELED_NETHER_BRICKS);
                b.set(sx + ox, 6, sz, Blocks.AIR);
                b.set(sx, 6, sz + oz, Blocks.AIR);
                b.set(sx + ox, 2, sz, Blocks.SHROOMLIGHT);
                b.roof(sx, sz, 9, 2, 3, Blocks.NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICKS, 0f);
                b.set(sx, 12, sz, Blocks.SHROOMLIGHT);
            }

        
        b.box(2, 0, 2, 4, 0, 4, Blocks.LAVA);
        for (int x = 1; x <= 5; x++)
            for (int z = 1; z <= 5; z++)
                if (x == 1 || x == 5 || z == 1 || z == 5)
                    b.slab(x, 1, z, Blocks.NETHER_BRICK_SLAB, false);

        
        b.facing(-5, 1, -3, Blocks.BLAST_FURNACE, Direction.EAST);
        b.facing(-5, 1, -1, Blocks.ANVIL, Direction.NORTH);
        b.set(-5, 1, 1, Blocks.LAVA_CAULDRON);
        b.set(-6, 3, -3, Blocks.SHROOMLIGHT);

        
        for (int[] p : new int[][]{{-3, 4}, {3, -4}, {-3, -4}}) {
            b.set(p[0], 0, p[1], Blocks.NETHERRACK);
            b.set(p[0], 1, p[1], Blocks.FIRE);
        }

        b.chest(-6, 1, 6, Direction.EAST, "ember_forge");
        b.chest(6, 1, -6, Direction.WEST, "ember_forge");
    }

    
    
    

    
    private static void quartzColonnade(B b) {
        b.clear(9, 12);
        b.foundationDisc(8, Blocks.QUARTZ_BRICKS);
        for (int x = -8; x <= 8; x++)
            for (int z = -8; z <= 8; z++) {
                int d = x * x + z * z;
                if (d > 64) continue;
                Block blk;
                if (d >= 56) blk = ((x + z) & 1) == 0 ? Blocks.QUARTZ_BRICKS : Blocks.SMOOTH_QUARTZ;
                else if (d >= 36 && d <= 49) blk = Blocks.QUARTZ_BRICKS;
                else if (d <= 12) blk = Blocks.CHISELED_QUARTZ_BLOCK;
                else blk = Blocks.SMOOTH_QUARTZ;
                b.set(x, 0, z, blk);
            }
        b.set(5, 0, 0, Blocks.SEA_LANTERN);
        b.set(-5, 0, 0, Blocks.SEA_LANTERN);
        b.set(0, 0, 5, Blocks.SEA_LANTERN);
        b.set(0, 0, -5, Blocks.SEA_LANTERN);

        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4.0;
            int px = (int) Math.round(7 * Math.cos(a));
            int pz = (int) Math.round(7 * Math.sin(a));
            boolean broken = b.rng.nextFloat() < 0.3f;
            int h = broken ? 3 : 5 + b.rng.nextInt(2);
            b.set(px, 1, pz, Blocks.CHISELED_QUARTZ_BLOCK);
            b.box(px, 2, pz, px, h, pz, Blocks.QUARTZ_PILLAR);
            if (!broken) {
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

        for (int i = -1; i <= 1; i += 2) {
            b.stair(4, 1, i, Blocks.QUARTZ_STAIRS, Direction.EAST, false);
            b.stair(-4, 1, i, Blocks.QUARTZ_STAIRS, Direction.WEST, false);
            b.stair(i, 1, 4, Blocks.QUARTZ_STAIRS, Direction.SOUTH, false);
            b.stair(i, 1, -4, Blocks.QUARTZ_STAIRS, Direction.NORTH, false);
        }

        b.roof(0, 0, 1, 2, 2, Blocks.QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ, 0f);
        b.set(0, 2, 0, Blocks.SEA_LANTERN);
        b.chest(0, 3, 0, Direction.SOUTH, "quartz_colonnade");
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
                    if ((Math.abs(x) == r || Math.abs(z) == r) && ((x + z) & 1) == 0)
                        b.set(x, 2 * t + 1, z, glaze[t]);
            b.set(-r, 2 * t + 2, -r, Blocks.LANTERN);
            b.set(r, 2 * t + 2, -r, Blocks.LANTERN);
            b.set(-r, 2 * t + 2, r, Blocks.LANTERN);
            b.set(r, 2 * t + 2, r, Blocks.LANTERN);
        }

        for (int i = 1; i <= 7; i++) {
            int z = 9 - i;
            b.box(-1, 0, z, 1, i - 1, z, Blocks.RED_TERRACOTTA);
            for (int x = -1; x <= 1; x++)
                b.stair(x, i, z, Blocks.RED_SANDSTONE_STAIRS, Direction.NORTH, false);
        }

        b.box(-2, 1, -3, 2, 3, 0, Blocks.AIR);
        for (int x = -2; x <= 2; x++)
            for (int z = -3; z <= 0; z++)
                b.set(x, 0, z, ((x + z) & 1) == 0 ? Blocks.ORANGE_GLAZED_TERRACOTTA : Blocks.RED_TERRACOTTA);
        b.set(-2, 1, 0, Blocks.LANTERN);
        b.set(2, 1, 0, Blocks.LANTERN);
        b.set(-2, 1, -3, Blocks.LANTERN);
        b.set(2, 1, -3, Blocks.LANTERN);

        b.box(0, 1, -8, 0, 2, -3, Blocks.AIR);
        if (ENABLE_TRAPS) {
            b.set(0, 0, -5, Blocks.TNT);
            b.set(0, -1, -5, Blocks.TNT);
            b.set(0, 1, -5, Blocks.STONE_PRESSURE_PLATE);
        }

        for (int sx = -2; sx <= 2; sx += 4)
            for (int sz = -2; sz <= 2; sz += 4)
                b.box(sx, 8, sz, sx, 10, sz, Blocks.ORANGE_TERRACOTTA);
        b.box(-2, 11, -2, 2, 11, 2, Blocks.WHITE_TERRACOTTA);
        b.set(0, 12, 0, Blocks.ORANGE_GLAZED_TERRACOTTA);
        b.hangingLantern(0, 10, 0, Blocks.LANTERN);

        b.chest(-1, 1, -2, Direction.SOUTH, "terracotta_ziggurat");
        b.chest(1, 1, -2, Direction.SOUTH, "terracotta_ziggurat");
    }

    private static void mushroomHut(B b) {
        b.clear(8, 16);
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++)
                if (x * x + z * z <= 36) {
                    b.support(x, z, -1, Blocks.MYCELIUM);
                    b.set(x, 0, z, Blocks.MYCELIUM);
                }

        
        b.box(-2, 0, -2, 2, 4, 2, Blocks.MUSHROOM_STEM);
        b.box(-1, 1, -1, 1, 4, 1, Blocks.AIR);
        b.box(-1, 0, -1, 1, 0, 1, Blocks.MOSS_BLOCK);
        b.box(0, 1, -2, 0, 2, -2, Blocks.AIR);       
        b.set(0, 1, -3, Blocks.MOSS_CARPET);
        b.set(2, 2, 0, Blocks.AIR);                   
        b.set(-2, 2, 0, Blocks.AIR);
        b.set(0, 2, 2, Blocks.AIR);

        
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++)
                if (x * x + z * z <= 36 && !(Math.abs(x) <= 2 && Math.abs(z) <= 2))
                    b.set(x, 4, z, Blocks.MUSHROOM_STEM);

        
        for (int y = 5; y <= 9; y++)
            for (int x = -6; x <= 6; x++)
                for (int z = -6; z <= 6; z++) {
                    double d = x * x + z * z;
                    double dy = y - 5;
                    boolean outer = d / 36.0 + dy * dy / 16.0 <= 1.0;
                    boolean inner = d / 20.0 + dy * dy / 9.0 <= 1.0;
                    if (inner) b.set(x, y, z, Blocks.AIR);
                    else if (outer) {
                        int r = b.rng.nextInt(10);
                        Block blk = r == 0 ? Blocks.SHROOMLIGHT : r < 3 ? Blocks.MUSHROOM_STEM : Blocks.RED_MUSHROOM_BLOCK;
                        b.set(x, y, z, blk);
                    }
                }
        b.set(0, 8, 0, Blocks.SHROOMLIGHT);

        
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
        for (int y = 1; y <= 4; y++) b.set(0, y, 1, ladder);

        
        b.chest(-1, 1, -1, Direction.EAST, "fungal_hut");
        b.set(1, 1, -1, Blocks.BREWING_STAND);
        b.set(-1, 1, 1, Blocks.CAULDRON);
        b.set(1, 1, 1, Blocks.CRAFTING_TABLE);
        b.chest(3, 5, 0, Direction.WEST, "fungal_hut");
        b.set(-3, 5, 0, Blocks.SHROOMLIGHT);

        
        for (int i = 0; i < 4; i++) {
            double a = b.rng.nextDouble() * Math.PI * 2;
            int x = (int) Math.round(7 * Math.cos(a));
            int z = (int) Math.round(7 * Math.sin(a));
            int h = 1 + b.rng.nextInt(2);
            b.support(x, z, 0, Blocks.MYCELIUM);
            b.box(x, 1, z, x, h, z, Blocks.MUSHROOM_STEM);
            b.box(x - 1, h + 1, z - 1, x + 1, h + 1, z + 1,
                    b.rng.nextBoolean() ? Blocks.RED_MUSHROOM_BLOCK : Blocks.BROWN_MUSHROOM_BLOCK);
        }
    }

    
    
    

    
    private static void gildedVault(B b) {
        b.clear(9, 14);
        b.foundation(7, Blocks.POLISHED_BLACKSTONE_BRICKS);
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++) {
                int r = b.rng.nextInt(20);
                Block blk = r == 0 ? Blocks.GILDED_BLACKSTONE
                        : r < 6 ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS
                        : Blocks.POLISHED_BLACKSTONE_BRICKS;
                b.set(x, 0, z, blk);
            }
        
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
        }

        
        for (int sx = -6; sx <= 6; sx += 12)
            for (int sz = -6; sz <= 6; sz += 12) {
                int h = 4 + b.rng.nextInt(3);
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
                b.set(sx, h + 1, sz, Blocks.GILDED_BLACKSTONE);
            }

        
        b.walls(-2, 1, -2, 2, 3, 2, Blocks.POLISHED_BLACKSTONE_BRICKS);
        b.box(-2, 1, -2, -2, 3, -2, Blocks.CHISELED_POLISHED_BLACKSTONE);
        b.box(2, 1, -2, 2, 3, -2, Blocks.CHISELED_POLISHED_BLACKSTONE);
        b.box(-2, 1, 2, -2, 3, 2, Blocks.CHISELED_POLISHED_BLACKSTONE);
        b.box(2, 1, 2, 2, 3, 2, Blocks.CHISELED_POLISHED_BLACKSTONE);
        b.roof(0, 0, 4, 3, 3, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICKS, 0.15f);
        b.set(0, 7, 0, Blocks.GOLD_BLOCK);
        b.box(0, 1, 2, 0, 2, 2, Blocks.AIR);
        b.box(-1, 0, -1, 1, 0, 1, Blocks.GOLD_BLOCK);
        b.set(0, 1, 1, Blocks.SOUL_LANTERN);

        
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
                    if (y == 0) b.set(x, y, z, b.rng.nextInt(3) == 0 ? Blocks.SNOW_BLOCK : Blocks.PACKED_ICE);
                    else if (d >= 18) b.set(x, y, z, b.pick(Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.ICE, Blocks.ICE));
                    else b.set(x, y, z, Blocks.AIR);
                }
        b.box(0, 1, 3, 0, 2, 5, Blocks.AIR); 
        for (int z = 6; z <= 8; z++) {
            b.set(0, 0, z, Blocks.BLUE_ICE);
            if (z != 7) { b.set(-1, 0, z, Blocks.PACKED_ICE); b.set(1, 0, z, Blocks.PACKED_ICE); }
        }
        b.set(-2, 0, 7, Blocks.SEA_LANTERN);
        b.set(2, 0, 7, Blocks.SEA_LANTERN);

        
        b.set(2, 0, 2, Blocks.SEA_LANTERN);
        b.set(-2, 0, 2, Blocks.SEA_LANTERN);
        b.set(2, 0, -2, Blocks.SEA_LANTERN);
        b.set(-2, 0, -2, Blocks.SEA_LANTERN);

        
        for (int i = 0; i < 9; i++) {
            int x = b.rng.nextInt(5) - 2, z = b.rng.nextInt(5) - 2;
            int d = x * x + z * z;
            if (d >= 2 && d < 9) {
                b.set(x, 3, z, Blocks.ICE);
                if (b.rng.nextBoolean()) b.set(x, 2, z, Blocks.ICE);
            }
        }

        
        b.box(-1, 1, -1, 1, 1, 1, Blocks.PACKED_ICE);
        b.set(0, 1, 0, Blocks.BLUE_ICE);
        b.chest(0, 2, 0, Direction.SOUTH, "frozen_shrine");

        
        for (int i = 0; i < 5; i++) {
            double a = b.rng.nextDouble() * Math.PI * 2;
            int dist = 7 + b.rng.nextInt(3);
            int x = (int) Math.round(dist * Math.cos(a));
            int z = (int) Math.round(dist * Math.sin(a));
            int h = 3 + b.rng.nextInt(5);
            b.support(x, z, -1, Blocks.PACKED_ICE);
            b.box(x, 0, z, x, h, z, Blocks.PACKED_ICE);
            b.box(x - 1, 0, z, x + 1, h / 3, z, Blocks.PACKED_ICE);
            b.box(x, 0, z - 1, x, h / 3, z + 1, Blocks.PACKED_ICE);
            b.set(x, h + 1, z, Blocks.BLUE_ICE);
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
                Block blk;
                if (d > 49) blk = b.rng.nextInt(3) == 0 ? Blocks.CRACKED_DEEPSLATE_BRICKS : Blocks.DEEPSLATE_BRICKS;
                else if (d >= 36) blk = Blocks.POLISHED_DEEPSLATE;
                else if (d >= 16 && d <= 25) blk = Blocks.CHISELED_DEEPSLATE;
                else blk = Blocks.DEEPSLATE_TILES;
                b.set(x, 0, z, blk);
            }
        
        for (int i = 2; i <= 5; i++) {
            if (i >= 4 && i <= 5) continue; 
            b.set(i, 0, 0, Blocks.SCULK);
            b.set(-i, 0, 0, Blocks.SCULK);
            b.set(0, 0, i, Blocks.SCULK);
            b.set(0, 0, -i, Blocks.SCULK);
        }

        
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4.0;
            int px = (int) Math.round(6 * Math.cos(a));
            int pz = (int) Math.round(6 * Math.sin(a));
            int h = 3 + b.rng.nextInt(3);
            b.set(px, 1, pz, Blocks.POLISHED_DEEPSLATE);
            b.box(px, 2, pz, px, h, pz, Blocks.DEEPSLATE_BRICKS);
            b.set(px, h, pz, Blocks.CHISELED_DEEPSLATE);
            b.set(px, h + 1, pz, (k & 1) == 0 ? Blocks.SOUL_LANTERN : Blocks.SKELETON_SKULL);
        }

        
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                b.set(sx, 0, sz, Blocks.SOUL_SOIL);
                b.set(sx, 1, sz, Blocks.SOUL_FIRE);
            }

        
        b.candle(4, 1, 0, Blocks.BLACK_CANDLE, 3);
        b.candle(-4, 1, 0, Blocks.BLACK_CANDLE, 3);
        b.candle(0, 1, 4, Blocks.BLACK_CANDLE, 3);
        b.candle(0, 1, -4, Blocks.BLACK_CANDLE, 3);

        
        for (int i = 0; i < 6; i++) {
            double a = (i + b.rng.nextDouble() * 0.6) * Math.PI / 3.0;
            int x = (int) Math.round(9 * Math.cos(a));
            int z = (int) Math.round(9 * Math.sin(a));
            b.support(x, z, 0, Blocks.COBBLED_DEEPSLATE);
            b.set(x, 1, z, Blocks.COBBLED_DEEPSLATE_WALL);
            if (b.rng.nextBoolean()) b.set(x, 2, z, Blocks.COBBLED_DEEPSLATE_WALL);
        }

        b.set(0, 0, 0, Blocks.CRYING_OBSIDIAN);
        b.chest(0, 1, 0, Direction.SOUTH, "ritual_circle");
    }

    
    
    

    
    private static void mireHut(B b) {
        b.clear(7, 12);

        
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++) {
                int m = Math.max(Math.abs(x), Math.abs(z));
                if (m >= 4 && m <= 6 && b.chance(0.35f)) b.set(x, 0, z, Blocks.MUD);
            }

        
        for (int sx = -3; sx <= 3; sx += 6)
            for (int sz = -3; sz <= 3; sz += 6) {
                b.box(sx, 0, sz, sx, 1, sz, Blocks.MANGROVE_LOG);
                b.support(sx, sz, -1, Blocks.MANGROVE_LOG);
                int[][] nb = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] n : nb)
                    if (b.chance(0.5f)) b.set(sx + n[0], 0, sz + n[1], Blocks.MANGROVE_ROOTS);
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
        b.set(-3, 4, 0, Blocks.AIR);           
        b.set(3, 4, 0, Blocks.AIR);
        b.set(0, 4, -3, Blocks.AIR);

        
        b.roof(0, 0, 6, 4, 4, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_PLANKS, 0f);
        b.set(0, 10, 0, Blocks.MANGROVE_FENCE);

        
        b.stair(0, 1, 6, Blocks.MANGROVE_STAIRS, Direction.NORTH, false);
        b.set(0, 1, 5, Blocks.MANGROVE_LOG);
        b.stair(0, 2, 5, Blocks.MANGROVE_STAIRS, Direction.NORTH, false);
        for (int sx = -4; sx <= 4; sx += 2) {
            if (Math.abs(sx) <= 1) continue;
            b.set(sx, 3, 4, Blocks.MANGROVE_FENCE);
        }
        b.set(-4, 4, 4, Blocks.LANTERN);
        b.set(4, 4, 4, Blocks.LANTERN);

        
        b.hangingLantern(0, 5, 0, Blocks.LANTERN);
        b.set(2, 3, -2, Blocks.CAULDRON);
        b.set(2, 3, 1, Blocks.BREWING_STAND);
        b.set(-2, 3, 2, Blocks.MOSS_CARPET);
        b.set(-2, 3, 1, Blocks.MOSS_CARPET);
        b.chest(-2, 3, -2, Direction.SOUTH, "mire_hut");
    }

    
    
    

    
    private static void boneRibcage(B b) {
        b.clear(11, 9);
        int[] ribZ = {-6, -3, 0, 3, 6};
        int[] ribR = {3, 4, 5, 4, 3};
        for (int i = 0; i < 5; i++) {
            int z = ribZ[i], rr = ribR[i];
            for (int deg = 0; deg <= 180; deg += 3) {
                double a = Math.toRadians(deg);
                b.set((int) Math.round(rr * Math.cos(a)), (int) Math.round(rr * Math.sin(a)), z, Blocks.BONE_BLOCK);
            }
            b.support(-rr, z, -1, Blocks.BONE_BLOCK);
            b.support(rr, z, -1, Blocks.BONE_BLOCK);
        }
        
        for (int z = -6; z <= 6; z++) {
            int seg = Math.min((z + 6) / 3, 3);
            double frac = (z + 6 - 3 * seg) / 3.0;
            int y = (int) Math.round(Mth.lerp(frac, ribR[seg], ribR[seg + 1]));
            b.set(0, y, z, Blocks.BONE_BLOCK);
            if ((z & 1) == 0) b.set(0, y + 1, z, Blocks.BONE_BLOCK);
        }
        
        for (int i = 0; i < 5; i++) {
            int z = -7 - i;
            int y = 3 - i / 2;
            b.set(0, y, z, Blocks.BONE_BLOCK);
            if (i < 3) b.set(0, y + 1, z, Blocks.BONE_BLOCK);
            b.support(0, z, y - 1, Blocks.BONE_BLOCK);
        }

        
        for (int x = -2; x <= 2; x++)
            for (int z = 8; z <= 10; z++)
                b.support(x, z, -1, Blocks.BONE_BLOCK);
        b.box(-2, 0, 8, 2, 3, 10, Blocks.BONE_BLOCK);
        b.box(-1, 1, 9, 1, 2, 9, Blocks.AIR);
        b.set(-1, 2, 8, Blocks.AIR);
        b.set(1, 2, 8, Blocks.AIR);
        b.set(0, 1, 8, Blocks.AIR);
        
        b.set(-1, 2, 10, Blocks.SOUL_LANTERN);
        b.set(1, 2, 10, Blocks.SOUL_LANTERN);
        b.set(0, 1, 10, Blocks.AIR);
        b.box(-2, 0, 11, 2, 0, 11, Blocks.BONE_BLOCK);
        for (int x = -2; x <= 2; x += 2) b.set(x, 1, 11, Blocks.CALCITE);
        
        for (int s = -1; s <= 1; s += 2) {
            b.set(3 * s, 3, 9, Blocks.BONE_BLOCK);
            b.set(3 * s, 4, 9, Blocks.BONE_BLOCK);
            b.set(4 * s, 5, 9, Blocks.BONE_BLOCK);
            b.set(4 * s, 6, 9, Blocks.BONE_BLOCK);
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

        BlockState get(int dx, int dy, int dz) {
            BlockPos p = origin.offset(dx, dy, dz);
            return ok(p) ? level.getBlockState(p) : Blocks.AIR.defaultBlockState();
        }

        void set(int dx, int dy, int dz, BlockState s) {
            BlockPos p = origin.offset(dx, dy, dz);
            if (ok(p)) level.setBlock(p, s, 2);
        }

        void set(int dx, int dy, int dz, Block blk) { set(dx, dy, dz, blk.defaultBlockState()); }

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

        void candle(int dx, int dy, int dz, Block candle, int count) {
            set(dx, dy, dz, candle.defaultBlockState()
                    .setValue(CandleBlock.CANDLES, count)
                    .setValue(CandleBlock.LIT, true));
        }

        
        void beam(int x, int y, int z, int len, Direction.Axis axis, Block blk) {
            BlockState s = blk.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
            for (int i = 0; i < len; i++)
                set(x + (axis == Direction.Axis.X ? i : 0), y, z + (axis == Direction.Axis.Z ? i : 0), s);
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
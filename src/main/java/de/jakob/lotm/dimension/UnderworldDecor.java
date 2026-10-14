package de.jakob.lotm.dimension;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;


public final class UnderworldDecor {

    private static final int CELL = 10;
    private static final int MARGIN = 8;
    private static final double[] CHANCE = {0.06, 0.035, 0.05, 0.06, 0.045, 0.07, 0.06, 0.05};
    private static final double PALE_CHANCE = 0.06;

    @FunctionalInterface
    private interface Out { void set(int x, int y, int z, BlockState state); }

    private static BlockState b(Block block) { return block.defaultBlockState(); }
    private static BlockState leaves(Block block) { return block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true); }

    private static final BlockState OAK_LOG = b(Blocks.OAK_LOG), OAK_LEAVES = leaves(Blocks.OAK_LEAVES);
    private static final BlockState MANGROVE_LOG = b(Blocks.MANGROVE_LOG), MANGROVE_LEAVES = leaves(Blocks.MANGROVE_LEAVES);
    private static final BlockState MANGROVE_ROOTS = b(Blocks.MANGROVE_ROOTS);
    private static final BlockState SANDSTONE = b(Blocks.SANDSTONE), CUT_SANDSTONE = b(Blocks.CUT_SANDSTONE),
            CHISELED_SANDSTONE = b(Blocks.CHISELED_SANDSTONE);
    private static final BlockState PACKED_ICE = b(Blocks.PACKED_ICE), BLUE_ICE = b(Blocks.BLUE_ICE), SNOW = b(Blocks.SNOW_BLOCK);
    private static final BlockState BASALT = b(Blocks.BASALT), POLISHED_BASALT = b(Blocks.POLISHED_BASALT),
            BLACKSTONE = b(Blocks.BLACKSTONE), CRYING = b(Blocks.CRYING_OBSIDIAN);
    private static final BlockState PURPUR = b(Blocks.PURPUR_PILLAR), END_BRICKS = b(Blocks.END_STONE_BRICKS),
            END_ROD = b(Blocks.END_ROD);

    private static final BlockState QUARTZ_BRICKS = b(Blocks.QUARTZ_BRICKS), SMOOTH_QUARTZ = b(Blocks.SMOOTH_QUARTZ),
            QUARTZ_PILLAR = b(Blocks.QUARTZ_PILLAR), CALCITE = b(Blocks.CALCITE), BONE = b(Blocks.BONE_BLOCK),
            GOLD = b(Blocks.GOLD_BLOCK), SEA_LANTERN = b(Blocks.SEA_LANTERN), DEAD_LOG = b(Blocks.BIRCH_LOG);

    private static final BlockState[] STONY = {
            b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.GRANITE), b(Blocks.COBBLESTONE), b(Blocks.TUFF)
    };

    private UnderworldDecor() {}

    

    public static void decorate(int minX, int minZ, int minY, int maxY, UnderworldStructures.Placer placer) {
        int maxX = minX + 15, maxZ = minZ + 15;
        Out out = (x, y, z, s) -> {
            if (x >= minX && x <= maxX && z >= minZ && z <= maxZ && y >= minY && y <= maxY) {
                placer.place(x - minX, y, z - minZ, s);
            }
        };

        for (int cx = Math.floorDiv(minX - MARGIN, CELL); cx <= Math.floorDiv(maxX + MARGIN, CELL); cx++) {
            for (int cz = Math.floorDiv(minZ - MARGIN, CELL); cz <= Math.floorDiv(maxZ + MARGIN, CELL); cz++) {
                int ox = cx * CELL + (int) (UnderworldTerrain.hash3(cx, 11, cz) * CELL);
                int oz = cz * CELL + (int) (UnderworldTerrain.hash3(cx, 12, cz) * CELL);

                UnderworldTerrain.Column col = UnderworldTerrain.column(ox, oz);
                if (col.shaft() || col.water() >= 0 || col.bank() || col.deckTop() >= 0) continue;

                if (col.zone() == UnderworldTerrain.Zone.WILD) {
                    if (col.top() >= 122) continue; 
                    if (col.pale()) {
                        if (UnderworldTerrain.hash3(cx, 13, cz) >= PALE_CHANCE) continue;
                        buildPale(cx, cz, ox, col.top() + 1, oz, out);
                    } else {
                        int world = UnderworldTerrain.worldIndex(ox, oz);
                        if (UnderworldTerrain.hash3(cx, 13, cz) >= CHANCE[world]) continue;
                        build(world, ox, col.top() + 1, oz, cx, cz, out);
                    }
                } else if (col.zone() == UnderworldTerrain.Zone.NATION && col.top() > 40) {
                    double r = Math.sqrt((double) ox * ox + (double) oz * oz);
                    if (r < 132 || r > 192 || UnderworldTerrain.nearBridge(ox, oz, 10)) continue;
                    if (UnderworldTerrain.hash3(cx, 13, cz) >= PALE_CHANCE) continue;
                    buildPale(cx, cz, ox, col.top() + 1, oz, out);
                }
            }
        }
    }

    

    private static int ri(int cx, int cz, int k, int lo, int hi) {
        return lo + (int) (UnderworldTerrain.hash3(cx, 100 + k, cz) * (hi - lo + 1));
    }

    private static void line(Out o, int x, int y, int z, int h, BlockState s) {
        for (int i = 0; i < h; i++) o.set(x, y + i, z, s);
    }

    private static void blob(Out o, double cx, double cy, double cz, double rx, double ry, double rz,
                             BlockState s, double drop, BlockState skip) {
        for (int x = (int) Math.floor(cx - rx); x <= Math.ceil(cx + rx); x++) {
            for (int y = (int) Math.floor(cy - ry); y <= Math.ceil(cy + ry); y++) {
                for (int z = (int) Math.floor(cz - rz); z <= Math.ceil(cz + rz); z++) {
                    double dx = (x - cx) / rx, dy = (y - cy) / ry, dz = (z - cz) / rz;
                    if (dx * dx + dy * dy + dz * dz > 1.0) continue;
                    if (UnderworldTerrain.hash3(x, y + 700, z) < drop) continue;
                    o.set(x, y, z, s);
                }
            }
        }
    }

    private static void disc(Out o, int cx, int y, int cz, double r, BlockState s) {
        int ri = (int) Math.ceil(r);
        for (int x = -ri; x <= ri; x++) for (int z = -ri; z <= ri; z++) {
            if (x * x + z * z <= r * r) o.set(cx + x, y, cz + z, s);
        }
    }

    

    private static BlockState stonyMix(int x, int y, int z) {
        return STONY[(int) (UnderworldTerrain.hash3(x, y + 300, z) * STONY.length)];
    }

    private static void build(int world, int ox, int oy, int oz, int cx, int cz, Out o) {
        switch (world) {
            case 0 -> { 
                int h = ri(cx, cz, 0, 6, 10);
                line(o, ox, oy, oz, h, OAK_LOG);
                blob(o, ox, oy + h, oz, 4.6, 3.2, 4.6, OAK_LEAVES, 0.12, null);
                line(o, ox + 1, oy + h - 3, oz, 1, OAK_LOG);
            }
            case 1 -> { 
                int h = ri(cx, cz, 0, 9, 17);
                for (int i = 0; i < h; i++) {
                    double r = 3.4 * (1 - i / (double) h) + 0.6;
                    disc(o, ox, oy + i, oz, r, i % 6 == 5 ? CHISELED_SANDSTONE : i % 2 == 0 ? SANDSTONE : CUT_SANDSTONE);
                }
            }
            case 2 -> { 
                int h = ri(cx, cz, 0, 10, 20);
                for (int i = 0; i < h; i++) {
                    double r = 3.6 * (1 - i / (double) h);
                    disc(o, ox, oy + i, oz, r, UnderworldTerrain.hash3(ox, oy + i, oz) < 0.4 ? BLUE_ICE : PACKED_ICE);
                }
                disc(o, ox, oy, oz, 4.5, SNOW);
            }
            case 3 -> { 
                for (int k = 0; k < 7; k++) {
                    int dx = ri(cx, cz, 10 + k, -4, 4), dz = ri(cx, cz, 20 + k, -4, 4);
                    int h = ri(cx, cz, 30 + k, 3, 14);
                    for (int i = 0; i < h; i++) {
                        double hh = UnderworldTerrain.hash3(ox + dx, oy + i, oz + dz);
                        o.set(ox + dx, oy + i, oz + dz, i == h - 1 && hh < 0.4 ? CRYING : hh < 0.5 ? BASALT : hh < 0.8 ? POLISHED_BASALT : BLACKSTONE);
                    }
                }
            }
            case 4 -> { 
                int h = ri(cx, cz, 0, 8, 18);
                disc(o, ox, oy, oz, 2.2, END_BRICKS);
                line(o, ox, oy + 1, oz, h, PURPUR);
                o.set(ox, oy + h + 1, oz, END_ROD);
            }
            case 5 -> { 
                int br = ri(cx, cz, 0, 3, 5);
                for (int x = -br; x <= br; x++) for (int y = 0; y <= br; y++) for (int z = -br; z <= br; z++) {
                    double q = (x * x + z * z) / (double) (br * br) + (y * y) / (double) (br * br);
                    if (q > 1.0 || UnderworldTerrain.hash3(ox + x, oy + y + 500, oz + z) < 0.08) continue;
                    o.set(ox + x, oy + y - 1, oz + z, stonyMix(ox + x, oy + y, oz + z));
                }
                for (int k = 0; k < 4; k++) {
                    double ang = UnderworldTerrain.hash3(cx, 80 + k, cz) * Math.PI * 2;
                    int sx = ox + (int) Math.round(Math.cos(ang) * (br + 3));
                    int sz = oz + (int) Math.round(Math.sin(ang) * (br + 3));
                    int h = ri(cx, cz, 40 + k, 6, 15);
                    for (int i = -2; i < h; i++) {
                        double r = 2.0 * (1 - i / (double) h) + 0.3;
                        disc(o, sx, oy + i, sz, r, stonyMix(sx, oy + i, sz));
                    }
                }
            }
            case 6 -> { 
                for (int k = 0; k < 3; k++) {
                    double ang = UnderworldTerrain.hash3(cx, 90 + k, cz) * Math.PI * 2;
                    int sx = ox + (int) Math.round(Math.cos(ang) * 3);
                    int sz = oz + (int) Math.round(Math.sin(ang) * 3);
                    int h = ri(cx, cz, 50 + k, 10, 20);
                    for (int i = -2; i < h; i++) {
                        double lean = i > 0 ? i * i * 0.02 : 0;
                        int px = sx + (int) Math.round(Math.cos(ang) * lean);
                        int pz = sz + (int) Math.round(Math.sin(ang) * lean);
                        double r = 2.4 * (1 - i / (double) h) + 0.2;
                        disc(o, px, oy + i, pz, r, UnderworldTerrain.hash3(px, oy + i, pz) < 0.1 ? CALCITE : BONE);
                    }
                }
            }
            default -> {}
        }
    }

    

    private static BlockState paleBlock(int x, int y, int z) {
        double h = UnderworldTerrain.hash3(x, y, z);
        return h < 0.2 ? CALCITE : h < 0.3 ? SMOOTH_QUARTZ : QUARTZ_BRICKS;
    }

    private static void buildPale(int cx, int cz, int ox, int oy, int oz, Out o) {
        int type = (int) (UnderworldTerrain.hash3(cx, 14, cz) * 5);
        switch (type) {
            case 0 -> { 
                for (int k = 0; k < 4; k++) {
                    int dx = ri(cx, cz, 40 + k, -4, 4), dz = ri(cx, cz, 50 + k, -4, 4);
                    int h = ri(cx, cz, 60 + k, 4, 14);
                    for (int i = -3; i < h; i++) {
                        BlockState s = i % 5 == 4 ? GOLD
                                : UnderworldTerrain.hash3(ox + dx, i, oz + dz) < 0.12 ? BONE : QUARTZ_PILLAR;
                        o.set(ox + dx, oy + i, oz + dz, s);
                    }
                }
            }
            case 1 -> { 
                boolean swap = UnderworldTerrain.hash3(cx, 15, cz) < 0.5;
                for (int k = -4; k <= 4; k++) {
                    double radius = 6.0 * Math.sqrt(1.0 - (k * k) / 25.0) + 0.5;
                    int topY = 0;
                    for (double a = 0.15; a < Math.PI - 0.15; a += 0.07) {
                        int px = (int) Math.round(radius * Math.cos(a));
                        int py = (int) Math.round(radius * Math.sin(a) * 1.1);
                        topY = Math.max(topY, py);
                        if (swap) o.set(ox + k * 2, oy + py, oz + px, BONE);
                        else      o.set(ox + px, oy + py, oz + k * 2, BONE);
                    }
                    if (swap) o.set(ox + k * 2, oy + topY, oz, BONE);   
                    else      o.set(ox, oy + topY, oz + k * 2, BONE);
                }
            }
            case 2 -> { 
                int h = ri(cx, cz, 10, 10, 18);
                disc(o, ox, oy - 1, oz, 3.0, SMOOTH_QUARTZ);
                for (int i = 0; i < h; i++) {
                    int half = i < h - 3 ? 1 : 0;
                    for (int x = -half; x <= half; x++) for (int z = -half; z <= half; z++) {
                        o.set(ox + x, oy + i, oz + z, i % 6 == 5 ? GOLD : paleBlock(ox + x, oy + i, oz + z));
                    }
                }
                o.set(ox, oy + h, oz, SEA_LANTERN);
            }
            case 3 -> { 
                int h = ri(cx, cz, 20, 12, 20);
                for (int i = -6; i <= h; i++) {
                    for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
                        boolean shell = Math.abs(x) == 2 || Math.abs(z) == 2;
                        if (i < 0) { o.set(ox + x, oy + i, oz + z, QUARTZ_BRICKS); continue; }
                        if (!shell) {
                            if (i % 5 == 0) o.set(ox + x, oy + i, oz + z, SMOOTH_QUARTZ);
                            continue;
                        }
                        if (i <= 2 && z == -2 && x == 0) continue;                 
                        if (i % 5 == 3 && (x == 0 || z == 0)) continue;             
                        if (i > h - 4 && UnderworldTerrain.hash3(ox + x, oy + i, oz + z) < (i - (h - 4)) * 0.2) continue;
                        o.set(ox + x, oy + i, oz + z, i == h ? GOLD : paleBlock(ox + x, oy + i, oz + z));
                    }
                }
            }
            case 4 -> { 
                int h = ri(cx, cz, 30, 7, 12);
                line(o, ox, oy, oz, h, DEAD_LOG);
                for (int k = 0; k < 3; k++) {
                    double ang = UnderworldTerrain.hash3(cx, 70 + k, cz) * Math.PI * 2;
                    int sy = oy + h - 5 + k * 2;
                    for (int s = 1; s <= 4; s++) {
                        o.set(ox + (int) Math.round(Math.cos(ang) * s), sy + s / 2, oz + (int) Math.round(Math.sin(ang) * s), DEAD_LOG);
                    }
                }
            }
            default -> {}
        }
    }
}
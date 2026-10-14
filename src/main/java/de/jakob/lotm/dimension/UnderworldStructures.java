package de.jakob.lotm.dimension;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public final class UnderworldStructures {

    @FunctionalInterface
    public interface Placer { void place(int lx, int y, int lz, BlockState state); }

    @FunctionalInterface
    private interface Mat { BlockState at(int x, int y, int z); }

    private record Shape(int x0, int y0, int z0, int x1, int y1, int z1, Mat mat) {}

    private static final int GROUND = UnderworldTerrain.NATION_Y;
    private static final int FLOOR  = GROUND + 1;

    
    private static final int BLACK_TOP  = 52;
    private static final int BLACK_FADE = 36;

    

    private static final BlockState QUARTZ_BRICKS = Blocks.QUARTZ_BRICKS.defaultBlockState();
    private static final BlockState SMOOTH_QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState PILLAR        = Blocks.QUARTZ_PILLAR.defaultBlockState();
    private static final BlockState CALCITE       = Blocks.CALCITE.defaultBlockState();
    private static final BlockState WHITE_CONCRETE = Blocks.WHITE_CONCRETE.defaultBlockState();
    private static final BlockState GOLD          = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState LANTERN       = Blocks.SEA_LANTERN.defaultBlockState();

    private static final BlockState BLACKSTONE    = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState POLISHED_BB   = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState BLACK_CONCRETE = Blocks.BLACK_CONCRETE.defaultBlockState();
    private static final BlockState BLACK_WOOL    = Blocks.BLACK_WOOL.defaultBlockState();
    private static final BlockState GLASS_BLACK   = Blocks.BLACK_STAINED_GLASS.defaultBlockState();
    private static final BlockState TINTED        = Blocks.TINTED_GLASS.defaultBlockState();

    private static final BlockState BONE          = Blocks.BONE_BLOCK.defaultBlockState();
    private static final BlockState WHITE_WOOL    = Blocks.WHITE_WOOL.defaultBlockState();
    private static final BlockState GRAY_WOOL     = Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
    private static final BlockState WHITE_TERRA   = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState GRAY_TERRA    = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState MOSS          = Blocks.MOSS_BLOCK.defaultBlockState();

    private static final List<Shape> SHAPES = new ArrayList<>();

    static {
        buildMainHall();
        buildCauseways();
        buildSunkenPalaces();
        buildPhoenix();
        buildCorpses();
        buildStyxCurtain();
        buildFloatingIslets();
    }

    private UnderworldStructures() {}

    

    public static void stamp(int chunkMinX, int chunkMinZ, int minY, int maxY, Placer placer) {
        int chunkMaxX = chunkMinX + 15, chunkMaxZ = chunkMinZ + 15;
        for (Shape s : SHAPES) {
            if (s.x1() < chunkMinX || s.x0() > chunkMaxX || s.z1() < chunkMinZ || s.z0() > chunkMaxZ) continue;
            int xa = Math.max(s.x0(), chunkMinX), xb = Math.min(s.x1(), chunkMaxX);
            int za = Math.max(s.z0(), chunkMinZ), zb = Math.min(s.z1(), chunkMaxZ);
            int ya = Math.max(s.y0(), minY),      yb = Math.min(s.y1(), maxY);
            for (int x = xa; x <= xb; x++) {
                for (int z = za; z <= zb; z++) {
                    for (int y = ya; y <= yb; y++) {
                        BlockState st = s.mat().at(x, y, z);
                        if (st != null) placer.place(x - chunkMinX, y, z - chunkMinZ, st);
                    }
                }
            }
        }
    }

    

    private static void add(int x0, int y0, int z0, int x1, int y1, int z1, Mat m) {
        SHAPES.add(new Shape(x0, y0, z0, x1, y1, z1, m));
    }

    private static double h3(int x, int y, int z) { return UnderworldTerrain.hash3(x, y, z); }

    private static double noise(double a, double b) { return UnderworldTerrain.noise(a, b); }

    private static boolean ruined(int x, int y, int z, double p) {
        return p > 0 && h3(x * 13 + 7, y * 17 - 3, z * 11 + 5) < p;
    }

    
    private static BlockState pale(int x, int y, int z) {
        double black = UnderworldTerrain.smooth((BLACK_TOP - y) / (double) BLACK_FADE);
        if (black > 0 && h3(x * 3 + 1, y * 5 + 2, z * 7 + 3) < black) {
            double k = h3(x + 9, y + 4, z + 1);
            return k < 0.5 ? BLACKSTONE : k < 0.8 ? POLISHED_BB : BLACK_CONCRETE;
        }
        double h = h3(x, y, z);
        if (h < 0.03)  return GOLD;
        if (h < 0.042) return LANTERN;
        if (h < 0.35)  return SMOOTH_QUARTZ;
        if (h < 0.55)  return CALCITE;
        if (h < 0.62)  return WHITE_CONCRETE;
        return QUARTZ_BRICKS;
    }

    private static BlockState phoenixBlack(int x, int y, int z) {
        double h = h3(x + 3, y + 5, z + 7);
        return h < 0.6 ? BLACK_CONCRETE : h < 0.85 ? BLACKSTONE : BLACK_WOOL;
    }

    private static void ellipsoid(double cx, double cy, double cz, double rx, double ry, double rz, Mat mat) {
        add((int) Math.floor(cx - rx), (int) Math.floor(cy - ry), (int) Math.floor(cz - rz),
                (int) Math.ceil(cx + rx),  (int) Math.ceil(cy + ry),  (int) Math.ceil(cz + rz),
                (x, y, z) -> {
                    double dx = (x - cx) / rx, dy = (y - cy) / ry, dz = (z - cz) / rz;
                    return dx * dx + dy * dy + dz * dz <= 1.0 ? mat.at(x, y, z) : null;
                });
    }

    private static void cylinder(int cx, int cz, double rad, int y0, int y1, Mat mat) {
        int r = (int) Math.ceil(rad);
        add(cx - r, y0, cz - r, cx + r, y1, cz + r, (x, y, z) -> {
            double dx = x - cx, dz = z - cz;
            return dx * dx + dz * dz <= rad * rad ? mat.at(x, y, z) : null;
        });
    }

    
    private static void tube(double[][] p, double[] r, Mat body, BlockState fur, double furChance) {
        double minX = 1e9, minY = 1e9, minZ = 1e9, maxX = -1e9, maxY = -1e9, maxZ = -1e9, maxR = 0;
        for (int i = 0; i < p.length; i++) {
            minX = Math.min(minX, p[i][0]); maxX = Math.max(maxX, p[i][0]);
            minY = Math.min(minY, p[i][1]); maxY = Math.max(maxY, p[i][1]);
            minZ = Math.min(minZ, p[i][2]); maxZ = Math.max(maxZ, p[i][2]);
            maxR = Math.max(maxR, r[i]);
        }
        int pad = (int) Math.ceil(maxR) + 2;
        add((int) minX - pad, (int) minY - pad, (int) minZ - pad,
                (int) maxX + pad, (int) maxY + pad, (int) maxZ + pad, (x, y, z) -> {
                    double best = Double.MAX_VALUE;
                    for (int i = 0; i < p.length - 1; i++) {
                        double ax = p[i][0], ay = p[i][1], az = p[i][2];
                        double bx = p[i + 1][0] - ax, by = p[i + 1][1] - ay, bz = p[i + 1][2] - az;
                        double len2 = bx * bx + by * by + bz * bz;
                        double t = len2 == 0 ? 0 : ((x - ax) * bx + (y - ay) * by + (z - az) * bz) / len2;
                        t = Math.max(0, Math.min(1, t));
                        double dx = x - (ax + bx * t), dy = y - (ay + by * t), dz = z - (az + bz * t);
                        double rel = Math.sqrt(dx * dx + dy * dy + dz * dz) - (r[i] + (r[i + 1] - r[i]) * t);
                        if (rel < best) best = rel;
                    }
                    if (best > 0) {
                        return (fur != null && best <= 1.2 && h3(x, y + 50, z) < furChance * 0.6) ? fur : null;
                    }
                    if (fur != null && best > -1.0 && h3(x, y, z) < furChance) return fur;
                    return body.at(x, y, z);
                });
    }

    

    
    private static void building(int cx, int cz, int hx, int hz, int f, int wallH, int roofH,
                                 int ext, double taper, int yb, double ruin, int doors, boolean pillars) {
        final int doorH = Math.min(9, wallH / 2 + 1);

        
        add(cx - hx - ext, yb, cz - hz - ext, cx + hx + ext, f - 1, cz + hz + ext, (x, y, z) -> {
            double k = f - 1 - y;
            double ax = hx + ext - k * taper + noise(y * 0.13, z * 0.11) * 2.5;
            double az = hz + ext - k * taper + noise(y * 0.13 + 40, x * 0.11) * 2.5;
            return (Math.abs(x - cx) <= ax && Math.abs(z - cz) <= az) ? pale(x, y, z) : null;
        });

        
        add(cx - hx, f, cz - hz, cx + hx, f + wallH, cz + hz, (x, y, z) -> {
            int dx = x - cx, dz = z - cz, ax = Math.abs(dx), az = Math.abs(dz), ly = y - f;
            boolean shell = ax >= hx - 1 || az >= hz - 1;
            if (!shell) {
                if (pillars && ax <= hx - 4 && az >= hz / 2 && az <= hz / 2 + 1 && Math.floorMod(dx, 10) < 2) {
                    return ruined(x, y, z, ruin * 3) ? null : PILLAR;
                }
                return null;
            }
            boolean door = ((doors & 1) != 0 && dx >= hx - 1 && az <= 3)
                    || ((doors & 2) != 0 && dx <= -(hx - 1) && az <= 3)
                    || ((doors & 4) != 0 && dz >= hz - 1 && ax <= 3)
                    || ((doors & 8) != 0 && dz <= -(hz - 1) && ax <= 3);
            if (door && ly < doorH) return null;
            if (ly >= wallH / 3 && ly <= wallH * 2 / 3
                    && ((az >= hz - 1 && Math.floorMod(x, 8) >= 2 && Math.floorMod(x, 8) <= 3)
                    || (ax >= hx - 1 && Math.floorMod(z, 8) >= 2 && Math.floorMod(z, 8) <= 3))) return null;
            if (ruined(x, y, z, ruin * (0.4 + 1.6 * ly / (double) wallH))) return null;
            if (ly == wallH) return GOLD;
            return pale(x, y, z);
        });

        
        add(cx - hx - 2, f + wallH, cz - hz - 2, cx + hx + 2, f + wallH + roofH, cz + hz + 2, (x, y, z) -> {
            int k = y - (f + wallH);
            double ex = hx + 2 - k * (hx - 2) / (double) roofH;
            double ez = hz + 2 - k * (hz - 2) / (double) roofH;
            int ax = Math.abs(x - cx), az = Math.abs(z - cz);
            if (ax > ex || az > ez) return null;
            boolean ring = ax > ex - 3 || az > ez - 3 || k == 0 || k == roofH;
            if (!ring) return null;
            if (ruined(x, y, z, ruin * (1.0 + 2.5 * k / roofH))) return null;
            if (k % 6 == 0 && (ax > ex - 1 || az > ez - 1)) return GOLD;
            return pale(x, y, z);
        });
    }

    
    private static void tower(int cx, int cz, int yb, int yt, double rb, double rt, double ruin) {
        int R = (int) Math.ceil(rb) + 1;
        add(cx - R, yb, cz - R, cx + R, yt, cz + R, (x, y, z) -> {
            double t = (y - yb) / (double) (yt - yb);
            double rad = rb + (rt - rb) * t;
            double dx = x - cx, dz = z - cz, d = Math.sqrt(dx * dx + dz * dz);
            if (d > rad) return null;
            int ly = y - yb;
            if (d <= rad - 2 && ly % 14 != 0 && y < yt) return null;
            if (ly % 12 >= 5 && ly % 12 <= 7 && (Math.abs(dx) < 1.0 || Math.abs(dz) < 1.0)
                    && d > rad - 2.5 && t < 0.85) return null;
            if (ly % 20 == 0 && d > rad - 1.5) return GOLD;
            if (ruined(x, y, z, ruin * t * t * 1.6)) return null;
            return pale(x, y, z);
        });
    }

    
    private static void corridor(boolean alongX, int fixed, int a, int b, int floorY, int halfW, double ruin) {
        int lo = Math.min(a, b), hi = Math.max(a, b), yb = 14;
        Mat m = (x, y, z) -> {
            int u = alongX ? x : z;
            int w = Math.abs(alongX ? z - fixed : x - fixed);
            int ly = y - floorY;
            if (ly < -2) {
                if (w >= halfW - 1 && w <= halfW && Math.floorMod(u, 16) < 2) {
                    return ruined(x, y, z, ruin) ? null : pale(x, y, z);
                }
                return null;
            }
            if (ly <= -1) return w <= halfW ? pale(x, y, z) : null;
            if (ly <= 5) {
                if (w != halfW) return null;
                if (ly >= 1 && ly <= 4 && Math.floorMod(u, 6) >= 2 && Math.floorMod(u, 6) <= 3) return null;
                return ruined(x, y, z, ruin) ? null : pale(x, y, z);
            }
            if (ly == 6 && w <= halfW) {
                if (ruined(x, y, z, ruin)) return null;
                return w == halfW ? GOLD : pale(x, y, z);
            }
            return null;
        };
        if (alongX) add(lo, yb, fixed - halfW - 1, hi, floorY + 8, fixed + halfW + 1, m);
        else        add(fixed - halfW - 1, yb, lo, fixed + halfW + 1, floorY + 8, hi, m);
    }

    

    private static void buildMainHall() {
        
        building(0, 0, 30, 22, FLOOR, 34, 14, 16, 0.42, 10, 0.012, 1 | 4 | 8, true);

        
        add(-27, FLOOR, -7, -21, FLOOR, 7, (x, y, z) -> GOLD);
        add(-27, FLOOR + 1, -3, -24, FLOOR + 3, 3, (x, y, z) -> pale(x, y, z));

        
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                tower(sx * 30, sz * 22, FLOOR, FLOOR + 96, 7, 1, 0.30);
            }
        }
    }

    private static void buildCauseways() {
        
        corridor(true,  0, 30,  176, FLOOR, 3, 0.08);
        corridor(false, 0, 22,  176, FLOOR, 3, 0.08);
        corridor(false, 0, -22, -176, FLOOR, 3, 0.08);
    }

    

    private static void buildSunkenPalaces() {
        Random rnd = new Random(0xD3A7L);
        int n = 8;
        int[] px = new int[n], pz = new int[n], pf = new int[n];

        for (int i = 0; i < n; i++) {
            double ang = Math.toRadians(22.5 + 45.0 * i);
            double rad = 80 + rnd.nextInt(30);
            int cx = (int) Math.round(Math.cos(ang) * rad);
            int cz = (int) Math.round(Math.sin(ang) * rad);
            int f  = FLOOR - 8 - rnd.nextInt(14);                     
            int hx = 9 + rnd.nextInt(5), hz = 9 + rnd.nextInt(5);
            int doors = Math.abs(cx) > Math.abs(cz) ? (cx > 0 ? 2 : 1) : (cz > 0 ? 8 : 4); 

            building(cx, cz, hx, hz, f, 14 + rnd.nextInt(8), 7 + rnd.nextInt(4), 4, 0.3,
                    Math.max(16, f - 40), 0.10, doors, false);

            int th = 55 + rnd.nextInt(40);
            tower(cx + hx + 3, cz + hz + 3, 22, f + th, 5, 1, 0.35);
            tower(cx - hx - 3, cz - hz - 3, 22, f + th - 12, 4.5, 1, 0.35);

            px[i] = cx; pz[i] = cz; pf[i] = f;
        }

        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            int fl = Math.max(pf[i], pf[j]);
            corridor(true,  pz[i], px[i], px[j], fl, 2, 0.15);
            corridor(false, px[j], pz[i], pz[j], fl, 2, 0.15);
        }
    }

    

    private static void buildPhoenix() {
        Mat blk = UnderworldStructures::phoenixBlack;

        
        for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) {
            cylinder(sx * 6, sz * 6, 1.6, FLOOR + 46, FLOOR + 56, blk);
        }
        ellipsoid(0, 146, 0, 15, 6.5, 8.5, blk);
        ellipsoid(17, 149, 0, 6, 4.5, 3.5, blk);
        ellipsoid(23, 148, 0, 3.8, 3.8, 3.2, blk);
        ellipsoid(28, 147, 0, 3.5, 1.6, 1.4, (x, y, z) -> GOLD);
        ellipsoid(22, 153, 0, 5, 2.5, 0.9, (x, y, z) -> GOLD);

        
        ellipsoid(-26, 144, 0, 14, 2.2, 4.5, blk);
        ellipsoid(-24, 141, -8, 13, 1.8, 2.6, blk);
        ellipsoid(-24, 141, 8, 13, 1.8, 2.6, blk);
        ellipsoid(-39, 143, 0, 3, 1, 1.2, (x, y, z) -> GOLD);

        
        add(-16, 124, -42, 16, 152, 42, (x, y, z) -> {
            int az = Math.abs(z);
            if (az < 6) return null;
            double w = (az - 6) / 34.0;
            double cy = 147 - w * 20;
            if (Math.abs(y - cy) > 2.2 - w * 0.8) return null;
            if (Math.abs(x + w * 8) > 14 * (1 - 0.55 * w)) return null;
            if (w > 0.5 && Math.floorMod(az, 4) == 3) return null;
            return phoenixBlack(x, y, z);
        });

        
        ellipsoid(0, 136, 0, 3.2, 4.6, 3.2, (x, y, z) -> Math.floorMod(y, 3) == 0 ? GOLD : WHITE_CONCRETE);
    }

    

    private static void buildCorpses() {
        buildSerpent(-148, -96,  1);   
        buildSerpent(-148,  96, -1);
        buildBloatedGiant(112, 112);
        buildBoneMountain(112, -112);
        buildWoman(-100, -116);
    }

    private static void buildSerpent(int x0, int zStart, int dir) {
        int n = 22;
        double[][] p = new double[n][3];
        double[] r = new double[n];
        for (int i = 0; i < n; i++) {
            p[i][0] = x0 + 8 * Math.sin(i * 0.55);
            p[i][1] = GROUND + 2.5 + 1.5 * Math.sin(i * 0.9 + 1);
            p[i][2] = zStart + dir * i * 4.0;
            r[i] = 1.6 + 2.2 * Math.sin(Math.PI * i / (n - 1.0));
        }
        r[0] = 3.6; r[1] = 3.8; 
        Mat scales = (x, y, z) -> {
            double h = h3(x, y, z);
            return h < 0.55 ? WHITE_TERRA : h < 0.85 ? GRAY_TERRA : BONE;
        };
        tube(p, r, scales, WHITE_WOOL, 0.55);

        Mat feathers = (x, y, z) -> h3(x, y, z) < 0.25 ? GRAY_WOOL : WHITE_WOOL;
        double[] mid = p[n / 2];
        ellipsoid(mid[0] + 13, mid[1] + 4, mid[2], 13, 1.6, 6, feathers);
        ellipsoid(mid[0] - 13, mid[1] + 3, mid[2], 13, 1.6, 6, feathers);
    }

    private static void buildBloatedGiant(int cx, int cz) {
        Mat rot = (x, y, z) -> {
            double h = h3(x, y, z);
            return h < 0.45 ? WHITE_TERRA : h < 0.75 ? GRAY_TERRA : h < 0.9 ? MOSS : BONE;
        };
        ellipsoid(cx, GROUND + 4, cz, 16, 12, 14, rot);          
        ellipsoid(cx - 20, GROUND + 6, cz + 2, 6, 5, 5.5, rot);  
        ellipsoid(cx - 8, GROUND + 2, cz - 16, 4, 4, 9, rot);    
        ellipsoid(cx - 8, GROUND + 2, cz + 16, 4, 4, 9, rot);
        ellipsoid(cx + 14, GROUND + 2, cz - 9, 8, 4, 4, rot);    
        ellipsoid(cx + 14, GROUND + 2, cz + 9, 8, 4, 4, rot);
    }

    private static void buildBoneMountain(int cx, int cz) {
        Mat bone = (x, y, z) -> h3(x, y, z) < 0.10 ? CALCITE : BONE;
        ellipsoid(cx, GROUND + 5, cz, 14, 9, 16, bone);          
        ellipsoid(cx, GROUND + 26, cz, 11, 20, 15, bone);        
        ellipsoid(cx + 5, GROUND + 52, cz, 8, 9, 8, bone);       
        ellipsoid(cx + 2, GROUND + 24, cz - 19, 5, 15, 5, bone); 
        ellipsoid(cx + 2, GROUND + 24, cz + 19, 5, 15, 5, bone);
        ellipsoid(cx + 16, GROUND + 6, cz - 8, 14, 6, 6, bone);  
        ellipsoid(cx + 16, GROUND + 6, cz + 8, 14, 6, 6, bone);
    }

    private static void buildWoman(int cx, int cz) {
        final int gy = GROUND + 2;
        Mat flesh = (x, y, z) -> h3(x, y, z) < 0.8 ? WHITE_TERRA : GRAY_TERRA;
        ellipsoid(cx, gy + 2, cz, 10, 4, 5.5, flesh);             
        ellipsoid(cx - 19, gy + 1.5, cz, 10, 3, 4, flesh);        
        ellipsoid(cx - 2, gy + 3, cz - 7, 6, 2, 2, flesh);        
        ellipsoid(cx - 2, gy + 3, cz + 7, 6, 2, 2, flesh);

        int hxp = cx + 14;
        
        ellipsoid(hxp, gy + 3, cz, 5, 4.5, 5, (x, y, z) -> {
            if (z - cz > -2) {
                double h = h3(x, y, z);
                return h < 0.55 ? BONE : h < 0.75 ? MOSS : null;
            }
            return WHITE_TERRA;
        });

        
        Random rnd = new Random(77);
        for (int k = 0; k < 12; k++) {
            double a = -1.3 + k * (2.6 / 11.0);
            double dirX = Math.cos(a), dirZ = Math.sin(a);
            double[][] p = new double[7][3];
            double[] r = new double[7];
            for (int j = 0; j < 7; j++) {
                double d = 3 + 3 * j;
                double wob = Math.sin(j * 1.1 + k) * 1.5;
                p[j][0] = hxp + dirX * d - dirZ * wob * 0.5;
                p[j][1] = gy + 2 + wob * 0.6 + rnd.nextDouble();
                p[j][2] = cz + dirZ * d + dirX * wob * 0.5;
                r[j] = 1.4 - 0.1 * j;
            }
            tube(p, r, (x, y, z) -> h3(x, y, z) < 0.7 ? GRAY_TERRA : WHITE_WOOL, null, 0);
        }
    }

    

    private static void buildStyxCurtain() {
        add(-45, 12, -13, -42, 319, 13, (x, y, z) -> {
            int az = Math.abs(z);
            if (h3(x * 7 + 3, y >> 3, z * 5 + 1) < (az / 13.0) * 0.55) return null;   
            if (y < 30 && h3(x, y * 3, z) > (y - 12) / 18.0) return null;             
            double s = h3(x, 77, z);                                                  
            return s < 0.18 ? BLACK_CONCRETE : s < 0.45 ? TINTED : GLASS_BLACK;
        });
    }

    

    private static void buildFloatingIslets() {
        Random rnd = new Random(0xF10A7L);
        for (int i = 0; i < 60; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2;
            boolean nearBridge = false;
            for (double b : UnderworldTerrain.BRIDGE_ANGLES) {
                if (Math.abs(Math.atan2(Math.sin(ang - b), Math.cos(ang - b))) < 0.14) nearBridge = true;
            }
            double rad = 208 + rnd.nextDouble() * 36;
            int cx = (int) Math.round(Math.cos(ang) * rad);
            int cz = (int) Math.round(Math.sin(ang) * rad);
            int cy = 30 + rnd.nextInt(170);
            double rx = 4 + rnd.nextDouble() * 9;
            double rz = rx * (0.8 + rnd.nextDouble() * 0.4);
            int depth = 6 + rnd.nextInt(14);
            var w = UnderworldChunkGenerator.WORLDS[rnd.nextInt(UnderworldTerrain.WORLD_COUNT)];
            if (!nearBridge) islet(cx, cy, cz, rx, rz, depth, w.top(), w.filler(), w.deep());
        }
    }

    private static void islet(int cx, int cy, int cz, double rx, double rz, int depth,
                              BlockState top, BlockState fill, BlockState deep) {
        int bx = (int) Math.ceil(rx) + 1, bz = (int) Math.ceil(rz) + 1;
        add(cx - bx, cy - depth - 1, cz - bz, cx + bx, cy + 3, cz + bz, (x, y, z) -> {
            double nx = (x - cx) / rx, nz = (z - cz) / rz;
            double q = nx * nx + nz * nz;
            if (q > 1) return null;
            double rho = Math.sqrt(q);
            int topY = cy + (int) Math.round((1 - q) * 2.5);
            int botY = cy - (int) Math.round(depth * (1 - rho)) - (h3(x, 0, z) < 0.3 ? 1 : 0);
            if (y > topY || y < botY) return null;
            int d = topY - y;
            return d == 0 ? top : d <= 2 ? fill : deep;
        });
    }
}
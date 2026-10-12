package de.jakob.lotm.dimension;


public final class UnderworldTerrain {

    public static final int MIN_Y  = 0;
    public static final int HEIGHT = 320;

    
    public static final int NATION_Y    = 84;   
    public static final int RIM_Y       = 96;   
    public static final int SEA_Y       = 69;   
    public static final int GORGE_BED   = 36;
    public static final int GORGE_WATER = 46;
    public static final int ABYSS_FLOOR = 6;
    public static final int ABYSS_WATER = 24;
    public static final int CEILING_Y   = 145;  

    
    public static final int NATION_RADIUS = 200;
    public static final int PIT_RADIUS    = 250;
    public static final int GROUND_INNER  = 118;
    public static final int WORLD_COUNT   = 6;

    
    public static final double BRIDGE_HALF_WIDTH = 3.0;
    public static final double[] BRIDGE_ANGLES = {0.0, Math.PI / 2, -Math.PI / 2};
    private static final int BRIDGE_START = NATION_RADIUS - 20;
    private static final int BRIDGE_END   = PIT_RADIUS + 10;

    private static final int[] WORLD_PERM = {0, 6, 3, 1, 7, 4, 2, 5};

    public enum Zone { NATION, CHASM, WILD }

    
    public record Column(Zone zone, int top, int water, int deckTop, boolean edge, boolean pale, boolean bank,
                         boolean shaft) {}

    private UnderworldTerrain() {}

    

    
    public static boolean isPale(int x, int z) {
        double r = Math.sqrt((double) x * x + (double) z * z);
        double rj = r + jitter(x, z);
        if (rj < NATION_RADIUS) return true;
        if (rj < PIT_RADIUS) return false;
        return paleWild(x, z, r);
    }

    public static Column column(int x, int z) {
        double r  = Math.sqrt((double) x * x + (double) z * z);
        double rj = r + jitter(x, z);

        Zone zone;
        int top, level;
        if (rj < NATION_RADIUS) {
            zone = Zone.NATION; top = nationTop(x, z, r); level = ABYSS_WATER;
        } else if (rj < PIT_RADIUS) {
            zone = Zone.CHASM; top = gorgeTop(x, z, rj); level = GORGE_WATER;
        } else {
            zone = Zone.WILD; top = wildTop(x, z, r, rj); level = SEA_Y;
        }

        int water = top < level ? level : -1;
        boolean bank = zone != Zone.NATION && water < 0 && top <= level + 2;
        boolean pale = zone == Zone.NATION || (zone == Zone.WILD && paleWild(x, z, r));
        boolean shaft = zone == Zone.WILD && r > 300 && water < 0 && top > SEA_Y + 6 && top < 150 && shaft(x, z);

        int deck = -1;
        boolean edge = false;
        for (double a : BRIDGE_ANGLES) {
            double dx = Math.cos(a), dz = Math.sin(a);
            double along = x * dx + z * dz;
            double across = Math.abs(-x * dz + z * dx);
            if (across <= BRIDGE_HALF_WIDTH && along > BRIDGE_START && along < BRIDGE_END) {
                double t = smooth((along - BRIDGE_START) / (BRIDGE_END - BRIDGE_START));
                deck = (int) Math.round(NATION_Y + (RIM_Y - NATION_Y) * t);
                edge = across >= BRIDGE_HALF_WIDTH - 0.5;
                break;
            }
        }
        return new Column(zone, top, water, deck, edge, pale, bank, shaft);
    }

    
    public static int worldIndex(int x, int z) {
        double wx = x + noise(x * 0.01 + 1.0, z * 0.01) * 40.0;
        double wz = z + noise(x * 0.01, z * 0.01 + 9.0) * 40.0;
        double t = 0.5 + 0.8 * noise(wx * 0.0035 + 7.0, wz * 0.0035 + 3.0);
        int i = (int) Math.floor(Math.max(0.0, Math.min(0.9999, t)) * WORLD_COUNT);
        int w = WORLD_PERM[i];
        if (noise(wx * 0.02, wz * 0.02) > 0.75) w = (w + 4) % WORLD_COUNT; 
        return w;
    }

    
    public static int ceilingBottom(int x, int z) {
        double h = CEILING_Y + noise(x * 0.008 + 12.0, z * 0.008) * 8.0 + noise(x * 0.03, z * 0.03 + 4.0) * 4.0;
        double sp = noise(x * 0.11 + 40.0, z * 0.11 - 9.0);
        if (sp > 0.3) h -= (sp - 0.3) * 40.0;
        return (int) Math.round(h);
    }

    
    public static boolean cave(int x, int y, int z) {
        double band = smooth((90.0 - y) / 70.0); 
        double a = noise3(x * 0.011, y * 0.018, z * 0.011) * 0.65 + noise3(x * 0.027 + 100.0, y * 0.040, z * 0.027) * 0.35;
        if (a > 0.44 - 0.10 * band) return true; 
        double w = 0.075 + 0.02 * band;
        double t1 = noise3(x * 0.017 + 300.0, y * 0.026, z * 0.017);
        if (Math.abs(t1) >= w) return false;
        double t2 = noise3(x * 0.017 - 300.0, y * 0.026 + 50.0, z * 0.017 + 80.0);
        return Math.abs(t2) < w; 
    }

    public static boolean nearBridge(int x, int z, double margin) {
        for (double a : BRIDGE_ANGLES) {
            double dx = Math.cos(a), dz = Math.sin(a);
            if (x * dx + z * dz > 0 && Math.abs(-x * dz + z * dx) <= BRIDGE_HALF_WIDTH + margin) return true;
        }
        return false;
    }

    

    private static int nationTop(int x, int z, double r) {
        
        double rg = r + noise(x * 0.025 + 5, z * 0.025 + 9) * 14.0 + noise(x * 0.09, z * 0.09) * 4.0;
        if (rg < GROUND_INNER) {
            return ABYSS_FLOOR + (int) Math.round((noise(x * 0.05 + 2, z * 0.05 - 7) + 1.0) * 2.0);
        }
        double amp = smooth((r - 130.0) / 25.0) * (nearBridge(x, z, 12) ? 0.0 : 1.0);
        int top = NATION_Y + (int) Math.round(noise(x * 0.04 + 11, z * 0.04 - 5) * 3.0 * amp);
        double crumble = smooth((GROUND_INNER + 14 - rg) / 14.0);
        return top - (int) Math.round(crumble * 12);
    }

    

    private static int gorgeTop(int x, int z, double rj) {
        double t = (rj - NATION_RADIUS) / (PIT_RADIUS - NATION_RADIUS);
        double down = smooth(t / 0.14);          
        double up   = smooth((t - 0.70) / 0.30); 
        double h = NATION_Y + (GORGE_BED - NATION_Y) * down + (RIM_Y - GORGE_BED) * up;
        h += noise(x * 0.05 + 1, z * 0.05) * 3.0 * Math.min(down, 1.0 - up);
        return (int) Math.round(h);
    }

    

    private static boolean paleWild(int x, int z, double r) {
        return r > 420 && noise(x * 0.0022 + 77.0, z * 0.0022 - 31.0) + noise(x * 0.012, z * 0.012) * 0.1 > 0.22;
    }

    private static int wildTop(int x, int z, double r, double rj) {
        double h = 72.0
                + noise(x * 0.004 + 50, z * 0.004 - 20) * 18.0
                + noise(x * 0.011, z * 0.011 + 7) * 9.0
                + noise(x * 0.045, z * 0.045) * 2.5;

        
        double m = noise(x * 0.0028 - 90, z * 0.0028 + 40);
        if (m > 0.3) {
            double k = smooth((m - 0.3) / 0.45);
            double ridge = 1.0 - Math.abs(noise(x * 0.012 + 5, z * 0.012 - 5));
            h += k * (40.0 + ridge * ridge * 35.0);
        }

        
        h = RIM_Y + (h - RIM_Y) * smooth((rj - PIT_RADIUS - 12.0) / 50.0);

        
        double fade = smooth((r - 275.0) / 25.0);
        if (fade > 0) {
            double d = mainRiverDist(x, z, r);
            double c1 = 1.0 - smooth((d - 4.0) / 24.0);
            double tr = Math.abs(noise(x * 0.0055 + 200.0, z * 0.0055 - 80.0));
            double c2 = 1.0 - smooth((tr - 0.012) / 0.045);
            double c = Math.max(c1, c2) * fade;
            double target = SEA_Y - (c1 >= c2 ? 6 : 4);
            if (h > target) h += (target - h) * c;
        }
        return Math.max(8, (int) Math.round(h));
    }

    private static double riverTheta(double r) {
        return 0.6 + r * 0.0060 + noise(r * 0.007 + 19.0, 4.2) * 0.6;
    }

    
    private static double mainRiverDist(int x, int z, double r) {
        double phi = Math.atan2(z, x);
        double diff = phi - riverTheta(r);
        diff -= 2 * Math.PI * Math.floor((diff + Math.PI) / (2 * Math.PI));
        double dth = (riverTheta(r + 3) - riverTheta(r - 3)) / 6.0;
        return Math.abs(diff) * r / Math.sqrt(1.0 + r * r * dth * dth);
    }

    
    private static boolean shaft(int x, int z) {
        int cx = Math.floorDiv(x, 96), cz = Math.floorDiv(z, 96);
        if (hash3(cx, 31, cz) > 0.5) return false;
        double sx = cx * 96 + 16 + hash3(cx, 32, cz) * 64, sz = cz * 96 + 16 + hash3(cx, 33, cz) * 64;
        double rad = 5.0 + hash3(cx, 34, cz) * 5.0;
        double dx = x - sx, dz = z - sz;
        return Math.sqrt(dx * dx + dz * dz) < rad * (1.0 + 0.25 * noise(x * 0.12, z * 0.12));
    }

    

    private static double jitter(int x, int z) {
        return noise(x * 0.012 + 31.7, z * 0.012 - 12.3) * 14.0 + noise(x * 0.05, z * 0.05) * 3.0;
    }

    public static double smooth(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }

    
    public static double noise(double x, double z) {
        int xi = (int) Math.floor(x), zi = (int) Math.floor(z);
        double xf = x - xi, zf = z - zi;
        double u = xf * xf * (3 - 2 * xf), v = zf * zf * (3 - 2 * zf);
        double a = hash2(xi, zi), b = hash2(xi + 1, zi), c = hash2(xi, zi + 1), d = hash2(xi + 1, zi + 1);
        double top = a + (b - a) * u, bot = c + (d - c) * u;
        return (top + (bot - top) * v) * 2.0 - 1.0;
    }

    
    public static double noise3(double x, double y, double z) {
        int xi = (int) Math.floor(x), yi = (int) Math.floor(y), zi = (int) Math.floor(z);
        double xf = x - xi, yf = y - yi, zf = z - zi;
        double u = xf * xf * (3 - 2 * xf), v = yf * yf * (3 - 2 * yf), w = zf * zf * (3 - 2 * zf);
        double c000 = hash3(xi, yi, zi),         c100 = hash3(xi + 1, yi, zi);
        double c010 = hash3(xi, yi + 1, zi),     c110 = hash3(xi + 1, yi + 1, zi);
        double c001 = hash3(xi, yi, zi + 1),     c101 = hash3(xi + 1, yi, zi + 1);
        double c011 = hash3(xi, yi + 1, zi + 1), c111 = hash3(xi + 1, yi + 1, zi + 1);
        double x00 = c000 + (c100 - c000) * u, x10 = c010 + (c110 - c010) * u;
        double x01 = c001 + (c101 - c001) * u, x11 = c011 + (c111 - c011) * u;
        double y0 = x00 + (x10 - x00) * v, y1 = x01 + (x11 - x01) * v;
        return (y0 + (y1 - y0) * w) * 2.0 - 1.0;
    }

    
    public static double hash2(int x, int z) {
        long h = (long) x * 0x9E3779B97F4A7C15L ^ (long) z * 0xC2B2AE3D27D4EB4FL;
        return finish(h);
    }

    public static double hash3(int x, int y, int z) {
        long h = (long) x * 0x9E3779B97F4A7C15L ^ (long) y * 0xD6E8FEB86659FD93L ^ (long) z * 0xC2B2AE3D27D4EB4FL;
        return finish(h);
    }

    private static double finish(long h) {
        h ^= h >>> 33; h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33; h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h >>> 11) * (1.0 / (1L << 53));
    }
}
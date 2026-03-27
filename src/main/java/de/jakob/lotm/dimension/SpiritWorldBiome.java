package de.jakob.lotm.dimension;

/**
 * Defines all Spirit World biomes. Each biome controls three things:
 *
 *  1. GENERATION MODE  – fundamentally different island geometry per biome
 *  2. TERRAIN PARAMS   – radius, height, depth, grid spacing, etc.
 *  3. FOG COLOUR       – unique animated colour theme
 *
 * Biome regions are large (~1800-block Voronoi cells) so that the player
 * must travel a meaningful distance to reach a new biome.
 *
 * Generation modes
 * ----------------
 *  ARCHIPELAGO  – many medium islands clustered together (Wool Meadows)
 *  SPIRE        – very tall, razor-thin crystal columns (Crystalline Peaks)
 *  SCATTERED    – hundreds of tiny islands spread across a huge Y range (Void Gardens, Fungal Depths)
 *  CONTINENTAL  – enormous, nearly continent-sized solid landmasses (Ember Wastes, Glacial Shelf)
 *  PLATEAU      – huge perfectly flat-topped tables (Quartz Flats, Gilded Ruins)
 *  CANYON       – large masses with deep layered canyon relief (Terracotta Canyon)
 */
public enum SpiritWorldBiome {

    // -------------------------------------------------------------------------
    // Biome declarations
    // -------------------------------------------------------------------------

    // Wool Meadows: increased gridSize (smaller grids = more islands), higher spawn chance,
    // and a wider radius range so islands are larger and more frequent.
    WOOL_MEADOWS(
            GenerationMode.ARCHIPELAGO,
            new TerrainParams(
                    64, 24, 0.55f, 0.85f, 24, 64, 72, -8, 14, 1.0f
            )
    ),

    CRYSTALLINE_PEAKS(
            GenerationMode.SPIRE,
            new TerrainParams(
                    55, 90, 0.10f, 0.80f, 3, 12, 55, -5, 70, 3.5f
            )
    ),

    VOID_GARDENS(
            GenerationMode.SCATTERED,
            new TerrainParams(
                    96, 12, 0.45f, 0.45f, 6, 24, 65, -86, 94, 1.4f
            )
    ),

    EMBER_WASTES(
            GenerationMode.CONTINENTAL,
            new TerrainParams(
                    60, 8, 0.90f, 0.42f, 80, 200, 220, -6, 10, 0.55f
            )
    ),

    QUARTZ_FLATS(
            GenerationMode.PLATEAU,
            new TerrainParams(
                    62, 4, 0.15f, 0.30f, 90, 280, 300, -4, 16, 0.45f
            )
    ),

    TERRACOTTA_CANYON(
            GenerationMode.CANYON,
            new TerrainParams(
                    58, 38, 0.85f, 0.38f, 50, 130, 180, -10, 26, 0.7f
            )
    ),

    FUNGAL_DEPTHS(
            GenerationMode.SCATTERED,
            new TerrainParams(
                    70, 16, 0.55f, 0.55f, 8, 32, 60, -60, 80, 1.6f
            )
    ),

    GLACIAL_SHELF(
            GenerationMode.CONTINENTAL,
            new TerrainParams(
                    58, 10, 0.80f, 0.38f, 70, 180, 200, -4, 8, 0.60f
            )
    );

    /** A biome and its normalised blend contribution at a given world position. */
    public record BiomeWeight(SpiritWorldBiome biome, double weight) {}

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    public final GenerationMode mode;
    public final TerrainParams terrain;

    SpiritWorldBiome(GenerationMode mode, TerrainParams terrain) {
        this.mode    = mode;
        this.terrain = terrain;
    }

    // -------------------------------------------------------------------------
    // Generation modes
    // -------------------------------------------------------------------------

    public enum GenerationMode {
        ARCHIPELAGO,
        SPIRE,
        SCATTERED,
        CONTINENTAL,
        PLATEAU,
        CANYON
    }

    // -------------------------------------------------------------------------
    // Block palette weights
    // -------------------------------------------------------------------------

    /**
     * Per-biome weights for each PatchType, indexed by PatchType ordinal:
     *   0=WOOL  1=AMETHYST  2=PRISMARINE  3=END_STONE  4=QUARTZ
     *   5=TERRACOTTA  6=NETHERRACK  7=BLACKSTONE  8=BASALT
     *   9=DEEPSLATE  10=STONE  11=GRASS  12=ICE  13=MUSHROOM  14=COPPER  15=GOLD_BLOCK
     */
    public double[] getPatchWeights() {
        return switch (this) {
            case WOOL_MEADOWS      -> new double[]{ 0.48, 0.06, 0.03, 0.01, 0.02, 0.10, 0.01, 0.005, 0.003, 0.002, 0.001, 0.22, 0.0, 0.0, 0.0, 0.0 };
            case CRYSTALLINE_PEAKS -> new double[]{ 0.02, 0.46, 0.30, 0.04, 0.14, 0.01, 0.005, 0.005, 0.003, 0.008, 0.001, 0.003, 0.0, 0.0, 0.0, 0.0 };
            case VOID_GARDENS      -> new double[]{ 0.03, 0.18, 0.12, 0.52, 0.05, 0.01, 0.03, 0.02, 0.004, 0.010, 0.002, 0.002, 0.0, 0.0, 0.0, 0.0 };
            case EMBER_WASTES      -> new double[]{ 0.005, 0.02, 0.01, 0.01, 0.01, 0.02, 0.50, 0.30, 0.12, 0.01, 0.003, 0.002, 0.0, 0.0, 0.0, 0.0 };
            case QUARTZ_FLATS      -> new double[]{ 0.01, 0.10, 0.08, 0.02, 0.72, 0.02, 0.005, 0.005, 0.005, 0.003, 0.002, 0.002, 0.0, 0.0, 0.0, 0.0 };
            case TERRACOTTA_CANYON -> new double[]{ 0.03, 0.03, 0.02, 0.02, 0.02, 0.78, 0.02, 0.02, 0.02, 0.005, 0.003, 0.03, 0.0, 0.0, 0.0, 0.0 };
            // Ice dominates, with snow, packed ice, blue ice accents
            case GLACIAL_SHELF     -> new double[]{ 0.0, 0.02, 0.01, 0.005, 0.02, 0.0, 0.0, 0.005, 0.01, 0.03, 0.005, 0.0, 0.90, 0.0, 0.0, 0.0 };
            // Glowing mushroom blocks, nylium variants, mycelium-toned deepslate
            case FUNGAL_DEPTHS     -> new double[]{ 0.0, 0.03, 0.0, 0.02, 0.0, 0.0, 0.02, 0.01, 0.005, 0.04, 0.005, 0.0, 0.0, 0.88, 0.0, 0.0 };
        };
    }

    // -------------------------------------------------------------------------
    // Fog colour
    // -------------------------------------------------------------------------

    public float[] getFogColor(long timeMs) {
        return switch (this) {
            case WOOL_MEADOWS -> {
                float[] c1 = hsb(hue(timeMs,  5,   0),   1.00f, 1.0f);
                float[] c2 = hsb(hue(timeMs, 80, 180),   0.95f, 1.0f);
                float[] c3 = hsb(hue(timeMs, 150,  90),  0.90f, 1.0f);
                float p = sinPulse(timeMs, 100, 0.30f, 0.70f);
                yield new float[]{
                        (c1[0]*0.5f + c2[0]*0.3f + c3[0]*0.2f) * p,
                        (c1[1]*0.5f + c2[1]*0.3f + c3[1]*0.2f) * p,
                        (c1[2]*0.5f + c2[2]*0.3f + c3[2]*0.2f) * p
                };
            }
            case CRYSTALLINE_PEAKS -> {
                float h = 0.56f + (float) Math.sin(timeMs / 8000.0) * 0.14f;
                float s = 0.75f + sinPulse(timeMs, 300, 0.12f, 0.0f);
                float[] c = hsb(h, s, 1.0f);
                float p = sinPulse(timeMs, 220, 0.10f, 0.90f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            case VOID_GARDENS -> {
                float h = 0.72f + (float) Math.sin(timeMs / 5000.0) * 0.06f;
                float[] c = hsb(h, 0.88f, 0.80f);
                float p = sinPulse(timeMs, 350, 0.20f, 0.80f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            case EMBER_WASTES -> {
                float baseH  = 0.01f + (float) Math.sin(timeMs / 900.0) * 0.04f;
                float[] fire = hsb(baseH, 1.00f, 1.0f);
                float[] glow = hsb(0.07f, 0.90f, 0.9f);
                float mix = sinPulse(timeMs, 55, 0.40f, 0.60f);
                float p   = sinPulse(timeMs, 40, 0.28f, 0.72f);
                yield new float[]{
                        (fire[0]*mix + glow[0]*(1-mix)) * p,
                        (fire[1]*mix + glow[1]*(1-mix)) * p,
                        (fire[2]*mix + glow[2]*(1-mix)) * p
                };
            }
            case QUARTZ_FLATS -> {
                float h = 0.10f + (float) Math.sin(timeMs / 7000.0) * 0.05f;
                float[] c = hsb(h, 0.18f, 1.0f);
                float p = sinPulse(timeMs, 600, 0.04f, 0.96f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            case TERRACOTTA_CANYON -> {
                float h = 0.06f + (float) Math.sin(timeMs / 3500.0) * 0.04f;
                float s = 0.88f + sinPulse(timeMs, 180, 0.08f, 0.0f);
                float[] c = hsb(h, s, 0.95f);
                float p = sinPulse(timeMs, 200, 0.16f, 0.84f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            // Bioluminescent blue-green spore fog, slow pulse like underwater
            case FUNGAL_DEPTHS -> {
                float h = 0.40f + (float) Math.sin(timeMs / 6000.0) * 0.08f;
                float s = 0.90f + sinPulse(timeMs, 400, 0.06f, 0.0f);
                float[] c = hsb(h, s, 0.70f);
                float p = sinPulse(timeMs, 500, 0.18f, 0.82f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            // Cold arctic white-blue, slow shimmer like aurora
            case GLACIAL_SHELF -> {
                float h = 0.58f + (float) Math.sin(timeMs / 9000.0) * 0.06f;
                float s = 0.40f + sinPulse(timeMs, 700, 0.10f, 0.0f);
                float[] c = hsb(h, s, 0.98f);
                float p = sinPulse(timeMs, 800, 0.04f, 0.96f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
        };
    }

    // -------------------------------------------------------------------------
    // Biome determination – large Voronoi cells
    // -------------------------------------------------------------------------

    private static final int    BIOME_GRID    = 250;

    /** Width (in blocks) of the cross-fade zone between two adjacent biomes. */
    private static final double BLEND_RADIUS  = 220.0;

    /**
     * Returns the nearest 1–2 Voronoi biome cells with smooth blend weights.
     *
     * Deep inside a biome: only one entry with weight 1.0.
     * Within {@code BLEND_RADIUS} blocks of any border: two entries whose
     * weights sum to 1.0, smoothly interpolated so neither hard-cuts.
     */
    public static BiomeWeight[] getBlendedBiomesAt(int x, int z) {
        SpiritWorldBiome[] values = values();
        double d1sq = Double.MAX_VALUE, d2sq = Double.MAX_VALUE;
        SpiritWorldBiome b1 = WOOL_MEADOWS, b2 = WOOL_MEADOWS;

        int gridX = Math.floorDiv(x, BIOME_GRID);
        int gridZ = Math.floorDiv(z, BIOME_GRID);

        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                int  cx   = gridX + ox, cz = gridZ + oz;
                long seed = jenkinsHash((long) cx * 1_234_567_891L + (long) cz * 987_654_321L);

                int px = cx * BIOME_GRID + (int)(pseudoRand(seed)                 * (BIOME_GRID - 1));
                int pz = cz * BIOME_GRID + (int)(pseudoRand(seed ^ 0xDEAD_BEEFL) * (BIOME_GRID - 1));

                double dsq = (double)(x - px) * (x - px) + (double)(z - pz) * (z - pz);
                int idx = Math.min(
                        Math.abs((int)(pseudoRand(seed ^ 0xCAFE_BABEL) * values.length)),
                        values.length - 1);
                SpiritWorldBiome b = values[idx];

                if (dsq < d1sq) { d2sq = d1sq; b2 = b1; d1sq = dsq; b1 = b; }
                else if (dsq < d2sq) { d2sq = dsq; b2 = b; }
            }
        }

        // Gap between nearest and second-nearest encodes boundary proximity.
        //   gap → 0   : right on border  → 50 / 50 blend
        //   gap → BLEND_RADIUS : deep in biome → 100 / 0
        double gap = Math.sqrt(d2sq) - Math.sqrt(d1sq);
        double t   = biomeSmooth(Math.min(gap / BLEND_RADIUS, 1.0));
        double w1  = 0.5 + t * 0.5;   // [0.5 … 1.0]
        double w2  = 1.0 - w1;         // [0.5 … 0.0]

        return w2 > 0.01
                ? new BiomeWeight[]{ new BiomeWeight(b1, w1), new BiomeWeight(b2, w2) }
                : new BiomeWeight[]{ new BiomeWeight(b1, 1.0) };
    }


    public static SpiritWorldBiome getBiomeAt(int x, int z) {
        return getBlendedBiomesAt(x, z)[0].biome();
    }

    private static double biomeSmooth(double x) {
        x = Math.max(0.0, Math.min(1.0, x));
        return x * x * (3.0 - 2.0 * x);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static float hue(long t, long periodMs, float offsetDeg) {
        return ((t / periodMs + (long) offsetDeg) % 360L) / 360.0f;
    }

    private static float sinPulse(long t, long periodMs, float amp, float base) {
        return (float) Math.sin(t / (double) periodMs) * amp + base;
    }

    static float[] hsb(float h, float s, float b) {
        int rgb = java.awt.Color.HSBtoRGB(h, s, b);
        return new float[]{
                ((rgb >> 16) & 0xFF) / 255.0f,
                ((rgb >>  8) & 0xFF) / 255.0f,
                ( rgb        & 0xFF) / 255.0f
        };
    }

    private static double pseudoRand(long seed) {
        seed ^= seed << 21;
        seed ^= seed >> 35;
        seed ^= seed << 4;
        return (double)(seed >>> 1) / (double)(1L << 62);
    }

    private static long jenkinsHash(long x) {
        x = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
        x = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
        return x ^ (x >>> 31);
    }

    // -------------------------------------------------------------------------
    // TerrainParams record
    // -------------------------------------------------------------------------

    public record TerrainParams(
            int baseHeight,
            int heightVariation,
            float depthMultiplier,
            float islandSpawnChance,
            int minRadius,
            int maxRadius,
            int gridSize,
            int yOffsetMin,
            int yOffsetMax,
            float edgeSharpness
    ) {}
}
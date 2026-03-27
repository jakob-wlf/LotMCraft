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
 *  SCATTERED    – hundreds of tiny islands spread across a huge Y range (Void Gardens)
 *  CONTINENTAL  – enormous, nearly continent-sized solid landmasses (Ember Wastes)
 *  PLATEAU      – huge perfectly flat-topped tables (Quartz Flats)
 *  CANYON       – large masses with deep layered canyon relief (Terracotta Canyon)
 */
public enum SpiritWorldBiome {

    // -------------------------------------------------------------------------
    // Biome declarations
    // -------------------------------------------------------------------------

    WOOL_MEADOWS(
            GenerationMode.ARCHIPELAGO,
            new TerrainParams(
                    64, 20, 0.55f, 0.72f, 18, 48, 90, -8, 14, 1.0f
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
    );

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
     *   9=DEEPSLATE  10=STONE  11=GRASS
     */
    public double[] getPatchWeights() {
        return switch (this) {
            case WOOL_MEADOWS      -> new double[]{ 0.48, 0.06, 0.03, 0.01, 0.02, 0.10, 0.01, 0.005, 0.003, 0.002, 0.001, 0.22 };
            case CRYSTALLINE_PEAKS -> new double[]{ 0.02, 0.46, 0.30, 0.04, 0.14, 0.01, 0.005, 0.005, 0.003, 0.008, 0.001, 0.003};
            case VOID_GARDENS      -> new double[]{ 0.03, 0.18, 0.12, 0.52, 0.05, 0.01, 0.03, 0.02, 0.004, 0.010, 0.002, 0.002};
            case EMBER_WASTES      -> new double[]{ 0.005, 0.02, 0.01, 0.01, 0.01, 0.02, 0.50, 0.30, 0.12, 0.01, 0.003, 0.002};
            case QUARTZ_FLATS      -> new double[]{ 0.01, 0.10, 0.08, 0.02, 0.72, 0.02, 0.005, 0.005, 0.005, 0.003, 0.002, 0.002};
            case TERRACOTTA_CANYON -> new double[]{ 0.03, 0.03, 0.02, 0.02, 0.02, 0.78, 0.02, 0.02, 0.02, 0.005, 0.003, 0.03 };
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
        };
    }

    // -------------------------------------------------------------------------
    // Biome determination – large Voronoi cells
    // -------------------------------------------------------------------------

    private static final int BIOME_GRID = 750;

    public static SpiritWorldBiome getBiomeAt(int x, int z) {
        SpiritWorldBiome[] values = values();
        SpiritWorldBiome result = WOOL_MEADOWS;
        double minDistSq = Double.MAX_VALUE;

        int gridX = Math.floorDiv(x, BIOME_GRID);
        int gridZ = Math.floorDiv(z, BIOME_GRID);

        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                int cx = gridX + ox;
                int cz = gridZ + oz;

                // Use a robust hash that won't overflow into sign issues
                long seed = jenkinsHash((long) cx * 1_234_567_891L + (long) cz * 987_654_321L);

                // Offset within the grid cell — guaranteed [0, BIOME_GRID)
                int px = cx * BIOME_GRID + (int)(pseudoRand(seed)                 * (BIOME_GRID - 1));
                int pz = cz * BIOME_GRID + (int)(pseudoRand(seed ^ 0xDEAD_BEEFL) * (BIOME_GRID - 1));

                double distSq = (double)(x - px) * (x - px) + (double)(z - pz) * (z - pz);
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    // Map this cell to a biome index deterministically
                    int idx = (int)(pseudoRand(seed ^ 0xCAFE_BABEL) * values.length);
                    // Clamp strictly — pseudoRand returns [0,1) so this is safe,
                    // but guard against any floating-point edge case.
                    idx = Math.min(Math.abs(idx), values.length - 1);
                    result = values[idx];
                }
            }
        }
        return result;
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

    /**
     * Returns a value in [0.0, 1.0).
     *
     * The key fix vs the original: we shift right by 1 (unsigned) so the
     * sign bit is always 0, guaranteeing a non-negative long before dividing.
     * The original used {@code seed & Long.MAX_VALUE} which still produced
     * very large longs that when cast to double / Long.MAX_VALUE could equal
     * exactly 1.0 due to floating-point rounding, breaking the < length check.
     */
    private static double pseudoRand(long seed) {
        seed ^= seed << 21;
        seed ^= seed >> 35;
        seed ^= seed << 4;
        // Unsigned right-shift strips the sign bit → always non-negative
        return (double)(seed >>> 1) / (double)(1L << 62);
    }

    /**
     * Jenkins one-at-a-time style integer finalisation.
     * Mixes the bits more thoroughly than a simple XOR-shift so nearby cell
     * coordinates don't produce correlated biome assignments.
     */
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
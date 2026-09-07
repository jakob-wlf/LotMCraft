package de.jakob.lotm.dimension;


public enum SpiritWorldBiome {

    WOOL_MEADOWS(
            GenerationMode.ARCHIPELAGO,
            new TerrainParams(64, 24, 0.55f, 0.85f, 24, 64, 72, -8, 14, 1.0f,
                    300)
    ),
    CRYSTALLINE_PEAKS(
            GenerationMode.SPIRE,
            new TerrainParams(55, 90, 0.10f, 0.80f, 3, 12, 55, -5, 70, 3.5f,
                    250)
    ),
    VOID_GARDENS(
            GenerationMode.SCATTERED,
            new TerrainParams(96, 12, 0.45f, 0.45f, 6, 24, 65, -86, 94, 1.4f,
                    250)
    ),
    EMBER_WASTES(
            GenerationMode.CONTINENTAL,
            new TerrainParams(60, 8, 0.90f, 0.42f, 80, 200, 220, -6, 10, 0.55f,
                    250)
    ),
    QUARTZ_FLATS(
            GenerationMode.PLATEAU,
            new TerrainParams(62, 4, 0.15f, 0.30f, 90, 280, 300, -4, 16, 0.45f,
                    250)
    ),
    TERRACOTTA_CANYON(
            GenerationMode.CANYON,
            new TerrainParams(58, 38, 0.85f, 0.38f, 50, 130, 180, -10, 26, 0.7f,
                    250)
    ),
    FUNGAL_DEPTHS(
            GenerationMode.SCATTERED,
            new TerrainParams(70, 16, 0.55f, 0.55f, 8, 32, 60, -60, 80, 1.6f,
                    250)
    ),
    GILDED_RUINS(
            GenerationMode.PLATEAU,
            new TerrainParams(60, 6, 0.20f, 0.35f, 80, 260, 280, -4, 14, 0.45f, 250)
    ),
    GLACIAL_SHELF(
            GenerationMode.CONTINENTAL,
            new TerrainParams(58, 10, 0.80f, 0.38f, 70, 180, 200, -4, 8, 0.60f,
                    250)
    ),

    SPIRIT_WORLD_WASTES(
            GenerationMode.CHAOS,
            new TerrainParams(64, 50, 0.65f, 0.55f, 20, 70, 85, -30, 50, 1.6f,
                    300)
    ),
    MIRE_HOLLOW(
            GenerationMode.SCATTERED,
            new TerrainParams(58, 14, 0.50f, 0.50f, 10, 30, 62, -40, 50, 1.3f,
                    250)
    ),
    BONE_STEPPES(
            GenerationMode.PLATEAU,
            new TerrainParams(60, 5, 0.30f, 0.18f, 85, 270, 290, -4, 15, 0.5f,
                    250)
    );


    public record BiomeWeight(SpiritWorldBiome biome, double weight) {}

    public final GenerationMode mode;
    public final TerrainParams terrain;

    SpiritWorldBiome(GenerationMode mode, TerrainParams terrain) {
        this.mode    = mode;
        this.terrain = terrain;
    }

    public enum GenerationMode {
        ARCHIPELAGO,
        SPIRE,
        SCATTERED,
        CONTINENTAL,
        PLATEAU,
        CANYON,
        CHAOS
    }


    public double[] getPatchWeights() {
        return switch (this) {
            case WOOL_MEADOWS      -> new double[]{ 0.48, 0.06, 0.03, 0.01, 0.02, 0.10, 0.01, 0.005, 0.003, 0.002, 0.001, 0.22, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
            case CRYSTALLINE_PEAKS -> new double[]{ 0.02, 0.46, 0.30, 0.04, 0.14, 0.01, 0.005, 0.005, 0.003, 0.008, 0.001, 0.003, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
            case VOID_GARDENS      -> new double[]{ 0.03, 0.18, 0.12, 0.52, 0.05, 0.01, 0.03, 0.02, 0.004, 0.010, 0.002, 0.002, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
            case EMBER_WASTES      -> new double[]{ 0.005, 0.02, 0.01, 0.01, 0.01, 0.02, 0.50, 0.30, 0.12, 0.01, 0.003, 0.002, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
            case QUARTZ_FLATS      -> new double[]{ 0.01, 0.10, 0.08, 0.02, 0.72, 0.02, 0.005, 0.005, 0.005, 0.003, 0.002, 0.002, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
            case TERRACOTTA_CANYON -> new double[]{ 0.03, 0.03, 0.02, 0.02, 0.02, 0.78, 0.02, 0.02, 0.02, 0.005, 0.003, 0.03, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
            case GLACIAL_SHELF     -> new double[]{ 0.0, 0.02, 0.01, 0.005, 0.02, 0.0, 0.0, 0.005, 0.01, 0.03, 0.005, 0.0, 0.90, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
            case FUNGAL_DEPTHS     -> new double[]{ 0.0, 0.03, 0.0, 0.02, 0.0, 0.0, 0.02, 0.01, 0.005, 0.04, 0.005, 0.0, 0.0, 0.88, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
            case GILDED_RUINS -> new double[]{ 0.0, 0.02, 0.01, 0.01, 0.02, 0.01, 0.0, 0.05, 0.0, 0.05, 0.01, 0.0, 0.0, 0.0, 0.60, 0.27, 0.0, 0.0, 0.0, 0.0 };
            case SPIRIT_WORLD_WASTES -> new double[]{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08, 0.10, 0.15, 0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.55, 0.0, 0.0, 0.07 };
            case MIRE_HOLLOW          -> new double[]{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05, 0.05, 0.08, 0.0, 0.0, 0.0, 0.0, 0.0, 0.80, 0.0, 0.02 };
            case BONE_STEPPES         -> new double[]{ 0.0, 0.02, 0.0, 0.0, 0.15, 0.0, 0.0, 0.0, 0.0, 0.03, 0.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.75, 0.0 };
        };
    }

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
            case GILDED_RUINS -> {
                float h = 0.10f + (float) Math.sin(timeMs / 5000.0) * 0.03f;
                float[] c = hsb(h, 0.70f, 0.85f);
                float p = sinPulse(timeMs, 400, 0.08f, 0.92f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            case CRYSTALLINE_PEAKS -> {
                float h = 0.56f + (float) Math.sin(timeMs / 8000.0) * 0.14f;
                float s = 0.75f + sinPulse(timeMs, 300, 0.12f, 0.0f);
                float[] c = hsb(h, s, 1.0f);
                float p = sinPulse(timeMs, 220, 0.10f, 0.90f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            case VOID_GARDENS -> hsb(0.75f, 0.80f, 0.15f);
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
            case FUNGAL_DEPTHS -> {
                float h = 0.40f + (float) Math.sin(timeMs / 6000.0) * 0.08f;
                float s = 0.90f + sinPulse(timeMs, 400, 0.06f, 0.0f);
                float[] c = hsb(h, s, 0.70f);
                float p = sinPulse(timeMs, 500, 0.18f, 0.82f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            case GLACIAL_SHELF -> {
                float h = 0.58f + (float) Math.sin(timeMs / 9000.0) * 0.06f;
                float s = 0.40f + sinPulse(timeMs, 700, 0.10f, 0.0f);
                float[] c = hsb(h, s, 0.98f);
                float p = sinPulse(timeMs, 800, 0.04f, 0.96f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            case SPIRIT_WORLD_WASTES -> {
                float h = 0.75f + (float) Math.sin(timeMs / 6000.0) * 0.05f;
                float s = 0.20f + sinPulse(timeMs, 500, 0.08f, 0.0f);
                float[] c = hsb(h, s, 0.38f);
                float p = sinPulse(timeMs, 350, 0.12f, 0.55f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            case MIRE_HOLLOW -> {
                float h = 0.28f + (float) Math.sin(timeMs / 5000.0) * 0.03f;
                float[] c = hsb(h, 0.55f, 0.42f);
                float p = sinPulse(timeMs, 450, 0.12f, 0.55f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
            case BONE_STEPPES -> {
                float h = 0.12f + (float) Math.sin(timeMs / 8000.0) * 0.02f;
                float[] c = hsb(h, 0.15f, 0.90f);
                float p = sinPulse(timeMs, 700, 0.05f, 0.90f);
                yield new float[]{ c[0]*p, c[1]*p, c[2]*p };
            }
        };
    }


    private static final double BLEND_RADIUS  = 220.0;

    private static final SpiritWorldBiome[] WEIGHTED_POOL = buildWeightedPool();

    private static SpiritWorldBiome[] buildWeightedPool() {
        return values();
    }

    public static BiomeWeight[] getBlendedBiomesAt(int x, int z) {
        SpiritWorldBiome[] values = values();
        double d1sq = Double.MAX_VALUE, d2sq = Double.MAX_VALUE;
        SpiritWorldBiome b1 = SPIRIT_WORLD_WASTES, b2 = SPIRIT_WORLD_WASTES;

        int searchStride = 0;
        for (SpiritWorldBiome b : values()) searchStride = Math.max(searchStride, b.terrain.cellSize());

        int gridX = Math.floorDiv(x, searchStride);
        int gridZ = Math.floorDiv(z, searchStride);

        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                int  cx   = gridX + ox, cz = gridZ + oz;
                long seed = jenkinsHash((long) cx * 1_234_567_891L + (long) cz * 987_654_321L);

                int poolIdx = Math.min(
                        Math.abs((int)(pseudoRand(seed ^ 0xCAFE_BABEL) * WEIGHTED_POOL.length)),
                        WEIGHTED_POOL.length - 1);
                SpiritWorldBiome b = WEIGHTED_POOL[poolIdx];

                int cs = b.terrain.cellSize();
                int px = cx * cs + (int)(pseudoRand(seed)                * (cs - 1));
                int pz = cz * cs + (int)(pseudoRand(seed ^ 0xDEAD_BEEFL) * (cs - 1));

                double dsq = (double)(x - px) * (x - px) + (double)(z - pz) * (z - pz);
                if (dsq < d1sq) { d2sq = d1sq; b2 = b1; d1sq = dsq; b1 = b; }
                else if (dsq < d2sq) { d2sq = dsq; b2 = b; }
            }
        }

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

    public record TerrainParams(
            int baseHeight, int heightVariation,
            float depthMultiplier, float islandSpawnChance,
            int minRadius, int maxRadius, int gridSize,
            int yOffsetMin, int yOffsetMax, float edgeSharpness,
            int cellSize
    ) {}
}
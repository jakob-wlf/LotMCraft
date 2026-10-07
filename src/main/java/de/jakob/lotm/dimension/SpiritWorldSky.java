package de.jakob.lotm.dimension;

import net.minecraft.world.phys.Vec3;

public final class SpiritWorldSky {

    private static final Vec3 BASE_TOP     = new Vec3(0.06, 0.05, 0.18);
    private static final Vec3 BASE_HORIZON = new Vec3(0.38, 0.62, 0.78);

    private static final float[] tint = {1f, 1f, 1f};
    private static long lastMs = -1;

    private SpiritWorldSky() {}

    public static void update(int blockX, int blockZ) {
        long now = System.currentTimeMillis();
        if (now == lastMs) return;

        float[] target = SpiritWorldBiome.getBiomeAt(blockX, blockZ).getFogColor(0L).clone();
        float max = Math.max(target[0], Math.max(target[1], target[2]));
        if (max > 0.001f) {
            target[0] /= max; target[1] /= max; target[2] /= max;
        }

        float k = lastMs < 0 ? 1f : 1f - (float) Math.exp(-(now - lastMs) / 1500.0); // ~1.5s easing
        for (int i = 0; i < 3; i++) tint[i] += (target[i] - tint[i]) * k;
        lastMs = now;
    }

    public static Vec3 tint() {
        return new Vec3(tint[0], tint[1], tint[2]);
    }

    public static Vec3 horizon() {
        return BASE_HORIZON.lerp(BASE_HORIZON.multiply(tint()), 0.75);
    }

    public static Vec3 top() {
        return BASE_TOP.lerp(BASE_TOP.multiply(tint()), 0.75);
    }
}
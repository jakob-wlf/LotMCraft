package de.jakob.lotm.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientAccommodationCache {
    private static int progressTicks = 0;
    private static int totalTicks = 0;

    public static int getProgressTicks() {
        return progressTicks;
    }

    public static int getTotalTicks() {
        return totalTicks;
    }

    public static void setProgress(int progress, int total) {
        progressTicks = Math.max(0, progress);
        totalTicks = Math.max(0, total);
    }

    public static void reset() {
        progressTicks = 0;
        totalTicks = 0;
    }
}

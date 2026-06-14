package de.jakob.lotm.util.data;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSacrificeCache {
    private static int killCount = 0;
    private static int remainingTicks = 0;
    private static int totalTicks = 0;

    public static int getKillCount() {
        return killCount;
    }

    public static void setKillCount(int count) {
        killCount = count;
    }

    public static int getRemainingTicks() {
        return remainingTicks;
    }

    public static void setRemainingTicks(int ticks) {
        remainingTicks = ticks;
    }

    public static int getTotalTicks() {
        return totalTicks;
    }

    public static void setTotalTicks(int ticks) {
        totalTicks = ticks;
    }

    public static void resetSacrificeDuration() {
        remainingTicks = 0;
        totalTicks = 0;
    }

    public static void tickDown() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }
}

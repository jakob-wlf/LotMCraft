package de.jakob.lotm.util.data;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientUniquenessCache {
    private static boolean hasUniqueness = false;
    private static String pathway = "";
    private static int killCount = 0;

    public static boolean hasUniqueness() {
        return hasUniqueness;
    }

    public static void setHasUniqueness(boolean value) {
        hasUniqueness = value;
    }

    public static String getPathway() {
        return pathway;
    }

    public static void setPathway(String value) {
        pathway = value == null ? "" : value;
    }

    public static int getKillCount() {
        return killCount;
    }

    public static void setKillCount(int value) {
        killCount = value;
    }
}

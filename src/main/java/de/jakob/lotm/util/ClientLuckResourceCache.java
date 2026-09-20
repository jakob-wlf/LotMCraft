package de.jakob.lotm.util;

public final class ClientLuckResourceCache {
    private static int luck;
    private static int storedLuck;
    private static int maximumLuck;
    private static float regenerationRate;
    private static float drainRate;
    private static boolean wheelOfFortune;

    private ClientLuckResourceCache() {}

    public static void update(int luck, int storedLuck, int maximumLuck, float regenerationRate,
                              float drainRate, boolean wheelOfFortune) {
        ClientLuckResourceCache.luck = luck;
        ClientLuckResourceCache.storedLuck = storedLuck;
        ClientLuckResourceCache.maximumLuck = maximumLuck;
        ClientLuckResourceCache.regenerationRate = regenerationRate;
        ClientLuckResourceCache.drainRate = drainRate;
        ClientLuckResourceCache.wheelOfFortune = wheelOfFortune;
    }

    public static int getLuck() {
        return luck;
    }

    public static int getStoredLuck() {
        return storedLuck;
    }

    public static int getMaximumLuck() {
        return maximumLuck;
    }

    public static float getRegenerationRate() {
        return regenerationRate;
    }

    public static float getDrainRate() {
        return drainRate;
    }

    public static boolean isWheelOfFortune() {
        return wheelOfFortune;
    }
}
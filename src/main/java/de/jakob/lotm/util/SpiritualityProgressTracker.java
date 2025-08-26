package de.jakob.lotm.util;

import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpiritualityProgressTracker {
    private static final Map<UUID, Float> playerProgress = new HashMap<>();

    public static boolean hasProgress(Player player) {
        return playerProgress.containsKey(player.getUUID());
    }

    public static float getProgress(Player player) {
        return playerProgress.getOrDefault(player.getUUID(), 0.0f);
    }

    public static void setProgress(Player player, float progress) {
        playerProgress.put(player.getUUID(), Math.max(0.0f, Math.min(1.0f, progress)));
    }

    public static void removeProgress(Player player) {
        playerProgress.remove(player.getUUID());
    }

    public static void clearAll() {
        playerProgress.clear();
    }

    public static void incrementProgress(Player player, float amount) {
        float current = getProgress(player);
        setProgress(player, current + amount);
    }
}

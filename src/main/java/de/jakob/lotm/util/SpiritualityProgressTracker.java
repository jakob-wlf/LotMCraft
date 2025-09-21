package de.jakob.lotm.util;

import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpiritualityProgressTracker {
    private static final Map<UUID, Float> playerProgress = new HashMap<>();

    public static boolean hasProgress(UUID uuid) {
        return playerProgress.containsKey(uuid);
    }

    public static float getProgress(UUID uuid) {
        return playerProgress.getOrDefault(uuid, 0.0f);
    }

    public static void setProgress(UUID uuid, float progress) {
        playerProgress.put(uuid, Math.max(0.0f, Math.min(1.0f, progress)));
    }

    public static void removeProgress(Player player) {
        playerProgress.remove(player.getUUID());
    }
}

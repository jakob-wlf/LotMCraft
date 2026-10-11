package de.jakob.lotm.network.packets.handlers;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PendingTeleportRequests {
    public record Pending(TeleportUse use, double maxRange, long expiresAtGameTime) {}

    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private PendingTeleportRequests() {}

    public static void arm(ServerPlayer player, TeleportUse use, double maxRange, int validForTicks) {
        long expires = player.serverLevel().getGameTime() + validForTicks;
        PENDING.put(player.getUUID(), new Pending(use, maxRange, expires));
    }

    @Nullable
    public static Pending consume(ServerPlayer player, TeleportUse use) {
        Pending pending = PENDING.remove(player.getUUID());
        if (pending == null) return null;
        if (pending.use() != use) return null;
        if (player.serverLevel().getGameTime() > pending.expiresAtGameTime()) return null;
        return pending;
    }

    public static void clear(UUID playerId) {
        PENDING.remove(playerId);
    }
}
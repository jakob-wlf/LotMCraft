package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.EnvisionedCharacteristicsData;
import de.jakob.lotm.attachments.SelfEnvisionStatusData;
import de.jakob.lotm.attachments.TargetEnvisionStatusData;
import de.jakob.lotm.network.packets.toServer.RequestSelfStatusActionPacket;
import de.jakob.lotm.network.packets.toServer.RequestTargetStatusActionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Handles server-side lifecycle events for Envisioning Status:
 * – Tracks player deaths (blocks restore if died after save; cancels active restore).
 * – Ticks expired self + target restores every second (20 ticks).
 * – Syncs slot data to the player on login.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class EnvisionStatusEventHandler {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < 20) return; // only run once per second
        tickCounter = 0;

        MinecraftServer server = event.getServer();
        SelfEnvisionStatusData.get(server).tickRestores(server);
        TargetEnvisionStatusData.get(server).tickRestores(server);
        EnvisionedCharacteristicsData.get(server).tickSlots(server);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        // Record death + cancel any active self restore
        SelfEnvisionStatusData.get(server).onPlayerDied(player.getUUID());

        // Sync updated slot state back to the player (blocked-by-death flag may change)
        // They just died so they're about to respawn — schedule a re-sync via scheduler
        // (simple approach: sync on the next tick when they're still the same entity)
        RequestSelfStatusActionPacket.sendSync(player, SelfEnvisionStatusData.get(server));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        // Sync both self and target status data to the client on login
        RequestSelfStatusActionPacket.sendSync(player, SelfEnvisionStatusData.get(server));
        RequestTargetStatusActionPacket.sendSync(player, TargetEnvisionStatusData.get(server));
        EnvisionedCharacteristicsData.sendSync(player, EnvisionedCharacteristicsData.get(server));
    }
}

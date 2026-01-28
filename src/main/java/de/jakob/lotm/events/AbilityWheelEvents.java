package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AbilityWheelEvents {

    /**
     * Syncs ability wheel data when player logs in.
     * This ensures the client always has the correct data on login.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            AbilityWheelHelper.syncToClient(serverPlayer);
        }
    }

    /**
     * Syncs ability wheel data when player changes dimension.
     * This prevents desync issues during dimension travel.
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            AbilityWheelHelper.syncToClient(serverPlayer);
        }
    }

    /**
     * Syncs ability wheel data when player respawns.
     * This ensures data is correct after death.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Small delay to ensure player is fully loaded
            serverPlayer.getServer().execute(() -> {
                AbilityWheelHelper.syncToClient(serverPlayer);
            });
        }
    }
}
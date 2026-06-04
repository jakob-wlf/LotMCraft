package de.jakob.lotm.sefirah;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Handles periodic recalculation of Sefirot Authority effects:
 *   – Cross-path ability grants
 *   – Passive divination / concealment immunity
 *
 * Recalculates every 20 ticks per sefirot owner to catch sequence changes.
 * Also re-initialises on login so the in-memory state survives server restarts.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SefirotAuthorityEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;
        if (entity.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return;
        if (!SefirahHandler.hasSefirot(player)) return;

        SefirotAuthorityManager.updatePlayerAuthority(player);
    }

    /** Restore authority state after login (in-memory maps are cleared on restart). */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Delay one tick to let SefirotData finish loading
        player.getServer().execute(() -> {
            if (SefirahHandler.hasSefirot(player)) {
                SefirotAuthorityManager.updatePlayerAuthority(player);
            }
        });
    }

    /** Clear authority state on logout so stale UUIDs don't linger. */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SefirotAuthorityManager.SEFIROT_DIVINATION_IMMUNE.remove(player.getUUID());
        SefirotAuthorityManager.RIVER_CONCEALMENT_ACTIVE.remove(player.getUUID());
        // Note: grantedAbilities entries stay (they're per-UUID, will be refreshed on next login)
    }
}

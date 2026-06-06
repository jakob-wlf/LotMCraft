package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.GatheringData;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class GatheringEventHandler {

    /**
     * Cancel all incoming damage for players who are currently in an active gathering.
     * This covers both direct hits and ability damage.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;
        // Owner of a sefirot is not protected — they don't participate as a gathered member
        if (player instanceof ServerPlayer sp && !SefirahHandler.getClaimedSefirot(sp).isEmpty()) return;
        // Only beyonders above the grey fog who are actively gathered receive immunity
        if (GatheringData.isGathered(player.getUUID()) && BeyonderData.isBeyonder(player)) {
            event.setNewDamage(0);
        }
    }

    /**
     * When a gathered player logs off, return them (clear state) so they
     * aren't stuck gathered on their next login.
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (GatheringData.isGathered(player.getUUID())) {
            // Can't teleport an offline player, just unmark and clear their stored return location
            GatheringData.unmarkGathered(player.getUUID());
            GatheringData.get(player.server).clearReturnLocation(player.getUUID());
        }
    }

    /**
     * Blocks unauthorised players from entering Sefirah Castle.
     * Authorised entries:
     *   1. The sefirah_castle owner (uses U-key teleport).
     *   2. Players already marked as currently gathered (pulled in by CALL action).
     *
     * Everyone else is immediately returned to overworld spawn with a warning.
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getTo().equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY)) return;

        boolean isOwner    = "sefirah_castle".equals(SefirahHandler.getClaimedSefirot(player));
        boolean isGathered = GatheringData.isGathered(player.getUUID());

        if (isOwner || isGathered) return;

        // Unauthorised — teleport back to overworld spawn
        player.sendSystemMessage(Component.literal(
                "§5Sefirah Castle does not acknowledge your uninvited presence."));
        net.minecraft.core.BlockPos spawn = player.server.overworld().getSharedSpawnPos();
        player.teleportTo(player.server.overworld(),
                spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }
}

package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.skin.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SkinChangerEvents {
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Clean up any skin overrides when player leaves
            String playerId = player.getUUID().toString();
            // The maps will be cleaned up naturally, but we could add explicit cleanup here
        }
    }
    
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Could sync current skin state if needed
            if (SkinManager.hasSkinOverride(player)) {
                String currentOverride = SkinManager.getCurrentSkinOverride(player);
                player.sendSystemMessage(Component.literal("You have an active skin override: " + currentOverride));
            }
        }
    }
}
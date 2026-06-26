package de.jakob.lotm.beyonders.abilities.black_emperor;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

//@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AuthorityChatHooks {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String raw = event.getRawText();

        if (CommandingOrdersAbility.handleAuthorityChat(player, raw)) {
            event.setCanceled(true);
        }
    }
}
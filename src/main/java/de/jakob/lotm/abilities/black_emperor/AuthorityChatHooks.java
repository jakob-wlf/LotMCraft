package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
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
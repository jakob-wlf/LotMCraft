package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ChatEvents {

    // a temp reset way for now
    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        Component message = event.getMessage();
        String messageText = message.getString();

        if (messageText.equalsIgnoreCase("fool")) {
            ControllingUtil.reset(player, player.serverLevel(), true);
        }
    }
}
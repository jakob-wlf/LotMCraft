package de.jakob.lotm.addons.rituals.wheel_of_fortune;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {
    @SubscribeEvent
    private static void onServerTick(ServerTickEvent.Post event) {
        Random random = new Random();
        var server = event.getServer();

        if (server.getTickCount() % 20 == 0 && random.nextInt(75600) == 0) {
            for(var player : server.getPlayerList().getPlayers()){
                if (!BeyonderData.getPathway(player).equals("wheel_of_fortune") ||
                        BeyonderData.getSequence(player) != 1) continue;
                if(!BeyonderData.hasUniqueness(player)) continue;

                player.getData(ModAttachments.RITUALS.get()).setCompleted(true);
            }
        }
    }

}

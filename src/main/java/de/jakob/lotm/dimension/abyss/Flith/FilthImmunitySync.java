package de.jakob.lotm.dimension.abyss.Flith;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.abyss.fog.AbyssFogNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FilthImmunitySync {
    private static final int SYNC_INTERVAL_TICKS = 20;
    private static void send(ServerPlayer player) {PacketDistributor.sendToPlayer(player, new AbyssFogNetwork.FilthImmunityPayload(LiquidFilthEffects.isImmune(player)));}
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % SYNC_INTERVAL_TICKS != 0) return;
        send(player);
    }
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {if (event.getEntity() instanceof ServerPlayer player) {send(player);}}
}

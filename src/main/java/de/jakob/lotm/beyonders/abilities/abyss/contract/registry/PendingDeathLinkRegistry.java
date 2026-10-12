package de.jakob.lotm.beyonders.abilities.abyss.contract.registry;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PendingDeathLinkRegistry {
    private static final Set<UUID> pendingDeaths = ConcurrentHashMap.newKeySet();
    public static void register(UUID uuid) {
        if (uuid == null) return;
        pendingDeaths.add(uuid);
    }
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!pendingDeaths.remove(player.getUUID())) return;
        player.hurt(player.damageSources().magic(), Float.MAX_VALUE);
    }
}
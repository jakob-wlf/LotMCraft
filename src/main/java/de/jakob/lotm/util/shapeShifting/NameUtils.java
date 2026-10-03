package de.jakob.lotm.util.shapeShifting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.toClient.NameSyncPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class NameUtils {
    public static final Map<UUID, String> mapping = new HashMap<>();

    public static void setPlayerName(ServerPlayer player, String name) {
        mapping.put(player.getUUID(), name);
        refreshPlayer(player);
        broadcastNicknameChange(player, name);
    }

    public static void resetPlayerName(ServerPlayer player) {
        mapping.remove(player.getUUID());
        refreshPlayer(player);
        broadcastNicknameChange(player, "");
    }

    private static void refreshPlayer(ServerPlayer player) {
        player.refreshDisplayName();
        player.refreshTabListName();
    }

    // tab list name change:
    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        Player player = event.getEntity();
        String nickname = mapping.get(player.getUUID());

        if (nickname != null && !nickname.isEmpty()) {
            event.setDisplayName(Component.literal(nickname));
        }
    }

    // chat name change:
    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        Player player = event.getEntity();
        String nickname = mapping.get(player.getUUID());

        if (nickname != null && !nickname.isEmpty()) {
            event.setDisplayname(Component.literal(nickname));
        }
    }

    public static void broadcastNicknameChange(ServerPlayer player, String newNickname) {
        for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            otherPlayer.connection.send(new NameSyncPacket(player.getUUID(), newNickname));
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                String nickname = mapping.get(onlinePlayer.getUUID());
                if (nickname != null && !nickname.isEmpty()) {
                    player.connection.send(new NameSyncPacket(onlinePlayer.getUUID(), nickname));
                }
            }
            String newPlayerNickname = mapping.get(player.getUUID());
            if (newPlayerNickname != null && !newPlayerNickname.isEmpty()) {
                for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
                    if (!otherPlayer.getUUID().equals(player.getUUID())) {
                        otherPlayer.connection.send(new NameSyncPacket(player.getUUID(), newPlayerNickname));
                    }
                }
            }
            resetPlayerName(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
                otherPlayer.connection.send(new NameSyncPacket(player.getUUID(), ""));
            }
            resetPlayerName(player);
        }
    }
}
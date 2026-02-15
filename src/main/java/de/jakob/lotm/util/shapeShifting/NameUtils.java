package de.jakob.lotm.util.shapeShifting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.toClient.NameSyncPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class NameUtils {
    public static void setPlayerName(ServerPlayer player, String name) {
        NameStorage.mapping.put(player.getUUID(), name);
        NameStorage.save();
        refreshPlayer(player);
        broadcastNicknameChange(player, name);
    }

    public static void resetPlayerName(ServerPlayer player) {
        NameStorage.mapping.remove(player.getUUID());
        NameStorage.save();
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
        String nickname = NameStorage.mapping.get(player.getUUID());
        if (nickname != null && !nickname.isEmpty()) {
            event.setDisplayName(Component.literal(nickname));
        }
    }

    // chat name change:
    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        Player player = event.getEntity();
        String nickname = NameStorage.mapping.get(player.getUUID());
        if (nickname != null && !nickname.isEmpty()) {
            event.setDisplayname(Component.literal(nickname));
        }
    }

    public static void broadcastNicknameChange(ServerPlayer player, String newNickname) {
        NameSyncPacket packet = new NameSyncPacket(player.getUUID(), newNickname);
        for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            otherPlayer.connection.send(packet);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                String nickname = NameStorage.mapping.get(onlinePlayer.getUUID());
                if (nickname != null && !nickname.isEmpty()) {
                    player.connection.send(new NameSyncPacket(onlinePlayer.getUUID(), nickname));
                }
            }
            String newPlayerNickname = NameStorage.mapping.get(player.getUUID());
            if (newPlayerNickname != null && !newPlayerNickname.isEmpty()) {
                NameSyncPacket packet = new NameSyncPacket(player.getUUID(), newPlayerNickname);
                for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
                    if (!otherPlayer.getUUID().equals(player.getUUID())) {
                        otherPlayer.connection.send(packet);
                    }
                }
            }
            resetPlayerName(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NameSyncPacket packet = new NameSyncPacket(player.getUUID(), "");
            for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
                otherPlayer.connection.send(packet);
            }
            resetPlayerName(player);
        }
    }
}
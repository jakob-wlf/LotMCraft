package de.jakob.lotm.network;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.*;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar(LOTMCraft.MOD_ID)
                .versioned(PROTOCOL_VERSION);

        // Register packets
        registrar.playToServer(
                BecomeBeyonderPacket.TYPE,
                BecomeBeyonderPacket.STREAM_CODEC,
                BecomeBeyonderPacket::handle
        );

        registrar.playToServer(
                ReceiveAbilityItemsPacket.TYPE,
                ReceiveAbilityItemsPacket.STREAM_CODEC,
                ReceiveAbilityItemsPacket::handle
        );

        registrar.playToServer(
                ReceiveAbilityPacket.TYPE,
                ReceiveAbilityPacket.STREAM_CODEC,
                ReceiveAbilityPacket::handle
        );

        registrar.playToServer(
                OpenAbilitySelectionPacket.TYPE,
                OpenAbilitySelectionPacket.STREAM_CODEC,
                OpenAbilitySelectionPacket::handle
        );

        registrar.playToServer(
                ToggleGriefingPacket.TYPE,
                ToggleGriefingPacket.STREAM_CODEC,
                ToggleGriefingPacket::handle
        );

        registrar.playToServer(
                ClearBeyonderDataPacket.TYPE,
                ClearBeyonderDataPacket.STREAM_CODEC,
                ClearBeyonderDataPacket::handle
        );

        registrar.playToServer(
                DebugButtonPacket.TYPE,
                DebugButtonPacket.STREAM_CODEC,
                DebugButtonPacket::handle
        );

        registrar.playToClient(
                SyncBeyonderDataPacket.TYPE,
                SyncBeyonderDataPacket.STREAM_CODEC,
                SyncBeyonderDataPacket::handle
        );

        registrar.playToClient(
                RingEffectPacket.TYPE,
                RingEffectPacket.STREAM_CODEC,
                RingEffectPacket::handle
        );

        registrar.playToClient(
                SyncExplodedTrapPacket.TYPE,
                SyncExplodedTrapPacket.STREAM_CODEC,
                SyncExplodedTrapPacket::handle
        );
        registrar.playToClient(
                SyncGriefingStatePacket.TYPE,
                SyncGriefingStatePacket.STREAM_CODEC,
                SyncGriefingStatePacket::handle
        );
        registrar.playToClient(
                SyncAbilityMenuPacket.TYPE,
                SyncAbilityMenuPacket.STREAM_CODEC,
                SyncAbilityMenuPacket::handle
        );
    }

    public static void sendToServer(CustomPacketPayload packet) {
        Minecraft.getInstance().getConnection().send(packet);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        player.connection.send(packet);
    }

    // Helper method to sync beyonder data to a specific player
    public static void syncBeyonderDataToPlayer(ServerPlayer player) {
        String pathway = BeyonderData.getPathway(player);
        int sequence = BeyonderData.getSequence(player);
        float spirituality = BeyonderData.getSpirituality(player);
        boolean griefingEnabled = BeyonderData.isGriefingEnabled(player);

        SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket(pathway, sequence, spirituality, griefingEnabled);
        sendToPlayer(player, packet);
    }

    // Helper method to sync to all players (useful for when other players need to see beyonder status)
    public static void syncBeyonderDataToAllPlayers(ServerPlayer targetPlayer) {
        String pathway = BeyonderData.getPathway(targetPlayer);
        int sequence = BeyonderData.getSequence(targetPlayer);
        float spirituality = BeyonderData.getSpirituality(targetPlayer);
        boolean griefingEnabled = BeyonderData.isGriefingEnabled(targetPlayer);

        SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket(pathway, sequence, spirituality, griefingEnabled);

        // Send to all players on the server
        targetPlayer.getServer().getPlayerList().getPlayers().forEach(player -> {
            sendToPlayer(player, packet);
        });
    }
}
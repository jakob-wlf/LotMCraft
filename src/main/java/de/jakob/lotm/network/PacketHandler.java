package de.jakob.lotm.network;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.*;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

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
                SyncDreamDivinationCoordinatesPacket.TYPE,
                SyncDreamDivinationCoordinatesPacket.STREAM_CODEC,
                SyncDreamDivinationCoordinatesPacket::handle
        );

        registrar.playToServer(
                SyncTravelersDoorCoordinatesPacket.TYPE,
                SyncTravelersDoorCoordinatesPacket.STREAM_CODEC,
                SyncTravelersDoorCoordinatesPacket::handle
        );

        registrar.playToServer(
                AbilitySelectionPacket.TYPE,
                AbilitySelectionPacket.STREAM_CODEC,
                AbilitySelectionPacket::handle
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
                OpenCoordinateScreenPacket.TYPE,
                OpenCoordinateScreenPacket.STREAM_CODEC,
                OpenCoordinateScreenPacket::handle
        );

        registrar.playToClient(
                OpenCoordinateScreenTravelersDoorPacket.TYPE,
                OpenCoordinateScreenTravelersDoorPacket.STREAM_CODEC,
                OpenCoordinateScreenTravelersDoorPacket::handle
        );

        registrar.playToClient(
                DisplayShadowParticlesPacket.TYPE,
                DisplayShadowParticlesPacket.STREAM_CODEC,
                DisplayShadowParticlesPacket::handle
        );

        registrar.playToClient(
                RemoveDreamDivinationUserPacket.TYPE,
                RemoveDreamDivinationUserPacket.STREAM_CODEC,
                RemoveDreamDivinationUserPacket::handle
        );

        registrar.playToClient(
                SyncLivingEntityBeyonderDataPacket.TYPE,
                SyncLivingEntityBeyonderDataPacket.STREAM_CODEC,
                SyncLivingEntityBeyonderDataPacket::handle
        );

        registrar.playToClient(
                SyncCullAbilityPacket.TYPE,
                SyncCullAbilityPacket.STREAM_CODEC,
                SyncCullAbilityPacket::handle
        );

        registrar.playToClient(
                SyncDangerPremonitionAbilityPacket.TYPE,
                SyncDangerPremonitionAbilityPacket.STREAM_CODEC,
                SyncDangerPremonitionAbilityPacket::handle
        );

        registrar.playToClient(
                SyncNightmareAbilityPacket.TYPE,
                SyncNightmareAbilityPacket.STREAM_CODEC,
                SyncNightmareAbilityPacket::handle
        );

        registrar.playToClient(
                SyncSpectatingAbilityPacket.TYPE,
                SyncSpectatingAbilityPacket.STREAM_CODEC,
                SyncSpectatingAbilityPacket::handle
        );

        registrar.playToClient(
                SyncSpiritVisionAbilityPacket.TYPE,
                SyncSpiritVisionAbilityPacket.STREAM_CODEC,
                SyncSpiritVisionAbilityPacket::handle
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

        //Skin Packets
        registrar.playToServer(
                SkinChangePacket.TYPE,
                SkinChangePacket.STREAM_CODEC,
                SkinChangePacket::handle
        );

        registrar.playToServer(
                SkinRestorePacket.TYPE,
                SkinRestorePacket.STREAM_CODEC,
                SkinRestorePacket::handle
        );

        registrar.playToClient(
                SkinDataPacket.TYPE,
                SkinDataPacket.STREAM_CODEC,
                SkinDataPacket::handle
        );
    }

    public static void sendToServer(CustomPacketPayload packet) {
        Minecraft.getInstance().getConnection().send(packet);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        player.connection.send(packet);
    }

    public static void syncBeyonderDataToPlayer(ServerPlayer player) {
        String pathway = BeyonderData.getPathway(player);
        int sequence = BeyonderData.getSequence(player);
        float spirituality = BeyonderData.getSpirituality(player);
        boolean griefingEnabled = BeyonderData.isGriefingEnabled(player);

        SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket(pathway, sequence, spirituality, griefingEnabled);
        sendToPlayer(player, packet);
    }

    public static void syncBeyonderDataToEntity(LivingEntity entity) {
        if (entity instanceof ServerPlayer) return;

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        SyncLivingEntityBeyonderDataPacket packet =
                new SyncLivingEntityBeyonderDataPacket(entity.getId(), pathway, sequence, BeyonderData.getMaxSpirituality(sequence));

        sendToAllPlayers(packet);
    }

    public static void sendToTracking(Entity entity, CustomPacketPayload payload) {
        if (!(entity.level() instanceof ServerLevel)) return;
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void syncSkinDataToAllPlayers(String playerName, String skinTexture, String skinSignature) {
        SkinDataPacket packet = new SkinDataPacket(playerName, skinTexture, skinSignature);
        sendToAllPlayers(packet);
    }

    public static void sendToAllPlayers(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
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
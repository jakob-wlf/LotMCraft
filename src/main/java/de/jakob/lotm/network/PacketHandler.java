package de.jakob.lotm.network;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.toClient.*;
import de.jakob.lotm.network.packets.toServer.*;
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
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar(LOTMCraft.MOD_ID)
                .versioned(PROTOCOL_VERSION);

        registerServerPackets(registrar);

        registerClientPackets(registrar);

    }

    private static void registerClientPackets(PayloadRegistrar registrar) {
        registrar.playToClient(
                SyncBeyonderDataPacket.TYPE,
                SyncBeyonderDataPacket.STREAM_CODEC,
                SyncBeyonderDataPacket::handle
        );

        registrar.playToClient(
                SyncQuestDataPacket.TYPE,
                SyncQuestDataPacket.STREAM_CODEC,
                SyncQuestDataPacket::handle
        );

        registrar.playToClient(
                SyncAbilityActiveStatusPacket.TYPE,
                SyncAbilityActiveStatusPacket.STREAM_CODEC,
                SyncAbilityActiveStatusPacket::handle
        );

        registrar.playToClient(
                SyncToggleAbilityPacket.TYPE,
                SyncToggleAbilityPacket.STREAM_CODEC,
                SyncToggleAbilityPacket::handle
        );

        registrar.playToClient(
                SyncOnHoldAbilityPacket.TYPE,
                SyncOnHoldAbilityPacket.STREAM_CODEC,
                SyncOnHoldAbilityPacket::handle
        );

        registrar.playToClient(
                SyncAbilityWheelDataPacket.TYPE,
                SyncAbilityWheelDataPacket.STREAM_CODEC,
                SyncAbilityWheelDataPacket::handle
        );

        registrar.playToClient(
                UseAbilityPacket.TYPE,
                UseAbilityPacket.STREAM_CODEC,
                UseAbilityPacket::handle
        );

        registrar.playToClient(
                SyncAbilityWheelPacket.TYPE,
                SyncAbilityWheelPacket.STREAM_CODEC,
                SyncAbilityWheelPacket::handle
        );


        registrar.playToClient(
                HybridMobSyncPacket.TYPE,
                HybridMobSyncPacket.STREAM_CODEC,
                HybridMobSyncPacket::handle
        );

        registrar.playToClient(
                SyncSanityPacket.TYPE,
                SyncSanityPacket.STREAM_CODEC,
                SyncSanityPacket::handle
        );


        registrar.playToClient(
                SendPassiveTheftEffectPacket.TYPE,
                SendPassiveTheftEffectPacket.STREAM_CODEC,
                SendPassiveTheftEffectPacket::handle
        );

        registrar.playToClient(
                SyncDecryptionLookedAtEntitiesAbilityPacket.TYPE,
                SyncDecryptionLookedAtEntitiesAbilityPacket.STREAM_CODEC,
                SyncDecryptionLookedAtEntitiesAbilityPacket::handle
        );


        registrar.playToClient(
                SyncIntrospectMenuPacket.TYPE,
                SyncIntrospectMenuPacket.STREAM_CODEC,
                SyncIntrospectMenuPacket::handle
        );

        registrar.playToClient(
                AddEffectPacket.TYPE,
                AddEffectPacket.STREAM_CODEC,
                AddEffectPacket::handle
        );

        registrar.playToClient(
                AddDirectionalEffectPacket.TYPE,
                AddDirectionalEffectPacket.STREAM_CODEC,
                AddDirectionalEffectPacket::handle
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
                DisplaySpaceConcealmentParticlesPacket.TYPE,
                DisplaySpaceConcealmentParticlesPacket.STREAM_CODEC,
                DisplaySpaceConcealmentParticlesPacket::handle
        );

        registrar.playToClient(
                SyncMirrorWorldPacket.TYPE,
                SyncMirrorWorldPacket.STREAM_CODEC,
                SyncMirrorWorldPacket::handle
        );

        registrar.playToClient(
                SyncTransformationPacket.TYPE,
                SyncTransformationPacket.STREAM_CODEC,
                SyncTransformationPacket::handle
        );

        registrar.playToClient(
                SyncShaderPacket.TYPE,
                SyncShaderPacket.STREAM_CODEC,
                SyncShaderPacket::handle
        );

        registrar.playToClient(
                SyncFogPacket.TYPE,
                SyncFogPacket.STREAM_CODEC,
                SyncFogPacket::handle
        );

        registrar.playToClient(
                SyncLivingEntityBeyonderDataPacket.TYPE,
                SyncLivingEntityBeyonderDataPacket.STREAM_CODEC,
                SyncLivingEntityBeyonderDataPacket::handle
        );

        registrar.playToClient(
                UpdateAbilityBarPacket.TYPE,
                UpdateAbilityBarPacket.STREAM_CODEC,
                UpdateAbilityBarPacket::handle
        );

        registrar.playToClient(
                ChangePlayerPerspectivePacket.TYPE,
                ChangePlayerPerspectivePacket.STREAM_CODEC,
                ChangePlayerPerspectivePacket::handle
        );

        registrar.playToClient(
                SyncPlayerListPacket.TYPE,
                SyncPlayerListPacket.STREAM_CODEC,
                SyncPlayerListPacket::handle
        );

        registrar.playToClient(
                SyncGriefingGamerulePacket.TYPE,
                SyncGriefingGamerulePacket.STREAM_CODEC,
                SyncGriefingGamerulePacket::handle
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
                SyncTelepathyAbilityPacket.TYPE,
                SyncTelepathyAbilityPacket.STREAM_CODEC,
                SyncTelepathyAbilityPacket::handle
        );

        registrar.playToClient(
                SyncSelectedMarionettePacket.TYPE,
                SyncSelectedMarionettePacket.STREAM_CODEC,
                SyncSelectedMarionettePacket::handle
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
                SkinDataPacket.TYPE,
                SkinDataPacket.STREAM_CODEC,
                SkinDataPacket::handle
        );

        registrar.playToClient(
                DarknessEffectPacket.TYPE,
                DarknessEffectPacket.STREAM_CODEC,
                DarknessEffectPacket::handle
        );

        registrar.playToClient(
                SyncAllyDataPacket.TYPE,
                SyncAllyDataPacket.STREAM_CODEC,
                SyncAllyDataPacket::handle
        );

        registrar.playToClient(
                PendingAllyRequestPacket.TYPE,
                PendingAllyRequestPacket.STREAM_CODEC,
                PendingAllyRequestPacket::handle
        );

        registrar.playToClient(
                RemoveMovableEffectPacket.TYPE,
                RemoveMovableEffectPacket.STREAM_CODEC,
                RemoveMovableEffectPacket::handle
        );

        registrar.playToClient(
                UpdateMovableEffectPositionPacket.TYPE,
                UpdateMovableEffectPositionPacket.STREAM_CODEC,
                UpdateMovableEffectPositionPacket::handle
        );

        registrar.playToClient(
                OpenCoordinateScreenForTeleportationPacket.TYPE,
                OpenCoordinateScreenForTeleportationPacket.STREAM_CODEC,
                OpenCoordinateScreenForTeleportationPacket::handle
        );

        registrar.playToClient(
                HotGroundEffectPacket.TYPE,
                HotGroundEffectPacket.STREAM_CODEC,
                HotGroundEffectPacket::handle
        );


        registrar.playToClient(
                AddMovableEffectPacket.TYPE,
                AddMovableEffectPacket.STREAM_CODEC,
                AddMovableEffectPacket::handle
        );

        registrar.playToClient(
                OpenQuestAcceptanceScreenPacket.TYPE,
                OpenQuestAcceptanceScreenPacket.STREAM_CODEC,
                OpenQuestAcceptanceScreenPacket::handle
        );

        registrar.playToClient(
                OpenPlayerDivinationScreenPacket.TYPE,
                OpenPlayerDivinationScreenPacket.STREAM_CODEC,
                OpenPlayerDivinationScreenPacket::handle
        );

        registrar.playToClient(
                OpenStructureDivinationScreenPacket.TYPE,
                OpenStructureDivinationScreenPacket.STREAM_CODEC,
                OpenStructureDivinationScreenPacket::handle
        );

        registrar.playToClient(
                OpenShapeShiftingScreenPacket.TYPE,
                OpenShapeShiftingScreenPacket.STREAM_CODEC,
                OpenShapeShiftingScreenPacket::handle
        );

        registrar.playToClient(
                NameSyncPacket.TYPE,
                NameSyncPacket.CODEC,
                NameSyncPacket::handle
        );

        registrar.playToClient(
                ShapeShiftingSyncPacket.TYPE,
                ShapeShiftingSyncPacket.STREAM_CODEC,
                ShapeShiftingSyncPacket::handle
        );
    }

    private static void registerServerPackets(PayloadRegistrar registrar) {
        registrar.playToServer(
                BecomeBeyonderPacket.TYPE,
                BecomeBeyonderPacket.STREAM_CODEC,
                BecomeBeyonderPacket::handle
        );

        registrar.playToServer(
                DiscardQuestPacket.TYPE,
                DiscardQuestPacket.STREAM_CODEC,
                DiscardQuestPacket::handle
        );

        registrar.playToServer(
                QuestAcceptanceResponsePacket.TYPE,
                QuestAcceptanceResponsePacket.STREAM_CODEC,
                QuestAcceptanceResponsePacket::handle
        );

        registrar.playToServer(
                RequestQuestDataPacket.TYPE,
                RequestQuestDataPacket.STREAM_CODEC,
                RequestQuestDataPacket::handle
        );

        registrar.playToServer(
                UseKeyboundAbilityPacket.TYPE,
                UseKeyboundAbilityPacket.STREAM_CODEC,
                UseKeyboundAbilityPacket::handle
        );

        registrar.playToServer(
                SyncAbilityBarAbilitiesPacket.TYPE,
                SyncAbilityBarAbilitiesPacket.STREAM_CODEC,
                SyncAbilityBarAbilitiesPacket::handle
        );

        registrar.playToServer(
                RequestAbilityBarPacket.TYPE,
                RequestAbilityBarPacket.STREAM_CODEC,
                RequestAbilityBarPacket::handle
        );

        registrar.playToServer(
                RequestActiveStatusOfAbilityPacket.TYPE,
                RequestActiveStatusOfAbilityPacket.STREAM_CODEC,
                RequestActiveStatusOfAbilityPacket::handle
        );

        registrar.playToServer(
                OpenAbilityWheelPacket.TYPE,
                OpenAbilityWheelPacket.STREAM_CODEC,
                OpenAbilityWheelPacket::handle
        );

        registrar.playToServer(
                CloseAbilityWheelPacket.TYPE,
                CloseAbilityWheelPacket.STREAM_CODEC,
                CloseAbilityWheelPacket::handle
        );

        registrar.playToServer(
                RequestAbilityWheelPacket.TYPE,
                RequestAbilityWheelPacket.STREAM_CODEC,
                RequestAbilityWheelPacket::handle
        );

        registrar.playToServer(
                SyncAbilityWheelAbilitiesPacket.TYPE,
                SyncAbilityWheelAbilitiesPacket.STREAM_CODEC,
                SyncAbilityWheelAbilitiesPacket::handle
        );

        registrar.playToServer(
                UpdateSelectedAbilityPacket.TYPE,
                UpdateSelectedAbilityPacket.STREAM_CODEC,
                UpdateSelectedAbilityPacket::handle
        );

        registrar.playToServer(
                UseSelectedAbilityPacket.TYPE,
                UseSelectedAbilityPacket.STREAM_CODEC,
                UseSelectedAbilityPacket::handle
        );

        registrar.playToServer(
                AllyRequestResponsePacket.TYPE,
                AllyRequestResponsePacket.STREAM_CODEC,
                AllyRequestResponsePacket::handle
        );


        registrar.playToServer(
                OpenIntrospectMenuPacket.TYPE,
                OpenIntrospectMenuPacket.STREAM_CODEC,
                OpenIntrospectMenuPacket::handle
        );

        registrar.playToServer(
                PerformMiraclePacket.TYPE,
                PerformMiraclePacket.STREAM_CODEC,
                PerformMiraclePacket::handle
        );

        registrar.playToServer(
                OpenRecipeMenuPacket.TYPE,
                OpenRecipeMenuPacket.STREAM_CODEC,
                OpenRecipeMenuPacket::handle
        );

        registrar.playToServer(
                TeleportToSefirotPacket.TYPE,
                TeleportToSefirotPacket.STREAM_CODEC,
                TeleportToSefirotPacket::handle
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
                TeleportPlayerToLocationPacket.TYPE,
                TeleportPlayerToLocationPacket.STREAM_CODEC,
                TeleportPlayerToLocationPacket::handle
        );

        registrar.playToServer(
                ToggleGriefingPacket.TYPE,
                ToggleGriefingPacket.STREAM_CODEC,
                ToggleGriefingPacket::handle
        );

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

        registrar.playToServer(
                InventoryOpenedPacket.TYPE,
                InventoryOpenedPacket.STREAM_CODEC,
                InventoryOpenedPacket::handle);

        registrar.playToServer(
                OpenMessagePacket.TYPE,
                OpenMessagePacket.STREAM_CODEC,
                OpenMessagePacket::handle);

        registrar.playToServer(
                OpenMessagesMenuPacket.TYPE,
                OpenMessagesMenuPacket.STREAM_CODEC,
                OpenMessagesMenuPacket::handle);

        registrar.playToServer(
                PlayerDivinationSelectedPacket.TYPE,
                PlayerDivinationSelectedPacket.STREAM_CODEC,
                PlayerDivinationSelectedPacket::handle);

        registrar.playToServer(
                StructureDivinationSelectedPacket.TYPE,
                StructureDivinationSelectedPacket.STREAM_CODEC,
                StructureDivinationSelectedPacket::handle);

        registrar.playToServer(
                ShapeShiftingSelectedPacket.TYPE,
                ShapeShiftingSelectedPacket.STREAM_CODEC,
                ShapeShiftingSelectedPacket::handle);
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
        float digestionProgress = BeyonderData.getDigestionProgress(player);

        SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket(pathway, sequence, spirituality, griefingEnabled, digestionProgress);
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

    public static void sendToAllPlayersInSameLevel(CustomPacketPayload payload, ServerLevel level) {
        level.players().forEach(player -> sendToPlayer(player, payload));
    }

    // Helper method to sync to all players (useful for when other players need to see beyonder status)
    public static void syncBeyonderDataToAllPlayers(ServerPlayer targetPlayer) {
        String pathway = BeyonderData.getPathway(targetPlayer);
        int sequence = BeyonderData.getSequence(targetPlayer);
        float spirituality = BeyonderData.getSpirituality(targetPlayer);
        boolean griefingEnabled = BeyonderData.isGriefingEnabled(targetPlayer);
        float digestionProgress = BeyonderData.getDigestionProgress(targetPlayer);

        SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket(pathway, sequence, spirituality, griefingEnabled, digestionProgress);

        targetPlayer.getServer().getPlayerList().getPlayers().forEach(player -> {
            sendToPlayer(player, packet);
        });
    }
}
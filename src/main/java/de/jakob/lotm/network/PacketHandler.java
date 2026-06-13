package de.jakob.lotm.network;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.packets.toClient.*;
import de.jakob.lotm.network.packets.toServer.*;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.Arrays;

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
                SyncWeaknessDetectionTargetsAbilityPacket.TYPE,
                SyncWeaknessDetectionTargetsAbilityPacket.STREAM_CODEC,
                SyncWeaknessDetectionTargetsAbilityPacket::handle
        );

        registrar.playToClient(
                SyncApotheosisPacket.TYPE,
                SyncApotheosisPacket.STREAM_CODEC,
                SyncApotheosisPacket::handle
        );

        registrar.playToClient(
                DisableAbilityUsageForTimePacket.TYPE,
                DisableAbilityUsageForTimePacket.STREAM_CODEC,
                DisableAbilityUsageForTimePacket::handle
        );

        registrar.playToClient(
                FireEffectPacket.TYPE,
                FireEffectPacket.STREAM_CODEC,
                FireEffectPacket::handle
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
                SyncSefirotAuthorityDataPacket.TYPE,
                SyncSefirotAuthorityDataPacket.STREAM_CODEC,
                SyncSefirotAuthorityDataPacket::handle
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
                SyncCopiedAbilitiesPacket.TYPE,
                SyncCopiedAbilitiesPacket.STREAM_CODEC,
                SyncCopiedAbilitiesPacket::handle
        );

        registrar.playToClient(
                OpenCopiedAbilityWheelPacket.TYPE,
                OpenCopiedAbilityWheelPacket.STREAM_CODEC,
                OpenCopiedAbilityWheelPacket::handle
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
                SyncKillCountPacket.TYPE,
                SyncKillCountPacket.STREAM_CODEC,
                SyncKillCountPacket::handle
        );

        registrar.playToClient(
                SyncSacrificeDurationPacket.TYPE,
                SyncSacrificeDurationPacket.STREAM_CODEC,
                SyncSacrificeDurationPacket::handle
        );

        registrar.playToClient(
                SyncSefirotAccommodationPacket.TYPE,
                SyncSefirotAccommodationPacket.STREAM_CODEC,
                SyncSefirotAccommodationPacket::handle
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
                OpenInternalUnderworldAbilityScreenPacket.TYPE,
                OpenInternalUnderworldAbilityScreenPacket.STREAM_CODEC,
                OpenInternalUnderworldAbilityScreenPacket::handle
        );

        registrar.playToClient(
                OpenCoordinateScreenTravelersDoorPacket.TYPE,
                OpenCoordinateScreenTravelersDoorPacket.STREAM_CODEC,
                OpenCoordinateScreenTravelersDoorPacket::handle
        );

        registrar.playToClient(
                OpenEnvisionLocationScreenPacket.TYPE,
                OpenEnvisionLocationScreenPacket.STREAM_CODEC,
                OpenEnvisionLocationScreenPacket::handle
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
                SyncEyeOfDeathAbilityPacket.TYPE,
                SyncEyeOfDeathAbilityPacket.STREAM_CODEC,
                SyncEyeOfDeathAbilityPacket::handle
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
                DarknessEffectPacket.TYPE,
                DarknessEffectPacket.STREAM_CODEC,
                DarknessEffectPacket::handle
        );

        registrar.playToClient(
                SyncCorrosionFovPacket.TYPE,
                SyncCorrosionFovPacket.STREAM_CODEC,
                SyncCorrosionFovPacket::handle
        );

        registrar.playToClient(
                SyncAbilitySelectionPacket.TYPE,
                SyncAbilitySelectionPacket.STREAM_CODEC,
                SyncAbilitySelectionPacket::handle
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
                PendingTeamInvitePacket.TYPE,
                PendingTeamInvitePacket.STREAM_CODEC,
                PendingTeamInvitePacket::handle
        );

        registrar.playToClient(
                SyncSharedAbilitiesDataPacket.TYPE,
                SyncSharedAbilitiesDataPacket.STREAM_CODEC,
                SyncSharedAbilitiesDataPacket::handle
        );

        registrar.playToClient(
                RemoveMovableEffectPacket.TYPE,
                RemoveMovableEffectPacket.STREAM_CODEC,
                RemoveMovableEffectPacket::handle
        );

        registrar.playToClient(
                CancelEffectByPositionPacket.TYPE,
                CancelEffectByPositionPacket.STREAM_CODEC,
                CancelEffectByPositionPacket::handle
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
                OpenBiomeDivinationScreenPacket.TYPE,
                OpenBiomeDivinationScreenPacket.STREAM_CODEC,
                OpenBiomeDivinationScreenPacket::handle
        );

        registrar.playToClient(
                OpenShapeShiftingScreenPacket.TYPE,
                OpenShapeShiftingScreenPacket.STREAM_CODEC,
                OpenShapeShiftingScreenPacket::handle
        );

        registrar.playToClient(
                OpenHistoricalVoidBorrowingScreenPacket.TYPE,
                OpenHistoricalVoidBorrowingScreenPacket.STREAM_CODEC,
                OpenHistoricalVoidBorrowingScreenPacket::handle
        );

        registrar.playToClient(
                SyncPlayerTeleportationPlayerNamesPacket.TYPE,
                SyncPlayerTeleportationPlayerNamesPacket.STREAM_CODEC,
                SyncPlayerTeleportationPlayerNamesPacket::handle
        );

        registrar.playToClient(
                SyncPsychologicalInvisibilityPacket.TYPE,
                SyncPsychologicalInvisibilityPacket.STREAM_CODEC,
                SyncPsychologicalInvisibilityPacket::handle
        );

        registrar.playToClient(
                AddClientSideTagPacket.TYPE,
                AddClientSideTagPacket.STREAM_CODEC,
                AddClientSideTagPacket::handle
        );

        registrar.playToClient(
                SyncPlayerTeleportationOnlinePlayersPacket.TYPE,
                SyncPlayerTeleportationOnlinePlayersPacket.STREAM_CODEC,
                SyncPlayerTeleportationOnlinePlayersPacket::handle
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

        registrar.playToClient(
                PlayAnimationPacket.TYPE,
                PlayAnimationPacket.STREAM_CODEC,
                PlayAnimationPacket::handle
        );

        registrar.playToClient(
                ResetClientEffectsPacket.TYPE,
                ResetClientEffectsPacket.STREAM_CODEC,
                ResetClientEffectsPacket::handle
        );

        registrar.playToClient(
                SyncOriginalBodyOwnerPacket.TYPE,
                SyncOriginalBodyOwnerPacket.STREAM_CODEC,
                SyncOriginalBodyOwnerPacket::handle
        );

        registrar.playToClient(
                SyncSkillScalingPacket.TYPE,
                SyncSkillScalingPacket.STREAM_CODEC,
                SyncSkillScalingPacket::handle
        );
      
        registrar.playToClient(
                SyncSpiritChannelingPacket.TYPE,
                SyncSpiritChannelingPacket.STREAM_CODEC,
                SyncSpiritChannelingPacket::handle
        );
      
        registrar.playToClient(
                SyncUniquenessPacket.TYPE,
                SyncUniquenessPacket.STREAM_CODEC,
                SyncUniquenessPacket::handle
        );

        registrar.playToClient(
                SyncControllingDataPacket.TYPE,
                SyncControllingDataPacket.STREAM_CODEC,
                SyncControllingDataPacket::handle
        );

        registrar.playToClient(
                OpenDiscernmentScreenPacket.TYPE,
                OpenDiscernmentScreenPacket.STREAM_CODEC,
                OpenDiscernmentScreenPacket::handle
        );

        registrar.playToClient(
                SyncDiscernmentDataPacket.TYPE,
                SyncDiscernmentDataPacket.STREAM_CODEC,
                SyncDiscernmentDataPacket::handle
        );

        registrar.playToClient(
                OpenCharSlotRollPacket.TYPE,
                OpenCharSlotRollPacket.STREAM_CODEC,
                OpenCharSlotRollPacket::handle
        );

        registrar.playToClient(
                OpenAbilitySealScreenPacket.TYPE,
                OpenAbilitySealScreenPacket.STREAM_CODEC,
                OpenAbilitySealScreenPacket::handle
        );

        registrar.playToClient(
                OpenAboveSeqAuthorityScreenPacket.TYPE,
                OpenAboveSeqAuthorityScreenPacket.STREAM_CODEC,
                OpenAboveSeqAuthorityScreenPacket::handle
        );

        registrar.playToClient(
                OpenDailySpinScreenPacket.TYPE,
                OpenDailySpinScreenPacket.STREAM_CODEC,
                OpenDailySpinScreenPacket::handle
        );

        registrar.playToClient(
                OpenSellYourSoulScreenPacket.TYPE,
                OpenSellYourSoulScreenPacket.STREAM_CODEC,
                OpenSellYourSoulScreenPacket::handle
        );

        registrar.playToClient(
                OpenSellYourSoulGatePacket.TYPE,
                OpenSellYourSoulGatePacket.STREAM_CODEC,
                OpenSellYourSoulGatePacket::handle
        );

        registrar.playToClient(
                OpenCharExchangeWheelPacket.TYPE,
                OpenCharExchangeWheelPacket.STREAM_CODEC,
                OpenCharExchangeWheelPacket::handle
        );

        registrar.playToClient(
                de.jakob.lotm.network.packets.toClient.OpenRiverVaultScreenPacket.TYPE,
                de.jakob.lotm.network.packets.toClient.OpenRiverVaultScreenPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toClient.OpenRiverVaultScreenPacket::handle
        );

        registrar.playToClient(
                de.jakob.lotm.network.packets.toClient.SyncSummonedBlasphemyPacket.TYPE,
                de.jakob.lotm.network.packets.toClient.SyncSummonedBlasphemyPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toClient.SyncSummonedBlasphemyPacket::handle
        );

        registrar.playToClient(
                de.jakob.lotm.network.packets.toClient.SyncEnvisionTriggerPacket.TYPE,
                de.jakob.lotm.network.packets.toClient.SyncEnvisionTriggerPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toClient.SyncEnvisionTriggerPacket::handle
        );

        registrar.playToClient(
                de.jakob.lotm.network.packets.toClient.SyncEnvisionStatusPacket.TYPE,
                de.jakob.lotm.network.packets.toClient.SyncEnvisionStatusPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toClient.SyncEnvisionStatusPacket::handle
        );

        registrar.playToClient(
                de.jakob.lotm.network.packets.toClient.SyncTargetEnvisionStatusPacket.TYPE,
                de.jakob.lotm.network.packets.toClient.SyncTargetEnvisionStatusPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toClient.SyncTargetEnvisionStatusPacket::handle
        );

        registrar.playToClient(
                de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.TYPE,
                de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toClient.SyncEnvisionCharacteristicsPacket::handle
        );

        registrar.playToClient(
                de.jakob.lotm.network.packets.toClient.SyncGreyFogStatusPacket.TYPE,
                de.jakob.lotm.network.packets.toClient.SyncGreyFogStatusPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toClient.SyncGreyFogStatusPacket::handle
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
                NextArtifactAbilityPacket.TYPE,
                NextArtifactAbilityPacket.STREAM_CODEC,
                NextArtifactAbilityPacket::handle
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
                SyncSharedAbilitiesPacket.TYPE,
                SyncSharedAbilitiesPacket.STREAM_CODEC,
                SyncSharedAbilitiesPacket::handle
        );

        registrar.playToServer(
                RequestSharedAbilitiesPacket.TYPE,
                RequestSharedAbilitiesPacket.STREAM_CODEC,
                RequestSharedAbilitiesPacket::handle
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
                UseQueuedSoulAbilityPacket.TYPE,
                UseQueuedSoulAbilityPacket.STREAM_CODEC,
                UseQueuedSoulAbilityPacket::handle
        );

        registrar.playToServer(
                UseSharedAbilityPacket.TYPE,
                UseSharedAbilityPacket.STREAM_CODEC,
                UseSharedAbilityPacket::handle
        );

        registrar.playToServer(
                UseCopiedAbilityPacket.TYPE,
                UseCopiedAbilityPacket.STREAM_CODEC,
                UseCopiedAbilityPacket::handle
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
                InventoryOpenedPacket.TYPE,
                InventoryOpenedPacket.STREAM_CODEC,
                InventoryOpenedPacket::handle);


        registrar.playToServer(
                OpenHonorificNamesMenuPacket.TYPE,
                OpenHonorificNamesMenuPacket.STREAM_CODEC,
                OpenHonorificNamesMenuPacket::handle);

        registrar.playToServer(
                HonorificNamesRespondPacket.TYPE,
                HonorificNamesRespondPacket.STREAM_CODEC,
                HonorificNamesRespondPacket::handle);

        registrar.playToServer(
                SetHonorificNamePacket.TYPE,
                SetHonorificNamePacket.STREAM_CODEC,
                SetHonorificNamePacket::handle);

        registrar.playToServer(
                PlayerDivinationSelectedPacket.TYPE,
                PlayerDivinationSelectedPacket.STREAM_CODEC,
                PlayerDivinationSelectedPacket::handle);

        registrar.playToServer(
                StructureDivinationSelectedPacket.TYPE,
                StructureDivinationSelectedPacket.STREAM_CODEC,
                StructureDivinationSelectedPacket::handle);

        registrar.playToServer(
                BiomeDivinationSelectedPacket.TYPE,
                BiomeDivinationSelectedPacket.STREAM_CODEC,
                BiomeDivinationSelectedPacket::handle);

        registrar.playToServer(
                HistoricalVoidBorrowingSelectedPacket.TYPE,
                HistoricalVoidBorrowingSelectedPacket.STREAM_CODEC,
                HistoricalVoidBorrowingSelectedPacket::handle);

        registrar.playToServer(
                ShapeShiftingSelectedPacket.TYPE,
                ShapeShiftingSelectedPacket.STREAM_CODEC,
                ShapeShiftingSelectedPacket::handle);

        registrar.playToServer(
                ReturnToMainBodyPacket.TYPE,
                ReturnToMainBodyPacket.STREAM_CODEC,
                ReturnToMainBodyPacket::handle);

        registrar.playToServer(
                StopDiscernmentPacket.TYPE,
                StopDiscernmentPacket.STREAM_CODEC,
                StopDiscernmentPacket::handle);

        registrar.playToServer(
                OpenArtifactWheelPacket.TYPE,
                OpenArtifactWheelPacket.STREAM_CODEC,
                OpenArtifactWheelPacket::handle);

        registrar.playToServer(
                SyncArtifactAbilityWheel.TYPE,
                SyncArtifactAbilityWheel.STREAM_CODEC,
                SyncArtifactAbilityWheel::handle
        );

        registrar.playToServer(
                RequestUniquenessApotheosisPacket.TYPE,
                RequestUniquenessApotheosisPacket.STREAM_CODEC,
                RequestUniquenessApotheosisPacket::handle
        );

        registrar.playToServer(
                ToggleSefirotAuthorityAbilityPacket.TYPE,
                ToggleSefirotAuthorityAbilityPacket.STREAM_CODEC,
                ToggleSefirotAuthorityAbilityPacket::handle
        );

        registrar.playToServer(
                RiverAuthorityActionPacket.TYPE,
                RiverAuthorityActionPacket.STREAM_CODEC,
                RiverAuthorityActionPacket::handle
        );

        registrar.playToServer(
                RequestRiverImprintScreenPacket.TYPE,
                RequestRiverImprintScreenPacket.STREAM_CODEC,
                RequestRiverImprintScreenPacket::handle
        );

        registrar.playToServer(
                RequestTranscendencePacket.TYPE,
                RequestTranscendencePacket.STREAM_CODEC,
                RequestTranscendencePacket::handle
        );

        registrar.playToServer(
                RequestGatheringScreenPacket.TYPE,
                RequestGatheringScreenPacket.STREAM_CODEC,
                RequestGatheringScreenPacket::handle
        );

        registrar.playToServer(
                GatheringActionPacket.TYPE,
                GatheringActionPacket.STREAM_CODEC,
                GatheringActionPacket::handle
        );

        registrar.playToServer(
                GatheringMessagePacket.TYPE,
                GatheringMessagePacket.STREAM_CODEC,
                GatheringMessagePacket::handle
        );

        registrar.playToServer(
                RequestRiverBlessingScreenPacket.TYPE,
                RequestRiverBlessingScreenPacket.STREAM_CODEC,
                RequestRiverBlessingScreenPacket::handle
        );

        registrar.playToServer(
                RiverBlessingActionPacket.TYPE,
                RiverBlessingActionPacket.STREAM_CODEC,
                RiverBlessingActionPacket::handle
        );

        registrar.playToServer(
                RiverAudienceActionPacket.TYPE,
                RiverAudienceActionPacket.STREAM_CODEC,
                RiverAudienceActionPacket::handle
        );

        registrar.playToServer(
                CharSlotRollResultPacket.TYPE,
                CharSlotRollResultPacket.STREAM_CODEC,
                CharSlotRollResultPacket::handle
        );

        registrar.playToServer(
                RequestAbilitySealScreenPacket.TYPE,
                RequestAbilitySealScreenPacket.STREAM_CODEC,
                RequestAbilitySealScreenPacket::handle
        );

        registrar.playToServer(
                RequestRiverVaultScreenPacket.TYPE,
                RequestRiverVaultScreenPacket.STREAM_CODEC,
                RequestRiverVaultScreenPacket::handle
        );

        registrar.playToServer(
                de.jakob.lotm.network.packets.toServer.RiverVaultActionPacket.TYPE,
                de.jakob.lotm.network.packets.toServer.RiverVaultActionPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toServer.RiverVaultActionPacket::handle
        );

        registrar.playToServer(
                SetAbilitySealPacket.TYPE,
                SetAbilitySealPacket.STREAM_CODEC,
                SetAbilitySealPacket::handle
        );

        registrar.playToServer(
                RequestDailySpinPacket.TYPE,
                RequestDailySpinPacket.STREAM_CODEC,
                RequestDailySpinPacket::handle
        );

        registrar.playToServer(
                RequestSellYourSoulPacket.TYPE,
                RequestSellYourSoulPacket.STREAM_CODEC,
                RequestSellYourSoulPacket::handle
        );

        registrar.playToServer(
                RequestSellYourSoulInfoPacket.TYPE,
                RequestSellYourSoulInfoPacket.STREAM_CODEC,
                RequestSellYourSoulInfoPacket::handle
        );

        registrar.playToServer(
                RequestCharExchangePacket.TYPE,
                RequestCharExchangePacket.STREAM_CODEC,
                RequestCharExchangePacket::handle
        );

        registrar.playToServer(
                RequestCharPathExchangePacket.TYPE,
                RequestCharPathExchangePacket.STREAM_CODEC,
                RequestCharPathExchangePacket::handle
        );

        registrar.playToServer(
                RequestRecipeExchangePacket.TYPE,
                RequestRecipeExchangePacket.STREAM_CODEC,
                RequestRecipeExchangePacket::handle
        );

        registrar.playToServer(
                RequestRecipePathExchangePacket.TYPE,
                RequestRecipePathExchangePacket.STREAM_CODEC,
                RequestRecipePathExchangePacket::handle
        );

        registrar.playToServer(
                de.jakob.lotm.network.packets.toServer.EnvisionSelfTeleportPacket.TYPE,
                de.jakob.lotm.network.packets.toServer.EnvisionSelfTeleportPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toServer.EnvisionSelfTeleportPacket::handle
        );

        registrar.playToServer(
                de.jakob.lotm.network.packets.toServer.EnvisionTargetTeleportPacket.TYPE,
                de.jakob.lotm.network.packets.toServer.EnvisionTargetTeleportPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toServer.EnvisionTargetTeleportPacket::handle
        );

        registrar.playToServer(
                de.jakob.lotm.network.packets.toServer.RequestSummonBlasphemyPacket.TYPE,
                de.jakob.lotm.network.packets.toServer.RequestSummonBlasphemyPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toServer.RequestSummonBlasphemyPacket::handle
        );

        registrar.playToServer(
                de.jakob.lotm.network.packets.toServer.RequestEnvisionBlasphemyPacket.TYPE,
                de.jakob.lotm.network.packets.toServer.RequestEnvisionBlasphemyPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toServer.RequestEnvisionBlasphemyPacket::handle
        );

        registrar.playToServer(
                de.jakob.lotm.network.packets.toServer.RequestSelfStatusActionPacket.TYPE,
                de.jakob.lotm.network.packets.toServer.RequestSelfStatusActionPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toServer.RequestSelfStatusActionPacket::handle
        );

        registrar.playToServer(
                de.jakob.lotm.network.packets.toServer.RequestTargetStatusActionPacket.TYPE,
                de.jakob.lotm.network.packets.toServer.RequestTargetStatusActionPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toServer.RequestTargetStatusActionPacket::handle
        );

        registrar.playToServer(
                de.jakob.lotm.network.packets.toServer.GreyFogActionPacket.TYPE,
                de.jakob.lotm.network.packets.toServer.GreyFogActionPacket.STREAM_CODEC,
                de.jakob.lotm.network.packets.toServer.GreyFogActionPacket::handle
        );
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
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
        ArrayList<Characteristic> charList = BeyonderData.getCharList(player);

        String[] history = BeyonderData.getPathwayHistory(player);

        SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket(pathway, sequence, spirituality, griefingEnabled, digestionProgress, history, charList);
        sendToPlayer(player, packet);
    }

    public static void syncBeyonderDataToEntity(LivingEntity entity) {
        if (entity instanceof ServerPlayer) return;

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        SyncLivingEntityBeyonderDataPacket packet =
                new SyncLivingEntityBeyonderDataPacket(entity.getId(), pathway, sequence, BeyonderData.getMaxSpirituality(pathway, sequence));

        sendToAllPlayers(packet);
    }

    public static void sendToTracking(Entity entity, CustomPacketPayload payload) {
        if (!(entity.level() instanceof ServerLevel)) return;
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        if (!(entity.level() instanceof ServerLevel)) return;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    public static void sendToAllPlayers(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static void sendToAllPlayersInSameLevel(CustomPacketPayload payload, ServerLevel level) {
        level.players().forEach(player -> sendToPlayer(player, payload));
    }

    public static void syncUniquenessToPlayer(ServerPlayer player) {
        de.jakob.lotm.attachments.UniquenessComponent comp = player.getData(de.jakob.lotm.attachments.ModAttachments.UNIQUENESS_COMPONENT);
        SyncUniquenessPacket packet = new SyncUniquenessPacket(
                comp.hasUniqueness(),
                comp.getUniquenessPathway(),
                comp.getKillCount()
        );
        sendToPlayer(player, packet);
    }

    // Helper method to sync to all players (useful for when other players need to see beyonder status)
    public static void syncBeyonderDataToAllPlayers(ServerPlayer targetPlayer) {
        String pathway = BeyonderData.getPathway(targetPlayer);
        int sequence = BeyonderData.getSequence(targetPlayer);
        float spirituality = BeyonderData.getSpirituality(targetPlayer);
        boolean griefingEnabled = BeyonderData.isGriefingEnabled(targetPlayer);
        float digestionProgress = BeyonderData.getDigestionProgress(targetPlayer);
        ArrayList<Characteristic> charList = BeyonderData.getCharList(targetPlayer);

        SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket(pathway, sequence, spirituality, griefingEnabled, digestionProgress, new String[10], charList);

        targetPlayer.getServer().getPlayerList().getPlayers().forEach(player -> {
            sendToPlayer(player, packet);
        });
    }
}
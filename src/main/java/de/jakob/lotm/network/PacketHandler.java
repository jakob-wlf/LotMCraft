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
                SyncBeyonderDataS2CPacket.TYPE,
                SyncBeyonderDataS2CPacket.STREAM_CODEC,
                SyncBeyonderDataS2CPacket::handle
        );

        registrar.playToClient(
                SyncWeaknessDetectionTargetsAbilityS2CPacket.TYPE,
                SyncWeaknessDetectionTargetsAbilityS2CPacket.STREAM_CODEC,
                SyncWeaknessDetectionTargetsAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncApotheosisS2CPacket.TYPE,
                SyncApotheosisS2CPacket.STREAM_CODEC,
                SyncApotheosisS2CPacket::handle
        );

        registrar.playToClient(
                DisableAbilityUsageForTimeS2CPacket.TYPE,
                DisableAbilityUsageForTimeS2CPacket.STREAM_CODEC,
                DisableAbilityUsageForTimeS2CPacket::handle
        );

        registrar.playToClient(
                FireEffectS2CPacket.TYPE,
                FireEffectS2CPacket.STREAM_CODEC,
                FireEffectS2CPacket::handle
        );

        registrar.playToClient(
                SyncQuestDataS2CPacket.TYPE,
                SyncQuestDataS2CPacket.STREAM_CODEC,
                SyncQuestDataS2CPacket::handle
        );

        registrar.playToClient(
                SyncAbilityActiveStatusS2CPacket.TYPE,
                SyncAbilityActiveStatusS2CPacket.STREAM_CODEC,
                SyncAbilityActiveStatusS2CPacket::handle
        );

        registrar.playToClient(
                SyncToggleAbilityS2CPacket.TYPE,
                SyncToggleAbilityS2CPacket.STREAM_CODEC,
                SyncToggleAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncActingCapS2CPacket.TYPE,
                SyncActingCapS2CPacket.STREAM_CODEC,
                SyncActingCapS2CPacket::handle
        );

        registrar.playToClient(
                SyncOnHoldAbilityS2CPacket.TYPE,
                SyncOnHoldAbilityS2CPacket.STREAM_CODEC,
                SyncOnHoldAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncAbilityWheelDataS2CPacket.TYPE,
                SyncAbilityWheelDataS2CPacket.STREAM_CODEC,
                SyncAbilityWheelDataS2CPacket::handle
        );

        registrar.playToClient(
                UseAbilityS2CPacket.TYPE,
                UseAbilityS2CPacket.STREAM_CODEC,
                UseAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncAbilityWheelS2CPacket.TYPE,
                SyncAbilityWheelS2CPacket.STREAM_CODEC,
                SyncAbilityWheelS2CPacket::handle
        );

        registrar.playToClient(
                SyncCopiedAbilitiesS2CPacket.TYPE,
                SyncCopiedAbilitiesS2CPacket.STREAM_CODEC,
                SyncCopiedAbilitiesS2CPacket::handle
        );

        registrar.playToClient(
                OpenCopiedAbilityWheelS2CPacket.TYPE,
                OpenCopiedAbilityWheelS2CPacket.STREAM_CODEC,
                OpenCopiedAbilityWheelS2CPacket::handle
        );


        registrar.playToClient(
                HybridMobSyncS2CPacket.TYPE,
                HybridMobSyncS2CPacket.STREAM_CODEC,
                HybridMobSyncS2CPacket::handle
        );

        registrar.playToClient(
                SyncSanityS2CPacket.TYPE,
                SyncSanityS2CPacket.STREAM_CODEC,
                SyncSanityS2CPacket::handle
        );


        registrar.playToClient(
                SendPassiveTheftEffectS2CPacket.TYPE,
                SendPassiveTheftEffectS2CPacket.STREAM_CODEC,
                SendPassiveTheftEffectS2CPacket::handle
        );

        registrar.playToClient(
                SyncDecryptionLookedAtEntitiesAbilityS2CPacket.TYPE,
                SyncDecryptionLookedAtEntitiesAbilityS2CPacket.STREAM_CODEC,
                SyncDecryptionLookedAtEntitiesAbilityS2CPacket::handle
        );

        registrar.playToClient(
                StartStopDiscernmentS2CPacket.TYPE,
                StartStopDiscernmentS2CPacket.STREAM_CODEC,
                StartStopDiscernmentS2CPacket::handle
        );

        registrar.playToClient(
                SyncIntrospectMenuS2CPacket.TYPE,
                SyncIntrospectMenuS2CPacket.STREAM_CODEC,
                SyncIntrospectMenuS2CPacket::handle
        );

        registrar.playToClient(
                SyncKillCountS2CPacket.TYPE,
                SyncKillCountS2CPacket.STREAM_CODEC,
                SyncKillCountS2CPacket::handle
        );

        registrar.playToClient(
                SyncSacrificeDurationS2CPacket.TYPE,
                SyncSacrificeDurationS2CPacket.STREAM_CODEC,
                SyncSacrificeDurationS2CPacket::handle
        );

        registrar.playToClient(
                AddEffectS2CPacket.TYPE,
                AddEffectS2CPacket.STREAM_CODEC,
                AddEffectS2CPacket::handle
        );

        registrar.playToClient(
                AddDirectionalEffectS2CPacket.TYPE,
                AddDirectionalEffectS2CPacket.STREAM_CODEC,
                AddDirectionalEffectS2CPacket::handle
        );

        registrar.playToClient(
                OpenCoordinateScreenS2CPacket.TYPE,
                OpenCoordinateScreenS2CPacket.CODEC,
                OpenCoordinateScreenS2CPacket::handle
        );

        registrar.playToClient(
                OpenCoordinateScreenTravelersDoorS2CPacket.TYPE,
                OpenCoordinateScreenTravelersDoorS2CPacket.STREAM_CODEC,
                OpenCoordinateScreenTravelersDoorS2CPacket::handle
        );

        registrar.playToClient(
                OpenEnvisionLocationScreenS2CPacket.TYPE,
                OpenEnvisionLocationScreenS2CPacket.STREAM_CODEC,
                OpenEnvisionLocationScreenS2CPacket::handle
        );

        registrar.playToClient(
                DisplayShadowParticlesS2CPacket.TYPE,
                DisplayShadowParticlesS2CPacket.STREAM_CODEC,
                DisplayShadowParticlesS2CPacket::handle
        );

        registrar.playToClient(
                DisplaySpaceConcealmentParticlesS2CPacket.TYPE,
                DisplaySpaceConcealmentParticlesS2CPacket.STREAM_CODEC,
                DisplaySpaceConcealmentParticlesS2CPacket::handle
        );

        registrar.playToClient(
                SyncMirrorWorldS2CPacket.TYPE,
                SyncMirrorWorldS2CPacket.STREAM_CODEC,
                SyncMirrorWorldS2CPacket::handle
        );

        registrar.playToClient(
                SyncTransformationS2CPacket.TYPE,
                SyncTransformationS2CPacket.STREAM_CODEC,
                SyncTransformationS2CPacket::handle
        );

        registrar.playToClient(
                SyncShaderS2CPacket.TYPE,
                SyncShaderS2CPacket.STREAM_CODEC,
                SyncShaderS2CPacket::handle
        );

        registrar.playToClient(
                SyncFogS2CPacket.TYPE,
                SyncFogS2CPacket.STREAM_CODEC,
                SyncFogS2CPacket::handle
        );

        registrar.playToClient(
                SyncLivingEntityBeyonderDataS2CPacket.TYPE,
                SyncLivingEntityBeyonderDataS2CPacket.STREAM_CODEC,
                SyncLivingEntityBeyonderDataS2CPacket::handle
        );

        registrar.playToClient(
                UpdateAbilityBarS2CPacket.TYPE,
                UpdateAbilityBarS2CPacket.STREAM_CODEC,
                UpdateAbilityBarS2CPacket::handle
        );

        registrar.playToClient(
                ChangePlayerPerspectiveS2CPacket.TYPE,
                ChangePlayerPerspectiveS2CPacket.STREAM_CODEC,
                ChangePlayerPerspectiveS2CPacket::handle
        );

        registrar.playToClient(
                SyncGriefingGameruleS2CPacket.TYPE,
                SyncGriefingGameruleS2CPacket.STREAM_CODEC,
                SyncGriefingGameruleS2CPacket::handle
        );

        registrar.playToClient(
                SyncCullAbilityS2CPacket.TYPE,
                SyncCullAbilityS2CPacket.STREAM_CODEC,
                SyncCullAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncDangerPremonitionAbilityS2CPacket.TYPE,
                SyncDangerPremonitionAbilityS2CPacket.STREAM_CODEC,
                SyncDangerPremonitionAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncNightmareAbilityS2CPacket.TYPE,
                SyncNightmareAbilityS2CPacket.STREAM_CODEC,
                SyncNightmareAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncSpectatingAbilityS2CPacket.TYPE,
                SyncSpectatingAbilityS2CPacket.STREAM_CODEC,
                SyncSpectatingAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncTelepathyAbilityS2CPacket.TYPE,
                SyncTelepathyAbilityS2CPacket.STREAM_CODEC,
                SyncTelepathyAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncSelectedMarionetteS2CPacket.TYPE,
                SyncSelectedMarionetteS2CPacket.STREAM_CODEC,
                SyncSelectedMarionetteS2CPacket::handle
        );

        registrar.playToClient(
                SyncSpiritVisionAbilityS2CPacket.TYPE,
                SyncSpiritVisionAbilityS2CPacket.STREAM_CODEC,
                SyncSpiritVisionAbilityS2CPacket::handle
        );

        registrar.playToClient(
                SyncEyeOfDeathAbilityS2CPacket.TYPE,
                SyncEyeOfDeathAbilityS2CPacket.STREAM_CODEC,
                SyncEyeOfDeathAbilityS2CPacket::handle
        );

        registrar.playToClient(
                RingEffectS2CPacket.TYPE,
                RingEffectS2CPacket.STREAM_CODEC,
                RingEffectS2CPacket::handle
        );

        registrar.playToClient(
                SyncExplodedTrapS2CPacket.TYPE,
                SyncExplodedTrapS2CPacket.STREAM_CODEC,
                SyncExplodedTrapS2CPacket::handle
        );
        registrar.playToClient(
                SyncGriefingStateS2CPacket.TYPE,
                SyncGriefingStateS2CPacket.STREAM_CODEC,
                SyncGriefingStateS2CPacket::handle
        );

        registrar.playToClient(
                DarknessEffectS2CPacket.TYPE,
                DarknessEffectS2CPacket.STREAM_CODEC,
                DarknessEffectS2CPacket::handle
        );

        registrar.playToClient(
                SyncCorrosionFovS2CPacket.TYPE,
                SyncCorrosionFovS2CPacket.STREAM_CODEC,
                SyncCorrosionFovS2CPacket::handle
        );

        registrar.playToClient(
                SyncAbilitySelectionS2CPacket.TYPE,
                SyncAbilitySelectionS2CPacket.STREAM_CODEC,
                SyncAbilitySelectionS2CPacket::handle
        );

        registrar.playToClient(
                SyncAllyDataS2CPacket.TYPE,
                SyncAllyDataS2CPacket.STREAM_CODEC,
                SyncAllyDataS2CPacket::handle
        );

        registrar.playToClient(
                PendingAllyRequestS2CPacket.TYPE,
                PendingAllyRequestS2CPacket.STREAM_CODEC,
                PendingAllyRequestS2CPacket::handle
        );

        registrar.playToClient(
                PendingTeamInviteS2CPacket.TYPE,
                PendingTeamInviteS2CPacket.STREAM_CODEC,
                PendingTeamInviteS2CPacket::handle
        );

        registrar.playToClient(
                SyncPlayerActingDataS2CPacket.TYPE,
                SyncPlayerActingDataS2CPacket.STREAM_CODEC,
                SyncPlayerActingDataS2CPacket::handle
        );

        registrar.playToClient(
                PlayActingEffectS2CPacket.TYPE,
                PlayActingEffectS2CPacket.STREAM_CODEC,
                PlayActingEffectS2CPacket::handle
        );


        registrar.playToClient(
                SyncSharedAbilitiesDataS2CPacket.TYPE,
                SyncSharedAbilitiesDataS2CPacket.STREAM_CODEC,
                SyncSharedAbilitiesDataS2CPacket::handle
        );

        registrar.playToClient(
                RemoveMovableEffectS2CPacket.TYPE,
                RemoveMovableEffectS2CPacket.STREAM_CODEC,
                RemoveMovableEffectS2CPacket::handle
        );

        registrar.playToClient(
                CancelEffectByPositionS2CPacket.TYPE,
                CancelEffectByPositionS2CPacket.STREAM_CODEC,
                CancelEffectByPositionS2CPacket::handle
        );

        registrar.playToClient(
                UpdateMovableEffectPositionS2CPacket.TYPE,
                UpdateMovableEffectPositionS2CPacket.STREAM_CODEC,
                UpdateMovableEffectPositionS2CPacket::handle
        );

        registrar.playToClient(
                OpenCoordinateScreenForTeleportationS2CPacket.TYPE,
                OpenCoordinateScreenForTeleportationS2CPacket.STREAM_CODEC,
                OpenCoordinateScreenForTeleportationS2CPacket::handle
        );

        registrar.playToClient(
                HotGroundEffectS2CPacket.TYPE,
                HotGroundEffectS2CPacket.STREAM_CODEC,
                HotGroundEffectS2CPacket::handle
        );


        registrar.playToClient(
                AddMovableEffectS2CPacket.TYPE,
                AddMovableEffectS2CPacket.STREAM_CODEC,
                AddMovableEffectS2CPacket::handle
        );

        registrar.playToClient(
                OpenQuestAcceptanceScreenS2CPacket.TYPE,
                OpenQuestAcceptanceScreenS2CPacket.STREAM_CODEC,
                OpenQuestAcceptanceScreenS2CPacket::handle
        );

        registrar.playToClient(
                OpenPlayerDivinationScreenS2CPacket.TYPE,
                OpenPlayerDivinationScreenS2CPacket.STREAM_CODEC,
                OpenPlayerDivinationScreenS2CPacket::handle
        );

        registrar.playToClient(
                OpenStructureDivinationScreenS2CPacket.TYPE,
                OpenStructureDivinationScreenS2CPacket.STREAM_CODEC,
                OpenStructureDivinationScreenS2CPacket::handle
        );

        registrar.playToClient(
                OpenBiomeDivinationScreenS2CPacket.TYPE,
                OpenBiomeDivinationScreenS2CPacket.STREAM_CODEC,
                OpenBiomeDivinationScreenS2CPacket::handle
        );

        registrar.playToClient(
                OpenShapeShiftingScreenS2CPacket.TYPE,
                OpenShapeShiftingScreenS2CPacket.STREAM_CODEC,
                OpenShapeShiftingScreenS2CPacket::handle
        );

        registrar.playToClient(
                OpenHistoricalVoidBorrowingScreenS2CPacket.TYPE,
                OpenHistoricalVoidBorrowingScreenS2CPacket.STREAM_CODEC,
                OpenHistoricalVoidBorrowingScreenS2CPacket::handle
        );

        registrar.playToClient(
                SyncPlayerTeleportationPlayerNamesS2CPacket.TYPE,
                SyncPlayerTeleportationPlayerNamesS2CPacket.STREAM_CODEC,
                SyncPlayerTeleportationPlayerNamesS2CPacket::handle
        );

        registrar.playToClient(
                SyncPsychologicalInvisibilityS2CPacket.TYPE,
                SyncPsychologicalInvisibilityS2CPacket.STREAM_CODEC,
                SyncPsychologicalInvisibilityS2CPacket::handle
        );

        registrar.playToClient(
                AddEntityTagS2CPacket.TYPE,
                AddEntityTagS2CPacket.STREAM_CODEC,
                AddEntityTagS2CPacket::handle
        );

        registrar.playToClient(
                SyncPlayerTeleportationOnlinePlayersS2CPacket.TYPE,
                SyncPlayerTeleportationOnlinePlayersS2CPacket.STREAM_CODEC,
                SyncPlayerTeleportationOnlinePlayersS2CPacket::handle
        );

        registrar.playToClient(
                NameSyncS2CPacket.TYPE,
                NameSyncS2CPacket.CODEC,
                NameSyncS2CPacket::handle
        );

        registrar.playToClient(
                ShapeShiftingSyncS2CPacket.TYPE,
                ShapeShiftingSyncS2CPacket.STREAM_CODEC,
                ShapeShiftingSyncS2CPacket::handle
        );

        registrar.playToClient(
                PlayAnimationS2CPacket.TYPE,
                PlayAnimationS2CPacket.STREAM_CODEC,
                PlayAnimationS2CPacket::handle
        );

        registrar.playToClient(
                ResetClientEffectsS2CPacket.TYPE,
                ResetClientEffectsS2CPacket.STREAM_CODEC,
                ResetClientEffectsS2CPacket::handle
        );

        registrar.playToClient(
                SyncOriginalBodyOwnerS2CPacket.TYPE,
                SyncOriginalBodyOwnerS2CPacket.STREAM_CODEC,
                SyncOriginalBodyOwnerS2CPacket::handle
        );

        registrar.playToClient(
                SyncSkillScalingS2CPacket.TYPE,
                SyncSkillScalingS2CPacket.STREAM_CODEC,
                SyncSkillScalingS2CPacket::handle
        );
      
        registrar.playToClient(
                SyncSpiritChannelingS2CPacket.TYPE,
                SyncSpiritChannelingS2CPacket.STREAM_CODEC,
                SyncSpiritChannelingS2CPacket::handle
        );
      
        registrar.playToClient(
                SyncUniquenessS2CPacket.TYPE,
                SyncUniquenessS2CPacket.STREAM_CODEC,
                SyncUniquenessS2CPacket::handle
        );

        registrar.playToClient(
                SyncControllingDataS2CPacket.TYPE,
                SyncControllingDataS2CPacket.STREAM_CODEC,
                SyncControllingDataS2CPacket::handle
        );

        registrar.playToClient(
                OpenDiscernmentScreenS2CPacket.TYPE,
                OpenDiscernmentScreenS2CPacket.STREAM_CODEC,
                OpenDiscernmentScreenS2CPacket::handle
        );

        registrar.playToClient(
                SyncDiscernmentDataS2CPacket.TYPE,
                SyncDiscernmentDataS2CPacket.STREAM_CODEC,
                SyncDiscernmentDataS2CPacket::handle
        );

        registrar.playToClient(
                SyncEnvisioningS2CPacket.TYPE,
                SyncEnvisioningS2CPacket.STREAM_CODEC,
                SyncEnvisioningS2CPacket::handle
        );
    }

    private static void registerServerPackets(PayloadRegistrar registrar) {
        registrar.playToServer(
                BecomeBeyonderC2SPacket.TYPE,
                BecomeBeyonderC2SPacket.STREAM_CODEC,
                BecomeBeyonderC2SPacket::handle
        );

        registrar.playToServer(
                UseTeleportationAuthorityC2SPacket.TYPE,
                UseTeleportationAuthorityC2SPacket.STREAM_CODEC,
                UseTeleportationAuthorityC2SPacket::handle
        );

        registrar.playToServer(
                ExecuteBeyonderTradeC2SPacket.TYPE,
                ExecuteBeyonderTradeC2SPacket.STREAM_CODEC,
                ExecuteBeyonderTradeC2SPacket::handle
        );

        registrar.playToServer(
                DiscardQuestC2SPacket.TYPE,
                DiscardQuestC2SPacket.STREAM_CODEC,
                DiscardQuestC2SPacket::handle
        );

        registrar.playToServer(
                QuestAcceptanceResponseC2SPacket.TYPE,
                QuestAcceptanceResponseC2SPacket.STREAM_CODEC,
                QuestAcceptanceResponseC2SPacket::handle
        );

        registrar.playToServer(
                NextArtifactAbilityC2SPacket.TYPE,
                NextArtifactAbilityC2SPacket.STREAM_CODEC,
                NextArtifactAbilityC2SPacket::handle
        );

        registrar.playToServer(
                RequestQuestDataC2SPacket.TYPE,
                RequestQuestDataC2SPacket.STREAM_CODEC,
                RequestQuestDataC2SPacket::handle
        );

        registrar.playToServer(
                UseKeyboundAbilityC2SPacket.TYPE,
                UseKeyboundAbilityC2SPacket.STREAM_CODEC,
                UseKeyboundAbilityC2SPacket::handle
        );

        registrar.playToServer(
                SyncAbilityBarAbilitiesC2SPacket.TYPE,
                SyncAbilityBarAbilitiesC2SPacket.STREAM_CODEC,
                SyncAbilityBarAbilitiesC2SPacket::handle
        );

        registrar.playToServer(
                RequestAbilityBarC2SPacket.TYPE,
                RequestAbilityBarC2SPacket.STREAM_CODEC,
                RequestAbilityBarC2SPacket::handle
        );

        registrar.playToServer(
                RequestActiveStatusOfAbilityC2SPacket.TYPE,
                RequestActiveStatusOfAbilityC2SPacket.STREAM_CODEC,
                RequestActiveStatusOfAbilityC2SPacket::handle
        );

        registrar.playToServer(
                OpenAbilityWheelC2SPacket.TYPE,
                OpenAbilityWheelC2SPacket.STREAM_CODEC,
                OpenAbilityWheelC2SPacket::handle
        );

        registrar.playToServer(
                CloseAbilityWheelC2SPacket.TYPE,
                CloseAbilityWheelC2SPacket.STREAM_CODEC,
                CloseAbilityWheelC2SPacket::handle
        );

        registrar.playToServer(
                RequestAbilityWheelC2SPacket.TYPE,
                RequestAbilityWheelC2SPacket.STREAM_CODEC,
                RequestAbilityWheelC2SPacket::handle
        );

        registrar.playToServer(
                SyncAbilityWheelAbilitiesC2SPacket.TYPE,
                SyncAbilityWheelAbilitiesC2SPacket.STREAM_CODEC,
                SyncAbilityWheelAbilitiesC2SPacket::handle
        );

        registrar.playToServer(
                SyncSharedAbilitiesC2SPacket.TYPE,
                SyncSharedAbilitiesC2SPacket.STREAM_CODEC,
                SyncSharedAbilitiesC2SPacket::handle
        );

        registrar.playToServer(
                RequestSharedAbilitiesC2SPacket.TYPE,
                RequestSharedAbilitiesC2SPacket.STREAM_CODEC,
                RequestSharedAbilitiesC2SPacket::handle
        );

        registrar.playToServer(
                UpdateSelectedAbilityC2SPacket.TYPE,
                UpdateSelectedAbilityC2SPacket.STREAM_CODEC,
                UpdateSelectedAbilityC2SPacket::handle
        );

        registrar.playToServer(
                UseSelectedAbilityC2SPacket.TYPE,
                UseSelectedAbilityC2SPacket.STREAM_CODEC,
                UseSelectedAbilityC2SPacket::handle
        );

        registrar.playToServer(
                UseSharedAbilityC2SPacket.TYPE,
                UseSharedAbilityC2SPacket.STREAM_CODEC,
                UseSharedAbilityC2SPacket::handle
        );

        registrar.playToServer(
                UseCopiedAbilityC2SPacket.TYPE,
                UseCopiedAbilityC2SPacket.STREAM_CODEC,
                UseCopiedAbilityC2SPacket::handle
        );

        registrar.playToServer(
                AllyRequestResponseC2SPacket.TYPE,
                AllyRequestResponseC2SPacket.STREAM_CODEC,
                AllyRequestResponseC2SPacket::handle
        );


        registrar.playToServer(
                OpenIntrospectMenuC2SPacket.TYPE,
                OpenIntrospectMenuC2SPacket.STREAM_CODEC,
                OpenIntrospectMenuC2SPacket::handle
        );

        registrar.playToServer(
                PerformMiracleC2SPacket.TYPE,
                PerformMiracleC2SPacket.STREAM_CODEC,
                PerformMiracleC2SPacket::handle
        );

        registrar.playToServer(
                OpenRecipeMenuC2SPacket.TYPE,
                OpenRecipeMenuC2SPacket.STREAM_CODEC,
                OpenRecipeMenuC2SPacket::handle
        );

        registrar.playToServer(
                TeleportToSefirotC2SPacket.TYPE,
                TeleportToSefirotC2SPacket.STREAM_CODEC,
                TeleportToSefirotC2SPacket::handle
        );

        registrar.playToServer(
                SyncDreamDivinationCoordinatesC2SPacket.TYPE,
                SyncDreamDivinationCoordinatesC2SPacket.STREAM_CODEC,
                SyncDreamDivinationCoordinatesC2SPacket::handle
        );

        registrar.playToServer(
                SyncTravelersDoorCoordinatesC2SPacket.TYPE,
                SyncTravelersDoorCoordinatesC2SPacket.STREAM_CODEC,
                SyncTravelersDoorCoordinatesC2SPacket::handle
        );

        registrar.playToServer(
                AbilitySelectionC2SPacket.TYPE,
                AbilitySelectionC2SPacket.STREAM_CODEC,
                AbilitySelectionC2SPacket::handle
        );

        registrar.playToServer(
                TeleportPlayerToLocationC2SPacket.TYPE,
                TeleportPlayerToLocationC2SPacket.STREAM_CODEC,
                TeleportPlayerToLocationC2SPacket::handle
        );

        registrar.playToServer(
                ToggleGriefingC2SPacket.TYPE,
                ToggleGriefingC2SPacket.STREAM_CODEC,
                ToggleGriefingC2SPacket::handle
        );

        registrar.playToServer(
                InventoryOpenedC2SPacket.TYPE,
                InventoryOpenedC2SPacket.STREAM_CODEC,
                InventoryOpenedC2SPacket::handle);


        registrar.playToServer(
                OpenHonorificNamesMenuC2SPacket.TYPE,
                OpenHonorificNamesMenuC2SPacket.STREAM_CODEC,
                OpenHonorificNamesMenuC2SPacket::handle);

        registrar.playToServer(
                HonorificNamesRespondC2SPacket.TYPE,
                HonorificNamesRespondC2SPacket.STREAM_CODEC,
                HonorificNamesRespondC2SPacket::handle);

        registrar.playToServer(
                SetHonorificNameC2SPacket.TYPE,
                SetHonorificNameC2SPacket.STREAM_CODEC,
                SetHonorificNameC2SPacket::handle);

        registrar.playToServer(
                PlayerDivinationSelectedC2SPacket.TYPE,
                PlayerDivinationSelectedC2SPacket.STREAM_CODEC,
                PlayerDivinationSelectedC2SPacket::handle);

        registrar.playToServer(
                StructureDivinationSelectedC2SPacket.TYPE,
                StructureDivinationSelectedC2SPacket.STREAM_CODEC,
                StructureDivinationSelectedC2SPacket::handle);

        registrar.playToServer(
                BiomeDivinationSelectedC2SPacket.TYPE,
                BiomeDivinationSelectedC2SPacket.STREAM_CODEC,
                BiomeDivinationSelectedC2SPacket::handle);

        registrar.playToServer(
                HistoricalVoidBorrowingSelectedC2SPacket.TYPE,
                HistoricalVoidBorrowingSelectedC2SPacket.STREAM_CODEC,
                HistoricalVoidBorrowingSelectedC2SPacket::handle);

        registrar.playToServer(
                ShapeShiftingSelectedC2SPacket.TYPE,
                ShapeShiftingSelectedC2SPacket.STREAM_CODEC,
                ShapeShiftingSelectedC2SPacket::handle);

        registrar.playToServer(
                ReturnToMainBodyC2SPacket.TYPE,
                ReturnToMainBodyC2SPacket.STREAM_CODEC,
                ReturnToMainBodyC2SPacket::handle);

        registrar.playToServer(
                OpenArtifactWheelC2SPacket.TYPE,
                OpenArtifactWheelC2SPacket.STREAM_CODEC,
                OpenArtifactWheelC2SPacket::handle);

        registrar.playToServer(
                SyncArtifactAbilityWheelC2SPacket.TYPE,
                SyncArtifactAbilityWheelC2SPacket.STREAM_CODEC,
                SyncArtifactAbilityWheelC2SPacket::handle
        );

        registrar.playToServer(
                RequestUniquenessApotheosisC2SPacket.TYPE,
                RequestUniquenessApotheosisC2SPacket.STREAM_CODEC,
                RequestUniquenessApotheosisC2SPacket::handle
        );

        registrar.playToServer(
                DiscernmentSelectedC2SPacket.TYPE,
                DiscernmentSelectedC2SPacket.STREAM_CODEC,
                DiscernmentSelectedC2SPacket::handle
        );

        registrar.playToServer(
                StopDiscernmentC2SPacket.TYPE,
                StopDiscernmentC2SPacket.STREAM_CODEC,
                StopDiscernmentC2SPacket::handle
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
        float digestionProgress = BeyonderData.getDigestionProgress(player);
        int[] charStacks = BeyonderData.getCharStacks(player);
        int cowardWormAmount = BeyonderData.getCowardWormAmount(player);

        String[] history = BeyonderData.getPathwayHistory(player);

        SyncBeyonderDataS2CPacket packet = new SyncBeyonderDataS2CPacket(pathway, sequence, spirituality, griefingEnabled, digestionProgress, history, charStacks, cowardWormAmount);
        sendToPlayer(player, packet);
    }

    public static void syncBeyonderDataToEntity(LivingEntity entity) {
        if (entity instanceof ServerPlayer) return;

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        SyncLivingEntityBeyonderDataS2CPacket packet =
                new SyncLivingEntityBeyonderDataS2CPacket(entity.getId(), pathway, sequence, BeyonderData.getMaxSpirituality(pathway, sequence));

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
        SyncUniquenessS2CPacket packet = new SyncUniquenessS2CPacket(
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
        int[] charStacks = BeyonderData.getCharStacks(targetPlayer);
        int wormAmount = BeyonderData.getCowardWormAmount(targetPlayer);

        SyncBeyonderDataS2CPacket packet = new SyncBeyonderDataS2CPacket(pathway, sequence, spirituality, griefingEnabled, digestionProgress, new String[10], charStacks, wormAmount);

        targetPlayer.getServer().getPlayerList().getPlayers().forEach(player -> {
            sendToPlayer(player, packet);
        });
    }
}
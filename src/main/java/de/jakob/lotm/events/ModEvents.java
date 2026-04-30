package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TeamComponent;
import de.jakob.lotm.command.*;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.client.ability_entities.death_pathway.underworld_gate.UnderworldGateModel;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.travelers_door.TravelersDoorModel;
import de.jakob.lotm.entity.client.ability_entities.meteor.MeteorModel;
import de.jakob.lotm.entity.client.ability_entities.mother_pathway.blooming_area.BloomingAreaModel;
import de.jakob.lotm.entity.client.ability_entities.mother_pathway.coffin.CoffinModel;
import de.jakob.lotm.entity.client.projectiles.paper_dagger.PaperDaggerProjectileModel;
import de.jakob.lotm.entity.client.projectiles.spear_of_destruction.SpearOfDestructionProjectileModel;
import de.jakob.lotm.entity.client.projectiles.spear_of_light.SpearOfLightProjectileModel;
import de.jakob.lotm.entity.client.projectiles.unshadowed_spear.UnshadowedSpearProjectileModel;
import de.jakob.lotm.entity.client.projectiles.wind_blade.WindBladeModel;
import de.jakob.lotm.entity.client.ability_entities.red_priest_pathway.war_banner.WarBannerModel;
import de.jakob.lotm.entity.client.ability_entities.tornado.TornadoModel;
import de.jakob.lotm.entity.client.ability_entities.tyrant_pathway.tsunami.TsunamiModel;
import de.jakob.lotm.entity.client.ability_entities.volcano.VolcanoModel;
import de.jakob.lotm.entity.client.ability_entities.wheel_of_fortune_pathway.cycle_of_fate.CycleOfFateModel;
import de.jakob.lotm.entity.client.ability_entities.mother_pathway.desolate_area.DesolateAreaModel;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.exile_doors.ExileDoorsModel;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.return_portal.HighSequenceDoorsModel;
import de.jakob.lotm.entity.client.ability_entities.justiciar_pathway.judgment_sword.JudgmentSwordModel;
import de.jakob.lotm.entity.client.ability_entities.sun_pathway.justice_sword.JusticeSwordModel;
import de.jakob.lotm.entity.client.projectiles.fireball.FireballModel;
import de.jakob.lotm.entity.client.projectiles.flaming_spear.FlamingSpearProjectileModel;
import de.jakob.lotm.entity.client.projectiles.frost_spear.FrostSpearProjectileModel;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.apprentice_door.ApprenticeDoorModel;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.book.ApprenticeBookModel;
import de.jakob.lotm.entity.client.ability_entities.wheel_of_fortune_pathway.misfortune_words.MisfortuneWordsModel;
import de.jakob.lotm.entity.client.beyonder_npc.QuestMarkerModel;
import de.jakob.lotm.entity.client.fire_raven.FireRavenModel;
import de.jakob.lotm.entity.client.spirits.bizarro_bane.SpiritBizarroBaneModel;
import de.jakob.lotm.entity.client.spirits.blue_wizard.SpiritBlueWizardModel;
import de.jakob.lotm.entity.client.spirits.bubbles.SpiritBubblesModel;
import de.jakob.lotm.entity.client.spirits.dervish.SpiritDervishModel;
import de.jakob.lotm.entity.client.spirits.ghost.SpiritGhostModel;
import de.jakob.lotm.entity.client.spirits.malmouth.SpiritMalmouthModel;
import de.jakob.lotm.entity.client.spirits.spirit_bane.SpiritBaneModel;
import de.jakob.lotm.entity.client.spirits.translucent_wizard.SpiritTranslucentWizardModel;
import de.jakob.lotm.entity.custom.*;
import de.jakob.lotm.entity.custom.ability_entities.OriginalBodyEntity;
import de.jakob.lotm.entity.custom.spirits.*;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataPacket;
import de.jakob.lotm.rendering.models.door.DoorHighMythicalCreatureModel;
import de.jakob.lotm.rendering.models.fool.FoolMythicalCreatureModel;
import de.jakob.lotm.rendering.models.red_priest.RedPriestMythicalCreatureModel;
import de.jakob.lotm.rendering.models.sun.SunMythicalCreatureModel;
import de.jakob.lotm.rendering.models.wheel_of_fortune.WheelOfFortuneMythicalCreatureModel;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.helper.TeamUtils;
import de.jakob.lotm.rendering.models.door.DoorMythicalCreatureModel;
import de.jakob.lotm.rendering.models.tyrant.TyrantMythicalCreatureModel;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import de.jakob.lotm.abilities.tyrant.LightningStormAbility;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static de.jakob.lotm.abilities.fool.HistoricalVoidSummoningAbility.MARKED_ENTITIES_TAG;
import static de.jakob.lotm.util.BeyonderData.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ModEvents {

    public static final Map<UUID, Integer> leoderoUsesLeft = new ConcurrentHashMap<>();
    private static final LightningStormAbility LIGHTNING_STORM = new LightningStormAbility("lightning_storm_leodero");

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FlamingSpearProjectileModel.LAYER_LOCATION, FlamingSpearProjectileModel::createBodyLayer);
        event.registerLayerDefinition(UnshadowedSpearProjectileModel.LAYER_LOCATION, UnshadowedSpearProjectileModel::createBodyLayer);
        event.registerLayerDefinition(FireballModel.LAYER_LOCATION, FireballModel::createBodyLayer);
        event.registerLayerDefinition(WindBladeModel.LAYER_LOCATION, WindBladeModel::createBodyLayer);
        event.registerLayerDefinition(ApprenticeDoorModel.LAYER_LOCATION, ApprenticeDoorModel::createBodyLayer);
        event.registerLayerDefinition(TravelersDoorModel.LAYER_LOCATION, TravelersDoorModel::createBodyLayer);
        event.registerLayerDefinition(ApprenticeBookModel.LAYER_LOCATION, ApprenticeBookModel::createBodyLayer);
        event.registerLayerDefinition(PaperDaggerProjectileModel.LAYER_LOCATION, PaperDaggerProjectileModel::createBodyLayer);
        event.registerLayerDefinition(FireRavenModel.LAYER_LOCATION, FireRavenModel::createBodyLayer);
        event.registerLayerDefinition(FrostSpearProjectileModel.LAYER_LOCATION, FrostSpearProjectileModel::createBodyLayer);
        event.registerLayerDefinition(TsunamiModel.LAYER_LOCATION, TsunamiModel::createBodyLayer);
        event.registerLayerDefinition(TornadoModel.LAYER_LOCATION, TornadoModel::createBodyLayer);
        event.registerLayerDefinition(ExileDoorsModel.LAYER_LOCATION, ExileDoorsModel::createBodyLayer);
        event.registerLayerDefinition(WarBannerModel.LAYER_LOCATION, WarBannerModel::createBodyLayer);
        event.registerLayerDefinition(MeteorModel.LAYER_LOCATION, MeteorModel::createBodyLayer);
        event.registerLayerDefinition(JudgmentSwordModel.LAYER_LOCATION, JudgmentSwordModel::createBodyLayer);
        event.registerLayerDefinition(JusticeSwordModel.LAYER_LOCATION, JusticeSwordModel::createBodyLayer);
        event.registerLayerDefinition(SpearOfLightProjectileModel.LAYER_LOCATION, SpearOfLightProjectileModel::createBodyLayer);
        event.registerLayerDefinition(VolcanoModel.LAYER_LOCATION, VolcanoModel::createBodyLayer);
        event.registerLayerDefinition(SpearOfDestructionProjectileModel.LAYER_LOCATION, SpearOfDestructionProjectileModel::createBodyLayer);
        event.registerLayerDefinition(HighSequenceDoorsModel.LAYER_LOCATION, HighSequenceDoorsModel::createBodyLayer);
        event.registerLayerDefinition(CoffinModel.LAYER_LOCATION, CoffinModel::createBodyLayer);
        event.registerLayerDefinition(MisfortuneWordsModel.LAYER_LOCATION, MisfortuneWordsModel::createBodyLayer);
        event.registerLayerDefinition(BloomingAreaModel.LAYER_LOCATION, BloomingAreaModel::createBodyLayer);
        event.registerLayerDefinition(DesolateAreaModel.LAYER_LOCATION, DesolateAreaModel::createBodyLayer);
        event.registerLayerDefinition(QuestMarkerModel.LAYER_LOCATION, QuestMarkerModel::createBodyLayer);
        event.registerLayerDefinition(CycleOfFateModel.LAYER_LOCATION, CycleOfFateModel::createBodyLayer);
        event.registerLayerDefinition(UnderworldGateModel.LAYER_LOCATION, UnderworldGateModel::createBodyLayer);

        // Spirits
        event.registerLayerDefinition(SpiritDervishModel.LAYER_LOCATION, SpiritDervishModel::createBodyLayer);
        event.registerLayerDefinition(SpiritBubblesModel.LAYER_LOCATION, SpiritBubblesModel::createBodyLayer);
        event.registerLayerDefinition(SpiritBlueWizardModel.LAYER_LOCATION, SpiritBlueWizardModel::createBodyLayer);
        event.registerLayerDefinition(SpiritTranslucentWizardModel.LAYER_LOCATION, SpiritTranslucentWizardModel::createBodyLayer);
        event.registerLayerDefinition(SpiritGhostModel.LAYER_LOCATION, SpiritGhostModel::createBodyLayer);
        event.registerLayerDefinition(SpiritBizarroBaneModel.LAYER_LOCATION, SpiritBizarroBaneModel::createBodyLayer);
        event.registerLayerDefinition(SpiritBaneModel.LAYER_LOCATION, SpiritBaneModel::createBodyLayer);
        event.registerLayerDefinition(SpiritMalmouthModel.LAYER_LOCATION, SpiritMalmouthModel::createBodyLayer);

        // Mythical Creature Forms
        event.registerLayerDefinition(TyrantMythicalCreatureModel.LAYER_LOCATION, TyrantMythicalCreatureModel::createBodyLayer);
        event.registerLayerDefinition(DoorMythicalCreatureModel.LAYER_LOCATION, DoorMythicalCreatureModel::createBodyLayer);
        event.registerLayerDefinition(FoolMythicalCreatureModel.LAYER_LOCATION, FoolMythicalCreatureModel::createBodyLayer);
        event.registerLayerDefinition(WheelOfFortuneMythicalCreatureModel.LAYER_LOCATION, WheelOfFortuneMythicalCreatureModel::createBodyLayer);
        event.registerLayerDefinition(RedPriestMythicalCreatureModel.LAYER_LOCATION, RedPriestMythicalCreatureModel::createBodyLayer);
        event.registerLayerDefinition(SunMythicalCreatureModel.LAYER_LOCATION, SunMythicalCreatureModel::createBodyLayer);
        event.registerLayerDefinition(DoorHighMythicalCreatureModel.LAYER_LOCATION, DoorHighMythicalCreatureModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FIRE_RAVEN.get(), FireRavenEntity.createAttributes().build());
        event.put(ModEntities.BEYONDER_NPC.get(), BeyonderNPCEntity.createAttributes().build());
        event.put(ModEntities.ERROR_AVATAR.get(), AvatarEntity.createAttributes().build());
        event.put(ModEntities.ORIGINAL_BODY.get(), OriginalBodyEntity.createAttributes().build());
        event.put(ModEntities.DAMAGE_TRACKER.get(), DamageTrackerEntity.createAttributes().build());

        event.put(ModEntities.SPIRIT_DERVISH_ENTITY.get(), SpiritDervishEntity.createAttributes().build());
        event.put(ModEntities.SPIRIT_BLUE_WIZARD.get(), SpiritBlueWizardEntity.createAttributes().build());
        event.put(ModEntities.SPIRIT_BUBBLES_ENTITY.get(), SpiritDervishEntity.createAttributes().build());
        event.put(ModEntities.SPIRIT_TRANSLUCENT_WIZARD.get(), SpiritTranslucentWizardEntity.createAttributes().build());
        event.put(ModEntities.SPIRIT_GHOST.get(), SpiritGhostEntity.createAttributes().build());
        event.put(ModEntities.SPIRIT_BIZARRO_BANE.get(), SpiritBizarroBaneEntity.createAttributes().build());
        event.put(ModEntities.SPIRIT_BANE.get(), SpiritBaneEntity.createAttributes().build());
        event.put(ModEntities.SPIRIT_MALMOUTH.get(), SpiritMalmouthEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntities.BEYONDER_NPC.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BeyonderNPCEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.SPIRIT_DERVISH_ENTITY.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.SPIRIT_BUBBLES_ENTITY.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.SPIRIT_BLUE_WIZARD.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.SPIRIT_TRANSLUCENT_WIZARD.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.SPIRIT_GHOST.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.SPIRIT_BIZARRO_BANE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.SPIRIT_BANE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                ModEntities.SPIRIT_MALMOUTH.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        BeyonderCommand.register(event.getDispatcher());
        LuckCheckCommand.register(event.getDispatcher());
        AllyRequestCommands.register(event.getDispatcher());
        AllyCommand.register(event.getDispatcher());
        SanityCommand.register(event.getDispatcher());
        DigestionCommand.register(event.getDispatcher());
        QuestCommand.register(event.getDispatcher());
        BeyonderMapCommand.register(event.getDispatcher());
        DisableAbilityCommand.register(event.getDispatcher());
        EnableAbilityCommand.register(event.getDispatcher());
        CharacteristicsStackCommand.register(event.getDispatcher());
        TeamCommand.register(event.getDispatcher());
        TeamInviteResponseCommand.register(event.getDispatcher());
        KillCountCommand.register(event.getDispatcher());
        UniquenessCommand.register(event.getDispatcher());
        SefirotCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer leavingPlayer)) return;

        TeamComponent team = leavingPlayer.getData(ModAttachments.TEAM_COMPONENT.get());
        MinecraftServer server = leavingPlayer.getServer();
        if (server == null) return;

        if (team.memberCount() > 0) {
            // Leaving player is a leader — clear the shared tab for all online members
            SyncSharedAbilitiesDataPacket clearPacket = new SyncSharedAbilitiesDataPacket(
                    "", new ArrayList<>(), new ArrayList<>(), new HashMap<>(), 0, 0);
            for (String memberUUID : team.memberUUIDs()) {
                ServerPlayer member = server.getPlayerList().getPlayer(
                        java.util.UUID.fromString(memberUUID));
                if (member != null) {
                    PacketHandler.sendToPlayer(member, clearPacket);
                }
            }
        } else if (team.isInTeam()) {
            // Leaving player is a member — re-sync the team so their abilities vanish from the pool
            ServerPlayer leader = server.getPlayerList().getPlayer(
                    java.util.UUID.fromString(team.leaderUUID()));
            if (leader != null) {
                TeamUtils.syncToTeam(leader);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        // Sync uniqueness data on login
        PacketHandler.syncUniquenessToPlayer(player);

        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());

        if (team.isInTeam()) {
            // Player is a member — check if their leader still has them listed
            ServerPlayer leader = server.getPlayerList().getPlayer(java.util.UUID.fromString(team.leaderUUID()));
            if (leader != null && !leader.getData(ModAttachments.TEAM_COMPONENT.get()).hasMember(player.getStringUUID())) {
                // Leader is online but no longer claims this player — clear stale membership
                player.setData(ModAttachments.TEAM_COMPONENT.get(), team.clearLeader());
                PacketHandler.sendToPlayer(player, new de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataPacket(
                        "", new ArrayList<>(), new ArrayList<>(), new java.util.HashMap<>(), 0, 0));
            }
        } else if (team.memberCount() > 0) {
            // Player is a leader — remove any members whose leaderUUID no longer points back to this leader
            TeamComponent current = team;
            for (String memberUUID : new ArrayList<>(current.memberUUIDs())) {
                ServerPlayer member = server.getPlayerList().getPlayer(java.util.UUID.fromString(memberUUID));
                if (member != null && !member.getData(ModAttachments.TEAM_COMPONENT.get()).leaderUUID().equals(player.getStringUUID())) {
                    current = current.removeMember(memberUUID);
                }
            }
            if (!current.memberUUIDs().equals(team.memberUUIDs())) {
                player.setData(ModAttachments.TEAM_COMPONENT.get(), current);
            }
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if (!event.getMessage().getString().equalsIgnoreCase("LEODERO!")) return;

        ServerPlayer player = event.getPlayer();
        UUID uuid = player.getUUID();

        int usesLeft = leoderoUsesLeft.getOrDefault(uuid, 1);
        if (usesLeft <= 0) return;

        leoderoUsesLeft.put(uuid, usesLeft - 1);

        if (player.level() instanceof ServerLevel serverLevel) {
            net.minecraft.world.entity.decoration.ArmorStand dummy = new net.minecraft.world.entity.decoration.ArmorStand(serverLevel, player.getX(), player.getY(), player.getZ());
            dummy.setYRot(player.getYRot());
            dummy.setXRot(player.getXRot());
            serverLevel.addFreshEntity(dummy);
            ServerScheduler.scheduleDelayed(0, () -> {
                LIGHTNING_STORM.onAbilityUse(serverLevel, dummy);
                dummy.discard();
            }, serverLevel);

        }
    }
}

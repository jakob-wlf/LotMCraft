package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.command.*;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.travelers_door.TravelersDoorModel;
import de.jakob.lotm.entity.client.ability_entities.meteor.MeteorModel;
import de.jakob.lotm.entity.client.ability_entities.mother_pathway.blooming_area.BloomingAreaModel;
import de.jakob.lotm.entity.client.ability_entities.mother_pathway.coffin.CoffinModel;
import de.jakob.lotm.entity.client.ability_entities.projectiles.paper_dagger.PaperDaggerProjectileModel;
import de.jakob.lotm.entity.client.ability_entities.projectiles.spear_of_destruction.SpearOfDestructionProjectileModel;
import de.jakob.lotm.entity.client.ability_entities.projectiles.spear_of_light.SpearOfLightProjectileModel;
import de.jakob.lotm.entity.client.ability_entities.projectiles.unshadowed_spear.UnshadowedSpearProjectileModel;
import de.jakob.lotm.entity.client.ability_entities.projectiles.wind_blade.WindBladeModel;
import de.jakob.lotm.entity.client.ability_entities.red_priest_pathway.war_banner.WarBannerModel;
import de.jakob.lotm.entity.client.ability_entities.tornado.TornadoModel;
import de.jakob.lotm.entity.client.ability_entities.tyrant_pathway.tsunami.TsunamiModel;
import de.jakob.lotm.entity.client.ability_entities.volcano.VolcanoModel;
import de.jakob.lotm.entity.client.ability_entities.wheel_of_fortune_pathway.cycle_of_fate.CycleOfFateModel;
import de.jakob.lotm.entity.client.ability_entities.mother_pathway.desolate_area.DesolateAreaModel;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.exile_doors.ExileDoorsModel;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.return_portal.HighSequenceDoorsModel;
import de.jakob.lotm.entity.client.ability_entities.sun_pathway.justice_sword.JusticeSwordModel;
import de.jakob.lotm.entity.client.ability_entities.projectiles.fireball.FireballModel;
import de.jakob.lotm.entity.client.ability_entities.projectiles.flaming_spear.FlamingSpearProjectileModel;
import de.jakob.lotm.entity.client.ability_entities.projectiles.frost_spear.FrostSpearProjectileModel;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.apprentice_door.ApprenticeDoorModel;
import de.jakob.lotm.entity.client.ability_entities.door_pathway.book.ApprenticeBookModel;
import de.jakob.lotm.entity.client.ability_entities.wheel_of_fortune_pathway.misfortune_words.MisfortuneWordsModel;
import de.jakob.lotm.entity.client.beyonder_npc.QuestMarkerModel;
import de.jakob.lotm.entity.client.fire_raven.FireRavenModel;
import de.jakob.lotm.entity.client.spirits.bubbles.SpiritBubblesModel;
import de.jakob.lotm.entity.client.spirits.dervish.SpiritDervishModel;
import de.jakob.lotm.entity.custom.*;
import de.jakob.lotm.entity.custom.ability_entities.OriginalBodyEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritDervishEntity;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.rendering.models.DoorMythicalCreatureModel;
import de.jakob.lotm.rendering.models.TyrantMythicalCreatureModel;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.SpiritualityProgressTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static de.jakob.lotm.abilities.fool.HistoricalVoidSummoningAbility.MARKED_ENTITIES_TAG;
import static de.jakob.lotm.util.BeyonderData.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ModEvents {

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

        // Spirits
        event.registerLayerDefinition(SpiritDervishModel.LAYER_LOCATION, SpiritDervishModel::createBodyLayer);
        event.registerLayerDefinition(SpiritBubblesModel.LAYER_LOCATION, SpiritBubblesModel::createBodyLayer);

        // Mythical Creature Forms
        event.registerLayerDefinition(TyrantMythicalCreatureModel.LAYER_LOCATION, TyrantMythicalCreatureModel::createBodyLayer);
        event.registerLayerDefinition(DoorMythicalCreatureModel.LAYER_LOCATION, DoorMythicalCreatureModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FIRE_RAVEN.get(), FireRavenEntity.createAttributes().build());
        event.put(ModEntities.SPIRIT_DERVISH_ENTITY.get(), SpiritDervishEntity.createAttributes().build());
        event.put(ModEntities.SPIRIT_BUBBLES_ENTITY.get(), SpiritDervishEntity.createAttributes().build());
        event.put(ModEntities.BEYONDER_NPC.get(), BeyonderNPCEntity.createAttributes().build());
        event.put(ModEntities.ERROR_AVATAR.get(), AvatarEntity.createAttributes().build());
        event.put(ModEntities.ORIGINAL_BODY.get(), OriginalBodyEntity.createAttributes().build());
        event.put(ModEntities.DAMAGE_TRACKER.get(), DamageTrackerEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntities.BEYONDER_NPC.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> {
                    // Get the ServerLevel to access gamerules
                    if (level.getLevel() instanceof ServerLevel serverLevel) {
                        if (!serverLevel.getGameRules().getBoolean(ModGameRules.ALLOW_BEYONDER_SPAWNING)) {
                            return false;
                        }
                    }

                    // Then check the normal mob spawn rules
                    return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
                },
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
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        BeyonderCommand.register(event.getDispatcher());
        SkinChangeCommand.register(event.getDispatcher());
        AllyRequestCommands.register(event.getDispatcher());
        SanityCommand.register(event.getDispatcher());
        DigestionCommand.register(event.getDispatcher());
        QuestCommand.register(event.getDispatcher());
        BeyonderMapCommand.register(event.getDispatcher());
        DisableAbilityCommand.register(event.getDispatcher());
        EnableAbilityCommand.register(event.getDispatcher());
        HonorificNameCommand.register(event.getDispatcher());
        CharacteristicsStackCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        // Only copy data if the original player was a beyonder
        if (isBeyonder(original)) {
            String pathway = getPathway(original);
            int sequence = getSequence(original);
            boolean griefingEnabled = original.getPersistentData().getBoolean(NBT_GRIEFING_ENABLED);
            Tag markedEntities = original.getPersistentData().get(MARKED_ENTITIES_TAG);

            // Copy the data to the new player
            CompoundTag newTag = newPlayer.getPersistentData();
            newTag.putString(NBT_PATHWAY, pathway);
            newTag.putInt(NBT_SEQUENCE, sequence);
            newTag.putFloat(NBT_SPIRITUALITY, BeyonderData.getMaxSpirituality(sequence));
            newTag.putBoolean(NBT_GRIEFING_ENABLED, griefingEnabled);
            if (markedEntities != null) {
                newTag.put(MARKED_ENTITIES_TAG, markedEntities.copy());
            }

            // Update spirituality progress tracker
            if (getMaxSpirituality(sequence) > 0) {
                float progress = 1;
                SpiritualityProgressTracker.setProgress(newPlayer.getUUID(), progress);
            }
        }
    }
}

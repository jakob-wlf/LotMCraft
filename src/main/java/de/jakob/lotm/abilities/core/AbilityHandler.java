package de.jakob.lotm.abilities.core;

import de.jakob.lotm.abilities.abyss.*;
import de.jakob.lotm.abilities.common.*;
import de.jakob.lotm.abilities.darkness.*;
import de.jakob.lotm.abilities.demoness.*;
import de.jakob.lotm.abilities.door.*;
import de.jakob.lotm.abilities.error.*;
import de.jakob.lotm.abilities.fool.*;
import de.jakob.lotm.abilities.mother.*;
import de.jakob.lotm.abilities.red_priest.*;
import de.jakob.lotm.abilities.sun.*;
import de.jakob.lotm.abilities.tyrant.*;
import de.jakob.lotm.abilities.visionary.*;
import de.jakob.lotm.abilities.wheel_of_fortune.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

public class AbilityHandler {

    private final HashSet<Ability> abilities = new HashSet<>();

    public AbilityHandler() {
        registerAbilities();
    }

    private void registerAbilities() {
        // COMMON
        abilities.add(new CogitationAbility("cogitation_ability"));
        abilities.add(new AllyAbility("ally_ability"));
        abilities.add(new DivinationAbility("divination_ability"));
        abilities.add(new SpiritVisionAbility("spirit_vision_ability"));
        abilities.add(new CurseOfMisfortuneAbility("curse_of_misfortune_ability"));

        // SUN PATHWAY
        abilities.add(new HolySongAbility("holy_song_ability"));
        abilities.add(new IlluminateAbility("illuminate_ability"));
        abilities.add(new HolyLightAbility("holy_light_ability"));
        abilities.add(new FireOfLightAbility("fire_of_light_ability"));
        abilities.add(new CleaveOfPurificationAbility("cleave_of_purification_ability"));
        abilities.add(new HolyOathAbility("holy_oath_ability"));
        abilities.add(new GodSaysItsEffectiveAbility("notary_buff_ability"));
        abilities.add(new GodSaysItsNotEffectiveAbility("notary_debuff_ability"));
        abilities.add(new LightOfHolinessAbility("light_of_holiness_ability"));
        abilities.add(new PurificationHaloAbility("purification_halo_ability"));
        abilities.add(new FlaringSunAbility("flaring_sun_ability"));
        abilities.add(new UnshadowedSpearAbility("unshadowed_spear_ability"));
        abilities.add(new UnshadowedDomainAbility("unshadowed_domain_ability"));
        abilities.add(new WallOfLightAbility("wall_of_light_ability"));
        abilities.add(new SwordOfJusticeAbility("sword_of_justice_ability"));
        abilities.add(new SpearOfLightAbility("spear_of_light_ability"));
        abilities.add(new SolarEnvoyAbility("solar_envoy_ability"));
        abilities.add(new WingsOfLightAbility("wings_of_light_ability"));
        abilities.add(new PureWhiteLightAbility("pure_white_light_ability"));
        abilities.add(new DivineKingdomManifestationAbility("divine_kingdom_manifestation_ability"));

        // RED-PRIEST PATHWAY
        abilities.add(new TrapAbility("trap_ability"));
        abilities.add(new ProvokingAbility("provoking_ability"));
        abilities.add(new PyrokinesisAbility("pyrokinesis_ability"));
        abilities.add(new CullAbility("cull_ability"));
        abilities.add(new FlameMasteryAbility("flame_mastery_ability"));
        abilities.add(new SteelMasteryAbility("steel_mastery_ability"));
        abilities.add(new ChainOfCommandAbility("chain_of_command_ability"));
        abilities.add(new WarCryAbility("war_cry_ability"));
        abilities.add(new WarSongAbility("war_song_ability"));
        abilities.add(new FogOfWarAbility("fog_of_war_ability"));
        abilities.add(new EssenceOfWarAbility("essence_of_war_ability"));
        abilities.add(new FlightAbility("flight_ability"));
        abilities.add(new WeatherManipulationAbility("weather_manipulation_ability"));
        abilities.add(new ConqueringAbility("conquering_ability"));
        abilities.add(new FlameAuthorityAbility("flame_authority_ability"));
        abilities.add(new PuppetSoldierCreationAbility("puppet_soldier_creation_ability"));

        // TYRANT PATHWAY
        abilities.add(new RagingBlowsAbility("raging_blows_ability"));
        abilities.add(new WaterManipulationAbility("water_manipulation_ability"));
        abilities.add(new WindManipulationAbility("wind_manipulation_ability"));
        abilities.add(new LightningAbility("lightning_ability"));
        abilities.add(new SirenSongAbility("siren_song_ability"));
        abilities.add(new TsunamiAbility("tsunami_ability"));
        abilities.add(new EarthquakeAbility("earthquake_ability"));
        abilities.add(new HurricaneAbility("hurricane_ability"));
        abilities.add(new RoarAbility("roar_ability"));
        abilities.add(new WaterMasteryAbility("water_mastery_ability"));
        abilities.add(new TorrentialDownpourAbility("torrential_downpour_ability"));
        abilities.add(new LightningStormAbility("lightning_storm_ability"));
        abilities.add(new ThunderclapAbility("thunderclap_ability"));
        abilities.add(new LightningBranchAbility("lightning_branch_ability"));
        abilities.add(new CalamityCreationAbility("calamity_creation_ability"));
        abilities.add(new MythicalCreatureFormTyrantAbility("mythical_creature_tyrant_ability"));
        abilities.add(new EnergyTransformationAbility("energy_transformation_ability"));
        abilities.add(new HeavenlyPunishmentAbility("heavenly_punishment_ability"));
        abilities.add(new ElectromagneticTornadoAbility("electromagnetic_tornado_ability"));
        abilities.add(new RoarOfTheThunderGodAbility("roar_of_the_thunder_god_ability"));

        // ABYSS PATHWAY
        abilities.add(new ToxicSmokeAbility("toxic_smoke_ability"));
        abilities.add(new PoisonousFlameAbility("poisonous_flame_ability"));
        abilities.add(new FlameSpellsAbility("flame_spells_ability"));
        abilities.add(new LanguageOfFoulnessAbility("language_of_foulness_ability"));
        abilities.add(new DevilTransformationAbility("devil_transformation_ability"));
        abilities.add(new AvatarOfDesireAbility("avatar_of_desire_ability"));
        abilities.add(new DefilingSeedAbility("defiling_seed_ability"));

        // FOOL PATHWAY
        abilities.add(new AirBulletAbility("air_bullet_ability"));
        abilities.add(new FlameControllingAbility("flame_controlling_ability"));
        abilities.add(new PaperFigurineSubstituteAbility("paper_figurine_substitute_ability"));
        abilities.add(new FlamingJumpAbility("flaming_jump_ability"));
        abilities.add(new UnderWaterBreathingAbility("underwater_breathing_ability"));
        abilities.add(new ShapeShiftingAbility("shapeshifting_ability"));
        abilities.add(new PuppeteeringAbility("puppeteering_ability"));
        abilities.add(new MarionetteControllingAbility("marionette_controlling_ability"));
        abilities.add(new HistoricalVoidSummoningAbility("historical_void_summoning_ability"));
        abilities.add(new HistoricalVoidHidingAbility("historical_void_hiding_ability"));
        abilities.add(new MiracleCreationAbility("miracle_creation_ability"));

        // DARKNESS PATHWAY
        abilities.add(new MidnightPoemAbility("midnight_poem_ability"));
        abilities.add(new NightmareAbility("nightmare_ability"));
        abilities.add(new RequiemAbility("requiem_ability"));
        abilities.add(new SpiritCommandingAbility("spirit_commanding_ability"));
        abilities.add(new NightDomainAbility("night_domain_ability"));
        abilities.add(new HairEntanglementAbility("hair_entanglement_ability"));
        abilities.add(new HorrorAuraAbility("horror_aura_ability"));
        abilities.add(new SurgeOfDarknessAbility("surge_of_darkness_ability"));
        abilities.add(new ConcealmentAbility("concealment_ability"));

        // DEMONESS PATHWAY
        abilities.add(new ShadowConcealmentAbility("shadow_concealment_ability"));
        abilities.add(new MightyBlowAbility("mighty_blow_ability"));
        abilities.add(new InstigationAbility("instigation_ability"));
        abilities.add(new BlackFlameAbility("black_flame_ability"));
        abilities.add(new FrostAbility("frost_ability"));
        abilities.add(new InvisibilityAbility("invisibility_ability"));
        abilities.add(new MirrorSubstituteAbility("mirror_substitution_ability"));
        abilities.add(new ThreadManipulationAbility("thread_manipulation_ability"));
        abilities.add(new CharmAbility("charm_ability"));
        abilities.add(new DiseaseAbility("disease_ability"));
        abilities.add(new PlagueAbility("plague_ability"));
        abilities.add(new MirrorWorldTraversalAbility("mirror_world_traversal_ability"));
        abilities.add(new CurseAbility("curse_ability"));
        abilities.add(new PetrificationAbility("petrification_ability"));
        abilities.add(new DisasterManifestationAbility("disaster_manifestation_ability"));
        abilities.add(new StructuralCollapseAbility("structural_collapse_ability"));
        abilities.add(new ApocalypseAbility("apocalypse_ability"));

        // MOTHER PATHWAY
        abilities.add(new PlantNurturingAbility("plant_nurturing_ability"));
        abilities.add(new HealingAbility("healing_ability"));
        abilities.add(new CleansingAbility("cleanse_ability"));
        abilities.add(new PlantControllingAbility("plant_controlling_ability"));
        abilities.add(new PoisonCreationAbility("poison_creation_ability"));
        abilities.add(new CrossbreedingAbility("crossbreeding_ability"));
        abilities.add(new NatureSpellsAbility("nature_spells_ability"));
        abilities.add(new LifeAuraAbility("life_aura_ability"));
        abilities.add(new MutationCreationAbility("mutation_creation_ability"));
        abilities.add(new GolemCreationAbility("golem_creation_ability"));
        abilities.add(new LifeDeprivationAbility("life_deprivation_ability"));
        abilities.add(new MaternalEmbraceAbility("maternal_embrace_ability"));
        abilities.add(new AreaDesolationAbility("area_desolation_ability"));
        abilities.add(new BloomingAreaAbility("blooming_area_ability"));

        // DOOR PATHWAY
        abilities.add(new DoorOpeningAbility("door_opening_ability"));
        abilities.add(new SpellsAbility("spells_ability"));
        abilities.add(new RecordingAbility("recording_ability"));
        abilities.add(new BlinkAbility("blink_ability"));
        abilities.add(new TravelersDoorAbility("travelers_door_ability"));
        abilities.add(new SpaceConcealmentAbility("space_concealment_ability"));
        abilities.add(new ExileAbility("exile_ability"));
        abilities.add(new DoorSubstitutionAbility("door_substitution_ability"));
        abilities.add(new WanderingAbility("wandering_ability"));
        abilities.add(new ConceptualizationAbility("conceptualization_ability"));
        abilities.add(new SealingAbility("sealing_ability"));
        abilities.add(new SpaceTearingAbility("space_tearing_ability"));
        abilities.add(new WaypointAbility("waypoint_ability"));
        abilities.add(new ReplicatingAbility("replicating_ability"));
        abilities.add(new DistortionFieldAbility("distortion_field_ability"));
        abilities.add(new AreaMiniaturizationAbility("area_miniaturization_ability"));
        abilities.add(new SpaceDistortionAbility("space_distortion_ability"));
        abilities.add(new PocketDimensionAbility("pocket_dimension_ability"));
        abilities.add(new MythicalCreatureFormDoorAbility("mythical_creature_door_ability"));
        abilities.add(new SpaceTimeStormAbility("space_time_storm_ability"));
        abilities.add(new BlackHoleAbility("black_hole_ability"));
        abilities.add(new PlayerTeleportationAbility("player_teleportation_ability"));

        // VISIONARY PATHWAY
        abilities.add(new SpectatingAbility("spectating_ability"));
        abilities.add(new TelepathyAbility("telepathy_ability"));
        abilities.add(new FrenzyAbility("frenzy_ability"));
        abilities.add(new AweAbility("awe_ability"));
        abilities.add(new PlacateAbility("placate_ability"));
        abilities.add(new PsychologicalInvisibilityAbility("psychological_invisibility_ability"));
        abilities.add(new BattleHypnosisAbility("battle_hypnosis_ability"));
        abilities.add(new DragonScalesAbility("dragon_scales_ability"));
        abilities.add(new SleepInducementAbility("sleep_inducement_ability"));
        abilities.add(new DreamTraversalAbility("dream_traversal_ability"));
        abilities.add(new NightmareSpectatorAbility("nightmare_spectator_ability"));
        abilities.add(new ManipulationAbility("manipulation_ability"));
        abilities.add(new MentalPlagueAbility("mental_plague_ability"));
        abilities.add(new MindInvasionAbility("mind_invasion_ability"));
        abilities.add(new IdentityAvatarAbility("identity_avatar_ability"));
        abilities.add(new PsycheStormAbility("psyche_storm_ability"));

        // WHEEL OF FORTUNE PATHWAY
        abilities.add(new CalamityAttractionAbility("calamity_attraction_ability"));
        abilities.add(new LuckReleaseAbility("luck_release_ability"));
        abilities.add(new MisfortuneGiftingAbility("misfortune_gifting_ability"));
        abilities.add(new MisfortuneFieldAbility("misfortune_field_ability"));
        abilities.add(new BlessingAbility("blessing_ability"));
        abilities.add(new SpiritualBaptismAbility("spiritual_baptism_ability"));
        abilities.add(new WordsOfMisfortuneAbility("words_of_misfortune_ability"));
        abilities.add(new ProphecyAbility("prophecy_ability"));

        // ERROR PATHWAY
        abilities.add(new TheftAbility("theft_ability"));
        abilities.add(new MentalDisruptionAbility("mental_disruption_ability"));
        abilities.add(new DecryptionAbility("decryption_ability"));
        abilities.add(new GiftAbility("gift_ability"));
        abilities.add(new AbilityTheftAbility("ability_theft_ability"));
        abilities.add(new MundaneConceptualTheft("mundane_conceptual_theft_ability"));
        abilities.add(new ParasitationAbility("parasitation_ability"));
        abilities.add(new HostControllingAbility("host_controlling_ability"));
        abilities.add(new AvatarCreationAbility("avatar_creation_ability"));
        abilities.add(new DeceitAbility("deceit_ability"));
        abilities.add(new LoopHoleCreationAbility("loophole_creation_ability"));
        abilities.add(new FateSiphoningAbility("fate_siphoning_ability"));
        abilities.add(new ConceptualTheftAbility("conceptual_theft_ability"));
    }

    public HashSet<Ability> getAbilities() {
        return new HashSet<>(abilities);
    }

    public Ability getById(String id) {
        return abilities.stream().filter(ability -> ability.getId().equals(id)).findFirst().orElse(null);
    }

    public HashSet<Ability> getByPathwayAndSequenceExact(String pathway, int sequence) {
        return abilities
                .stream()
                .filter(ability -> ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) == sequence)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public HashSet<Ability> getByPathwayAndSequence(String pathway, int sequence) {
        return abilities
                .stream()
                .filter(ability -> ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) >= sequence)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public ArrayList<Ability> getByPathwayAndSequenceExactOrderedBySequence(String pathway, int sequence) {
        return new ArrayList<>(
                abilities.stream()
                        .filter(ability -> ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) == sequence)
                        .sorted(Comparator.comparing(ability -> ability.getRequirements().get(pathway)))
                        .toList()
        );
    }

    public ArrayList<Ability> getByPathwayAndSequenceOrderedBySequence(String pathway, int sequence) {
        return new ArrayList<>(
                abilities.stream()
                        .filter(ability -> ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) >= sequence)
                        .sorted(Comparator.comparing(ability -> ability.getRequirements().get(pathway)))
                        .toList()
        );
    }
}
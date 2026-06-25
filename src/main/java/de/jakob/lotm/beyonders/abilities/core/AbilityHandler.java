package de.jakob.lotm.beyonders.abilities.core;

import de.jakob.lotm.beyonders.abilities.abyss.*;
import de.jakob.lotm.beyonders.abilities.black_emperor.*;
import de.jakob.lotm.beyonders.abilities.darkness.*;
import de.jakob.lotm.beyonders.abilities.death.*;
import de.jakob.lotm.beyonders.abilities.demoness.*;
import de.jakob.lotm.beyonders.abilities.door.*;
import de.jakob.lotm.beyonders.abilities.error.*;
import de.jakob.lotm.beyonders.abilities.fool.*;
import de.jakob.lotm.beyonders.abilities.justiciar.*;
import de.jakob.lotm.beyonders.abilities.mother.*;
import de.jakob.lotm.beyonders.abilities.red_priest.*;
import de.jakob.lotm.beyonders.abilities.sefirah.AboveTheSequenceAuthorityAbility;
import de.jakob.lotm.beyonders.abilities.sefirah.SefirotAuthorityAbility;
import de.jakob.lotm.beyonders.abilities.sun.*;
import de.jakob.lotm.beyonders.abilities.twilight_giant.CombatMasteryAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.SupernaturalResistanceAbility;
import de.jakob.lotm.beyonders.abilities.tyrant.*;
import de.jakob.lotm.beyonders.abilities.visionary.*;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.*;
import de.jakob.lotm.beyonders.abilities.abyss.*;
import de.jakob.lotm.beyonders.abilities.common.*;
import de.jakob.lotm.beyonders.abilities.darkness.*;
import de.jakob.lotm.beyonders.abilities.death.*;
import de.jakob.lotm.beyonders.abilities.demoness.*;
import de.jakob.lotm.beyonders.abilities.door.*;
import de.jakob.lotm.beyonders.abilities.error.*;
import de.jakob.lotm.beyonders.abilities.fool.*;
import de.jakob.lotm.beyonders.abilities.justiciar.*;
import de.jakob.lotm.beyonders.abilities.mother.*;
import de.jakob.lotm.beyonders.abilities.red_priest.*;
import de.jakob.lotm.beyonders.abilities.sun.*;
import de.jakob.lotm.beyonders.abilities.tyrant.*;
import de.jakob.lotm.beyonders.abilities.visionary.*;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.*;

import java.util.*;
import java.util.stream.Collectors;

public class AbilityHandler {

    private final HashSet<Ability> abilities = new HashSet<>();
    private final HashSet<Ability> disabledAbilities = new HashSet<>();

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
        abilities.add(new AngelAuthorityAbility("angel_authority_ability"));
        abilities.add(new AngelFlightAbility("angel_authority_flight"));
        abilities.add(new MythicalCreatureFormAbility("mythical_creature_form_ability"));

        // SUN PATHWAY
        abilities.add(new HolySongAbility("holy_song_ability"));
        abilities.add(new EvilDetectionAbility("evil_detection_ability"));
        abilities.add(new SunSpellsAbility("sun_spells_ability"));
        abilities.add(new HolyLightAbility("holy_light_ability"));
        abilities.add(new FireOfLightAbility("fire_of_light_ability"));
        abilities.add(new CleaveOfPurificationAbility("cleave_of_purification_ability"));
        abilities.add(new SunHaloAbility("sun_halo_ability"));
        abilities.add(new HolyOathAbility("holy_oath_ability"));
        abilities.add(new HolyLightSummoningAbility("holy_light_summoning_ability"));
        abilities.add(new GodSaysItsEffectiveAbility("notary_buff_ability"));
        abilities.add(new GodSaysItsNotEffectiveAbility("notary_debuff_ability"));
        abilities.add(new LightOfHolinessAbility("light_of_holiness_ability"));
        abilities.add(new PurificationHaloAbility("purification_halo_ability"));
        abilities.add(new FlaringSunAbility("flaring_sun_ability"));
        abilities.add(new HolyEyeAbility("holy_eye_ability"));
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
        abilities.add(new SacrificeAbility("sacrifice_ability"));
        abilities.add(new FlightAbility("flight_ability"));
        abilities.add(new WeatherManipulationAbility("weather_manipulation_ability"));
        abilities.add(new ConqueringAbility("conquering_ability"));
        abilities.add(new FlameAuthorityAbility("flame_authority_ability"));
        abilities.add(new PuppetSoldierCreationAbility("puppet_soldier_creation_ability"));

        // TYRANT PATHWAY
        abilities.add(new IllusoryScalesAbility("illusory_scales_ability"));
        abilities.add(new WrathAbility("wrath_ability"));
        abilities.add(new RagingBlowsAbility("raging_blows_ability"));
        abilities.add(new WaterManipulationAbility("water_manipulation_ability"));
        abilities.add(new WindManipulationAbility("wind_manipulation_ability"));
        abilities.add(new BlessingsOfWIndAbility("blessings_of_wind_ability"));
        abilities.add(new LightningAbility("lightning_ability"));
        abilities.add(new SirenSongAbility("siren_song_ability"));
        abilities.add(new StrongLightningAbility("strong_lightning_ability"));
        abilities.add(new TsunamiAbility("tsunami_ability"));
        abilities.add(new EarthquakeAbility("earthquake_ability"));
        abilities.add(new HurricaneAbility("hurricane_ability"));
        abilities.add(new RoarAbility("roar_ability"));
        abilities.add(new WaterMasteryAbility("water_mastery_ability"));
        abilities.add(new TorrentialDownpourAbility("torrential_downpour_ability"));
        abilities.add(new LightningStormAbility("lightning_storm_ability"));
        abilities.add(new WrathOfTheOceanAbility("wrath_of_the_ocean_ability"));
        abilities.add(new ThunderclapAbility("thunderclap_ability"));
        abilities.add(new LightningBranchAbility("lightning_branch_ability"));
        abilities.add(new CalamityCreationAbility("calamity_creation_ability"));
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
        abilities.add(new DemonicSpellsAbility("demonic_spells_ability"));
        abilities.add(new MindFogAbility("mind_fog_ability"));
        abilities.add(new DesireControlAbility("desire_control_ability"));
        abilities.add(new CorruptingVoiceAbility("corrupting_voice_ability"));
        abilities.add(new MaliceSeedAbility("malice_seed_ability"));
        abilities.add(new FearAuraAbility("fear_aura_ability"));
        abilities.add(new FlamesOfTheAbyssAbility("flames_of_the_abyss_ability"));


        // FOOL PATHWAY
        abilities.add(new AirBulletAbility("air_bullet_ability"));
        abilities.add(new PaperEquipmentAbility("paper_equipment_ability"));
        abilities.add(new DamageTransferAbility("damage_transfer_ability"));
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
        abilities.add(new GraftingAbility("grafting_ability"));
        abilities.add(new FoolingAbility("fooling_ability"));

        // DARKNESS PATHWAY
        abilities.add(new MidnightPoemAbility("midnight_poem_ability"));
        abilities.add(new NightmareAbility("nightmare_ability"));
        abilities.add(new RequiemAbility("requiem_ability"));
        abilities.add(new SpiritCommandingAbility("spirit_commanding_ability"));
        abilities.add(new NightDomainAbility("night_domain_ability"));
        abilities.add(new HairEntanglementAbility("hair_entanglement_ability"));
        abilities.add(new LesserConcealmentAbility("lesser_concealment_ability"));
        abilities.add(new HorrorAuraAbility("horror_aura_ability"));
        abilities.add(new SurgeOfDarknessAbility("surge_of_darkness_ability"));
        abilities.add(new ConcealmentAbility("concealment_ability"));
        abilities.add(new IdentityConcealmentAbility("identity_concealment_ability"));
        abilities.add(new ConcealedDomainAbility("concealed_domain_ability"));
        abilities.add(new SwordOfDarknessAbility("sword_of_darkness_ability"));

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
        abilities.add(new WorldCreationAbility("world_creation_ability"));
        abilities.add(new WrathOfNatureAbility("wrath_of_nature_ability"));

        // DOOR PATHWAY
        abilities.add(new DoorOpeningAbility("door_opening_ability"));
        abilities.add(new SpellsAbility("spells_ability"));
        abilities.add(new RecordingAbility("recording_ability"));
        abilities.add(new BlinkAbility("blink_ability"));
        abilities.add(new TravelersDoorAbility("travelers_door_ability"));
        abilities.add(new InvisibleHandAbility("invisible_hand_ability"));
        abilities.add(new SpaceConcealmentAbility("space_concealment_ability"));
        abilities.add(new ExileAbility("exile_ability"));
        abilities.add(new DoorSubstitutionAbility("door_substitution_ability"));
        abilities.add(new WanderingAbility("wandering_ability"));
        abilities.add(new SymbolizationAbility("symbolization_ability"));
        abilities.add(new SealingAbility("sealing_ability"));
        abilities.add(new SpaceTearingAbility("space_tearing_ability"));
        abilities.add(new WaypointAbility("waypoint_ability"));
        abilities.add(new ReplicatingAbility("replicating_ability"));
        abilities.add(new DistortionFieldAbility("distortion_field_ability"));
        abilities.add(new AreaMiniaturizationAbility("area_miniaturization_ability"));
        abilities.add(new SpaceDistortionAbility("space_distortion_ability"));
        abilities.add(new PocketDimensionAbility("pocket_dimension_ability"));
        abilities.add(new BlackHoleAbility("black_hole_ability"));
        abilities.add(new PlayerTeleportationAbility("player_teleportation_ability"));
        abilities.add(new SpaceTimeStormAbility("space_time_storm_ability"));
        abilities.add(new SpaceFragmentationAbility("space_fragmentation_ability"));
        abilities.add(new ConceptualizationAbility("conceptualization_ability"));
        abilities.add(new DoorAuthorityAbility("door_authority_ability"));
        abilities.add(new SealingAuthorityAbility("sealing_authority_ability"));

        // VISIONARY PATHWAY
        abilities.add(new SpectatingAbility("spectating_ability"));
        abilities.add(new TelepathyAbility("telepathy_ability"));
        abilities.add(new PsychologicalCueAbility("psychological_cue_ability"));
        abilities.add(new FrenzyAbility("frenzy_ability"));
        abilities.add(new AweAbility("awe_ability"));
        abilities.add(new PlacateAbility("placate_ability"));
        abilities.add(new PsychologicalInvisibilityAbility("psychological_invisibility_ability"));
        abilities.add(new BattleHypnosisAbility("battle_hypnosis_ability"));
        abilities.add(new DragonScalesAbility("dragon_scales_ability"));
        abilities.add(new SleepInducementAbility("sleep_inducement_ability"));
        abilities.add(new DreamTraversalAbility("dream_traversal_ability"));
        abilities.add(new NightmareSpectatorAbility("nightmare_spectator_ability"));
        abilities.add(new VirtualPersonaAbility("virtual_persona_ability"));
        abilities.add(new ManipulationAbility("manipulation_ability"));
        abilities.add(new MentalPlagueAbility("mental_plague_ability"));
        abilities.add(new DreamWeaveAbility("dream_weave_ability"));
        abilities.add(new DreamMazeAbility("dream_maze_ability"));
        abilities.add(new PureIdealismAbility("pure_idealism_ability"));
        abilities.add(new DiscernmentAbility("discernment_ability"));
        abilities.add(new DisasterFantasiaAbility("disaster_fantasia_ability"));
        abilities.add(new StoryWritingAbility("story_writing_ability"));
        abilities.add(new EnvisionPositionAbility("envision_position_ability"));

        abilities.add(new MindWorldAuthorityAbility("mind_world_authority_ability"));
        abilities.add(new MindWorldAuthorityEnvisioningAbility("mind_world_authority_envisioning_ability"));

        // WHEEL OF FORTUNE PATHWAY
        abilities.add(new PsycheStormAbility("psyche_storm_ability"));
        abilities.add(new CalamityAttractionAbility("calamity_attraction_ability"));
        abilities.add(new LuckReleaseAbility("luck_release_ability"));
        abilities.add(new LuckPerceptionAbility("luck_perception_ability"));
        abilities.add(new MisfortuneGiftingAbility("misfortune_gifting_ability"));
        abilities.add(new MisfortuneFieldAbility("misfortune_field_ability"));
        abilities.add(new BlessingAbility("blessing_ability"));
        abilities.add(new SpiritualBaptismAbility("spiritual_baptism_ability"));
        abilities.add(new WordsOfMisfortuneAbility("words_of_misfortune_ability"));
        abilities.add(new ProphecyAbility("prophecy_ability"));
        abilities.add(new CycleOfFateAbility("cycle_of_fate_ability"));

        // JUSTICIAR PATHWAY
        abilities.add(new AuthorityAbility("authority_ability"));
        abilities.add(new RecognitionAbility("recognition_ability"));
        abilities.add(new JurisdictionAbility("jurisdiction_ability"));
        abilities.add(new EyeOfOrderAbility("eye_of_order_ability"));
        abilities.add(new IllusionaryTortureDevicesAbility("illusionary_torture_devices_ability"));
        abilities.add(new VerdictExileAbility("verdict_exile_ability"));
        abilities.add(new ProhibitionAbility("prohibition_ability"));
        abilities.add(new ImprisonAbility("imprison_ability"));
        abilities.add(new JusticeLanguageAbility("justice_language_ability"));
        abilities.add(new ConfinementAbility("confinement_ability"));
        abilities.add(new PunishmentAbility("punishment_ability"));
        abilities.add(new LawAbility("law_ability"));
        abilities.add(new ExecutionAbility("execution_ability"));
        abilities.add(new BalancingAbility("balancing_ability"));
        abilities.add(new SwordOfJudgmentAbility("sword_of_judgment_ability"));
        abilities.add(new DeliveringJudgmentAbility("delivering_judgment_ability"));
        abilities.add(new ExileOfBalanceAbility("exile_of_balance_ability"));
        abilities.add(new IndividualBalanceAbility("individual_balance_ability"));
        abilities.add(new OrderProxyAbility("order_proxy_ability"));
        abilities.add(new AncientCourtOfJudgmentAbility("ancient_court_of_judgment_ability"));
        abilities.add(new WorldJudgmentAbility("world_judgment_ability"));


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
        abilities.add(new TimeManipulationAbility("time_manipulation_ability"));

        // BLACK EMPEROR PATHWAY
//        abilities.add(new Eloquence("eloquence_ability"));
//        abilities.add(new MausoleumDomainAbility("mausoleum_domain_ability"));
//        abilities.add(new BriberAbility("briber_ability"));
//        abilities.add(new WeaknessDetectionAbility("weakness_detection_ability"));
//        abilities.add(new DistortionAbility("distortion_ability"));
//        abilities.add(new CorrosionAbility("corrosion_ability"));
//        abilities.add(new DisorderAbility("disorder_ability"));
//        abilities.add(new CommandingPresenceAbility("commanding_presence_ability"));
//        abilities.add(new ExploitAbility("exploit_ability"));
//        abilities.add(new BestowmentAbility("bestowment_ability"));
//        abilities.add(new MagnifyAbility("magnify_ability"));
//        abilities.add(new CommandingOrdersAbility("commanding_orders_ability"));

        // DEATH PATHWAY
        abilities.add(new EyeOfDeathAbility("eye_of_death_ability"));
        abilities.add(new SpiritCommunicationAbility("spirit_communication_ability"));
        abilities.add(new ZombieDisguiseAbility("zombie_disguise_ability"));
        abilities.add(new SpiritChannelingAbility("spirit_channeling_ability"));
        abilities.add(new RestructionAbility("restruction_ability"));
        abilities.add(new WordOfSpiritAbility("word_of_spirit_ability"));
        abilities.add(new DoorToTheUnderworldAbility("door_to_the_underworld_ability"));
        abilities.add(new InternalUnderworldAbility("internal_underworld_ability"));
        abilities.add(new DeathEnvoyAbility("death_envoy_ability"));
        abilities.add(new UndyingSealAbility("undying_seal_ability"));
        abilities.add(new SpiritWorldTraversalAbility("spirit_world_traversal_ability"));
        abilities.add(new DeathSpellsAbility("death_spells_ability"));
        abilities.add(new PaleEyeAbility("pale_eye_ability"));
        abilities.add(new HandOfDeathAbility("hand_of_death_ability"));
        abilities.add(new DeathFlameAbility("death_flame_ability"));
        abilities.add(new NationOfTheDeadAbility("nation_of_the_dead_ability"));
        abilities.add(new WordOfSpiritAbility("word_of_spirit_ability"));

        //Twilight Giant Pathway
        abilities.add(new CombatMasteryAbility("combat_mastery_ability"));
        abilities.add(new SupernaturalResistanceAbility("supernatural_resistance_ability"));

        // Sefirot Abilities
        abilities.add(new SefirotAuthorityAbility("sefirot_authority_ability"));
        abilities.add(new AboveTheSequenceAuthorityAbility("above_the_sequence_authority_ability"));













        //abilities.add(new DivineKingdomAbility("divine_kingdom_ability"));
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
                .filter(ability ->
                        ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) == sequence)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public ArrayList<Ability> getByPathwayAndSequenceExactOrdered(String pathway, int sequence) {
        return abilities
                .stream()
                .filter(ability ->
                        ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) == sequence)
                .sorted(Comparator.comparing(Ability::getId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public HashSet<Ability> getByPathwayAndSequence(String pathway, int sequence) {
        return abilities
                .stream()
                .filter(ability ->
                        ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) >= sequence)
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

    public Ability getRandomAbility(String pathway, int sequence, Random random, boolean exact, List<Ability> excluded) {
        List<Ability> pool;
        if (exact) {
            pool = new ArrayList<>(getByPathwayAndSequenceExact(pathway, sequence));
        } else {
            pool = new ArrayList<>(getByPathwayAndSequence(pathway, sequence));
        }

        // filter the pool to remove excluded abilities
        List<Ability> filteredPool = pool.stream()
                .filter(ability -> !excluded.contains(ability))
                .collect(Collectors.toList());

        if (filteredPool.isEmpty()) return null;
        return filteredPool.get(random.nextInt(filteredPool.size()));
    }

    public void disableAbility(Ability ability) {
        disabledAbilities.add(ability);
    }

    public void enableAbility(Ability ability) {
        disabledAbilities.remove(ability);
    }
    public boolean isDisabled(Ability ability) {
        return disabledAbilities.contains(ability);
    }

    public ArrayList<Ability> getByPathwayAndSequenceOrderedBySequence(String pathway, int sequence) {
        return new ArrayList<>(
                abilities.stream()
                        .filter(ability -> ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) >= sequence)
                        .sorted(Comparator.comparing(Ability::getId))
                        .sorted(Comparator.comparing(ability -> ability.getRequirements().get(pathway)))
                        .toList()
        );
    }

    public ArrayList<Ability> getAllAbilitiesUpToSequenceOrdered(int sequence) {
        return new ArrayList<>(
                abilities.stream()
                        .filter(ability -> ability.getRequirements().values().stream().anyMatch(reqSeq -> reqSeq >= sequence))
                        .sorted(Comparator.comparing(Ability::getId))
                        .sorted(Comparator.comparingInt(Ability::lowestSequenceUsable).reversed())
                        .toList()
        );
    }
}
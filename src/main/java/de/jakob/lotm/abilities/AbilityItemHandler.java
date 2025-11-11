package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.abyss.*;
import de.jakob.lotm.abilities.common.CogitationAbility;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.common.SpiritVisionAbility;
import de.jakob.lotm.abilities.darkness.MidnightPoemAbility;
import de.jakob.lotm.abilities.darkness.NightmareAbility;
import de.jakob.lotm.abilities.darkness.RequiemAbility;
import de.jakob.lotm.abilities.demoness.*;
import de.jakob.lotm.abilities.door.*;
import de.jakob.lotm.abilities.fool.*;
import de.jakob.lotm.abilities.mother.CleansingAbility;
import de.jakob.lotm.abilities.mother.HealingAbility;
import de.jakob.lotm.abilities.mother.PlantControllingAbility;
import de.jakob.lotm.abilities.mother.PlantNurturingAbility;
import de.jakob.lotm.abilities.red_priest.*;
import de.jakob.lotm.abilities.sun.*;
import de.jakob.lotm.abilities.tyrant.*;
import de.jakob.lotm.abilities.visionary.*;
import de.jakob.lotm.abilities.wheel_of_fortune.CalamityAttractionAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.LuckReleaseAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.MisfortuneGiftingAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.PsycheStormAbility;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AbilityItemHandler {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);
    public static final DeferredItem<Item> COGITATION = ITEMS.registerItem("cogitation_ability", CogitationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> DIVINATION = ITEMS.registerItem("divination_ability", DivinationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> SPIRIT_VISION = ITEMS.registerItem("spirit_vision_ability", SpiritVisionAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static final DeferredItem<Item> HOLY_SONG = ITEMS.registerItem("holy_song_ability", HolySongAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ILLUMINATE = ITEMS.registerItem("illuminate_ability", IlluminateAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HOLY_LIGHT = ITEMS.registerItem("holy_light_ability",  HolyLightAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FIRE_OF_LIGHT = ITEMS.registerItem("fire_of_light_ability",  FireOfLightAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HOLY_LIGHT_SUMMONING = ITEMS.registerItem("holy_light_summoning_ability",  HolyLightSummoningAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CLEAVE_OF_PURIFICATION = ITEMS.registerItem("cleave_of_purification_ability",  CleaveOfPurificationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HOLY_OATH = ITEMS.registerItem("holy_oath_ability",  HolyOathAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> NOTARY_BUFF = ITEMS.registerItem("notary_buff_ability",  GodSaysItsEffectiveAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> NOTARY_DEBUFF = ITEMS.registerItem("notary_debuff_ability",  GodSaysItsNotEffectiveAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> LIGHT_OF_HOLINESS = ITEMS.registerItem("light_of_holiness_ability",  LightOfHolinessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PURIFICATION_HALO = ITEMS.registerItem("purification_halo_ability",  PurificationHaloAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FLARING_SUN = ITEMS.registerItem("flaring_sun_ability",  FlaringSunAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> UNSHADOWED_SPEAR = ITEMS.registerItem("unshadowed_spear_ability",  UnshadowedSpearAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> UNSHADOWED_DOMAIN = ITEMS.registerItem("unshadowed_domain_ability",  UnshadowedDomainAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WALL_OF_LIGHT = ITEMS.registerItem("wall_of_light_ability",  WallOfLightAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SWORD_OF_JUSTICE = ITEMS.registerItem("sword_of_justice_ability",  SwordOfJusticeAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SPEAR_OF_LIGHT = ITEMS.registerItem("spear_of_light_ability",  SpearOfLightAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SOLAR_ENVOY = ITEMS.registerItem("solar_envoy_ability",  SolarEnvoyAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WINGS_OF_LIGHT = ITEMS.registerItem("wings_of_light_ability",  WingsOfLightAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PURE_WHITE_LIGHT = ITEMS.registerItem("pure_white_light_ability",  PureWhiteLightAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> DIVINE_KINGDOM_MANIFESTATION = ITEMS.registerItem("divine_kingdom_manifestation_ability",  DivineKingdomManifestationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> TRAP = ITEMS.registerItem("trap_ability", TrapAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PROVOKING = ITEMS.registerItem("provoking_ability", ProvokingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PYROKINESIS = ITEMS.registerItem("pyrokinesis_ability", PyrokinesisAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CULL = ITEMS.registerItem("cull_ability", CullAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FLAME_MASTERY = ITEMS.registerItem("flame_mastery_ability", FlameMasteryAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> STEEL_MASTERY = ITEMS.registerItem("steel_mastery_ability", SteelMasteryAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CHAIN_OF_COMMAND = ITEMS.registerItem("chain_of_command_ability", ChainOfCommandAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WAR_CRY = ITEMS.registerItem("war_cry_ability", WarCryAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WAR_SONG = ITEMS.registerItem("war_song_ability", WarSongAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FOG_OF_WAR = ITEMS.registerItem("fog_of_war_ability", FogOfWarAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> ESSENCE_OF_WAR = ITEMS.registerItem("essence_of_war_ability", EssenceOfWarAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FLIGHT = ITEMS.registerItem("flight_ability", FlightAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WEATHER_MANIPULATION = ITEMS.registerItem("weather_manipulation_ability", WeatherManipulationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> RAGING_BLOWS = ITEMS.registerItem("raging_blows_ability", RagingBlowsAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WATER_MANIPULATION = ITEMS.registerItem("water_manipulation_ability", WaterManipulationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WIND_MANIPULATION = ITEMS.registerItem("wind_manipulation_ability", WindManipulationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> LIGHTNING = ITEMS.registerItem("lightning_ability", LightningAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SIREN_SONG = ITEMS.registerItem("siren_song_ability", SirenSongAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> TSUNAMI = ITEMS.registerItem("tsunami_ability", TsunamiAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> EARTHQUAKE = ITEMS.registerItem("earthquake_ability", EarthquakeAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HURRICANE = ITEMS.registerItem("hurricane_ability", HurricaneAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> ROAR = ITEMS.registerItem("roar_ability", RoarAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WATER_MASTERY = ITEMS.registerItem("water_mastery_ability", WaterMasteryAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> TORRENTIAL_DOWNPOUR = ITEMS.registerItem("torrential_downpour_ability", TorrentialDownpourAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> LIGHTNING_STORM = ITEMS.registerItem("lightning_storm_ability", LightningStormAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> THUNDERCLAP = ITEMS.registerItem("thunderclap_ability", ThunderclapAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> LIGHTNING_BRANCH = ITEMS.registerItem("lightning_branch_ability", LightningBranchAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CALAMITY_CREATION = ITEMS.registerItem("calamity_creation_ability", CalamityCreationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> ENERGY_TRANSFORMATION = ITEMS.registerItem("energy_transformation_ability", EnergyTransformationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HEAVENLY_PUNISHMENT = ITEMS.registerItem("heavenly_punishment_ability", HeavenlyPunishmentAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> ELECTROMAGNETIC_TORNADO = ITEMS.registerItem("electromagnetic_tornado_ability", ElectromagneticTornadoAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> ROAR_OF_THE_THUNDER_GOD = ITEMS.registerItem("roar_of_the_thunder_god_ability", RoarOfTheThunderGodAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> TOXIC_SMOKE = ITEMS.registerItem("toxic_smoke_ability", ToxicSmokeAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> POISONOUS_FLAME = ITEMS.registerItem("poisonous_flame_ability", PoisonousFlameAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FLAME_SPELLS = ITEMS.registerItem("flame_spells_ability", FlameSpellsAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> LANGUAGE_OF_FOULNESS = ITEMS.registerItem("language_of_foulness_ability", LanguageOfFoulnessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> DEVIL_TRANSFORMATION = ITEMS.registerItem("devil_transformation_ability", DevilTransformationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> AVATAR_OF_DESIRE = ITEMS.registerItem("avatar_of_desire_ability", AvatarOfDesireAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> DEFILING_SEED = ITEMS.registerItem("defiling_seed_ability", DefilingSeedAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> AIR_BULLET = ITEMS.registerItem("air_bullet_ability", AirBulletAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FLAME_CONTROLLING = ITEMS.registerItem("flame_controlling_ability", FlameControllingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PAPER_FIGURINE_SUBSTITUTE = ITEMS.registerItem("paper_figurine_substitute_ability", PaperFigurineSubstituteAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FLAMING_JUMP = ITEMS.registerItem("flaming_jump_ability", FlamingJumpAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> UNDERWATER_BREATHING = ITEMS.registerItem("underwater_breathing_ability", UnderWaterBreathingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SHAPESHIFTING = ITEMS.registerItem("shapeshifting_ability", ShapeShiftingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PUPPETEERING = ITEMS.registerItem("puppeteering_ability", PuppeteeringAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> MARIONETTE_CONTROLLING = ITEMS.registerItem("marionette_controlling_ability", MarionetteControllingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HISTORICAL_VOID_SUMMONING = ITEMS.registerItem("historical_void_summoning_ability", HistoricalVoidSummoningAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HISTORICAL_VOID_HIDING = ITEMS.registerItem("historical_void_hiding_ability", HistoricalVoidHidingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MIDNIGHT_POEM = ITEMS.registerItem("midnight_poem_ability", MidnightPoemAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> NIGHTMARE = ITEMS.registerItem("nightmare_ability", NightmareAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> REQUIEM = ITEMS.registerItem("requiem_ability", RequiemAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SHADOW_CONCEALMENT = ITEMS.registerItem("shadow_concealment_ability", ShadowConcealmentAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> MIGHTY_BLOW = ITEMS.registerItem("mighty_blow_ability", MightyBlowAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> INSTIGATION = ITEMS.registerItem("instigation_ability", InstigationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> BLACK_FLAME = ITEMS.registerItem("black_flame_ability", BlackFlameAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FROST = ITEMS.registerItem("frost_ability", FrostAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> INVISIBILITY = ITEMS.registerItem("invisibility_ability", InvisibilityAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> MIRROR_SUBSTITUTION = ITEMS.registerItem("mirror_substitution_ability", MirrorSubstituteAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> THREAD_MANIPULATION = ITEMS.registerItem("thread_manipulation_ability", ThreadManipulationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CHARM = ITEMS.registerItem("charm_ability", CharmAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> DISEASE = ITEMS.registerItem("disease_ability", DiseaseAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PLAGUE = ITEMS.registerItem("plague_ability", PlagueAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> MIRROR_WORLD_TRAVERSAL = ITEMS.registerItem("mirror_world_traversal_ability", MirrorWorldTraversalAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CURSE = ITEMS.registerItem("curse_ability", CurseAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PETRIFICATION = ITEMS.registerItem("petrification_ability", PetrificationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PLANT_NURTURING = ITEMS.registerItem("plant_nurturing_ability", PlantNurturingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HEALING = ITEMS.registerItem("healing_ability", HealingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CLEANSING = ITEMS.registerItem("cleanse_ability", CleansingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PLANT_CONTROLLING = ITEMS.registerItem("plant_controlling_ability", PlantControllingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DOOR_OPENING = ITEMS.registerItem("door_opening_ability", DoorOpeningAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SPELLS = ITEMS.registerItem("spells_ability", SpellsAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> RECORDING = ITEMS.registerItem("recording_ability", RecordingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> BLINK = ITEMS.registerItem("blink_ability", BlinkAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> TRAVELERS_DOOR = ITEMS.registerItem("travelers_door_ability", TravelersDoorAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SPACE_CONCEALMENT = ITEMS.registerItem("space_concealment_ability", SpaceConcealmentAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> EXILE = ITEMS.registerItem("exile_ability", ExileAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> DOOR_SUBSTITUTION = ITEMS.registerItem("door_substitution_ability", DoorSubstitutionAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WANDERING = ITEMS.registerItem("wandering_ability", WanderingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CONCEPTUALIZATION = ITEMS.registerItem("conceptualization_ability", ConceptualizationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SEALING = ITEMS.registerItem("sealing_ability", SealingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SPACE_TEARING = ITEMS.registerItem("space_tearing_ability", SpaceTearingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> BLACK_HOLE = ITEMS.registerItem("black_hole_ability", BlackHoleAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> REPLICATING = ITEMS.registerItem("replicating_ability", ReplicatingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> DISTORTION_FIELD = ITEMS.registerItem("distortion_field_ability", DistortionFieldAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> AREA_MINIATURIZATION = ITEMS.registerItem("area_miniaturization_ability", AreaMiniaturizationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SPECTATING = ITEMS.registerItem("spectating_ability", SpectatingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> TELEPATHY = ITEMS.registerItem("telepathy_ability", TelepathyAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FRENZY = ITEMS.registerItem("frenzy_ability", FrenzyAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> AWE = ITEMS.registerItem("awe_ability", AweAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PLACATE = ITEMS.registerItem("placate_ability", PlacateAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PSYCHOLOGICAL_INVISIBILITY = ITEMS.registerItem("psychological_invisibility_ability", PsychologicalInvisibilityAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> BATTLE_HYPNOSIS = ITEMS.registerItem("battle_hypnosis_ability", BattleHypnosisAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> DRAGON_SCALES = ITEMS.registerItem("dragon_scales_ability", DragonScalesAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SLEEP_INDUCEMENT = ITEMS.registerItem("sleep_inducement_ability", SleepInducementAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> DREAM_TRAVERSAL = ITEMS.registerItem("dream_traversal_ability", DreamTraversalAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> NIGHTMARE_SPECTATOR = ITEMS.registerItem("nightmare_spectator_ability", NightmareSpectatorAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PSYCHE_STORM = ITEMS.registerItem("psyche_storm_ability", PsycheStormAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CALAMITY_ATTRACTION = ITEMS.registerItem("calamity_attraction_ability", CalamityAttractionAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> LUCK_ACCUMULATION = ITEMS.registerItem("luck_release_ability", LuckReleaseAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> MISFORTUNE_GIFTING = ITEMS.registerItem("misfortune_gifting_ability", MisfortuneGiftingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> ABILITY_NOT_IMPLEMENTED = ITEMS.registerItem("ability_not_implemented", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static void registerAbilities(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

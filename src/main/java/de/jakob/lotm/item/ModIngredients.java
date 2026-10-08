package de.jakob.lotm.item;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;

public class ModIngredients {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);

    public static final DeferredItem<Item> LAVOS_SQUID_BLOOD = ITEMS.registerItem("lavos_squid_blood", (properties) -> new PotionIngredient(properties, 9, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> HORNACIS_GRAY_MOUNTAIN_GOAT_HORN = ITEMS.registerItem("hornacis_gray_mountain_goat_horn", (properties) -> new PotionIngredient(properties, 8, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> ROOT_OF_MIST_TREANT = ITEMS.registerItem("true_root_of_mist_treant", (properties) -> new PotionIngredient(properties, 7, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> THOUSAND_FACED_HUNTER_BLOOD = ITEMS.registerItem("thousand_faced_hunter_blood", (properties) -> new PotionIngredient(properties, 6, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_WRAITH_DUST = ITEMS.registerItem("ancient_wraith_dust", (properties) -> new PotionIngredient(properties, 5, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> BIZARRO_BANE_EYE = ITEMS.registerItem("bizarro_bane_eye", (properties) -> new PotionIngredient(properties, 4, true, "fool"), new Item.Properties());


    public static final DeferredItem<Item> CRYSTAL_SUNFLOWER = ITEMS.registerItem("crystal_sunflower", (properties) -> new PotionIngredient(properties, 9, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> POWDER_OF_DAZZLING_SOUL = ITEMS.registerItem("powder_of_dazzling_soul", (properties) -> new PotionIngredient(properties, 8, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_PACT_TREE_FRUIT = ITEMS.registerItem("spirit_pact_tree_fruit", (properties) -> new PotionIngredient(properties, 7, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> CRYSTALLIZED_ROOTS = ITEMS.registerItem("crystallized_roots", (properties) -> new PotionIngredient(properties, 6, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> PURE_WHITE_BRILLIANT_ROCK = ITEMS.registerItem("pure_white_brilliant_rock", (properties) -> new PotionIngredient(properties, 5, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> GOLDEN_BLOOD = ITEMS.registerItem("golden_blood", (properties) -> new PotionIngredient(properties, 4, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> SUN_ORB = ITEMS.registerItem("sun_orb", (properties) -> new PotionIngredient(properties, 3, true, "sun"), new Item.Properties());


    public static final DeferredItem<Item> ILLUSION_CRYSTAL = ITEMS.registerItem("illusion_crystal", (properties) -> new PotionIngredient(properties, 9, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_EATER_STOMACH_POUCH = ITEMS.registerItem("spirit_eater_stomach_pouch", (properties) -> new PotionIngredient(properties, 8, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> METEORITE_CRYSTAL = ITEMS.registerItem("meteorite_crystal", (properties) -> new PotionIngredient(properties, 7, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_WRAITH_ARTIFACT = ITEMS.registerItem("ancient_wraith_artifact", (properties) -> new PotionIngredient(properties, 6, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> SHADOWLESS_DEMONIC_WOLF_HEART = ITEMS.registerItem("shadowless_demonic_wolf_heart", (properties) -> new PotionIngredient(properties, 5, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> GOLDEN_PHOENIX_EYE = ITEMS.registerItem("golden_phoenix_eyes", (properties) -> new PotionIngredient(properties, 4, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> MIST_WATCHER_CRYSTAL = ITEMS.registerItem("mist_watcher_crystal", (properties) -> new PotionIngredient(properties, 3, true, "door"), new Item.Properties());


    public static final DeferredItem<Item> MURLOC_BLADDER = ITEMS.registerItem("murloc_bladder", (properties) -> new PotionIngredient(properties, 9, true, "tyrant"), new Item.Properties());
    public static final DeferredItem<Item> DRAGON_EYED_CONDOR_EYEBALL = ITEMS.registerItem("dragon_eyed_condor_eyeball", (properties) -> new PotionIngredient(properties, 8, true,"tyrant"), new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_LOGBOOK = ITEMS.registerItem("ancient_logbook", (properties) -> new PotionIngredient(properties, 7, true, "tyrant"), new Item.Properties());
    public static final DeferredItem<Item> BLUE_SHADOW_FALCON_FEATHERS = ITEMS.registerItem("blue_shadow_falcon_feathers", (properties) -> new PotionIngredient(properties, 6, true, "tyrant"), new Item.Properties());
    public static final DeferredItem<Item> SIREN_VOCAL_SAC = ITEMS.registerItem("siren_vocal_sac", (properties) -> new PotionIngredient(properties, 5, true, "tyrant"), new Item.Properties());
    public static final DeferredItem<Item> WHALE_OF_PUNISHMENT_STOMACH = ITEMS.registerItem("whale_of_punishment_stomach", (properties) -> new PotionIngredient(properties, 4, true, "tyrant"), new Item.Properties());
    public static final DeferredItem<Item> KING_OF_GREEN_WINGS_EYE = ITEMS.registerItem("king_of_green_wings_eye", (properties) -> new PotionIngredient(properties, 3, true, "tyrant"), new Item.Properties());


    public static final DeferredItem<Item> MIDNIGHT_BEAUTY_FLOWER = ITEMS.registerItem("midnight_beauty_flower", (properties) -> new PotionIngredient(properties, 9, true, "darkness"), new Item.Properties());
    public static final DeferredItem<Item> SOUL_SNARING_BELL_FLOWER = ITEMS.registerItem("soul_snaring_bell_flower", (properties) -> new PotionIngredient(properties, 8, true,"darkness"), new Item.Properties());
    public static final DeferredItem<Item> DREAM_EATING_RAVEN_HEART = ITEMS.registerItem("dream_eating_raven_heart", (properties) -> new PotionIngredient(properties, 7, true, "darkness"), new Item.Properties());
    public static final DeferredItem<Item> DEEP_SLEEPER_SKULL = ITEMS.registerItem("deep_sleeper_skull", (properties) -> new PotionIngredient(properties, 6, true, "darkness"), new Item.Properties());
    public static final DeferredItem<Item> SOURCE_OF_MAD_DREAMS = ITEMS.registerItem("source_of_mad_dreams", (properties) -> new PotionIngredient(properties, 5, true, "darkness"), new Item.Properties());


    public static final DeferredItem<Item> RED_CHESTNUT_FLOWER = ITEMS.registerItem("red_chestnut_flower", (properties) -> new PotionIngredient(properties, 9, true, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> REDCROWN_BALSAM_POWDER = ITEMS.registerItem("redcrown_balsam_powder", (properties) -> new PotionIngredient(properties, 8, true,"red_priest"), new Item.Properties());
    public static final DeferredItem<Item> MAGMA_ELF_CORE = ITEMS.registerItem("magma_elf_core", (properties) -> new PotionIngredient(properties, 7, true, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> SPHINX_BRAIN = ITEMS.registerItem("sphinx_brain", (properties) -> new PotionIngredient(properties, 6, true, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> BLACK_HUNTING_SPIDER_COMPOSITE_EYES = ITEMS.registerItem("black_hunting_spider_composite_eyes", (properties) -> new PotionIngredient(properties, 5, true, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> MAGMA_GIANT_CORE = ITEMS.registerItem("magma_giant_core", (properties) -> new PotionIngredient(properties, 4, true, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> WAR_COMET_CORE = ITEMS.registerItem("war_comet_core", (properties) -> new PotionIngredient(properties, 3, true, "red_priest"), new Item.Properties());

    public static final DeferredItem<Item> GOAT_HORNED_BLACKFISH_BLOOD = ITEMS.registerItem("goat_horned_blackfish_blood", (properties) -> new PotionIngredient(properties, 9, true, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> RAINBOW_SALAMANDER_PITUITARY_GLAND = ITEMS.registerItem("rainbow_salamander_pituitary_gland", (properties) -> new PotionIngredient(properties, 8, true,"visionary"), new Item.Properties());
    public static final DeferredItem<Item> TREE_OF_ELDERS_FRUIT = ITEMS.registerItem("tree_of_elders_fruit", (properties) -> new PotionIngredient(properties, 7, true, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> ILLUSORY_CHIME_TREES_FRUIT = ITEMS.registerItem("illusory_chime_tree_fruit", (properties) -> new PotionIngredient(properties, 6, true, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> DREAM_CATCHERS_HEART = ITEMS.registerItem("dream_catcher_heart", (properties) -> new PotionIngredient(properties, 5, true, "visionary"), new Item.Properties());

    public static final DeferredItem<Item> BLACK_FEATHER_OF_MONSTER_BIRD = ITEMS.registerItem("black_feather_of_monster_bird", (properties) -> new PotionIngredient(properties, 9, true, "demoness"), new Item.Properties());
    public static final DeferredItem<Item> ABYSS_DEMONIC_FISH_BLOOD = ITEMS.registerItem("abyss_demonic_fish_blood", (properties) -> new PotionIngredient(properties, 8, true,"demoness"), new Item.Properties());
    public static final DeferredItem<Item> AGATE_PEACOCK_EGG = ITEMS.registerItem("agate_peacock_egg", (properties) -> new PotionIngredient(properties, 7, true, "demoness"), new Item.Properties());
    public static final DeferredItem<Item> SUCCUBUS_EYES = ITEMS.registerItem("succubus_eyes", (properties) -> new PotionIngredient(properties, 6, true, "demoness"), new Item.Properties());
    public static final DeferredItem<Item> SHADOW_LIZARD_SCALES = ITEMS.registerItem("shadow_lizard_scales", (properties) -> new PotionIngredient(properties, 5, true, "demoness"), new Item.Properties());

    public static final DeferredItem<Item> SILVER_FOUR_LEAF_CLOVER = ITEMS.registerItem("silver_four_leaf_clover", (properties) -> new PotionIngredient(properties, 9, true, "wheel_of_fortune"), new Item.Properties());
    public static final DeferredItem<Item> CRYSTAL_OF_A_YOUNG_UNICORN = ITEMS.registerItem("crystal_of_a_young_unicorn", (properties) -> new PotionIngredient(properties, 8, true, "wheel_of_fortune"), new Item.Properties());
    public static final DeferredItem<Item> DIVINE_BLESSED_CRYSTAL = ITEMS.registerItem("divine_blessed_crystal", (properties) -> new PotionIngredient(properties, 7, true, "wheel_of_fortune"), new Item.Properties());
    public static final DeferredItem<Item> CRYSTAL_CORE_OF_THE_CALAMITY_PHOENIX = ITEMS.registerItem("crystal_core_of_the_calamity_phoenix", (properties) -> new PotionIngredient(properties, 6, true, "wheel_of_fortune"), new Item.Properties());
    public static final DeferredItem<Item> HEART_OF_A_BLUE_SPOTTED_FIREBIRD = ITEMS.registerItem("heart_of_a_blue_spotted_firebird", (properties) -> new PotionIngredient(properties, 5, true, "wheel_of_fortune"), new Item.Properties());
    public static final DeferredItem<Item> FLOWER_OF_GOOD_FORTUNE = ITEMS.registerItem("flower_of_good_fortune", (properties) -> new PotionIngredient(properties, 4, true, "wheel_of_fortune"), new Item.Properties());
    public static final DeferredItem<Item> BRAIN_OF_A_FOUR_EARED_GIANT_APE = ITEMS.registerItem("brain_of_a_four_eared_giant_ape", (properties) -> new PotionIngredient(properties, 3, true, "wheel_of_fortune"), new Item.Properties());


    public static final DeferredItem<Item> CLOTH_WRAPPED_PERSONS_CRYSTAL = ITEMS.registerItem("cloth_wrapped_persons_crystal", (properties) -> new PotionIngredient(properties, 9, true, "death"), new Item.Properties());
    public static final DeferredItem<Item> DEATH_CALLING_CROWS_EYEBALL = ITEMS.registerItem("death_calling_crows_eyeball", (properties) -> new PotionIngredient(properties, 8, true, "death"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_WORLD_CRYSTAL = ITEMS.registerItem("spirit_world_crystal", (properties) -> new PotionIngredient(properties, 7, true, "death"), new Item.Properties());
    public static final DeferredItem<Item> SOUL_OF_A_PALE_LICH = ITEMS.registerItem("soul_of_a_pale_lich", (properties) -> new PotionIngredient(properties, 6, true, "death"), new Item.Properties());
    public static final DeferredItem<Item> CRYSTAL_CORE_OF_THOUSAND_ARMED_WRAITH = ITEMS.registerItem("crystal_core_of_thousand_armed_wraith", (properties) -> new PotionIngredient(properties, 5, true, "death"), new Item.Properties());
    public static final DeferredItem<Item> BRAIN_OF_AN_ADULT_FEATHERED_SERPENT = ITEMS.registerItem("brain_of_an_adult_feathered_serpent", (properties) -> new PotionIngredient(properties, 4, true, "death"), new Item.Properties());
    public static final DeferredItem<Item> MOLT_OF_THE_IMMORTAL_CICADA = ITEMS.registerItem("molt_of_the_immortal_cicada", (properties) -> new PotionIngredient(properties, 3, true, "death"), new Item.Properties());


    public static final DeferredItem<Item> STAR_CRYSTAL = ITEMS.registerItem("star_crystal", (properties) -> new PotionIngredient(properties, 9, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> NIGHT_VANILLA_LIQUIDS = ITEMS.registerItem("night_vanilla_liquids", (properties) -> new PotionIngredient(properties, 9, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> GOLD_MINT_LEAVES = ITEMS.registerItem("gold_mint_leaves", (properties) -> new PotionIngredient(properties, 9, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> POISON_HEMLOCK = ITEMS.registerItem("poison_hemlock", (properties) -> new PotionIngredient(properties, 9, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> DRAGON_BLOOD_GRASS_POWDER = ITEMS.registerItem("dragon_blood_grass_powder", (properties) -> new PotionIngredient(properties, 9, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> HUMAN_FACED_ROSE_COMPLETE_STALK = ITEMS.registerItem("human_faced_rose_complete_stalk", (properties) -> new PotionIngredient(properties, 8, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> PURIFIED_WATER = ITEMS.registerItem("purified_water", (properties) -> new PotionIngredient(properties, 9, false, "fool", "door", "visionary", "demoness"), new Item.Properties());
    public static final DeferredItem<Item> TORNAPPLE_JIMSONWEED_JUICE = ITEMS.registerItem("tornapple_jimsonweed_juice", (properties) -> new PotionIngredient(properties, 8, false, "fool", "demoness"), new Item.Properties());
    public static final DeferredItem<Item> BLACK_RIMMED_SUNFLOWER_POWDER = ITEMS.registerItem("black_rimmed_sunflower_powder", (properties) -> new PotionIngredient(properties, 8, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> GOLDEN_CLOAK_GRASS_POWDER = ITEMS.registerItem("golden_cloak_grass_powder", (properties) -> new PotionIngredient(properties, 8, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> MIST_TREANT_TRUE_ROOT = ITEMS.registerItem("mist_treant_true_root", (properties) -> new PotionIngredient(properties, 7, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> DARK_PATTERNED_BLACK_PANTHER_SPINAL_FLUID = ITEMS.registerItem("dark_patterned_black_panther_spinal_fluid", (properties) -> new PotionIngredient(properties, 7, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> MIST_TREANT_JUICE = ITEMS.registerItem("mist_treant_juice", (properties) -> new PotionIngredient(properties, 7, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> DROPLET_GEM_POWDER = ITEMS.registerItem("droplet_gem_powder", (properties) -> new PotionIngredient(properties, 7, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> FANTASY_GRASS_ESSENTIAL_OIL = ITEMS.registerItem("fantasy_grass_essential_oil", (properties) -> new PotionIngredient(properties, 7, false, "fool", "black_emperor"), new Item.Properties());
    public static final DeferredItem<Item> MUTATED_PITUITARY_GLAND_OF_A_THOUSAND_FACED_HUNTER = ITEMS.registerItem("mutated_pituitary_gland_of_a_thousand_faced_hunter", (properties) -> new PotionIngredient(properties, 6, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> HUMAN_SKINED_SHADOW_CHARACTERISTIC = ITEMS.registerItem("human_skined_shadow_characteristic", (properties) -> new PotionIngredient(properties, 6, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> BLACK_JIMSONWEED_JUICE = ITEMS.registerItem("black_jimsonweed_juice", (properties) -> new PotionIngredient(properties, 6, false, "fool", "black_emperor", "demoness"), new Item.Properties());
    public static final DeferredItem<Item> DRAGON_TOOTH_GRASS_POWDER = ITEMS.registerItem("dragon_tooth_grass_powder", (properties) -> new PotionIngredient(properties, 6, false, "fool", "visionary"), new Item.Properties());
    public static final DeferredItem<Item> DEEP_SEA_NAGA_HAIR_STRAND = ITEMS.registerItem("deep_sea_naga_hair_strand", (properties) -> new PotionIngredient(properties, 6, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> DUST_OF_ANCIENT_WRAITHS = ITEMS.registerItem("dust_of_ancient_wraiths", (properties) -> new PotionIngredient(properties, 5, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> SIX_WINGED_GARGOYLE_CORE_CRYSTAL = ITEMS.registerItem("six_winged_gargoyle_core_crystal", (properties) -> new PotionIngredient(properties, 5, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> SPRING_WATER_OF_GOLDEN_SPRING = ITEMS.registerItem("spring_water_of_golden_spring", (properties) -> new PotionIngredient(properties, 5, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> DRAGO_BARK = ITEMS.registerItem("drago_bark", (properties) -> new PotionIngredient(properties, 5, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> REMNANT_SPIRITUALITY_OF_ANCIENT_WRAITHS = ITEMS.registerItem("remnant_spirituality_of_ancient_wraiths", (properties) -> new PotionIngredient(properties, 5, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> SIX_WINGED_GARGOYLE_EYES = ITEMS.registerItem("six_winged_gargoyle_eyes", (properties) -> new PotionIngredient(properties, 5, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> BIZARRO_BANE_MAIN_EYE = ITEMS.registerItem("bizarro_bane_main_eye", (properties) -> new PotionIngredient(properties, 4, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_WORLD_PLUNDERER_TRUE_SOUL = ITEMS.registerItem("spirit_world_plunderer_true_soul", (properties) -> new PotionIngredient(properties, 4, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> BIZARRO_BANE_BLOOD = ITEMS.registerItem("bizarro_bane_blood", (properties) -> new PotionIngredient(properties, 4, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_WORLD_PLUNDERER_DUST = ITEMS.registerItem("spirit_world_plunderer_dust", (properties) -> new PotionIngredient(properties, 4, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> RED_HAIR_BIRCH_BARK = ITEMS.registerItem("red_hair_birch_bark", (properties) -> new PotionIngredient(properties, 4, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> GOLDEN_GRAPEVINES = ITEMS.registerItem("golden_grapevines", (properties) -> new PotionIngredient(properties, 4, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> FINGERNAIL_SIZED_SELF_MADE_RUBBER_MASK = ITEMS.registerItem("fingernail_sized_self_made_rubber_mask", (properties) -> new PotionIngredient(properties, 4, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> HOUND_OF_FULGRIM_EYES = ITEMS.registerItem("hound_of_fulgrim_eyes", (properties) -> new PotionIngredient(properties, 3, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> DEMONIC_WOLF_OF_FOG_TRANSFORMED_HEART = ITEMS.registerItem("demonic_wolf_of_fog_transformed_heart", (properties) -> new PotionIngredient(properties, 3, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> HOUND_OF_FULGRIM_BLOOD = ITEMS.registerItem("hound_of_fulgrim_blood", (properties) -> new PotionIngredient(properties, 3, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> WHITE_FROST_CRYSTAL_OF_DEMONIC_WOLF_OF_FOG = ITEMS.registerItem("white_frost_crystal_of_demonic_wolf_of_fog", (properties) -> new PotionIngredient(properties, 3, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> REAL_ANCIENT_HISTORICAL_RECORDS = ITEMS.registerItem("real_ancient_historical_records", (properties) -> new PotionIngredient(properties, 3, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> DARK_DEMONIC_WOLF_HEART = ITEMS.registerItem("dark_demonic_wolf_heart", (properties) -> new PotionIngredient(properties, 2, true, "fool"), new Item.Properties());
    public static final DeferredItem<Item> DARK_DEMONIC_WOLF_BLOOD = ITEMS.registerItem("dark_demonic_wolf_blood", (properties) -> new PotionIngredient(properties, 2, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> WORM_OF_TIME_INGREDIENT = ITEMS.registerItem("worm_of_time_ingredient", (properties) -> new PotionIngredient(properties, 2, false, "fool", "door"), new Item.Properties());
    public static final DeferredItem<Item> WORM_OF_STAR = ITEMS.registerItem("worm_of_star", (properties) -> new PotionIngredient(properties, 2, false, "fool", "door"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_WORLD_SPECIALTIES = ITEMS.registerItem("spirit_world_specialties", (properties) -> new PotionIngredient(properties, 1, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> FOG_OF_HISTORY = ITEMS.registerItem("fog_of_history", (properties) -> new PotionIngredient(properties, 0, false, "fool"), new Item.Properties());
    public static final DeferredItem<Item> GEM_DEVOURING_WORM = ITEMS.registerItem("gem_devouring_worm", (properties) -> new PotionIngredient(properties, 9, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> GOAT_BEARD_HYDROSOL = ITEMS.registerItem("goat_beard_hydrosol", (properties) -> new PotionIngredient(properties, 9, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_WELL_WATER = ITEMS.registerItem("ancient_well_water", (properties) -> new PotionIngredient(properties, 9, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> FLOWER_GROWN_FROM_A_CORPSE = ITEMS.registerItem("flower_grown_from_a_corpse", (properties) -> new PotionIngredient(properties, 9, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_WORLD_CONTAMINATED_SOIL = ITEMS.registerItem("spirit_world_contaminated_soil", (properties) -> new PotionIngredient(properties, 9, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> DEEP_SEA_MARLIN_BLOOD = ITEMS.registerItem("deep_sea_marlin_blood", (properties) -> new PotionIngredient(properties, 8, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> HORNBEAM_ESSENTIAL_OILS = ITEMS.registerItem("hornbeam_essential_oils", (properties) -> new PotionIngredient(properties, 8, false, "door", "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> STRING_GRASS_POWDER = ITEMS.registerItem("string_grass_powder", (properties) -> new PotionIngredient(properties, 8, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> LAVOS_SQUID_CRYSTALLIZED_BLOOD = ITEMS.registerItem("lavos_squid_crystallized_blood", (properties) -> new PotionIngredient(properties, 7, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> CLEMATIS_POWDER = ITEMS.registerItem("clematis_powder", (properties) -> new PotionIngredient(properties, 7, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> WITHERED_GRAPEVINE = ITEMS.registerItem("withered_grapevine", (properties) -> new PotionIngredient(properties, 7, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> OCTOPUS_EYEBALLS = ITEMS.registerItem("octopus_eyeballs", (properties) -> new PotionIngredient(properties, 7, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> STRONG_LIQUOR = ITEMS.registerItem("strong_liquor", (properties) -> new PotionIngredient(properties, 7, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> ASMANN_COMPLETE_BRAIN = ITEMS.registerItem("asmann_complete_brain", (properties) -> new PotionIngredient(properties, 6, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> CURSED_ARTIFACTS_OF_AN_ANCIENT_WRAITH = ITEMS.registerItem("cursed_artifacts_of_an_ancient_wraith", (properties) -> new PotionIngredient(properties, 6, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> DIARY_PAGES_OVER_22_YEARS_OLD = ITEMS.registerItem("diary_pages_over_22_years_old", (properties) -> new PotionIngredient(properties, 6, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> MERCURY = ITEMS.registerItem("mercury", (properties) -> new PotionIngredient(properties, 6, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> HONEYSUCKLE_ESSENTIAL_OIL = ITEMS.registerItem("honeysuckle_essential_oil", (properties) -> new PotionIngredient(properties, 6, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> SEAWATER_LAKE_WATER_GLACIER_WATER = ITEMS.registerItem("seawater_lake_water_glacier_water", (properties) -> new PotionIngredient(properties, 6, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> SISKUN_DEMON_WORM = ITEMS.registerItem("siskun_demon_worm", (properties) -> new PotionIngredient(properties, 5, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> TRAPPED_GHOST_RESIDUE_POWDER = ITEMS.registerItem("trapped_ghost_residue_powder", (properties) -> new PotionIngredient(properties, 5, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> LEMON_BALM_POWDER = ITEMS.registerItem("lemon_balm_powder", (properties) -> new PotionIngredient(properties, 5, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> STAR_CHART_DRAWN_WITH_SPIRIT_BLOOD = ITEMS.registerItem("star_chart_drawn_with_spirit_blood", (properties) -> new PotionIngredient(properties, 5, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> SHADOWLESS_DEMONIC_WOLF_BLOOD = ITEMS.registerItem("shadowless_demonic_wolf_blood", (properties) -> new PotionIngredient(properties, 5, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> GOLDEN_PHOENIX_EYE_2 = ITEMS.registerItem("golden_phoenix_eye", (properties) -> new PotionIngredient(properties, 4, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> SISKUN_DEMON_WORM_NEST = ITEMS.registerItem("siskun_demon_worm_nest", (properties) -> new PotionIngredient(properties, 4, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> ANKH_GATE_ASHES = ITEMS.registerItem("ankh_gate_ashes", (properties) -> new PotionIngredient(properties, 4, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> WALNUT = ITEMS.registerItem("walnut", (properties) -> new PotionIngredient(properties, 4, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> LAKE_WATER_FRESHLY_REFLECTING_THE_COSMOS = ITEMS.registerItem("lake_water_freshly_reflecting_the_cosmos", (properties) -> new PotionIngredient(properties, 4, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> WANDERING_SKIN = ITEMS.registerItem("wandering_skin", (properties) -> new PotionIngredient(properties, 3, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> CRYSTAL_LEFT_BY_A_MIST_WATCHER = ITEMS.registerItem("crystal_left_by_a_mist_watcher", (properties) -> new PotionIngredient(properties, 3, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> WANDERING_SKIN_PUS = ITEMS.registerItem("wandering_skin_pus", (properties) -> new PotionIngredient(properties, 3, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> MIST_WATCHER_EYELID = ITEMS.registerItem("mist_watcher_eyelid", (properties) -> new PotionIngredient(properties, 3, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> WHITE_MUSTARD_SEEDS = ITEMS.registerItem("white_mustard_seeds", (properties) -> new PotionIngredient(properties, 3, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> STAR_WATER_FROM_THE_RITUAL_SITE = ITEMS.registerItem("star_water_from_the_ritual_site", (properties) -> new PotionIngredient(properties, 3, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> ACTIVE_VOID = ITEMS.registerItem("active_void", (properties) -> new PotionIngredient(properties, 2, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> INTERDIMENSIONAL_HUNTER_BRAIN = ITEMS.registerItem("interdimensional_hunter_brain", (properties) -> new PotionIngredient(properties, 2, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> WORM_OF_SPIRIT = ITEMS.registerItem("worm_of_spirit", (properties) -> new PotionIngredient(properties, 2, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> COMPASS_LINKED_TO_THE_ASTRAL_WORLD = ITEMS.registerItem("compass_linked_to_the_astral_world", (properties) -> new PotionIngredient(properties, 1, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> WANDERING_ASTEROID_CORE = ITEMS.registerItem("wandering_asteroid_core", (properties) -> new PotionIngredient(properties, 1, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> ONE_FAVORITE_FOOD_OR_DRINK = ITEMS.registerItem("one_favorite_food_or_drink", (properties) -> new PotionIngredient(properties, 1, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES = ITEMS.registerItem("astronomical_geographical_and_folkloric_knowledge_of_three_galaxies", (properties) -> new PotionIngredient(properties, 1, false, "door"), new Item.Properties());
    public static final DeferredItem<Item> BLOOD_SPECKLED_BLACK_MOSQUITO = ITEMS.registerItem("blood_speckled_black_mosquito", (properties) -> new PotionIngredient(properties, 9, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> CANDLE_DEVOURER_CORE = ITEMS.registerItem("candle_devourer_core", (properties) -> new PotionIngredient(properties, 9, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> ANOTHER_BLOOD = ITEMS.registerItem("another_blood", (properties) -> new PotionIngredient(properties, 9, false, "error", "hanged_man"), new Item.Properties());
    public static final DeferredItem<Item> NAIL_FRAGMENTS_FROM_DIFFERENT_PEOPLE = ITEMS.registerItem("nail_fragments_from_different_people", (properties) -> new PotionIngredient(properties, 9, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> SAPPHIRE = ITEMS.registerItem("sapphire", (properties) -> new PotionIngredient(properties, 9, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> VERBENA_POWDER = ITEMS.registerItem("verbena_powder", (properties) -> new PotionIngredient(properties, 9, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> HUMAN_FACED_PITCHER_PLANT = ITEMS.registerItem("human_faced_pitcher_plant", (properties) -> new PotionIngredient(properties, 8, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> SOUL_BEWITCHING_INSECT_COLONY_LARVAE = ITEMS.registerItem("soul_bewitching_insect_colony_larvae", (properties) -> new PotionIngredient(properties, 8, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> ANOTHER_TEARS = ITEMS.registerItem("another_tears", (properties) -> new PotionIngredient(properties, 8, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> LAPIS_LAZULI = ITEMS.registerItem("lapis_lazuli", (properties) -> new PotionIngredient(properties, 8, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> WHITE_CHESTNUT_BALM = ITEMS.registerItem("white_chestnut_balm", (properties) -> new PotionIngredient(properties, 8, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> SOUL_BEWITCHING_INSECT_COLONY_ADULT_INSECT = ITEMS.registerItem("soul_bewitching_insect_colony_adult_insect", (properties) -> new PotionIngredient(properties, 7, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> SPHINX_BLOOD = ITEMS.registerItem("sphinx_blood", (properties) -> new PotionIngredient(properties, 7, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> SOUL_BEWITCHING_INSECT_COLONY_MUCUS = ITEMS.registerItem("soul_bewitching_insect_colony_mucus", (properties) -> new PotionIngredient(properties, 7, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> MOONSTONE = ITEMS.registerItem("moonstone", (properties) -> new PotionIngredient(properties, 7, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> WILD_ROSE = ITEMS.registerItem("wild_rose", (properties) -> new PotionIngredient(properties, 7, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> SELF_DESIGNED_CIPHER = ITEMS.registerItem("self_designed_cipher", (properties) -> new PotionIngredient(properties, 7, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> CRYSTAL_THREADWORM = ITEMS.registerItem("crystal_threadworm", (properties) -> new PotionIngredient(properties, 6, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> CLOAKED_SPECTER_ATTACHMENT = ITEMS.registerItem("cloaked_specter_attachment", (properties) -> new PotionIngredient(properties, 6, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> FRESHLY_STOLEN_WINE = ITEMS.registerItem("freshly_stolen_wine", (properties) -> new PotionIngredient(properties, 6, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> CLOAKED_SPECTER_RESIDUAL_POWDER = ITEMS.registerItem("cloaked_specter_residual_powder", (properties) -> new PotionIngredient(properties, 6, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> CITRINE = ITEMS.registerItem("citrine", (properties) -> new PotionIngredient(properties, 6, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> LARCH_ESSENTIAL_OIL = ITEMS.registerItem("larch_essential_oil", (properties) -> new PotionIngredient(properties, 6, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> TINDER = ITEMS.registerItem("tinder", (properties) -> new PotionIngredient(properties, 6, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> DREAM_EATING_RAT_HEART = ITEMS.registerItem("dream_eating_rat_heart", (properties) -> new PotionIngredient(properties, 5, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_OF_DEPRAVED_BREATH = ITEMS.registerItem("spirit_of_depraved_breath", (properties) -> new PotionIngredient(properties, 5, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> DREAM_EATING_RAT_BLOOD = ITEMS.registerItem("dream_eating_rat_blood", (properties) -> new PotionIngredient(properties, 5, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> DE_SPIRITUALIZED_DEPRAVED_BREATH = ITEMS.registerItem("de_spiritualized_depraved_breath", (properties) -> new PotionIngredient(properties, 5, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> CELESTINE = ITEMS.registerItem("celestine", (properties) -> new PotionIngredient(properties, 5, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> LAVENDER_HYDROSOL = ITEMS.registerItem("lavender_hydrosol", (properties) -> new PotionIngredient(properties, 5, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> ANOTHER_TEARS_SHED_DUE_TO_SHATTERED_IDEALS = ITEMS.registerItem("another_tears_shed_due_to_shattered_ideals", (properties) -> new PotionIngredient(properties, 5, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> SOUL_SNATCHER_POSTMORTEM_CRYSTAL = ITEMS.registerItem("soul_snatcher_postmortem_crystal", (properties) -> new PotionIngredient(properties, 4, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> PUPPET_EVIL_INSECT = ITEMS.registerItem("puppet_evil_insect", (properties) -> new PotionIngredient(properties, 4, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> SOUL_SNATCHER_BLOOD = ITEMS.registerItem("soul_snatcher_blood", (properties) -> new PotionIngredient(properties, 4, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> PUPPET_EVIL_INSECT_SECRETIONS = ITEMS.registerItem("puppet_evil_insect_secretions", (properties) -> new PotionIngredient(properties, 4, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> AMETHYST = ITEMS.registerItem("amethyst", (properties) -> new PotionIngredient(properties, 4, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> IMPRISONED_SOUL = ITEMS.registerItem("imprisoned_soul", (properties) -> new PotionIngredient(properties, 4, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> SWAMP_GIANT_PALMS = ITEMS.registerItem("swamp_giant_palms", (properties) -> new PotionIngredient(properties, 3, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> BLASPHEMOUS_PRIEST_CORE = ITEMS.registerItem("blasphemous_priest_core", (properties) -> new PotionIngredient(properties, 3, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> SWAMP_GIANT_BLOOD = ITEMS.registerItem("swamp_giant_blood", (properties) -> new PotionIngredient(properties, 3, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> BLASPHEMOUS_PRIEST_FUNGAL_SHROUD = ITEMS.registerItem("blasphemous_priest_fungal_shroud", (properties) -> new PotionIngredient(properties, 3, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> GOLD = ITEMS.registerItem("gold", (properties) -> new PotionIngredient(properties, 3, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> WATER_VIOLET_HYDROSOL = ITEMS.registerItem("water_violet_hydrosol", (properties) -> new PotionIngredient(properties, 3, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_WORLD_WRAITHS_WHO_DIED_DUE_TO_THE_ADVANCER_DECEIT = ITEMS.registerItem("spirit_world_wraiths_who_died_due_to_the_advancer_deceit", (properties) -> new PotionIngredient(properties, 3, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> GIANT_KING_COURT_PRIEST_POSTMORTEM_CRYSTAL = ITEMS.registerItem("giant_king_court_priest_postmortem_crystal", (properties) -> new PotionIngredient(properties, 2, true, "error"), new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_RIVER_WATER = ITEMS.registerItem("ancient_river_water", (properties) -> new PotionIngredient(properties, 2, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> FALSE_HISTORY_BELIEVED_TO_BE_TRUE = ITEMS.registerItem("false_history_believed_to_be_true", (properties) -> new PotionIngredient(properties, 2, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_CLOCK = ITEMS.registerItem("ancient_clock", (properties) -> new PotionIngredient(properties, 1, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> USED_ALMANAC = ITEMS.registerItem("used_almanac", (properties) -> new PotionIngredient(properties, 1, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> SEGMENT_OF_CONCEALED_FATE = ITEMS.registerItem("segment_of_concealed_fate", (properties) -> new PotionIngredient(properties, 1, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> BEACONS_EXTRACTED_FROM_THE_RIVER_OF_FATE = ITEMS.registerItem("beacons_extracted_from_the_river_of_fate", (properties) -> new PotionIngredient(properties, 0, false, "error"), new Item.Properties());
    public static final DeferredItem<Item> MATURED_MANHAL_FISH_EYEBALL = ITEMS.registerItem("matured_manhal_fish_eyeball", (properties) -> new PotionIngredient(properties, 9, true, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> AUTUMN_CROCUS_ESSENCE = ITEMS.registerItem("autumn_crocus_essence", (properties) -> new PotionIngredient(properties, 9, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> COW_TEETH_PAEONOL_POWDER = ITEMS.registerItem("cow_teeth_paeonol_powder", (properties) -> new PotionIngredient(properties, 9, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> ELF_FLOWER_PETALS = ITEMS.registerItem("elf_flower_petals", (properties) -> new PotionIngredient(properties, 9, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> FARSMAN_RABBIT_SPINAL_FLUID = ITEMS.registerItem("farsman_rabbit_spinal_fluid", (properties) -> new PotionIngredient(properties, 8, true, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> CHESTNUT_SPORE = ITEMS.registerItem("chestnut_spore", (properties) -> new PotionIngredient(properties, 8, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> PURE_WHITE_ELF_FLOWERS = ITEMS.registerItem("pure_white_elf_flowers", (properties) -> new PotionIngredient(properties, 8, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> TREE_OF_ELDER_FRUIT = ITEMS.registerItem("tree_of_elder_fruit", (properties) -> new PotionIngredient(properties, 7, true, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> MIRROR_DRAGON_EYES = ITEMS.registerItem("mirror_dragon_eyes", (properties) -> new PotionIngredient(properties, 7, true, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> MIRROR_DRAGON_BLOOD = ITEMS.registerItem("mirror_dragon_blood", (properties) -> new PotionIngredient(properties, 7, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> BLACK_HUNTING_GIANT_LIZARD_SPINAL_FLUID = ITEMS.registerItem("black_hunting_giant_lizard_spinal_fluid", (properties) -> new PotionIngredient(properties, 6, true, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> MIND_ILLUSION_CRYSTAL = ITEMS.registerItem("mind_illusion_crystal", (properties) -> new PotionIngredient(properties, 5, true, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> MIND_DRAGON_BLOOD_ADULT = ITEMS.registerItem("mind_dragon_blood_adult", (properties) -> new PotionIngredient(properties, 5, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> MIND_DRAGON_BLOOD_ELDERLY = ITEMS.registerItem("mind_dragon_blood_elderly", (properties) -> new PotionIngredient(properties, 4, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> TREE_MENTOR_GOLDEN_LEAF = ITEMS.registerItem("tree_mentor_golden_leaf", (properties) -> new PotionIngredient(properties, 4, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> DROP_OF_TEARS = ITEMS.registerItem("drop_of_tears", (properties) -> new PotionIngredient(properties, 4, false, "visionary"), new Item.Properties());
    public static final DeferredItem<Item> SIREN_ROCK = ITEMS.registerItem("siren_rock", (properties) -> new PotionIngredient(properties, 9, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> MIDSUMMER_GRASS = ITEMS.registerItem("midsummer_grass", (properties) -> new PotionIngredient(properties, 9, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> JULY_WINE_JUICE = ITEMS.registerItem("july_wine_juice", (properties) -> new PotionIngredient(properties, 9, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> ELF_DARK_LEAF = ITEMS.registerItem("elf_dark_leaf", (properties) -> new PotionIngredient(properties, 9, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> HEART_OF_A_MAGMA_TITAN = ITEMS.registerItem("heart_of_a_magma_titan", (properties) -> new PotionIngredient(properties, 8, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> ELF_DARK_LEAF_2 = ITEMS.registerItem("elf_dark_leaf_2", (properties) -> new PotionIngredient(properties, 8, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> ACONITE_JUICE = ITEMS.registerItem("aconite_juice", (properties) -> new PotionIngredient(properties, 8, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> DAWN_ROOSTER_RED_COMB = ITEMS.registerItem("dawn_rooster_red_comb", (properties) -> new PotionIngredient(properties, 7, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> DAWN_ROOSTER_BLOOD = ITEMS.registerItem("dawn_rooster_blood", (properties) -> new PotionIngredient(properties, 7, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> SUN_ESSENTIAL_OIL = ITEMS.registerItem("sun_essential_oil", (properties) -> new PotionIngredient(properties, 7, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> FINGERED_CITRON_POWDER = ITEMS.registerItem("fingered_citron_powder", (properties) -> new PotionIngredient(properties, 7, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> SOLIDIFIED_MAGMA = ITEMS.registerItem("solidified_magma", (properties) -> new PotionIngredient(properties, 7, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> TREE_OF_ELDER_CRYSTALIZED_ROOTS = ITEMS.registerItem("tree_of_elder_crystalized_roots", (properties) -> new PotionIngredient(properties, 6, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_PACT_BIRD_FEATHER = ITEMS.registerItem("spirit_pact_bird_feather", (properties) -> new PotionIngredient(properties, 6, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> RADIANCE_SPIRIT_PACT_TREE_JUICE = ITEMS.registerItem("radiance_spirit_pact_tree_juice", (properties) -> new PotionIngredient(properties, 6, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> AQUA_FERN_JUICE = ITEMS.registerItem("aqua_fern_juice", (properties) -> new PotionIngredient(properties, 6, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> KING_OF_DAWN_ROOSTERS_RED_COMB = ITEMS.registerItem("king_of_dawn_roosters_red_comb", (properties) -> new PotionIngredient(properties, 5, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> ROSEMARY = ITEMS.registerItem("rosemary", (properties) -> new PotionIngredient(properties, 5, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> FINGERED_CITRON_JUICE = ITEMS.registerItem("fingered_citron_juice", (properties) -> new PotionIngredient(properties, 5, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> KING_OF_DAWN_ROOSTERS_BLOOD = ITEMS.registerItem("king_of_dawn_roosters_blood", (properties) -> new PotionIngredient(properties, 5, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> GOLDEN_BLOOD_OF_THE_SUN_GOD = ITEMS.registerItem("golden_blood_of_the_sun_god", (properties) -> new PotionIngredient(properties, 4, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> SUN_DIVINE_BIRD_BLOOD = ITEMS.registerItem("sun_divine_bird_blood", (properties) -> new PotionIngredient(properties, 4, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> HOLY_BRILLIANCE_ROCK_LIQUID = ITEMS.registerItem("holy_brilliance_rock_liquid", (properties) -> new PotionIngredient(properties, 4, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> MUTATED_FINGERED_CITRON_JUICE = ITEMS.registerItem("mutated_fingered_citron_juice", (properties) -> new PotionIngredient(properties, 4, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> MAGMA_HEART_POWDER = ITEMS.registerItem("magma_heart_powder", (properties) -> new PotionIngredient(properties, 4, false, "sun"), new Item.Properties());
    public static final DeferredItem<Item> BLUE_SHADOW_FALCON_CRYSTALLINE_FEATHERS = ITEMS.registerItem("blue_shadow_falcon_crystalline_feathers", (properties) -> new PotionIngredient(properties, 6, true, "tyrant"), new Item.Properties());
    public static final DeferredItem<Item> DAFFODIL_JUICE = ITEMS.registerItem("daffodil_juice", (properties) -> new PotionIngredient(properties, 7, false, "demoness"), new Item.Properties());
    public static final DeferredItem<Item> BLACK_WIDOW_SPIDER_SILK_GLAND = ITEMS.registerItem("black_widow_spider_silk_gland", (properties) -> new PotionIngredient(properties, 6, true, "demoness"), new Item.Properties());
    public static final DeferredItem<Item> SUCCUBUS_HAIR_COMPLETE_REMNANTS = ITEMS.registerItem("succubus_hair_complete_remnants", (properties) -> new PotionIngredient(properties, 6, false, "demoness"), new Item.Properties());
    public static final DeferredItem<Item> FEYNAPOTTER_FLY_POWDER = ITEMS.registerItem("feynapotter_fly_powder", (properties) -> new PotionIngredient(properties, 6, false, "demoness"), new Item.Properties());
    public static final DeferredItem<Item> MUMMY_ASHES = ITEMS.registerItem("mummy_ashes", (properties) -> new PotionIngredient(properties, 6, false, "demoness"), new Item.Properties());
    public static final DeferredItem<Item> FLYING_UNICORN_HORN = ITEMS.registerItem("flying_unicorn_horn", (properties) -> new PotionIngredient(properties, 9, true, "moon"), new Item.Properties());
    public static final DeferredItem<Item> ROYAL_JELLYFISH_VENOM_CRYSTAL = ITEMS.registerItem("royal_jellyfish_venom_crystal", (properties) -> new PotionIngredient(properties, 9, true, "moon"), new Item.Properties());
    public static final DeferredItem<Item> SPRING_OF_THE_ELVES_MARROW_CRYSTAL = ITEMS.registerItem("spring_of_the_elves_marrow_crystal", (properties) -> new PotionIngredient(properties, 8, true, "moon"), new Item.Properties());
    public static final DeferredItem<Item> TERROR_DEMON_WORM_EYES = ITEMS.registerItem("terror_demon_worm_eyes", (properties) -> new PotionIngredient(properties, 8, true, "justiciar"), new Item.Properties());
    public static final DeferredItem<Item> SILVER_WAR_BEAR_RIGHT_PALM = ITEMS.registerItem("silver_war_bear_right_palm", (properties) -> new PotionIngredient(properties, 8, true, "justiciar"), new Item.Properties());
    public static final DeferredItem<Item> FLASH_PATTERNED_BLACK_SNAKE_HORN = ITEMS.registerItem("flash_patterned_black_snake_horn", (properties) -> new PotionIngredient(properties, 7, true, "justiciar"), new Item.Properties());
    public static final DeferredItem<Item> DUST_OF_A_LAKE_SPIRIT = ITEMS.registerItem("dust_of_a_lake_spirit", (properties) -> new PotionIngredient(properties, 7, true, "justiciar"), new Item.Properties());
    public static final DeferredItem<Item> RED_WINE = ITEMS.registerItem("red_wine", (properties) -> new PotionIngredient(properties, 7, false, "black_emperor", "red_priest", "hanged_man", "sun", "darkness"), new Item.Properties());
    public static final DeferredItem<Item> POPLAR_TREE_LEAF_POWDER = ITEMS.registerItem("poplar_tree_leaf_powder", (properties) -> new PotionIngredient(properties, 9, false, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> BASIL = ITEMS.registerItem("basil", (properties) -> new PotionIngredient(properties, 9, false, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> DISTILLED_LIQUOR = ITEMS.registerItem("distilled_liquor", (properties) -> new PotionIngredient(properties, 8, false, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> HONEYSUCKLE_EXTRACT = ITEMS.registerItem("honeysuckle_extract", (properties) -> new PotionIngredient(properties, 8, false, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> GRAPEVINE_POWDER = ITEMS.registerItem("grapevine_powder", (properties) -> new PotionIngredient(properties, 8, false, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> FIRE_SALAMANDER_GLAND = ITEMS.registerItem("fire_salamander_gland", (properties) -> new PotionIngredient(properties, 7, true, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> FIRE_SALAMANDER_BLOOD = ITEMS.registerItem("fire_salamander_blood", (properties) -> new PotionIngredient(properties, 7, false, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> MAGMA_PYROXENE_POWDER = ITEMS.registerItem("magma_pyroxene_powder", (properties) -> new PotionIngredient(properties, 7, false, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> SUN_STAR_EXTRACT = ITEMS.registerItem("sun_star_extract", (properties) -> new PotionIngredient(properties, 7, false, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> SPHINX_BRAIN_2 = ITEMS.registerItem("sphinx_brain_2", (properties) -> new PotionIngredient(properties, 6, true, "red_priest"), new Item.Properties());
    public static final DeferredItem<Item> GRASS_OF_MADNESS = ITEMS.registerItem("grass_of_madness", (properties) -> new PotionIngredient(properties, 8, true, "black_emperor"), new Item.Properties());
    public static final DeferredItem<Item> LAND_RHINOCEROS_CORE_HORN_CRYSTAL = ITEMS.registerItem("land_rhinoceros_core_horn_crystal", (properties) -> new PotionIngredient(properties, 8, true, "black_emperor"), new Item.Properties());
    public static final DeferredItem<Item> DEEP_GRAINED_WALNUT = ITEMS.registerItem("deep_grained_walnut", (properties) -> new PotionIngredient(properties, 8, false, "black_emperor"), new Item.Properties());
    public static final DeferredItem<Item> FRAGRANCE_HORNET_GRASS = ITEMS.registerItem("fragrance_hornet_grass", (properties) -> new PotionIngredient(properties, 8, false, "black_emperor"), new Item.Properties());
    public static final DeferredItem<Item> SOAKING_POPLAR_BARK_EXTRACTED = ITEMS.registerItem("soaking_poplar_bark_extracted", (properties) -> new PotionIngredient(properties, 8, false, "black_emperor"), new Item.Properties());
    public static final DeferredItem<Item> LIQUOR = ITEMS.registerItem("liquor", (properties) -> new PotionIngredient(properties, 8, false, "black_emperor"), new Item.Properties());
    public static final DeferredItem<Item> WEEPING_INFANT_FLOWER = ITEMS.registerItem("weeping_infant_flower", (properties) -> new PotionIngredient(properties, 7, true, "black_emperor"), new Item.Properties());
    public static final DeferredItem<Item> STRANGE_FACED_CANNABIS_CRYSTAL = ITEMS.registerItem("strange_faced_cannabis_crystal", (properties) -> new PotionIngredient(properties, 7, true, "black_emperor"), new Item.Properties());
    public static final DeferredItem<Item> GOLDEN_JIMSONWEED_JUICE = ITEMS.registerItem("golden_jimsonweed_juice", (properties) -> new PotionIngredient(properties, 8, false, "black_emperor"), new Item.Properties());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static List<PotionIngredient> getAllOfPathway(String pathway) {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionIngredient)
                .map(i -> ((PotionIngredient) i))
                .filter(i -> i.getPathways() != null && Arrays.asList(i.getPathways()).contains(pathway))
                .toList();
    }

    public static List<PotionIngredient> getAll() {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionIngredient)
                .map(i -> ((PotionIngredient) i))
                .toList();
    }


    public static PotionIngredient selectRandomIngredient(Random random) {
        List<PotionIngredient> ingredients = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionIngredient)
                .map(i -> ((PotionIngredient) i))
                .toList();

        if (ingredients.isEmpty()) {
            return null;
        }

        // Calculate weights for each potion
        // Higher sequence = more common = higher weight
        // Weight formula: sequence + 1 makes sequence 9 -> weight 10, sequence 0 -> weight 1
        Map<PotionIngredient, Integer> weights = new HashMap<>();
        int totalWeight = 0;

        for (PotionIngredient ingredient : ingredients) {
            int weight = ingredient.getSequence() + 1; // Higher sequence = more common = higher weight
            weights.put(ingredient, weight);
            totalWeight += weight;
        }

        // Generate random number between 0 and totalWeight-1
        int randomValue = random.nextInt(totalWeight);

        // Find the selected potion based on cumulative weights
        int cumulativeWeight = 0;
        for (Map.Entry<PotionIngredient, Integer> entry : weights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should never reach here with valid input)
        return ingredients.get(ingredients.size() - 1);
    }

    public static PotionIngredient selectRandomIngredientOfPathway(Random random, String pathway) {
        List<PotionIngredient> ingredients = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionIngredient)
                .map(i -> ((PotionIngredient) i))
                .filter(i -> i.getPathways() != null && Arrays.asList(i.getPathways()).contains(pathway))
                .toList();

        if (ingredients.isEmpty()) {
            return null;
        }

        // Calculate weights for each potion
        // Higher sequence = more common = higher weight
        // Weight formula: sequence + 1 makes sequence 9 -> weight 10, sequence 0 -> weight 1
        Map<PotionIngredient, Integer> weights = new HashMap<>();
        int totalWeight = 0;

        for (PotionIngredient ingredient : ingredients) {
            int weight = ingredient.getSequence() + 1; // Higher sequence = more common = higher weight
            weights.put(ingredient, weight);
            totalWeight += weight;
        }

        // Generate random number between 0 and totalWeight-1
        int randomValue = random.nextInt(totalWeight);

        // Find the selected potion based on cumulative weights
        int cumulativeWeight = 0;
        for (Map.Entry<PotionIngredient, Integer> entry : weights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should never reach here with valid input)
        return ingredients.get(ingredients.size() - 1);
    }

    public static PotionIngredient selectRandomIngredientOfPathwayAndSequence(Random random, String pathway, int sequence) {
        List<PotionIngredient> ingredients = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionIngredient)
                .map(i -> ((PotionIngredient) i))
                .filter(i -> i.getPathways() != null && Arrays.asList(i.getPathways()).contains(pathway))
                .filter(i -> i.getSequence() == sequence)
                .toList();

        if (ingredients.isEmpty()) {
            return null;
        }

        return ingredients.get(random.nextInt(ingredients.size()));
    }
}


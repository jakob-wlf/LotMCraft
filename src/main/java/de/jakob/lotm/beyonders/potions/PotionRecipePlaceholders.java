package de.jakob.lotm.beyonders.potions;

import de.jakob.lotm.item.ModIngredients;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class PotionRecipePlaceholders {

    public static final Set<PotionRecipePlaceholder> RECIPES = new HashSet<>();

    public static boolean initialized = false;

    public static void initPotionRecipes() {
        if (initialized) return;
        initialized = true;
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SEER_POTION.get(),
                new ItemStack(ModIngredients.MIDSUMMER_GRASS.get()),
                new ItemStack(ModIngredients.ELF_DARK_LEAF.get()),
                new ItemStack(ModIngredients.ANOTHER_BLOOD.get()),
                new ItemStack(ModIngredients.POPLAR_TREE_LEAF_POWDER.get()),
                new ItemStack(ModIngredients.LAVOS_SQUID_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CLOWN_POTION.get(),
                new ItemStack(ModIngredients.HONEYSUCKLE_EXTRACT.get()),
                new ItemStack(ModIngredients.WHITE_CHESTNUT_BALM.get()),
                new ItemStack(ModIngredients.LIQUOR.get()),
                new ItemStack(ModIngredients.PURE_WHITE_ELF_FLOWERS.get()),
                new ItemStack(ModIngredients.HORNACIS_GRAY_MOUNTAIN_GOAT_HORN.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.MAGICIAN_POTION.get(),
                new ItemStack(ModIngredients.SPHINX_BLOOD.get()),
                new ItemStack(ModIngredients.MAGMA_PYROXENE_POWDER.get()),
                new ItemStack(ModIngredients.MOONSTONE.get()),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.MIST_TREANT_TRUE_ROOT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.FACELESS_POTION.get(),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.FRESHLY_STOLEN_WINE.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.SEAWATER_LAKE_WATER_GLACIER_WATER.get()),
                new ItemStack(ModIngredients.MUTATED_PITUITARY_GLAND_OF_A_THOUSAND_FACED_HUNTER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.MARIONETTIST_POTION.get(),
                new ItemStack(ModIngredients.SIX_WINGED_GARGOYLE_EYES.get()),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.MIND_DRAGON_BLOOD_ADULT.get()),
                new ItemStack(ModIngredients.LAVENDER_HYDROSOL.get()),
                new ItemStack(ModIngredients.DUST_OF_ANCIENT_WRAITHS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.BIZARRO_SORCERER_POTION.get(),
                new ItemStack(ModIngredients.BIZARRO_BANE_BLOOD.get()),
                new ItemStack(ModIngredients.RED_HAIR_BIRCH_BARK.get()),
                new ItemStack(ModIngredients.GOLDEN_GRAPEVINES.get()),
                new ItemStack(ModIngredients.FINGERNAIL_SIZED_SELF_MADE_RUBBER_MASK.get()),
                new ItemStack(ModIngredients.BIZARRO_BANE_MAIN_EYE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SCHOLAR_OF_YORE_POTION.get(),
                new ItemStack(ModIngredients.HOUND_OF_FULGRIM_BLOOD.get()),
                new ItemStack(ModIngredients.WHITE_FROST_CRYSTAL_OF_DEMONIC_WOLF_OF_FOG.get()),
                new ItemStack(ModIngredients.REAL_ANCIENT_HISTORICAL_RECORDS.get()),
                new ItemStack(ModIngredients.WATER_VIOLET_HYDROSOL.get()),
                new ItemStack(ModIngredients.HOUND_OF_FULGRIM_EYES.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.MIRACLE_INVOKER_POTION.get(),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_HEART.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.ATTENDANT_OF_MYSTERIES_POTION.get(),
                new ItemStack(ModIngredients.SPIRIT_WORLD_SPECIALTIES.get()),
                new ItemStack(ModIngredients.ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES.get()),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.COMPASS_LINKED_TO_THE_ASTRAL_WORLD.get()),
                new ItemStack(ModIngredients.ROOT_OF_MIST_TREANT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.BARD_POTION.get(),
                new ItemStack(ModIngredients.MIDSUMMER_GRASS.get()),
                new ItemStack(ModIngredients.JULY_WINE_JUICE.get()),
                new ItemStack(ModIngredients.ELF_DARK_LEAF.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(ModIngredients.CRYSTAL_SUNFLOWER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.LIGHT_SUPPLICANT_POTION.get(),
                new ItemStack(ModIngredients.ELF_DARK_LEAF.get()),
                new ItemStack(ModIngredients.ACONITE_JUICE.get()),
                new ItemStack(ModIngredients.STRING_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.LAPIS_LAZULI.get()),
                new ItemStack(ModIngredients.POWDER_OF_DAZZLING_SOUL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SOLAR_HIGH_PRIEST_POTION.get(),
                new ItemStack(ModIngredients.DAWN_ROOSTER_BLOOD.get()),
                new ItemStack(ModIngredients.SUN_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.FINGERED_CITRON_POWDER.get()),
                new ItemStack(ModIngredients.SOLIDIFIED_MAGMA.get()),
                new ItemStack(ModIngredients.DAWN_ROOSTER_RED_COMB.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.NOTARY_POTION.get(),
                new ItemStack(ModIngredients.RADIANCE_SPIRIT_PACT_TREE_JUICE.get()),
                new ItemStack(ModIngredients.AQUA_FERN_JUICE.get()),
                new ItemStack(ModIngredients.TINDER.get()),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.TREE_OF_ELDER_CRYSTALIZED_ROOTS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PRIEST_OF_LIGHT_POTION.get(),
                new ItemStack(ModIngredients.FINGERED_CITRON_JUICE.get()),
                new ItemStack(ModIngredients.KING_OF_DAWN_ROOSTERS_BLOOD.get()),
                new ItemStack(ModIngredients.ANOTHER_TEARS_SHED_DUE_TO_SHATTERED_IDEALS.get()),
                new ItemStack(ModIngredients.TRAPPED_GHOST_RESIDUE_POWDER.get()),
                new ItemStack(ModIngredients.KING_OF_DAWN_ROOSTERS_RED_COMB.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.UNSHADOWED.get(),
                new ItemStack(ModIngredients.SUN_DIVINE_BIRD_BLOOD.get()),
                new ItemStack(ModIngredients.HOLY_BRILLIANCE_ROCK_LIQUID.get()),
                new ItemStack(ModIngredients.MUTATED_FINGERED_CITRON_JUICE.get()),
                new ItemStack(ModIngredients.MAGMA_HEART_POWDER.get()),
                new ItemStack(ModIngredients.GOLDEN_BLOOD_OF_THE_SUN_GOD.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.JUSTICE_MENTOR_POTION.get(),
                new ItemStack(ModIngredients.BLASPHEMOUS_PRIEST_FUNGAL_SHROUD.get()),
                new ItemStack(ModIngredients.WATER_VIOLET_HYDROSOL.get()),
                new ItemStack(ModIngredients.WHITE_FROST_CRYSTAL_OF_DEMONIC_WOLF_OF_FOG.get()),
                new ItemStack(ModIngredients.MIST_WATCHER_EYELID.get()),
                new ItemStack(ModIngredients.SPIRIT_PACT_TREE_FRUIT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.LIGHTSEEKER_POTION.get(),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.CRYSTALLIZED_ROOTS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.WHITE_ANGEL_POTION.get(),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.WANDERING_ASTEROID_CORE.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES.get()),
                new ItemStack(ModIngredients.PURE_WHITE_BRILLIANT_ROCK.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SAILOR_POTION.get(),
                new ItemStack(ModIngredients.SAPPHIRE.get()),
                new ItemStack(ModIngredients.VERBENA_POWDER.get()),
                new ItemStack(ModIngredients.GOAT_BEARD_HYDROSOL.get()),
                new ItemStack(ModIngredients.ELF_DARK_LEAF.get()),
                new ItemStack(ModIngredients.MURLOC_BLADDER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.FOLK_OF_RAGE_POTION.get(),
                new ItemStack(ModIngredients.PURE_WHITE_ELF_FLOWERS.get()),
                new ItemStack(ModIngredients.STRING_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.ANOTHER_TEARS.get()),
                new ItemStack(ModIngredients.DISTILLED_LIQUOR.get()),
                new ItemStack(ModIngredients.DRAGON_EYED_CONDOR_EYEBALL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SEAFARER_POTION.get(),
                new ItemStack(ModIngredients.WITHERED_GRAPEVINE.get()),
                new ItemStack(ModIngredients.MOONSTONE.get()),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.MIRROR_DRAGON_BLOOD.get()),
                new ItemStack(ModIngredients.ANCIENT_LOGBOOK.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.WIND_BLESSED_POTION.get(),
                new ItemStack(ModIngredients.DIARY_PAGES_OVER_22_YEARS_OLD.get()),
                new ItemStack(ModIngredients.MERCURY.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.BLUE_SHADOW_FALCON_CRYSTALLINE_FEATHERS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.OCEAN_SONGSTER_POTION.get(),
                new ItemStack(ModIngredients.FINGERED_CITRON_JUICE.get()),
                new ItemStack(ModIngredients.KING_OF_DAWN_ROOSTERS_BLOOD.get()),
                new ItemStack(ModIngredients.STAR_CHART_DRAWN_WITH_SPIRIT_BLOOD.get()),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.BLUE_SHADOW_FALCON_FEATHERS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CATACLYSMIC_INTERRER_POTION.get(),
                new ItemStack(ModIngredients.DROP_OF_TEARS.get()),
                new ItemStack(ModIngredients.GOLDEN_GRAPEVINES.get()),
                new ItemStack(ModIngredients.LAKE_WATER_FRESHLY_REFLECTING_THE_COSMOS.get()),
                new ItemStack(ModIngredients.IMPRISONED_SOUL.get()),
                new ItemStack(ModIngredients.SIREN_VOCAL_SAC.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SEA_KING_POTION.get(),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.HOUND_OF_FULGRIM_BLOOD.get()),
                new ItemStack(ModIngredients.SWAMP_GIANT_BLOOD.get()),
                new ItemStack(ModIngredients.STAR_WATER_FROM_THE_RITUAL_SITE.get()),
                new ItemStack(ModIngredients.WHALE_OF_PUNISHMENT_STOMACH.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CALAMITY_POTION.get(),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.KING_OF_GREEN_WINGS_EYE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.THUNDER_GOD_POTION.get(),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(ModIngredients.ANCIENT_CLOCK.get()),
                new ItemStack(ModIngredients.ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.THOUSAND_FACED_HUNTER_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.HUNTER_POTION.get(),
                new ItemStack(ModIngredients.POPLAR_TREE_LEAF_POWDER.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_CONTAMINATED_SOIL.get()),
                new ItemStack(ModIngredients.JULY_WINE_JUICE.get()),
                new ItemStack(ModIngredients.RED_CHESTNUT_FLOWER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PROVOKER_POTION.get(),
                new ItemStack(ModIngredients.DISTILLED_LIQUOR.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_EXTRACT.get()),
                new ItemStack(ModIngredients.GRAPEVINE_POWDER.get()),
                new ItemStack(ModIngredients.ACONITE_JUICE.get()),
                new ItemStack(ModIngredients.REDCROWN_BALSAM_POWDER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PYROMANIAC_POTION.get(),
                new ItemStack(ModIngredients.FIRE_SALAMANDER_BLOOD.get()),
                new ItemStack(ModIngredients.MAGMA_PYROXENE_POWDER.get()),
                new ItemStack(ModIngredients.SUN_STAR_EXTRACT.get()),
                new ItemStack(ModIngredients.FINGERED_CITRON_POWDER.get()),
                new ItemStack(ModIngredients.FIRE_SALAMANDER_GLAND.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CONSPIRER_POTION.get(),
                new ItemStack(ModIngredients.SPHINX_BLOOD.get()),
                new ItemStack(ModIngredients.TINDER.get()),
                new ItemStack(ModIngredients.FRESHLY_STOLEN_WINE.get()),
                new ItemStack(ModIngredients.CITRINE.get()),
                new ItemStack(ModIngredients.BLACK_HUNTING_SPIDER_COMPOSITE_EYES.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.REAPER_POTION.get(),
                new ItemStack(ModIngredients.SIX_WINGED_GARGOYLE_EYES.get()),
                new ItemStack(ModIngredients.KING_OF_DAWN_ROOSTERS_BLOOD.get()),
                new ItemStack(ModIngredients.DE_SPIRITUALIZED_DEPRAVED_BREATH.get()),
                new ItemStack(ModIngredients.STAR_CHART_DRAWN_WITH_SPIRIT_BLOOD.get()),
                new ItemStack(ModIngredients.MAGMA_ELF_CORE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.IRON_BLOODED_KNIGHT_POTION.get(),
                new ItemStack(ModIngredients.TREE_MENTOR_GOLDEN_LEAF.get()),
                new ItemStack(ModIngredients.DROP_OF_TEARS.get()),
                new ItemStack(ModIngredients.SUN_DIVINE_BIRD_BLOOD.get()),
                new ItemStack(ModIngredients.LAKE_WATER_FRESHLY_REFLECTING_THE_COSMOS.get()),
                new ItemStack(ModIngredients.MAGMA_GIANT_CORE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.WAR_BISHOP_POTION.get(),
                new ItemStack(ModIngredients.WHITE_MUSTARD_SEEDS.get()),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.SWAMP_GIANT_BLOOD.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_WRAITHS_WHO_DIED_DUE_TO_THE_ADVANCER_DECEIT.get()),
                new ItemStack(ModIngredients.WAR_COMET_CORE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.WEATHER_WARLOCK_POTION.get(),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.HORNBEAM_ESSENTIAL_OILS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CONQUEROR_POTION.get(),
                new ItemStack(ModIngredients.ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_SPECIALTIES.get()),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.RED_WINE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.APPRENTICE_POTION.get(),
                new ItemStack(ModIngredients.GOAT_BEARD_HYDROSOL.get()),
                new ItemStack(ModIngredients.ANCIENT_WELL_WATER.get()),
                new ItemStack(ModIngredients.FLOWER_GROWN_FROM_A_CORPSE.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_CONTAMINATED_SOIL.get()),
                new ItemStack(ModIngredients.GEM_DEVOURING_WORM.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.TRICKMASTER_POTION.get(),
                new ItemStack(ModIngredients.STRING_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.FRAGRANCE_HORNET_GRASS.get()),
                new ItemStack(ModIngredients.SOAKING_POPLAR_BARK_EXTRACTED.get()),
                new ItemStack(ModIngredients.CHESTNUT_SPORE.get()),
                new ItemStack(ModIngredients.SPIRIT_EATER_STOMACH_POUCH.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.ASTROLOGER_POTION.get(),
                new ItemStack(ModIngredients.CLEMATIS_POWDER.get()),
                new ItemStack(ModIngredients.WITHERED_GRAPEVINE.get()),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.METEORITE_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SCRIBE_POTION.get(),
                new ItemStack(ModIngredients.DIARY_PAGES_OVER_22_YEARS_OLD.get()),
                new ItemStack(ModIngredients.MERCURY.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.SEAWATER_LAKE_WATER_GLACIER_WATER.get()),
                new ItemStack(ModIngredients.ASMANN_COMPLETE_BRAIN.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.TRAVELER_POTION.get(),
                new ItemStack(ModIngredients.TRAPPED_GHOST_RESIDUE_POWDER.get()),
                new ItemStack(ModIngredients.LEMON_BALM_POWDER.get()),
                new ItemStack(ModIngredients.STAR_CHART_DRAWN_WITH_SPIRIT_BLOOD.get()),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.SISKUN_DEMON_WORM.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SECRETS_SORCERER_POTION.get(),
                new ItemStack(ModIngredients.SISKUN_DEMON_WORM_NEST.get()),
                new ItemStack(ModIngredients.ANKH_GATE_ASHES.get()),
                new ItemStack(ModIngredients.WALNUT.get()),
                new ItemStack(ModIngredients.LAKE_WATER_FRESHLY_REFLECTING_THE_COSMOS.get()),
                new ItemStack(ModIngredients.GOLDEN_PHOENIX_EYE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.WANDERER_POTION.get(),
                new ItemStack(ModIngredients.WANDERING_SKIN_PUS.get()),
                new ItemStack(ModIngredients.MIST_WATCHER_EYELID.get()),
                new ItemStack(ModIngredients.WHITE_MUSTARD_SEEDS.get()),
                new ItemStack(ModIngredients.STAR_WATER_FROM_THE_RITUAL_SITE.get()),
                new ItemStack(ModIngredients.WANDERING_SKIN.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PLANESWALKER_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.ACTIVE_VOID.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.KEY_OF_STARS_POTION.get(),
                new ItemStack(ModIngredients.COMPASS_LINKED_TO_THE_ASTRAL_WORLD.get()),
                new ItemStack(ModIngredients.WANDERING_ASTEROID_CORE.get()),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(ModIngredients.ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES.get()),
                new ItemStack(ModIngredients.ILLUSION_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CRIMINAL_POTION.get(),
                new ItemStack(ModIngredients.NAIL_FRAGMENTS_FROM_DIFFERENT_PEOPLE.get()),
                new ItemStack(ModIngredients.FLOWER_GROWN_FROM_A_CORPSE.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(ModIngredients.JULY_WINE_JUICE.get()),
                new ItemStack(ModIngredients.ANCIENT_WRAITH_DUST.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.UNWINGED_ANGEL_POTION.get(),
                new ItemStack(ModIngredients.DEEP_GRAINED_WALNUT.get()),
                new ItemStack(ModIngredients.LIQUOR.get()),
                new ItemStack(ModIngredients.FRAGRANCE_HORNET_GRASS.get()),
                new ItemStack(ModIngredients.DISTILLED_LIQUOR.get()),
                new ItemStack(ModIngredients.BIZARRO_BANE_EYE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SERIAL_KILLER_POTION.get(),
                new ItemStack(ModIngredients.MAGMA_PYROXENE_POWDER.get()),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.CLEMATIS_POWDER.get()),
                new ItemStack(ModIngredients.MOONSTONE.get()),
                new ItemStack(ModIngredients.GOLDEN_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DEVIL_POTION.get(),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.RADIANCE_SPIRIT_PACT_TREE_JUICE.get()),
                new ItemStack(ModIngredients.MERCURY.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.SUN_ORB.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DESIRE_APOSTLE_POTION.get(),
                new ItemStack(ModIngredients.LAVENDER_HYDROSOL.get()),
                new ItemStack(ModIngredients.TRAPPED_GHOST_RESIDUE_POWDER.get()),
                new ItemStack(ModIngredients.MIND_DRAGON_BLOOD_ADULT.get()),
                new ItemStack(ModIngredients.KING_OF_DAWN_ROOSTERS_BLOOD.get()),
                new ItemStack(ModIngredients.ANCIENT_WRAITH_ARTIFACT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DEMON_POTION.get(),
                new ItemStack(ModIngredients.SUN_DIVINE_BIRD_BLOOD.get()),
                new ItemStack(ModIngredients.MAGMA_HEART_POWDER.get()),
                new ItemStack(ModIngredients.FINGERNAIL_SIZED_SELF_MADE_RUBBER_MASK.get()),
                new ItemStack(ModIngredients.SISKUN_DEMON_WORM_NEST.get()),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_HEART.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.BLATHERER_POTION.get(),
                new ItemStack(ModIngredients.REAL_ANCIENT_HISTORICAL_RECORDS.get()),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.STAR_WATER_FROM_THE_RITUAL_SITE.get()),
                new ItemStack(ModIngredients.MIST_WATCHER_EYELID.get()),
                new ItemStack(ModIngredients.MIST_WATCHER_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.BLOODY_ARCHDUKE_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.MIDNIGHT_BEAUTY_FLOWER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.FILTHY_MONARCH_POTION.get(),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_SPECIALTIES.get()),
                new ItemStack(ModIngredients.WANDERING_ASTEROID_CORE.get()),
                new ItemStack(ModIngredients.COMPASS_LINKED_TO_THE_ASTRAL_WORLD.get()),
                new ItemStack(ModIngredients.SOUL_SNARING_BELL_FLOWER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SLEEPLESS_POTION.get(),
                new ItemStack(ModIngredients.FLOWER_GROWN_FROM_A_CORPSE.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_CONTAMINATED_SOIL.get()),
                new ItemStack(ModIngredients.MIDSUMMER_GRASS.get()),
                new ItemStack(ModIngredients.ANCIENT_WELL_WATER.get()),
                new ItemStack(ModIngredients.DREAM_EATING_RAVEN_HEART.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.MIDNIGHT_POET_POTION.get(),
                new ItemStack(ModIngredients.CHESTNUT_SPORE.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_EXTRACT.get()),
                new ItemStack(ModIngredients.ELF_DARK_LEAF_2.get()),
                new ItemStack(ModIngredients.PURE_WHITE_ELF_FLOWERS.get()),
                new ItemStack(ModIngredients.DEEP_SLEEPER_SKULL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.NIGHTMARE_POTION.get(),
                new ItemStack(ModIngredients.MOONSTONE.get()),
                new ItemStack(ModIngredients.MIRROR_DRAGON_BLOOD.get()),
                new ItemStack(ModIngredients.SELF_DESIGNED_CIPHER.get()),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.SOURCE_OF_MAD_DREAMS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SOUL_ASSURER_POTION.get(),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.SEAWATER_LAKE_WATER_GLACIER_WATER.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.GOAT_HORNED_BLACKFISH_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SPIRIT_WARLOCK_POTION.get(),
                new ItemStack(ModIngredients.STAR_CHART_DRAWN_WITH_SPIRIT_BLOOD.get()),
                new ItemStack(ModIngredients.LEMON_BALM_POWDER.get()),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_BLOOD.get()),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.TREE_OF_ELDERS_FRUIT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.NIGHTWATCHER_POTION.get(),
                new ItemStack(ModIngredients.DROP_OF_TEARS.get()),
                new ItemStack(ModIngredients.ANKH_GATE_ASHES.get()),
                new ItemStack(ModIngredients.BIZARRO_BANE_BLOOD.get()),
                new ItemStack(ModIngredients.HOLY_BRILLIANCE_ROCK_LIQUID.get()),
                new ItemStack(ModIngredients.ILLUSORY_CHIME_TREES_FRUIT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.HORROR_BISHOP_POTION.get(),
                new ItemStack(ModIngredients.REAL_ANCIENT_HISTORICAL_RECORDS.get()),
                new ItemStack(ModIngredients.WHITE_FROST_CRYSTAL_OF_DEMONIC_WOLF_OF_FOG.get()),
                new ItemStack(ModIngredients.SWAMP_GIANT_BLOOD.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_WRAITHS_WHO_DIED_DUE_TO_THE_ADVANCER_DECEIT.get()),
                new ItemStack(ModIngredients.BLACK_FEATHER_OF_MONSTER_BIRD.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SERVANT_OF_CONCEALMENT_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.AGATE_PEACOCK_EGG.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.KNIGHT_OF_MISFORTUNE_POTION.get(),
                new ItemStack(ModIngredients.ANCIENT_CLOCK.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_SPECIALTIES.get()),
                new ItemStack(ModIngredients.COMPASS_LINKED_TO_THE_ASTRAL_WORLD.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.SHADOW_LIZARD_SCALES.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.ARBITER.get(),
                new ItemStack(ModIngredients.GOAT_BEARD_HYDROSOL.get()),
                new ItemStack(ModIngredients.FLOWER_GROWN_FROM_A_CORPSE.get()),
                new ItemStack(ModIngredients.JULY_WINE_JUICE.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(ModIngredients.SILVER_WAR_BEAR_RIGHT_PALM.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SHERIFF.get(),
                new ItemStack(ModIngredients.LIQUOR.get()),
                new ItemStack(ModIngredients.ANOTHER_TEARS.get()),
                new ItemStack(ModIngredients.CHESTNUT_SPORE.get()),
                new ItemStack(ModIngredients.LAPIS_LAZULI.get()),
                new ItemStack(ModIngredients.TERROR_DEMON_WORM_EYES.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.INTERROGATOR.get(),
                new ItemStack(ModIngredients.SELF_DESIGNED_CIPHER.get()),
                new ItemStack(ModIngredients.CLEMATIS_POWDER.get()),
                new ItemStack(ModIngredients.SOUL_BEWITCHING_INSECT_COLONY_MUCUS.get()),
                new ItemStack(ModIngredients.WITHERED_GRAPEVINE.get()),
                new ItemStack(ModIngredients.FLASH_PATTERNED_BLACK_SNAKE_HORN.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.JUDGE.get(),
                new ItemStack(ModIngredients.LARCH_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.RADIANCE_SPIRIT_PACT_TREE_JUICE.get()),
                new ItemStack(ModIngredients.DUST_OF_A_LAKE_SPIRIT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DISCIPLINARY_PALADIN.get(),
                new ItemStack(ModIngredients.TRAPPED_GHOST_RESIDUE_POWDER.get()),
                new ItemStack(ModIngredients.LEMON_BALM_POWDER.get()),
                new ItemStack(ModIngredients.DE_SPIRITUALIZED_DEPRAVED_BREATH.get()),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_BLOOD.get()),
                new ItemStack(ModIngredients.SILVER_FOUR_LEAF_CLOVER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.IMPERATIVE_MAGE.get(),
                new ItemStack(ModIngredients.ANKH_GATE_ASHES.get()),
                new ItemStack(ModIngredients.FINGERNAIL_SIZED_SELF_MADE_RUBBER_MASK.get()),
                new ItemStack(ModIngredients.IMPRISONED_SOUL.get()),
                new ItemStack(ModIngredients.AMETHYST.get()),
                new ItemStack(ModIngredients.CRYSTAL_OF_A_YOUNG_UNICORN.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CHAOS_HUNTER.get(),
                new ItemStack(ModIngredients.WHITE_FROST_CRYSTAL_OF_DEMONIC_WOLF_OF_FOG.get()),
                new ItemStack(ModIngredients.WANDERING_SKIN_PUS.get()),
                new ItemStack(ModIngredients.WHITE_MUSTARD_SEEDS.get()),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.DIVINE_BLESSED_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.BALANCER.get(),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.CRYSTAL_CORE_OF_THE_CALAMITY_PHOENIX.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.HAND_OF_ORDER.get(),
                new ItemStack(ModIngredients.WANDERING_ASTEROID_CORE.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(ModIngredients.ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES.get()),
                new ItemStack(ModIngredients.HEART_OF_A_BLUE_SPOTTED_FIREBIRD.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PLANTER_POTION.get(),
                new ItemStack(ModIngredients.MIDSUMMER_GRASS.get()),
                new ItemStack(ModIngredients.POPLAR_TREE_LEAF_POWDER.get()),
                new ItemStack(ModIngredients.FLOWER_GROWN_FROM_A_CORPSE.get()),
                new ItemStack(ModIngredients.JULY_WINE_JUICE.get()),
                new ItemStack(ModIngredients.FLOWER_OF_GOOD_FORTUNE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DOCTOR_POTION.get(),
                new ItemStack(ModIngredients.ELF_DARK_LEAF_2.get()),
                new ItemStack(ModIngredients.CHESTNUT_SPORE.get()),
                new ItemStack(ModIngredients.WHITE_CHESTNUT_BALM.get()),
                new ItemStack(ModIngredients.GRAPEVINE_POWDER.get()),
                new ItemStack(ModIngredients.BRAIN_OF_A_FOUR_EARED_GIANT_APE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.HARVEST_PRIEST_POTION.get(),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.SELF_DESIGNED_CIPHER.get()),
                new ItemStack(ModIngredients.FIRE_SALAMANDER_BLOOD.get()),
                new ItemStack(ModIngredients.MIRROR_DRAGON_BLOOD.get()),
                new ItemStack(ModIngredients.CLOTH_WRAPPED_PERSONS_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.BIOLOGIST_POTION.get(),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.LARCH_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.RADIANCE_SPIRIT_PACT_TREE_JUICE.get()),
                new ItemStack(ModIngredients.SEAWATER_LAKE_WATER_GLACIER_WATER.get()),
                new ItemStack(ModIngredients.DEATH_CALLING_CROWS_EYEBALL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DRUID_POTION.get(),
                new ItemStack(ModIngredients.FINGERED_CITRON_JUICE.get()),
                new ItemStack(ModIngredients.TRAPPED_GHOST_RESIDUE_POWDER.get()),
                new ItemStack(ModIngredients.CELESTINE.get()),
                new ItemStack(ModIngredients.ANOTHER_TEARS_SHED_DUE_TO_SHATTERED_IDEALS.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CLASSICAL_ALCHEMIST_POTION.get(),
                new ItemStack(ModIngredients.SISKUN_DEMON_WORM_NEST.get()),
                new ItemStack(ModIngredients.LAKE_WATER_FRESHLY_REFLECTING_THE_COSMOS.get()),
                new ItemStack(ModIngredients.TREE_MENTOR_GOLDEN_LEAF.get()),
                new ItemStack(ModIngredients.GOLDEN_GRAPEVINES.get()),
                new ItemStack(ModIngredients.SOUL_OF_A_PALE_LICH.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PALLBEARER_POTION.get(),
                new ItemStack(ModIngredients.SPIRIT_WORLD_WRAITHS_WHO_DIED_DUE_TO_THE_ADVANCER_DECEIT.get()),
                new ItemStack(ModIngredients.WANDERING_SKIN_PUS.get()),
                new ItemStack(ModIngredients.SWAMP_GIANT_BLOOD.get()),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.CRYSTAL_CORE_OF_THOUSAND_ARMED_WRAITH.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DESOLATE_MATRIARCH_POTION.get(),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.BRAIN_OF_AN_ADULT_FEATHERED_SERPENT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.NATUREWALKER_POTION.get(),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.ANCIENT_CLOCK.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(ModIngredients.MOLT_OF_THE_IMMORTAL_CICADA.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.ASSASSIN_POTION.get(),
                new ItemStack(ModIngredients.SPIRIT_WORLD_CONTAMINATED_SOIL.get()),
                new ItemStack(ModIngredients.GOAT_BEARD_HYDROSOL.get()),
                new ItemStack(ModIngredients.VERBENA_POWDER.get()),
                new ItemStack(ModIngredients.SAPPHIRE.get()),
                new ItemStack(ModIngredients.PURIFIED_WATER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.INSTIGATOR_POTION.get(),
                new ItemStack(ModIngredients.STRING_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.FRAGRANCE_HORNET_GRASS.get()),
                new ItemStack(ModIngredients.LAPIS_LAZULI.get()),
                new ItemStack(ModIngredients.CHESTNUT_SPORE.get()),
                new ItemStack(ModIngredients.TORNAPPLE_JIMSONWEED_JUICE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.WITCH_POTION.get(),
                new ItemStack(ModIngredients.MOONSTONE.get()),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.CLEMATIS_POWDER.get()),
                new ItemStack(ModIngredients.FINGERED_CITRON_POWDER.get()),
                new ItemStack(ModIngredients.ABYSS_DEMONIC_FISH_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_PLEASURE_POTION.get(),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.CITRINE.get()),
                new ItemStack(ModIngredients.AQUA_FERN_JUICE.get()),
                new ItemStack(ModIngredients.FRESHLY_STOLEN_WINE.get()),
                new ItemStack(ModIngredients.SUCCUBUS_EYES.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_AFFLICTION_POTION.get(),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_BLOOD.get()),
                new ItemStack(ModIngredients.FINGERED_CITRON_JUICE.get()),
                new ItemStack(ModIngredients.MIND_DRAGON_BLOOD_ADULT.get()),
                new ItemStack(ModIngredients.DE_SPIRITUALIZED_DEPRAVED_BREATH.get()),
                new ItemStack(ModIngredients.BLACK_JIMSONWEED_JUICE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_DESPAIR_POTION.get(),
                new ItemStack(ModIngredients.AMETHYST.get()),
                new ItemStack(ModIngredients.GOLDEN_GRAPEVINES.get()),
                new ItemStack(ModIngredients.SISKUN_DEMON_WORM_NEST.get()),
                new ItemStack(ModIngredients.FINGERNAIL_SIZED_SELF_MADE_RUBBER_MASK.get()),
                new ItemStack(ModIngredients.DAFFODIL_JUICE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_UNAGING_POTION.get(),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.SWAMP_GIANT_BLOOD.get()),
                new ItemStack(ModIngredients.REAL_ANCIENT_HISTORICAL_RECORDS.get()),
                new ItemStack(ModIngredients.WANDERING_SKIN_PUS.get()),
                new ItemStack(ModIngredients.BLACK_WIDOW_SPIDER_SILK_GLAND.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_CATASTROPHE_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.SUCCUBUS_HAIR_COMPLETE_REMNANTS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_APOCALYPSE_POTION.get(),
                new ItemStack(ModIngredients.WANDERING_ASTEROID_CORE.get()),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(ModIngredients.ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES.get()),
                new ItemStack(ModIngredients.FEYNAPOTTER_FLY_POWDER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SPECTATOR_POTION.get(),
                new ItemStack(ModIngredients.NAIL_FRAGMENTS_FROM_DIFFERENT_PEOPLE.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(ModIngredients.GOAT_BEARD_HYDROSOL.get()),
                new ItemStack(ModIngredients.ELF_DARK_LEAF.get()),
                new ItemStack(ModIngredients.MATURED_MANHAL_FISH_EYEBALL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.TELEPATHIST_POTION.get(),
                new ItemStack(ModIngredients.CHESTNUT_SPORE.get()),
                new ItemStack(ModIngredients.PURE_WHITE_ELF_FLOWERS.get()),
                new ItemStack(ModIngredients.STRING_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.DEEP_GRAINED_WALNUT.get()),
                new ItemStack(ModIngredients.RAINBOW_SALAMANDER_PITUITARY_GLAND.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PSYCHIATRIST_POTION.get(),
                new ItemStack(ModIngredients.MIRROR_DRAGON_BLOOD.get()),
                new ItemStack(ModIngredients.SUN_STAR_EXTRACT.get()),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.SOUL_BEWITCHING_INSECT_COLONY_MUCUS.get()),
                new ItemStack(ModIngredients.TREE_OF_ELDER_FRUIT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.HYPNOTIST_POTION.get(),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.SEAWATER_LAKE_WATER_GLACIER_WATER.get()),
                new ItemStack(ModIngredients.FRESHLY_STOLEN_WINE.get()),
                new ItemStack(ModIngredients.BLACK_HUNTING_GIANT_LIZARD_SPINAL_FLUID.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DREAMWALKER_POTION.get(),
                new ItemStack(ModIngredients.LAVENDER_HYDROSOL.get()),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_BLOOD.get()),
                new ItemStack(ModIngredients.DE_SPIRITUALIZED_DEPRAVED_BREATH.get()),
                new ItemStack(ModIngredients.DREAM_CATCHERS_HEART.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.MANIPULATOR_POTION.get(),
                new ItemStack(ModIngredients.TREE_MENTOR_GOLDEN_LEAF.get()),
                new ItemStack(ModIngredients.MUTATED_FINGERED_CITRON_JUICE.get()),
                new ItemStack(ModIngredients.ANKH_GATE_ASHES.get()),
                new ItemStack(ModIngredients.BIZARRO_BANE_BLOOD.get()),
                new ItemStack(ModIngredients.DRAGON_TOOTH_GRASS_POWDER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DREAM_WEAVER_POTION.get(),
                new ItemStack(ModIngredients.WANDERING_SKIN_PUS.get()),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.WHITE_MUSTARD_SEEDS.get()),
                new ItemStack(ModIngredients.WATER_VIOLET_HYDROSOL.get()),
                new ItemStack(ModIngredients.AUTUMN_CROCUS_ESSENCE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DISCERNER_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.COW_TEETH_PAEONOL_POWDER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.AUTHOR_POTION.get(),
                new ItemStack(ModIngredients.ANCIENT_CLOCK.get()),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_SPECIALTIES.get()),
                new ItemStack(ModIngredients.ELF_FLOWER_PETALS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.MARAUDER_POTION.get(),
                new ItemStack(ModIngredients.ANOTHER_BLOOD.get()),
                new ItemStack(ModIngredients.NAIL_FRAGMENTS_FROM_DIFFERENT_PEOPLE.get()),
                new ItemStack(ModIngredients.SAPPHIRE.get()),
                new ItemStack(ModIngredients.VERBENA_POWDER.get()),
                new ItemStack(ModIngredients.BLOOD_SPECKLED_BLACK_MOSQUITO.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SWINDLER_POTION.get(),
                new ItemStack(ModIngredients.ANOTHER_TEARS.get()),
                new ItemStack(ModIngredients.LAPIS_LAZULI.get()),
                new ItemStack(ModIngredients.WHITE_CHESTNUT_BALM.get()),
                new ItemStack(ModIngredients.GRAPEVINE_POWDER.get()),
                new ItemStack(ModIngredients.HUMAN_FACED_PITCHER_PLANT.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CRYPTOLOGITS_POTION.get(),
                new ItemStack(ModIngredients.SPHINX_BLOOD.get()),
                new ItemStack(ModIngredients.SOUL_BEWITCHING_INSECT_COLONY_MUCUS.get()),
                new ItemStack(ModIngredients.MOONSTONE.get()),
                new ItemStack(ModIngredients.WILD_ROSE.get()),
                new ItemStack(ModIngredients.SPHINX_BRAIN.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PROMETHEUS_POTION.get(),
                new ItemStack(ModIngredients.FRESHLY_STOLEN_WINE.get()),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.CITRINE.get()),
                new ItemStack(ModIngredients.LARCH_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.CRYSTAL_THREADWORM.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DREAM_STEALER_POTION.get(),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_BLOOD.get()),
                new ItemStack(ModIngredients.DE_SPIRITUALIZED_DEPRAVED_BREATH.get()),
                new ItemStack(ModIngredients.CELESTINE.get()),
                new ItemStack(ModIngredients.LAVENDER_HYDROSOL.get()),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_HEART.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PARASITE_POTION.get(),
                new ItemStack(ModIngredients.SOUL_SNATCHER_BLOOD.get()),
                new ItemStack(ModIngredients.PUPPET_EVIL_INSECT_SECRETIONS.get()),
                new ItemStack(ModIngredients.AMETHYST.get()),
                new ItemStack(ModIngredients.IMPRISONED_SOUL.get()),
                new ItemStack(ModIngredients.SOUL_SNATCHER_POSTMORTEM_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.MENTOR_OF_DECEIT_POTION.get(),
                new ItemStack(ModIngredients.SWAMP_GIANT_BLOOD.get()),
                new ItemStack(ModIngredients.BLASPHEMOUS_PRIEST_FUNGAL_SHROUD.get()),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.WATER_VIOLET_HYDROSOL.get()),
                new ItemStack(ModIngredients.SWAMP_GIANT_PALMS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.TROJAN_HORSE_OF_DESTINY_POTION.get(),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.GIANT_KING_COURT_PRIEST_POSTMORTEM_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.WORM_OF_TIME_POTION.get(),
                new ItemStack(ModIngredients.ANCIENT_CLOCK.get()),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES.get()),
                new ItemStack(ModIngredients.CANDLE_DEVOURER_CORE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.MONSTER_POTION.get(),
                new ItemStack(ModIngredients.POPLAR_TREE_LEAF_POWDER.get()),
                new ItemStack(ModIngredients.GOAT_BEARD_HYDROSOL.get()),
                new ItemStack(ModIngredients.VERBENA_POWDER.get()),
                new ItemStack(ModIngredients.ELF_DARK_LEAF.get()),
                new ItemStack(ModIngredients.STAR_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.ROBOT_POTION.get(),
                new ItemStack(ModIngredients.DISTILLED_LIQUOR.get()),
                new ItemStack(ModIngredients.ACONITE_JUICE.get()),
                new ItemStack(ModIngredients.LIQUOR.get()),
                new ItemStack(ModIngredients.GOLDEN_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.NIGHT_VANILLA_LIQUIDS.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.LUCKY_ONE_POTION.get(),
                new ItemStack(ModIngredients.SOUL_BEWITCHING_INSECT_COLONY_MUCUS.get()),
                new ItemStack(ModIngredients.CLEMATIS_POWDER.get()),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.GOLD_MINT_LEAVES.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CALAMITY_PRIEST_POTION.get(),
                new ItemStack(ModIngredients.LARCH_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.RADIANCE_SPIRIT_PACT_TREE_JUICE.get()),
                new ItemStack(ModIngredients.AQUA_FERN_JUICE.get()),
                new ItemStack(ModIngredients.SEAWATER_LAKE_WATER_GLACIER_WATER.get()),
                new ItemStack(ModIngredients.POISON_HEMLOCK.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.WINNER_POTION.get(),
                new ItemStack(ModIngredients.SIX_WINGED_GARGOYLE_EYES.get()),
                new ItemStack(ModIngredients.STAR_CHART_DRAWN_WITH_SPIRIT_BLOOD.get()),
                new ItemStack(ModIngredients.LEMON_BALM_POWDER.get()),
                new ItemStack(ModIngredients.CELESTINE.get()),
                new ItemStack(ModIngredients.DRAGON_BLOOD_GRASS_POWDER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.MISFORTUNE_MAGE_POTION.get(),
                new ItemStack(ModIngredients.DROP_OF_TEARS.get()),
                new ItemStack(ModIngredients.SOUL_SNATCHER_BLOOD.get()),
                new ItemStack(ModIngredients.TREE_MENTOR_GOLDEN_LEAF.get()),
                new ItemStack(ModIngredients.MAGMA_HEART_POWDER.get()),
                new ItemStack(ModIngredients.HUMAN_FACED_ROSE_COMPLETE_STALK.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CHAOSWALKER_POTION.get(),
                new ItemStack(ModIngredients.SPIRIT_WORLD_WRAITHS_WHO_DIED_DUE_TO_THE_ADVANCER_DECEIT.get()),
                new ItemStack(ModIngredients.REAL_ANCIENT_HISTORICAL_RECORDS.get()),
                new ItemStack(ModIngredients.HOUND_OF_FULGRIM_BLOOD.get()),
                new ItemStack(ModIngredients.WHITE_FROST_CRYSTAL_OF_DEMONIC_WOLF_OF_FOG.get()),
                new ItemStack(ModIngredients.BLACK_RIMMED_SUNFLOWER_POWDER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SOOTHSAYER_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.GOLDEN_CLOAK_GRASS_POWDER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SNAKE_OF_MERCURY_POTION.get(),
                new ItemStack(ModIngredients.COMPASS_LINKED_TO_THE_ASTRAL_WORLD.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.WANDERING_ASTEROID_CORE.get()),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(ModIngredients.DARK_PATTERNED_BLACK_PANTHER_SPINAL_FLUID.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.CORPSE_COLLECTOR_POTION.get(),
                new ItemStack(ModIngredients.ELF_DARK_LEAF.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(ModIngredients.ANCIENT_WELL_WATER.get()),
                new ItemStack(ModIngredients.ANOTHER_BLOOD.get()),
                new ItemStack(ModIngredients.MIST_TREANT_JUICE.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.GRAVEDIGGER_POTION.get(),
                new ItemStack(ModIngredients.GRAPEVINE_POWDER.get()),
                new ItemStack(ModIngredients.DISTILLED_LIQUOR.get()),
                new ItemStack(ModIngredients.DEEP_GRAINED_WALNUT.get()),
                new ItemStack(ModIngredients.GOLDEN_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.DROPLET_GEM_POWDER.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SPIRIT_MEDIUM_POTION.get(),
                new ItemStack(ModIngredients.DAWN_ROOSTER_BLOOD.get()),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.MIRROR_DRAGON_BLOOD.get()),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.FANTASY_GRASS_ESSENTIAL_OIL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SPIRIT_GUIDE_POTION.get(),
                new ItemStack(ModIngredients.CITRINE.get()),
                new ItemStack(ModIngredients.FRESHLY_STOLEN_WINE.get()),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.TINDER.get()),
                new ItemStack(ModIngredients.HUMAN_SKINED_SHADOW_CHARACTERISTIC.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.GATEKEEPER_POTION.get(),
                new ItemStack(ModIngredients.MIND_DRAGON_BLOOD_ADULT.get()),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_BLOOD.get()),
                new ItemStack(ModIngredients.DE_SPIRITUALIZED_DEPRAVED_BREATH.get()),
                new ItemStack(ModIngredients.LAVENDER_HYDROSOL.get()),
                new ItemStack(ModIngredients.DEEP_SEA_NAGA_HAIR_STRAND.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.UNDYING_POTION.get(),
                new ItemStack(ModIngredients.MAGMA_HEART_POWDER.get()),
                new ItemStack(ModIngredients.PUPPET_EVIL_INSECT_SECRETIONS.get()),
                new ItemStack(ModIngredients.MUTATED_FINGERED_CITRON_JUICE.get()),
                new ItemStack(ModIngredients.SOUL_SNATCHER_BLOOD.get()),
                new ItemStack(ModIngredients.SIX_WINGED_GARGOYLE_CORE_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.FERRYMAN_POTION.get(),
                new ItemStack(ModIngredients.MIST_WATCHER_EYELID.get()),
                new ItemStack(ModIngredients.WHITE_MUSTARD_SEEDS.get()),
                new ItemStack(ModIngredients.WHITE_FROST_CRYSTAL_OF_DEMONIC_WOLF_OF_FOG.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_WRAITHS_WHO_DIED_DUE_TO_THE_ADVANCER_DECEIT.get()),
                new ItemStack(ModIngredients.SPRING_WATER_OF_GOLDEN_SPRING.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.DEATH_CONSUL_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.DRAGO_BARK.get())
        ));
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.PALE_EMPEROR_POTION.get(),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(ModIngredients.WANDERING_ASTEROID_CORE.get()),
                new ItemStack(ModIngredients.COMPASS_LINKED_TO_THE_ASTRAL_WORLD.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_SPECIALTIES.get()),
                new ItemStack(ModIngredients.REMNANT_SPIRITUALITY_OF_ANCIENT_WRAITHS.get())
        ));
    }
}

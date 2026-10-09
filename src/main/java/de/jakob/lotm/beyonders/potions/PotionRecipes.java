package de.jakob.lotm.beyonders.potions;

import de.jakob.lotm.item.ModIngredients;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PotionRecipes {

    public static final Set<PotionRecipe> RECIPES = new HashSet<>();

    public static boolean initialized = false;

    public static void initPotionRecipes() {
        initialized = true;

        // ===== FOOL =====
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SEER_POTION.get(),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.NIGHT_VANILLA_LIQUIDS.get()),
                new ItemStack(ModIngredients.GOLD_MINT_LEAVES.get()),
                new ItemStack(ModIngredients.POISON_HEMLOCK.get()),
                new ItemStack(ModIngredients.LAVOS_SQUID_BLOOD.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CLOWN_POTION.get(),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.TORNAPPLE_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.BLACK_RIMMED_SUNFLOWER_POWDER.get()),
                new ItemStack(ModIngredients.GOLDEN_CLOAK_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.HORNACIS_GRAY_MOUNTAIN_GOAT_HORN.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MAGICIAN_POTION.get(),
                new ItemStack(ModIngredients.MIST_TREANT_JUICE.get()),
                new ItemStack(ModIngredients.DROPLET_GEM_POWDER.get()),
                new ItemStack(ModIngredients.FANTASY_GRASS_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.ROOT_OF_MIST_TREANT.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.FACELESS_POTION.get(),
                new ItemStack(ModIngredients.THOUSAND_FACED_HUNTER_BLOOD.get()),
                new ItemStack(ModIngredients.BLACK_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.DRAGON_TOOTH_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.DEEP_SEA_NAGA_HAIR_STRAND.get()),
                new ItemStack(ModIngredients.MUTATED_PITUITARY_GLAND_OF_A_THOUSAND_FACED_HUNTER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MARIONETTIST_POTION.get(),
                new ItemStack(ModIngredients.SPRING_WATER_OF_GOLDEN_SPRING.get()),
                new ItemStack(ModIngredients.DRAGO_BARK.get()),
                new ItemStack(ModIngredients.REMNANT_SPIRITUALITY_OF_ANCIENT_WRAITHS.get()),
                new ItemStack(ModIngredients.SIX_WINGED_GARGOYLE_EYES.get()),
                new ItemStack(ModIngredients.DUST_OF_ANCIENT_WRAITHS.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BIZARRO_SORCERER_POTION.get(),
                new ItemStack(ModIngredients.BIZARRO_BANE_BLOOD.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_PLUNDERER_DUST.get()),
                new ItemStack(ModIngredients.RED_HAIR_BIRCH_BARK.get()),
                new ItemStack(ModIngredients.FINGERNAIL_SIZED_SELF_MADE_RUBBER_MASK.get()),
                new ItemStack(ModIngredients.BIZARRO_BANE_MAIN_EYE.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SCHOLAR_OF_YORE_POTION.get(),
                new ItemStack(ModIngredients.HOUND_OF_FULGRIM_BLOOD.get()),
                new ItemStack(ModIngredients.WHITE_FROST_CRYSTAL_OF_DEMONIC_WOLF_OF_FOG.get()),
                new ItemStack(ModIngredients.REAL_ANCIENT_HISTORICAL_RECORDS.get()),
                new ItemStack(ModIngredients.DEMONIC_WOLF_OF_FOG_TRANSFORMED_HEART.get()),
                new ItemStack(ModIngredients.HOUND_OF_FULGRIM_EYES.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MIRACLE_INVOKER_POTION.get(),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.FOG_OF_HISTORY.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_HEART.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ATTENDANT_OF_MYSTERIES_POTION.get(),
                new ItemStack(ModIngredients.SPIRIT_WORLD_SPECIALTIES.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.FOG_OF_HISTORY.get()),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("fool", 1)
                ))
        ));

        // ===== DOOR =====
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.APPRENTICE_POTION.get(),
                new ItemStack(ModIngredients.GOAT_BEARD_HYDROSOL.get()),
                new ItemStack(ModIngredients.ANCIENT_WELL_WATER.get()),
                new ItemStack(ModIngredients.FLOWER_GROWN_FROM_A_CORPSE.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_CONTAMINATED_SOIL.get()),
                new ItemStack(ModIngredients.ILLUSION_CRYSTAL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.TRICKMASTER_POTION.get(),
                new ItemStack(ModIngredients.HORNBEAM_ESSENTIAL_OILS.get()),
                new ItemStack(ModIngredients.STRING_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.GOAT_BEARD_HYDROSOL.get()),
                new ItemStack(ModIngredients.ANCIENT_WELL_WATER.get()),
                new ItemStack(ModIngredients.SPIRIT_EATER_STOMACH_POUCH.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ASTROLOGER_POTION.get(),
                new ItemStack(ModIngredients.CLEMATIS_POWDER.get()),
                new ItemStack(ModIngredients.WITHERED_GRAPEVINE.get()),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.METEORITE_CRYSTAL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SCRIBE_POTION.get(),
                new ItemStack(ModIngredients.DIARY_PAGES_OVER_22_YEARS_OLD.get()),
                new ItemStack(ModIngredients.MERCURY.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.SEAWATER_LAKE_WATER_GLACIER_WATER.get()),
                new ItemStack(ModIngredients.ASMANN_COMPLETE_BRAIN.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.TRAVELER_POTION.get(),
                new ItemStack(ModIngredients.TRAPPED_GHOST_RESIDUE_POWDER.get()),
                new ItemStack(ModIngredients.LEMON_BALM_POWDER.get()),
                new ItemStack(ModIngredients.STAR_CHART_DRAWN_WITH_SPIRIT_BLOOD.get()),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.SISKUN_DEMON_WORM.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SECRETS_SORCERER_POTION.get(),
                new ItemStack(ModIngredients.SISKUN_DEMON_WORM_NEST.get()),
                new ItemStack(ModIngredients.ANKH_GATE_ASHES.get()),
                new ItemStack(ModIngredients.WALNUT.get()),
                new ItemStack(ModIngredients.LAKE_WATER_FRESHLY_REFLECTING_THE_COSMOS.get()),
                new ItemStack(ModIngredients.GOLDEN_PHOENIX_EYE.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WANDERER_POTION.get(),
                new ItemStack(ModIngredients.WANDERING_SKIN_PUS.get()),
                new ItemStack(ModIngredients.MIST_WATCHER_EYELID.get()),
                new ItemStack(ModIngredients.WHITE_MUSTARD_SEEDS.get()),
                new ItemStack(ModIngredients.STAR_WATER_FROM_THE_RITUAL_SITE.get()),
                new ItemStack(ModIngredients.CRYSTAL_LEFT_BY_A_MIST_WATCHER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PLANESWALKER_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(Items.ENDER_PEARL),
                new ItemStack(ModIngredients.ACTIVE_VOID.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.KEY_OF_STARS_POTION.get(),
                new ItemStack(ModIngredients.COMPASS_LINKED_TO_THE_ASTRAL_WORLD.get()),
                new ItemStack(ModIngredients.WANDERING_ASTEROID_CORE.get()),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(Items.CLOCK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("door", 1)
                ))
        ));

        // ===== ERROR =====
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MARAUDER_POTION.get(),
                new ItemStack(ModIngredients.ANOTHER_BLOOD.get()),
                new ItemStack(ModIngredients.NAIL_FRAGMENTS_FROM_DIFFERENT_PEOPLE.get()),
                new ItemStack(ModIngredients.SAPPHIRE.get()),
                new ItemStack(ModIngredients.VERBENA_POWDER.get()),
                new ItemStack(ModIngredients.BLOOD_SPECKLED_BLACK_MOSQUITO.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SWINDLER_POTION.get(),
                new ItemStack(ModIngredients.ANOTHER_TEARS.get()),
                new ItemStack(ModIngredients.LAPIS_LAZULI.get()),
                new ItemStack(ModIngredients.WHITE_CHESTNUT_BALM.get()),
                new ItemStack(ModIngredients.ANOTHER_BLOOD.get()),
                new ItemStack(ModIngredients.HUMAN_FACED_PITCHER_PLANT.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CRYPTOLOGIST_POTION.get(),
                new ItemStack(ModIngredients.SPHINX_BLOOD.get()),
                new ItemStack(ModIngredients.SOUL_BEWITCHING_INSECT_COLONY_MUCUS.get()),
                new ItemStack(ModIngredients.MOONSTONE.get()),
                new ItemStack(ModIngredients.WILD_ROSE.get()),
                new ItemStack(ModIngredients.SPHINX_BRAIN.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PROMETHEUS_POTION.get(),
                new ItemStack(ModIngredients.FRESHLY_STOLEN_WINE.get()),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.CITRINE.get()),
                new ItemStack(ModIngredients.LARCH_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.CRYSTAL_THREADWORM.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DREAM_STEALER_POTION.get(),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_BLOOD.get()),
                new ItemStack(ModIngredients.DE_SPIRITUALIZED_DEPRAVED_BREATH.get()),
                new ItemStack(ModIngredients.CELESTINE.get()),
                new ItemStack(ModIngredients.LAVENDER_HYDROSOL.get()),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_HEART.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PARASITE_POTION.get(),
                new ItemStack(ModIngredients.SOUL_SNATCHER_BLOOD.get()),
                new ItemStack(ModIngredients.PUPPET_EVIL_INSECT_SECRETIONS.get()),
                new ItemStack(ModIngredients.AMETHYST.get()),
                new ItemStack(ModIngredients.IMPRISONED_SOUL.get()),
                new ItemStack(ModIngredients.SOUL_SNATCHER_POSTMORTEM_CRYSTAL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MENTOR_OF_DECEIT_POTION.get(),
                new ItemStack(ModIngredients.SWAMP_GIANT_BLOOD.get()),
                new ItemStack(ModIngredients.BLASPHEMOUS_PRIEST_FUNGAL_SHROUD.get()),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.WATER_VIOLET_HYDROSOL.get()),
                new ItemStack(ModIngredients.SWAMP_GIANT_PALMS.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.TROJAN_HORSE_OF_DESTINY_POTION.get(),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.GIANT_KING_COURT_PRIEST_POSTMORTEM_CRYSTAL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WORM_OF_TIME_POTION.get(),
                new ItemStack(ModIngredients.ANCIENT_CLOCK.get()),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(Items.CLOCK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("error", 1)
                ))
        ));

        // ===== RED PRIEST =====
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.HUNTER_POTION.get(),
                new ItemStack(ModIngredients.RED_WINE.get()),
                new ItemStack(ModIngredients.RED_CHESTNUT_FLOWER.get()),
                new ItemStack(ModIngredients.POPLAR_TREE_LEAF_POWDER.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(ModIngredients.REDCROWN_BALSAM_POWDER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PROVOKER_POTION.get(),
                new ItemStack(ModIngredients.DISTILLED_LIQUOR.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_EXTRACT.get()),
                new ItemStack(ModIngredients.GRAPEVINE_POWDER.get()),
                new ItemStack(ModIngredients.HORNBEAM_ESSENTIAL_OILS.get()),
                new ItemStack(ModIngredients.REDCROWN_BALSAM_POWDER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PYROMANIAC_POTION.get(),
                new ItemStack(ModIngredients.FIRE_SALAMANDER_BLOOD.get()),
                new ItemStack(ModIngredients.MAGMA_PYROXENE_POWDER.get()),
                new ItemStack(ModIngredients.SUN_STAR_EXTRACT.get()),
                new ItemStack(ModIngredients.RED_WINE.get()),
                new ItemStack(ModIngredients.FIRE_SALAMANDER_GLAND.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CONSPIRER_POTION.get(),
                new ItemStack(ModIngredients.HORNBEAM_ESSENTIAL_OILS.get()),
                new ItemStack(ModIngredients.GRAPEVINE_POWDER.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(ModIngredients.RED_WINE.get()),
                new ItemStack(ModIngredients.SPHINX_BRAIN.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.REAPER_POTION.get(),
                new ItemStack(ModIngredients.FIRE_SALAMANDER_BLOOD.get()),
                new ItemStack(ModIngredients.MAGMA_PYROXENE_POWDER.get()),
                new ItemStack(ModIngredients.SUN_STAR_EXTRACT.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_EXTRACT.get()),
                new ItemStack(ModIngredients.BLACK_HUNTING_SPIDER_COMPOSITE_EYES.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.IRON_BLOODED_KNIGHT_POTION.get(),
                new ItemStack(ModIngredients.MAGMA_PYROXENE_POWDER.get()),
                new ItemStack(ModIngredients.REDCROWN_BALSAM_POWDER.get()),
                new ItemStack(ModIngredients.DISTILLED_LIQUOR.get()),
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(ModIngredients.MAGMA_GIANT_CORE.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WAR_BISHOP_POTION.get(),
                new ItemStack(ModIngredients.MAGMA_PYROXENE_POWDER.get()),
                new ItemStack(ModIngredients.SUN_STAR_EXTRACT.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_EXTRACT.get()),
                new ItemStack(Items.BLAZE_POWDER),
                new ItemStack(ModIngredients.WAR_COMET_CORE.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WEATHER_WARLOCK_POTION.get(),
                new ItemStack(ModIngredients.MAGMA_PYROXENE_POWDER.get()),
                new ItemStack(ModIngredients.RED_WINE.get()),
                new ItemStack(ModIngredients.SUN_STAR_EXTRACT.get()),
                new ItemStack(Items.LIGHTNING_ROD),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("red_priest", 2)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CONQUEROR_POTION.get(),
                new ItemStack(ModIngredients.RED_WINE.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_EXTRACT.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("red_priest", 1)
                ))
        ));

        // ===== DEMONESS =====
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ASSASSIN_POTION.get(),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.TORNAPPLE_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.BLACK_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.DAFFODIL_JUICE.get()),
                new ItemStack(ModIngredients.BLACK_FEATHER_OF_MONSTER_BIRD.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.INSTIGATOR_POTION.get(),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.TORNAPPLE_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.BLACK_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.DAFFODIL_JUICE.get()),
                new ItemStack(ModIngredients.ABYSS_DEMONIC_FISH_BLOOD.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WITCH_POTION.get(),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.TORNAPPLE_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.BLACK_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.DAFFODIL_JUICE.get()),
                new ItemStack(ModIngredients.AGATE_PEACOCK_EGG.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_PLEASURE_POTION.get(),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.BLACK_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.SUCCUBUS_HAIR_COMPLETE_REMNANTS.get()),
                new ItemStack(ModIngredients.FEYNAPOTTER_FLY_POWDER.get()),
                new ItemStack(ModIngredients.SUCCUBUS_EYES.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_AFFLICTION_POTION.get(),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.BLACK_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.SUCCUBUS_HAIR_COMPLETE_REMNANTS.get()),
                new ItemStack(ModIngredients.FEYNAPOTTER_FLY_POWDER.get()),
                new ItemStack(ModIngredients.BLACK_WIDOW_SPIDER_SILK_GLAND.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_DESPAIR_POTION.get(),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.DAFFODIL_JUICE.get()),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.SHADOW_LIZARD_SCALES.get()),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("demoness", 4)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_UNAGING_POTION.get(),
                new ItemStack(ModIngredients.DAFFODIL_JUICE.get()),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.SHADOW_LIZARD_SCALES.get()),
                new ItemStack(Items.CLOCK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("demoness", 3)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_CATASTROPHE_POTION.get(),
                new ItemStack(ModIngredients.DAFFODIL_JUICE.get()),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.SHADOW_LIZARD_SCALES.get()),
                new ItemStack(Items.TINTED_GLASS),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("demoness", 2)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_APOCALYPSE_POTION.get(),
                new ItemStack(ModIngredients.DAFFODIL_JUICE.get()),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.SHADOW_LIZARD_SCALES.get()),
                new ItemStack(Items.WITHER_SKELETON_SKULL),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("demoness", 1)
                ))
        ));

        // ===== DEATH =====

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CORPSE_COLLECTOR_POTION.get(),
                new ItemStack(ModIngredients.DEEP_GRAINED_WALNUT.get()),
                new ItemStack(ModIngredients.FRAGRANCE_HORNET_GRASS.get()),
                new ItemStack(ModIngredients.SOAKING_POPLAR_BARK_EXTRACTED.get()),
                new ItemStack(Items.BONE),
                new ItemStack(ModIngredients.CLOTH_WRAPPED_PERSONS_CRYSTAL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.GRAVEDIGGER_POTION.get(),
                new ItemStack(ModIngredients.LIQUOR.get()),
                new ItemStack(ModIngredients.GOLDEN_JIMSONWEED_JUICE.get()),
                new ItemStack(ModIngredients.RED_WINE.get()),
                new ItemStack(Items.POPPY),
                new ItemStack(ModIngredients.DEATH_CALLING_CROWS_EYEBALL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SPIRIT_MEDIUM_POTION.get(),
                new ItemStack(ModIngredients.DAFFODIL_JUICE.get()),
                new ItemStack(ModIngredients.SUCCUBUS_HAIR_COMPLETE_REMNANTS.get()),
                new ItemStack(ModIngredients.FEYNAPOTTER_FLY_POWDER.get()),
                new ItemStack(Items.FERMENTED_SPIDER_EYE),
                new ItemStack(ModIngredients.SPIRIT_WORLD_CRYSTAL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SPIRIT_GUIDE_POTION.get(),
                new ItemStack(ModIngredients.MUMMY_ASHES.get()),
                new ItemStack(ModIngredients.GOAT_BEARD_HYDROSOL.get()),
                new ItemStack(ModIngredients.ANCIENT_WELL_WATER.get()),
                new ItemStack(Items.SPIDER_EYE),
                new ItemStack(ModIngredients.SOUL_OF_A_PALE_LICH.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.GATEKEEPER_POTION.get(),
                new ItemStack(ModIngredients.FLOWER_GROWN_FROM_A_CORPSE.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_CONTAMINATED_SOIL.get()),
                new ItemStack(ModIngredients.HORNBEAM_ESSENTIAL_OILS.get()),
                new ItemStack(Items.ROTTEN_FLESH),
                new ItemStack(ModIngredients.CRYSTAL_CORE_OF_THOUSAND_ARMED_WRAITH.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.UNDYING_POTION.get(),
                new ItemStack(ModIngredients.STRING_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.CLEMATIS_POWDER.get()),
                new ItemStack(ModIngredients.WITHERED_GRAPEVINE.get()),
                new ItemStack(Items.INK_SAC),
                new ItemStack(ModIngredients.BRAIN_OF_AN_ADULT_FEATHERED_SERPENT.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.FERRYMAN_POTION.get(),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.DIARY_PAGES_OVER_22_YEARS_OLD.get()),
                new ItemStack(Items.FEATHER),
                new ItemStack(ModIngredients.MOLT_OF_THE_IMMORTAL_CICADA.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEATH_CONSUL_POTION.get(),
                new ItemStack(ModIngredients.MERCURY.get()),
                new ItemStack(ModIngredients.HONEYSUCKLE_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.SEAWATER_LAKE_WATER_GLACIER_WATER.get()),
                new ItemStack(Items.GLOW_INK_SAC),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("death", 2)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PALE_EMPEROR_POTION.get(),
                new ItemStack(ModIngredients.TRAPPED_GHOST_RESIDUE_POWDER.get()),
                new ItemStack(ModIngredients.LEMON_BALM_POWDER.get()),
                new ItemStack(ModIngredients.STAR_CHART_DRAWN_WITH_SPIRIT_BLOOD.get()),
                new ItemStack(Items.GUNPOWDER),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("death", 1)))
        ));


// ===== DARKNESS =====

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SLEEPLESS_POTION.get(),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.SISKUN_DEMON_WORM_NEST.get()),
                new ItemStack(ModIngredients.ANKH_GATE_ASHES.get()),
                new ItemStack(Items.AMETHYST_SHARD),
                new ItemStack(ModIngredients.MIDNIGHT_BEAUTY_FLOWER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MIDNIGHT_POET_POTION.get(),
                new ItemStack(ModIngredients.WALNUT.get()),
                new ItemStack(ModIngredients.LAKE_WATER_FRESHLY_REFLECTING_THE_COSMOS.get()),
                new ItemStack(ModIngredients.WANDERING_SKIN_PUS.get()),
                new ItemStack(Items.ENDER_PEARL),
                new ItemStack(ModIngredients.SOUL_SNARING_BELL_FLOWER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.NIGHTMARE_POTION.get(),
                new ItemStack(ModIngredients.MIST_WATCHER_EYELID.get()),
                new ItemStack(ModIngredients.WHITE_MUSTARD_SEEDS.get()),
                new ItemStack(ModIngredients.STAR_WATER_FROM_THE_RITUAL_SITE.get()),
                new ItemStack(Items.PHANTOM_MEMBRANE),
                new ItemStack(ModIngredients.DREAM_EATING_RAVEN_HEART.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SOUL_ASSURER_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_SPIRIT.get()),
                new ItemStack(ModIngredients.COMPASS_LINKED_TO_THE_ASTRAL_WORLD.get()),
                new ItemStack(ModIngredients.WANDERING_ASTEROID_CORE.get()),
                new ItemStack(Items.WHEAT),
                new ItemStack(ModIngredients.DEEP_SLEEPER_SKULL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SPIRIT_WARLOCK_POTION.get(),
                new ItemStack(ModIngredients.ONE_FAVORITE_FOOD_OR_DRINK.get()),
                new ItemStack(ModIngredients.ASTRONOMICAL_GEOGRAPHICAL_AND_FOLKLORIC_KNOWLEDGE_OF_THREE_GALAXIES.get()),
                new ItemStack(ModIngredients.ANOTHER_BLOOD.get()),
                new ItemStack(Items.HONEYCOMB),
                new ItemStack(ModIngredients.SOURCE_OF_MAD_DREAMS.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.NIGHTWATCHER_POTION.get(),
                new ItemStack(ModIngredients.NAIL_FRAGMENTS_FROM_DIFFERENT_PEOPLE.get()),
                new ItemStack(ModIngredients.SAPPHIRE.get()),
                new ItemStack(ModIngredients.VERBENA_POWDER.get()),
                new ItemStack(Items.SUGAR),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("darkness", 4)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.HORROR_BISHOP_POTION.get(),
                new ItemStack(ModIngredients.ANOTHER_TEARS.get()),
                new ItemStack(ModIngredients.LAPIS_LAZULI.get()),
                new ItemStack(ModIngredients.WHITE_CHESTNUT_BALM.get()),
                new ItemStack(Items.COAL),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("darkness", 3)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SERVANT_OF_CONCEALMENT_POTION.get(),
                new ItemStack(ModIngredients.SPHINX_BLOOD.get()),
                new ItemStack(ModIngredients.SOUL_BEWITCHING_INSECT_COLONY_MUCUS.get()),
                new ItemStack(ModIngredients.MOONSTONE.get()),
                new ItemStack(Items.REDSTONE),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("darkness", 2)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.KNIGHT_OF_MISFORTUNE_POTION.get(),
                new ItemStack(ModIngredients.WILD_ROSE.get()),
                new ItemStack(ModIngredients.SELF_DESIGNED_CIPHER.get()),
                new ItemStack(ModIngredients.FRESHLY_STOLEN_WINE.get()),
                new ItemStack(Items.LAPIS_LAZULI),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("darkness", 1)))
        ));


// ===== WHEEL OF FORTUNE =====

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MONSTER_POTION.get(),
                new ItemStack(ModIngredients.CLOAKED_SPECTER_RESIDUAL_POWDER.get()),
                new ItemStack(ModIngredients.CITRINE.get()),
                new ItemStack(ModIngredients.LARCH_ESSENTIAL_OIL.get()),
                new ItemStack(Items.STRING),
                new ItemStack(ModIngredients.SILVER_FOUR_LEAF_CLOVER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ROBOT_POTION.get(),
                new ItemStack(ModIngredients.TINDER.get()),
                new ItemStack(ModIngredients.DREAM_EATING_RAT_BLOOD.get()),
                new ItemStack(ModIngredients.DE_SPIRITUALIZED_DEPRAVED_BREATH.get()),
                new ItemStack(Items.LEATHER),
                new ItemStack(ModIngredients.CRYSTAL_OF_A_YOUNG_UNICORN.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.LUCKY_ONE_POTION.get(),
                new ItemStack(ModIngredients.CELESTINE.get()),
                new ItemStack(ModIngredients.LAVENDER_HYDROSOL.get()),
                new ItemStack(ModIngredients.ANOTHER_TEARS_SHED_DUE_TO_SHATTERED_IDEALS.get()),
                new ItemStack(Items.SLIME_BALL),
                new ItemStack(ModIngredients.DIVINE_BLESSED_CRYSTAL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CALAMITY_PRIEST_POTION.get(),
                new ItemStack(ModIngredients.SOUL_SNATCHER_BLOOD.get()),
                new ItemStack(ModIngredients.PUPPET_EVIL_INSECT_SECRETIONS.get()),
                new ItemStack(ModIngredients.AMETHYST.get()),
                new ItemStack(Items.GHAST_TEAR),
                new ItemStack(ModIngredients.CRYSTAL_CORE_OF_THE_CALAMITY_PHOENIX.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WINNER_POTION.get(),
                new ItemStack(ModIngredients.IMPRISONED_SOUL.get()),
                new ItemStack(ModIngredients.SWAMP_GIANT_BLOOD.get()),
                new ItemStack(ModIngredients.BLASPHEMOUS_PRIEST_FUNGAL_SHROUD.get()),
                new ItemStack(Items.BLAZE_POWDER),
                new ItemStack(ModIngredients.HEART_OF_A_BLUE_SPOTTED_FIREBIRD.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MISFORTUNE_MAGE_POTION.get(),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.WATER_VIOLET_HYDROSOL.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_WRAITHS_WHO_DIED_DUE_TO_THE_ADVANCER_DECEIT.get()),
                new ItemStack(Items.GOLD_NUGGET),
                new ItemStack(ModIngredients.FLOWER_OF_GOOD_FORTUNE.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CHAOSWALKER_POTION.get(),
                new ItemStack(ModIngredients.ANCIENT_RIVER_WATER.get()),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(ModIngredients.ANCIENT_CLOCK.get()),
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(ModIngredients.BRAIN_OF_A_FOUR_EARED_GIANT_APE.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SOOTHSAYER_POTION.get(),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(ModIngredients.BEACONS_EXTRACTED_FROM_THE_RIVER_OF_FATE.get()),
                new ItemStack(Items.COPPER_INGOT),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 2)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SNAKE_OF_MERCURY_POTION.get(),
                new ItemStack(ModIngredients.NIGHT_VANILLA_LIQUIDS.get()),
                new ItemStack(ModIngredients.GOLD_MINT_LEAVES.get()),
                new ItemStack(ModIngredients.POISON_HEMLOCK.get()),
                new ItemStack(Items.PRISMARINE_CRYSTALS),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 1)))
        ));


// ===== MOTHER =====

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PLANTER_POTION.get(),
                new ItemStack(ModIngredients.DRAGON_BLOOD_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.TORNAPPLE_JIMSONWEED_JUICE.get()),
                new ItemStack(Items.PRISMARINE_SHARD),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 9)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DOCTOR_POTION.get(),
                new ItemStack(ModIngredients.BLACK_RIMMED_SUNFLOWER_POWDER.get()),
                new ItemStack(ModIngredients.GOLDEN_CLOAK_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.MIST_TREANT_JUICE.get()),
                new ItemStack(Items.NAUTILUS_SHELL),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 8)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.HARVEST_PRIEST_POTION.get(),
                new ItemStack(ModIngredients.DROPLET_GEM_POWDER.get()),
                new ItemStack(ModIngredients.FANTASY_GRASS_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.BLACK_JIMSONWEED_JUICE.get()),
                new ItemStack(Items.TURTLE_SCUTE),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 7)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BIOLOGIST_POTION.get(),
                new ItemStack(ModIngredients.DRAGON_TOOTH_GRASS_POWDER.get()),
                new ItemStack(ModIngredients.DEEP_SEA_NAGA_HAIR_STRAND.get()),
                new ItemStack(ModIngredients.SPRING_WATER_OF_GOLDEN_SPRING.get()),
                new ItemStack(Items.MOSS_BLOCK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 6)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DRUID_POTION.get(),
                new ItemStack(ModIngredients.DRAGO_BARK.get()),
                new ItemStack(ModIngredients.REMNANT_SPIRITUALITY_OF_ANCIENT_WRAITHS.get()),
                new ItemStack(ModIngredients.SIX_WINGED_GARGOYLE_EYES.get()),
                new ItemStack(Items.VINE),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 5)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CLASSICAL_ALCHEMIST_POTION.get(),
                new ItemStack(ModIngredients.BIZARRO_BANE_BLOOD.get()),
                new ItemStack(ModIngredients.RED_HAIR_BIRCH_BARK.get()),
                new ItemStack(ModIngredients.GOLDEN_GRAPEVINES.get()),
                new ItemStack(Items.AZALEA),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 4)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PALLBEARER_POTION.get(),
                new ItemStack(ModIngredients.FINGERNAIL_SIZED_SELF_MADE_RUBBER_MASK.get()),
                new ItemStack(ModIngredients.HOUND_OF_FULGRIM_BLOOD.get()),
                new ItemStack(ModIngredients.WHITE_FROST_CRYSTAL_OF_DEMONIC_WOLF_OF_FOG.get()),
                new ItemStack(Items.BONE_MEAL),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 3)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DESOLATE_MATRIARCH_POTION.get(),
                new ItemStack(ModIngredients.REAL_ANCIENT_HISTORICAL_RECORDS.get()),
                new ItemStack(ModIngredients.DARK_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(Items.MELON_SEEDS),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 2)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.NATUREWALKER_POTION.get(),
                new ItemStack(ModIngredients.WORM_OF_STAR.get()),
                new ItemStack(ModIngredients.SPIRIT_WORLD_SPECIALTIES.get()),
                new ItemStack(ModIngredients.FOG_OF_HISTORY.get()),
                new ItemStack(Items.PUMPKIN_SEEDS),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 1)))
        ));


// ===== ABYSS =====

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CRIMINAL_POTION.get(),
                new ItemStack(ModIngredients.POPLAR_TREE_LEAF_POWDER.get()),
                new ItemStack(ModIngredients.BASIL.get()),
                new ItemStack(ModIngredients.DISTILLED_LIQUOR.get()),
                new ItemStack(Items.WARPED_FUNGUS),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 9)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.UNWINGED_ANGEL_POTION.get(),
                new ItemStack(ModIngredients.HONEYSUCKLE_EXTRACT.get()),
                new ItemStack(ModIngredients.GRAPEVINE_POWDER.get()),
                new ItemStack(ModIngredients.FIRE_SALAMANDER_BLOOD.get()),
                new ItemStack(Items.CRIMSON_FUNGUS),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 8)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SERIAL_KILLER_POTION.get(),
                new ItemStack(ModIngredients.MAGMA_PYROXENE_POWDER.get()),
                new ItemStack(ModIngredients.SUN_STAR_EXTRACT.get()),
                new ItemStack(ModIngredients.MIDSUMMER_GRASS.get()),
                new ItemStack(Items.SOUL_SAND),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 7)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEVIL_POTION.get(),
                new ItemStack(ModIngredients.JULY_WINE_JUICE.get()),
                new ItemStack(ModIngredients.ELF_DARK_LEAF.get()),
                new ItemStack(ModIngredients.ACONITE_JUICE.get()),
                new ItemStack(Items.OBSIDIAN),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 6)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DESIRE_APOSTLE_POTION.get(),
                new ItemStack(ModIngredients.DAWN_ROOSTER_BLOOD.get()),
                new ItemStack(ModIngredients.SUN_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.FINGERED_CITRON_POWDER.get()),
                new ItemStack(Items.CLOCK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 5)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMON_POTION.get(),
                new ItemStack(ModIngredients.SOLIDIFIED_MAGMA.get()),
                new ItemStack(ModIngredients.RADIANCE_SPIRIT_PACT_TREE_JUICE.get()),
                new ItemStack(ModIngredients.AQUA_FERN_JUICE.get()),
                new ItemStack(Items.COMPASS),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 4)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BLATHERER_POTION.get(),
                new ItemStack(ModIngredients.FINGERED_CITRON_JUICE.get()),
                new ItemStack(ModIngredients.KING_OF_DAWN_ROOSTERS_BLOOD.get()),
                new ItemStack(ModIngredients.SUN_DIVINE_BIRD_BLOOD.get()),
                new ItemStack(Items.PAPER),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 3)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BLOODY_ARCHDUKE_POTION.get(),
                new ItemStack(ModIngredients.HOLY_BRILLIANCE_ROCK_LIQUID.get()),
                new ItemStack(ModIngredients.MUTATED_FINGERED_CITRON_JUICE.get()),
                new ItemStack(ModIngredients.MAGMA_HEART_POWDER.get()),
                new ItemStack(Items.BOOK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 2)))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.FILTHY_MONARCH_POTION.get(),
                new ItemStack(ModIngredients.AUTUMN_CROCUS_ESSENCE.get()),
                new ItemStack(ModIngredients.COW_TEETH_PAEONOL_POWDER.get()),
                new ItemStack(ModIngredients.ELF_FLOWER_PETALS.get()),
                new ItemStack(Items.GLASS_BOTTLE),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 1)))
        ));

        // ===== TYRANT =====

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SAILOR_POTION.get(),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.DEEP_SEA_MARLIN_BLOOD.get()),
                new ItemStack(Items.KELP),
                new ItemStack(Items.SEA_PICKLE),
                new ItemStack(ModIngredients.MURLOC_BLADDER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.FOLK_OF_RAGE_POTION.get(),
                new ItemStack(ModIngredients.DISTILLED_LIQUOR.get()),
                new ItemStack(ModIngredients.BLUE_SHADOW_FALCON_FEATHERS.get()),
                new ItemStack(Items.PRISMARINE_SHARD),
                new ItemStack(Items.PHANTOM_MEMBRANE),
                new ItemStack(ModIngredients.DRAGON_EYED_CONDOR_EYEBALL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SEAFARER_POTION.get(),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.DEEP_SEA_MARLIN_BLOOD.get()),
                new ItemStack(Items.PRISMARINE_CRYSTALS),
                new ItemStack(Items.NAUTILUS_SHELL),
                new ItemStack(ModIngredients.ANCIENT_LOGBOOK.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WIND_BLESSED_POTION.get(),
                new ItemStack(ModIngredients.DRAGON_EYED_CONDOR_EYEBALL.get()),
                new ItemStack(ModIngredients.HORNBEAM_ESSENTIAL_OILS.get()),
                new ItemStack(Items.FEATHER),
                new ItemStack(Items.GLASS_BOTTLE),
                new ItemStack(ModIngredients.BLUE_SHADOW_FALCON_CRYSTALLINE_FEATHERS.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.OCEAN_SONGSTER_POTION.get(),
                new ItemStack(ModIngredients.DEEP_SEA_MARLIN_BLOOD.get()),
                new ItemStack(ModIngredients.LAVOS_SQUID_CRYSTALLIZED_BLOOD.get()),
                new ItemStack(Items.NAUTILUS_SHELL),
                new ItemStack(Items.PRISMARINE_CRYSTALS),
                new ItemStack(ModIngredients.SIREN_VOCAL_SAC.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CATACLYSMIC_INTERRER_POTION.get(),
                new ItemStack(ModIngredients.STRONG_LIQUOR.get()),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_BLOOD.get()),
                new ItemStack(Items.HEART_OF_THE_SEA),
                new ItemStack(Items.TUBE_CORAL),
                new ItemStack(ModIngredients.WHALE_OF_PUNISHMENT_STOMACH.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SEA_KING_POTION.get(),
                new ItemStack(ModIngredients.SIREN_VOCAL_SAC.get()),
                new ItemStack(ModIngredients.OCTOPUS_EYEBALLS.get()),
                new ItemStack(Items.PRISMARINE_SHARD),
                new ItemStack(Items.CONDUIT),
                new ItemStack(ModIngredients.KING_OF_GREEN_WINGS_EYE.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CALAMITY_POTION.get(),
                new ItemStack(ModIngredients.WHALE_OF_PUNISHMENT_STOMACH.get()),
                new ItemStack(ModIngredients.SUN_DIVINE_BIRD_BLOOD.get()),
                new ItemStack(Items.HEART_OF_THE_SEA),
                new ItemStack(Items.TRIDENT),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("tyrant", 2)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.THUNDER_GOD_POTION.get(),
                new ItemStack(ModIngredients.KING_OF_GREEN_WINGS_EYE.get()),
                new ItemStack(ModIngredients.WORM_OF_TIME_INGREDIENT.get()),
                new ItemStack(Items.LIGHTNING_ROD),
                new ItemStack(Items.HEART_OF_THE_SEA),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("tyrant", 1)
                ))
        ));


// ===== SUN =====

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BARD_POTION.get(),
                new ItemStack(ModIngredients.RED_WINE.get()),
                new ItemStack(ModIngredients.MIDSUMMER_GRASS.get()),
                new ItemStack(Items.GLOWSTONE_DUST),
                new ItemStack(Items.SUNFLOWER),
                new ItemStack(ModIngredients.CRYSTAL_SUNFLOWER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.LIGHT_SUPPLICANT_POTION.get(),
                new ItemStack(ModIngredients.JULY_WINE_JUICE.get()),
                new ItemStack(ModIngredients.ELF_DARK_LEAF.get()),
                new ItemStack(Items.GLOWSTONE_DUST),
                new ItemStack(Items.GOLD_NUGGET),
                new ItemStack(ModIngredients.POWDER_OF_DAZZLING_SOUL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SOLAR_HIGH_PRIEST_POTION.get(),
                new ItemStack(ModIngredients.SUN_ESSENTIAL_OIL.get()),
                new ItemStack(ModIngredients.FINGERED_CITRON_POWDER.get()),
                new ItemStack(Items.GOLDEN_APPLE),
                new ItemStack(Items.BLAZE_POWDER),
                new ItemStack(ModIngredients.DAWN_ROOSTER_RED_COMB.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.NOTARY_POTION.get(),
                new ItemStack(ModIngredients.RADIANCE_SPIRIT_PACT_TREE_JUICE.get()),
                new ItemStack(ModIngredients.AQUA_FERN_JUICE.get()),
                new ItemStack(Items.GLOWSTONE_DUST),
                new ItemStack(Items.PAPER),
                new ItemStack(ModIngredients.CRYSTALLIZED_ROOTS.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PRIEST_OF_LIGHT_POTION.get(),
                new ItemStack(ModIngredients.ROSEMARY.get()),
                new ItemStack(ModIngredients.FINGERED_CITRON_JUICE.get()),
                new ItemStack(Items.GOLD_BLOCK),
                new ItemStack(Items.BLAZE_POWDER),
                new ItemStack(ModIngredients.PURE_WHITE_BRILLIANT_ROCK.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.UNSHADOWED.get(),
                new ItemStack(ModIngredients.HOLY_BRILLIANCE_ROCK_LIQUID.get()),
                new ItemStack(ModIngredients.MUTATED_FINGERED_CITRON_JUICE.get()),
                new ItemStack(Items.GLOWSTONE),
                new ItemStack(Items.BLAZE_ROD),
                new ItemStack(ModIngredients.GOLDEN_BLOOD.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.JUSTICE_MENTOR_POTION.get(),
                new ItemStack(ModIngredients.SUN_DIVINE_BIRD_BLOOD.get()),
                new ItemStack(ModIngredients.MAGMA_HEART_POWDER.get()),
                new ItemStack(Items.WRITABLE_BOOK),
                new ItemStack(Items.GOLD_BLOCK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("sun", 3)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.LIGHTSEEKER_POTION.get(),
                new ItemStack(ModIngredients.GOLDEN_BLOOD.get()),
                new ItemStack(ModIngredients.SUN_ORB.get()),
                new ItemStack(Items.GLOWSTONE),
                new ItemStack(Items.GOLDEN_APPLE),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("sun", 2)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WHITE_ANGEL_POTION.get(),
                new ItemStack(ModIngredients.HOLY_BRILLIANCE_ROCK_LIQUID.get()),
                new ItemStack(ModIngredients.MAGMA_HEART_POWDER.get()),
                new ItemStack(Items.GOLD_BLOCK),
                new ItemStack(Items.GLOWSTONE),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("sun", 1)
                ))
        ));


// ===== VISIONARY =====

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SPECTATOR_POTION.get(),
                new ItemStack(ModIngredients.PURIFIED_WATER.get()),
                new ItemStack(ModIngredients.AUTUMN_CROCUS_ESSENCE.get()),
                new ItemStack(Items.GLASS_BOTTLE),
                new ItemStack(Items.PINK_PETALS),
                new ItemStack(ModIngredients.MATURED_MANHAL_FISH_EYEBALL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.TELEPATHIST_POTION.get(),
                new ItemStack(ModIngredients.CHESTNUT_SPORE.get()),
                new ItemStack(ModIngredients.PURE_WHITE_ELF_FLOWERS.get()),
                new ItemStack(Items.AMETHYST_SHARD),
                new ItemStack(Items.GLASS_BOTTLE),
                new ItemStack(ModIngredients.RAINBOW_SALAMANDER_PITUITARY_GLAND.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PSYCHIATRIST_POTION.get(),
                new ItemStack(ModIngredients.MIRROR_DRAGON_BLOOD.get()),
                new ItemStack(ModIngredients.ELF_FLOWER_PETALS.get()),
                new ItemStack(Items.FERMENTED_SPIDER_EYE),
                new ItemStack(Items.PAPER),
                new ItemStack(ModIngredients.TREE_OF_ELDERS_FRUIT.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.HYPNOTIST_POTION.get(),
                new ItemStack(ModIngredients.MIRROR_DRAGON_BLOOD.get()),
                new ItemStack(ModIngredients.CHESTNUT_SPORE.get()),
                new ItemStack(Items.CLOCK),
                new ItemStack(Items.GLASS_PANE),
                new ItemStack(ModIngredients.ILLUSORY_CHIME_TREES_FRUIT.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DREAMWALKER_POTION.get(),
                new ItemStack(ModIngredients.MIND_ILLUSION_CRYSTAL.get()),
                new ItemStack(ModIngredients.MIND_DRAGON_BLOOD_ADULT.get()),
                new ItemStack(Items.PHANTOM_MEMBRANE),
                new ItemStack(Items.POPPY),
                new ItemStack(ModIngredients.DREAM_CATCHERS_HEART.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MANIPULATOR_POTION.get(),
                new ItemStack(ModIngredients.MIND_DRAGON_BLOOD_ADULT.get()),
                new ItemStack(ModIngredients.TREE_MENTOR_GOLDEN_LEAF.get()),
                new ItemStack(Items.AMETHYST_SHARD),
                new ItemStack(Items.WRITABLE_BOOK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("visionary", 4)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DREAM_WEAVER_POTION.get(),
                new ItemStack(ModIngredients.DREAM_CATCHERS_HEART.get()),
                new ItemStack(ModIngredients.DROP_OF_TEARS.get()),
                new ItemStack(Items.PHANTOM_MEMBRANE),
                new ItemStack(Items.OAK_SAPLING),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("visionary", 3)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DISCERNER_POTION.get(),
                new ItemStack(ModIngredients.MIND_DRAGON_BLOOD_ELDERLY.get()),
                new ItemStack(ModIngredients.TREE_MENTOR_GOLDEN_LEAF.get()),
                new ItemStack(Items.BLACK_DYE),
                new ItemStack(Items.FEATHER),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("visionary", 2)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.AUTHOR_POTION.get(),
                new ItemStack(ModIngredients.DROP_OF_TEARS.get()),
                new ItemStack(ModIngredients.MIND_ILLUSION_CRYSTAL.get()),
                new ItemStack(Items.WRITABLE_BOOK),
                new ItemStack(Items.COMPASS),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("visionary", 1)
                ))
        ));


// ===== JUSTICIAR =====

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ARBITER.get(),
                new ItemStack(ModIngredients.RED_WINE.get()),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(Items.GREEN_DYE),
                new ItemStack(Items.FEATHER),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("justiciar", 9)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SHERIFF.get(),
                new ItemStack(ModIngredients.TERROR_DEMON_WORM_EYES.get()),
                new ItemStack(ModIngredients.SILVER_WAR_BEAR_RIGHT_PALM.get()),
                new ItemStack(Items.PAPER),
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("justiciar", 8)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.INTERROGATOR.get(),
                new ItemStack(ModIngredients.FLASH_PATTERNED_BLACK_SNAKE_HORN.get()),
                new ItemStack(ModIngredients.DUST_OF_A_LAKE_SPIRIT.get()),
                new ItemStack(Items.LIGHTNING_ROD),
                new ItemStack(Items.BLACK_DYE),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("justiciar", 7)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.JUDGE.get(),
                new ItemStack(ModIngredients.TERROR_DEMON_WORM_EYES.get()),
                new ItemStack(ModIngredients.FLASH_PATTERNED_BLACK_SNAKE_HORN.get()),
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.WRITABLE_BOOK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("justiciar", 6)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DISCIPLINARY_PALADIN.get(),
                new ItemStack(ModIngredients.DUST_OF_A_LAKE_SPIRIT.get()),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(Items.SHIELD),
                new ItemStack(Items.IRON_BLOCK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("justiciar", 5)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.IMPERATIVE_MAGE.get(),
                new ItemStack(ModIngredients.FLASH_PATTERNED_BLACK_SNAKE_HORN.get()),
                new ItemStack(ModIngredients.ANCIENT_CLOCK.get()),
                new ItemStack(Items.WRITABLE_BOOK),
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("justiciar", 4)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CHAOS_HUNTER.get(),
                new ItemStack(ModIngredients.GOLD.get()),
                new ItemStack(ModIngredients.SEGMENT_OF_CONCEALED_FATE.get()),
                new ItemStack(Items.IRON_SWORD),
                new ItemStack(Items.CHAIN),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("justiciar", 3)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BALANCER.get(),
                new ItemStack(ModIngredients.ANCIENT_CLOCK.get()),
                new ItemStack(ModIngredients.FALSE_HISTORY_BELIEVED_TO_BE_TRUE.get()),
                new ItemStack(Items.GOLD_BLOCK),
                new ItemStack(Items.WRITABLE_BOOK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("justiciar", 2)
                ))
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.HAND_OF_ORDER.get(),
                new ItemStack(ModIngredients.USED_ALMANAC.get()),
                new ItemStack(ModIngredients.BEACONS_EXTRACTED_FROM_THE_RIVER_OF_FATE.get()),
                new ItemStack(Items.BOOK),
                new ItemStack(Items.IRON_BLOCK),
                new ItemStack(Objects.requireNonNull(
                        BeyonderCharacteristicItemHandler
                                .selectCharacteristicOfPathwayAndSequence("justiciar", 1)
                ))
        ));
    }

    private record StackKey(Item item, int count) {
        static StackKey of(ItemStack stack) {
            return new StackKey(stack.getItem(), stack.getCount());
        }
    }

    private static Set<StackKey> keysOf(List<ItemStack> stacks) {
        return stacks.stream()
                .filter(s -> s != null && !s.isEmpty())
                .map(StackKey::of)
                .collect(Collectors.toSet());
    }

    @Nullable
    public static BeyonderPotion getByIngredients(List<ItemStack> supplementary, ItemStack main) {
        if (!initialized) {
            initPotionRecipes();
            PotionRecipeItemHandler.initializeRecipes();
        }

        Set<StackKey> given = keysOf(supplementary);

        // 1) Normal lookup
        for (PotionRecipe r : RECIPES) {
            if (areSimilar(r.mainIngredient(), main)
                    && keysOf(r.supplementaryIngredients()).equals(given)) {
                return r.potion();
            }
        }

        // 2) Fallback: main is the characteristic item
        for (PotionRecipe r : RECIPES) {
            if (!keysOf(r.supplementaryIngredients()).equals(given)) {
                continue;
            }
            BeyonderCharacteristicItem characteristicItem = BeyonderCharacteristicItemHandler
                    .selectCharacteristicOfPathwayAndSequence(r.potion().getPathway(), r.potion().getSequence());
            if (characteristicItem != null && areSimilar(main, new ItemStack(characteristicItem))) {
                return r.potion();
            }
        }

        return null;
    }

    public static boolean areSimilar(ItemStack i1, ItemStack i2) {
        if(i1 == null || i2 == null) {
            return false;
        }
        return i1.is(i2.getItem()) && i1.getCount() == i2.getCount();
    }
}

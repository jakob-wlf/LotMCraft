package de.jakob.lotm.potions;

import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PotionRecipes {

    public static final Set<PotionRecipe> RECIPES = new HashSet<>();

    public static boolean initialized = false;

    public static void initPotionRecipes() {
        initialized = true;
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SEER_POTION.get(),
                new ItemStack(Items.SHORT_GRASS, 1),
                new ItemStack(Items.FERMENTED_SPIDER_EYE, 1),
                new ItemStack(ModIngredients.LAVOS_SQUID_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CLOWN_POTION.get(),
                new ItemStack(Items.SUNFLOWER, 1),
                new ItemStack(Items.APPLE, 1),
                new ItemStack(ModIngredients.HORNACIS_GRAY_MOUNTAIN_GOAT_HORN.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MAGICIAN_POTION.get(),
                new ItemStack(Items.PRISMARINE_CRYSTALS, 1),
                new ItemStack(Items.AMETHYST_SHARD, 1),
                new ItemStack(ModIngredients.ROOT_OF_MIST_TREANT.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.FACELESS_POTION.get(),
                new ItemStack(Items.SPIDER_EYE, 1),
                new ItemStack(Items.GLASS, 1),
                new ItemStack(ModIngredients.THOUSAND_FACED_HUNTER_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MARIONETTIST_POTION.get(),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Items.MANGROVE_LOG, 1),
                new ItemStack(ModIngredients.ANCIENT_WRAITH_DUST.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BIZARRO_SORCERER_POTION.get(),
                new ItemStack(Items.GOLD_BLOCK, 1),
                new ItemStack(Items.GOAT_HORN, 1),
                new ItemStack(ModIngredients.BIZARRO_BANE_EYE.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SCHOLAR_OF_YORE_POTION.get(),
                new ItemStack(Items.WRITABLE_BOOK, 1),
                new ItemStack(Items.END_STONE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("fool", 3)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MIRACLE_INVOKER_POTION.get(),
                new ItemStack(Items.NETHER_STAR, 1),
                new ItemStack(Items.PAPER, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("fool", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ATTENDANT_OF_MYSTERIES_POTION.get(),
                new ItemStack(Items.CAKE, 1),
                new ItemStack(Items.CHORUS_FRUIT, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("fool", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.APPRENTICE_POTION.get(),
                new ItemStack(Items.ROSE_BUSH, 1),
                new ItemStack(Items.OAK_DOOR, 1),
                new ItemStack(ModIngredients.ILLUSION_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.TRICKMASTER_POTION.get(),
                new ItemStack(Items.SHORT_GRASS, 1),
                new ItemStack(Items.POPPY, 1),
                new ItemStack(ModIngredients.SPIRIT_EATER_STOMACH_POUCH.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ASTROLOGER_POTION.get(),
                new ItemStack(Items.AMETHYST_SHARD, 1),
                new ItemStack(Items.SPIDER_EYE, 1),
                new ItemStack(ModIngredients.METEORITE_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SCRIBE_POTION.get(),
                new ItemStack(Items.HONEY_BOTTLE, 1),
                new ItemStack(Items.BOOK, 1),
                new ItemStack(ModIngredients.ANCIENT_WRAITH_ARTIFACT.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.TRAVELER_POTION.get(),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(Items.SPIDER_EYE, 1),
                new ItemStack(ModIngredients.SHADOWLESS_DEMONIC_WOLF_HEART.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SECRETS_SORCERER_POTION.get(),
                new ItemStack(Items.BEE_NEST, 1),
                new ItemStack(Items.PRISMARINE_CRYSTALS, 1),
                new ItemStack(ModIngredients.GOLDEN_PHOENIX_EYE.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WANDERER_POTION.get(),
                new ItemStack(Items.END_STONE, 1),
                new ItemStack(Items.CHORUS_FRUIT, 1),
                new ItemStack(ModIngredients.MIST_WATCHER_CRYSTAL.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PLANESWALKER_POTION.get(),
                new ItemStack(Items.NETHER_STAR, 1),
                new ItemStack(Items.IRON_DOOR, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("door", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.KEY_OF_STARS_POTION.get(),
                new ItemStack(Items.COMPASS, 1),
                new ItemStack(Items.NETHER_STAR, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("door", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BARD_POTION.get(),
                new ItemStack(Items.SWEET_BERRIES, 1),
                new ItemStack(Items.MAP, 1),
                new ItemStack(ModIngredients.CRYSTAL_SUNFLOWER.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.LIGHT_SUPPLICANT_POTION.get(),
                new ItemStack(Items.SUNFLOWER, 1),
                new ItemStack(Items.GLOW_BERRIES, 1),
                new ItemStack(ModIngredients.POWDER_OF_DAZZLING_SOUL.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SOLAR_HIGH_PRIEST_POTION.get(),
                new ItemStack(Items.MAGMA_BLOCK, 1),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(ModIngredients.SPIRIT_PACT_TREE_FRUIT.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.NOTARY_POTION.get(),
                new ItemStack(Items.SUNFLOWER, 1),
                new ItemStack(Items.WRITABLE_BOOK, 1),
                new ItemStack(ModIngredients.CRYSTALLIZED_ROOTS.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PRIEST_OF_LIGHT_POTION.get(),
                new ItemStack(Items.BLAZE_POWDER, 1),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(ModIngredients.PURE_WHITE_BRILLIANT_ROCK.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.UNSHADOWED.get(),
                new ItemStack(Items.MAGMA_BLOCK, 1),
                new ItemStack(Items.FEATHER, 1),
                new ItemStack(ModIngredients.GOLDEN_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.JUSTICE_MENTOR_POTION.get(),
                new ItemStack(Items.GOLDEN_SWORD, 1),
                new ItemStack(Items.GOLDEN_CHESTPLATE, 1),
                new ItemStack(ModIngredients.SUN_ORB.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.LIGHTSEEKER_POTION.get(),
                new ItemStack(Items.BOOKSHELF, 1),
                new ItemStack(Items.YELLOW_CANDLE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("sun", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WHITE_ANGEL_POTION.get(),
                new ItemStack(Items.WHITE_CANDLE, 1),
                new ItemStack(Items.PURPUR_BLOCK, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("sun", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SAILOR_POTION.get(),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Items.COMPASS, 1),
                new ItemStack(ModIngredients.MURLOC_BLADDER.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.FOLK_OF_RAGE_POTION.get(),
                new ItemStack(Items.ROTTEN_FLESH, 1),
                new ItemStack(Items.SEA_PICKLE, 1),
                new ItemStack(ModIngredients.DRAGON_EYED_CONDOR_EYEBALL.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SEAFARER_POTION.get(),
                new ItemStack(Items.PRISMARINE_CRYSTALS, 1),
                new ItemStack(Items.TROPICAL_FISH_BUCKET, 1),
                new ItemStack(ModIngredients.ANCIENT_LOGBOOK.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WIND_BLESSED_POTION.get(),
                new ItemStack(Items.FEATHER, 1),
                new ItemStack(Items.PHANTOM_MEMBRANE, 1),
                new ItemStack(ModIngredients.BLUE_SHADOW_FALCON_FEATHERS.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.OCEAN_SONGSTER_POTION.get(),
                new ItemStack(Items.LIGHTNING_ROD, 1),
                new ItemStack(Items.MUSIC_DISC_PIGSTEP, 1),
                new ItemStack(ModIngredients.SIREN_VOCAL_SAC.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CATACLYSMIC_INTERRER_POTION.get(),
                new ItemStack(Items.SEA_LANTERN, 1),
                new ItemStack(Items.OBSIDIAN, 1),
                new ItemStack(ModIngredients.WHALE_OF_PUNISHMENT_STOMACH.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SEA_KING_POTION.get(),
                new ItemStack(Items.PRISMARINE_BRICKS, 1),
                new ItemStack(Items.TROPICAL_FISH_BUCKET, 1),
                new ItemStack(ModIngredients.KING_OF_GREEN_WINGS_EYE.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CALAMITY_POTION.get(),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(Items.PURPUR_BLOCK, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("tyrant", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.THUNDER_GOD_POTION.get(),
                new ItemStack(Items.DRAGON_BREATH, 1),
                new ItemStack(Items.LIGHTNING_ROD, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("tyrant", 1)))
        ));



        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SLEEPLESS_POTION.get(),
                new ItemStack(Items.LILY_OF_THE_VALLEY, 1),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(ModIngredients.MIDNIGHT_BEAUTY_FLOWER.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MIDNIGHT_POET_POTION.get(),
                new ItemStack(Items.SWEET_BERRIES, 1),
                new ItemStack(Items.SPRUCE_LOG, 1),
                new ItemStack(ModIngredients.SOUL_SNARING_BELL_FLOWER.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.NIGHTMARE_POTION.get(),
                new ItemStack(Items.CORNFLOWER, 1),
                new ItemStack(Items.AMETHYST_SHARD, 1),
                new ItemStack(ModIngredients.DREAM_EATING_RAVEN_HEART.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SOUL_ASSURER_POTION.get(),
                new ItemStack(Items.PURPLE_BED, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 1),
                new ItemStack(ModIngredients.DEEP_SLEEPER_SKULL.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SPIRIT_WARLOCK_POTION.get(),
                new ItemStack(Items.GHAST_TEAR, 1),
                new ItemStack(Items.BONE, 1),
                new ItemStack(ModIngredients.SOURCE_OF_MAD_DREAMS.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.NIGHTWATCHER_POTION.get(),
                new ItemStack(Items.END_STONE, 1),
                new ItemStack(Items.BIRCH_LEAVES, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("darkness", 4)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.HORROR_BISHOP_POTION.get(),
                new ItemStack(Items.WITHER_SKELETON_SKULL, 1),
                new ItemStack(Items.COAL_BLOCK, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("darkness", 3)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SERVANT_OF_CONCEALMENT_POTION.get(),
                new ItemStack(Items.GLASS, 1),
                new ItemStack(Items.NETHER_STAR, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("darkness", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.KNIGHT_OF_MISFORTUNE_POTION.get(),
                new ItemStack(Items.DRAGON_BREATH, 1),
                new ItemStack(Items.END_CRYSTAL, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("darkness", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.HUNTER_POTION.get(),
                new ItemStack(Items.POPPY, 1),
                new ItemStack(Items.SPRUCE_LEAVES, 1),
                new ItemStack(ModIngredients.RED_CHESTNUT_FLOWER.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PROVOKER_POTION.get(),
                new ItemStack(Items.SWEET_BERRIES, 1),
                new ItemStack(Items.HONEY_BOTTLE, 1),
                new ItemStack(ModIngredients.REDCROWN_BALSAM_POWDER.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PYROMANIAC_POTION.get(),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(Items.MAGMA_BLOCK, 1),
                new ItemStack(ModIngredients.MAGMA_ELF_CORE.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CONSPIRER_POTION.get(),
                new ItemStack(Items.GUNPOWDER, 1),
                new ItemStack(Items.SPIDER_EYE, 1),
                new ItemStack(ModIngredients.SPHINX_BRAIN.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.REAPER_POTION.get(),
                new ItemStack(Items.GHAST_TEAR, 1),
                new ItemStack(Items.BLAZE_POWDER, 1),
                new ItemStack(ModIngredients.BLACK_HUNTING_SPIDER_COMPOSITE_EYES.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.IRON_BLOODED_KNIGHT_POTION.get(),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(Items.SOUL_SOIL, 1),
                new ItemStack(ModIngredients.MAGMA_GIANT_CORE.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WAR_BISHOP_POTION.get(),
                new ItemStack(Items.LEATHER, 1),
                new ItemStack(Items.REDSTONE_ORE, 1),
                new ItemStack(ModIngredients.WAR_COMET_CORE.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WEATHER_WARLOCK_POTION.get(),
                new ItemStack(Items.LIGHTNING_ROD, 1),
                new ItemStack(Items.END_STONE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("red_priest", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CONQUEROR_POTION.get(),
                new ItemStack(Items.DRAGON_HEAD, 1),
                new ItemStack(Items.FLINT_AND_STEEL, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("red_priest", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SPECTATOR_POTION.get(),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Items.SUNFLOWER, 1),
                new ItemStack(ModIngredients.GOAT_HORNED_BLACKFISH_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.TELEPATHIST_POTION.get(),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Items.PUMPKIN_SEEDS, 1),
                new ItemStack(ModIngredients.RAINBOW_SALAMANDER_PITUITARY_GLAND.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PSYCHIATRIST_POTION.get(),
                new ItemStack(Items.OAK_LOG, 1),
                new ItemStack(Items.STRING, 1),
                new ItemStack(ModIngredients.TREE_OF_ELDERS_FRUIT.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.HYPNOTIST_POTION.get(),
                new ItemStack(Items.APPLE, 1),
                new ItemStack(Items.PHANTOM_MEMBRANE, 1),
                new ItemStack(ModIngredients.ILLUSORY_CHIME_TREES_FRUIT.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DREAMWALKER_POTION.get(),
                new ItemStack(Items.GHAST_TEAR, 1),
                new ItemStack(Items.WHITE_BED, 1),
                new ItemStack(ModIngredients.DREAM_CATCHERS_HEART.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MANIPULATOR_POTION.get(),
                new ItemStack(Items.GHAST_TEAR, 1),
                new ItemStack(Items.DRAGON_BREATH, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("visionary", 4)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DREAM_WEAVER_POTION.get(),
                new ItemStack(ModIngredients.ROOT_OF_MIST_TREANT.get(), 1),
                new ItemStack(Items.CHERRY_SAPLING, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("visionary", 3)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DISCERNER_POTION.get(),
                new ItemStack(ModIngredients.SUN_ORB.get(), 1),
                new ItemStack(Items.WITHER_ROSE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("visionary", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.AUTHOR_POTION.get(),
                new ItemStack(Items.FEATHER, 1),
                new ItemStack(Items.WRITABLE_BOOK, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("visionary", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ASSASSIN_POTION.get(),
                new ItemStack(Items.SWEET_BERRIES, 1),
                new ItemStack(Items.SPIDER_EYE, 1),
                new ItemStack(ModIngredients.BLACK_FEATHER_OF_MONSTER_BIRD.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.INSTIGATOR_POTION.get(),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Items.BONE, 1),
                new ItemStack(ModIngredients.ABYSS_DEMONIC_FISH_BLOOD.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WITCH_POTION.get(),
                new ItemStack(Items.GLOW_BERRIES, 1),
                new ItemStack(Items.NETHER_WART, 1),
                new ItemStack(ModIngredients.AGATE_PEACOCK_EGG.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_PLEASURE_POTION.get(),
                new ItemStack(Items.BONE_MEAL, 1),
                new ItemStack(Items.STRING, 1),
                new ItemStack(ModIngredients.SUCCUBUS_EYES.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_AFFLICTION_POTION.get(),
                new ItemStack(Items.FERMENTED_SPIDER_EYE, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 1),
                new ItemStack(ModIngredients.SHADOW_LIZARD_SCALES.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_DESPAIR_POTION.get(),
                new ItemStack(Items.PUFFERFISH, 1),
                new ItemStack(Items.WARPED_FUNGUS, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("demoness", 4)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_UNAGING_POTION.get(),
                new ItemStack(Items.END_STONE, 1),
                new ItemStack(Items.ENDER_EYE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("demoness", 3)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_CATASTROPHE_POTION.get(),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("demoness", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEMONESS_OF_APOCALYPSE_POTION.get(),
                new ItemStack(Items.DRAGON_BREATH, 1),
                new ItemStack(Items.CHORUS_FRUIT, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("demoness", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PLANTER_POTION.get(),
                new ItemStack(Items.SHORT_GRASS, 1),
                new ItemStack(Items.SHEARS, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 9)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DOCTOR_POTION.get(),
                new ItemStack(Items.GOLDEN_APPLE, 1),
                new ItemStack(Items.WHEAT_SEEDS, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 8)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.HARVEST_PRIEST_POTION.get(),
                new ItemStack(Items.OAK_LOG, 1),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 7)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BIOLOGIST_POTION.get(),
                new ItemStack(Items.BEEF, 1),
                new ItemStack(Items.PORKCHOP, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 6)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DRUID_POTION.get(),
                new ItemStack(Items.RED_MUSHROOM, 1),
                new ItemStack(Items.WARPED_STEM, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 5)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CLASSICAL_ALCHEMIST_POTION.get(),
                new ItemStack(ModItems.BLOOD.get(), 1),
                new ItemStack(Items.SOUL_SOIL, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 4)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PALLBEARER_POTION.get(),
                new ItemStack(Items.WITHER_SKELETON_SKULL, 1),
                new ItemStack(Items.WITHER_ROSE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 3)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DESOLATE_MATRIARCH_POTION.get(),
                new ItemStack(Items.CHORUS_FRUIT, 1),
                new ItemStack(Items.CRYING_OBSIDIAN, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.NATUREWALKER_POTION.get(),
                new ItemStack(Items.DARK_OAK_SAPLING, 1),
                new ItemStack(Items.COARSE_DIRT, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("mother", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MARAUDER_POTION.get(),
                new ItemStack(Items.AMETHYST_SHARD, 1),
                new ItemStack(Items.RED_CONCRETE_POWDER, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("error", 9)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SWINDLER_POTION.get(),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Items.LAPIS_LAZULI, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("error", 8)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CRYPTOLOGITS_POTION.get(),
                new ItemStack(Items.ROSE_BUSH, 1),
                new ItemStack(Items.WRITABLE_BOOK, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("error", 7)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PROMETHEUS_POTION.get(),
                new ItemStack(Items.BLAZE_POWDER, 1),
                new ItemStack(Items.SWEET_BERRIES, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("error", 6)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DREAM_STEALER_POTION.get(),
                new ItemStack(Items.GHAST_TEAR, 1),
                new ItemStack(Items.BLAZE_POWDER, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("error", 5)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.PARASITE_POTION.get(),
                new ItemStack(Items.AMETHYST_BLOCK, 1),
                new ItemStack(Items.SOUL_SOIL, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("error", 4)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MENTOR_OF_DECEIT_POTION.get(),
                new ItemStack(Items.CHORUS_FRUIT, 1),
                new ItemStack(Items.GHAST_TEAR, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("error", 3)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.TROJAN_HORSE_OF_DESTINY_POTION.get(),
                new ItemStack(Items.NETHER_STAR, 1),
                new ItemStack(Items.PAPER, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("error", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WORM_OF_TIME_POTION.get(),
                new ItemStack(Items.CLOCK, 1),
                new ItemStack(Items.BOOK, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("error", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MONSTER_POTION.get(),
                new ItemStack(Items.POPPY, 1),
                new ItemStack(Items.OAK_LEAVES, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 9)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ROBOT_POTION.get(),
                new ItemStack(Items.BOOK, 1),
                new ItemStack(Items.FEATHER, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 8)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.LUCKY_ONE_POTION.get(),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Items.GOLD_INGOT, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 7)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CALAMITY_PRIEST_POTION.get(),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(Items.OBSIDIAN, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 6)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.WINNER_POTION.get(),
                new ItemStack(Items.GOLDEN_CARROT, 1),
                new ItemStack(Items.DIAMOND_PICKAXE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 5)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MISFORTUNE_MAGE_POTION.get(),
                new ItemStack(Items.PRISMARINE_CRYSTALS, 1),
                new ItemStack(Items.CALCITE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 4)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CHAOSWALKER_POTION.get(),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(Items.CHORUS_FRUIT, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 3)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SOOTHSAYER_POTION.get(),
                new ItemStack(Items.BOOK, 1),
                new ItemStack(Items.END_STONE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 2)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SNAKE_OF_MERCURY_POTION.get(),
                new ItemStack(Items.WATER_BUCKET, 1),
                new ItemStack(Items.END_STONE, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("wheel_of_fortune", 1)))
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CRIMINAL_POTION.get(),
                new ItemStack(Items.IRON_BARS, 1),
                new ItemStack(Items.IRON_BLOCK, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 9)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.UNWINGED_ANGEL_POTION.get(),
                new ItemStack(Items.INK_SAC, 1),
                new ItemStack(Items.FEATHER, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 8)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SERIAL_KILLER_POTION.get(),
                new ItemStack(Items.IRON_SWORD, 1),
                new ItemStack(Items.ROTTEN_FLESH, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 7)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DEVIL_POTION.get(),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(Items.NETHERRACK, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 6)))
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.DESIRE_APOSTLE_POTION.get(),
                new ItemStack(Items.WITHER_ROSE, 1),
                new ItemStack(Items.RED_MUSHROOM, 1),
                new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence("abyss", 5)))
        ));

    }

    @Nullable
    public static BeyonderPotion getByIngredients(ItemStack supp1, ItemStack supp2, ItemStack main) {
        if(!initialized) {
            initPotionRecipes();
            PotionRecipeItemHandler.initializeRecipes();
        }

        PotionRecipe recipe = RECIPES.stream().filter(r -> {
            if(!areSimilar(r.mainIngredient(), main))
                return false;

            if(!areSimilar(r.supplementaryIngredient1(), supp1)) {
                return areSimilar(r.supplementaryIngredient1(), supp2) && areSimilar(r.supplementaryIngredient2(), supp1);
            }
            return areSimilar(r.supplementaryIngredient2(), supp2);
        }).findFirst().orElse(null);

        if(recipe != null) {
            return recipe.potion();
        }

        recipe = RECIPES.stream().filter(r -> {
            if((areSimilar(r.supplementaryIngredient1(), supp1) && areSimilar(r.supplementaryIngredient2(), supp2)) || (areSimilar(r.supplementaryIngredient1(), supp2) && areSimilar(r.supplementaryIngredient2(), supp1))) {
                BeyonderCharacteristicItem characteristicItem = BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(r.potion().getPathway(), r.potion().getSequence());
                if(characteristicItem == null) {
                    return false;
                }
                return areSimilar(main, new ItemStack(characteristicItem));
            }
            return false;
        }).findFirst().orElse(null);

        return recipe == null ? null : recipe.potion();
    }

    public static boolean areSimilar(ItemStack i1, ItemStack i2) {
        if(i1 == null || i2 == null) {
            return false;
        }
        return i1.is(i2.getItem()) && i1.getCount() == i2.getCount();
    }
}

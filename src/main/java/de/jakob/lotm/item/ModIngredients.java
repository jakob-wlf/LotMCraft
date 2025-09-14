package de.jakob.lotm.item;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.potions.BeyonderPotion;
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


    public static final DeferredItem<Item> CRYSTAL_SUNFLOWER = ITEMS.registerItem("crystal_sunflower", (properties) -> new PotionIngredient(properties, 9, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> POWDER_OF_DAZZLING_SOUL = ITEMS.registerItem("powder_of_dazzling_soul", (properties) -> new PotionIngredient(properties, 8, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_PACT_TREE_FRUIT = ITEMS.registerItem("spirit_pact_tree_fruit", (properties) -> new PotionIngredient(properties, 7, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> CRYSTALLIZED_ROOTS = ITEMS.registerItem("crystallized_roots", (properties) -> new PotionIngredient(properties, 6, true, "sun"), new Item.Properties());
    public static final DeferredItem<Item> PURE_WHITE_BRILLIANT_ROCK = ITEMS.registerItem("pure_white_brilliant_rock", (properties) -> new PotionIngredient(properties, 5, true, "sun"), new Item.Properties());


    public static final DeferredItem<Item> ILLUSION_CRYSTAL = ITEMS.registerItem("illusion_crystal", (properties) -> new PotionIngredient(properties, 9, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_EATER_STOMACH_POUCH = ITEMS.registerItem("spirit_eater_stomach_pouch", (properties) -> new PotionIngredient(properties, 8, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> METEORITE_CRYSTAL = ITEMS.registerItem("meteorite_crystal", (properties) -> new PotionIngredient(properties, 7, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_WRAITH_ARTIFACT = ITEMS.registerItem("ancient_wraith_artifact", (properties) -> new PotionIngredient(properties, 6, true, "door"), new Item.Properties());
    public static final DeferredItem<Item> SHADOWLESS_DEMONIC_WOLF_HEART = ITEMS.registerItem("shadowless_demonic_wolf_heart", (properties) -> new PotionIngredient(properties, 5, true, "door"), new Item.Properties());


    public static final DeferredItem<Item> MURLOC_BLADDER = ITEMS.registerItem("murloc_bladder", (properties) -> new PotionIngredient(properties, 9, true, "tyrant"), new Item.Properties());
    public static final DeferredItem<Item> DRAGON_EYED_CONDOR_EYEBALL = ITEMS.registerItem("dragon_eyed_condor_eyeball", (properties) -> new PotionIngredient(properties, 8, true,"tyrant"), new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_LOGBOOK = ITEMS.registerItem("ancient_logbook", (properties) -> new PotionIngredient(properties, 7, true, "tyrant"), new Item.Properties());
    public static final DeferredItem<Item> BLUE_SHADOW_FALCON_FEATHERS = ITEMS.registerItem("blue_shadow_falcon_feathers", (properties) -> new PotionIngredient(properties, 6, true, "tyrant"), new Item.Properties());
    public static final DeferredItem<Item> SIREN_VOCAL_SAC = ITEMS.registerItem("siren_vocal_sac", (properties) -> new PotionIngredient(properties, 5, true, "tyrant"), new Item.Properties());


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

    public static final DeferredItem<Item> BLACK_FEATHER_OF_MONSTER_BIRD = ITEMS.registerItem("black_feather_of_monster_bird", (properties) -> new PotionIngredient(properties, 9, true), new Item.Properties());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
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
}


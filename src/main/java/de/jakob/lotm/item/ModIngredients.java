package de.jakob.lotm.item;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.potions.BeyonderPotion;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ModIngredients {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);

    public static final DeferredItem<Item> LAVOS_SQUID_BLOOD = ITEMS.registerItem("lavos_squid_blood", (properties) -> new PotionIngredient(properties, 9, true), new Item.Properties());
    public static final DeferredItem<Item> METEORITE_CRYSTAL = ITEMS.registerItem("meteorite_crystal", (properties) -> new PotionIngredient(properties, 7, true), new Item.Properties());
    public static final DeferredItem<Item> ILLUSION_CRYSTAL = ITEMS.registerItem("illusion_crystal", (properties) -> new PotionIngredient(properties, 9, true), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_EATER_STOMACH_POUCH = ITEMS.registerItem("spirit_eater_stomach_pouch", (properties) -> new PotionIngredient(properties, 8, true), new Item.Properties());
    public static final DeferredItem<Item> ROOT_OF_MIST_TREANT = ITEMS.registerItem("true_root_of_mist_treant", (properties) -> new PotionIngredient(properties, 7, true), new Item.Properties());
    public static final DeferredItem<Item> HORNACIS_GRAY_MOUNTAIN_GOAT_HORN = ITEMS.registerItem("hornacis_gray_mountain_goat_horn", (properties) -> new PotionIngredient(properties, 8, true), new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_PACT_TREE_FRUIT = ITEMS.registerItem("spirit_pact_tree_fruit", (properties) -> new PotionIngredient(properties, 8, true), new Item.Properties());
    public static final DeferredItem<Item> CRYSTAL_SUNFLOWER = ITEMS.registerItem("crystal_sunflower", (properties) -> new PotionIngredient(properties, 8, true), new Item.Properties());
    public static final DeferredItem<Item> POWDER_OF_DAZZLING_SOUL = ITEMS.registerItem("powder_of_dazzling_soul", (properties) -> new PotionIngredient(properties, 8, true), new Item.Properties());

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
}


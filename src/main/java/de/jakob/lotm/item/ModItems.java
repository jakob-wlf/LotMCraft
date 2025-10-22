package de.jakob.lotm.item;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.item.custom.MarionetteControllerItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);

    public static final DeferredItem<Item> FOOL_Card = ITEMS.registerItem("fool_card", Item::new, new Item.Properties());
    public static final DeferredItem<Item> MOD_ICON = ITEMS.registerItem("lotm_icon", Item::new, new Item.Properties());
    public static final DeferredItem<Item> PAPER_FIGURINE_SUBSTITUTE = ITEMS.registerItem("paper_figurine_substitute", Item::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MIRROR = ITEMS.registerItem("mirror", Item::new, new Item.Properties().stacksTo(1));

    public static final Supplier<Item> MARIONETTE_CONTROLLER = ITEMS.register("marionette_controller",
            () -> new MarionetteControllerItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> SUBORDINATE_CONTROLLER = ITEMS.register("subordinate_controller",
            () -> new MarionetteControllerItem(new Item.Properties().stacksTo(1)));

    public static PotionIngredient selectRandomIngredient(List<PotionIngredient> ingredients, Random random) {
        if (ingredients == null || ingredients.isEmpty()) {
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
        return ingredients.getLast();
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

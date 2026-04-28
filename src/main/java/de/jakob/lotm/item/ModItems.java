package de.jakob.lotm.item;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.artifacts.SealedArtifactItem;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.item.custom.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
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
    public static final DeferredItem<Item> CRYSTAL_BALL = ITEMS.registerItem("crystal_ball", Item::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> CANE = ITEMS.registerItem("cane", Item::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MOD_ICON = ITEMS.registerItem("lotm_icon", Item::new, new Item.Properties());
    public static final DeferredItem<Item> PAPER_FIGURINE_SUBSTITUTE = ITEMS.registerItem("paper_figurine_substitute", Item::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MIRROR = ITEMS.registerItem("mirror", Item::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BLOOD = ITEMS.registerItem("blood", Item::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> STORY_BOOK = ITEMS.registerItem("story_book", StoryBookItem::new, new Item.Properties().stacksTo(1));

    public static final Supplier<Item> MARIONETTE_CONTROLLER = ITEMS.register("marionette_controller",
            () -> new MarionetteControllerItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> SUBORDINATE_CONTROLLER = ITEMS.register("subordinate_controller",
            () -> new SubordinateControllerItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> EXCAVATED_AREA_ITEM = ITEMS.register("excavated_area",
            () -> new ExcavatedAreaItem(new Item.Properties()
                    .stacksTo(1)
            )
    );
    public static final Supplier<Item> SUN_ITEM = ITEMS.register("sun",
            () -> new SunItem(new Item.Properties()
                    .stacksTo(1)
            )
    );
    public static final Supplier<Item> MOON_ITEM = ITEMS.register("moon",
            () -> new MoonItem(new Item.Properties()
                    .stacksTo(1)
            )
    );
    public static final Supplier<Item> GUIDING_BOOK = ITEMS.register("guiding_book",
            () -> new GuidingBookItem(new Item.Properties()
                    .stacksTo(1)
            )
    );

    public static final DeferredHolder<Item, Item> SEALED_ARTIFACT = ITEMS.register("sealed_artifact",
            () -> new SealedArtifactItem(new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()));
    public static final DeferredHolder<Item, Item> SEALED_ARTIFACT_BELL = ITEMS.register("sealed_artifact_bell",
            () -> new SealedArtifactItem(new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()));
    public static final DeferredHolder<Item, Item> SEALED_ARTIFACT_STAR = ITEMS.register("sealed_artifact_star",
            () -> new SealedArtifactItem(new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()));
    public static final DeferredHolder<Item, Item> SEALED_ARTIFACT_CHAIN = ITEMS.register("sealed_artifact_chain",
            () -> new SealedArtifactItem(new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()));
    public static final DeferredHolder<Item, Item> SEALED_ARTIFACT_GEM = ITEMS.register("sealed_artifact_gem",
            () -> new SealedArtifactItem(new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()));

    public static final DeferredItem<BlockItem> MYSTICAL_RING = ITEMS.register("mystical_ring",
            () -> new BlockItem(ModBlocks.MYSTICAL_RING.get(), new Item.Properties())
    );

    // Uniquenesses (registered, so I can use them in the renderer for the Uniqueness Entity)
    public static final DeferredItem<Item> RED_PRIEST_UNIQUENESS = ITEMS.register("red_priest_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "red_priest")
    );
    public static final DeferredItem<Item> TYRANT_UNIQUENESS = ITEMS.register("tyrant_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "tyrant")
    );
    public static final DeferredItem<Item> SUN_UNIQUENESS = ITEMS.register("sun_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "sun")
    );
    public static final DeferredItem<Item> FOOL_UNIQUENESS = ITEMS.register("fool_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "fool")
    );
    public static final DeferredItem<Item> ERROR_UNIQUENESS = ITEMS.register("error_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "error")
    );
    public static final DeferredItem<Item> VISIONARY_UNIQUENESS = ITEMS.register("visionary_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "visionary")
    );
    public static final DeferredItem<Item> DOOR_UNIQUENESS = ITEMS.register("door_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "door")
    );
    public static final DeferredItem<Item> DARKNESS_UNIQUENESS = ITEMS.register("darkness_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "darkness")
    );
    public static final DeferredItem<Item> WHEEL_OF_FORTUNE_UNIQUENESS = ITEMS.register("wheel_of_fortune_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "wheel_of_fortune")
    );
    public static final DeferredItem<Item> ABYSS_UNIQUENESS = ITEMS.register("abyss_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "abyss")
    );
    public static final DeferredItem<Item> MOTHER_UNIQUENESS = ITEMS.register("mother_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "mother")
    );
    public static final DeferredItem<Item> DEMONESS_UNIQUENESS = ITEMS.register("demoness_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "demoness")
    );
    public static final DeferredItem<Item> JUSTICIAR_UNIQUENESS = ITEMS.register("justiciar_uniqueness",
            () -> new UniquenessItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "justiciar")
    );

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

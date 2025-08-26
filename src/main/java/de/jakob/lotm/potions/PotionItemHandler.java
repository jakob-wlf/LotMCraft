package de.jakob.lotm.potions;

import de.jakob.lotm.LOTMCraft;
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

public class PotionItemHandler {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);

    public static DeferredItem<Item> EMPTY_BOTTLE = ITEMS.registerItem("empty_bottle", Item::new);

    public static DeferredItem<Item> SEER_POTION = ITEMS.registerItem("seer_potion", properties ->
            new BeyonderPotion(properties, 9, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CLOWN_POTION = ITEMS.registerItem("clown_potion", properties ->
                    new BeyonderPotion(properties, 8, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MAGICIAN_POTION = ITEMS.registerItem("magician_potion", properties ->
                    new BeyonderPotion(properties, 7, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> BARD_POTION = ITEMS.registerItem("bard_potion", properties ->
                    new BeyonderPotion(properties, 9, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> LIGHT_SUPPLICANT_POTION = ITEMS.registerItem("light_supplicant_potion", properties ->
                    new BeyonderPotion(properties, 8, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SOLAR_HIGH_PRIEST_POTION = ITEMS.registerItem("solar_high_priest_potion", properties ->
                    new BeyonderPotion(properties, 7, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> NOTARY_POTION = ITEMS.registerItem("notary_potion", properties ->
                    new BeyonderPotion(properties, 6, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> SAILOR_POTION = ITEMS.registerItem("sailor_potion", properties ->
                    new BeyonderPotion(properties, 9, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> FOLK_OF_RAGE_POTION = ITEMS.registerItem("folk_of_rage_potion", properties ->
                    new BeyonderPotion(properties, 8, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SEAFARER_POTION = ITEMS.registerItem("seafarer_potion", properties ->
                    new BeyonderPotion(properties, 7, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WIND_BLESSED_POTION = ITEMS.registerItem("wind_blessed_potion", properties ->
                    new BeyonderPotion(properties, 6, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> HUNTER_POTION = ITEMS.registerItem("hunter_potion", properties ->
                    new BeyonderPotion(properties, 9, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PROVOKER_POTION = ITEMS.registerItem("provoker_potion", properties ->
                    new BeyonderPotion(properties, 8, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PYROMANIAC_POTION = ITEMS.registerItem("pyromaniac_potion", properties ->
                    new BeyonderPotion(properties, 7, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> APPRENTICE_POTION = ITEMS.registerItem("apprentice_potion", properties ->
                    new BeyonderPotion(properties, 9, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> TRICKMASTER_POTION = ITEMS.registerItem("trickmaster_potion", properties ->
                    new BeyonderPotion(properties, 8, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> ASTROLOGER_POTION = ITEMS.registerItem("astrologer_potion", properties ->
                    new BeyonderPotion(properties, 7, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> CRIMINAL_POTION = ITEMS.registerItem("criminal_potion", properties ->
                    new BeyonderPotion(properties, 9, "abyss"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> UNWINGED_ANGEL_POTION = ITEMS.registerItem("unwinged_angel_potion", properties ->
                    new BeyonderPotion(properties, 8, "abyss"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SERIAL_KILLER_POTION = ITEMS.registerItem("serial_killer_potion", properties ->
                    new BeyonderPotion(properties, 7, "abyss"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> SLEEPLESS_POTION = ITEMS.registerItem("sleepless_potion", properties ->
                    new BeyonderPotion(properties, 9, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MIDNIGHT_POET_POTION = ITEMS.registerItem("midnight_poet_potion", properties ->
                    new BeyonderPotion(properties, 8, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> NIGHTMARE_POTION = ITEMS.registerItem("nightmare_potion", properties ->
                    new BeyonderPotion(properties, 7, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> PLANTER_POTION = ITEMS.registerItem("planter_potion", properties ->
                    new BeyonderPotion(properties, 9, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DOCTOR_POTION = ITEMS.registerItem("doctor_potion", properties ->
                    new BeyonderPotion(properties, 8, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> HARVEST_PRIEST_POTION = ITEMS.registerItem("harvest_priest_potion", properties ->
                    new BeyonderPotion(properties, 7, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> ASSASSIN_POTION = ITEMS.registerItem("assassin_potion", properties ->
                    new BeyonderPotion(properties, 9, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> INSTIGATOR_POTION = ITEMS.registerItem("instigator_potion", properties ->
                    new BeyonderPotion(properties, 8, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WITCH_POTION = ITEMS.registerItem("witch_potion", properties ->
                    new BeyonderPotion(properties, 7, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));



    public static void registerPotions(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static BeyonderPotion selectRandomPotion(Random random) {
        List<BeyonderPotion> potions = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderPotion)
                .map(i -> ((BeyonderPotion) i))
                .toList();

        if (potions.isEmpty()) {
            return null;
        }

        // Calculate weights for each potion
        // Higher sequence = more common = higher weight
        // Weight formula: sequence + 1 makes sequence 9 -> weight 10, sequence 0 -> weight 1
        Map<BeyonderPotion, Integer> potionWeights = new HashMap<>();
        int totalWeight = 0;

        for (BeyonderPotion potion : potions) {
            int weight = potion.getSequence() + 1; // Higher sequence = more common = higher weight
            potionWeights.put(potion, weight);
            totalWeight += weight;
        }

        // Generate random number between 0 and totalWeight-1
        int randomValue = random.nextInt(totalWeight);

        // Find the selected potion based on cumulative weights
        int cumulativeWeight = 0;
        for (Map.Entry<BeyonderPotion, Integer> entry : potionWeights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should never reach here with valid input)
        return potions.get(potions.size() - 1);
    }

}

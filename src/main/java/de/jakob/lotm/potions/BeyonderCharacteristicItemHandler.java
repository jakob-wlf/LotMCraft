package de.jakob.lotm.potions;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BeyonderCharacteristicItemHandler {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);



    public static void registerCharacteristics(IEventBus eventBus) {
        for(String pathway : BeyonderData.pathways) {
            if(!BeyonderData.implementedRecipes.containsKey(pathway))
                continue;
            for(String sequence : BeyonderData.pathwayInfos.get(pathway).sequenceNames()) {
                int seq = BeyonderData.pathwayInfos.get(pathway).getSequenceIndex(sequence);
                if(seq == -1 || seq == 0)
                    continue;
                ITEMS.registerItem(
                        sequence + "_characteristic",
                        p -> new BeyonderCharacteristicItem(p, pathway, seq),
                        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
            }
        }
        ITEMS.register(eventBus);
    }

    public static BeyonderCharacteristicItem selectRandomCharacteristic(Random random) {
        List<BeyonderCharacteristicItem> items = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderCharacteristicItem)
                .map(i -> ((BeyonderCharacteristicItem) i))
                .toList();

        if (items.isEmpty()) {
            return null;
        }

        // Calculate weights for each potion
        // Higher sequence = more common = higher weight
        // Weight formula: sequence + 1 makes sequence 9 -> weight 10, sequence 0 -> weight 1
        Map<BeyonderCharacteristicItem, Integer> weights = new HashMap<>();
        int totalWeight = 0;

        for (BeyonderCharacteristicItem item : items) {
            int weight = item.getSequence() + 1; // Higher sequence = more common = higher weight
            weights.put(item, weight);
            totalWeight += weight;
        }

        // Generate random number between 0 and totalWeight-1
        int randomValue = random.nextInt(totalWeight);

        // Find the selected potion based on cumulative weights
        int cumulativeWeight = 0;
        for (Map.Entry<BeyonderCharacteristicItem, Integer> entry : weights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should never reach here with valid input)
        return items.getLast();
    }

    public static BeyonderCharacteristicItem selectRandomCharacteristicOfPathway(Random random, String pathway) {
        List<BeyonderCharacteristicItem> recipes = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderCharacteristicItem)
                .map(i -> ((BeyonderCharacteristicItem) i))
                .filter(i -> i.getPathway().equals(pathway))
                .toList();

        if (recipes.isEmpty()) {
            return null;
        }

        // Calculate weights for each potion
        // Higher sequence = more common = higher weight
        // Weight formula: sequence + 1 makes sequence 9 -> weight 10, sequence 0 -> weight 1
        Map<BeyonderCharacteristicItem, Integer> weights = new HashMap<>();
        int totalWeight = 0;

        for (BeyonderCharacteristicItem recipeItem : recipes) {
            int weight = recipeItem.getSequence() + 1; // Higher sequence = more common = higher weight
            weights.put(recipeItem, weight);
            totalWeight += weight;
        }

        // Generate random number between 0 and totalWeight-1
        int randomValue = random.nextInt(totalWeight);

        // Find the selected potion based on cumulative weights
        int cumulativeWeight = 0;
        for (Map.Entry<BeyonderCharacteristicItem, Integer> entry : weights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should never reach here with valid input)
        return recipes.getLast();
    }

    public static BeyonderCharacteristicItem selectCharacteristicOfPathwayAndSequence(String pathway, int sequence) {
        List<BeyonderCharacteristicItem> recipes = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderCharacteristicItem)
                .map(i -> ((BeyonderCharacteristicItem) i))
                .filter(i -> i.getPathway().equals(pathway))
                .filter(i -> i.getSequence() == sequence)
                .toList();

        if (recipes.isEmpty()) {
            return null;
        }

        return recipes.get(0);
    }

    public static List<BeyonderCharacteristicItem> selectAllOfPathway(String pathway) {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderCharacteristicItem)
                .map(i -> ((BeyonderCharacteristicItem) i))
                .filter(i -> i.getPathway().equals(pathway))
                .toList();
    }

}

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

public class PotionRecipeItemHandler {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);


    public static void registerRecipes(IEventBus eventBus) {
        for(String pathway : BeyonderData.pathways) {
            if(!BeyonderData.implementedRecipes.containsKey(pathway))
                continue;
            for(String sequence : BeyonderData.pathwayInfos.get(pathway).sequenceNames()) {
                int seq = BeyonderData.pathwayInfos.get(pathway).getSequenceIndex(sequence);
                if(seq == -1 || !BeyonderData.implementedRecipes.get(pathway).contains(seq))
                    continue;
                ITEMS.registerItem(
                        sequence,
                        PotionRecipeItem::new,
                        new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
            }
        }
        ITEMS.register(eventBus);
    }

    public static void initializeRecipes() {
        for(PotionRecipe recipe : PotionRecipes.RECIPES) {
            String sequenceName = BeyonderData.pathwayInfos.get(recipe.potion().getPathway()).sequenceNames()[recipe.potion().getSequence()];
            ITEMS.getEntries()
                    .stream()
                    .map(DeferredHolder::get)
                    .filter(r -> r instanceof PotionRecipeItem item
                            && BuiltInRegistries.ITEM.getKey(item).getPath().equals(sequenceName))
                    .forEach(r -> ((PotionRecipeItem) r).setRecipe(recipe));
        }
    }

    public static PotionRecipeItem selectRandomrecipe(Random random) {
        if(!PotionRecipes.initialized) {
            PotionRecipes.initPotionRecipes();
            initializeRecipes();
        }
        List<PotionRecipeItem> recipes = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionRecipeItem)
                .map(i -> ((PotionRecipeItem) i))
                .filter(i -> i.getRecipe() != null)
                .toList();

        if (recipes.isEmpty()) {
            return null;
        }

        // Calculate weights for each potion
        // Higher sequence = more common = higher weight
        // Weight formula: sequence + 1 makes sequence 9 -> weight 10, sequence 0 -> weight 1
        Map<PotionRecipeItem, Integer> weights = new HashMap<>();
        int totalWeight = 0;

        for (PotionRecipeItem recipeItem : recipes) {
            int weight = recipeItem.getRecipe().potion().getSequence() + 1; // Higher sequence = more common = higher weight
            weights.put(recipeItem, weight);
            totalWeight += weight;
        }

        // Generate random number between 0 and totalWeight-1
        int randomValue = random.nextInt(totalWeight);

        // Find the selected potion based on cumulative weights
        int cumulativeWeight = 0;
        for (Map.Entry<PotionRecipeItem, Integer> entry : weights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should never reach here with valid input)
        return recipes.getLast();
    }

    public static PotionRecipeItem selectRecipeOfPathwayAndSequence(String pathway, int sequence) {
        if(!PotionRecipes.initialized) {
            PotionRecipes.initPotionRecipes();
            initializeRecipes();
        }

        List<PotionRecipeItem> recipes = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionRecipeItem)
                .map(i -> ((PotionRecipeItem) i))
                .filter(i -> i.getRecipe() != null && i.getRecipe().potion().getPathway().equals(pathway))
                .filter(i -> i.getRecipe() != null)
                .filter(i -> i.getRecipe().potion().getSequence() == sequence)
                .toList();

        if (recipes.isEmpty()) {
            return null;
        }

        return recipes.get(0);
    }

    public static PotionRecipeItem selectRandomRecipeOfSequence(Random random, int sequence) {
        if(!PotionRecipes.initialized) {
            PotionRecipes.initPotionRecipes();
            initializeRecipes();
        }

        List<PotionRecipeItem> recipes = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionRecipeItem)
                .map(i -> ((PotionRecipeItem) i))
                .filter(i -> i.getRecipe() != null)
                .filter(i -> i.getRecipe().potion().getSequence() == sequence)
                .toList();

        if (recipes.isEmpty()) {
            return null;
        }

        return recipes.get(random.nextInt(recipes.size()));
    }

    public static List<PotionRecipeItem> getAllRecipes() {
        if(!PotionRecipes.initialized) {
            PotionRecipes.initPotionRecipes();
            initializeRecipes();
        }
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionRecipeItem)
                .map(i -> ((PotionRecipeItem) i))
                .filter(i -> i.getRecipe() != null)
                .toList();
    }

    public static List<PotionRecipeItem> selectAllOfPathway(String pathway) {
        if(!PotionRecipes.initialized) {
            PotionRecipes.initPotionRecipes();
            initializeRecipes();
        }
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PotionRecipeItem)
                .map(i -> ((PotionRecipeItem) i))
                .filter(i -> i.getRecipe() != null && i.getRecipe().potion().getPathway().equals(pathway))
                .filter(i -> i.getRecipe() != null)
                .toList();
    }

}

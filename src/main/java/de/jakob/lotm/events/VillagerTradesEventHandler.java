package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.PotionIngredient;
import de.jakob.lotm.potions.*;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.villager.ModVillagers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class VillagerTradesEventHandler {

    private static final int[] costsPerSequence = new int[]{1000, 300, 250, 180, 160, 70, 64, 40, 29, 29};
    private static final int[] costsPerSequenceForIngredients = new int[]{1000, 120, 50, 35, 29, 18, 14, 11, 8, 4};
    private static final int[] costsPerSequenceForRecipes = new int[]{1000, 120, 50, 35, 29, 18, 14, 11, 8, 4};

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if (event.getType() == ModVillagers.BEYONDER_PROFESSION.value()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            HashMap<Item, Integer> tradeableItems = new HashMap<>();
            PotionItemHandler.selectAllPotions().forEach(p -> tradeableItems.put(p, costsPerSequence[p.getSequence()]));
            ModIngredients.getAll().forEach(i -> tradeableItems.put(i, costsPerSequenceForIngredients[i.getSequence()]));
            PotionRecipeItemHandler.getAllRecipes().forEach(r -> tradeableItems.put(r, costsPerSequenceForRecipes[r.getRecipe().potion().getSequence()]));

            for(Map.Entry<Item, Integer> entry : tradeableItems.entrySet()) {
                int level = getLevelForItem(entry.getKey());
                int sequence = getSequenceForItem(entry.getKey());

                trades.get(level).add((entity, randomSource) -> {
                    Random random = new Random();
                    int diamondAmount = Math.max(1, random.nextInt(entry.getValue() - 4, entry.getValue() + 5));
                    ItemCost diamondCost = new ItemCost(Items.DIAMOND, diamondAmount);
                    java.util.Optional<ItemCost> additionalCost = getAdditionalCostForSequence(sequence, random);

                    return new MerchantOffer(
                            diamondCost,
                            additionalCost,
                            new ItemStack(entry.getKey(), 1),
                            random.nextInt(1, 2),
                            30 * level,
                            0.005f
                    );
                });
            }
        }

        if (event.getType() == ModVillagers.EVERNIGHT_PROFESSION.value()) {
            populateVillagerWithPathwayProfession(event.getTrades(), "darkness");

        }
        if (event.getType() == ModVillagers.BLAZING_SUN_PROFESSION.value()) {
            populateVillagerWithPathwayProfession(event.getTrades(), "sun");
        }
    }

    private static void populateVillagerWithPathwayProfession(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, String pathway) {
        HashMap<Item, Integer> tradeableItems = new HashMap<>();
        PotionItemHandler.selectAllPotionsOfPathway(pathway).forEach(p -> tradeableItems.put(p, costsPerSequence[p.getSequence()]));
        ModIngredients.getAllOfPathway(pathway).forEach(i -> tradeableItems.put(i, costsPerSequenceForIngredients[i.getSequence()]));
        PotionRecipeItemHandler.selectAllOfPathway(pathway).forEach(r -> tradeableItems.put(r, costsPerSequenceForRecipes[r.getRecipe().potion().getSequence()]));
        BeyonderCharacteristicItemHandler.selectAllOfPathway(pathway).forEach(r -> tradeableItems.put(r, costsPerSequenceForRecipes[r.getSequence()]));

        for(Map.Entry<Item, Integer> entry : tradeableItems.entrySet()) {
            int level = getLevelForItem(entry.getKey());
            int sequence = getSequenceForItem(entry.getKey());

            trades.get(level).add((entity, randomSource) -> {
                Random random = new Random();
                int diamondAmount = Math.max(1, random.nextInt(entry.getValue() - 4, entry.getValue() + 5));
                ItemCost diamondCost = new ItemCost(Items.DIAMOND, diamondAmount);
                java.util.Optional<ItemCost> additionalCost = getAdditionalCostForSequence(sequence, random);

                return new MerchantOffer(
                        diamondCost,
                        additionalCost,
                        new ItemStack(entry.getKey(), 1),
                        random.nextInt(1, 2),
                        30 * level,
                        0.005f
                );
            });
        }
    }

    private static java.util.Optional<ItemCost> getAdditionalCostForSequence(int sequence, Random random) {
        ItemCost cost = switch(sequence) {
            case 5 -> new ItemCost(Items.ANCIENT_DEBRIS, 1);
            case 4 -> new ItemCost(Items.NETHER_STAR, 1);
            case 3 -> new ItemCost(Items.GOLD_BLOCK, 64);
            case 2 -> new ItemCost(Items.NETHERITE_INGOT, random.nextInt(8, 13));
            case 1 -> random.nextBoolean() ? new ItemCost(Items.NETHER_STAR, 10) : new ItemCost(Items.DRAGON_HEAD, 20);
            default -> null;
        };
        return java.util.Optional.ofNullable(cost);
    }

    private static int getSequenceForItem(Item item) {
        if(item instanceof PotionRecipeItem recipeItem) {
            return recipeItem.getRecipe().potion().getSequence();
        } else if(item instanceof BeyonderPotion potion) {
            return potion.getSequence();
        } else if(item instanceof PotionIngredient ingredient) {
            return ingredient.getSequence();
        } else if(item instanceof BeyonderCharacteristicItem characteristic) {
            return characteristic.getSequence();
        }
        return 9; // Default to sequence 9 (no additional cost)
    }

    private static int getLevelForSequence(int sequence) {
        return switch(sequence) {
            default -> 1;
            case 8 -> 2;
            case 7, 6 -> 3;
            case 5 -> 4;
            case 4, 3, 2, 1 -> 5;
        };
    }

    private static int getLevelForItem(Item item) {
        int level = 1;
        if(item instanceof PotionRecipeItem recipeItem) {
            level = getLevelForSequence(recipeItem.getRecipe().potion().getSequence());
        } else if(item instanceof BeyonderPotion potion) {
            level = getLevelForSequence(potion.getSequence());
        } else if(item instanceof PotionIngredient ingredient) {
            level = getLevelForSequence(ingredient.getSequence());
        }

        return level;
    }

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (event.getLevel().isClientSide()) return;

        MerchantOffers offers = villager.getOffers();

        if(offers.isEmpty()) return;

        offers.removeIf(offer -> {
                    var item = offer.getResult().getItem();

                    if(item instanceof PotionIngredient obj){
                        for(var path : obj.getPathways()){
                            return !BeyonderData.beyonderMap.check(path,obj.getSequence());
                        }
                    }

                    if(item instanceof BeyonderPotion potion){
                        return !BeyonderData.beyonderMap.check(potion.getPathway(), potion.getSequence());
                    }

                    if(item instanceof BeyonderCharacteristicItem cha){
                        return !BeyonderData.beyonderMap.check(cha.getPathway(), cha.getSequence());
                    }

                    return false;
                }
        );
    }

}
package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.client.*;
import de.jakob.lotm.entity.custom.FireRavenEntity;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.PotionIngredient;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.SpiritualityProgressTracker;
import de.jakob.lotm.villager.ModVillagers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.*;

import static de.jakob.lotm.util.BeyonderData.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FlamingSpearProjectileModel.LAYER_LOCATION, FlamingSpearProjectileModel::createBodyLayer);
        event.registerLayerDefinition(UnshadowedSpearProjectileModel.LAYER_LOCATION, UnshadowedSpearProjectileModel::createBodyLayer);
        event.registerLayerDefinition(FireballModel.LAYER_LOCATION, FireballModel::createBodyLayer);
        event.registerLayerDefinition(WindBladeModel.LAYER_LOCATION, WindBladeModel::createBodyLayer);
        event.registerLayerDefinition(ApprenticeDoorModel.LAYER_LOCATION, ApprenticeDoorModel::createBodyLayer);
        event.registerLayerDefinition(TravelersDoorModel.LAYER_LOCATION, TravelersDoorModel::createBodyLayer);
        event.registerLayerDefinition(ApprenticeBookModel.LAYER_LOCATION, ApprenticeBookModel::createBodyLayer);
        event.registerLayerDefinition(PaperDaggerProjectileModel.LAYER_LOCATION, PaperDaggerProjectileModel::createBodyLayer);
        event.registerLayerDefinition(FireRavenModel.LAYER_LOCATION, FireRavenModel::createBodyLayer);
        event.registerLayerDefinition(FrostSpearProjectileModel.LAYER_LOCATION, FrostSpearProjectileModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FIRE_RAVEN.get(), FireRavenEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        // Only copy data if the original player was a beyonder
        if (isBeyonder(original)) {
            String pathway = getPathway(original);
            int sequence = getSequence(original);
            boolean griefingEnabled = original.getPersistentData().getBoolean(NBT_GRIEFING_ENABLED);

            // Copy the data to the new player
            CompoundTag newTag = newPlayer.getPersistentData();
            newTag.putString(NBT_PATHWAY, pathway);
            newTag.putInt(NBT_SEQUENCE, sequence);
            newTag.putFloat(NBT_SPIRITUALITY, BeyonderData.getMaxSpirituality(sequence));
            newTag.putBoolean(NBT_GRIEFING_ENABLED, griefingEnabled);

            // Update spirituality progress tracker
            if (getMaxSpirituality(sequence) > 0) {
                float progress = 1;
                SpiritualityProgressTracker.setProgress(newPlayer, progress);
            }
        }
    }

    private static final int[] costsPerSequence = new int[]{1000, 300, 250, 180, 160, 70, 64, 40, 29, 29};
    private static final int[] costsPerSequenceForIngredients = new int[]{1000, 120, 50, 35, 29, 18, 14, 11, 8, 4};
    private static final int[] costsPerSequenceForRecipes = new int[]{1000, 120, 50, 35, 29, 18, 14, 11, 8, 4};

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if (event.getType() == ModVillagers.BEYONDER_PROFESSION.value()) {
            Random random = new Random();

            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            HashMap<Item, Integer> tradeableItems = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                switch (random.nextInt(3)) {
                    case 0 -> {
                        PotionRecipeItem recipe = PotionRecipeItemHandler.selectRandomrecipe(random);
                        if (recipe == null)
                            return;
                        int sequence = recipe.getRecipe().potion().getSequence();
                        tradeableItems.put(recipe, costsPerSequenceForRecipes[sequence]);
                    }
                    case 1 -> {
                        BeyonderPotion potion = PotionItemHandler.selectRandomPotion(random);
                        if (potion == null)
                            return;
                        int sequence = potion.getSequence();
                        tradeableItems.put(potion, costsPerSequence[sequence]);
                    }
                    case 2 -> {
                        PotionIngredient ingredient = ModIngredients.selectRandomIngredient(random);
                        if (ingredient == null)
                            return;
                        int sequence = ingredient.getSequence();
                        tradeableItems.put(ingredient, costsPerSequenceForIngredients[sequence]);
                    }
                }
            }

            List<Item> keySet = new ArrayList<>(tradeableItems.keySet().stream().toList());
            keySet.sort(Comparator.comparing(tradeableItems::get));

            for (int i = 0; i < keySet.size(); i++) {
                int level = switch (i) {
                    case 2, 3 -> 2;
                    case 4, 5 -> 3;
                    case 6, 7 -> 4;
                    case 8, 9 -> 5;
                    default -> 1;
                };
                Item item = keySet.get(i);
                int cost = tradeableItems.get(item);

                trades.get(level).add((entity, randomSource) -> new MerchantOffer(
                        new ItemCost(Items.DIAMOND, random.nextInt(cost - 4, cost + 5)),
                        new ItemStack(item, 1),
                        random.nextInt(1, 2),
                        20 * level,
                        .2f
                ));
            }
        }

        if (event.getType() == ModVillagers.EVERNIGHT_PROFESSION.value()) {
            Random random = new Random();

            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            HashMap<Item, Integer> tradeableItems = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                switch (random.nextInt(3)) {
//                    case 0 -> {
//                        PotionRecipeItem recipe = PotionRecipeItemHandler.selectRandomRecipeOfPathway(random, "darkness");
//                        if (recipe == null)
//                            return;
//                        int sequence = recipe.getRecipe().potion().getSequence();
//                        tradeableItems.put(recipe, costsPerSequenceForRecipes[sequence]);
//                    }
                    case 0, 1, 2 -> {
                        BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfPathway(random, "darkness");
                        if (potion == null)
                            return;
                        int sequence = potion.getSequence();
                        tradeableItems.put(potion, costsPerSequence[sequence]);
                    }
//                    case 2 -> {
//                        PotionIngredient ingredient = ModIngredients.selectRandomIngredientOfPathway(random, "darkness");
//                        if (ingredient == null)
//                            return;
//                        int sequence = ingredient.getSequence();
//                        tradeableItems.put(ingredient, costsPerSequenceForIngredients[sequence]);
//                    }
                }
            }

            List<Item> keySet = new ArrayList<>(tradeableItems.keySet().stream().toList());
            keySet.sort(Comparator.comparing(tradeableItems::get));

            for (int i = 0; i < keySet.size(); i++) {
                int level = switch (i) {
                    case 2, 3 -> 2;
                    case 4, 5 -> 3;
                    case 6, 7 -> 4;
                    case 8, 9 -> 5;
                    default -> 1;
                };
                Item item = keySet.get(i);
                int cost = tradeableItems.get(item);

                trades.get(level).add((entity, randomSource) -> new MerchantOffer(
                        new ItemCost(Items.DIAMOND, random.nextInt(cost - 4, cost + 5)),
                        new ItemStack(item, 1),
                        random.nextInt(1, 2),
                        20 * level,
                        .2f
                ));
            }
        }
    }
}

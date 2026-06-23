package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MysteriousTabletData;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.item.PotionIngredient;
import de.jakob.lotm.beyonders.potions.*;
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
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
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
    private static final String ANCIENT_TRADER_TAG = "AncientTrader";

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

                // limit the sequence of items
                if(sequence >= 4) {
                    trades.get(level).add((entity, randomSource) -> {
                        Random random = new Random();
                        int diamondAmount = Math.max(1, random.nextInt(entry.getValue() - 4, entry.getValue() + 5));

                        ItemCost firstItemCost = new ItemCost(Items.DIAMOND, diamondAmount);

                        // change main item for seq4 items
                        if (sequence == 4) {
                            firstItemCost = new ItemCost(Items.ANCIENT_DEBRIS, diamondAmount);
                        }

                        java.util.Optional<ItemCost> additionalCost = getAdditionalCostForSequence(sequence, random);

                        return new MerchantOffer(
                                firstItemCost,
                                additionalCost,
                                new ItemStack(entry.getKey(), 1),
                                random.nextInt(1, 2),
                                30 * level,
                                0.005f
                        );
                    });
                }
            }
        }

        if (event.getType() == ModVillagers.ANCIENT_TRADER_PROFESSION.value()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            addAncientTraderPotionTrades(trades, 1, 4, 2);
            addAncientTraderPotionTrades(trades, 2, 3, 2);
            addAncientTraderPotionTrades(trades, 3, 2, 2);
            addAncientTraderPotionTrades(trades, 4, 1, 1);

            trades.get(5).add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(Items.DIAMOND, 64),
                    java.util.Optional.of(new ItemCost(Items.NETHER_STAR, 1)),
                    new ItemStack(ModItems.UPPER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get(), 1),
                    1,
                    30,
                    0.05f
            ));
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

            if(sequence >= 5) {
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
    }

    private static void addAncientTraderPotionTrades(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, int level, int sequence, int maxUses) {
        trades.get(level).add((entity, randomSource) -> createAncientTraderPotionOffer(level, sequence, maxUses, randomSource));
        trades.get(level).add((entity, randomSource) -> createAncientTraderPotionOffer(level, sequence, maxUses, randomSource));
    }

    public static MerchantOffers buildAncientTraderOffers(RandomSource randomSource, boolean includeFragment) {
        MerchantOffers offers = new MerchantOffers();
        addAncientTraderPotionOffers(offers, 4, 2, randomSource, 2);
        addAncientTraderPotionOffers(offers, 3, 2, randomSource, 2);
        addAncientTraderPotionOffers(offers, 2, 2, randomSource, 2);
        addAncientTraderPotionOffers(offers, 1, 1, randomSource, 1);

        if (includeFragment) {
            offers.add(new MerchantOffer(
                    new ItemCost(Items.DIAMOND, 64),
                    java.util.Optional.of(new ItemCost(Items.NETHER_STAR, 1)),
                    new ItemStack(ModItems.UPPER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get(), 1),
                    1,
                    30,
                    0.05f
            ));
        }

        return offers;
    }

    private static void addAncientTraderPotionOffers(MerchantOffers offers, int sequence, int maxUses, RandomSource randomSource, int count) {
        for (int i = 0; i < count; i++) {
            MerchantOffer offer = createAncientTraderPotionOffer(1, sequence, maxUses, randomSource);
            if (offer != null) {
                offers.add(offer);
            }
        }
    }

    private static MerchantOffer createAncientTraderPotionOffer(int level, int sequence, int maxUses, net.minecraft.util.RandomSource randomSource) {
        Random random = new Random(randomSource.nextLong());
        BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(random, sequence);
        if (potion == null) {
            return null;
        }

        BeyonderCharacteristicItem characteristic = BeyonderCharacteristicItemHandler
                .selectCharacteristicOfPathwayAndSequence(potion.getPathway(), potion.getSequence());

        ItemCost firstItemCost;
        java.util.Optional<ItemCost> additionalCost = java.util.Optional.empty();
        if (characteristic != null) {
            firstItemCost = new ItemCost(characteristic, 1);
        } else {
            int cost = costsPerSequence[sequence];
            int diamondAmount = Math.max(1, random.nextInt(cost - 4, cost + 5));
            firstItemCost = new ItemCost(Items.DIAMOND, diamondAmount);

            if (sequence == 4) {
                firstItemCost = new ItemCost(Items.ANCIENT_DEBRIS, diamondAmount);
            }

            additionalCost = getAdditionalCostForSequence(sequence, random);
        }

        return new MerchantOffer(
            firstItemCost,
            additionalCost,
            new ItemStack(potion, 1),
            maxUses,
            30 * level,
            0.005f
        );
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
    public static void onAncientTraderTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        if (villager.level().isClientSide) {
            return;
        }

        boolean isTagged = villager.getPersistentData().getBoolean(ANCIENT_TRADER_TAG);
        boolean isAncientProfession = villager.getVillagerData().getProfession() == ModVillagers.ANCIENT_TRADER_PROFESSION.value();
        if (!isTagged && !isAncientProfession) {
            return;
        }

        if (!isTagged) {
            villager.getPersistentData().putBoolean(ANCIENT_TRADER_TAG, true);
        }

        if (!villager.isPersistenceRequired()) {
            villager.setPersistenceRequired();
        }
    }

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (event.getLevel().isClientSide()) return;

        MerchantOffers offers = villager.getOffers();

        if(offers.isEmpty()) return;

        boolean isAncientTrader = villager.getVillagerData().getProfession() == ModVillagers.ANCIENT_TRADER_PROFESSION.value();

        if (event.getLevel() instanceof ServerLevel serverLevel) {
            MysteriousTabletData data = MysteriousTabletData.get(serverLevel.getServer());
            offers.removeIf(offer -> offer.getResult().is(ModItems.UPPER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get())
                && !data.canSpawnFragment(MysteriousTabletData.FragmentType.UPPER));
        }

        offers.removeIf(offer -> {
                    if (isAncientTrader) {
                        return false;
                    }
                    var item = offer.getResult().getItem();

                    if(item instanceof PotionIngredient obj){
                        for(var path : obj.getPathways()){
                            return !BeyonderData.playerMap.check(path,obj.getSequence()) || obj.getSequence() < 4;
                        }
                    }

                    if(item instanceof BeyonderPotion potion){
                        return !BeyonderData.playerMap.check(potion.getPathway(), potion.getSequence()) || potion.getSequence() < 4;
                    }

                    if(item instanceof BeyonderCharacteristicItem cha){
                        return !BeyonderData.playerMap.check(cha.getPathway(), cha.getSequence()) || cha.getSequence() < 4;
                    }

                    return false;
                }
        );
    }

}
package de.jakob.lotm.gui.custom.DailySpin;

import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.potions.PotionRecipes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Server-side logic for the Daily Spin and Sell Your Soul systems.
 */
public class DailySpinHandler {

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final float JACKPOT_CHANCE = 0.0001f; // 0.01%

    private static final List<Supplier<? extends Item>> UNIQUENESS_ITEMS = List.of(
            ModItems.RED_PRIEST_UNIQUENESS,
            ModItems.TYRANT_UNIQUENESS,
            ModItems.SUN_UNIQUENESS,
            ModItems.FOOL_UNIQUENESS,
            ModItems.ERROR_UNIQUENESS,
            ModItems.VISIONARY_UNIQUENESS,
            ModItems.DOOR_UNIQUENESS,
            ModItems.DARKNESS_UNIQUENESS,
            ModItems.WHEEL_OF_FORTUNE_UNIQUENESS,
            ModItems.ABYSS_UNIQUENESS,
            ModItems.MOTHER_UNIQUENESS,
            ModItems.DEMONESS_UNIQUENESS,
            ModItems.JUSTICIAR_UNIQUENESS
    );

    // ── Daily Spin ─────────────────────────────────────────────────────────────

    public record SpinResult(ItemStack reward, List<String> reelNames, int landingIndex) {}

    /**
     * Builds the reel display data and determines the actual reward for the player.
     * Loot pool:
     *   0.01%  — random Uniqueness (any pathway)
     *   ~50%   — random characteristic of the player's current seq (any pathway)
     *   ~50%   — random recipe of the player's current seq (any pathway)
     */
    public static SpinResult buildDailySpin(ServerPlayer player) {
        String pathway = BeyonderData.getPathway(player);
        int seq        = BeyonderData.getSequence(player);
        // Clamp to valid Beyonder range; non-Beyonders (seq=10) default to seq 9
        if (seq < 1 || seq > 9) seq = 9;
        Random rand    = new Random();

        // Ensure recipes are initialised
        if (!PotionRecipes.initialized) {
            PotionRecipes.initPotionRecipes();
            PotionRecipeItemHandler.initializeRecipes();
        }

        // 1. Determine actual reward
        boolean isJackpot = rand.nextFloat() < JACKPOT_CHANCE;

        ItemStack reward;
        String rewardName;

        net.minecraft.server.level.ServerLevel serverLevel = player.serverLevel();
        if (isJackpot) {
            reward     = getRandomUniqueness(rand);
            rewardName = "★ JACKPOT ★";
        } else {
            // 50/50 between characteristic (slot-filtered, same seq) and recipe (same seq)
            if (rand.nextBoolean()) {
                ItemStack char_ = getRandomCharacteristicAtSeq(seq, rand, serverLevel);
                if (!char_.isEmpty()) {
                    reward     = char_;
                    rewardName = char_.getHoverName().getString();
                } else {
                    ItemStack recipe = getRandomRecipeAtSeq(seq, rand);
                    reward     = recipe;
                    rewardName = recipe.isEmpty() ? "???" : recipe.getHoverName().getString();
                }
            } else {
                ItemStack recipe = getRandomRecipeAtSeq(seq, rand);
                if (!recipe.isEmpty()) {
                    reward     = recipe;
                    rewardName = recipe.getHoverName().getString();
                } else {
                    ItemStack char_ = getRandomCharacteristicAtSeq(seq, rand, serverLevel);
                    reward     = char_;
                    rewardName = char_.isEmpty() ? "???" : char_.getHoverName().getString();
                }
            }
        }

        // 2. Build the visual reel using item names from pathways that still have seq slots available.
        //    This prevents the reel from showing options that are impossible to progress through.
        List<String> allCharNames = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : BeyonderCharacteristicItemHandler.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof BeyonderCharacteristicItem c && c.getSequence() == seq) {
                // Only include if there is a slot available at this sequence for this pathway
                if (BeyonderData.hasSequenceSlotAvailable(player.serverLevel(), c.getPathway(), seq)) {
                    allCharNames.add(new ItemStack(item).getHoverName().getString());
                }
            }
        }
        // Fallback: if all slots are taken, show everything so the reel is never empty
        if (allCharNames.isEmpty()) {
            for (DeferredHolder<Item, ? extends Item> holder : BeyonderCharacteristicItemHandler.ITEMS.getEntries()) {
                Item item = holder.get();
                if (item instanceof BeyonderCharacteristicItem c && c.getSequence() == seq) {
                    allCharNames.add(new ItemStack(item).getHoverName().getString());
                }
            }
        }
        List<String> allRecipeNames = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : PotionRecipeItemHandler.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof PotionRecipeItem r && r.getRecipe() != null
                    && r.getRecipe().potion().getSequence() == seq) {
                if (BeyonderData.hasSequenceSlotAvailable(player.serverLevel(), r.getRecipe().potion().getPathway(), seq)) {
                    allRecipeNames.add(new ItemStack(item).getHoverName().getString());
                }
            }
        }
        if (allRecipeNames.isEmpty()) {
            for (DeferredHolder<Item, ? extends Item> holder : PotionRecipeItemHandler.ITEMS.getEntries()) {
                Item item = holder.get();
                if (item instanceof PotionRecipeItem r && r.getRecipe() != null
                        && r.getRecipe().potion().getSequence() == seq) {
                    allRecipeNames.add(new ItemStack(item).getHoverName().getString());
                }
            }
        }
        List<String> reel = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            reel.add(allCharNames.isEmpty() ? "???" : allCharNames.get(rand.nextInt(allCharNames.size())));
        }
        for (int i = 0; i < 10; i++) {
            reel.add(allRecipeNames.isEmpty() ? "???" : allRecipeNames.get(rand.nextInt(allRecipeNames.size())));
        }
        reel.add("★ JACKPOT ★");
        if (reel.size() < 5) { reel.add("???"); reel.add("???"); }
        Collections.shuffle(reel, rand);

        // 3. Replace one slot with the real reward name
        int landingIndex = rand.nextInt(reel.size());
        reel.set(landingIndex, rewardName);

        return new SpinResult(reward, reel, landingIndex);
    }

    // ── Sell Your Soul ─────────────────────────────────────────────────────────

    /**
     * Outcome codes:
     *   0 = sanity set to 50%
     *   1 = digestion set to 0
     *   2 = ad overlay (client-side only)
     *   3 = random same-seq characteristic from any pathway (5%)
     *   4 = reverted to Sequence 9 of current pathway (0.01%)
     */
    public record SoulResult(int outcome, ItemStack rewardItem, String rewardName) {}

    public static SoulResult buildSoulResult(ServerPlayer player) {
        int seq     = BeyonderData.getSequence(player);
        Random rand = new Random();
        float roll  = rand.nextFloat();

        if (roll < 0.0001f) {
            // 0.01%: revert to Sequence 9
            return new SoulResult(4, ItemStack.EMPTY, "");
        } else if (roll < 0.0501f) {
            // 5%: random same-seq characteristic (slot-filtered)
            ItemStack item = getRandomCharacteristicAtSeq(seq, rand, player.serverLevel());
            String name = item.isEmpty() ? "???" : item.getHoverName().getString();
            return new SoulResult(3, item, name);
        } else if (roll < 0.3501f) {
            // 30%: sanity drain
            return new SoulResult(0, ItemStack.EMPTY, "");
        } else if (roll < 0.6501f) {
            // 30%: digestion wipe
            return new SoulResult(1, ItemStack.EMPTY, "");
        } else {
            // ~35%: watch an ad
            return new SoulResult(2, ItemStack.EMPTY, "");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static ItemStack getRandomRecipeAtSeq(int seq, Random rand) {
        PotionRecipeItem item = PotionRecipeItemHandler.selectRandomRecipeOfSequence(rand, seq);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    static ItemStack getRandomCharacteristicAtSeq(int seq, Random rand) {
        return getRandomCharacteristicAtSeq(seq, rand, null);
    }

    static ItemStack getRandomCharacteristicAtSeq(int seq, Random rand, ServerLevel level) {
        List<ItemStack> options = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : BeyonderCharacteristicItemHandler.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof BeyonderCharacteristicItem c && c.getSequence() == seq) {
                if (level == null || BeyonderData.hasSequenceSlotAvailable(level, c.getPathway(), seq)) {
                    options.add(new ItemStack(item));
                }
            }
        }
        // Fallback if all slots are full
        if (options.isEmpty()) {
            for (DeferredHolder<Item, ? extends Item> holder : BeyonderCharacteristicItemHandler.ITEMS.getEntries()) {
                Item item = holder.get();
                if (item instanceof BeyonderCharacteristicItem c && c.getSequence() == seq) {
                    options.add(new ItemStack(item));
                }
            }
        }
        if (options.isEmpty()) return ItemStack.EMPTY;
        return options.get(rand.nextInt(options.size())).copy();
    }

    private static ItemStack getRandomUniqueness(Random rand) {
        return new ItemStack(UNIQUENESS_ITEMS.get(rand.nextInt(UNIQUENESS_ITEMS.size())).get());
    }
}

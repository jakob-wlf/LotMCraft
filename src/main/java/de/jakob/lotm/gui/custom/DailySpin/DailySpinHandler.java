package de.jakob.lotm.gui.custom.DailySpin;

import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
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
     *   ~100%  — random potion one sequence higher (seq-1), slot-filtered;
     *            falls back to same seq if seq-1 is unavailable or below 1
     */
    public static SpinResult buildDailySpin(ServerPlayer player) {
        int seq = BeyonderData.getSequence(player);
        if (seq < 1 || seq > 9) seq = 9;
        Random rand = new Random();

        boolean isJackpot = rand.nextFloat() < JACKPOT_CHANCE;
        ItemStack reward;
        String rewardName;
        int rewardSeq;

        if (isJackpot) {
            reward     = getRandomUniqueness(rand);
            rewardName = "\u2605 JACKPOT \u2605";
            rewardSeq  = seq;
        } else {
            // Try one seq higher (lower number), strictly slot-filtered only.
            // If no slots exist at seq-1, fall back to same seq.
            int higherSeq = seq - 1;
            ItemStack potion = ItemStack.EMPTY;
            rewardSeq = seq;
            if (higherSeq >= 1) {
                potion = getRandomPotionAtSeqStrict(higherSeq, rand, player.serverLevel());
                if (!potion.isEmpty()) rewardSeq = higherSeq;
            }
            if (potion.isEmpty()) {
                potion = getRandomPotionAtSeq(seq, rand);
            }
            reward     = potion;
            rewardName = potion.isEmpty() ? "???" : potion.getHoverName().getString();
        }

        // Build reel from potion names at the reward sequence (same strict filter)
        List<String> potionNames = getPotionNamesAtSeqStrict(rewardSeq, player.serverLevel());
        if (potionNames.isEmpty()) potionNames = getPotionNamesAtSeq(rewardSeq);
        List<String> reel = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            reel.add(potionNames.isEmpty() ? "???" : potionNames.get(rand.nextInt(potionNames.size())));
        }
        reel.add("\u2605 JACKPOT \u2605");
        Collections.shuffle(reel, rand);

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
     *   5 = random same-seq potion from any pathway (10%)
     */
    public record SoulResult(int outcome, ItemStack rewardItem, String rewardName) {}

    public static SoulResult buildSoulResult(ServerPlayer player) {
        int seq     = BeyonderData.getSequence(player);
        Random rand = new Random();
        float roll  = rand.nextFloat();

        if (roll < 0.0001f) {
            // 0.01%: revert to Sequence 9
            return new SoulResult(4, ItemStack.EMPTY, "");
        } else if (roll < 0.1001f) {
            // 10%: random same-seq characteristic (slot-filtered)
            ItemStack item = getRandomCharacteristicAtSeq(seq, rand, player.serverLevel());
            String name = item.isEmpty() ? "???" : item.getHoverName().getString();
            return new SoulResult(3, item, name);
        } else if (roll < 0.2001f) {
            // 10%: random same-seq potion from any pathway
            ItemStack item = getRandomPotionAtSeq(seq, rand);
            String name = item.isEmpty() ? "???" : item.getHoverName().getString();
            return new SoulResult(5, item, name);
        } else if (roll < 0.4001f) {
            // 10%: sanity drain
            return new SoulResult(0, ItemStack.EMPTY, "");
        } else if (roll < 0.5001f) {
            // 10%: digestion wipe
            return new SoulResult(1, ItemStack.EMPTY, "");
        } else {
            // ~60%: watch an ad
            return new SoulResult(2, ItemStack.EMPTY, "");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Strictly slot-filtered: returns empty if no slots are available at this seq. */
    private static ItemStack getRandomPotionAtSeqStrict(int seq, Random rand, ServerLevel level) {
        List<ItemStack> options = new ArrayList<>();
        for (String pathway : BeyonderData.pathwayInfos.keySet()) {
            if (!BeyonderData.hasSequenceSlotAvailable(level, pathway, seq)) continue;
            BeyonderPotion potion = PotionItemHandler.selectPotionOfPathwayAndSequence(rand, pathway, seq);
            if (potion != null) options.add(potion.getDefaultInstance());
        }
        if (options.isEmpty()) return ItemStack.EMPTY;
        return options.get(rand.nextInt(options.size())).copy();
    }

    private static List<String> getPotionNamesAtSeqStrict(int seq, ServerLevel level) {
        List<String> names = new ArrayList<>();
        for (String pathway : BeyonderData.pathwayInfos.keySet()) {
            if (!BeyonderData.hasSequenceSlotAvailable(level, pathway, seq)) continue;
            BeyonderPotion potion = PotionItemHandler.selectPotionOfPathwayAndSequence(null, pathway, seq);
            if (potion != null) names.add(potion.getDefaultInstance().getHoverName().getString());
        }
        return names;
    }

    private static List<String> getPotionNamesAtSeq(int seq) {
        List<String> names = new ArrayList<>();
        for (String pathway : BeyonderData.pathwayInfos.keySet()) {
            BeyonderPotion potion = PotionItemHandler.selectPotionOfPathwayAndSequence(null, pathway, seq);
            if (potion != null) names.add(potion.getDefaultInstance().getHoverName().getString());
        }
        return names;
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

    private static ItemStack getRandomPotionAtSeq(int seq, Random rand) {
        List<ItemStack> options = new ArrayList<>();
        for (String pathway : BeyonderData.pathwayInfos.keySet()) {
            BeyonderPotion potion = PotionItemHandler.selectPotionOfPathwayAndSequence(rand, pathway, seq);
            if (potion != null) options.add(potion.getDefaultInstance());
        }
        if (options.isEmpty()) return ItemStack.EMPTY;
        return options.get(rand.nextInt(options.size())).copy();
    }

    private static ItemStack getRandomUniqueness(Random rand) {
        return new ItemStack(UNIQUENESS_ITEMS.get(rand.nextInt(UNIQUENESS_ITEMS.size())).get());
    }
}

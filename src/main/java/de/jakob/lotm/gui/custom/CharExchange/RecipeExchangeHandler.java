package de.jakob.lotm.gui.custom.CharExchange;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenCharExchangeWheelPacket;
import de.jakob.lotm.beyonders.potions.PotionRecipeItem;
import de.jakob.lotm.beyonders.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Server-side logic for the Recipe Exchange.
 *
 * Per-pathway logic:
 *   - For each pathway, tries to give a recipe one seq higher (seq-1), slot-filtered.
 *   - Falls back to same-seq recipe for any pathway that has no slot at seq-1.
 *   - Excludes the sacrificed recipe's own pathway.
 */
public class RecipeExchangeHandler {

    public static void processExchange(ServerPlayer player, int slotIndex) {
        if (!player.level().getGameRules().getBoolean(de.jakob.lotm.gamerule.ModGameRules.DO_CHAR_EXCHANGE_WHEEL)) {
            player.sendSystemMessage(Component.literal("§cRecipe Exchange is disabled."));
            return;
        }
        if (slotIndex < 0 || slotIndex >= player.getInventory().getContainerSize()) return;
        ItemStack sacrificed = player.getInventory().getItem(slotIndex);
        if (sacrificed.isEmpty() || !(sacrificed.getItem() instanceof PotionRecipeItem recipeItem)) return;
        if (recipeItem.getRecipe() == null) return;

        int sacrificedSeq = recipeItem.getRecipe().potion().getSequence();
        String sacrificedPathway = recipeItem.getRecipe().potion().getPathway();

        player.getInventory().setItem(slotIndex, ItemStack.EMPTY);

        Random rand = ThreadLocalRandom.current();

        List<ItemStack> pool = buildRecipePool(sacrificedSeq, sacrificedSeq - 1, sacrificedPathway);
        ItemStack rewardItem = pool.isEmpty() ? ItemStack.EMPTY : pool.get(rand.nextInt(pool.size())).copy();
        String rewardName = rewardItem.isEmpty() ? "???" : rewardItem.getHoverName().getString();
        List<String> candidates = pool.stream()
                .map(s -> s.getHoverName().getString()).distinct()
                .collect(java.util.stream.Collectors.toList());

        if (!rewardItem.isEmpty()) {
            player.getInventory().add(rewardItem.copy());
        }

        player.containerMenu.broadcastChanges();
        PacketHandler.sendToPlayer(player, buildWheelPacket("Recipe Exchange", rewardName, candidates, rand));
    }

    private static OpenCharExchangeWheelPacket buildWheelPacket(String title, String rewardName, List<String> candidates, Random rand) {
        List<String> reel = new ArrayList<>();
        for (int i = 0; i < 20; i++) reel.add(candidates.isEmpty() ? "???" : candidates.get(rand.nextInt(candidates.size())));
        Collections.shuffle(reel, rand);
        int landingIndex = rand.nextInt(reel.size());
        reel.set(landingIndex, rewardName);
        return new OpenCharExchangeWheelPacket(reel, landingIndex, CharExchangeHandler.OUTCOME_UPGRADE, rewardName, title);
    }

    /** Builds the full recipe candidate pool: seq-1 per pathway if a recipe exists there, else same seq.
     * The sacrificed pathway is excluded from same-seq fallback only — it's still eligible for seq-1. */
    private static List<ItemStack> buildRecipePool(int sameSeq, int higherSeq, String excludePathway) {
        List<ItemStack> options = new ArrayList<>();
        for (String pathway : BeyonderData.pathwayInfos.keySet()) {
            PotionRecipeItem recipe = null;
            if (higherSeq >= 1) recipe = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, higherSeq);
            if (recipe == null && !pathway.equals(excludePathway))
                recipe = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, sameSeq);
            if (recipe != null) options.add(new ItemStack(recipe));
        }
        return options;
    }
}

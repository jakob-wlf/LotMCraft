package de.jakob.lotm.gui.custom.CharExchange;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenCharExchangeWheelPacket;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
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
 * Server-side logic for the Recipe Path Exchange.
 *
 * Always gives a recipe at the same sequence as the sacrificed one,
 * but from a randomly different pathway.
 */
public class RecipePathExchangeHandler {

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

        List<ItemStack> pool = buildRecipePoolAtSeq(sacrificedSeq, sacrificedPathway);
        ItemStack rewardItem = pool.isEmpty() ? ItemStack.EMPTY : pool.get(rand.nextInt(pool.size())).copy();
        String rewardName = rewardItem.isEmpty() ? "???" : rewardItem.getHoverName().getString();
        List<String> candidates = pool.stream()
                .map(s -> s.getHoverName().getString()).distinct()
                .collect(java.util.stream.Collectors.toList());

        if (!rewardItem.isEmpty()) {
            player.getInventory().add(rewardItem.copy());
        }

        player.containerMenu.broadcastChanges();
        PacketHandler.sendToPlayer(player, buildWheelPacket("Recipe Path Exchange", rewardName, candidates, rand));
    }

    private static OpenCharExchangeWheelPacket buildWheelPacket(String title, String rewardName, List<String> candidates, Random rand) {
        List<String> reel = new ArrayList<>();
        for (int i = 0; i < 20; i++) reel.add(candidates.isEmpty() ? "???" : candidates.get(rand.nextInt(candidates.size())));
        Collections.shuffle(reel, rand);
        int landingIndex = rand.nextInt(reel.size());
        reel.set(landingIndex, rewardName);
        return new OpenCharExchangeWheelPacket(reel, landingIndex, CharExchangeHandler.OUTCOME_UPGRADE, rewardName, title);
    }

    private static List<ItemStack> buildRecipePoolAtSeq(int seq, String excludePathway) {
        List<ItemStack> options = new ArrayList<>();
        for (String pathway : BeyonderData.pathwayInfos.keySet()) {
            if (pathway.equals(excludePathway)) continue;
            PotionRecipeItem recipe = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, seq);
            if (recipe != null) options.add(new ItemStack(recipe));
        }
        return options;
    }
}

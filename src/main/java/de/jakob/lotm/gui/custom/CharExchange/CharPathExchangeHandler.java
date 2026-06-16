package de.jakob.lotm.gui.custom.CharExchange;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenCharExchangeWheelPacket;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Server-side logic for the Char Path Exchange.
 *
 * Same odds as CharExchangeHandler (5 / 10 / 85), but the jackpot (5%)
 * gives a completely random characteristic from ANY pathway at ANY sequence
 * (1-9) rather than one rank higher.
 *
 * Outcomes (reuse OpenCharExchangeWheelPacket codes):
 *   0 = Garbage       (85%)
 *   1 = GarbageCollect (10%)
 *   2 = Random Char    (5%)
 */
public class CharPathExchangeHandler {

    public static void processExchange(ServerPlayer player, int slotIndex) {
        if (!player.level().getGameRules().getBoolean(de.jakob.lotm.gamerule.ModGameRules.DO_CHAR_EXCHANGE_WHEEL)) {
            player.sendSystemMessage(Component.literal("§cCharacteristics Exchange is disabled."));
            return;
        }
        if (slotIndex < 0 || slotIndex >= player.getInventory().getContainerSize()) return;
        ItemStack sacrificed = player.getInventory().getItem(slotIndex);
        if (sacrificed.isEmpty() || !(sacrificed.getItem() instanceof BeyonderCharacteristicItem charItem)) return;

        int sacrificedSeq = charItem.getSequence();
        String sacrificedPathway = charItem.getPathway();

        // Remove the sacrificed characteristic
        player.getInventory().setItem(slotIndex, ItemStack.EMPTY);

        Random rand = ThreadLocalRandom.current();

        // Always give a characteristic at the same sequence from a different pathway
        List<ItemStack> pool = buildCharPoolAtSeq(sacrificedSeq, sacrificedPathway);
        ItemStack rewardItem = pool.isEmpty() ? ItemStack.EMPTY : pool.get(rand.nextInt(pool.size())).copy();
        String rewardName = rewardItem.isEmpty() ? "???" : rewardItem.getHoverName().getString();
        List<String> candidates = pool.stream()
                .map(s -> s.getHoverName().getString()).distinct()
                .collect(java.util.stream.Collectors.toList());

        if (!rewardItem.isEmpty()) {
            player.getInventory().add(rewardItem.copy());
        }

        // Sync inventory to client immediately
        player.containerMenu.broadcastChanges();

        PacketHandler.sendToPlayer(player, buildWheelPacket("Char Path Exchange", rewardName, candidates, rand));
    }

    // ── Reel builder ─────────────────────────────────────────────────────────

    private static OpenCharExchangeWheelPacket buildWheelPacket(String title, String rewardName, List<String> candidates, Random rand) {
        List<String> reel = new ArrayList<>();
        for (int i = 0; i < 20; i++) reel.add(candidates.isEmpty() ? "???" : candidates.get(rand.nextInt(candidates.size())));
        Collections.shuffle(reel, rand);
        int landingIndex = rand.nextInt(reel.size());
        reel.set(landingIndex, rewardName);
        return new OpenCharExchangeWheelPacket(reel, landingIndex, CharExchangeHandler.OUTCOME_UPGRADE, rewardName, title);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static List<ItemStack> buildCharPoolAtSeq(int seq, String excludePathway) {
        List<ItemStack> options = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : BeyonderCharacteristicItemHandler.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof BeyonderCharacteristicItem c && c.getSequence() == seq
                    && !c.getPathway().equals(excludePathway)) {
                options.add(new ItemStack(item));
            }
        }
        return options;
    }
}

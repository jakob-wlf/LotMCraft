package de.jakob.lotm.gui.custom.CharExchange;

import de.jakob.lotm.item.custom.GarbageItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenCharExchangeWheelPacket;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
 * Server-side logic for the Characteristics Exchange.
 *
 * Outcomes:
 *   0 = Garbage       (85%) — gives the player a Garbage item locked to a slot
 *   1 = GarbageCollect (10%) — removes ALL Garbage items from the player's inventory
 *   2 = Upgrade         (5%) — gives a random characteristic one sequence higher
 *                              (e.g. sacrifice seq 6 → receive seq 5)
 */
public class CharExchangeHandler {

    // ── Outcome constants ────────────────────────────────────────────────────
    public static final int OUTCOME_GARBAGE         = 0;
    public static final int OUTCOME_GARBAGE_COLLECT = 1;
    public static final int OUTCOME_UPGRADE         = 2;

    // ── Reel label sets ──────────────────────────────────────────────────────
    private static final String[] OUTCOME_LABELS = {
            "\uD83D\uDDD1 Garbage",
            "\uD83E\uDDF9 Garbage Collector",
            "\u2605 Fate's Favour"
    };

    // ── Entry point ──────────────────────────────────────────────────────────

    /**
     * Called on the server when the player confirms their characteristic choice.
     *
     * @param player    the player performing the exchange
     * @param slotIndex the inventory slot (0-35) that contains the chosen characteristic
     */
    public static void processExchange(ServerPlayer player, int slotIndex) {
        if (!player.level().getGameRules().getBoolean(de.jakob.lotm.gamerule.ModGameRules.DO_CHAR_EXCHANGE_WHEEL)) {
            player.sendSystemMessage(Component.literal("§cCharacteristics Exchange is disabled."));
            return;
        }
        // Validate slot
        if (slotIndex < 0 || slotIndex >= player.getInventory().getContainerSize()) return;
        ItemStack sacrificed = player.getInventory().getItem(slotIndex);
        if (sacrificed.isEmpty() || !(sacrificed.getItem() instanceof BeyonderCharacteristicItem charItem)) return;

        int sacrificedSeq = charItem.getSequence();
        String sacrificedPathway = charItem.getPathway();

        // Remove the sacrificed characteristic
        player.getInventory().setItem(slotIndex, ItemStack.EMPTY);

        Random rand = ThreadLocalRandom.current();

        // Per-pathway pool: use seq-1 for pathways that have a slot there,
        // fall back to same seq for pathways that don't.
        int higherSeq = sacrificedSeq - 1;
        List<ItemStack> pool = buildCharPool(sacrificedSeq, higherSeq, sacrificedPathway, player.serverLevel());
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

        PacketHandler.sendToPlayer(player, buildWheelPacket("Characteristics Exchange", rewardName, candidates, rand));
    }

    // ── Reel builder ─────────────────────────────────────────────────────────

    private static OpenCharExchangeWheelPacket buildWheelPacket(String title, String rewardName, List<String> candidates, Random rand) {
        List<String> reel = new ArrayList<>();
        for (int i = 0; i < 20; i++) reel.add(candidates.isEmpty() ? "???" : candidates.get(rand.nextInt(candidates.size())));
        Collections.shuffle(reel, rand);
        int landingIndex = rand.nextInt(reel.size());
        reel.set(landingIndex, rewardName);
        return new OpenCharExchangeWheelPacket(reel, landingIndex, OUTCOME_UPGRADE, rewardName, title);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Builds the full candidate pool across all pathways (excluding excludePathway).
     * For each pathway: use higherSeq if a slot is available there, otherwise use sameSeq. */
    private static List<ItemStack> buildCharPool(int sameSeq, int higherSeq, String excludePathway, ServerLevel level) {
        List<ItemStack> options = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : BeyonderCharacteristicItemHandler.ITEMS.getEntries()) {
            Item item = holder.get();
            if (!(item instanceof BeyonderCharacteristicItem c)) continue;
            if (c.getPathway().equals(excludePathway)) continue;
            boolean useHigher = higherSeq >= 1
                    && BeyonderData.hasSequenceSlotAvailable(level, c.getPathway(), higherSeq);
            int targetSeq = useHigher ? higherSeq : sameSeq;
            if (c.getSequence() == targetSeq) {
                options.add(new ItemStack(item));
            }
        }
        return options;
    }

    /** Removes every GarbageItem stack from the player's main inventory (slots 0-35). */
    public static void removeAllGarbageFromInventory(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof GarbageItem) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }
}

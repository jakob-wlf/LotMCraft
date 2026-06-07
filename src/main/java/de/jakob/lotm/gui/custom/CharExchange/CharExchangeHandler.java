package de.jakob.lotm.gui.custom.CharExchange;

import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.item.custom.GarbageItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenCharExchangeWheelPacket;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
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
        // Validate slot
        if (slotIndex < 0 || slotIndex >= player.getInventory().getContainerSize()) return;
        ItemStack sacrificed = player.getInventory().getItem(slotIndex);
        if (sacrificed.isEmpty() || !(sacrificed.getItem() instanceof BeyonderCharacteristicItem charItem)) return;

        int sacrificedSeq = charItem.getSequence();

        // Remove the sacrificed characteristic
        player.getInventory().setItem(slotIndex, ItemStack.EMPTY);

        // Roll outcome — use ThreadLocalRandom so each call gets a properly seeded
        // per-thread instance rather than a freshly constructed Random whose seed
        // entropy can be very low on a busy server tick.
        Random rand = ThreadLocalRandom.current();
        float roll = rand.nextFloat();

        int outcome;
        ItemStack rewardItem = ItemStack.EMPTY;
        String rewardName;

        if (roll < 0.05f) {
            // 5% — upgrade (seq - 1 = one rank higher)
            outcome = OUTCOME_UPGRADE;
            int targetSeq = Math.max(1, sacrificedSeq - 1);
            rewardItem = getRandomCharacteristicAtSeq(targetSeq, rand);
            rewardName = rewardItem.isEmpty() ? "???" : rewardItem.getHoverName().getString();
        } else if (roll < 0.15f) {
            // 10% — garbage collector
            outcome = OUTCOME_GARBAGE_COLLECT;
            removeAllGarbageFromInventory(player);
            rewardName = "Inventory Cleansed";
        } else {
            // 85% — garbage
            outcome = OUTCOME_GARBAGE;
            rewardName = "Garbage";
        }

        // Give reward item (upgrade only — garbage is given after slot assignment)
        if (outcome == OUTCOME_UPGRADE && !rewardItem.isEmpty()) {
            player.getInventory().add(rewardItem.copy());
        }

        // Give garbage item — use add() so the server properly syncs the slot to the client,
        // then immediately scan inventory to find it and lock it to that slot.
        if (outcome == OUTCOME_GARBAGE) {
            ItemStack garbageStack = new ItemStack(ModItems.GARBAGE.get());
            boolean added = player.getInventory().add(garbageStack);
            if (added) {
                // Find the newly added garbage item (no locked slot yet) and lock it
                for (int i = 0; i < 36; i++) {
                    ItemStack s = player.getInventory().getItem(i);
                    if (!s.isEmpty() && s.getItem() instanceof GarbageItem
                            && GarbageItem.getLockedSlot(s) < 0) {
                        GarbageItem.lockToSlot(s, i);
                        player.getInventory().setItem(i, s);
                        break;
                    }
                }
            } else {
                // Inventory full — drop at feet
                player.drop(garbageStack, false);
            }
        }

        // Sync inventory to client immediately so the reward / garbage appears without needing to re-open inventory.
        player.containerMenu.broadcastChanges();

        // Build visual reel
        OpenCharExchangeWheelPacket packet = buildWheelPacket(outcome, rewardName, sacrificedSeq, rand);
        PacketHandler.sendToPlayer(player, packet);
    }

    // ── Reel builder ─────────────────────────────────────────────────────────

    private static OpenCharExchangeWheelPacket buildWheelPacket(
            int outcome, String rewardName, int sacrificedSeq, Random rand) {

        // Build a reel weighted to match odds: 17 garbage, 2 gc, 1 upgrade
        List<String> reel = new ArrayList<>();
        for (int i = 0; i < 17; i++) reel.add(OUTCOME_LABELS[OUTCOME_GARBAGE]);
        for (int i = 0; i < 2;  i++) reel.add(OUTCOME_LABELS[OUTCOME_GARBAGE_COLLECT]);
        reel.add(OUTCOME_LABELS[OUTCOME_UPGRADE]);
        Collections.shuffle(reel, rand);

        // Find first slot that matches the actual outcome, use it as landing index
        int landingIndex = 0;
        String targetLabel = OUTCOME_LABELS[outcome];
        for (int i = 0; i < reel.size(); i++) {
            if (reel.get(i).equals(targetLabel)) { landingIndex = i; break; }
        }
        // Replace landing slot with the specific reward name for the winning label
        reel.set(landingIndex, outcome == OUTCOME_GARBAGE ? "\uD83D\uDDD1 " + rewardName
                : outcome == OUTCOME_GARBAGE_COLLECT     ? "\uD83E\uDDF9 " + rewardName
                : "\u2605 " + rewardName);

        return new OpenCharExchangeWheelPacket(reel, landingIndex, outcome, rewardName);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static ItemStack getRandomCharacteristicAtSeq(int seq, Random rand) {
        List<ItemStack> options = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : BeyonderCharacteristicItemHandler.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof BeyonderCharacteristicItem c && c.getSequence() == seq) {
                options.add(new ItemStack(item));
            }
        }
        if (options.isEmpty()) return ItemStack.EMPTY;
        return options.get(rand.nextInt(options.size())).copy();
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

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
        if (slotIndex < 0 || slotIndex >= player.getInventory().getContainerSize()) return;
        ItemStack sacrificed = player.getInventory().getItem(slotIndex);
        if (sacrificed.isEmpty() || !(sacrificed.getItem() instanceof BeyonderCharacteristicItem)) return;

        // Remove the sacrificed characteristic
        player.getInventory().setItem(slotIndex, ItemStack.EMPTY);

        Random rand = new Random();
        float roll = rand.nextFloat();

        int outcome;
        ItemStack rewardItem = ItemStack.EMPTY;
        String rewardName;

        if (roll < 1.0f) {
            // 100% TEST — random characteristic from any pathway, any sequence 1-9
            outcome = CharExchangeHandler.OUTCOME_UPGRADE;
            rewardItem = getRandomCharacteristicAnySeq(rand);
            rewardName = rewardItem.isEmpty() ? "???" : rewardItem.getHoverName().getString();
        } else if (roll < 0.15f) {
            // 10% — garbage collector
            outcome = CharExchangeHandler.OUTCOME_GARBAGE_COLLECT;
            CharExchangeHandler.removeAllGarbageFromInventory(player);
            rewardName = "Inventory Cleansed";
        } else {
            // 85% — garbage
            outcome = CharExchangeHandler.OUTCOME_GARBAGE;
            rewardName = "Garbage";
        }

        // Give reward
        if (outcome == CharExchangeHandler.OUTCOME_UPGRADE && !rewardItem.isEmpty()) {
            player.getInventory().add(rewardItem.copy());
        }

        if (outcome == CharExchangeHandler.OUTCOME_GARBAGE) {
            ItemStack garbageStack = new ItemStack(ModItems.GARBAGE.get());
            boolean added = player.getInventory().add(garbageStack);
            if (added) {
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
                player.drop(garbageStack, false);
            }
        }

        PacketHandler.sendToPlayer(player, buildWheelPacket(outcome, rewardName, rand));
    }

    // ── Reel builder ─────────────────────────────────────────────────────────

    private static final String[] OUTCOME_LABELS = {
            "\uD83D\uDDD1 Garbage",
            "\uD83E\uDDF9 Garbage Collector",
            "\u2605 Fate's Favour"
    };

    private static OpenCharExchangeWheelPacket buildWheelPacket(int outcome, String rewardName, Random rand) {
        List<String> reel = new ArrayList<>();
        for (int i = 0; i < 17; i++) reel.add(OUTCOME_LABELS[CharExchangeHandler.OUTCOME_GARBAGE]);
        for (int i = 0; i < 2;  i++) reel.add(OUTCOME_LABELS[CharExchangeHandler.OUTCOME_GARBAGE_COLLECT]);
        reel.add(OUTCOME_LABELS[CharExchangeHandler.OUTCOME_UPGRADE]);
        Collections.shuffle(reel, rand);

        int landingIndex = 0;
        String targetLabel = OUTCOME_LABELS[outcome];
        for (int i = 0; i < reel.size(); i++) {
            if (reel.get(i).equals(targetLabel)) { landingIndex = i; break; }
        }

        reel.set(landingIndex, outcome == CharExchangeHandler.OUTCOME_GARBAGE ? "\uD83D\uDDD1 " + rewardName
                : outcome == CharExchangeHandler.OUTCOME_GARBAGE_COLLECT     ? "\uD83E\uDDF9 " + rewardName
                : "\u2605 " + rewardName);

        return new OpenCharExchangeWheelPacket(reel, landingIndex, outcome, rewardName);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Picks a random characteristic from the entire pool (any pathway, any seq 1-9). */
    private static ItemStack getRandomCharacteristicAnySeq(Random rand) {
        List<ItemStack> options = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : BeyonderCharacteristicItemHandler.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof BeyonderCharacteristicItem c && c.getSequence() >= 1 && c.getSequence() <= 9) {
                options.add(new ItemStack(item));
            }
        }
        if (options.isEmpty()) return ItemStack.EMPTY;
        return options.get(rand.nextInt(options.size())).copy();
    }
}

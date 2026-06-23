package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.item.custom.GarbageItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Enforces Garbage item restrictions:
 *  - Cannot be thrown/dropped (Q key or drag-outside-inventory)
 *  - Cannot be placed in external containers (chests, barrels, etc.)
 *  - CAN be freely moved within the player's own inventory
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class GarbageSlotEnforcer {

    // ── Prevent dropping ──────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity entity = event.getEntity();
        if (!(entity.getItem().getItem() instanceof GarbageItem)) return;

        event.setCanceled(true);

        // Return the item immediately and sync to client so it doesn't visually vanish
        Player player = event.getPlayer();
        if (player instanceof ServerPlayer sp) {
            player.getInventory().add(entity.getItem().copy());
            sp.inventoryMenu.broadcastChanges();
        }
    }

    // ── Container restriction (tick) ──────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        enforceContainerRestriction(player);
    }

    /**
     * If the player has an external container open and a Garbage item has been
     * placed in one of its slots, pull it back into the player's inventory.
     */
    private static void enforceContainerRestriction(Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || menu == player.inventoryMenu) return;

        Container playerInv = player.getInventory();

        for (Slot slot : menu.slots) {
            if (slot.container == playerInv) continue;

            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof GarbageItem)) continue;

            slot.set(ItemStack.EMPTY);
            player.getInventory().add(stack);
        }
    }
}


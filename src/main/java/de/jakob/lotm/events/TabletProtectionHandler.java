package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.custom.BrewingCauldronBlockEntity;
import de.jakob.lotm.gui.custom.BrewingCauldron.BrewingCauldronMenu;
import de.jakob.lotm.item.custom.BlasphemyCardItem;
import de.jakob.lotm.item.custom.BlasphemySlateHalfItem;
import de.jakob.lotm.item.custom.BlasphemySlateItem;
import de.jakob.lotm.item.custom.MysteriousTabletFragmentItem;
import de.jakob.lotm.item.custom.MysteriousTabletItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class TabletProtectionHandler {

    private static boolean isProtected(Item item) {
        return item instanceof MysteriousTabletFragmentItem
                || item instanceof MysteriousTabletItem
                || item instanceof BlasphemyCardItem
                || item instanceof BlasphemySlateHalfItem
                || item instanceof BlasphemySlateItem;
    }

    /**
     * When a player opens any container, immediately eject tablet items that are in
     * non-player-inventory slots (chests, barrels, etc.) back to the player.
     */
    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity().level().isClientSide()) return;
        Player player = event.getEntity();
        var server = player.getServer();
        if (server == null) return;

        server.execute(() -> {
            AbstractContainerMenu menu = event.getContainer();
            boolean changed = false;
            boolean isCauldron = menu instanceof BrewingCauldronMenu;
            for (Slot slot : menu.slots) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty() || !isProtected(stack.getItem())) continue;
                if (slot.container instanceof Inventory) continue; // player's own inventory — allowed
                // Blasphemy Cards are intentionally allowed in the brewing cauldron recipe slot
                if (isCauldron && stack.getItem() instanceof BlasphemyCardItem) continue;
                if (MysteriousTabletFragmentItem.isChestCopy(stack)) continue; // naturally-spawned chest copy
                ItemStack copy = stack.copy();
                slot.set(ItemStack.EMPTY);
                if (!player.addItem(copy)) {
                    player.drop(copy, false);
                }
                changed = true;
            }
            if (changed) menu.broadcastChanges();
        });
    }

    /**
     * Every tick while a player has a non-inventory container open, immediately return
     * any tablet items placed into it back to the player's inventory.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player instanceof ServerPlayer)) return;

        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || menu == player.inventoryMenu) return;

        boolean changed = false;
        boolean isCauldron = menu instanceof BrewingCauldronMenu;
        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || !isProtected(stack.getItem())) continue;
            if (slot.container instanceof Inventory) continue;
            // Blasphemy Cards are intentionally allowed in the brewing cauldron recipe slot
            if (isCauldron && stack.getItem() instanceof BlasphemyCardItem) continue;
            if (MysteriousTabletFragmentItem.isChestCopy(stack)) continue; // naturally-spawned chest copy
            ItemStack copy = stack.copy();
            slot.set(ItemStack.EMPTY);
            if (!player.addItem(copy)) {
                player.drop(copy, false);
            }
            changed = true;
        }
        if (changed) menu.broadcastChanges();
    }

    /**
     * Every 5 seconds, scan block entity containers in loaded chunks near players.
     * Only ejects to the world when no player has the container open — the per-tick
     * check above handles the player-is-watching case with instant return-to-inventory.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        ServerLevel level = (ServerLevel) event.getLevel();
        if (level.getGameTime() % 100 != 0) return;

        Set<LevelChunk> checked = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            ChunkPos center = new ChunkPos(player.blockPosition());
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(center.x + dx, center.z + dz);
                    if (chunk == null || !checked.add(chunk)) continue;
                    for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                        BlockEntity be = entry.getValue();
                        if (be instanceof Container container) {
                            ejectTabletItemsIfUnwatched(container, be, entry.getKey(), level);
                        }
                    }
                }
            }
        }
    }

    private static void ejectTabletItemsIfUnwatched(Container container, BlockEntity be, BlockPos pos, ServerLevel level) {
        // Brewing Cauldron recipe slot allows Blasphemy Cards — skip ejection entirely for it.
        if (be instanceof BrewingCauldronBlockEntity) return;
        // If any player currently has this container open, onPlayerTick handles it.
        for (ServerPlayer p : level.players()) {
            AbstractContainerMenu menu = p.containerMenu;
            if (menu == null || menu == p.inventoryMenu) continue;
            for (Slot slot : menu.slots) {
                if (slot.container == container) return;
            }
        }

        boolean changed = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || !isProtected(stack.getItem())) continue;
            if (MysteriousTabletFragmentItem.isChestCopy(stack)) continue; // naturally-spawned chest copy
            ItemStack ejected = container.removeItem(i, stack.getCount());
            if (!ejected.isEmpty()) {
                ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, ejected);
                level.addFreshEntity(drop);
                changed = true;
            }
        }
        if (changed) {
            be.setChanged();
        }
    }
}

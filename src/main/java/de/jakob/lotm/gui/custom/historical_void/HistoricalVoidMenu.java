package de.jakob.lotm.gui.custom.historical_void;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class HistoricalVoidMenu extends AbstractContainerMenu {
    public static final int ROWS = 6;
    public static final int COLS = 9;
    public static final int SLOT_COUNT = ROWS * COLS; // shared by both item & entity summon GUIs

    private final Container container;

    public HistoricalVoidMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT));
    }

    public HistoricalVoidMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.HISTORICAL_VOID_MENU.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        this.container = container;
        container.startOpen(playerInventory.player);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int index = col + row * COLS;
                this.addSlot(new DisplaySlot(container, index, 8 + col * 18, 18 + row * 18));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // shift-click disabled
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public Container getContainer() {
        return container;
    }

    public static class DisplaySlot extends Slot {
        public DisplaySlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
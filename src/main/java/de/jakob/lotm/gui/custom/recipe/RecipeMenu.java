package de.jakob.lotm.gui.custom.recipe;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeMenu extends AbstractContainerMenu {
    public static final int SUPPLEMENTARY_COUNT = 4;
    public static final int MAIN_SLOT = 4;
    public static final int SLOT_COUNT = SUPPLEMENTARY_COUNT + 1;

    public static final int[][] SLOT_POS = {
            {34, 40}, {70, 40}, {106, 40}, {142, 40},   // supplementary 1-4
            {88, 92}                                    // main
    };

    private final ItemStackHandler itemHandler;

    public RecipeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf ignored) {
        this(new ArrayList<>(List.of()), containerId, playerInventory);
    }

    public RecipeMenu(List<ItemStack> ingredients, int containerId, Inventory playerInventory) {
        super(ModMenuTypes.RECIPE_MENU.get(), containerId);
        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        };

        for (int i = 0; i < SLOT_COUNT; i++) {
            this.addSlot(new SlotItemHandler(itemHandler, i, SLOT_POS[i][0], SLOT_POS[i][1]));
        }

        if (!ingredients.isEmpty()) {
            int size = Math.min(ingredients.size(), itemHandler.getSlots());

            for (int i = 0; i < size; i++) {
                itemHandler.setStackInSlot(i, ingredients.get(i).copy());
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
package de.jakob.lotm.gui.custom.Recipe;

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
    private final ItemStackHandler itemHandler;

    // Client-side constructor
    public RecipeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf ignored) {
        this(new ArrayList<>(List.of()), containerId, playerInventory);
    }
    // Server-side constructor
    public RecipeMenu(List<ItemStack> ingredients, int containerId, Inventory playerInventory) {
        super(ModMenuTypes.RECIPE_MENU.get(), containerId);
        this.itemHandler = new ItemStackHandler(3) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        };

        this.addSlot(new SlotItemHandler(itemHandler, 0, 38, 30));
        this.addSlot(new SlotItemHandler(itemHandler, 1, 86, 30));
        this.addSlot(new SlotItemHandler(itemHandler, 2, 63, 58));

        if(!ingredients.isEmpty()) {
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
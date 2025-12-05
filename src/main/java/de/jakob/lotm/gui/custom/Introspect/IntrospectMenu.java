package de.jakob.lotm.gui.custom.Introspect;

import de.jakob.lotm.abilities.*;
import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.jakob.lotm.util.Config.items;

public class IntrospectMenu extends AbstractContainerMenu {
    private final ItemStackHandler itemHandler;

    private int sequence;
    private String pathway;
    private float digestionProgress;

    // Client-side constructor
    public IntrospectMenu(int containerId, Inventory playerInventory, FriendlyByteBuf ignored) {
        this(new ArrayList<>(List.of()), containerId, playerInventory, 9, "fool", 0.0f);
    }

    public void updateData(int sequence, String pathway, float digestionProgress) {
        this.sequence = sequence;
        this.pathway = pathway;
        this.digestionProgress = digestionProgress;
    }

    // Server-side constructor
    public IntrospectMenu(List<ItemStack> passiveAbilities, int containerId, Inventory playerInventory, int sequence, String pathway, float digestionProgress) {
        super(ModMenuTypes.INTROSPECT_MENU.get(), containerId);

        this.sequence = sequence;
        this.pathway = pathway;
        this.digestionProgress = digestionProgress;
        this.itemHandler = new ItemStackHandler(9) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false; // Prevent insertion
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        };

        for (int i = 0; i < 9; i++) {
            int x = 7 + (i * 18);
            int y = 139;
            this.addSlot(new SlotItemHandler(itemHandler, i, x, y));
        }

        if(!passiveAbilities.isEmpty()) {
            int size = Math.min(passiveAbilities.size(), itemHandler.getSlots());

            // Populate with provided items
            for (int i = 0; i < size; i++) {
                itemHandler.setStackInSlot(i, passiveAbilities.get(i).copy());
            }
        }
    }

    private long lastShiftClickTime = 0;
    private static final long SHIFT_CLICK_COOLDOWN = 200; // 200ms cooldown

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
//    public ItemStackHandler getItemHandler() {
//        return itemHandler;
//    }

    public int getSequence() {
        return sequence;
    }

    public String getPathway() {
        return pathway;
    }

    public float getDigestionProgress() {
        return digestionProgress;
    }
}
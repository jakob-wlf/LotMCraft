package de.jakob.lotm.gui.custom;

import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.ReceiveAbilityPacket;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.ToggleAbilityItem;
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

public class AbilitySelectionMenu extends AbstractContainerMenu {
    private final ItemStackHandler itemHandler;
    private final int rows;
    private final int cols = 9; // Standard chest width

    private int sequence;
    private String pathway;
    
    // Client-side constructor
    public AbilitySelectionMenu(int containerId, Inventory playerInventory, FriendlyByteBuf ignored) {
        this(containerId, playerInventory, new ArrayList<>(List.of()), 9, "fool");
    }

    public void updateData(int sequence, String pathway) {
        // You'll need to make these fields non-final and add setters
        this.sequence = sequence;
        this.pathway = pathway;
    }


    // Server-side constructor
    public AbilitySelectionMenu(int containerId, Inventory playerInventory, List<ItemStack> items, int sequence, String pathway) {
        super(ModMenuTypes.ABILITY_SELECTION_MENU.get(), containerId);
        
        // Calculate rows needed based on items
        this.rows = 1;
        this.sequence = sequence;
        this.pathway = pathway;

        // Create item handler
        this.itemHandler = new ItemStackHandler(rows * cols) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false; // Prevent insertion
            }
            
            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                Item item = itemHandler.getStackInSlot(slot).getItem();
                if(item instanceof AbilityItem || item instanceof ToggleAbilityItem)
                    PacketHandler.sendToServer(new ReceiveAbilityPacket(item));
                return ItemStack.EMPTY;
            }
        };

        int size = Math.min(items.size(), itemHandler.getSlots());
        int difference = cols - size;
        int startIndex = (int) (difference / 2f);

        // Populate with provided items
        for (int i = startIndex; i < Math.min(9, size + startIndex); i++) {
            itemHandler.setStackInSlot(i, items.get(i - startIndex).copy());
        }
        
        for (int col = 0; col < cols; col++) {
            this.addSlot(new SlotItemHandler(itemHandler, col,
                8 + col * 18, 18));
        }


        // Add player inventory slots
        int playerInvY = 50;
        
        // Player inventory (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    8 + col * 18, playerInvY + row * 18));
            }
        }
        
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                8 + col * 18, playerInvY + 58));
        }
    }
    
    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // Disable shift-clicking
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    public int getRows() {
        return rows;
    }
    
    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public int getSequence() {
        return sequence;
    }

    public String getPathway() {
        return pathway;
    }
}
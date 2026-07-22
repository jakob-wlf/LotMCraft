package de.jakob.lotm.gui.custom.Introspect;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IntrospectMenu extends AbstractContainerMenu {
    private final ItemStackHandler itemHandler;

    private int sequence;
    private String pathway;
    private float digestionProgress;
    private float sanity;
    private float corruption;
    private boolean sefirotOwner;

    // Client-side constructor
    public IntrospectMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(new ArrayList<>(List.of()), containerId, playerInventory, buf.readInt(), buf.readUtf(), 0.0f, 1.0f, 0.0f, buf.readBoolean());
    }

    public void updateData(int sequence, String pathway, float digestionProgress, float sanity, float corruption) {
        this.sequence = sequence;
        this.pathway = pathway;
        this.digestionProgress = digestionProgress;
        this.sanity = sanity;
        this.corruption = corruption;
    }

    // Server-side constructor
    public IntrospectMenu(List<ItemStack> passiveAbilities, int containerId, Inventory playerInventory, int sequence, String pathway, float digestionProgress, float sanity, float corruption, boolean sefirotOwner) {
        super(ModMenuTypes.INTROSPECT_MENU.get(), containerId);

        this.sequence = sequence;
        this.pathway = pathway;
        this.digestionProgress = digestionProgress;
        this.sanity = sanity;
        this.corruption = corruption;
        this.sefirotOwner = sefirotOwner;
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

        boolean showKillCount = pathway.equals("red_priest") && sequence <= 3;
        int slotY = showKillCount ? 188 : 178;
        for (int i = 0; i < 9; i++) {
            int x = 7 + (i * 18);
            this.addSlot(new SlotItemHandler(itemHandler, i, x, slotY));
        }

        if(!passiveAbilities.isEmpty()) {
            int size = Math.min(passiveAbilities.size(), itemHandler.getSlots());

            // Populate with provided items
            for (int i = 0; i < size; i++) {
                itemHandler.setStackInSlot(i, passiveAbilities.get(i).copy());
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

    public int getSequence() {
        return sequence;
    }

    public String getPathway() {
        return pathway;
    }

    public float getDigestionProgress() {
        return digestionProgress;
    }

    public float getSanity() {
        return sanity;
    }

    public float getCorruption() {
        return corruption;
    }

    public boolean isSefirotOwner() {
        return sefirotOwner;
    }
}
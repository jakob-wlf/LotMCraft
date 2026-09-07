package de.jakob.lotm.gui.custom.introspect;

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
    private int sequence;
    private String pathway;
    private float digestionProgress;
    private float sanity;

    // Client-side constructor
    public IntrospectMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readInt(), buf.readUtf(), 0.0f, 1.0f);
    }

    public void updateData(int sequence, String pathway, float digestionProgress, float sanity) {
        this.sequence = sequence;
        this.pathway = pathway;
        this.digestionProgress = digestionProgress;
        this.sanity = sanity;
    }

    // Server-side constructor
    public IntrospectMenu(int containerId, Inventory playerInventory, int sequence, String pathway, float digestionProgress, float sanity) {
        super(ModMenuTypes.INTROSPECT_MENU.get(), containerId);

        this.sequence = sequence;
        this.pathway = pathway;
        this.digestionProgress = digestionProgress;
        this.sanity = sanity;
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
}
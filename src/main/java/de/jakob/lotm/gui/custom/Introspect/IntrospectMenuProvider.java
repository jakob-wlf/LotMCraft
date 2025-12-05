package de.jakob.lotm.gui.custom.Introspect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntrospectMenuProvider implements MenuProvider {
    private final int sequence;
    private final String pathway;
    private final float digestionProgress;
    private final List<ItemStack> passiveAbilities;


    public IntrospectMenuProvider(List<ItemStack> passiveAbilities, int sequence, String pathway, float digestionProgress) {
        this.sequence = sequence;
        this.pathway = pathway;
        this.digestionProgress = digestionProgress;
        this.passiveAbilities = passiveAbilities;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IntrospectMenu(passiveAbilities, containerId, playerInventory, sequence, pathway, digestionProgress);
    }
}
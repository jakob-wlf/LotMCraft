package de.jakob.lotm.gui.custom.cohort;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CohortMenuProvider implements MenuProvider {

    private final List<String[]> members;
    public CohortMenuProvider(List<String[]> members) {
        this.members = members;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("lotm.cohort_menu").append(" ("+this.members.size() + " members)").withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CohortMenu(members, containerId, playerInventory);
    }
}
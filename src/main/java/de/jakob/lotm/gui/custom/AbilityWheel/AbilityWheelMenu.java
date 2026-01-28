package de.jakob.lotm.gui.custom.AbilityWheel;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class AbilityWheelMenu extends AbstractContainerMenu {

    public AbilityWheelMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.ABILITY_WHEEL_MENU.get(), containerId);

        System.out.println("created instance");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
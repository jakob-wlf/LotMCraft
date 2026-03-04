package de.jakob.lotm.gui.custom.CopiedAbilityWheel;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CopiedAbilityWheelMenu extends AbstractContainerMenu {

    public CopiedAbilityWheelMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.COPIED_ABILITY_WHEEL_MENU.get(), containerId);
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

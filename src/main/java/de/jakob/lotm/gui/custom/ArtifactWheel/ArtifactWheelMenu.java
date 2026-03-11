package de.jakob.lotm.gui.custom.ArtifactWheel;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ArtifactWheelMenu extends AbstractContainerMenu {

    private final List<String> abilities;
    private int selectedAbilityIndex = 0;

    public ArtifactWheelMenu(int containerId, Inventory playerInventory, List<String> abilities) {
        super(ModMenuTypes.ARTIFACT_WHEEL_MENU.get(), containerId);
        this.abilities = abilities;
    }

    public List<String> getAbilities() {
        return abilities;
    }

    public int getSelectedAbilityIndex() {
        return selectedAbilityIndex;
    }

    public void setSelectedAbilityIndex(int index) {
        this.selectedAbilityIndex = index;
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
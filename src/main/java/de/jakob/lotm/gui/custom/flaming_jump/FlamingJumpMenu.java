package de.jakob.lotm.gui.custom.flaming_jump;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FlamingJumpMenu extends AbstractContainerMenu {

    private final List<BlockPos> fireLocations;

    public FlamingJumpMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readFireLocations(buf));
    }

    private static List<BlockPos> readFireLocations(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<BlockPos> locations = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            locations.add(buf.readBlockPos());
        }
        return locations;
    }

    public FlamingJumpMenu(int containerId, Inventory playerInventory, List<BlockPos> fireLocations) {
        super(ModMenuTypes.FLAMING_JUMP_MENU.get(), containerId);
        this.fireLocations = fireLocations;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public List<BlockPos> getFireLocations() {
        return fireLocations;
    }
}

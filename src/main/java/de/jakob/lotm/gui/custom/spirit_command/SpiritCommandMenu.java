package de.jakob.lotm.gui.custom.spirit_command;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpiritCommandMenu extends AbstractContainerMenu {
    private final List<String> powers;

    public SpiritCommandMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, read(buf));
    }

    public SpiritCommandMenu(int containerId, Inventory inventory, List<String> powers) {
        super(ModMenuTypes.SPIRIT_COMMAND_MENU.get(), containerId);
        this.powers = powers;
    }

    private static List<String> read(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> powers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) powers.add(buf.readUtf());
        return powers;
    }

    public List<String> powers() {
        return powers;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}

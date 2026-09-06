package de.jakob.lotm.gui.custom.flaming_jump;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlamingJumpMenuProvider implements MenuProvider {
    private final List<BlockPos> fireLocations;

    public FlamingJumpMenuProvider(List<BlockPos> fireLocations) {
        this.fireLocations = fireLocations;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("gui.lotm.flaming_jump.title");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new FlamingJumpMenu(i, inventory, fireLocations);
    }
}

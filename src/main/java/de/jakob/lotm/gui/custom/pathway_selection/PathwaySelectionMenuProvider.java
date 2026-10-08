package de.jakob.lotm.gui.custom.pathway_selection;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PathwaySelectionMenuProvider implements MenuProvider {
    private final List<String> offered;

    public PathwaySelectionMenuProvider(List<String> offered) {
        this.offered = List.copyOf(offered);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Choose Your Path");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new PathwaySelectionMenu(containerId, inventory, offered);
    }

    public void writeExtraData(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(offered, (b, id) -> b.writeUtf(id, PathwaySelectionMenu.MAX_ID_LENGTH));
    }
}
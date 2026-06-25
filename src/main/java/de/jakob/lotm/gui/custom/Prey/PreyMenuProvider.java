package de.jakob.lotm.gui.custom.Prey;

import de.jakob.lotm.util.playerMap.HonorificName;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class PreyMenuProvider implements MenuProvider {

    private final Map<UUID, HonorificName> honorificNames;

    public PreyMenuProvider(Map<UUID, HonorificName> honorificNames) {
        this.honorificNames = honorificNames;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("lotmcraft.gui.prey.title");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PreyMenu(id, inventory, honorificNames);
    }
}

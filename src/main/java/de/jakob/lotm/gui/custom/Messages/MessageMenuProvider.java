package de.jakob.lotm.gui.custom.Messages;

import de.jakob.lotm.util.beyonderMap.StoredData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class MessageMenuProvider implements MenuProvider {
    private StoredData data;

    public MessageMenuProvider(StoredData data){
        this.data = data;
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new MessagesMenu(i, inventory, data);
    }
}

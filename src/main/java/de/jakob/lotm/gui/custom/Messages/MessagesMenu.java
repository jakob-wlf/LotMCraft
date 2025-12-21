package de.jakob.lotm.gui.custom.Messages;

import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.util.beyonderMap.HonorificName;
import de.jakob.lotm.util.beyonderMap.MessageType;
import de.jakob.lotm.util.beyonderMap.StoredData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;

public class MessagesMenu extends AbstractContainerMenu {
    private LinkedList<HonorificName> honorificNames;
    private HonorificName name;
    private LinkedList<MessageType> messages;

    //Client-side
    public MessagesMenu(int containerId, Inventory inv, FriendlyByteBuf ignored) {
        this(containerId, inv,
                new LinkedList<>(),
                new HonorificName("DEFAULT", "DEFAULT", "DEFAULT", "DEFAULT"),
                new LinkedList<>());
    }

    public MessagesMenu(int containerId, Inventory inv, StoredData data){
        this(containerId, inv, new LinkedList<>(), data.honorificName(), data.msgs());
    }

    //Server-side
    public MessagesMenu(int containerId, Inventory inv, LinkedList<HonorificName> list, HonorificName name, LinkedList<MessageType> msgs){
        super(ModMenuTypes.MESSAGES_MENU.get(), containerId);

        honorificNames = list;
        this.name = name;
        messages = msgs;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }


}
package de.jakob.lotm.gui.custom.Messages;

import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.util.beyonderMap.HonorificName;
import de.jakob.lotm.util.beyonderMap.MessageType;
import de.jakob.lotm.util.beyonderMap.StoredData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class MessagesMenu extends AbstractContainerMenu {
    private LinkedList<HonorificName> knownNames;
    private HonorificName name;
    private LinkedList<MessageType> messages;

    private int selectedMessageIndex = -1;

    // CLIENT constructor
    public MessagesMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(
                id,
                inv,
                buf.readCollection(s -> new LinkedList<>(), HonorificName::fromNetwork),
                HonorificName.fromNetwork(buf),
                buf.readCollection(s -> new LinkedList<>(), MessageType::fromNetwork)
        );
    }

    // SERVER constructor
    public MessagesMenu(int id, Inventory inv, StoredData data) {
        this(
                id,
                inv,
                data.knownNames(),
                data.honorificName(),
                data.msgs()
        );
    }

    private MessagesMenu(
            int id,
            Inventory inv,
            LinkedList<HonorificName> knownNames,
            HonorificName name,
            LinkedList<MessageType> messages
    ) {
        super(ModMenuTypes.MESSAGES_MENU.get(), id);
        this.knownNames = knownNames;
        this.messages = messages;
        this.name = name;
    }

    public List<MessageType> getMessages() {
        return messages;
    }

    public int getSelectedMessageIndex() {
        return selectedMessageIndex;
    }

    public void selectMessage(int index) {
        this.selectedMessageIndex = index;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

}

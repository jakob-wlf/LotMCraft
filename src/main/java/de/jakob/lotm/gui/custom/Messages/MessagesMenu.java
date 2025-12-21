package de.jakob.lotm.gui.custom.Messages;

import de.jakob.lotm.util.beyonderMap.HonorificName;
import de.jakob.lotm.util.beyonderMap.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import java.util.LinkedList;

public class MessagesMenu {
    private LinkedList<HonorificName> honorificNames;
    private HonorificName name;
    private LinkedList<MessageType> messages;

    //Client-side
    public MessagesMenu(LinkedList<HonorificName> list, HonorificName name, LinkedList<MessageType> msgs, FriendlyByteBuf ignored) {
        this(list, name, msgs);
    }

    //Server-side
    public MessagesMenu(LinkedList<HonorificName> list, HonorificName name, LinkedList<MessageType> msgs){
        honorificNames = list;
        this.name = name;
        messages = msgs;


    }
}
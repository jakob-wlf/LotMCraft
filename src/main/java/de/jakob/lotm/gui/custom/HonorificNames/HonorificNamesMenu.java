package de.jakob.lotm.gui.custom.HonorificNames;

import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.util.playerMap.HonorificName;
import de.jakob.lotm.util.playerMap.PendingPrayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;

public class HonorificNamesMenu extends AbstractContainerMenu {

    private final HonorificName ownName;
    private final String pathway;
    private final int sequence;
    private final LinkedList<PendingPrayer> pendingPrayers;
    private final boolean sefirotOwner;

    // CLIENT constructor (reads from network buffer)
    public HonorificNamesMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(
                id, inv,
                HonorificName.fromNetwork(buf),
                buf.readUtf(64),
                buf.readInt(),
                buf.readCollection(s -> new LinkedList<>(), PendingPrayer::fromNetwork),
                buf.readBoolean()
        );
    }

    // SERVER constructor
    public HonorificNamesMenu(int id, Inventory inv, HonorificName ownName, String pathway, int sequence,
                              LinkedList<PendingPrayer> pendingPrayers, boolean sefirotOwner) {
        super(ModMenuTypes.HONORIFIC_NAMES_MENU.get(), id);
        this.ownName = ownName;
        this.pathway = pathway;
        this.sequence = sequence;
        this.pendingPrayers = pendingPrayers;
        this.sefirotOwner = sefirotOwner;
    }

    public HonorificName getOwnName() {
        return ownName;
    }

    public String getPathway() {
        return pathway;
    }

    public int getSequence() {
        return sequence;
    }

    public LinkedList<PendingPrayer> getPendingPrayers() {
        return pendingPrayers;
    }

    public boolean isSefirotOwner() {
        return sefirotOwner;
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

package de.jakob.lotm.gui.custom.HonorificNames;

import de.jakob.lotm.util.playerMap.HonorificName;
import de.jakob.lotm.util.playerMap.PendingPrayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class HonorificNamesMenuProvider implements MenuProvider {

    private final HonorificName ownName;
    private final String pathway;
    private final int sequence;
    private final LinkedList<PendingPrayer> pendingPrayers;
    private final boolean sefirotOwner;

    public HonorificNamesMenuProvider(HonorificName ownName, String pathway, int sequence,
                                      LinkedList<PendingPrayer> pendingPrayers, boolean sefirotOwner) {
        this.ownName = ownName;
        this.pathway = pathway;
        this.sequence = sequence;
        this.pendingPrayers = pendingPrayers;
        this.sefirotOwner = sefirotOwner;
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new HonorificNamesMenu(id, inventory, ownName, pathway, sequence, pendingPrayers, sefirotOwner);
    }
}

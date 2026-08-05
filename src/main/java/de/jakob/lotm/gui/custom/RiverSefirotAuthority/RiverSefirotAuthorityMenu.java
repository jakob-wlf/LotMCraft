package de.jakob.lotm.gui.custom.RiverSefirotAuthority;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu for the River of Eternal Darkness sefirot authority ability screen.
 * Carries the same data as SefirotAuthorityMenu but uses its own menu type so
 * it can be bound to the river-themed screen.
 */
public class RiverSefirotAuthorityMenu extends AbstractContainerMenu {

    private final List<String> availableIds;
    private final List<String> unlockedIds;
    private final List<String> neighborPaths;
    private final int imprintPercent;

    /** Client-side constructor — reads data written by the server. */
    public RiverSefirotAuthorityMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory,
                buf.readCollection(ArrayList::new, b -> b.readUtf()),
                buf.readCollection(ArrayList::new, b -> b.readUtf()),
                buf.readCollection(ArrayList::new, b -> b.readUtf()),
                buf.readInt());
    }

    /** Server-side constructor without imprint. */
    public RiverSefirotAuthorityMenu(int containerId, Inventory playerInventory,
                                     List<String> availableIds, List<String> unlockedIds,
                                     List<String> neighborPaths) {
        this(containerId, playerInventory, availableIds, unlockedIds, neighborPaths, 0);
    }

    /** Full server-side constructor. */
    public RiverSefirotAuthorityMenu(int containerId, Inventory playerInventory,
                                     List<String> availableIds, List<String> unlockedIds,
                                     List<String> neighborPaths, int imprintPercent) {
        super(ModMenuTypes.RIVER_SEFIROT_AUTHORITY_MENU.get(), containerId);
        this.availableIds   = availableIds;
        this.unlockedIds    = unlockedIds;
        this.neighborPaths  = neighborPaths;
        this.imprintPercent = imprintPercent;
    }

    public List<String> getAvailableIds()  { return availableIds;  }
    public List<String> getUnlockedIds()   { return unlockedIds;   }
    public List<String> getNeighborPaths() { return neighborPaths; }
    public int          getImprintPercent(){ return imprintPercent; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}

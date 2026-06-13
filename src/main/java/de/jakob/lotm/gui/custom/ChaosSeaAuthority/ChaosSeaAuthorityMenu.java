package de.jakob.lotm.gui.custom.ChaosSeaAuthority;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu for the Chaos Sea sefirot authority ability screen.
 * Carries the same data as the other authority menus but uses its own
 * menu type so it can be bound to the chaos-sea-themed screen.
 */
public class ChaosSeaAuthorityMenu extends AbstractContainerMenu {

    private final List<String> availableIds;
    private final List<String> unlockedIds;
    private final List<String> neighborPaths;

    /** Client-side constructor — reads data written by the server. */
    public ChaosSeaAuthorityMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory,
                buf.readCollection(ArrayList::new, b -> b.readUtf()),
                buf.readCollection(ArrayList::new, b -> b.readUtf()),
                buf.readCollection(ArrayList::new, b -> b.readUtf()));
    }

    /** Server-side constructor. */
    public ChaosSeaAuthorityMenu(int containerId, Inventory playerInventory,
                                  List<String> availableIds, List<String> unlockedIds,
                                  List<String> neighborPaths) {
        super(ModMenuTypes.CHAOS_SEA_AUTHORITY_MENU.get(), containerId);
        this.availableIds  = availableIds;
        this.unlockedIds   = unlockedIds;
        this.neighborPaths = neighborPaths;
    }

    public List<String> getAvailableIds()  { return availableIds;  }
    public List<String> getUnlockedIds()   { return unlockedIds;   }
    public List<String> getNeighborPaths() { return neighborPaths; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}

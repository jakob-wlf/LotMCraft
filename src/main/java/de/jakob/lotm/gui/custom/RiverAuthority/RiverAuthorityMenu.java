package de.jakob.lotm.gui.custom.RiverAuthority;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RiverAuthorityMenu extends AbstractContainerMenu {

    /** Ordered list of imprint entries for display (sorted by tier desc, then UUID). */
    private final List<ImprintEntry> entries;

    /** Represents one imprint entry in the GUI. */
    public record ImprintEntry(UUID uuid, String name, String pathway, int sequence, int imprintTier) {}

    /** Client-side constructor — reads serialized entries from buffer. */
    public RiverAuthorityMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readEntries(buf));
    }

    /** Server-side constructor. */
    public RiverAuthorityMenu(int containerId, Inventory playerInventory, List<ImprintEntry> entries) {
        super(ModMenuTypes.RIVER_AUTHORITY_MENU.get(), containerId);
        this.entries = entries;
    }

    public List<ImprintEntry> getEntries() {
        return entries;
    }

    private static List<ImprintEntry> readEntries(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<ImprintEntry> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            UUID uuid = UUID.fromString(buf.readUtf(64));
            String name = buf.readUtf(64);
            String pathway = buf.readUtf(64);
            int sequence = buf.readVarInt();
            int tier = buf.readVarInt();
            list.add(new ImprintEntry(uuid, name, pathway, sequence, tier));
        }
        return list;
    }

    public static void writeEntries(RegistryFriendlyByteBuf buf, List<ImprintEntry> entries) {
        buf.writeVarInt(entries.size());
        for (ImprintEntry e : entries) {
            buf.writeUtf(e.uuid().toString(), 64);
            buf.writeUtf(e.name(), 64);
            buf.writeUtf(e.pathway(), 64);
            buf.writeVarInt(e.sequence());
            buf.writeVarInt(e.imprintTier());
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}

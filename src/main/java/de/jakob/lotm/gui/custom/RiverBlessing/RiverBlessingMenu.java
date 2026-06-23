package de.jakob.lotm.gui.custom.RiverBlessing;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Menu for the River of Eternal Darkness Blessings GUI.
 *
 * Carries the list of players who have prayed to the River owner (potential
 * blessing candidates) and the UUIDs of those currently blessed.
 */
public class RiverBlessingMenu extends AbstractContainerMenu {

    private final List<BlessingEntry> prayers;
    private final List<UUID> blessed;
    private final int maxBlessings;

    /** One row in the prayer list panel. */
    public record BlessingEntry(UUID uuid, String name, String pathway, int sequence) {}

    // ── Client-side constructor ───────────────────────────────────────────────

    public RiverBlessingMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readEntries(buf), readUUIDs(buf), buf.readVarInt());
    }

    // ── Server-side constructor ───────────────────────────────────────────────

    public RiverBlessingMenu(int containerId, Inventory playerInventory,
                             List<BlessingEntry> prayers, List<UUID> blessed, int maxBlessings) {
        super(ModMenuTypes.RIVER_BLESSING_MENU.get(), containerId);
        this.prayers      = prayers;
        this.blessed      = blessed;
        this.maxBlessings = maxBlessings;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public List<BlessingEntry> getPrayers()   { return prayers;      }
    public List<UUID>          getBlessed()   { return blessed;      }
    public int                 getMaxBlessings() { return maxBlessings; }

    // ── Serialisation ─────────────────────────────────────────────────────────

    private static List<BlessingEntry> readEntries(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<BlessingEntry> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            UUID   uuid = UUID.fromString(buf.readUtf(64));
            String name = buf.readUtf(64);
            String path = buf.readUtf(64);
            int    seq  = buf.readVarInt();
            list.add(new BlessingEntry(uuid, name, path, seq));
        }
        return list;
    }

    private static List<UUID> readUUIDs(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<UUID> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) list.add(UUID.fromString(buf.readUtf(64)));
        return list;
    }

    public static void writeToBuffer(RegistryFriendlyByteBuf buf,
                                     List<BlessingEntry> prayers,
                                     List<UUID> blessed,
                                     int maxBlessings) {
        buf.writeVarInt(prayers.size());
        for (BlessingEntry e : prayers) {
            buf.writeUtf(e.uuid().toString(), 64);
            buf.writeUtf(e.name(), 64);
            buf.writeUtf(e.pathway(), 64);
            buf.writeVarInt(e.sequence());
        }
        buf.writeVarInt(blessed.size());
        for (UUID u : blessed) buf.writeUtf(u.toString(), 64);
        buf.writeVarInt(maxBlessings);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return true; }
}

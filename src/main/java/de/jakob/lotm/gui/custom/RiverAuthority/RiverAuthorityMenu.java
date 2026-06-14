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
    private final boolean globalLeakageOff;

    /** Represents one imprint entry in the GUI. */
    public record ImprintEntry(UUID uuid, String name, String pathway, int sequence, int imprintTier, boolean online, List<String> sealedAbilityIds, boolean leakageExempt) {}

    /** Client-side constructor — reads serialized entries from buffer. */
    public RiverAuthorityMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readEntries(buf), buf.readBoolean());
    }

    /** Server-side constructor. */
    public RiverAuthorityMenu(int containerId, Inventory playerInventory, List<ImprintEntry> entries, boolean globalLeakageOff) {
        super(ModMenuTypes.RIVER_AUTHORITY_MENU.get(), containerId);
        this.entries = entries;
        this.globalLeakageOff = globalLeakageOff;
    }

    public List<ImprintEntry> getEntries() {
        return entries;
    }

    public boolean isGlobalLeakageOff() {
        return globalLeakageOff;
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
            boolean online = buf.readBoolean();
            int sealCount = buf.readVarInt();
            List<String> sealed = new ArrayList<>(sealCount);
            for (int j = 0; j < sealCount; j++) sealed.add(buf.readUtf(64));
            boolean leakageExempt = buf.readBoolean();
            list.add(new ImprintEntry(uuid, name, pathway, sequence, tier, online, sealed, leakageExempt));
        }
        return list;
    }

    public static void writeBuf(RegistryFriendlyByteBuf buf, List<ImprintEntry> entries, boolean globalLeakageOff) {
        writeEntries(buf, entries);
        buf.writeBoolean(globalLeakageOff);
    }

    public static void writeEntries(RegistryFriendlyByteBuf buf, List<ImprintEntry> entries) {
        buf.writeVarInt(entries.size());
        for (ImprintEntry e : entries) {
            buf.writeUtf(e.uuid().toString(), 64);
            buf.writeUtf(e.name(), 64);
            buf.writeUtf(e.pathway(), 64);
            buf.writeVarInt(e.sequence());
            buf.writeVarInt(e.imprintTier());
            buf.writeBoolean(e.online());
            buf.writeVarInt(e.sealedAbilityIds().size());
            for (String id : e.sealedAbilityIds()) buf.writeUtf(id, 64);
            buf.writeBoolean(e.leakageExempt());
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

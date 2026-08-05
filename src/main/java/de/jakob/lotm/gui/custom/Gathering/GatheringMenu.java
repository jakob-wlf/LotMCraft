package de.jakob.lotm.gui.custom.Gathering;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GatheringMenu extends AbstractContainerMenu {

    private final List<GatheringEntry> prayers;
    private final List<UUID> members;
    private final boolean gatheringActive;
    private final String sefirahType;

    /** One row in the prayer / member list. */
    public record GatheringEntry(UUID uuid, String name, String pathway, int sequence) {}

    /** Client-side constructor. */
    public GatheringMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readUtf(64), readEntries(buf), readUUIDs(buf), buf.readBoolean());
    }

    /** Server-side constructor. */
    public GatheringMenu(int containerId, Inventory playerInventory,
                         String sefirahType, List<GatheringEntry> prayers, List<UUID> members, boolean gatheringActive) {
        super(ModMenuTypes.GATHERING_MENU.get(), containerId);
        this.sefirahType = sefirahType;
        this.prayers = prayers;
        this.members = members;
        this.gatheringActive = gatheringActive;
    }

    public List<GatheringEntry> getPrayers()   { return prayers; }
    public List<UUID>           getMembers()   { return members; }
    public boolean              isActive()     { return gatheringActive; }
    public String               getSefirahType() { return sefirahType; }

    private static List<GatheringEntry> readEntries(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<GatheringEntry> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            UUID uuid     = UUID.fromString(buf.readUtf(64));
            String name   = buf.readUtf(64);
            String path   = buf.readUtf(64);
            int seq       = buf.readVarInt();
            list.add(new GatheringEntry(uuid, name, path, seq));
        }
        return list;
    }

    private static List<UUID> readUUIDs(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<UUID> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) list.add(UUID.fromString(buf.readUtf(64)));
        return list;
    }

    public static void writeToBuffer(RegistryFriendlyByteBuf buf, String sefirahType,
                                     List<GatheringEntry> prayers, List<UUID> members, boolean active) {
        buf.writeUtf(sefirahType, 64);
        buf.writeVarInt(prayers.size());
        for (GatheringEntry e : prayers) {
            buf.writeUtf(e.uuid().toString(), 64);
            buf.writeUtf(e.name(), 64);
            buf.writeUtf(e.pathway(), 64);
            buf.writeVarInt(e.sequence());
        }
        buf.writeVarInt(members.size());
        for (UUID m : members) buf.writeUtf(m.toString(), 64);
        buf.writeBoolean(active);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return true; }
}

package de.jakob.lotm.gui.custom.Prey;

import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.util.playerMap.HonorificName;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PreyMenu extends AbstractContainerMenu {

    private final Map<UUID, HonorificName> honorificNames;

    // CLIENT constructor
    public PreyMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, readHonorificNames(buf));
    }

    // SERVER constructor
    public PreyMenu(int id, Inventory inv, Map<UUID, HonorificName> honorificNames) {
        super(ModMenuTypes.PREY_MENU.get(), id);
        this.honorificNames = honorificNames;
    }

    private static Map<UUID, HonorificName> readHonorificNames(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<UUID, HonorificName> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buf.readUUID(), HonorificName.fromNetwork(buf));
        }
        return map;
    }

    public static void writeHonorificNames(FriendlyByteBuf buf, Map<UUID, HonorificName> honorificNames) {
        buf.writeVarInt(honorificNames.size());
        for (Map.Entry<UUID, HonorificName> entry : honorificNames.entrySet()) {
            buf.writeUUID(entry.getKey());
            entry.getValue().toNetwork(buf);
        }
    }

    public Map<UUID, HonorificName> getHonorificNames() {
        return honorificNames;
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

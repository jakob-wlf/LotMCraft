package de.jakob.lotm.gui.custom.malice_seed;

import de.jakob.lotm.beyonders.abilities.abyss.MaliceSeedAbility.SeedInfo;
import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MaliceSeedMenu extends AbstractContainerMenu {
    private List<SeedInfo> seeds;
    public MaliceSeedMenu(int containerId, Inventory ignoredPlayerInventory, List<SeedInfo> seeds) {
        super(ModMenuTypes.MALICE_SEED_MENU.get(), containerId);
        this.seeds = new ArrayList<>(seeds);
    }
    public List<SeedInfo> getSeeds() {return seeds;}
    public static void writeSeeds(RegistryFriendlyByteBuf buf, List<SeedInfo> seeds) {
        buf.writeVarInt(seeds.size());
        for (SeedInfo seed : seeds) {
            buf.writeUUID(seed.hostUUID());
            buf.writeUtf(seed.hostName());
            buf.writeVarInt(seed.phase());
            buf.writeVarInt(seed.progressPercent());
            buf.writeBoolean(seed.isPlayer());
        }
    }
    public static List<SeedInfo> readSeeds(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<SeedInfo> seeds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            UUID hostUUID = buf.readUUID();
            String hostName = buf.readUtf();
            int phase = buf.readVarInt();
            int progressPercent = buf.readVarInt();
            boolean isPlayer = buf.readBoolean();
            seeds.add(new SeedInfo(hostUUID, hostName, phase, progressPercent, "", isPlayer));
        }
        return seeds;
    }
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {return ItemStack.EMPTY;}
    @Override
    public boolean stillValid(@NotNull Player player) {return true;}
    public void applySync(List<SeedInfo> newSeeds) {this.seeds = new ArrayList<>(newSeeds);}
}
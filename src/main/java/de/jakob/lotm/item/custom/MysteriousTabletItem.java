package de.jakob.lotm.item.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MysteriousTabletData;
import de.jakob.lotm.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class MysteriousTabletItem extends Item {

    public static final String KEY_TABLET_ID = "TabletId";

    public MysteriousTabletItem(Properties properties) {
        super(properties);
    }

    public static UUID getTabletId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(KEY_TABLET_ID) ? tag.getUUID(KEY_TABLET_ID) : null;
    }

    public static void setTabletId(ItemStack stack, UUID id) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(KEY_TABLET_ID, id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public boolean syncTablet(ServerLevel level, ItemStack stack) {
        MysteriousTabletData data = MysteriousTabletData.get(level.getServer());
        if (data.isLockedByCastle()) {
            return false;
        }

        UUID currentId = getTabletId(stack);
        UUID recordedId = data.getTabletId();
        long tick = level.getGameTime();

        if (recordedId == null) {
            if (!data.canSpawnTablet()) {
                return false;
            }
            if (currentId == null) {
                currentId = UUID.randomUUID();
                setTabletId(stack, currentId);
            }
            data.markTabletExists(currentId, tick);
            return true;
        }

        if (currentId == null) {
            return false;
        }

        if (recordedId.equals(currentId)) {
            data.markTabletSeen(currentId, tick);
            return true;
        }

        // UUID mismatch: a different tablet is in inventory while one is already recorded.
        // Before rejecting it, verify the recorded tablet actually exists in any online
        // player's inventory (same logic as /fragment check). If it's gone, clear the stale
        // record and accept the new tablet immediately.
        if (!isTabletPresent(level.getServer(), recordedId)) {
            data.clearTablet();
            data.markTabletExists(currentId, tick);
            return true;
        }

        return false;
    }

    /**
     * Returns true if a tablet exists in any online or offline player's inventory.
     * For online players the UUID is matched exactly. For offline players any tablet
     * item counts, since only one can exist at a time.
     */
    public static boolean isTabletPresent(MinecraftServer server, UUID tabletId) {
        if (tabletId == null) return false;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (!s.isEmpty() && s.getItem() instanceof MysteriousTabletItem
                        && tabletId.equals(getTabletId(s))) {
                    return true;
                }
            }
        }
        return isItemInAnyOfflineInventory(server, LOTMCraft.MOD_ID + ":mysterious_tablet");
    }

    /**
     * Returns true if a fragment of the given type exists in any online or offline player's inventory.
     * For online players the UUID is matched exactly. For offline players any item of that fragment
     * type counts, since only one can exist at a time.
     */
    public static boolean isFragmentPresent(MinecraftServer server,
                                            MysteriousTabletData.FragmentType type, UUID fragId) {
        if (fragId == null) return false;

        // Check online player inventories by UUID (chest copies are never in player inventories
        // with a UUID, so no ChestCopy filtering is needed here).
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (!s.isEmpty() && s.getItem() instanceof MysteriousTabletFragmentItem f
                        && f.getFragmentType() == type
                        && fragId.equals(MysteriousTabletFragmentItem.getFragmentId(s))) {
                    return true;
                }
            }
        }

        // Check offline player .dat files by item ID.
        String itemId = LOTMCraft.MOD_ID + ":" + type.getId() + "_fragment_of_a_mysterious_tablet";
        return isItemInAnyOfflineInventory(server, itemId);
    }

    /**
     * Scans all offline player .dat files and returns true if any inventory contains an item
     * with the given registry ID. Online players are skipped (they should already be checked
     * in-memory). This mirrors the logic used by /fragment check.
     */
    private static boolean isItemInAnyOfflineInventory(MinecraftServer server, String itemId) {
        Path dir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".dat"))
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        try {
                            UUID uuid = UUID.fromString(name.substring(0, name.length() - 4));
                            return server.getPlayerList().getPlayer(uuid) == null; // offline only
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    })
                    .anyMatch(p -> {
                        try {
                            CompoundTag root = NbtIo.readCompressed(p, NbtAccounter.unlimitedHeap());
                            ListTag inv = root.getList("Inventory", Tag.TAG_COMPOUND);
                            for (int i = 0; i < inv.size(); i++) {
                                if (itemId.equals(inv.getCompound(i).getString("id"))) return true;
                            }
                        } catch (IOException ignored) {}
                        return false;
                    });
        } catch (IOException e) {
            return false;
        }
    }
}

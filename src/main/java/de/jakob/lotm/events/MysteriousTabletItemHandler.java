package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MysteriousTabletData;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.item.custom.MysteriousTabletFragmentItem;
import de.jakob.lotm.item.custom.MysteriousTabletItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MysteriousTabletItemHandler {

    private static final int PLAYER_CHECK_INTERVAL = 20;
    private static final int CLEANUP_INTERVAL = 100;
    private static final long UNSEEN_TICKS = 200L;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();

        if (entity instanceof ItemEntity itemEntity) {
            if (itemEntity.level().isClientSide) {
                return;
            }
            if (!(itemEntity.level() instanceof ServerLevel level)) {
                return;
            }
            if (!syncStack(level, itemEntity.getItem())) {
                itemEntity.discard();
            }
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }
        if (player.tickCount % PLAYER_CHECK_INTERVAL != 0) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!syncStack(level, stack)) {
                player.getInventory().removeItem(i, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long time = overworld.getGameTime();

        if (time % PLAYER_CHECK_INTERVAL == 0) {
            // Fast-path: clear records whose items are no longer present in any online
            // player's inventory. This mirrors the /fragment check scan and prevents the
            // 4-10 second delay that the time-based cleanup alone would cause.
            MysteriousTabletData data = MysteriousTabletData.get(event.getServer());
            if (!data.isLockedByCastle()) {
                if (data.tabletExists()
                        && time - data.getTabletLastSeen() > PLAYER_CHECK_INTERVAL
                        && !MysteriousTabletItem.isTabletPresent(event.getServer(), data.getTabletId())) {
                    data.clearTablet();
                }
                for (MysteriousTabletData.FragmentType type : MysteriousTabletData.FragmentType.values()) {
                    if (data.hasFragment(type)
                            && time - data.getFragmentLastSeen(type) > PLAYER_CHECK_INTERVAL
                            && !MysteriousTabletItem.isFragmentPresent(
                                    event.getServer(), type, data.getFragmentId(type))) {
                        data.clearFragment(type);
                    }
                }
            }
        }

        // Slow-path fallback: time-based cleanup covers offline players whose .dat files
        // can't be scanned cheaply at runtime.
        if (time % CLEANUP_INTERVAL != 0) {
            return;
        }

        MysteriousTabletData data = MysteriousTabletData.get(event.getServer());
        if (data.isLockedByCastle()) {
            return;
        }

        if (data.tabletExists()
                && time - data.getTabletLastSeen() > UNSEEN_TICKS
                && !MysteriousTabletItem.isTabletPresent(event.getServer(), data.getTabletId())) {
            data.clearTablet();
        }

        for (MysteriousTabletData.FragmentType type : MysteriousTabletData.FragmentType.values()) {
            if (!data.hasFragment(type)) {
                continue;
            }
            if (time - data.getFragmentLastSeen(type) > UNSEEN_TICKS
                    && !MysteriousTabletItem.isFragmentPresent(
                            event.getServer(), type, data.getFragmentId(type))) {
                data.clearFragment(type);
            }
        }

        // If no player holds the LOWER fragment, ensure chest copies exist in all known positions.
        if (data.canSpawnFragment(MysteriousTabletData.FragmentType.LOWER)) {
            ServerLevel overworldLevel = event.getServer().overworld();
            Set<BlockPos> ancientPositions = data.getAncientCityChestPositions();
            if (!ancientPositions.isEmpty()) {
                for (BlockPos pos : ancientPositions) {
                    if (overworldLevel.isLoaded(pos)) {
                        BlockEntity be = overworldLevel.getBlockEntity(pos);
                        if (be instanceof ChestBlockEntity chest) {
                            ensureFragmentInChest(overworldLevel, chest, MysteriousTabletData.FragmentType.LOWER);
                        }
                    }
                }
            } else {
                // No known positions yet — scan for a loaded ancient city near any online player
                tryPopulateAncientCityChest(overworldLevel, data);
            }
        }
    }

    /**
     * When no ancient city chest positions are known, scans loaded ancient city structure
     * chunks near online players and places a LOWER chest copy in every chest found.
     */
    private static void tryPopulateAncientCityChest(ServerLevel level, MysteriousTabletData data) {
        ResourceKey<Structure> cityKey = ResourceKey.create(Registries.STRUCTURE,
                ResourceLocation.withDefaultNamespace("ancient_city"));
        var registry = level.registryAccess().registry(Registries.STRUCTURE).orElse(null);
        if (registry == null) return;
        var holder = registry.getHolder(cityKey).orElse(null);
        if (holder == null) return;

        for (ServerPlayer player : level.players()) {
            StructureStart start = level.structureManager()
                    .getStructureWithPieceAt(player.blockPosition(), HolderSet.direct(holder));
            if (start == null || !start.isValid()) continue;

            BoundingBox box = start.getBoundingBox();
            for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
                for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;
                    for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                        if (!(entry.getValue() instanceof ChestBlockEntity chest)) continue;
                        ensureFragmentInChest(level, chest, MysteriousTabletData.FragmentType.LOWER);
                        data.addAncientCityChestPos(entry.getKey());
                        // Keep scanning — we want ALL chests in this city to have a copy
                    }
                }
            }
            // Found a structure near this player — positions recorded, stop scanning
            if (!data.getAncientCityChestPositions().isEmpty()) return;
        }
    }

    private static boolean syncStack(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        if (stack.getItem() instanceof MysteriousTabletFragmentItem fragmentItem) {
            return fragmentItem.syncFragment(level, stack);
        }

        if (stack.getItem() instanceof MysteriousTabletItem tabletItem) {
            return tabletItem.syncTablet(level, stack);
        }

        return true;
    }

    /**
     * Ensures a chest copy of the given fragment type is present in the chest.
     * If already present (with or without ChestCopy flag), does nothing.
     * If absent, places a new ChestCopy-flagged fragment in the first empty slot.
     */
    private static void ensureFragmentInChest(ServerLevel level, ChestBlockEntity chest,
                                              MysteriousTabletData.FragmentType type) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof MysteriousTabletFragmentItem f
                    && f.getFragmentType() == type) {
                // Fragment already present; ensure it has the ChestCopy flag (migration)
                if (!MysteriousTabletFragmentItem.isChestCopy(stack)) {
                    MysteriousTabletFragmentItem.setChestCopy(stack, true);
                    chest.setChanged();
                }
                return;
            }
        }

        ItemStack fragment = new ItemStack(switch (type) {
            case LOWER -> ModItems.LOWER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get();
            case LEFT  -> ModItems.LEFT_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get();
            case UPPER -> ModItems.UPPER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get();
            case RIGHT -> ModItems.RIGHT_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get();
        });
        MysteriousTabletFragmentItem.setChestCopy(fragment, true);
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).isEmpty()) {
                chest.setItem(i, fragment);
                chest.setChanged();
                return;
            }
        }
    }
}
package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MysteriousTabletData;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.item.custom.MysteriousTabletFragmentItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Set;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SpiritWorldChestSpawner {
    private static final int PLAYER_CHECK_INTERVAL = 100;
    private static final ResourceLocation STRUCTURE_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
            "fragment_structure");
    private static final ResourceKey<LootTable> FRAGMENT_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "chests/spirit_world_cache"));
    private static final ResourceKey<Structure> FRAGMENT_STRUCTURE_KEY = ResourceKey.create(Registries.STRUCTURE, STRUCTURE_ID);

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!level.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            return;
        }

        MysteriousTabletData data = MysteriousTabletData.get(level.getServer());
        if (!data.canSpawnFragment(MysteriousTabletData.FragmentType.LEFT)) {
            return;
        }

        ChunkPos chunkPos = event.getChunk().getPos();
        BlockPos probePos = new BlockPos(
                chunkPos.getMinBlockX() + 8,
                level.getMinBuildHeight() + 1,
                chunkPos.getMinBlockZ() + 8
        );

        StructureStart start = getFragmentStructureStart(level, probePos);
        if (start == null || !start.isValid()) {
            return;
        }

        tryPopulateStructureChest(level, data, start);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!level.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            return;
        }
        if (level.getGameTime() % PLAYER_CHECK_INTERVAL != 0) {
            return;
        }

        MysteriousTabletData data = MysteriousTabletData.get(level.getServer());
        if (!data.canSpawnFragment(MysteriousTabletData.FragmentType.LEFT)) {
            return;
        }

        // If we know of chest positions, ensure copies exist in all loaded ones
        Set<BlockPos> knownPositions = data.getSpiritChestPositions();
        if (!knownPositions.isEmpty()) {
            for (BlockPos knownChestPos : knownPositions) {
                if (level.isLoaded(knownChestPos)) {
                    BlockEntity be = level.getBlockEntity(knownChestPos);
                    if (be instanceof ChestBlockEntity chest) {
                        ensureFragmentInChest(level, chest);
                    }
                }
            }
            return;
        }

        for (ServerPlayer player : level.players()) {
            StructureStart start = getFragmentStructureStart(level, player.blockPosition());
            if (start == null || !start.isValid()) {
                continue;
            }

            if (tryPopulateStructureChest(level, data, start)) {
                return;
            }
        }
    }

    private static StructureStart getFragmentStructureStart(ServerLevel level, BlockPos probePos) {
        var registry = level.registryAccess().registry(Registries.STRUCTURE).orElse(null);
        if (registry == null) {
            return null;
        }

        var holder = registry.getHolder(FRAGMENT_STRUCTURE_KEY).orElse(null);
        if (holder == null) {
            return null;
        }

        return level.structureManager().getStructureWithPieceAt(probePos, HolderSet.direct(holder));
    }

    private static BlockPos getTopCenter(BoundingBox box) {
        int x = (box.minX() + box.maxX()) / 2;
        int z = (box.minZ() + box.maxZ()) / 2;
        int y = box.maxY();
        return new BlockPos(x, y, z);
    }

    private static boolean tryPopulateStructureChest(ServerLevel level, MysteriousTabletData data, StructureStart start) {
        BoundingBox box = start.getBoundingBox();

        BlockPos chestPos = findChestInStructure(level, box);
        if (chestPos == null) {
            chestPos = getTopCenter(box);
            if (!level.getBlockState(chestPos).isAir()) {
                chestPos = chestPos.above();
            }
            level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        }

        BlockEntity blockEntity = level.getBlockEntity(chestPos);
        if (blockEntity instanceof ChestBlockEntity chest) {
            ensureFragmentInChest(level, chest);
            data.addSpiritChestPos(chestPos);
            return true;
        }

        return false;
    }

    private static BlockPos findChestInStructure(ServerLevel level, BoundingBox box) {
        BlockPos center = getTopCenter(box);
        if (level.getBlockEntity(center) instanceof ChestBlockEntity) {
            return center;
        }

        for (int y = box.minY(); y <= box.maxY(); y++) {
            for (int x = box.minX(); x <= box.maxX(); x++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockEntity(pos) instanceof ChestBlockEntity) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    private static boolean ensureFragmentInChest(ServerLevel level, ChestBlockEntity chest) {
        chest.unpackLootTable(null);

        // Scan for an existing chest copy of LEFT type — don't place duplicates
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof MysteriousTabletFragmentItem f
                    && f.getFragmentType() == MysteriousTabletData.FragmentType.LEFT) {
                // If the item doesn't have the ChestCopy flag (old format), apply it for migration
                if (!MysteriousTabletFragmentItem.isChestCopy(stack)) {
                    MysteriousTabletFragmentItem.setChestCopy(stack, true);
                    chest.setChanged();
                }
                return true;
            }
        }

        // No fragment found — place a new chest copy
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).isEmpty()) {
                ItemStack fragment = new ItemStack(ModItems.LEFT_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get());
                MysteriousTabletFragmentItem.setChestCopy(fragment, true);
                chest.setItem(i, fragment);
                chest.setChanged();
                return true;
            }
        }

        return false;
    }
}

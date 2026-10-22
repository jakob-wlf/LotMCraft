package de.jakob.lotm.beyonders.abilities.black_emperor.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SealRegistry {

    private static final Map<UUID, SealedRegion> BY_OWNER = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Set<SealedRegion>> BY_DIM = new ConcurrentHashMap<>();

    private SealRegistry() {}

    public static void add(SealedRegion region) {
        BY_OWNER.put(region.owner, region);
        BY_DIM.computeIfAbsent(region.dimension, k -> ConcurrentHashMap.newKeySet()).add(region);
    }

    public static SealedRegion getByOwner(UUID owner) { return BY_OWNER.get(owner); }

    public static void remove(SealedRegion region) {
        BY_OWNER.remove(region.owner, region);
        Set<SealedRegion> set = BY_DIM.get(region.dimension);
        if (set != null) set.remove(region);
    }

    public static Set<SealedRegion> getAllInDimension(ResourceKey<Level> dim) {
        return BY_DIM.getOrDefault(dim, Set.of());
    }

    public static SealedRegion getRegionContaining(ServerLevel level, Vec3 pos) {
        Set<SealedRegion> regions = BY_DIM.get(level.dimension());
        if (regions == null) return null;
        for (SealedRegion r : regions) if (r.contains(pos)) return r;
        return null;
    }

    public static SealedRegion getRegionWithBoundary(ServerLevel level, BlockPos pos) {
        Set<SealedRegion> regions = BY_DIM.get(level.dimension());
        if (regions == null) return null;
        for (SealedRegion r : regions) if (r.boundary.contains(pos)) return r;
        return null;
    }

    public static boolean isBreached(ServerLevel level, SealedRegion region) {
        for (var entry : region.originalBoundaryBlocks.entrySet()) {
            var now = level.getBlockState(entry.getKey()).getBlock();
            if(now == Blocks.AIR) level.getServer().sendSystemMessage(Component.literal(now + " " + entry.getValue()));
            if (now == Blocks.AIR && entry.getValue() != Blocks.AIR) return true;
        }
        return false;
    }
}
package de.jakob.lotm.beyonders.abilities.black_emperor.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class SealedRegion {

    public final UUID owner;
    public final ResourceKey<Level> dimension;
    public final Set<BlockPos> boundary;
    public final Set<BlockPos> interior;
    public final Map<BlockPos, Block> originalBoundaryBlocks;
    public final AABB interiorBounds;
    public final long sealedAtTick;

    public SealedRegion(UUID owner, ResourceKey<Level> dim,
                        Set<BlockPos> boundary, Set<BlockPos> interior,  AABB interiorBounds,
                        ServerLevel level, long tick) {
        this.owner = owner;
        this.dimension = dim;
        this.boundary = Set.copyOf(boundary);
        this.interior = Set.copyOf(interior);
        Map<BlockPos, Block> orig = new HashMap<>();
        for (BlockPos pos : boundary) orig.put(pos, level.getBlockState(pos).getBlock());
        this.originalBoundaryBlocks = Map.copyOf(orig);
        this.interiorBounds = interiorBounds;
        this.sealedAtTick = tick;
    }

    public boolean contains(Vec3 worldPos) {
        return interiorBounds.contains(worldPos);
    }
}
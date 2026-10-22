package de.jakob.lotm.beyonders.abilities.black_emperor.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public final class RoomDetector {

    public static final int MAX_INTERIOR_VOLUME = 4096;
    public static final int MAX_RADIUS          = 16;
    public static final int MIN_FOOTPRINT_X     = 3;
    public static final int MIN_FOOTPRINT_Z     = 3;
    public static final int MIN_HEIGHT          = 2;

    public record DetectedRoom(
            Set<BlockPos> interior,
            Set<BlockPos> boundary,
            AABB interiorBounds
    ) {}

    private RoomDetector() {}

    public static DetectedRoom detect(ServerLevel level, BlockPos origin) {
        if (!isPassable(level, origin)) return null;

        Set<BlockPos> interior = new HashSet<>();
        Set<BlockPos> boundary = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(origin);
        visited.add(origin);

        int r2 = MAX_RADIUS * MAX_RADIUS;

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            if (pos.distSqr(origin) > r2) return null;
            if (!isPassable(level, pos)) continue;

            interior.add(pos);
            if (interior.size() > MAX_INTERIOR_VOLUME) return null;

            for (var dir : DIRS) {
                BlockPos n = pos.offset(dir);
                if (visited.contains(n)) continue;
                visited.add(n);

                if (isPassable(level, n)) {
                    queue.add(n);
                } else {
                    boundary.add(n);
                }
            }
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : interior) {
            minX = Math.min(minX, p.getX()); maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY()); maxY = Math.max(maxY, p.getY());
            minZ = Math.min(minZ, p.getZ()); maxZ = Math.max(maxZ, p.getZ());
        }
        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        if (sizeX < MIN_FOOTPRINT_X || sizeZ < MIN_FOOTPRINT_Z || sizeY < MIN_HEIGHT) return null;

        AABB bounds = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        return new DetectedRoom(interior, boundary, bounds);
    }

    private static final BlockPos[] DIRS = {
            new BlockPos( 1, 0, 0), new BlockPos(-1, 0, 0),
            new BlockPos( 0, 1, 0), new BlockPos( 0,-1, 0),
            new BlockPos( 0, 0, 1), new BlockPos( 0, 0,-1)
    };

    public static boolean isPassable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DoorBlock
                || state.getBlock() instanceof TrapDoorBlock
                || state.getBlock() instanceof FenceGateBlock) {
            return false;
        }
        return state.getCollisionShape(level, pos).isEmpty();
    }
}
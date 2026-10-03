package de.jakob.lotm.beyonders.abilities.demoness.handlers;

import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.*;
import java.util.function.Consumer;

public class GlassScanJob {

    private static final int REGION_SIZE = 16;
    private static final int REGIONS_PER_TICK = 2;

    private final ServerLevel level;
    private final Vec3 center;
    private final double radiusSqr;
    private final Consumer<BlockPos> callback;

    private final Deque<AABB> pendingRegions = new ArrayDeque<>();

    private BlockPos bestCandidate = null;
    private double bestDistSqr = Double.MAX_VALUE;

    public GlassScanJob(ServerLevel level, Vec3 center, int radius, Consumer<BlockPos> callback) {
        this.level = level;
        this.center = center;
        this.radiusSqr = (double) radius * radius;
        this.callback = callback;
        buildRegionQueue(radius);
    }

    private void buildRegionQueue(int radius) {
        int minX = Mth.floor(center.x - radius);
        int minY = Mth.floor(center.y - radius);
        int minZ = Mth.floor(center.z - radius);
        int maxX = Mth.ceil(center.x + radius);
        int maxY = Mth.ceil(center.y + radius);
        int maxZ = Mth.ceil(center.z + radius);

        List<AABB> regions = new ArrayList<>();

        for (int x = minX; x < maxX; x += REGION_SIZE) {
            for (int y = minY; y < maxY; y += REGION_SIZE) {
                for (int z = minZ; z < maxZ; z += REGION_SIZE) {
                    AABB region = new AABB(
                            x, y, z,
                            Math.min(x + REGION_SIZE, maxX),
                            Math.min(y + REGION_SIZE, maxY),
                            Math.min(z + REGION_SIZE, maxZ)
                    );

                    if (regionMinDistSqr(region) <= radiusSqr) {
                        regions.add(region);
                    }
                }
            }
        }

        regions.sort(Comparator.comparingDouble(this::regionMinDistSqr));
        pendingRegions.addAll(regions);
    }

    private double regionMinDistSqr(AABB region) {
        double dx = Math.max(0, Math.max(region.minX - center.x, center.x - region.maxX));
        double dy = Math.max(0, Math.max(region.minY - center.y, center.y - region.maxY));
        double dz = Math.max(0, Math.max(region.minZ - center.z, center.z - region.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }

    public void start() {
        tick();
    }

    private void tick() {
        int processed = 0;

        while (processed < REGIONS_PER_TICK && !pendingRegions.isEmpty()) {
            if (bestCandidate != null && bestDistSqr <= regionMinDistSqr(pendingRegions.peekFirst())) {
                callback.accept(bestCandidate);
                return;
            }

            scanRegion(pendingRegions.pollFirst());
            processed++;
        }

        if (pendingRegions.isEmpty()) {
            callback.accept(bestCandidate);
            return;
        }

        ServerScheduler.scheduleDelayed(1, this::tick);
    }

    private void scanRegion(AABB region) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        int minX = Mth.floor(region.minX);
        int minY = Mth.floor(region.minY);
        int minZ = Mth.floor(region.minZ);
        int maxX = Mth.floor(region.maxX) - 1;
        int maxY = Mth.floor(region.maxY) - 1;
        int maxZ = Mth.floor(region.maxZ) - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutable.set(x, y, z);

                    double distSqr = mutable.distToCenterSqr(center);
                    if (distSqr > radiusSqr || distSqr >= bestDistSqr) continue;

                    BlockState state = level.getBlockState(mutable);
                    if (state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES)) {
                        bestDistSqr = distSqr;
                        bestCandidate = mutable.immutable();
                    }
                }
            }
        }
    }
}
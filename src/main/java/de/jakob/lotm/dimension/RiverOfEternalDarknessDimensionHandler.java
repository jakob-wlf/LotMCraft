package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.RiverOfEternalDarknessData;
import de.jakob.lotm.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class RiverOfEternalDarknessDimensionHandler {

    private static final int FLOOR_Y = 64;
    private static final int HALF_SIZE = 100;
    private static final int RIVER_START_Z = -100;
    private static final int RIVER_END_Z = 100;
    private static final int THRONE_Z = 100;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (level.isClientSide) {
            return;
        }

        if (!level.dimension().equals(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY)) {
            return;
        }

        RiverOfEternalDarknessData data = RiverOfEternalDarknessData.get(level.getServer());
        if (data.isRiverBuilt()) {
            return;
        }

        buildRiver(level);
        data.setRiverBuilt(true);
    }

    private static void buildRiver(ServerLevel level) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = -HALF_SIZE; x <= HALF_SIZE; x++) {
            for (int z = -HALF_SIZE; z <= HALF_SIZE; z++) {
                pos.set(x, FLOOR_Y, z);
                level.setBlock(pos, selectFloorState(x, z), 3);
            }
        }

        buildEndPlatform(level);

        for (int z = RIVER_START_Z; z <= RIVER_END_Z; z++) {
            int centerX = riverCenterX(z);
            int halfWidth = riverHalfWidth(z);

            int channelMin = centerX - halfWidth;
            int channelMax = centerX + halfWidth;
            int bankMin = channelMin - 3;
            int bankMax = channelMax + 3;
            int bedY = FLOOR_Y - 4;

            for (int x = bankMin; x <= bankMax; x++) {
                if (x >= channelMin && x <= channelMax) {
                    // Carve a deeper river channel
                    pos.set(x, FLOOR_Y, z);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

                    pos.set(x, bedY, z);
                    level.setBlock(pos, selectBedState(x, z), 3);

                    for (int y = bedY + 1; y <= FLOOR_Y - 1; y++) {
                        pos.set(x, y, z);
                        level.setBlock(pos, ModFluids.DROPS_OF_ETERNAL_DARKNESS_SOURCE.get()
                            .defaultFluidState()
                            .createLegacyBlock(), 3);
                    }

                    if (x == channelMin || x == channelMax) {
                        BlockState slope = Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, x < centerX ? Direction.EAST : Direction.WEST);
                        pos.set(x, FLOOR_Y - 2, z);
                        level.setBlock(pos, slope, 3);
                    }

                    if (x == channelMin + 1 || x == channelMax - 1) {
                        BlockState slope = Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, x < centerX ? Direction.EAST : Direction.WEST);
                        pos.set(x, FLOOR_Y - 3, z);
                        level.setBlock(pos, slope, 3);
                    }
                } else {
                    // Build raised banks
                    pos.set(x, FLOOR_Y, z);
                    level.setBlock(pos, selectBankState(x, z), 3);

                    if (x == channelMin - 1 || x == channelMax + 1) {
                        for (int y = bedY; y <= FLOOR_Y; y++) {
                            pos.set(x, y, z);
                            level.setBlock(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
                        }
                    }

                    if (x == bankMin || x == bankMax) {
                        BlockState outerSlope = Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, x == bankMin ? Direction.WEST : Direction.EAST);
                        pos.set(x, FLOOR_Y, z);
                        level.setBlock(pos, outerSlope, 3);

                        if ((z % 16 == 0)) {
                            pos.set(x, FLOOR_Y, z);
                            level.setBlock(pos, Blocks.SOUL_LANTERN.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        buildRiverStartCap(level);
        addDecorations(level);
        buildThrone(level, new BlockPos(0, FLOOR_Y + 1, THRONE_Z));
    }

    private static void buildRiverStartCap(ServerLevel level) {
        int centerX = riverCenterX(RIVER_START_Z);
        int halfWidth = riverHalfWidth(RIVER_START_Z);
        int channelMin = centerX - halfWidth;
        int channelMax = centerX + halfWidth;
        int bedY = FLOOR_Y - 4;
        int capZ = RIVER_START_Z - 1;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = channelMin - 1; x <= channelMax + 1; x++) {
                for (int y = bedY; y <= FLOOR_Y; y++) {
                pos.set(x, y, capZ);
                level.setBlock(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
            }
        }
    }

    private static int riverCenterX(int z) {
        double base = 20.0 * Math.sin(z / 15.0);
        if (z >= THRONE_Z - 15) {
            double t = Math.min(1.0, (z - (THRONE_Z - 15)) / 15.0);
            base *= 1.0 - t;
        }
        return (int) Math.round(base);
    }

    private static int riverHalfWidth(int z) {
        int halfWidth = 5
                + (int) Math.round(2.0 * Math.sin(z / 11.0))
                + (int) Math.round(1.0 * Math.sin(z / 5.0));
        if (z >= THRONE_Z - 15) {
            halfWidth += (int) Math.round((z - (THRONE_Z - 15)) / 4.0);
        }
        return Math.max(4, Math.min(10, halfWidth));
    }

    private static void buildEndPlatform(ServerLevel level) {
        int platformZ = -HALF_SIZE + 2;
        int platformDepth = 10;
        int platformHalfWidth = 20;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = -platformHalfWidth; x <= platformHalfWidth; x++) {
            for (int z = platformZ; z <= platformZ + platformDepth; z++) {
                pos.set(x, FLOOR_Y, z);
                level.setBlock(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);

                if (x == -platformHalfWidth || x == platformHalfWidth
                        || z == platformZ || z == platformZ + platformDepth) {
                    pos.set(x, FLOOR_Y + 1, z);
                    level.setBlock(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void addDecorations(ServerLevel level) {
        List<MoundInfo> mounds = new ArrayList<>();
        for (int x = -HALF_SIZE + 8; x <= HALF_SIZE - 8; x += 14) {
            for (int z = -HALF_SIZE + 8; z <= HALF_SIZE - 8; z += 14) {
                int jitterX = Math.floorMod(hashCoord(x, z), 7) - 3;
                int jitterZ = Math.floorMod(hashCoord(z, x), 7) - 3;
                int placeX = x + jitterX;
                int placeZ = z + jitterZ;

                if (isNearRiver(placeX, placeZ) || isNearThrone(placeX, placeZ) || isNearEndPlatform(placeX, placeZ)) {
                    continue;
                }

                int roll = Math.floorMod(hashCoord(placeX, placeZ), 100);
                if (roll < 70) {
                    int height = 14 + Math.floorMod(hashCoord(placeZ, placeX * 3), 18);
                    int radius = 5 + Math.floorMod(hashCoord(placeZ + 5, placeX - 2), 7);
                    if (isOutsideBasePlatform(placeX, placeZ, radius + 3)) {
                        continue;
                    }
                    if (isNearRiver(placeX, placeZ, radius + 3)) {
                        continue;
                    }
                    if (isNearEndPlatform(placeX, placeZ, radius + 4)) {
                        continue;
                    }
                    BlockPos center = new BlockPos(placeX, FLOOR_Y + 1, placeZ);
                    buildMound(level, center, height, radius);
                    mounds.add(new MoundInfo(center, height));
                }
            }
        }

        linkMounds(level, mounds);
    }

    private static boolean isNearRiver(int x, int z) {
        return isNearRiver(x, z, 6);
    }

    private static boolean isNearRiver(int x, int z, int padding) {
        if (z < RIVER_START_Z - 6 || z > RIVER_END_Z + 6) {
            return false;
        }

        int centerX = riverCenterX(z);
        int halfWidth = riverHalfWidth(z);
        return Math.abs(x - centerX) <= halfWidth + padding;
    }

    private static boolean isNearEndPlatform(int x, int z) {
        return isNearEndPlatform(x, z, 4);
    }

    private static boolean isNearEndPlatform(int x, int z, int padding) {
        int platformZ = -HALF_SIZE + 2;
        int platformDepth = 10;
        int platformHalfWidth = 20;
        int minX = -platformHalfWidth - padding;
        int maxX = platformHalfWidth + padding;
        int minZ = platformZ - padding;
        int maxZ = platformZ + platformDepth + padding;
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    private static boolean isNearThrone(int x, int z) {
        return z >= THRONE_Z - 10 && Math.abs(x) <= 16;
    }

    private static boolean isOutsideBasePlatform(int x, int z, int padding) {
        int min = -HALF_SIZE + padding;
        int max = HALF_SIZE - padding;
        return x < min || x > max || z < min || z > max;
    }

    private static void buildMound(ServerLevel level, BlockPos center, int height, int baseRadius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = 0; y < height; y++) {
            int shrink = Math.max(1, y / 2);
            int radius = Math.max(1, baseRadius - shrink + Math.floorMod(hashCoord(center.getX(), center.getZ() + y), 2));
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    int distSq = x * x + z * z;
                    int jag = Math.floorMod(hashCoord(center.getX() + x + y, center.getZ() + z - y), 5);
                    if (distSq > radius * radius + jag) {
                        continue;
                    }

                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    level.setBlock(pos, selectMoundState(pos.getX(), pos.getY(), pos.getZ()), 3);
                }
            }
        }

        pos.set(center.getX(), center.getY() + height, center.getZ());
        level.setBlock(pos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);

        addMoundFires(level, center, baseRadius + 2);
    }

    private static void buildThrone(ServerLevel level, BlockPos center) {
        return;
    }

    private static void addMoundFires(ServerLevel level, BlockPos center, int radius) {
        int y = center.getY();
        int[][] offsets = new int[][]{
                {radius, 0}, {-radius, 0}, {0, radius}, {0, -radius},
                {radius, radius}, {radius, -radius}, {-radius, radius}, {-radius, -radius}
        };

        for (int[] offset : offsets) {
            int x = center.getX() + offset[0];
            int z = center.getZ() + offset[1];
            if (isOutsideBasePlatform(x, z, 1)) {
                continue;
            }
            if (Math.floorMod(hashCoord(x, z), 3) == 0 && !isNearRiver(x, z)) {
                placeSoulFire(level, new BlockPos(x, y, z));
            }
        }
    }

    private static void placeSoulFire(ServerLevel level, BlockPos groundPos) {
        if (isNearRiver(groundPos.getX(), groundPos.getZ())) {
            return;
        }

        if (isOutsideBasePlatform(groundPos.getX(), groundPos.getZ(), 1)) {
            return;
        }

            BlockPos basePos = groundPos.below();
            if (!level.getBlockState(groundPos).isAir()) {
                level.setBlock(groundPos, Blocks.AIR.defaultBlockState(), 3);
            }

            level.setBlock(basePos, Blocks.SOUL_SOIL.defaultBlockState(), 3);
            level.setBlock(groundPos, Blocks.SOUL_FIRE.defaultBlockState(), 3);
    }

    private static BlockState selectFloorState(int x, int z) {
        int selector = Math.floorMod(hashCoord(x, z), 14);
        return switch (selector) {
            case 0, 1, 2 -> Blocks.DEEPSLATE_TILES.defaultBlockState();
            case 3, 4 -> Blocks.DEEPSLATE_BRICKS.defaultBlockState();
            case 5 -> Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState();
            case 6 -> Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
            case 7 -> Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            case 8 -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case 9 -> Blocks.BLACKSTONE.defaultBlockState();
            case 10 -> Blocks.GILDED_BLACKSTONE.defaultBlockState();
            default -> Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        };
    }

    private static BlockState selectBankState(int x, int z) {
        int selector = Math.floorMod(hashCoord(x, z), 9);
        return switch (selector) {
            case 0 -> Blocks.DEEPSLATE_TILES.defaultBlockState();
            case 1 -> Blocks.DEEPSLATE_BRICKS.defaultBlockState();
            case 2 -> Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            case 3 -> Blocks.BLACKSTONE.defaultBlockState();
            case 4 -> Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
            case 5 -> Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            case 6 -> Blocks.OBSIDIAN.defaultBlockState();
            default -> Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        };
    }

    private static BlockState selectBedState(int x, int z) {
        int selector = Math.floorMod(hashCoord(x, z), 7);
        return switch (selector) {
            case 0 -> Blocks.POLISHED_BASALT.defaultBlockState();
            case 1 -> Blocks.SMOOTH_BASALT.defaultBlockState();
            case 2 -> Blocks.BASALT.defaultBlockState();
            case 3 -> Blocks.BLACKSTONE.defaultBlockState();
            case 4 -> Blocks.DEEPSLATE_TILES.defaultBlockState();
            case 5 -> Blocks.DEEPSLATE_BRICKS.defaultBlockState();
            default -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
        };
    }

    private static BlockState selectMoundState(int x, int y, int z) {
        int selector = Math.floorMod(hashCoord(x + y * 5, z), 6);
        return switch (selector) {
            case 0 -> Blocks.DEEPSLATE_BRICKS.defaultBlockState();
            case 1 -> Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
            case 2 -> Blocks.BLACKSTONE.defaultBlockState();
            case 3 -> Blocks.POLISHED_BASALT.defaultBlockState();
            case 4 -> Blocks.SMOOTH_BASALT.defaultBlockState();
            default -> Blocks.OBSIDIAN.defaultBlockState();
        };
    }

    private static void linkMounds(ServerLevel level, List<MoundInfo> mounds) {
        for (int i = 0; i < mounds.size(); i++) {
            MoundInfo current = mounds.get(i);
            MoundInfo nearest = null;
            int bestDist = Integer.MAX_VALUE;
            for (int j = i + 1; j < mounds.size(); j++) {
                MoundInfo other = mounds.get(j);
                int dist = Math.abs(current.center.getX() - other.center.getX())
                        + Math.abs(current.center.getZ() - other.center.getZ());
                if (dist < bestDist) {
                    bestDist = dist;
                    nearest = other;
                }
            }

            if (nearest != null && bestDist < 50) {
                connectMounds(level, current, nearest);
            }
        }
    }

    private static void connectMounds(ServerLevel level, MoundInfo a, MoundInfo b) {
        int baseY = Math.min(a.topY(), b.topY()) - 2;
        BlockPos start = new BlockPos(a.center.getX(), baseY, a.center.getZ());
        BlockPos mid = new BlockPos(b.center.getX(), baseY, a.center.getZ());
        BlockPos end = new BlockPos(b.center.getX(), baseY, b.center.getZ());

        placeChainLinkLine(level, start, mid, Direction.Axis.X, 4);
        placeChainLinkLine(level, mid, end, Direction.Axis.Z, 4);

        placeSoulFire(level, new BlockPos(a.center.getX(), FLOOR_Y, a.center.getZ()));
        placeSoulFire(level, new BlockPos(b.center.getX(), FLOOR_Y, b.center.getZ()));
        placeSoulFire(level, new BlockPos((a.center.getX() + b.center.getX()) / 2, FLOOR_Y,
                (a.center.getZ() + b.center.getZ()) / 2));
    }

    private static void placeChainLinkLine(ServerLevel level, BlockPos start, BlockPos end, Direction.Axis axis, int sag) {
        int dx = end.getX() - start.getX();
        int dz = end.getZ() - start.getZ();
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps == 0) {
            return;
        }

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            int x = start.getX() + (int) Math.round(dx * t);
            int z = start.getZ() + (int) Math.round(dz * t);
            int sagOffset = (int) Math.round(Math.sin(Math.PI * t) * sag);
            int y = start.getY() - sagOffset;

            placeChainLink(level, new BlockPos(x, y, z), axis, i % 3 == 0);
        }
    }

    private static void placeChainLink(ServerLevel level, BlockPos center, Direction.Axis axis, boolean thick) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState core = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        pos.set(center);
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, core, 3);
        }

        if (!thick) {
            return;
        }

        int[][] offsets = axis == Direction.Axis.X
                ? new int[][]{{0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0}}
                : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}};

        for (int[] offset : offsets) {
            pos.set(center.getX() + offset[0], center.getY() + offset[1], center.getZ() + offset[2]);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, core, 3);
            }
        }
    }

    private static final class MoundInfo {
        private final BlockPos center;
        private final int height;

        private MoundInfo(BlockPos center, int height) {
            this.center = center;
            this.height = height;
        }

        private int topY() {
            return center.getY() + height;
        }
    }

    private static int hashCoord(int x, int z) {
        int hash = x * 734287 ^ z * 912931;
        hash = (hash << 13) ^ hash;
        return hash * (hash * hash * 15731 + 789221) + 1376312589;
    }
}

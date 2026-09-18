// MirrorGateManager.java
package de.jakob.lotm.dimension;

import de.jakob.lotm.entity.custom.ability_entities.PortalEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public final class MirrorGateManager {

    public static final float MIRROR_WORLD_OFFSET = 4;

    public static BlockPos getCoordinatesInMirrorWorld(BlockPos origin, Level level) {
        double x = origin.getX() / MIRROR_WORLD_OFFSET;
        double y = Math.max(10, origin.getY());
        double z = origin.getZ() / MIRROR_WORLD_OFFSET;

        BlockPos returnPos = level.getWorldBorder().clampToBounds(BlockPos.containing(x, y, z));
        returnPos = getSafePosition((ServerLevel) level, returnPos);
        if(returnPos == null) {
            returnPos = level.getWorldBorder().clampToBounds(BlockPos.containing(x, y, z));
            level.setBlockAndUpdate(returnPos.below(), Blocks.GLASS.defaultBlockState());
        }
        return returnPos;
    }

    public static BlockPos getCoordinatesInOverworld(BlockPos origin, Level level) {
        double x = origin.getX() * MIRROR_WORLD_OFFSET;
        double y = origin.getY();
        double z = origin.getZ() * MIRROR_WORLD_OFFSET;

        return level.getWorldBorder().clampToBounds(BlockPos.containing(x, y, z));
    }

    private static BlockPos getSafePosition(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable().setY(level.getMaxBuildHeight());
        while (mutablePos.getY() > 0) {
            BlockState state = level.getBlockState(mutablePos);
            if (state.isAir() && !level.getBlockState(mutablePos.below()).isAir()) {
                return mutablePos.immutable();
            }
            mutablePos.move(Direction.DOWN);
        }
        return null;
    }

    private static final BlockState FRAME = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    private static final BlockState GLASS_GRAY = Blocks.GRAY_STAINED_GLASS.defaultBlockState();
    private static final BlockState GLASS_WHITE = Blocks.WHITE_STAINED_GLASS.defaultBlockState();
    private static final BlockState MIRROR_SEAM = Blocks.TINTED_GLASS.defaultBlockState();


    private static final String[] ARCH_SHAPE = {
            "..#G#..",
            ".#GGG#.",
            "#GGGGG#",
            "#GGGGG#",
            "#GGGGG#",
            "#GGGGG#",
            "#GGGGG#"
    };

    public static void createMirrorGate(ServerLevel mirrorWorldLevel, BlockPos mirrorWorldPos, ServerLevel overworldLevel, BlockPos overworldPos) {
        BlockPos safePos = getSafePosition(mirrorWorldLevel, mirrorWorldPos);
        if (safePos != null) {
            mirrorWorldPos = safePos;
        }

        if (portalAlreadyExistsNear(mirrorWorldLevel, mirrorWorldPos)) {
            return;
        }

        PortalEntity portalToMirrorWorld = new PortalEntity(mirrorWorldPos.getCenter(), mirrorWorldLevel, overworldLevel, overworldPos.getCenter(), -1);
        mirrorWorldLevel.addFreshEntity(portalToMirrorWorld);

        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(mirrorWorldLevel.getRandom());
        Direction.Axis widthAxis = (facing.getAxis() == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;

        int width = ARCH_SHAPE[0].length();
        int height = ARCH_SHAPE.length;
        int halfWidth = width / 2;
        int centerCol = halfWidth;

        for (int row = 0; row < height; row++) {
            String line = ARCH_SHAPE[row];
            int y = mirrorWorldPos.getY() + (height - 1 - row);

            for (int col = 0; col < width; col++) {
                char c = line.charAt(col);
                if (c == '.') continue;

                int offset = col - halfWidth;
                int x = mirrorWorldPos.getX() + (widthAxis == Direction.Axis.X ? offset : 0);
                int z = mirrorWorldPos.getZ() + (widthAxis == Direction.Axis.Z ? offset : 0);

                switch (c) {
                    case '#': {
                        BlockPos pos = new BlockPos(x, y, z);
                        mirrorWorldLevel.setBlock(pos, FRAME, 3);
                        break;
                    }
                    case 'G': {
                        BlockPos pos = new BlockPos(x, y, z);

                        boolean isSeam = (col == centerCol) && (row % 2 == 0);
                        if (isSeam) {
                            mirrorWorldLevel.setBlock(pos, MIRROR_SEAM, 3);
                        } else {
                            boolean useGray = ((row + col) % 2 == 0);
                            mirrorWorldLevel.setBlock(pos, useGray ? GLASS_GRAY : GLASS_WHITE, 3);
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    private static boolean portalAlreadyExistsNear(ServerLevel level, BlockPos pos) {
        AABB box = new AABB(
                pos.getX() - 40, level.getMinBuildHeight(), pos.getZ() - 40,
                pos.getX() + 40, level.getMaxBuildHeight(), pos.getZ() + 40
        );
        double horizontalRadiusSq = 120;
        boolean entityNearby = level.getEntitiesOfClass(PortalEntity.class, box).stream()
                .anyMatch(portal -> {
                    double dx = portal.getX() - pos.getX();
                    double dz = portal.getZ() - pos.getZ();
                    return (dx * dx + dz * dz) < horizontalRadiusSq;
                });

        if (entityNearby) {
            return true;
        }

        return columnHasTintedGlass(level, pos);
    }

    private static boolean columnHasTintedGlass(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            cursor.set(pos.getX(), y, pos.getZ());
            if (level.getBlockState(cursor).is(Blocks.TINTED_GLASS)) {
                return true;
            }
        }
        return false;
    }
}
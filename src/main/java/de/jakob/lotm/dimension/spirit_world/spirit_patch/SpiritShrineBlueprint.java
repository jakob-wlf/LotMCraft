package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import de.jakob.lotm.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SpiritShrineBlueprint {

    public static final int RADIUS = 4;
    public static final int MAX_FOUNDATION_DEPTH = 12;

    private static Map<BlockPos, BlockState> cache;

    private SpiritShrineBlueprint() {}

    private static boolean inFootprint(int dx, int dz) { return dx * dx + dz * dz <= 20; }

    private static synchronized Map<BlockPos, BlockState> blueprint() {
        if (cache != null) return cache;
        Map<BlockPos, BlockState> m = new LinkedHashMap<>();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -RADIUS; dx <= RADIUS; dx++)
            for (int dz = -RADIUS; dz <= RADIUS; dz++)
                if (inFootprint(dx, dz))
                    for (int dy = 0; dy <= 8; dy++) m.put(new BlockPos(dx, dy, dz), air);

        for (int dx = -RADIUS; dx <= RADIUS; dx++)
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (!inFootprint(dx, dz)) continue;
                int d2 = dx * dx + dz * dz;
                BlockState s = d2 >= 13 ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                        : (((dx + dz) & 1) == 0 ? Blocks.SMOOTH_QUARTZ.defaultBlockState()
                        : Blocks.CALCITE.defaultBlockState());
                m.put(new BlockPos(dx, -1, dz), s);
            }

        for (int dy = 0; dy <= 3; dy++) {
            m.put(new BlockPos(-2, dy, 0), Blocks.QUARTZ_PILLAR.defaultBlockState());
            m.put(new BlockPos(2, dy, 0), Blocks.QUARTZ_PILLAR.defaultBlockState());
        }
        for (int dx = -2; dx <= 2; dx++) m.put(new BlockPos(dx, 4, 0), Blocks.QUARTZ_BRICKS.defaultBlockState());
        m.put(new BlockPos(-2, 4, 0), Blocks.SEA_LANTERN.defaultBlockState());
        m.put(new BlockPos(2, 4, 0), Blocks.SEA_LANTERN.defaultBlockState());
        m.put(new BlockPos(0, 5, 0), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());

        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                m.put(new BlockPos(3 * sx, 0, 3 * sz), Blocks.AMETHYST_BLOCK.defaultBlockState());
                m.put(new BlockPos(3 * sx, 1, 3 * sz), Blocks.AMETHYST_BLOCK.defaultBlockState());
                m.put(new BlockPos(3 * sx, 2, 3 * sz), Blocks.SEA_LANTERN.defaultBlockState());
            }

        BlockState portal = ModBlocks.SPIRIT_PORTAL.get().defaultBlockState();
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = 0; dy <= 2; dy++) m.put(new BlockPos(dx, dy, 0), portal);

        cache = m;
        return m;
    }

    public static BoundingBox boundsFor(BlockPos a) {
        return new BoundingBox(a.getX() - RADIUS, a.getY() - MAX_FOUNDATION_DEPTH - 1, a.getZ() - RADIUS,
                a.getX() + RADIUS, a.getY() + 8, a.getZ() + RADIUS);
    }

    public static void placeWorldgen(LevelAccessor level, BlockPos anchor, BoundingBox clip) {
        place(level, anchor, clip);
        for (int dx = -RADIUS; dx <= RADIUS; dx++)
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (!inFootprint(dx, dz)) continue;
                for (int k = 2; k <= MAX_FOUNDATION_DEPTH + 1; k++) {
                    BlockPos p = anchor.offset(dx, -k, dz);
                    BlockState cur = level.getBlockState(p);
                    boolean replaceable = cur.isAir() || !cur.getFluidState().isEmpty()
                            || cur.is(BlockTags.REPLACEABLE) || cur.is(BlockTags.LEAVES) || cur.is(BlockTags.LOGS);
                    if (!replaceable) break;
                    if (clip.isInside(p)) level.setBlock(p, Blocks.CALCITE.defaultBlockState(), 2);
                }
            }
    }

    public static void placeRuntime(LevelAccessor level, BlockPos anchor, boolean floating) {
        place(level, anchor, null);
        if (!floating) return;
        for (int k = 1; k <= 7; k++) {
            int rSq = 20 - 3 * k;
            for (int dx = -RADIUS; dx <= RADIUS; dx++)
                for (int dz = -RADIUS; dz <= RADIUS; dz++)
                    if (dx * dx + dz * dz <= rSq)
                        level.setBlock(anchor.offset(dx, -1 - k, dz),
                                (k % 3 == 0 ? Blocks.AMETHYST_BLOCK : Blocks.CALCITE).defaultBlockState(), 3);
        }
    }

    private static void place(LevelAccessor level, BlockPos anchor, BoundingBox clip) {
        int flags = clip == null ? 3 : 2;
        for (Map.Entry<BlockPos, BlockState> e : blueprint().entrySet()) {
            BlockPos p = anchor.offset(e.getKey());
            if (clip != null && !clip.isInside(p)) continue;
            level.setBlock(p, e.getValue(), flags);
        }
    }
}

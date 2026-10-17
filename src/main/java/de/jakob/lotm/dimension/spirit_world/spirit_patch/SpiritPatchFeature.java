package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SpiritPatchFeature extends Feature<NoneFeatureConfiguration> {

    private static final BlockState[] WOOLS = {
            Blocks.PINK_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState(),
            Blocks.MAGENTA_WOOL.defaultBlockState(), Blocks.LIGHT_BLUE_WOOL.defaultBlockState(),
            Blocks.PURPLE_WOOL.defaultBlockState(), Blocks.LIME_WOOL.defaultBlockState()
    };

    public SpiritPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos o = ctx.origin();
        return rand.nextInt(3) == 0 ? island(level, rand, o) : spire(level, rand, o);
    }

    private boolean spire(WorldGenLevel level, RandomSource rand, BlockPos base) {
        int height = 5 + rand.nextInt(10);
        for (int h = 0; h <= height; h++) {
            int r = (int) Math.round(2.5 * (1.0 - (double) h / height));
            BlockState s = h == height ? Blocks.SEA_LANTERN.defaultBlockState()
                    : (h % 3 == 0 ? Blocks.CALCITE : Blocks.AMETHYST_BLOCK).defaultBlockState();
            for (int dx = -r; dx <= r; dx++)
                for (int dz = -r; dz <= r; dz++)
                    if (dx * dx + dz * dz <= r * r + 1)
                        level.setBlock(base.offset(dx, h, dz), s, 2);
        }
        return true;
    }

    private boolean island(WorldGenLevel level, RandomSource rand, BlockPos ground) {
        BlockPos c = ground.above(12 + rand.nextInt(14));
        int rx = 3 + rand.nextInt(4);
        int depth = 3 + rand.nextInt(3);
        for (int dy = 0; dy >= -depth; dy--) {
            double r = rx * (1.0 - (double) -dy / (depth + 1));
            for (int dx = -rx; dx <= rx; dx++)
                for (int dz = -rx; dz <= rx; dz++) {
                    if (dx * dx + dz * dz > r * r) continue;
                    BlockPos p = c.offset(dx, dy, dz);
                    if (!level.getBlockState(p).isAir()) continue;
                    BlockState s = dy == 0 ? WOOLS[rand.nextInt(WOOLS.length)] : Blocks.CALCITE.defaultBlockState();
                    level.setBlock(p, s, 2);
                }
        }
        return true;
    }
}

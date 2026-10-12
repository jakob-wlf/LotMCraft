package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import static net.minecraft.world.level.levelgen.SurfaceRules.*;

public class SpiritPatchSurfaceRules {

    private static RuleSource block(Block b) { return state(b.defaultBlockState()); }

    public static RuleSource make() {
        ConditionSource onFloor = stoneDepthCheck(0, false, CaveSurface.FLOOR);
        ConditionSource underFloor = stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);

        RuleSource top = sequence(
                ifTrue(noiseCondition(Noises.SURFACE, -2.0, -0.30), block(Blocks.PINK_WOOL)),
                ifTrue(noiseCondition(Noises.SURFACE, -0.30, 0.10), block(Blocks.WHITE_WOOL)),
                ifTrue(noiseCondition(Noises.SURFACE, 0.10, 0.40), block(Blocks.LIGHT_BLUE_WOOL)),
                ifTrue(noiseCondition(Noises.CALCITE, 0.35, 2.0), block(Blocks.PURPLE_WOOL)),
                block(Blocks.MAGENTA_WOOL));

        return ifTrue(isBiome(SpiritPatchBiomes.SPIRIT_WORLD_PATCH), sequence(
                ifTrue(onFloor, top),
                ifTrue(underFloor, block(Blocks.CALCITE))));
    }
}

package de.jakob.lotm.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import de.jakob.lotm.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

public class CauldronReplacementProcessor extends StructureProcessor {
    public static final MapCodec<CauldronReplacementProcessor> CODEC =
            MapCodec.unit(() -> CauldronReplacementProcessor.INSTANCE);

    public static final CauldronReplacementProcessor INSTANCE = new CauldronReplacementProcessor();

    private final Random random = new Random();

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo processBlock(
            @NotNull LevelReader level,
            @NotNull BlockPos jigsawPiecePos,
            @NotNull BlockPos jigsawPieceBottomCenterPos,
            StructureTemplate.@NotNull StructureBlockInfo blockInfoLocal,
            StructureTemplate.StructureBlockInfo blockInfoGlobal,
            @NotNull StructurePlaceSettings placementSettings) {

        if (blockInfoGlobal.state().is(Blocks.CAULDRON)) {
            if (random.nextFloat() < 0.5f) {
                return new StructureTemplate.StructureBlockInfo(
                        blockInfoGlobal.pos(),
                        ModBlocks.BREWING_CAULDRON.get().defaultBlockState(),
                        blockInfoGlobal.nbt()
                );
            }
        }

        return blockInfoGlobal;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModProcessorTypes.CAULDRON_REPLACEMENT.get();
    }
}
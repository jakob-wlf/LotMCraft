package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class SpiritShrinePiece extends StructurePiece {

    private final BlockPos anchor;

    public SpiritShrinePiece(BlockPos anchor) {
        super(SpiritPatchRegistries.SHRINE_PIECE.get(), 0, SpiritShrineBlueprint.boundsFor(anchor));
        this.anchor = anchor;
    }

    public SpiritShrinePiece(CompoundTag tag) {
        super(SpiritPatchRegistries.SHRINE_PIECE.get(), tag);
        this.anchor = new BlockPos(tag.getInt("AX"), tag.getInt("AY"), tag.getInt("AZ"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("AX", anchor.getX());
        tag.putInt("AY", anchor.getY());
        tag.putInt("AZ", anchor.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        SpiritShrineBlueprint.placeWorldgen(level, anchor, chunkBox);
    }
}

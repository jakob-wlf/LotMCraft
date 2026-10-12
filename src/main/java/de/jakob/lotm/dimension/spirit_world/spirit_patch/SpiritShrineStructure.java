package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import com.mojang.serialization.MapCodec;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class SpiritShrineStructure extends Structure {

    public static final MapCodec<SpiritShrineStructure> CODEC = simpleCodec(SpiritShrineStructure::new);

    private static final int INTERIOR_RADIUS = 8;

    public SpiritShrineStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        int x = ctx.chunkPos().getMiddleBlockX();
        int z = ctx.chunkPos().getMiddleBlockZ();
        int y = ctx.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                ctx.heightAccessor(), ctx.randomState());

        if (y <= ctx.chunkGenerator().getSeaLevel()) return Optional.empty();

        // centre must be in the patch
        if (!isPatch(ctx, x, y - 1, z)) return Optional.empty();

        int r = INTERIOR_RADIUS;
        int d = (int) (r * 0.707);
        int[][] offs = {{r, 0}, {-r, 0}, {0, r}, {0, -r}, {d, d}, {-d, d}, {d, -d}, {-d, -d}};
        int hits = 0;
        for (int[] o : offs) {
            int ox = x + o[0], oz = z + o[1];
            int oy = ctx.chunkGenerator().getFirstOccupiedHeight(ox, oz, Heightmap.Types.WORLD_SURFACE_WG,
                    ctx.heightAccessor(), ctx.randomState());
            if (isPatch(ctx, ox, oy - 1, oz)) hits++;
        }
        if (hits < 6) return Optional.empty();   // allow up to 2 misses

        BlockPos anchor = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(anchor, builder -> builder.addPiece(new SpiritShrinePiece(anchor))));
    }

    private static boolean isPatch(GenerationContext ctx, int x, int y, int z) {
        return ctx.biomeSource().getNoiseBiome(
                QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z),
                ctx.randomState().sampler()).is(SpiritPatchBiomes.SPIRIT_WORLD_PATCH);
    }

    @Override
    public StructureType<?> type() {
        return SpiritPatchRegistries.SHRINE_TYPE.get();
    }
}

package de.jakob.lotm.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public class PairedStructure extends Structure {

    public static final MapCodec<PairedStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    StructureSettings.CODEC.forGetter(s -> s.modifiableStructureInfo().getOriginalStructureInfo().structureSettings()),

                    // Above-ground pool config
                    StructureTemplatePool.CODEC.fieldOf("above_start_pool").forGetter(s -> s.aboveStartPool),
                    ResourceLocation.CODEC.optionalFieldOf("above_start_jigsaw_name").forGetter(s -> s.aboveStartJigsawName),
                    Codec.intRange(0, 30).fieldOf("above_size").forGetter(s -> s.aboveSize),
                    HeightProvider.CODEC.fieldOf("above_start_height").forGetter(s -> s.aboveStartHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("above_project_start_to_heightmap").forGetter(s -> s.aboveProjectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("above_max_distance_from_center").forGetter(s -> s.aboveMaxDistanceFromCenter),
                    TerrainAdjustment.CODEC.optionalFieldOf("above_terrain_adaptation", TerrainAdjustment.NONE).forGetter(s -> s.aboveTerrainAdaptation),

                    // Below-ground pool config
                    StructureTemplatePool.CODEC.fieldOf("below_start_pool").forGetter(s -> s.belowStartPool),
                    ResourceLocation.CODEC.optionalFieldOf("below_start_jigsaw_name").forGetter(s -> s.belowStartJigsawName),
                    Codec.intRange(0, 30).fieldOf("below_size").forGetter(s -> s.belowSize),
                    HeightProvider.CODEC.fieldOf("below_start_height").forGetter(s -> s.belowStartHeight),
                    Codec.intRange(1, 128).fieldOf("below_max_distance_from_center").forGetter(s -> s.belowMaxDistanceFromCenter),
                    TerrainAdjustment.CODEC.optionalFieldOf("below_terrain_adaptation", TerrainAdjustment.NONE).forGetter(s -> s.belowTerrainAdaptation),

                    // Shared settings
                    DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(s -> s.dimensionPadding),
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(s -> s.liquidSettings)

            ).apply(instance, PairedStructure::new));

    // Above ground
    private final Holder<StructureTemplatePool> aboveStartPool;
    private final Optional<ResourceLocation> aboveStartJigsawName;
    private final int aboveSize;
    private final HeightProvider aboveStartHeight;
    private final Optional<Heightmap.Types> aboveProjectStartToHeightmap;
    private final int aboveMaxDistanceFromCenter;
    private final TerrainAdjustment aboveTerrainAdaptation;

    // Below ground
    private final Holder<StructureTemplatePool> belowStartPool;
    private final Optional<ResourceLocation> belowStartJigsawName;
    private final int belowSize;
    private final HeightProvider belowStartHeight;
    private final int belowMaxDistanceFromCenter;
    private final TerrainAdjustment belowTerrainAdaptation;

    // Shared
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    public PairedStructure(
            StructureSettings config,
            Holder<StructureTemplatePool> aboveStartPool,
            Optional<ResourceLocation> aboveStartJigsawName,
            int aboveSize,
            HeightProvider aboveStartHeight,
            Optional<Heightmap.Types> aboveProjectStartToHeightmap,
            int aboveMaxDistanceFromCenter,
            TerrainAdjustment aboveTerrainAdaptation,
            Holder<StructureTemplatePool> belowStartPool,
            Optional<ResourceLocation> belowStartJigsawName,
            int belowSize,
            HeightProvider belowStartHeight,
            int belowMaxDistanceFromCenter,
            TerrainAdjustment belowTerrainAdaptation,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings)
    {
        super(config);
        this.aboveStartPool = aboveStartPool;
        this.aboveStartJigsawName = aboveStartJigsawName;
        this.aboveSize = aboveSize;
        this.aboveStartHeight = aboveStartHeight;
        this.aboveProjectStartToHeightmap = aboveProjectStartToHeightmap;
        this.aboveMaxDistanceFromCenter = aboveMaxDistanceFromCenter;
        this.aboveTerrainAdaptation = aboveTerrainAdaptation;
        this.belowStartPool = belowStartPool;
        this.belowStartJigsawName = belowStartJigsawName;
        this.belowSize = belowSize;
        this.belowStartHeight = belowStartHeight;
        this.belowMaxDistanceFromCenter = belowMaxDistanceFromCenter;
        this.belowTerrainAdaptation = belowTerrainAdaptation;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    @Override
    public @NotNull TerrainAdjustment terrainAdaptation() {
        // The above-ground structure drives terrain adaptation for the pair.
        // The below-ground one typically wants NONE to avoid carving underground.
        return this.aboveTerrainAdaptation;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        WorldGenerationContext wgContext = new WorldGenerationContext(
                context.chunkGenerator(), context.heightAccessor());

        // --- Above-ground placement ---
        int aboveY = this.aboveStartHeight.sample(context.random(), wgContext);
        BlockPos abovePos = new BlockPos(chunkPos.getMinBlockX(), aboveY, chunkPos.getMinBlockZ());

        Optional<GenerationStub> aboveStub = JigsawPlacement.addPieces(
                context,
                this.aboveStartPool,
                this.aboveStartJigsawName,
                this.aboveSize,
                abovePos,
                false,
                this.aboveProjectStartToHeightmap,
                this.aboveMaxDistanceFromCenter,
                PoolAliasLookup.EMPTY,
                this.dimensionPadding,
                this.liquidSettings);

        // Bail out entirely if the above-ground structure couldn't place.
        // This means neither structure spawns, keeping them always in sync.
        if (aboveStub.isEmpty()) {
            return Optional.empty();
        }

        // --- Below-ground placement ---
        // Uses the same X/Z as above, but samples its own Y from below_start_height.
        // Set project_start_to_heightmap to empty in JSON for the below pool so it
        // doesn't get pushed up to surface level.
        int belowY = this.belowStartHeight.sample(context.random(), wgContext);
        BlockPos belowPos = new BlockPos(chunkPos.getMinBlockX(), belowY, chunkPos.getMinBlockZ());

        Optional<GenerationStub> belowStub = JigsawPlacement.addPieces(
                context,
                this.belowStartPool,
                this.belowStartJigsawName,
                this.belowSize,
                belowPos,
                false,
                Optional.empty(), // Never project below structure to heightmap
                this.belowMaxDistanceFromCenter,
                PoolAliasLookup.EMPTY,
                this.dimensionPadding,
                this.liquidSettings);

        // Merge the two piece lists into the above stub.
        // If below fails for some reason we still return above so it isn't wasted.
        if (belowStub.isPresent()) {
            GenerationStub above = aboveStub.get();
            GenerationStub below = belowStub.get();

            return Optional.of(new GenerationStub(above.position(), pieces -> {
                above.generator().left().ifPresent(gen -> gen.accept(pieces));
                below.generator().left().ifPresent(gen -> gen.accept(pieces));
            }));
        }

        return aboveStub;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.PAIRED_STRUCTURE.get(); // Register this in ModStructures
    }
}
package de.jakob.lotm.structure;

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
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A Jigsaw structure that spawns at a Y offset below the surface,
 * but applies terrain adaptation at the surface level (the "adaptation offset").
 *
 * Use this for large structures with deep foundations (e.g. a Victorian city
 * with 8-block dirt/grass basements) so the terrain blends naturally at
 * ground level rather than at the buried spawn point.
 */
public class CityStructure extends Structure {

    public static final MapCodec<CityStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    StructureSettings.CODEC.forGetter(s -> s.modifiableStructureInfo().getOriginalStructureInfo().structureSettings()),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(s -> s.startJigsawName),
                    Codec.intRange(0, 30).fieldOf("size").forGetter(s -> s.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
                    // How many blocks below the surface the structure actually spawns.
                    // E.g. 8 means the structure origin is 8 blocks underground, but
                    // terrain adaptation will still be calculated at surface level.
                    Codec.intRange(0, 64).fieldOf("foundation_depth").forGetter(s -> s.foundationDepth),
                    DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(s -> s.dimensionPadding),
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(s -> s.liquidSettings),
                    TerrainAdjustment.CODEC.optionalFieldOf("terrain_adaptation", TerrainAdjustment.NONE).forGetter(s -> s.terrainAdaptation)
            ).apply(instance, CityStructure::new));

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final int foundationDepth;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;
    private final TerrainAdjustment terrainAdaptation;

    public CityStructure(StructureSettings config,
                         Holder<StructureTemplatePool> startPool,
                         Optional<ResourceLocation> startJigsawName,
                         int size,
                         HeightProvider startHeight,
                         Optional<Heightmap.Types> projectStartToHeightmap,
                         int maxDistanceFromCenter,
                         int foundationDepth,
                         DimensionPadding dimensionPadding,
                         LiquidSettings liquidSettings,
                         TerrainAdjustment terrainAdaptation) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.foundationDepth = foundationDepth;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
        this.terrainAdaptation = terrainAdaptation;
    }

    @Override
    public @NotNull TerrainAdjustment terrainAdaptation() {
        return this.terrainAdaptation;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();

        // Calculate the surface Y using the heightmap + start_height as normal
        int surfaceY = this.startHeight.sample(
                context.random(),
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

        if (this.projectStartToHeightmap.isPresent()) {
            surfaceY += context.chunkGenerator().getFirstOccupiedHeight(
                    chunkPos.getMinBlockX(),
                    chunkPos.getMinBlockZ(),
                    this.projectStartToHeightmap.get(),
                    context.heightAccessor(),
                    context.randomState());
        }

        // The structure pieces themselves spawn foundationDepth blocks underground...
        BlockPos placementPos = new BlockPos(chunkPos.getMinBlockX(), surfaceY - this.foundationDepth, chunkPos.getMinBlockZ());

        // ...but we give JigsawPlacement the surface pos so terrain adaptation
        // carves/fills at ground level, not at the buried spawn point.
        // We achieve this by wrapping the context so that getFirstOccupiedHeight
        // reports the surface, while the actual blockPos is shifted down.
        //
        // Concretely: pass projectStartToHeightmap as empty (we already resolved Y above),
        // and shift blockPos down by foundationDepth. The Structure bounding box will
        // sit at placementPos (underground), but terrainAdaptation() returns the value
        // set in JSON so Minecraft uses the structure's *bounding box* top for blending —
        // which, for beard_thin/bury, already reaches up through your foundation to surface.
        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                this.startJigsawName,
                this.size,
                placementPos,
                false,
                // Pass empty here — we already applied heightmap offset manually above
                Optional.empty(),
                this.maxDistanceFromCenter,
                PoolAliasLookup.EMPTY,
                this.dimensionPadding,
                this.liquidSettings);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CITY_STRUCTURE.get();
    }
}
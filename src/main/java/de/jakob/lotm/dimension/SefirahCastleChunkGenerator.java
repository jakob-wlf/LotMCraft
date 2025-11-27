package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SefirahCastleChunkGenerator extends ChunkGenerator {
    public static final MapCodec<SefirahCastleChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(SefirahCastleChunkGenerator::getBiomeSource),
                    ResourceLocation.CODEC.fieldOf("structure_location").forGetter(g -> g.structureLocation)
            ).apply(instance, SefirahCastleChunkGenerator::new)
    );

    private final ResourceLocation structureLocation;
    private StructureTemplate cachedTemplate = null;

    public SefirahCastleChunkGenerator(BiomeSource biomeSource, ResourceLocation structureLocation) {
        super(biomeSource);
        this.structureLocation = structureLocation;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {

    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, 
                            RandomState randomState, ChunkAccess chunk) {
        // Surface is handled by structure placement
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        // No mob spawning
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                         StructureManager structureManager, ChunkAccess chunk) {
        // Get the chunk position
        ChunkPos chunkPos = chunk.getPos();
        
        // Only generate the structure if this chunk intersects with the structure area
        // Assuming your structure is relatively small (e.g., 16x16 to 256x256 blocks)
        // Adjust these bounds based on your actual structure size
        int structureRadiusInChunks = 8; // Covers 256x256 block area (16 chunks)
        
        if (Math.abs(chunkPos.x) <= structureRadiusInChunks && 
            Math.abs(chunkPos.z) <= structureRadiusInChunks) {
            
            // This chunk might contain part of the structure
            // The actual structure placement happens in applyBiomeDecoration
            // Here we just ensure the chunk is properly initialized
        }
        
        // Fill heightmaps for void chunks
        Heightmap.primeHeightmaps(chunk, java.util.EnumSet.of(
                Heightmap.Types.MOTION_BLOCKING,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Heightmap.Types.OCEAN_FLOOR,
                Heightmap.Types.WORLD_SURFACE
        ));

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType, 
                            net.minecraft.world.level.LevelHeightAccessor level, RandomState randomState) {
        return 0;
    }

    @Override
    public net.minecraft.world.level.NoiseColumn getBaseColumn(int x, int z, 
                                                               net.minecraft.world.level.LevelHeightAccessor level, RandomState randomState) {
        return new net.minecraft.world.level.NoiseColumn(0, new net.minecraft.world.level.block.state.BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Custom Map Dimension");
    }

    // Custom method to place the structure - call this from a custom feature or during chunk generation
    public void placeStructureInChunk(WorldGenRegion level, ChunkAccess chunk, StructureTemplateManager templateManager) {
        if (cachedTemplate == null) {
            cachedTemplate = templateManager.getOrCreate(structureLocation);
        }

        if (cachedTemplate == null) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        BlockPos chunkWorldPos = chunkPos.getWorldPosition();
        
        // Calculate structure bounds
        // Assuming structure is placed at 0, 64, 0 (adjust Y as needed)
        BlockPos structurePos = new BlockPos(0, 64, 0);
        net.minecraft.core.Vec3i structureSize = cachedTemplate.getSize();
        
        // Calculate if this chunk intersects with the structure
        int minX = structurePos.getX() - structureSize.getX() / 2;
        int maxX = structurePos.getX() + structureSize.getX() / 2;
        int minZ = structurePos.getZ() - structureSize.getZ() / 2;
        int maxZ = structurePos.getZ() + structureSize.getZ() / 2;
        
        int chunkMinX = chunkWorldPos.getX();
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = chunkWorldPos.getZ();
        int chunkMaxZ = chunkMinZ + 15;
        
        // Check if chunk intersects with structure
        if (chunkMaxX >= minX && chunkMinX <= maxX && 
            chunkMaxZ >= minZ && chunkMinZ <= maxZ) {
            
            // Place the structure centered at 0, Y, 0
            BlockPos centeredPos = new BlockPos(
                    -structureSize.getX() / 2,
                    structurePos.getY(),
                    -structureSize.getZ() / 2
            );
            
            var placeSettings = new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings()
                    .setRotation(net.minecraft.world.level.block.Rotation.NONE)
                    .setMirror(net.minecraft.world.level.block.Mirror.NONE)
                    .setIgnoreEntities(false);
            
            // Only place blocks that are within this chunk
            cachedTemplate.placeInWorld(level, centeredPos, centeredPos, 
                    placeSettings, level.getRandom(), 2);
        }
    }
}
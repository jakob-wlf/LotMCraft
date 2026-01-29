package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class PreGeneratedChunkGenerator extends ChunkGenerator {
    public static final MapCodec<PreGeneratedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, PreGeneratedChunkGenerator::new)
    );

    private final Map<ChunkPos, CompoundTag> chunkCache = new HashMap<>();
    private static final String REGION_PATH = "data/lotmcraft/dimension_data/sefirah_castle/";

    public PreGeneratedChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState,
                             net.minecraft.world.level.biome.BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk,
                             GenerationStep.Carving carving) {
        // No carving needed
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
                             RandomState randomState, ChunkAccess chunk) {
        // Surface already built
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        // No mob spawning
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(() -> {
            loadPreGeneratedChunk(chunk);
            return chunk;
        });
    }

    private void loadPreGeneratedChunk(ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        CompoundTag chunkData = getChunkData(pos);

        if (chunkData != null && chunkData.contains("sections")) {
            try {
                loadChunkSections(chunk, chunkData);
            } catch (Exception e) {
                e.printStackTrace();
                fillWithDefaultTerrain(chunk);
            }
        } else {
            // If no pre-generated data exists, create default terrain for testing
            fillWithDefaultTerrain(chunk);
        }

        // Update heightmaps
        Heightmap.primeHeightmaps(chunk, java.util.EnumSet.of(
                Heightmap.Types.MOTION_BLOCKING,
                Heightmap.Types.WORLD_SURFACE,
                Heightmap.Types.OCEAN_FLOOR
        ));
    }

    private CompoundTag getChunkData(ChunkPos pos) {
        if (chunkCache.containsKey(pos)) {
            return chunkCache.get(pos);
        }

        int regionX = pos.x >> 5;
        int regionZ = pos.z >> 5;
        String regionFileName = "r." + regionX + "." + regionZ + ".mca";

        try {
            InputStream stream = getClass().getClassLoader()
                    .getResourceAsStream(REGION_PATH + regionFileName);

            if (stream != null) {
                CompoundTag data = loadChunkFromRegion(stream, pos.x & 31, pos.z & 31);
                if (data != null) {
                    chunkCache.put(pos, data);
                    return data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private CompoundTag loadChunkFromRegion(InputStream regionStream, int localX, int localZ)
            throws IOException {
        try {
            byte[] regionData = regionStream.readAllBytes();

            // MCA file format: 8KB header (4096 bytes locations + 4096 bytes timestamps)
            if (regionData.length < 8192) {
                return null;
            }

            // Calculate offset in header (each chunk has 4 bytes)
            int headerOffset = 4 * ((localX & 31) + (localZ & 31) * 32);

            // Read location data (3 bytes offset + 1 byte sector count)
            int offset = ((regionData[headerOffset] & 0xFF) << 16) |
                    ((regionData[headerOffset + 1] & 0xFF) << 8) |
                    (regionData[headerOffset + 2] & 0xFF);

            if (offset == 0) {
                return null; // Chunk doesn't exist
            }

            // Convert offset from sectors to bytes (1 sector = 4096 bytes)
            int byteOffset = offset * 4096;

            if (byteOffset + 5 > regionData.length) {
                return null;
            }

            // Read chunk length (first 4 bytes at offset)
            int length = ((regionData[byteOffset] & 0xFF) << 24) |
                    ((regionData[byteOffset + 1] & 0xFF) << 16) |
                    ((regionData[byteOffset + 2] & 0xFF) << 8) |
                    (regionData[byteOffset + 3] & 0xFF);

            // Read compression type (1 = GZip, 2 = Zlib)
            int compressionType = regionData[byteOffset + 4] & 0xFF;

            // Read compressed data
            byte[] compressedData = new byte[length - 1];
            System.arraycopy(regionData, byteOffset + 5, compressedData, 0, length - 1);

            // Decompress based on type
            InputStream decompressed;
            if (compressionType == 1) {
                decompressed = new GZIPInputStream(new ByteArrayInputStream(compressedData));
            } else if (compressionType == 2) {
                decompressed = new InflaterInputStream(new ByteArrayInputStream(compressedData));
            } else {
                return null;
            }

            // Read NBT data
            return NbtIo.read(new DataInputStream(decompressed));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadChunkSections(ChunkAccess chunk, CompoundTag chunkData) {
        // Get the Level tag which contains the actual chunk data
        CompoundTag level = chunkData.contains("Level") ? chunkData.getCompound("Level") : chunkData;

        ListTag sections = level.getList("sections", 10); // 10 = CompoundTag type

        for (int i = 0; i < sections.size(); i++) {
            CompoundTag section = sections.getCompound(i);
            byte sectionY = section.getByte("Y");

            if (!section.contains("block_states")) {
                continue;
            }

            CompoundTag blockStates = section.getCompound("block_states");

            // Load palette
            ListTag palette = blockStates.getList("palette", 10);
            BlockState[] paletteArray = new BlockState[palette.size()];

            for (int j = 0; j < palette.size(); j++) {
                CompoundTag blockTag = palette.getCompound(j);
                String blockName = blockTag.getString("Name");

                // Parse block state from name
                BlockState state = parseBlockState(blockName, blockTag);
                paletteArray[j] = state != null ? state : Blocks.AIR.defaultBlockState();
            }

            // Load block data
            if (blockStates.contains("data")) {
                long[] data = blockStates.getLongArray("data");

                // Calculate bits per block - Minecraft uses actual palette size, not rounded up
                int bitsPerBlock = Math.max(4, 32 - Integer.numberOfLeadingZeros(paletteArray.length - 1));

                // Decode packed data and place blocks
                int sectionYOffset = sectionY * 16;

                // Minecraft stores blocks in YZX order (Y changes slowest, X changes fastest)
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            int blockIndex = (y * 16 + z) * 16 + x; // YZX order
                            int paletteIndex = extractPaletteIndex(data, blockIndex, bitsPerBlock);

                            if (paletteIndex >= 0 && paletteIndex < paletteArray.length) {
                                BlockState state = paletteArray[paletteIndex];
                                chunk.setBlockState(new BlockPos(x, sectionYOffset + y, z), state, false);
                            }
                        }
                    }
                }
            } else if (paletteArray.length == 1) {
                // Single block type in section
                BlockState state = paletteArray[0];
                int sectionYOffset = sectionY * 16;

                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            chunk.setBlockState(new BlockPos(x, sectionYOffset + y, z), state, false);
                        }
                    }
                }
            }
        }
    }

    private BlockState parseBlockState(String blockName, CompoundTag blockTag) {
        try {
            // Parse the block name into a ResourceLocation
            net.minecraft.resources.ResourceLocation blockId =
                    net.minecraft.resources.ResourceLocation.parse(blockName);

            // Get block from registry
            net.minecraft.world.level.block.Block block =
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(blockId);

            if (block != null) {
                BlockState state = block.defaultBlockState();

                // Parse properties if they exist
                if (blockTag.contains("Properties")) {
                    CompoundTag properties = blockTag.getCompound("Properties");

                    for (String propertyName : properties.getAllKeys()) {
                        String propertyValue = properties.getString(propertyName);

                        // Apply property to state
                        for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
                            if (property.getName().equals(propertyName)) {
                                state = setPropertyValue(state, property, propertyValue);
                                break;
                            }
                        }
                    }
                }

                return state;
            }
        } catch (Exception e) {
            // Log but don't crash - just return air for invalid blocks
            System.err.println("Failed to parse block state: " + blockName + " - " + e.getMessage());
        }
        return Blocks.AIR.defaultBlockState();
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> BlockState setPropertyValue(
            BlockState state,
            net.minecraft.world.level.block.state.properties.Property<T> property,
            String value) {
        try {
            java.util.Optional<T> parsedValue = property.getValue(value);
            if (parsedValue.isPresent()) {
                return state.setValue(property, parsedValue.get());
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return state;
    }

    private int extractPaletteIndex(long[] data, int blockIndex, int bitsPerBlock) {
        // Calculate which long contains our data
        long blocksPerLong = 64 / bitsPerBlock;
        int longIndex = (int) (blockIndex / blocksPerLong);
        int localIndex = (int) (blockIndex % blocksPerLong);

        if (longIndex >= data.length) {
            return 0;
        }

        // Calculate bit offset within the long
        int bitOffset = localIndex * bitsPerBlock;

        // Create mask for extracting the value
        long mask = (1L << bitsPerBlock) - 1;

        // Extract and return the value
        return (int) ((data[longIndex] >>> bitOffset) & mask);
    }

    private int ceilLog2(int value) {
        return value > 1 ? 32 - Integer.numberOfLeadingZeros(value - 1) : 0;
    }

    private void fillWithDefaultTerrain(ChunkAccess chunk) {
        // Create a simple test structure so you can see something
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Create a flat world at y=64 for testing
                for (int y = chunk.getMinBuildHeight(); y < 60; y++) {
                    chunk.setBlockState(new BlockPos(x, y, z), stone, false);
                }
                for (int y = 60; y < 63; y++) {
                    chunk.setBlockState(new BlockPos(x, y, z), dirt, false);
                }
                chunk.setBlockState(new BlockPos(x, 63, z), grass, false);
            }
        }
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType,
                             LevelHeightAccessor level, RandomState randomState) {
        return 64; // Return expected ground level
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        BlockState[] column = new BlockState[level.getHeight()];
        for (int i = 0; i < column.length; i++) {
            column[i] = Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(level.getMinBuildHeight(), column);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("PreGenerated: Sefirah Castle");
        info.add("Chunk Cache Size: " + chunkCache.size());
    }
}
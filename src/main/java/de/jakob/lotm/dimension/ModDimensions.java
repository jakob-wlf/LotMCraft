package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDimensions {

    // -------------------------------------------------------------------------
    // Chunk Generator Registry
    // -------------------------------------------------------------------------

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, LOTMCraft.MOD_ID);

    public static final Supplier<MapCodec<EmptyChunkGenerator>> EMPTY_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("empty", () -> EmptyChunkGenerator.CODEC);

    public static final Supplier<MapCodec<SpiritWorldChunkGenerator>> SPIRIT_WORLD_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("spirit_world", () -> SpiritWorldChunkGenerator.CODEC);

    public static final Supplier<MapCodec<PreGeneratedChunkGenerator>> PREGENERATED_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("pregenerated", () -> PreGeneratedChunkGenerator.CODEC);

    public static final Supplier<MapCodec<ConcealmentWorldChunkGenerator>> CONCEALMENT_WORLD_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("concealment_world", () -> ConcealmentWorldChunkGenerator.CODEC);

    public static final Supplier<MapCodec<NatureDimensionWorldChunkGenerator>> NATURE_WORLD_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("nature", () -> NatureDimensionWorldChunkGenerator.CODEC);

    // -------------------------------------------------------------------------
    // BiomeSource Registry
    // -------------------------------------------------------------------------

    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, LOTMCraft.MOD_ID);

    public static final Supplier<MapCodec<SpiritWorldBiomeSource>> SPIRIT_WORLD_BIOME_SOURCE =
            BIOME_SOURCES.register("spirit_world_biome_source", () -> SpiritWorldBiomeSource.CODEC);

    // =========================================================================
    // SPACE dimension
    // =========================================================================

    public static final ResourceKey<LevelStem> SPACE_LEVEL_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"));

    public static final ResourceKey<DimensionType> SPACE_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"));

    public static final ResourceKey<Biome> SPACE_BIOME_KEY =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space_biome"));

    // =========================================================================
    // NATURE / WORLD CREATION dimension
    // =========================================================================

    public static final ResourceKey<LevelStem> WORLD_CREATION_LEVEL_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "nature"));

    public static final ResourceKey<DimensionType> WORLD_CREATION_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "nature"));

    public static final ResourceKey<Biome> WORLD_CREATION_BIOME_KEY =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "nature_biome"));

    // =========================================================================
    // SPIRIT WORLD dimension
    // =========================================================================

    public static final ResourceKey<LevelStem> SPIRIT_WORLD_LEVEL_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));

    public static final ResourceKey<Level> SPIRIT_WORLD_DIMENSION_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));

    public static final ResourceKey<DimensionType> SPIRIT_WORLD_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));

    // --- Spirit World per-biome keys ---
    // Index order MUST match SpiritWorldBiomeSource.BIOME_ORDER and
    // the SPIRIT_WORLD_BIOME_KEYS convenience array below.

    /** index 0 */
    public static final ResourceKey<Biome> SPIRIT_BIOME_WOOL_MEADOWS =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_wool_meadows"));

    /** index 1 */
    public static final ResourceKey<Biome> SPIRIT_BIOME_CRYSTALLINE_PEAKS =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_crystalline_peaks"));

    /** index 2 */
    public static final ResourceKey<Biome> SPIRIT_BIOME_VOID_GARDENS =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_void_gardens"));

    /** index 3 */
    public static final ResourceKey<Biome> SPIRIT_BIOME_EMBER_WASTES =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_ember_wastes"));

    /** index 4 */
    public static final ResourceKey<Biome> SPIRIT_BIOME_QUARTZ_FLATS =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_quartz_flats"));

    /** index 5 */
    public static final ResourceKey<Biome> SPIRIT_BIOME_TERRACOTTA_CANYON =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_terracotta_canyon"));

    /**
     * Convenience array — same order as {@link SpiritWorldBiome#values()} and
     * {@link SpiritWorldBiomeSource#BIOME_ORDER}.
     */
    @SuppressWarnings("unchecked")
    public static final ResourceKey<Biome>[] SPIRIT_WORLD_BIOME_KEYS = new ResourceKey[]{
            SPIRIT_BIOME_WOOL_MEADOWS,
            SPIRIT_BIOME_CRYSTALLINE_PEAKS,
            SPIRIT_BIOME_VOID_GARDENS,
            SPIRIT_BIOME_EMBER_WASTES,
            SPIRIT_BIOME_QUARTZ_FLATS,
            SPIRIT_BIOME_TERRACOTTA_CANYON,
    };

    // =========================================================================
    // SEFIRAH CASTLE dimension
    // =========================================================================

    public static final ResourceKey<LevelStem> SEFIRAH_CASTLE_LEVEL_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));

    public static final ResourceKey<Level> SEFIRAH_CASTLE_DIMENSION_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));

    public static final ResourceKey<DimensionType> SEFIRAH_CASTLE_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));

    public static final ResourceKey<Biome> SEFIRAH_CASTLE_BIOME_KEY =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle_biome"));

    // =========================================================================
    // CONCEALMENT WORLD dimension
    // =========================================================================

    public static final ResourceKey<LevelStem> CONCEALMENT_WORLD_LEVEL_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "concealment_world"));

    public static final ResourceKey<Level> CONCEALMENT_WORLD_DIMENSION_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "concealment_world"));

    public static final ResourceKey<DimensionType> CONCEALMENT_WORLD_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "concealment_world"));

    public static final ResourceKey<Biome> CONCEALMENT_WORLD_BIOME_KEY =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "concealment_world_biome"));

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    public static void register(IEventBus eventBus) {
        CHUNK_GENERATORS.register(eventBus);
        BIOME_SOURCES.register(eventBus);
    }
}
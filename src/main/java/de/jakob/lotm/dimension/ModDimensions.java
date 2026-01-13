package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDimensions {

    // Chunk Generator Registry
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, LOTMCraft.MOD_ID);

    public static final Supplier<MapCodec<EmptyChunkGenerator>> EMPTY_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("empty", () -> EmptyChunkGenerator.CODEC);

    public static final Supplier<MapCodec<SpiritWorldChunkGenerator>> SPIRIT_WORLD_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("spirit_world", () -> SpiritWorldChunkGenerator.CODEC);

    public static final Supplier<MapCodec<PreGeneratedChunkGenerator>> PREGENERATED_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("pregenerated", () -> PreGeneratedChunkGenerator.CODEC);



    // Dimension Keys
    public static final ResourceKey<LevelStem> SPACE_LEVEL_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"));

    public static final ResourceKey<DimensionType> SPACE_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"));

    public static final ResourceKey<LevelStem> SPIRIT_WORLD_LEVEL_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));

    public static final ResourceKey<Level> SPIRIT_WORLD_DIMENSION_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));

    public static final ResourceKey<DimensionType> SPIRIT_WORLD_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));

    // Biome Key
    public static final ResourceKey<Biome> SPACE_BIOME_KEY =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space_biome"));

    public static final ResourceKey<Biome> SPIRIT_WORLD_BIOME_KEY =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world_biome"));

    public static final ResourceKey<LevelStem> SEFIRAH_CASTLE_LEVEL_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));

    public static final ResourceKey<DimensionType> SEFIRAH_CASTLE_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));
    public static final ResourceKey<Biome> SEFIRAH_CASTLE_BIOME_KEY =
            ResourceKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle_biome"));

    public static final ResourceKey<Level> SEFIRAH_CASTLE_DIMENSION_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));

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

    public static final Supplier<MapCodec<ConcealmentWorldChunkGenerator>> CONCEALMENT_WORLD_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("concealment_world", () -> ConcealmentWorldChunkGenerator.CODEC);

    public static void register(IEventBus eventBus) {
        CHUNK_GENERATORS.register(eventBus);
    }
}
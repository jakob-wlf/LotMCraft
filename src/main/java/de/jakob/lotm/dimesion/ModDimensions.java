package de.jakob.lotm.dimesion;

import com.mojang.serialization.MapCodec;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
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

    // Dimension Keys
    public static final ResourceKey<LevelStem> SPACE_LEVEL_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"));

    public static final ResourceKey<Level> SPACE_DIMENSION_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"));

    public static final ResourceKey<DimensionType> SPACE_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"));

    public static void register(IEventBus eventBus) {
        CHUNK_GENERATORS.register(eventBus);
    }
}
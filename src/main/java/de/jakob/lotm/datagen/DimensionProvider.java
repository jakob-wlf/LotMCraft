package de.jakob.lotm.datagen;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.EmptyChunkGenerator;
import de.jakob.lotm.dimension.ModDimensions;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.OptionalLong;
import java.util.Set;

public class DimensionProvider {

    public static void addDimensionProvider(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(
                        packOutput,
                        lookupProvider,
                        new RegistrySetBuilder()
                                .add(Registries.DIMENSION_TYPE, bootstrap -> {
                                    bootstrap.register(ModDimensions.SPACE_TYPE_KEY, new DimensionType(
                                            OptionalLong.empty(), // fixed time (no day/night cycle)
                                            false, // has skylight
                                            false, // has ceiling
                                            false, // ultrawarm
                                            false, // natural
                                            1.0, // coordinate scale
                                            true, // bed works
                                            false, // respawn anchor works
                                            -64, // min y
                                            384, // height
                                            384, // logical height
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"), // effects location
                                            0.0f, // ambient light level
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)
                                    ));
                                })
                                .add(Registries.LEVEL_STEM, bootstrap -> {
                                    var biomeRegistry = bootstrap.lookup(Registries.BIOME);
                                    var dimensionTypes = bootstrap.lookup(Registries.DIMENSION_TYPE);

                                    // Use THE_VOID biome for a completely empty dimension
                                    var biomeSource = new FixedBiomeSource(
                                            biomeRegistry.getOrThrow(Biomes.THE_VOID)
                                    );

                                    var chunkGenerator = new EmptyChunkGenerator(biomeSource);

                                    bootstrap.register(ModDimensions.SPACE_LEVEL_KEY,
                                            new LevelStem(dimensionTypes.getOrThrow(ModDimensions.SPACE_TYPE_KEY), chunkGenerator)
                                    );
                                }),
                        Set.of(LOTMCraft.MOD_ID)
                )
        );
    }

}

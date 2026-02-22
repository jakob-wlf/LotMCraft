package de.jakob.lotm.datagen;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.*;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.joml.Vector3f;

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
                                // Register the custom biome
                                .add(Registries.BIOME, bootstrap -> {
                                    bootstrap.register(ModDimensions.SPACE_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.0f)
                                                    .downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x000000) // Very dark sky
                                                            .fogColor(0x000000) // Dark fog
                                                            .waterColor(0x1b5ee3)
                                                            .waterFogColor(0x050533)
                                                            .grassColorOverride(0x4ad145) // Jungle grass color
                                                            .foliageColorOverride(0x30BB00) // Dense jungle foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build()
                                    );
                                    bootstrap.register(ModDimensions.WORLD_CREATION_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.0f)
                                                    .downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xcafa96) // Very dark sky
                                                            .fogColor(0x000000) // Dark fog
                                                            .waterColor(0x1b5ee3)
                                                            .waterFogColor(0x050533)
                                                            .grassColorOverride(0x4ad145) // Jungle grass color
                                                            .foliageColorOverride(0x30BB00) // Dense jungle foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build()
                                    );
                                    bootstrap.register(ModDimensions.SEFIRAH_CASTLE_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false) // No rain/snow
                                                    .temperature(0.5f)
                                                    .downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x808080) // Gray sky
                                                            .fogColor(0x808080) // Gray fog
                                                            .waterColor(0x3f76e4)
                                                            .waterFogColor(0x050533)
                                                            .grassColorOverride(0x79c05a)
                                                            .foliageColorOverride(0x59ae30)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build()
                                    );
                                    // Second biome - Void (example)
                                    bootstrap.register(ModDimensions.SPIRIT_WORLD_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.5f)
                                                    .downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xFF00FF) // Starting magenta - will cycle
                                                            .fogColor(0x00FFFF) // Starting cyan - will cycle
                                                            .waterColor(0xFF1493) // Deep pink water
                                                            .waterFogColor(0x9400D3) // Purple water fog
                                                            .grassColorOverride(0x00FF00) // Bright green grass
                                                            .foliageColorOverride(0xFFD700) // Gold foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(new Vector3f(255, 255, 255), 5),
                                                                    0.005f
                                                            ))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build()
                                    );
                                    bootstrap.register(ModDimensions.CONCEALMENT_WORLD_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false) // No weather
                                                    .temperature(0.5f)
                                                    .downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x0A0A14) // Dark blue-purple like End
                                                            .fogColor(0x0A0A14) // Same as sky for End-like effect
                                                            .waterColor(0x3f76e4)
                                                            .waterFogColor(0x050533)
                                                            .grassColorOverride(0x79c05a) // Normal grass color
                                                            .foliageColorOverride(0x59ae30)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build()
                                    );

                                })
                                .add(Registries.DIMENSION_TYPE, bootstrap -> {
                                    bootstrap.register(ModDimensions.SPACE_TYPE_KEY, new DimensionType(
                                            OptionalLong.empty(), // fixed time (no day/night cycle)
                                            true, // has skylight
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
                                            1.0f, // ambient light level
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)
                                    ));
                                    bootstrap.register(ModDimensions.WORLD_CREATION_TYPE_KEY, new DimensionType(
                                            OptionalLong.empty(), // fixed time (no day/night cycle)
                                            true, // has skylight
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
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "nature"), // effects location
                                            1.0f, // ambient light level
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)
                                    ));
                                    bootstrap.register(ModDimensions.SEFIRAH_CASTLE_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(6000), // Fixed time (noon, no day/night cycle)
                                            false, // NO skylight - disables sun/moon rendering
                                            true, // Has ceiling (helps disable sky rendering)
                                            false, // Not ultrawarm
                                            false, // Not natural (disables weather)
                                            1.0, // Normal coordinate scale
                                            false, // Beds don't work
                                            false, // Respawn anchors don't work
                                            -64, // min y
                                            384, // height
                                            384, // logical height
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"),
                                            1.0f, // Full ambient light (since no skylight)
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)
                                    ));
                                    // Second dimension type - Void
                                    bootstrap.register(ModDimensions.SPIRIT_WORLD_TYPE_KEY, new DimensionType(
                                            OptionalLong.empty(), // No fixed time - no day/night cycle
                                            true, // Has skylight - always bright
                                            false, // No ceiling
                                            false, // Not ultrawarm
                                            false, // Not natural
                                            1.0, // Normal coordinate scale
                                            false, // Beds don't work - too disorienting
                                            false, // Respawn anchors don't work
                                            0, // min y - centered around 0 for disorientation
                                            256, // height
                                            256, // logical height
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"),
                                            1.0f, // FULL ambient light - always bright everywhere
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0) // No mob spawning for now
                                    ));
                                    bootstrap.register(ModDimensions.CONCEALMENT_WORLD_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(6000), // Fixed time (noon, no day/night cycle)
                                            true, // Has skylight - but with fixed time it won't change
                                            false, // No ceiling
                                            false, // Not ultrawarm
                                            false, // Not natural (disables weather)
                                            1.0, // Normal coordinate scale
                                            true, // Beds work
                                            false, // Respawn anchors don't work
                                            -64, // min y
                                            384, // height
                                            384, // logical height
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "concealment_world"),
                                            1.0f, // Full ambient light everywhere
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0) // No mob spawning
                                    ));
                                })
                                .add(Registries.LEVEL_STEM, bootstrap -> {
                                    var biomeRegistry = bootstrap.lookup(Registries.BIOME);
                                    var dimensionTypes = bootstrap.lookup(Registries.DIMENSION_TYPE);

                                    // Use your custom biome
                                    var biomeSource = new FixedBiomeSource(
                                            biomeRegistry.getOrThrow(ModDimensions.SPACE_BIOME_KEY)
                                    );

                                    var chunkGenerator = new EmptyChunkGenerator(biomeSource);

                                    bootstrap.register(ModDimensions.SPACE_LEVEL_KEY,
                                            new LevelStem(dimensionTypes.getOrThrow(ModDimensions.SPACE_TYPE_KEY), chunkGenerator)
                                    );

                                    var natureBiomeSource = new FixedBiomeSource(
                                            biomeRegistry.getOrThrow(ModDimensions.WORLD_CREATION_BIOME_KEY)
                                    );

                                    var natureChunkGenerator = new NatureDimensionWorldChunkGenerator(natureBiomeSource);

                                    bootstrap.register(ModDimensions.WORLD_CREATION_LEVEL_KEY,
                                            new LevelStem(dimensionTypes.getOrThrow(ModDimensions.WORLD_CREATION_TYPE_KEY), natureChunkGenerator)
                                    );

                                    var spiritBiomeSource = new FixedBiomeSource(
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_WORLD_BIOME_KEY)
                                    );
                                    var spiritChunkGenerator = new SpiritWorldChunkGenerator(spiritBiomeSource);
                                    bootstrap.register(ModDimensions.SPIRIT_WORLD_LEVEL_KEY,
                                            new LevelStem(dimensionTypes.getOrThrow(ModDimensions.SPIRIT_WORLD_TYPE_KEY), spiritChunkGenerator)
                                    );
                                    var sefirahCastleBiomeSource = new FixedBiomeSource(
                                            biomeRegistry.getOrThrow(ModDimensions.SEFIRAH_CASTLE_BIOME_KEY)
                                    );

                                    var sefirahCastleGenerator = new PreGeneratedChunkGenerator(sefirahCastleBiomeSource);

                                    bootstrap.register(ModDimensions.SEFIRAH_CASTLE_LEVEL_KEY,
                                            new LevelStem(dimensionTypes.getOrThrow(ModDimensions.SEFIRAH_CASTLE_TYPE_KEY),
                                                    sefirahCastleGenerator)
                                    );

                                    var concealmentBiomeSource = new FixedBiomeSource(
                                            biomeRegistry.getOrThrow(ModDimensions.CONCEALMENT_WORLD_BIOME_KEY)
                                    );

                                    var concealmentChunkGenerator = new ConcealmentWorldChunkGenerator(concealmentBiomeSource);

                                    bootstrap.register(ModDimensions.CONCEALMENT_WORLD_LEVEL_KEY,
                                            new LevelStem(dimensionTypes.getOrThrow(ModDimensions.CONCEALMENT_WORLD_TYPE_KEY),
                                                    concealmentChunkGenerator)
                                    );
                                }),
                        Set.of(LOTMCraft.MOD_ID)
                )
        );
    }

}

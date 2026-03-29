package de.jakob.lotm.datagen;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.*;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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

import java.util.List;
import java.util.OptionalLong;
import java.util.Set;

public class DimensionProvider {

    public static void addDimensionProvider(GatherDataEvent event) {
        var generator     = event.getGenerator();
        var packOutput    = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(
                        packOutput,
                        lookupProvider,
                        new RegistrySetBuilder()

                                // =============================================================
                                // BIOME REGISTRY
                                // =============================================================
                                .add(Registries.BIOME, bootstrap -> {

                                    // ---------------------------------------------------------
                                    // Non-Spirit-World biomes
                                    // ---------------------------------------------------------

                                    bootstrap.register(ModDimensions.SPACE_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.0f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x000000)
                                                            .fogColor(0x000000)
                                                            .waterColor(0x1b5ee3)
                                                            .waterFogColor(0x050533)
                                                            .grassColorOverride(0x4ad145)
                                                            .foliageColorOverride(0x30BB00)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.WORLD_CREATION_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.0f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xcafa96)
                                                            .fogColor(0x000000)
                                                            .waterColor(0x1b5ee3)
                                                            .waterFogColor(0x050533)
                                                            .grassColorOverride(0x4ad145)
                                                            .foliageColorOverride(0x30BB00)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.SEFIRAH_CASTLE_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x808080)
                                                            .fogColor(0x808080)
                                                            .waterColor(0x3f76e4)
                                                            .waterFogColor(0x050533)
                                                            .grassColorOverride(0x79c05a)
                                                            .foliageColorOverride(0x59ae30)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.CONCEALMENT_WORLD_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x0A0A14)
                                                            .fogColor(0x0A0A14)
                                                            .waterColor(0x3f76e4)
                                                            .waterFogColor(0x050533)
                                                            .grassColorOverride(0x79c05a)
                                                            .foliageColorOverride(0x59ae30)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // ---------------------------------------------------------
                                    // Spirit World – 9 distinct biomes
                                    // Index order MUST match ModDimensions.SPIRIT_WORLD_BIOME_KEYS
                                    // and SpiritWorldBiomeSource.BIOME_ORDER.
                                    // ---------------------------------------------------------

                                    // 0 – WOOL_MEADOWS
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_WOOL_MEADOWS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.8f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xFF99DD)
                                                            .fogColor(0xFF55BB)
                                                            .waterColor(0xFF69B4)
                                                            .waterFogColor(0xAA1177)
                                                            .grassColorOverride(0x55FF88)
                                                            .foliageColorOverride(0xFFDD00)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(1.0f, 0.4f, 0.9f), 1.2f),
                                                                    0.004f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 1 – CRYSTALLINE_PEAKS
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_CRYSTALLINE_PEAKS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(-0.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x050520)
                                                            .fogColor(0x0033AA)
                                                            .waterColor(0x00EEFF)
                                                            .waterFogColor(0x002266)
                                                            .grassColorOverride(0xAAEEFF)
                                                            .foliageColorOverride(0x55BBFF)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.END_ROD,
                                                                    0.003f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 2 – VOID_GARDENS
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_VOID_GARDENS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x220044)
                                                            .fogColor(0x550077)
                                                            .waterColor(0xCC44FF)
                                                            .waterFogColor(0x330055)
                                                            .grassColorOverride(0xDD88FF)
                                                            .foliageColorOverride(0xFF99EE)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.PORTAL,
                                                                    0.002f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 3 – EMBER_WASTES
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_EMBER_WASTES,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(2.0f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x1A0500)
                                                            .fogColor(0x882200)
                                                            .waterColor(0xFF4400)
                                                            .waterFogColor(0x660000)
                                                            .grassColorOverride(0x993300)
                                                            .foliageColorOverride(0xCC4400)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.LAVA,
                                                                    0.001f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 4 – QUARTZ_FLATS
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_QUARTZ_FLATS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(1.0f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xFFF5CC)
                                                            .fogColor(0xFFEEAA)
                                                            .waterColor(0xEEDDAA)
                                                            .waterFogColor(0xBBAA66)
                                                            .grassColorOverride(0xEEFFCC)
                                                            .foliageColorOverride(0xDDEE99)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(1.0f, 0.98f, 0.8f), 0.8f),
                                                                    0.002f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 5 – TERRACOTTA_CANYON
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_TERRACOTTA_CANYON,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(1.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x3A1800)
                                                            .fogColor(0xBB5500)
                                                            .waterColor(0xCC6622)
                                                            .waterFogColor(0x883300)
                                                            .grassColorOverride(0xCC6633)
                                                            .foliageColorOverride(0xFF8844)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(0.8f, 0.3f, 0.05f), 1.5f),
                                                                    0.005f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 6 – FUNGAL_DEPTHS
                                    // Bioluminescent deep-cave feel. Dark teal-black sky; pulsing
                                    // blue-green spore fog; spore-blossom and warped-fungus particles.
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_FUNGAL_DEPTHS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.7f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x020D0A)       // near-black deep teal sky
                                                            .fogColor(0x0A4A2A)       // deep bioluminescent green fog
                                                            .waterColor(0x00FF88)     // vivid neon-green water
                                                            .waterFogColor(0x003311)
                                                            .grassColorOverride(0x22EE66)  // glowing lime grass
                                                            .foliageColorOverride(0x44FF99) // vivid mint foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.SPORE_BLOSSOM_AIR,
                                                                    0.006f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 7 – GLACIAL_SHELF
                                    // Arctic continental platforms. Pale icy-blue sky; aurora-shimmer
                                    // white-blue fog; snowflake particles drifting down.
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_GLACIAL_SHELF,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(-1.0f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xC8E8FF)       // pale icy sky
                                                            .fogColor(0xDDEEFF)       // milky arctic haze
                                                            .waterColor(0x3B6FCC)     // deep glacial blue water
                                                            .waterFogColor(0x1A3366)
                                                            .grassColorOverride(0xCCEEFF)  // frost-white grass
                                                            .foliageColorOverride(0xAADDFF) // pale blue foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.SNOWFLAKE,
                                                                    0.003f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 8 – GILDED_RUINS
                                    // Ancient oxidized-copper plateaus. Warm amber sky with a verdigris
                                    // tinge; golden dust particle shimmer; crumbling mesa silhouettes.
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_GILDED_RUINS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(1.2f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x2A1A05)       // deep antique-gold sky
                                                            .fogColor(0x886622)       // amber-dust fog
                                                            .waterColor(0xCCAA33)     // golden murky water
                                                            .waterFogColor(0x664400)
                                                            .grassColorOverride(0x99AA44)  // copper-patina grass
                                                            .foliageColorOverride(0xAABB22) // oxidized-green foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(0.9f, 0.7f, 0.1f), 1.0f),
                                                                    0.003f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());
                                })

                                // =============================================================
                                // DIMENSION TYPE REGISTRY (unchanged)
                                // =============================================================
                                .add(Registries.DIMENSION_TYPE, bootstrap -> {

                                    bootstrap.register(ModDimensions.SPACE_TYPE_KEY, new DimensionType(
                                            OptionalLong.empty(), true, false, false, false,
                                            1.0, true, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));

                                    bootstrap.register(ModDimensions.WORLD_CREATION_TYPE_KEY, new DimensionType(
                                            OptionalLong.empty(), true, false, false, false,
                                            1.0, true, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "nature"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));

                                    bootstrap.register(ModDimensions.SEFIRAH_CASTLE_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(6000), false, true, false, false,
                                            1.0, false, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));

                                    bootstrap.register(ModDimensions.SPIRIT_WORLD_TYPE_KEY, new DimensionType(
                                            OptionalLong.empty(), true, false, false, false,
                                            1.0, false, false, 0, 256, 256,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));

                                    bootstrap.register(ModDimensions.CONCEALMENT_WORLD_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(6000), true, false, false, false,
                                            1.0, true, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "concealment_world"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));
                                })

                                // =============================================================
                                // LEVEL STEM REGISTRY
                                // =============================================================
                                .add(Registries.LEVEL_STEM, bootstrap -> {
                                    var biomeRegistry   = bootstrap.lookup(Registries.BIOME);
                                    var dimensionTypes  = bootstrap.lookup(Registries.DIMENSION_TYPE);

                                    // SPACE
                                    bootstrap.register(ModDimensions.SPACE_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.SPACE_TYPE_KEY),
                                                    new EmptyChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.SPACE_BIOME_KEY)))));

                                    // NATURE / WORLD CREATION
                                    bootstrap.register(ModDimensions.WORLD_CREATION_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.WORLD_CREATION_TYPE_KEY),
                                                    new NatureDimensionWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.WORLD_CREATION_BIOME_KEY)))));

                                    // SPIRIT WORLD — 9 biomes
                                    // The holder list order MUST match ModDimensions.SPIRIT_WORLD_BIOME_KEYS
                                    // and SpiritWorldBiomeSource.BIOME_ORDER.
                                    var spiritBiomeSource = new SpiritWorldBiomeSource(List.of(
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_WOOL_MEADOWS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_CRYSTALLINE_PEAKS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_VOID_GARDENS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_EMBER_WASTES),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_QUARTZ_FLATS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_TERRACOTTA_CANYON),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_FUNGAL_DEPTHS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_GLACIAL_SHELF),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_GILDED_RUINS)
                                    ));
                                    bootstrap.register(ModDimensions.SPIRIT_WORLD_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.SPIRIT_WORLD_TYPE_KEY),
                                                    new SpiritWorldChunkGenerator(spiritBiomeSource)));

                                    // SEFIRAH CASTLE
                                    bootstrap.register(ModDimensions.SEFIRAH_CASTLE_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.SEFIRAH_CASTLE_TYPE_KEY),
                                                    new PreGeneratedChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.SEFIRAH_CASTLE_BIOME_KEY)))));

                                    // CONCEALMENT WORLD
                                    bootstrap.register(ModDimensions.CONCEALMENT_WORLD_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.CONCEALMENT_WORLD_TYPE_KEY),
                                                    new ConcealmentWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.CONCEALMENT_WORLD_BIOME_KEY)))));
                                }),
                        Set.of(LOTMCraft.MOD_ID)
                )
        );
    }
}
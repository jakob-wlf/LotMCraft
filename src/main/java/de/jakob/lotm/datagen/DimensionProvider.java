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
                                    // Non-Spirit-World biomes (unchanged)
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
                                    // Spirit World – 6 distinct biomes
                                    // Index order MUST match ModDimensions.SPIRIT_WORLD_BIOME_KEYS
                                    // and SpiritWorldBiomeSource.BIOME_ORDER.
                                    // ---------------------------------------------------------

                                    // 0 – WOOL_MEADOWS
                                    // Bright, rainbow-saturated pastel sky with warm magenta fog.
                                    // Colourful wool and grass patches in rolling island clusters.
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_WOOL_MEADOWS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.8f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xFF99DD)       // warm candy pink sky
                                                            .fogColor(0xFF55BB)       // deep magenta fog
                                                            .waterColor(0xFF69B4)     // hot pink water
                                                            .waterFogColor(0xAA1177)
                                                            .grassColorOverride(0x55FF88)  // vivid lime grass
                                                            .foliageColorOverride(0xFFDD00) // golden foliage
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
                                    // Cold deep-indigo sky; icy cyan fog; end-rod particles drift upward.
                                    // Needle-thin amethyst/prismarine spires shooting into the void.
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_CRYSTALLINE_PEAKS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(-0.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x050520)       // near-black indigo sky
                                                            .fogColor(0x0033AA)       // deep cold blue fog
                                                            .waterColor(0x00EEFF)     // electric cyan water
                                                            .waterFogColor(0x002266)
                                                            .grassColorOverride(0xAAEEFF)  // icy blue-white grass
                                                            .foliageColorOverride(0x55BBFF) // pale sky foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.END_ROD,
                                                                    0.003f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 2 – VOID_GARDENS
                                    // Soft purple sky; deep violet fog; slow falling note-block particles.
                                    // Tiny end-stone/purpur islands scattered across a huge Y range.
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_VOID_GARDENS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x220044)       // dark violet sky
                                                            .fogColor(0x550077)       // deep purple fog
                                                            .waterColor(0xCC44FF)     // purple water
                                                            .waterFogColor(0x330055)
                                                            .grassColorOverride(0xDD88FF)  // lavender grass
                                                            .foliageColorOverride(0xFF99EE) // pink foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.PORTAL,
                                                                    0.002f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 3 – EMBER_WASTES
                                    // Dark blood-orange sky; thick ember-red fog; flame particle wisps.
                                    // Enormous flat netherrack/blackstone continents.
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_EMBER_WASTES,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(2.0f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x1A0500)       // almost-black deep red sky
                                                            .fogColor(0x882200)       // smouldering ember fog
                                                            .waterColor(0xFF4400)     // lava-orange water
                                                            .waterFogColor(0x660000)
                                                            .grassColorOverride(0x993300)  // scorched earth grass
                                                            .foliageColorOverride(0xCC4400) // ember foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.LAVA,
                                                                    0.001f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    // 4 – QUARTZ_FLATS
                                    // Pale cream/gold sky; warm white fog; barely visible.
                                    // Giant perfectly flat quartz table-tops.
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_QUARTZ_FLATS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(1.0f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xFFF5CC)       // warm ivory sky
                                                            .fogColor(0xFFEEAA)       // golden-white haze fog
                                                            .waterColor(0xEEDDAA)     // pale amber water
                                                            .waterFogColor(0xBBAA66)
                                                            .grassColorOverride(0xEEFFCC)  // bleached grass
                                                            .foliageColorOverride(0xDDEE99) // pale gold foliage
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
                                    // Warm sienna/terracotta sky; dusty orange fog; falling dust particles.
                                    // Layered mesa canyons with visible depth strata.
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_TERRACOTTA_CANYON,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(1.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x3A1800)       // dark burnt-sienna sky
                                                            .fogColor(0xBB5500)       // terracotta dust fog
                                                            .waterColor(0xCC6622)     // muddy canyon water
                                                            .waterFogColor(0x883300)
                                                            .grassColorOverride(0xCC6633)  // terracotta-tinted grass
                                                            .foliageColorOverride(0xFF8844) // warm orange foliage
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(0.8f, 0.3f, 0.05f), 1.5f),
                                                                    0.005f))
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

                                    // SPIRIT WORLD — SpiritWorldBiomeSource replaces FixedBiomeSource
                                    // The holder list order MUST match ModDimensions.SPIRIT_WORLD_BIOME_KEYS
                                    // and SpiritWorldBiomeSource.BIOME_ORDER.
                                    var spiritBiomeSource = new SpiritWorldBiomeSource(List.of(
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_WOOL_MEADOWS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_CRYSTALLINE_PEAKS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_VOID_GARDENS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_EMBER_WASTES),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_QUARTZ_FLATS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_TERRACOTTA_CANYON)
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
package de.jakob.lotm.datagen;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.*;
import de.jakob.lotm.entity.ModEntities;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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
                                .add(Registries.BIOME, bootstrap -> {

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

                                    bootstrap.register(ModDimensions.DEEP_SPACE_BIOME_KEY,
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

                                    bootstrap.register(ModDimensions.BROOD_HIVE_BIOME_KEY,
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

                                    bootstrap.register(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.2f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x000000)
                                                            .fogColor(0x050505)
                                                            .waterColor(0x000000)
                                                            .waterFogColor(0x000000)
                                                            .grassColorOverride(0x0a0a0a)
                                                            .foliageColorOverride(0x0a0a0a)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.UNDERWORLD_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.15f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x000000)
                                                            .fogColor(0x09090d)
                                                            .waterColor(0xd8d8de)
                                                            .waterFogColor(0x777780)
                                                            .grassColorOverride(0x111116)
                                                            .foliageColorOverride(0x17171d)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(underworldSpawns(false))
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.ETERNAL_DARKNESS_FORK_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.05f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x000000)
                                                            .fogColor(0x020203)
                                                            .waterColor(0x090909)
                                                            .waterFogColor(0x020203)
                                                            .grassColorOverride(0x17120f)
                                                            .foliageColorOverride(0x1c1713)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(underworldSpawns(true))
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

                                    bootstrap.register(ModDimensions.HISTORICAL_VOID_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x000000)
                                                            .fogColor(0xAAAAAA)
                                                            .waterColor(0xAAAAAA)
                                                            .waterFogColor(0xBBBBBB)
                                                            .grassColorOverride(0xAAAAAA)
                                                            .foliageColorOverride(0xAAAAAA)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.MIRROR_WORLD_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x000000)
                                                            .fogColor(0xAAAAAA)
                                                            .waterColor(0xAAAAAA)
                                                            .waterFogColor(0xBBBBBB)
                                                            .grassColorOverride(0xAAAAAA)
                                                            .foliageColorOverride(0xAAAAAA)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());


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

                                    bootstrap.register(ModDimensions.SPACE_TIME_LABYRINTH_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.0f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x050010)          // near-black with a violet tint
                                                            .fogColor(0x0D0025)          // deep indigo fog (short render distance feel)
                                                            .waterColor(0x6600CC)        // electric purple water
                                                            .waterFogColor(0x220044)     // void purple water fog
                                                            .grassColorOverride(0x3B006B)
                                                            .foliageColorOverride(0x4D0088)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(0.45f, 0.05f, 0.9f), 1.0f), // purple dust
                                                                    0.007f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

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
                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_FUNGAL_DEPTHS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.7f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x020D0A)
                                                            .fogColor(0x0A4A2A)
                                                            .waterColor(0x00FF88)
                                                            .waterFogColor(0x003311)
                                                            .grassColorOverride(0x22EE66)
                                                            .foliageColorOverride(0x44FF99)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.SPORE_BLOSSOM_AIR,
                                                                    0.006f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_GLACIAL_SHELF,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(-1.0f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xC8E8FF)
                                                            .fogColor(0xDDEEFF)
                                                            .waterColor(0x3B6FCC)
                                                            .waterFogColor(0x1A3366)
                                                            .grassColorOverride(0xCCEEFF)
                                                            .foliageColorOverride(0xAADDFF)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    ParticleTypes.SNOWFLAKE,
                                                                    0.003f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_GILDED_RUINS,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(1.2f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x2A1A05)
                                                            .fogColor(0x886622)
                                                            .waterColor(0xCCAA33)
                                                            .waterFogColor(0x664400)
                                                            .grassColorOverride(0x99AA44)
                                                            .foliageColorOverride(0xAABB22)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(0.9f, 0.7f, 0.1f), 1.0f),
                                                                    0.003f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_WASTES,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.3f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x1A1220)
                                                            .fogColor(0x4A3A55)
                                                            .waterColor(0x6B4A88)
                                                            .waterFogColor(0x241A33)
                                                            .grassColorOverride(0x6B6B5A)
                                                            .foliageColorOverride(0x5A5A6B)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(0.55f, 0.35f, 0.75f), 1.0f),
                                                                    0.0025f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_MIRE_HOLLOW,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.6f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x0F1A0A)
                                                            .fogColor(0x2A3D1F)
                                                            .waterColor(0x4A6B2A)
                                                            .waterFogColor(0x141F0D)
                                                            .grassColorOverride(0x5A7A3A)
                                                            .foliageColorOverride(0x4A6B2A)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(0.35f, 0.45f, 0.2f), 1.1f),
                                                                    0.004f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.SPIRIT_BIOME_BONE_STEPPES,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.9f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xEDE6D0)
                                                            .fogColor(0xD8CBA0)
                                                            .waterColor(0xC9BFA0)
                                                            .waterFogColor(0x9A8F6D)
                                                            .grassColorOverride(0xC7C0A0)
                                                            .foliageColorOverride(0xB8AE88)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(0.9f, 0.87f, 0.75f), 0.7f),
                                                                    0.0018f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.DREAM_MAZE_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.5f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0x1A0533)
                                                            .fogColor(0x2D0A4E)
                                                            .waterColor(0xAA44FF)
                                                            .waterFogColor(0x330055)
                                                            .grassColorOverride(0xCC99FF)
                                                            .foliageColorOverride(0xBB77EE)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .ambientParticle(new AmbientParticleSettings(
                                                                    new DustParticleOptions(
                                                                            new Vector3f(0.6f, 0.2f, 1.0f), 1.0f),
                                                                    0.004f))
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());

                                    bootstrap.register(ModDimensions.KEY_OF_LIGHT_BIOME_KEY,
                                            new Biome.BiomeBuilder()
                                                    .hasPrecipitation(false)
                                                    .temperature(0.8f).downfall(0.0f)
                                                    .specialEffects(new BiomeSpecialEffects.Builder()
                                                            .skyColor(0xBFE8FF)
                                                            .fogColor(0xE8F6FF)
                                                            .waterColor(0x3F76E4)
                                                            .waterFogColor(0x050533)
                                                            .grassColorOverride(0x79C05A)
                                                            .foliageColorOverride(0x59AE30)
                                                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                                            .build())
                                                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                                                    .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                                                    .build());
                                })

                                .add(Registries.DIMENSION_TYPE, bootstrap -> {
                                    bootstrap.register(ModDimensions.DREAM_MAZE_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(18000), false, false, false, false,
                                            1.0, false, false, 0, 256, 256,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "dream_maze"),
                                            0.1f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));

                                    bootstrap.register(ModDimensions.SPACE_TYPE_KEY, new DimensionType(
                                            OptionalLong.empty(), true, false, false, false,
                                            1.0, true, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));

                                    bootstrap.register(ModDimensions.DEEP_SPACE_TYPE_KEY, new DimensionType(
                                            OptionalLong.empty(), true, false, false, false,
                                            1.0, true, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "deep_space"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));

                                    bootstrap.register(ModDimensions.SPACE_TIME_LABYRINTH_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(18000),   // always "night" (fixed time)
                                            false,                    // hasSkylight – false: no daylight cycle lighting
                                            false,                    // hasCeiling
                                            false,                    // ultraWarm
                                            false,                    // natural
                                            1.0,                      // coordinateScale
                                            false,                    // bedWorks
                                            false,                    // respawnAnchorWorks
                                            0,                        // minY
                                            256,                      // height
                                            256,                      // logicalHeight
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space_time_labyrinth"),
                                            0.05f,                    // ambientLight – very dark
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));

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

                                    bootstrap.register(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(18000), false, false, false, false,
                                            1.0, false, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "river_of_eternal_darkness"),
                                            0.2f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));

                                    bootstrap.register(ModDimensions.BROOD_HIVE_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(6000), false, true, false, false,
                                            1.0, false, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "brood_hive"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));

                                    bootstrap.register(ModDimensions.UNDERWORLD_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(18000), false, false, false, false,
                                            1.0, false, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "underworld"),
                                            0.08f,
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

                                    bootstrap.register(ModDimensions.HISTORICAL_VOID_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(6000), true, false, false, false,
                                            1.0, true, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "historical_void"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));

                                    bootstrap.register(ModDimensions.MIRROR_WORLD_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(6000), true, false, false, false,
                                            1.0, true, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mirror_world"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));

                                    bootstrap.register(ModDimensions.KEY_OF_LIGHT_TYPE_KEY, new DimensionType(
                                            OptionalLong.of(6000), true, false, false, false,
                                            1.0, false, false, -64, 384, 384,
                                            BlockTags.INFINIBURN_OVERWORLD,
                                            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "key_of_light"),
                                            1.0f,
                                            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)));
                                })

                                .add(Registries.LEVEL_STEM, bootstrap -> {
                                    var biomeRegistry   = bootstrap.lookup(Registries.BIOME);
                                    var dimensionTypes  = bootstrap.lookup(Registries.DIMENSION_TYPE);

                                    bootstrap.register(ModDimensions.DREAM_MAZE_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.DREAM_MAZE_TYPE_KEY),
                                                    new EmptyChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.DREAM_MAZE_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.SPACE_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.SPACE_TYPE_KEY),
                                                    new EmptyChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.SPACE_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.DEEP_SPACE_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.DEEP_SPACE_TYPE_KEY),
                                                    new DeepSpaceDimensionChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.DEEP_SPACE_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.WORLD_CREATION_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.WORLD_CREATION_TYPE_KEY),
                                                    new NatureDimensionWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.WORLD_CREATION_BIOME_KEY)))));
                                    var spiritBiomeSource = new SpiritWorldBiomeSource(List.of(
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_WOOL_MEADOWS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_CRYSTALLINE_PEAKS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_VOID_GARDENS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_EMBER_WASTES),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_QUARTZ_FLATS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_TERRACOTTA_CANYON),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_FUNGAL_DEPTHS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_GLACIAL_SHELF),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_GILDED_RUINS),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_WASTES),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_MIRE_HOLLOW),
                                            biomeRegistry.getOrThrow(ModDimensions.SPIRIT_BIOME_BONE_STEPPES)
                                    ));
                                    bootstrap.register(ModDimensions.SPIRIT_WORLD_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.SPIRIT_WORLD_TYPE_KEY),
                                                    new SpiritWorldChunkGenerator(spiritBiomeSource)));

                                    bootstrap.register(ModDimensions.SEFIRAH_CASTLE_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.SEFIRAH_CASTLE_TYPE_KEY),
                                                    new SefirotChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.SEFIRAH_CASTLE_BIOME_KEY)), "sefirah_castle")));

                                    bootstrap.register(ModDimensions.BROOD_HIVE_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.BROOD_HIVE_TYPE_KEY),
                                                    new SefirotChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.BROOD_HIVE_BIOME_KEY)), "brood_hive")));

                                    bootstrap.register(ModDimensions.SPACE_TIME_LABYRINTH_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.SPACE_TIME_LABYRINTH_TYPE_KEY),
                                                    new SpaceTimeLabyrinthChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.SPACE_TIME_LABYRINTH_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.CONCEALMENT_WORLD_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.CONCEALMENT_WORLD_TYPE_KEY),
                                                    new ConcealmentWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.CONCEALMENT_WORLD_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_TYPE_KEY),
                                                    new SefirotChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_BIOME_KEY)),
                                                                    "river_of_eternal_darkness")));

                                    bootstrap.register(ModDimensions.UNDERWORLD_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.UNDERWORLD_TYPE_KEY),
                                                    new UnderworldChunkGenerator(
                                                            new UnderworldBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.UNDERWORLD_BIOME_KEY),
                                                                    biomeRegistry.getOrThrow(ModDimensions.ETERNAL_DARKNESS_FORK_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.CHAOS_SEA_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.CHAOS_SEA_TYPE_KEY),
                                                    new SefirotChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.CHAOS_SEA_BIOME_KEY)),
                                                            "chaos_sea")));

                                    bootstrap.register(ModDimensions.BROOD_HIVE_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.BROOD_HIVE_TYPE_KEY),
                                                    // new PreGeneratedChunkGenerator(
                                                    new NatureDimensionWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.BROOD_HIVE_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.CITY_OF_CALAMITY_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.CITY_OF_CALAMITY_TYPE_KEY),
                                                    // new PreGeneratedChunkGenerator(
                                                    new NatureDimensionWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.CITY_OF_CALAMITY_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.NATION_OF_DISORDER_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.NATION_OF_DISORDER_TYPE_KEY),
                                                    // new PreGeneratedChunkGenerator(
                                                    new NatureDimensionWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.NATION_OF_DISORDER_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.TENEBROUS_WORLD_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.TENEBROUS_WORLD_TYPE_KEY),
                                                     // new PreGeneratedChunkGenerator(
                                                    new NatureDimensionWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.TENEBROUS_WORLD_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.KNOWLEDGE_MOOR_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.KNOWLEDGE_MOOR_TYPE_KEY),
                                                    // new PreGeneratedChunkGenerator(
                                                    new NatureDimensionWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.KNOWLEDGE_MOOR_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.KEY_OF_LIGHT_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.KEY_OF_LIGHT_TYPE_KEY),
                                                    new SefirotChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.KEY_OF_LIGHT_BIOME_KEY)),
                                                            "data/lotmcraft/dimension_data/key_of_light/")));

                                    bootstrap.register(ModDimensions.HISTORICAL_VOID_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.HISTORICAL_VOID_TYPE_KEY),
                                                    new HistoricalVoidChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.HISTORICAL_VOID_BIOME_KEY)))));

                                    bootstrap.register(ModDimensions.MIRROR_WORLD_LEVEL_KEY,
                                            new LevelStem(
                                                    dimensionTypes.getOrThrow(ModDimensions.MIRROR_WORLD_TYPE_KEY),
                                                    new MirrorWorldChunkGenerator(
                                                            new FixedBiomeSource(
                                                                    biomeRegistry.getOrThrow(ModDimensions.MIRROR_WORLD_BIOME_KEY)))));
                                }),
                        Set.of(LOTMCraft.MOD_ID)
                )
        );
    }

        private static MobSpawnSettings underworldSpawns(boolean eternalFork) {
                MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder()
                                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 65, 1, 3))
                                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 55, 1, 3))
                                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 25, 1, 2))
                                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPIRIT_GHOST.get(), eternalFork ? 55 : 35, 1, 3))
                                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPIRIT_DERVISH_ENTITY.get(), 25, 1, 2))
                                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPIRIT_BUBBLES_ENTITY.get(), 22, 1, 2))
                                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPIRIT_BLUE_WIZARD.get(), 14, 1, 1))
                                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPIRIT_TRANSLUCENT_WIZARD.get(), 12, 1, 1))
                                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.SPIRIT_MALMOUTH.get(), 10, 1, 1));

                if (eternalFork) {
                        builder.addSpawn(MobCategory.MONSTER,
                            new MobSpawnSettings.SpawnerData(ModEntities.SPIRIT_BANE.get(), 14, 1, 1));
                }
                return builder.build();
        }
}
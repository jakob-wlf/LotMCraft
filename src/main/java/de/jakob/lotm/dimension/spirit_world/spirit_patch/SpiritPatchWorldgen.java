package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class SpiritPatchWorldgen {

    public static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
        ctx.register(SpiritPatchBiomes.DECOR_CONFIGURED,
                new ConfiguredFeature<>(SpiritPatchRegistries.PATCH_DECOR.get(), NoneFeatureConfiguration.INSTANCE));
    }

    public static void placedFeatures(BootstrapContext<PlacedFeature> ctx) {
        var cfg = ctx.lookup(Registries.CONFIGURED_FEATURE);
        ctx.register(SpiritPatchBiomes.DECOR_PLACED, new PlacedFeature(
                cfg.getOrThrow(SpiritPatchBiomes.DECOR_CONFIGURED),
                List.of(CountPlacement.of(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())));
    }

    public static void biome(BootstrapContext<Biome> ctx) {
        var placed = ctx.lookup(Registries.PLACED_FEATURE);
        var carvers = ctx.lookup(Registries.CONFIGURED_CARVER);

        BiomeGenerationSettings.Builder gen = new BiomeGenerationSettings.Builder(placed, carvers)
                .addCarver(GenerationStep.Carving.AIR, Carvers.CAVE)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, SpiritPatchBiomes.DECOR_PLACED);

        ctx.register(SpiritPatchBiomes.SPIRIT_WORLD_PATCH, new Biome.BiomeBuilder()
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
                                new DustParticleOptions(new Vector3f(1.0f, 0.4f, 0.9f), 1.2f), 0.004f))
                        .build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(gen.build())
                .build());
    }

    public static void structures(BootstrapContext<Structure> ctx) {
        var biomes = ctx.lookup(Registries.BIOME);
        ctx.register(SpiritPatchBiomes.SHRINE, new SpiritShrineStructure(new Structure.StructureSettings(
                HolderSet.direct(biomes.getOrThrow(SpiritPatchBiomes.SPIRIT_WORLD_PATCH)),
                Map.<MobCategory, net.minecraft.world.level.levelgen.structure.StructureSpawnOverride>of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE)));
    }

    public static void structureSets(BootstrapContext<StructureSet> ctx) {
        var structures = ctx.lookup(Registries.STRUCTURE);
        ctx.register(SpiritPatchBiomes.SHRINE_SET, new StructureSet(
                structures.getOrThrow(SpiritPatchBiomes.SHRINE),
                new RandomSpreadStructurePlacement(4, 1, RandomSpreadType.LINEAR, 1747302)));
    }
}
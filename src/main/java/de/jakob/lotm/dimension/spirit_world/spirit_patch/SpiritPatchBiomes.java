package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SpiritPatchBiomes {
    public static final ResourceKey<Biome> SPIRIT_WORLD_PATCH = ResourceKey.create(Registries.BIOME, id("spirit_world_patch"));
    public static final ResourceKey<Structure> SHRINE = ResourceKey.create(Registries.STRUCTURE, id("spirit_shrine"));
    public static final ResourceKey<StructureSet> SHRINE_SET = ResourceKey.create(Registries.STRUCTURE_SET, id("spirit_shrine"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> DECOR_CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE, id("spirit_patch_decor"));
    public static final ResourceKey<PlacedFeature> DECOR_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, id("spirit_patch_decor"));

    private static ResourceLocation id(String p) { return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, p); }
}

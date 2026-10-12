package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class SpiritPatchRegion extends Region {

    public static final int WEIGHT = 1;

    private static final Climate.ParameterPoint PATCH_POINT = Climate.parameters(
            Climate.Parameter.span(0f, 1.5f),
            Climate.Parameter.span(-.2f, 1.00f),
            Climate.Parameter.span(0.1f, 1.00f),
            Climate.Parameter.span(-0.78f, -0.37f),
            Climate.Parameter.point(0.0f),
            Climate.Parameter.span(-0.3f, 0.3f),
            0.0f);

    public SpiritPatchRegion(ResourceLocation name) {
        super(name, RegionType.OVERWORLD, WEIGHT);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        this.addModifiedVanillaOverworldBiomes(mapper, builder -> { });
        mapper.accept(Pair.of(PATCH_POINT, SpiritPatchBiomes.SPIRIT_WORLD_PATCH));
    }
}

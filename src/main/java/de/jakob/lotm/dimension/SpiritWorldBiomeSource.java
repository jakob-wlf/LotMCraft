package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.stream.Stream;

public class SpiritWorldBiomeSource extends BiomeSource {

    public static final MapCodec<SpiritWorldBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Biome.CODEC.listOf()
                            .fieldOf("biomes")
                            .forGetter(src -> src.biomes)
            ).apply(instance, SpiritWorldBiomeSource::new));

    public static final SpiritWorldBiome[] BIOME_ORDER = SpiritWorldBiome.values();

    private final List<Holder<Biome>> biomes;

    public SpiritWorldBiomeSource(List<Holder<Biome>> biomes) {
        this.biomes = List.copyOf(biomes);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return biomes.stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ,
                                       Climate.Sampler sampler) {
        int blockX = biomeX << 2;
        int blockZ = biomeZ << 2;

        SpiritWorldBiome swb = SpiritWorldBiome.getBiomeAt(blockX, blockZ);

        for (int i = 0; i < BIOME_ORDER.length; i++) {
            if (BIOME_ORDER[i] == swb) {
                return biomes.get(i);
            }
        }

        return biomes.get(0);
    }
}
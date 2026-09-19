package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

public final class UnderworldBiomeSource extends BiomeSource {
    public static final MapCodec<UnderworldBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Biome.CODEC.fieldOf("underworld").forGetter(source -> source.underworld),
                    Biome.CODEC.fieldOf("eternal_darkness_fork").forGetter(source -> source.eternalDarknessFork)
            ).apply(instance, UnderworldBiomeSource::new));

    private static final int REGION_SIZE = 512;
    private static final int RARITY = 12;

    private final Holder<Biome> underworld;
    private final Holder<Biome> eternalDarknessFork;

    public UnderworldBiomeSource(Holder<Biome> underworld, Holder<Biome> eternalDarknessFork) {
        this.underworld = underworld;
        this.eternalDarknessFork = eternalDarknessFork;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(underworld, eternalDarknessFork);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
        return isEternalDarknessFork(biomeX << 2, biomeZ << 2)
                ? eternalDarknessFork
                : underworld;
    }

    public static boolean isEternalDarknessFork(int blockX, int blockZ) {
        int regionX = Math.floorDiv(blockX, REGION_SIZE);
        int regionZ = Math.floorDiv(blockZ, REGION_SIZE);
        long hash = regionX * 341873128712L + regionZ * 132897987541L + 0x455445524E414C4CL;
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdl;
        hash ^= hash >>> 33;
        return Math.floorMod(hash, RARITY) == 0;
    }
}
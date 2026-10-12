package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;


public class UnderworldBiomeSource extends BiomeSource {

    public static final MapCodec<UnderworldBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Biome.CODEC.fieldOf("styx_chasm").forGetter(s -> s.styxChasm),
                    Biome.CODEC.fieldOf("pale_white_nation").forGetter(s -> s.paleWhiteNation)
            ).apply(instance, UnderworldBiomeSource::new));

    private final Holder<Biome> styxChasm;
    private final Holder<Biome> paleWhiteNation;

    public UnderworldBiomeSource(Holder<Biome> styxChasm, Holder<Biome> paleWhiteNation) {
        this.styxChasm = styxChasm;
        this.paleWhiteNation = paleWhiteNation;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(styxChasm, paleWhiteNation);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        int x = (quartX << 2) + 2;
        int z = (quartZ << 2) + 2;
        return UnderworldTerrain.isPale(x, z) ? paleWhiteNation : styxChasm;
    }
}
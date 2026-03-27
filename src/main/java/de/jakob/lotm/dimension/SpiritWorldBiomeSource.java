package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.stream.Stream;

/**
 * Custom BiomeSource for the Spirit World.
 *
 * Delegates spatial biome selection to {@link SpiritWorldBiome#getBiomeAt(int, int)},
 * which uses a large-cell Voronoi system to assign one of the six biomes.
 * Each biome is mapped to its own registered {@link Biome} holder so Minecraft's
 * rendering pipeline (sky/fog colour, grass tint, particles) is fully driven by
 * the correct biome per region.
 *
 * The holder list order MUST match {@link SpiritWorldBiome#values()} and
 * {@link ModDimensions#SPIRIT_WORLD_BIOME_KEYS}.
 */
public class SpiritWorldBiomeSource extends BiomeSource {

    /**
     * Codec: serialises/deserialises the six biome holders by their registry key.
     * The list is positional — index N corresponds to SpiritWorldBiome.values()[N].
     */
    public static final MapCodec<SpiritWorldBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Biome.CODEC.listOf()
                            .fieldOf("biomes")
                            .forGetter(src -> src.biomes)
            ).apply(instance, SpiritWorldBiomeSource::new));

    /**
     * The canonical enum order — the index of each entry here is used in
     * {@link #getNoiseBiome} to pick the correct holder.
     * Must stay in sync with {@link SpiritWorldBiome#values()}.
     */
    public static final SpiritWorldBiome[] BIOME_ORDER = SpiritWorldBiome.values();

    /** Holders in the same positional order as {@link #BIOME_ORDER}. */
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

    /**
     * Minecraft calls this in biome coordinates (block / 4).
     * We convert back to approximate block coordinates before querying the
     * Voronoi system so the biome boundary positions are consistent with
     * what {@link SpiritWorldChunkGenerator} sees.
     */
    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ,
                                       Climate.Sampler sampler) {
        // Biome coords → block coords (centre of the 4×4×4 cell)
        int blockX = biomeX << 2;
        int blockZ = biomeZ << 2;

        SpiritWorldBiome swb = SpiritWorldBiome.getBiomeAt(blockX, blockZ);

        // Linear scan to map enum ordinal → list index.
        // BIOME_ORDER is only 6 entries so this is effectively O(1).
        for (int i = 0; i < BIOME_ORDER.length; i++) {
            if (BIOME_ORDER[i] == swb) {
                return biomes.get(i);
            }
        }

        // Fallback — should never happen unless BIOME_ORDER/biomes are out of sync
        return biomes.get(0);
    }
}
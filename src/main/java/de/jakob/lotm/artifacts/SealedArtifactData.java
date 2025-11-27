package de.jakob.lotm.artifacts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data component for sealed artifacts containing abilities and negative effects
 */
public record SealedArtifactData(
        String pathway,
        int sequence,
        List<AbilityItem> abilities,
        NegativeEffect negativeEffect
) {
    
    public static final Codec<SealedArtifactData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("pathway").forGetter(SealedArtifactData::pathway),
                    Codec.INT.fieldOf("sequence").forGetter(SealedArtifactData::sequence),
                    Codec.list(BuiltInRegistries.ITEM.byNameCodec())
                            .xmap(
                                    items -> items.stream()
                                            .filter(item -> item instanceof AbilityItem)
                                            .map(item -> (AbilityItem) item)
                                            .collect(Collectors.toList()),
                                    abilities -> abilities.stream()
                                            .map(ability -> (Item) ability)
                                            .collect(Collectors.toList())
                            )
                            .fieldOf("abilities")
                            .forGetter(SealedArtifactData::abilities),
                    NegativeEffect.CODEC.fieldOf("negative_effect").forGetter(SealedArtifactData::negativeEffect)
            ).apply(instance, SealedArtifactData::new)
    );
}
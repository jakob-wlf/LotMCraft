package de.jakob.lotm.artifacts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data component for sealed artifacts containing abilities and negative effects
 */
public record SealedArtifactData(
        String pathway,
        int sequence,
        List<Ability> abilities,
        NegativeEffect negativeEffect
) {

    public static final Codec<SealedArtifactData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("pathway").forGetter(SealedArtifactData::pathway),
                    Codec.INT.fieldOf("sequence").forGetter(SealedArtifactData::sequence),
                    Codec.list(Codec.STRING)
                            .xmap(
                                    // String IDs -> Ability objects (with null filtering)
                                    ids -> ids.stream()
                                            .map(id -> {
                                                try {
                                                    return LOTMCraft.abilityHandler.getById(id);
                                                } catch (Exception e) {
                                                    return null;
                                                }
                                            })
                                            .filter(ability -> ability != null)
                                            .collect(Collectors.toList()),
                                    // Ability objects -> String IDs
                                    abilities -> abilities.stream()
                                            .map(Ability::getId)
                                            .collect(Collectors.toList())
                            )
                            .fieldOf("abilities")
                            .orElse(Collections.emptyList()) // Fallback to empty list
                            .forGetter(SealedArtifactData::abilities),
                    NegativeEffect.CODEC.fieldOf("negative_effect")
                            .orElseGet(() -> NegativeEffect.createDefault()) // Fallback to default
                            .forGetter(SealedArtifactData::negativeEffect)
            ).apply(instance, SealedArtifactData::new)
    );
}
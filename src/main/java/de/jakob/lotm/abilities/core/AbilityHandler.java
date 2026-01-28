package de.jakob.lotm.abilities.core;

import de.jakob.lotm.abilities.sun.IlluminateAbility2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class AbilityHandler {

    private final HashSet<Ability> abilities = new HashSet<>();

    public AbilityHandler() {
        registerAbilities();
    }

    private void registerAbilities() {
        abilities.add(new IlluminateAbility2("illuminate"));
    }

    public Ability getById(String id) {
        return abilities.stream().filter(ability -> ability.getId().equals(id)).findFirst().orElse(null);
    }

    public HashSet<Ability> getByPathwayAndSequenceExact(String pathway, int sequence) {
        return abilities
                .stream()
                .filter(ability -> ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) == sequence)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public HashSet<Ability> getByPathwayAndSequence(String pathway, int sequence) {
        return abilities
                .stream()
                .filter(ability -> ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) >= sequence)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public ArrayList<Ability> getByPathwayAndSequenceExactOrderedBySequence(String pathway, int sequence) {
        return new ArrayList<>(
                abilities.stream()
                        .filter(ability -> ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) == sequence)
                        .sorted(Comparator.comparing(ability -> ability.getRequirements().get(pathway)))
                        .toList()
        );
    }

    public ArrayList<Ability> getByPathwayAndSequenceOrderedBySequence(String pathway, int sequence) {
        return new ArrayList<>(
                abilities.stream()
                        .filter(ability -> ability.getRequirements().containsKey(pathway) && ability.getRequirements().get(pathway) >= sequence)
                        .sorted(Comparator.comparing(ability -> ability.getRequirements().get(pathway)))
                        .toList()
        );
    }
}

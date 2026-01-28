package de.jakob.lotm.abilities.core;

import de.jakob.lotm.abilities.sun.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

public class AbilityHandler {

    private final HashSet<Ability> abilities = new HashSet<>();

    public AbilityHandler() {
        registerAbilities();
    }

    private void registerAbilities() {
        abilities.add(new HolySongAbility("holy_song_ability"));
        abilities.add(new IlluminateAbility("illuminate_ability"));
        abilities.add(new HolyLightAbility("holy_light_ability"));
        abilities.add(new FireOfLightAbility("fire_of_light_ability"));
        abilities.add(new CleaveOfPurificationAbility("cleave_of_purification_ability"));
        abilities.add(new GodSaysItsEffectiveAbility("notary_buff_ability"));
        abilities.add(new GodSaysItsNotEffectiveAbility("notary_debuff_ability"));
        abilities.add(new LightOfHolinessAbility("light_of_holiness_ability"));
        abilities.add(new PurificationHaloAbility("purification_halo_ability"));
        abilities.add(new FlaringSunAbility("flaring_sun_ability"));
        abilities.add(new UnshadowedSpearAbility("unshadowed_spear_ability"));
        abilities.add(new UnshadowedDomainAbility("unshadowed_domain_ability"));
        abilities.add(new WallOfLightAbility("wall_of_light_ability"));
        abilities.add(new SwordOfJusticeAbility("sword_of_justice_ability"));
        abilities.add(new SpearOfLightAbility("spear_of_light_ability"));
        abilities.add(new PureWhiteLightAbility("pure_white_light_ability"));
        abilities.add(new DivineKingdomManifestationAbility("divine_kingdom_manifestation_ability"));
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

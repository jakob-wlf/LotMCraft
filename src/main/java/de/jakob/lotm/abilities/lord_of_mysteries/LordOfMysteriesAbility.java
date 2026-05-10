package de.jakob.lotm.abilities.lord_of_mysteries;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.LordOfMysteriesUtil;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class LordOfMysteriesAbility extends PassiveAbilityItem {
    private static final String PILLAR_AUTHORITY_ID = "pillar_authority_ability";

    public LordOfMysteriesAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of(LordOfMysteriesUtil.PATHWAY_ID, LordOfMysteriesUtil.SEQUENCE);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @Override
    public void onPassiveAbilityGained(LivingEntity entity, ServerLevel serverLevel) {
        if (entity instanceof ServerPlayer player) {
            AbilityWheelHelper.setAbilities(player, getGrantedAbilityIds());
        }
    }

    public static ArrayList<String> getGrantedAbilityIds() {
        LinkedHashSet<String> abilityIds = new LinkedHashSet<>();
        for (Ability ability : getGrantedAbilities()) {
            abilityIds.add(ability.getId());
        }
        abilityIds.add(PILLAR_AUTHORITY_ID);
        return new ArrayList<>(abilityIds);
    }

    public static ArrayList<Ability> getGrantedAbilities() {
        LinkedHashSet<Ability> granted = new LinkedHashSet<>();
        granted.addAll(LOTMCraft.abilityHandler.getByPathwayAndSequenceOrderedBySequence("fool", 0));
        granted.addAll(LOTMCraft.abilityHandler.getByPathwayAndSequenceOrderedBySequence("door", 0));
        granted.addAll(LOTMCraft.abilityHandler.getByPathwayAndSequenceOrderedBySequence("error", 0));

        Ability pillarAuthority = LOTMCraft.abilityHandler.getById(PILLAR_AUTHORITY_ID);
        if (pillarAuthority != null) {
            granted.add(pillarAuthority);
        }
        granted.removeIf(Ability::getShouldBeHidden);

        List<Ability> sorted = granted.stream()
                .sorted(Comparator.comparingInt(Ability::lowestSequenceUsable).reversed().thenComparing(Ability::getId))
                .toList();
        return new ArrayList<>(sorted);
    }
}

package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class LifeDeprivationAbility extends SelectableAbilityItem {
    public LifeDeprivationAbility(Properties properties) {
        super(properties, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.life_deprivation.target", "ability.lotmcraft.life_deprivation.area"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {

    }
}

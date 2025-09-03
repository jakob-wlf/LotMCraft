package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ThreadManipulationAbility extends SelectableAbilityItem {
    public ThreadManipulationAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[0];
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {

    }
}

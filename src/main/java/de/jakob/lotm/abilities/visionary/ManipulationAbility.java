package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ManipulationAbility extends AbilityItem {
    public ManipulationAbility(Properties properties) {
        super(properties, 30);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 600;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {

    }
}

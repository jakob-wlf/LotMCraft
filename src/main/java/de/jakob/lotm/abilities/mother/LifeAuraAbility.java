package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class LifeAuraAbility extends ToggleAbilityItem {

    public LifeAuraAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 3;
    }

    @Override
    protected void start(Level level, LivingEntity entity) {

    }

    @Override
    protected void tick(Level level, LivingEntity entity) {

    }

    @Override
    protected void stop(Level level, LivingEntity entity) {

    }
}

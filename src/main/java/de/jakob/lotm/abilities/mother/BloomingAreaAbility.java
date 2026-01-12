package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class BloomingAreaAbility extends AbilityItem {
    public BloomingAreaAbility(Properties properties) {
        super(properties, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1400;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {

    }
}

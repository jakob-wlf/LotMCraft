package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class MentalPlagueAbility extends AbilityItem {
    public MentalPlagueAbility(Properties properties) {
        super(properties, 20);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {

    }
}

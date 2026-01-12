package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ConcealmentAbility extends AbilityItem {
    public ConcealmentAbility(Properties properties) {
        super(properties, 4);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1500;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {

    }
}

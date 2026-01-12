package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SpiritualBaptismAbility extends AbilityItem {
    public SpiritualBaptismAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 900;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {

    }
}

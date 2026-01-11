package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class MaternalEmbraceAbility extends AbilityItem {
    public MaternalEmbraceAbility(Properties properties) {
        super(properties, 20);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1600;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {

    }
}

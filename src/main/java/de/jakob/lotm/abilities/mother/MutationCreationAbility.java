package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class MutationCreationAbility extends AbilityItem {
    public MutationCreationAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {

    }
}

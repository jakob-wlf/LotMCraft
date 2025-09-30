package de.jakob.lotm.abilities.fool.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PuppeteeringEnhancementsAbility extends PassiveAbilityItem {
    public PuppeteeringEnhancementsAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 4));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }
}

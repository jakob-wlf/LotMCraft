package de.jakob.lotm.beyonders.abilities.fool.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PuppeteeringEnhancementsAbility extends PassiveAbility {
    public PuppeteeringEnhancementsAbility(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 4));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }
}

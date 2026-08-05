package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PassiveLuckAbility extends PassiveAbilityItem {


    public PassiveLuckAbility(Properties properties) {
        super(properties);

    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "wheel_of_fortune", 7
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

}
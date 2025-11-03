package de.jakob.lotm.abilities.abyss.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class FireResistanceAbyssAbility extends PassiveAbilityItem {

    public FireResistanceAbyssAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "abyss", 6
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }
}

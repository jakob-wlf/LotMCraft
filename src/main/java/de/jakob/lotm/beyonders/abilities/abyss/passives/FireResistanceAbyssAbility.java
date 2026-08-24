package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class FireResistanceAbyssAbility extends PassiveAbility {

    public FireResistanceAbyssAbility(String id) {
        super(id);
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

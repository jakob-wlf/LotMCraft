package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class CriminalProficiencyAbility extends PassiveAbilityItem {
    public CriminalProficiencyAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 9));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }
}

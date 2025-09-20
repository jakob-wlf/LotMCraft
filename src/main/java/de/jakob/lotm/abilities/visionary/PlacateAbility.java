package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PlacateAbility extends SelectableAbilityItem {
    public PlacateAbility(Properties properties) {
        super(properties, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 50;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.placate.self", "ability.lotmcraft.placate.others"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {

    }
}

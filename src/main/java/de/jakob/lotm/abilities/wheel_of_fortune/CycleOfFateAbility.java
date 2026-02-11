package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.core.SelectableAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class CycleOfFateAbility extends SelectableAbility {
    public CycleOfFateAbility(String id) {
        super(id, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.cycle_of_fate.create_cycle", "ability.lotmcraft.cycle_of_fate.trigger_cycle"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {

    }
}

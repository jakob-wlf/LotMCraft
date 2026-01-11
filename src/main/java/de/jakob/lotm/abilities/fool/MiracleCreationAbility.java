package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;


/* IDEAS:
CATEGORIES:
- Structure Summoning
- Calamity Creation
    - Meteor
    - Tornado
    - Earthquake
- Area Manipulation
    - Slow Time
    - Make the ground hot
    - Darkness
    - Forbid Godhood
    - Reverse Gravity
- Teleportation
- Target Manipulation
    - Make Target lost
- Other
 */
public class MiracleCreationAbility extends SelectableAbilityItem {
    public MiracleCreationAbility(Properties properties) {
        super(properties, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.miracle_creation.summon_structure",
                "ability.lotmcraft.miracle_creation.calamity_creation",
                "ability.lotmcraft.miracle_creation.area_manipulation",
                "ability.lotmcraft.miracle_creation.teleportation",
                "ability.lotmcraft.miracle_creation.target_manipulation"
        };
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {

    }
}

package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class IdentityAvatarAbility extends AbilityItem {
    public IdentityAvatarAbility(Properties properties) {
        super(properties, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1700;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {

    }
}

package de.jakob.lotm.abilities.visionary.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class VirtualAvatarAbility extends PassiveAbilityItem {

    public VirtualAvatarAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "visionary", 3
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }
}

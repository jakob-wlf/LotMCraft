package de.jakob.lotm.abilities.tyrant.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class RiptideAbility extends PassiveAbilityItem {
    public RiptideAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 4));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(!BeyonderData.isBeyonder(entity))
            return;

        if(entity.isInWater()) {
            BeyonderData.addModifier(entity, "riptide", 1.5);
        }
        else {
            BeyonderData.removeModifier(entity, "riptide");
        }
    }
}

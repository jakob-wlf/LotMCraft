package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PuppetSoldierCreationAbility extends AbilityItem {
    public PuppetSoldierCreationAbility(Properties properties) {
        super(properties, 20 * 90);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2000;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }
    }
}

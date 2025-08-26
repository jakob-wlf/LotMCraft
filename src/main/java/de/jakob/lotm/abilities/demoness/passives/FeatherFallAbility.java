package de.jakob.lotm.abilities.demoness.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class FeatherFallAbility extends PassiveAbilityItem {

    public FeatherFallAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "demoness", 9
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(entity.fallDistance > 3)
            entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 1, false, false, false));
    }

}

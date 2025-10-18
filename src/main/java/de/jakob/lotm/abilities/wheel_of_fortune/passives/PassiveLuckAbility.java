package de.jakob.lotm.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassiveLuckAbility extends PassiveAbilityItem {

    private final HashMap<Integer, List<MobEffectInstance>> effectsPerSequence = new HashMap<>();

    public PassiveLuckAbility(Properties properties) {
        super(properties);

    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "wheel_of_fortune", 7
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }
}
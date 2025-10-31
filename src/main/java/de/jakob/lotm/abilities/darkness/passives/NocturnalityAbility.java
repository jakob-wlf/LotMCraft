package de.jakob.lotm.abilities.darkness.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NocturnalityAbility extends PassiveAbilityItem {

    private final Set<MobEffect> modifiedEffectTypes = new HashSet<>();
    private int tick = 0;

    public NocturnalityAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 9));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }
}
package de.jakob.lotm.abilities.sun.passives;

import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalEnhancementsSunAbility extends PassiveAbilityItem {

    private final HashMap<Integer, List<MobEffectInstance>> effectsPerSequence = new HashMap<>();

    public PhysicalEnhancementsSunAbility(Properties properties) {
        super(properties);

        initEffects();
    }

    private void initEffects() {
        effectsPerSequence.put(9, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 0, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 0, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 0, false, false, false)
        ));

        effectsPerSequence.put(7, List.of(
                new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 0, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 5, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 2, false, false, false)
        ));

        effectsPerSequence.put(6, List.of(
                new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 6, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 1, false, false, false)
        ));

        effectsPerSequence.put(5, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 7, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 1, false, false, false)
        ));

        effectsPerSequence.put(4, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 11, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 1, false, false, false)

        ));

        effectsPerSequence.put(3, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 14, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 1, false, false, false)
        ));

        effectsPerSequence.put(2, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 5, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 18, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 1, false, false, false)
        ));

        effectsPerSequence.put(1, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 5, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 22, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 1, false, false, false)
        ));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "sun", 9
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        int sequence = BeyonderData.getSequence(entity);

        if (sequence < 0 || sequence > 9) {
            return;
        }

        List<MobEffectInstance> effects = getEffectsForSequence(sequence);
        applyPotionEffects(entity, effects);
    }

    private List<MobEffectInstance> getEffectsForSequence(int sequence) {
        if(effectsPerSequence.containsKey(sequence)) {
            return effectsPerSequence.get(sequence);
        } else {
            for(int i = sequence; i < 10; i++) {
                if(effectsPerSequence.containsKey(i)) {
                    return effectsPerSequence.get(i);
                }
            }

            return List.of();
        }
    }
}

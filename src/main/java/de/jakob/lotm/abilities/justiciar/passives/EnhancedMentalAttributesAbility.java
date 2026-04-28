package de.jakob.lotm.abilities.justiciar.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnhancedMentalAttributesAbility extends PassiveAbilityItem {

    public EnhancedMentalAttributesAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 5));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        // Divination immunity — refresh every tick call (every 5 ticks)
        DivinationAbility.DIVINATION_IMMUNE.add(entity.getUUID());

        // Rapidly reduce LOOSING_CONTROL duration
        MobEffectInstance loosingControl = entity.getEffect(ModEffects.LOOSING_CONTROL);
        if (loosingControl != null) {
            int remaining = loosingControl.getDuration();
            if (remaining <= 40) {
                entity.removeEffect(ModEffects.LOOSING_CONTROL);
            } else {
                // Replace with a shortened instance (reduce by 40 ticks per call)
                entity.addEffect(new MobEffectInstance(
                        ModEffects.LOOSING_CONTROL,
                        remaining - 40,
                        loosingControl.getAmplifier(),
                        loosingControl.isAmbient(),
                        loosingControl.isVisible(),
                        loosingControl.showIcon()
                ));
            }
        }

        // Reduce all harmful effect durations faster
        List<MobEffectInstance> harmfulEffects = new ArrayList<>(entity.getActiveEffects().stream()
                .filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .filter(e -> e.getEffect().value() != ModEffects.LOOSING_CONTROL.value()) // already handled above
                .toList());

        for (MobEffectInstance effect : harmfulEffects) {
            int remaining = effect.getDuration();
            if (remaining <= 10) {
                entity.removeEffect(effect.getEffect());
            } else {
                entity.addEffect(new MobEffectInstance(
                        effect.getEffect(),
                        remaining - 10,
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                ));
            }
        }
    }
}

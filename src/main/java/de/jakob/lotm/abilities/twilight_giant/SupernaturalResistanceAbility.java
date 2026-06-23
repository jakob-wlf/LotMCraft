package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SupernaturalResistanceAbility extends ToggleAbility {

    public SupernaturalResistanceAbility(String id) {
        super(id); // ToggleAbilities have no cooldown
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "twilight_giant", 8
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 5; // Costs 5 spirituality PER TICK while active (= 100/second at 20 tps)
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        clearNegativeEffects(entity);
        applyResistance(entity);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        clearNegativeEffects(entity);
        applyResistance(entity);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.removeEffect(MobEffects.DAMAGE_RESISTANCE);
    }

    private void applyResistance(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, tickRate + 2, 0, false, false, true));
    }

    private void clearNegativeEffects(LivingEntity entity) {
        entity.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
                .toList()
                .forEach(entity::removeEffect);
    }
}
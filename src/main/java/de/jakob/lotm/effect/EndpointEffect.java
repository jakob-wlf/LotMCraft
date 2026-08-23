package de.jakob.lotm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Endpoint effect — cosmetic HUD marker only.
 *
 * The actual Endpoint state is stored in EndpointComponent (an attachment) so it
 * cannot be removed by milk or other effect-clearing mechanics. BeyonderDataTickHandler
 * re-applies this effect each tick while the attachment is active, keeping the HUD
 * icon visible, and clears both when a strong-enough purification cure is nearby.
 *
 * All healing-block logic (LivingHealEvent cancellation, setHealth mixin) checks
 * EndpointComponent.isActive(), not this effect's presence.
 */
public class EndpointEffect extends MobEffect {

    public EndpointEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}

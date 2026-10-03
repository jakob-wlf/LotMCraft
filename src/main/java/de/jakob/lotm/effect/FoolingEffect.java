package de.jakob.lotm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fooling effect — cosmetic HUD marker only.
 *
 * The actual Fooling state is stored in FoolingComponent (an attachment) so it
 * cannot be removed by milk or other effect-clearing mechanics.
 * BeyonderDataTickHandler re-applies this effect for 2 ticks every tick while
 * the attachment has ticks remaining, keeping the HUD icon visible.
 *
 * All gameplay logic (stun, input scrambling, ability scrambling) is driven by
 * FoolingComponent.isFooled() / ticksRemaining, not by this effect's duration.
 *
 * Client-side input inversion lives in ClientFoolingCache.
 */
public class FoolingEffect extends MobEffect {

    public static final int STUN_INTERVAL_TICKS = 200; // every 10 seconds
    public static final int STUN_DURATION_TICKS  = 60;  // stun lasts 3 seconds

    protected FoolingEffect(MobEffectCategory category, int color) {
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

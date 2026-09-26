package de.jakob.lotm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Death Decree mark effect — cosmetic HUD/screen-darkening marker only.
 *
 * The actual stack count is stored in DeathDecreeMarkComponent (an attachment) so it
 * cannot be removed by milk or other effect-clearing mechanics. BeyonderDataTickHandler
 * re-applies this effect each tick while the attachment has stacks, with amplifier =
 * stacks - 1, so the client can read amplifier to darken the screen progressively via
 * DeathDecreeOverlayRenderer.
 */
public class DeathDecreeMarkEffect extends MobEffect {

    public DeathDecreeMarkEffect(MobEffectCategory category, int color) {
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

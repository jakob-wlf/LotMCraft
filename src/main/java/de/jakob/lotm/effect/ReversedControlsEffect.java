package de.jakob.lotm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ReversedControlsEffect extends MobEffect {
    public ReversedControlsEffect() {
        super(MobEffectCategory.HARMFUL, 0x7F00FF); // purple tint
    }
}
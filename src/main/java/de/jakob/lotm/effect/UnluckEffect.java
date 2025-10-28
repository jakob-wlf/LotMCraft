package de.jakob.lotm.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class UnluckEffect extends MobEffect {

    protected UnluckEffect(MobEffectCategory category, int color) {
        super(category, color);
    }


    //Luck handled through event handler
    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        return true;
    }


}
package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.abilities.core.Ability;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;


import java.util.Map;

public class Eloquence extends Ability {


    public Eloquence(String id) {
        super(id, 5.5f);
    }


    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("black_emperor", 9);
    }


    @Override
    public float getSpiritualityCost() {
        return 50;
    }


    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(5))) {

            if (target != entity) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        200,
                        0
                ));
            }
        }
    }



}
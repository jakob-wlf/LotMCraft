package de.jakob.lotm.effect;

import de.jakob.lotm.damage.ModDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class LoosingControlEffect extends MobEffect {
    protected LoosingControlEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    Random random = new Random();

    /**
     * amplifier controls the chance of dying
     * amplifier 0: 0%
     * amplifier 1: 5%
     * amplifier 2: 10%
     * amplifier 3: 15%
     * amplifier 4: 30%
     * amplifier 5: 50%
     * amplifier 6: 70%
     * amplifier 7: 85%
     * amplifier 8: 99%
     * amplifier > 8: 100%
    */
    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if(livingEntity.level().isClientSide) {
            float yaw = random.nextFloat() * 360f - 180f;
            float pitch = random.nextFloat() * 60f - 30f;

            livingEntity.setYRot(yaw);
            livingEntity.setXRot(pitch);

            livingEntity.yBodyRot = yaw;
            livingEntity.yHeadRot = yaw;
            return true;
        }

        int effectiveAmplifier = Math.max(1, amplifier + 1);

        if(random.nextInt(7) == 0 && livingEntity.getHealth() > effectiveAmplifier) {
            livingEntity.hurt(createCustomDamageSource(livingEntity), effectiveAmplifier);
        }

        float totalProbability = getProbability(amplifier);

        if (amplifier > 8 || shouldKillThisTick(totalProbability)) {
            livingEntity.removeEffect(ModEffects.LOOSING_CONTROL);
            livingEntity.hurt(
                    createCustomDamageSource(livingEntity),
                    Math.max(livingEntity.getMaxHealth() + 5, 10000)
            );
        }

        return true;
    }

    private boolean shouldKillThisTick(float totalProbability) {
        if(totalProbability == 0f) {
            return false;
        }
        // Effect is designed to last 5 seconds = 100 ticks
        int ticks = 100;

        // Correct probability distribution:
        float perTickChance = 1f - (float) Math.pow(1f - totalProbability, 1f / ticks);

        return random.nextFloat() <= perTickChance;
    }


    private float getProbability(int amplifier) {
        return switch(amplifier) {
            case 0 -> 0.00f;
            case 1 -> 0.05f;
            case 2 -> 0.1f;
            case 3 -> 0.15f;
            case 4 -> 0.3f;
            case 5 -> 0.5f;
            case 6 -> 0.7f;
            case 7 -> 0.85f;
            case 8 -> 0.99f;
            default -> 1f;
        };
    }

    private DamageSource createCustomDamageSource(Entity entity) {
        return new DamageSource(
                entity.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ModDamageTypes.LOOSING_CONTROL)
        );
    }


    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}

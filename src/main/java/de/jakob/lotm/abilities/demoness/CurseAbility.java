package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class CurseAbility extends AbilityItem {
    public CurseAbility(Properties properties) {
        super(properties, 1.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 300;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 2, 2);

        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.curse.target_missing").withColor(0x6d32a8));
            return;
        }

        if(AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, 3));
            entity.hurt(entity.damageSources().generic(), 10);
            return;
        }

        ServerScheduler.scheduleForDuration(0, 8, 20 * 60 * 2, () -> {
            if (target.isDeadOrDying()) {
                return;
            }
            switch(random.nextInt(3)) {
                case 0 -> {
                    target.hurt(target.damageSources().onFire(), (float) (DamageLookup.lookupDamage(4, .6) * multiplier(entity)));
                    ParticleUtil.spawnParticles(serverLevel, ModParticles.BLACK_FLAME.get(), target.position().add(0, target.getEyeHeight() / 2, 0), 200, .4, target.getEyeHeight() / 2, .4, 0.01);
                }
                case 1 -> {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 3));
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 2, 3));
                }
            }
        }, serverLevel);
    }
}

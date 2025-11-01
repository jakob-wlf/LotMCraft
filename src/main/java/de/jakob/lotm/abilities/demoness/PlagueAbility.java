package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class PlagueAbility extends AbilityItem {
    public PlagueAbility(Properties properties) {
        super(properties, 120);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 400;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(0, 0, 0), 10f);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel))
            return;

        ServerScheduler.scheduleForDuration(0, 20, 20 * 80, () -> {
            if (entity.level().isClientSide)
                return;
            ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.DISEASE.get(), entity.position(), 160, 50, 0.02);
            ParticleUtil.spawnParticles((ServerLevel) entity.level(), dust, entity.position(), 160, 50, 0.02);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 70, entity.position(), new MobEffectInstance(MobEffects.WITHER, 20, 3, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 70, entity.position(), new MobEffectInstance(MobEffects.BLINDNESS, 20, 4, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 70, entity.position(), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, false));
            AbilityUtil.damageNearbyEntities((ServerLevel) entity.level(), entity, 70, 5.5 * multiplier(entity), entity.position(), true, false, true, 0);
        });
    }
}

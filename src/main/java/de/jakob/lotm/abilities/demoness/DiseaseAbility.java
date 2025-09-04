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
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class DiseaseAbility extends AbilityItem {
    public DiseaseAbility(Properties properties) {
        super(properties, 120);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 60;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        ServerScheduler.scheduleForDuration(0, 20, 20 * 80, () -> {
            if(entity.level().isClientSide)
                return;
            ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.DISEASE.get(), entity.position(), 160, 30, 0.02);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 40, entity.position(), new MobEffectInstance(MobEffects.POISON, 20, 0, false, false, false));
            AbilityUtil.damageNearbyEntities((ServerLevel) entity.level(), entity, 40, 3 * multiplier(entity), entity.position(), true, false, true, 0);
        });
    }
}

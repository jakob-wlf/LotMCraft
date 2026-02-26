package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class HolyOathAbility extends ToggleAbility {
    public HolyOathAbility(String id) {
        super(id);
    }

    @Override
    protected float getSpiritualityCost() {
        return 1.5F;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 7));
    }

    @Override
    public void start(Level level, LivingEntity entity) {

    }

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            2f
    );

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        ParticleUtil.spawnParticles((ServerLevel) level, dustOptions, entity.getEyePosition().subtract(0, entity.getEyeHeight() / 2, 0), 3, .3, .6, .3, 0);
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 1, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 1, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 2, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 6, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 1, false, false, false));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {

    }
}

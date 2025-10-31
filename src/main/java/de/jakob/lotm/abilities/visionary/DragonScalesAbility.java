package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.ToggleAbilityItem;
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

public class DragonScalesAbility extends ToggleAbilityItem {
    public DragonScalesAbility(Properties properties) {
        super(properties);
    }

    @Override
    protected float getSpiritualityCost() {
        return 2;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 6));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(255 / 255f, 216 / 255f, 138 / 255f), 1.75f);

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 2, false, false, false));
        ParticleUtil.spawnParticles((ServerLevel) level, dust, entity.getEyePosition(), 5, .45f, .8, .45f, 0);
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {

    }
}

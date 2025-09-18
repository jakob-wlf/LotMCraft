package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MidnightPoemAbility extends SelectableAbilityItem {

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), 2.5f);
    private final DustParticleOptions dustBig = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), 10f);

    public MidnightPoemAbility(Properties properties) {
        super(properties, 8f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 19;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[] {"ability.lotmcraft.midnight_poem.lullaby", "ability.lotmcraft.midnight_poem.wilt"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> lullaby(level, entity);
            case 1 -> wilt(level, entity);
        }
    }

    //TODO: Improve animation
    private void wilt(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.blockPosition(), ModSounds.MIDNIGHT_POEM.get(), entity.getSoundSource(), 1.0f, 1.0f);
        List<LivingEntity> targets = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 35);

        int duration = (int) (20 * 20 * multiplier(entity));

        targets.forEach(target -> {
            target.addEffect(new MobEffectInstance(ModEffects.ASLEEP, duration, 1, false, false, true));
        });

        ParticleUtil.spawnParticles((ServerLevel) level, dustBig, entity.getEyePosition().subtract(0, .4, 0), 800, 7, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.CRIMSON_LEAF.get(), entity.position().subtract(0, .2, 0), 500, 7, .01, 7, 0.07);

        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 20, 12 * multiplier(entity), entity.getEyePosition(), true, false);
    }

    private void lullaby(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.blockPosition(), ModSounds.MIDNIGHT_POEM.get(), entity.getSoundSource(), 1.0f, 1.0f);
        List<LivingEntity> targets = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 35);

        int duration = (int) (20 * 20 * multiplier(entity));

        targets.forEach(target -> {
            target.addEffect(new MobEffectInstance(ModEffects.ASLEEP, duration, 1, false, false, true));
        });

        ParticleUtil.spawnParticles((ServerLevel) level, dustBig, entity.getEyePosition().subtract(0, .4, 0), 800, 7, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.CRIMSON_LEAF.get(), entity.position().subtract(0, .2, 0), 500, 7, .01, 7, 0.07);

        ServerScheduler.scheduleForDuration(0, 2, duration, () -> {
            targets.forEach(target -> {
                if(target.isAlive())
                    ParticleUtil.spawnParticles((ServerLevel) level, dust, target.getEyePosition().subtract(0, .4, 0), 1, .5, 0);
            });
        }, ((ServerLevel) level));
    }
}

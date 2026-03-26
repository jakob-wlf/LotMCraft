package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
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

public class MidnightPoemAbility extends SelectableAbility {

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), 2.5f);
    private final DustParticleOptions dustBig = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), 10f);

    public MidnightPoemAbility(String id) {
        super(id, 4f);
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
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> lullaby(level, entity);
            case 1 -> wilt(level, entity);
        }
    }

    private void wilt(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.blockPosition(), ModSounds.MIDNIGHT_POEM.get(), entity.getSoundSource(), 1.0f, 1.0f);

        ParticleUtil.spawnParticles((ServerLevel) level, dustBig, entity.getEyePosition().subtract(0, .4, 0), 800, 7, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.CRIMSON_LEAF.get(), entity.position().subtract(0, .2, 0), 500, 7, .01, 7, 0.07);

        // Wilt damage is reduced by nearby light_source interactions
        Location loc = new Location(entity.position(), level);
        int seq = BeyonderData.getSequence(entity);
        float damageMult = InteractionHandler.isInteractionPossible(loc, "light_source", seq) ? 0.4f : 1f;

        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 20, DamageLookup.lookupDamage(8, 1.1) * multiplier(entity) * damageMult, entity.getEyePosition(), true, false, ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity));
    }

    private void lullaby(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.blockPosition(), ModSounds.MIDNIGHT_POEM.get(), entity.getSoundSource(), 1.0f, 1.0f);
        List<LivingEntity> targets = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 35);

        int duration = (int) (20 * 5 * multiplier(entity));

        targets.forEach(target -> {
            int actualDuration = AbilityUtil.isTargetSignificantlyStronger(entity, target) ? 35 : AbilityUtil.isTargetSignificantlyWeaker(entity, target) ? 20 * 25 : duration;
            target.addEffect(new MobEffectInstance(ModEffects.ASLEEP, actualDuration, 1, false, false, true));
        });

        ParticleUtil.spawnParticles((ServerLevel) level, dustBig, entity.getEyePosition().subtract(0, .4, 0), 800, 7, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.CRIMSON_LEAF.get(), entity.position().subtract(0, .2, 0), 500, 7, .01, 7, 0.07);

        ServerScheduler.scheduleForDuration(0, 2, duration, () -> {
            targets.forEach(target -> {
                if(target.isAlive())
                    ParticleUtil.spawnParticles((ServerLevel) level, dust, target.getEyePosition().subtract(0, .4, 0), 1, .5, 0);
            });
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }
}

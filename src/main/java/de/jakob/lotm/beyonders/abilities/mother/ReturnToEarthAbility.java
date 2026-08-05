package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ReturnToEarthAbility extends Ability {
    public ReturnToEarthAbility(String id) { super(id, 10); canBeShared = false; }
    @Override public Map<String, Integer> getRequirements() { return new HashMap<>(Map.of("mother", 3)); }
    @Override public float getSpiritualityCost() { return 420; }
    @Override public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1f, 0.7f);
        ServerScheduler.scheduleForDuration(0, 4, 60, () -> {
            AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 22).forEach(target -> {
                if (!target.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) return;
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SOUL_FIRE_FLAME, target.getEyePosition(), 8, 0.35, 0.6, 0.35, 0.03);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 4, false, false, false));
                target.hurt(target.damageSources().magic(), (float) (4.5f * multiplier(entity)));
                target.invulnerableTime = 0;
            });
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }
}

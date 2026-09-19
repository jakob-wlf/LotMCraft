package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ReturnToEarthAbility extends Ability {
    public ReturnToEarthAbility(String id) {
        super(id, 12);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("mother", 3);
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }


    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);

        boolean hasSingleTarget = target != null;
        Set<LivingEntity> targets = hasSingleTarget ?
                Set.of(target) :
                new HashSet<>(AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 20, false, true));

        int drainDuration = 20 * 10;
        double dps = DamageLookup.lookupDps(3, hasSingleTarget ? .85 : .6, 10, 30) * multiplier(entity);

        serverLevel.playSound(null, BlockPos.containing(entity.position()), SoundEvents.WITHER_SPAWN, entity.getSoundSource(), 1f, 1f);
        for(LivingEntity e : targets) BeyonderData.addModifierWithTimeLimit(e, "return_to_earth", .8, drainDuration * 500);

        AtomicInteger tickCounter = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(0, 1, drainDuration, () -> {
            for(LivingEntity e : targets) {
                if(tickCounter.get() % 10 == 0) {
                    e.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.MOTHER_GENERIC), (float) dps);
                    serverLevel.playSound(null, BlockPos.containing(e.position()), SoundEvents.WITHER_SHOOT, e.getSoundSource(), .5f, .5f);
                    if(tickCounter.get() % 20 == 0) {
                        e.teleportRelative(0, -.001, 0);
                    }
                }

                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 20, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 20, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 20, false, false, false));

                ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.SOUL, e.getEyePosition(), 2, 25);
                ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.SOUL, e.getEyePosition().subtract(0, .75, 0), 2, 25);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.LARGE_SMOKE, e.position().add(0, e.getEyeHeight() / 2, 0), 40, .3, .8, .3, 0);
            }

            tickCounter.incrementAndGet();
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }
}

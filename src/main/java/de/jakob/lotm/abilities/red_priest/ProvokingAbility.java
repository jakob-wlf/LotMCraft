package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProvokingAbility extends AbilityItem {
    public ProvokingAbility(Properties properties) {
        super(properties, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 15;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        Vec3 pos = entity.getEyePosition();

        if(!level.isClientSide) {
            List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, pos, 18);
            nearbyEntities.forEach(e -> {
                if(e instanceof Mob mob)
                    mob.setTarget(entity);

                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 6, 1, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 6, 1, false, false, false));
                e.hurt(entity.damageSources().mobAttack(entity), 6 * (float) multiplier(entity));
            });
        }
        else {
            entity.playSound(SoundEvents.PILLAGER_AMBIENT, 1.0f, 0.6f + (float) Math.random() * 0.4f);
            entity.playSound(SoundEvents.WOLF_GROWL, 1.0f, 1);
            List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ClientLevel) level, pos, 18);

            nearbyEntities.forEach(e -> {
                ParticleUtil.spawnParticles((ClientLevel) level, ParticleTypes.ANGRY_VILLAGER, e.getEyePosition().add(0, .5, 0), 10, 0.8, 0);
                ParticleUtil.spawnParticles((ClientLevel) level, ParticleTypes.LARGE_SMOKE, e.getEyePosition().add(0, .5, 0), 5, 0.8, 0);
            });

        }
    }
}

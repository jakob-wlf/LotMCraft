package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class RagingBlowsAbility extends Ability {
    public RagingBlowsAbility(String id) {
        super(id, 1.2f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 8));
    }

    @Override
    public float getSpiritualityCost() {
        return 14;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            ServerScheduler.scheduleForDuration(0, 6, 6 * 9, () -> {
                Vec3 pos = VectorUtil.getRelativePosition(entity.getEyePosition(), entity.getLookAngle().normalize(), random.nextDouble(1, 2), random.nextDouble(-1.5, 1.5), random.nextDouble(-.5, .5));
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.POOF, pos, 4, 0, 0.125);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, pos, 10, 0, 0.125);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CRIT, pos, 13, 0, 0.115);

                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE.value(), entity.getSoundSource(), 1, 1);

                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.75f, DamageLookup.lookupDamage(8, .8) * multiplier(entity), pos, true, false, true, 0);
            }, (ServerLevel) level);
        }
    }
}

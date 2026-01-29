package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class MightyBlowAbility extends Ability {

    public MightyBlowAbility(String id) {
        super(id, 2.5f);

        hasOptimalDistance = true;
        optimalDistance = 1.5f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 9));
    }

    @Override
    public float getSpiritualityCost() {
        return 15;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 pos = VectorUtil.getRelativePosition(entity.getEyePosition(), entity.getLookAngle().normalize(), 1.5, 0, -.15);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.POOF, pos, 10, 0, 0.15);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, pos, 30, 0, 0.15);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CRIT, pos, 60, 0, 0.325);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.EXPLOSION, pos, 1, 0, 0.115);

        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 3.5f, DamageLookup.lookupDamage(9, 1.5) * (float) multiplier(entity), pos, true, false, true, 0);

        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE.value(), entity.getSoundSource(), 1, 1);
    }

}

package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class CleaveOfPurificationAbility extends Ability {
    public CleaveOfPurificationAbility(String id) {
        super(id, .8f);

        hasOptimalDistance = true;
        optimalDistance = 1f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        reqs.put("sun", 7);
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 20;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.getEyePosition().subtract(0, .2, 0).add(entity.getLookAngle().normalize());
        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 2.0f, .5f);

        ParticleUtil.drawParticleLine(
                (ServerLevel) level,
                ParticleTypes.END_ROD,
                startPos,
                entity.getLookAngle().normalize(),
                2,
                .5, 8, .4
        );

        ParticleUtil.drawParticleLine(
                (ServerLevel) level,
                ModParticles.HOLY_FLAME.get(),
                startPos,
                entity.getLookAngle().normalize(),
                2,
                .5, 32, .4
        );

        ParticleUtil.drawParticleLine(
                (ServerLevel) level,
                ParticleTypes.FIREWORK,
                startPos,
                entity.getLookAngle().normalize(),
                2,
                .5, 12, .4
        );

        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.75, DamageLookup.lookupDamage(7, .9) * multiplier(entity), startPos, true, false, true, 0);

        BlockState block = level.getBlockState(BlockPos.containing(startPos));
        if(block.isAir()) {
            level.setBlockAndUpdate(BlockPos.containing(startPos), Blocks.LIGHT.defaultBlockState());
        }

        ServerScheduler.scheduleDelayed(25, () -> level.setBlockAndUpdate(BlockPos.containing(startPos), Blocks.AIR.defaultBlockState()));
    }
}

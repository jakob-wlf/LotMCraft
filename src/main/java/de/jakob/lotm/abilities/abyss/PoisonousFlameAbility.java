package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class PoisonousFlameAbility extends AbilityItem {
    public PoisonousFlameAbility(Properties properties) {
        super(properties, .8f);

        hasOptimalDistance = true;
        optimalDistance = 1.75f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        reqs.put("abyss", 8);
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 12;
    }

    private final DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(35 / 255f, 168 / 255f, 102 / 255f), 1f
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.getEyePosition().subtract(0, .2, 0).add(entity.getLookAngle().normalize());
        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 2.0f, .5f);

        ParticleUtil.drawParticleLine(
                (ServerLevel) level,
                ParticleTypes.SMOKE,
                startPos,
                entity.getLookAngle().normalize(),
                2,
                .5, 23, .4
        );

        ParticleUtil.drawParticleLine(
                (ServerLevel) level,
                ModParticles.GREEN_FLAME.get(),
                startPos,
                entity.getLookAngle().normalize(),
                2,
                .5, 35, .4
        );

        ParticleUtil.drawParticleLine(
                (ServerLevel) level,
                dustOptions,
                startPos,
                entity.getLookAngle().normalize(),
                2,
                .5, 23, .4
        );

        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.75, DamageLookup.lookupDamage(8, .9) * multiplier(entity), startPos, true, false, true, 0, 20 * 3);
        AbilityUtil.addPotionEffectToNearbyEntities(
                (ServerLevel) level, entity, 3, startPos, new MobEffectInstance(MobEffects.POISON, 20 * 8, 2, false, true)
        );

        BlockState block = level.getBlockState(BlockPos.containing(startPos));
        if(block.isAir()) {
            level.setBlockAndUpdate(BlockPos.containing(startPos), Blocks.LIGHT.defaultBlockState());
        }

        ServerScheduler.scheduleDelayed(25, () -> level.setBlockAndUpdate(BlockPos.containing(startPos), Blocks.AIR.defaultBlockState()));
    }
}

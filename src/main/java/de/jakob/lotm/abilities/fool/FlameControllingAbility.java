package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class FlameControllingAbility extends AbilityItem {
    public FlameControllingAbility(Properties properties) {
        super(properties, .75f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 20;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(-.65, .65), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 10, 1.4f).subtract(startPos).normalize();

        AtomicReference<Vec3> currentPos = new AtomicReference<>(startPos);

        AtomicBoolean hasHit = new AtomicBoolean(false);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 20, () -> {
            if(hasHit.get())
                return;

            Vec3 pos = currentPos.get();

            if(AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5f, DamageLookup.lookupDamage(7, .83) * (float) multiplier(entity), pos, true, false, true, 0, 20 * 5)) {
                hasHit.set(true);
                return;
            }

            if(!level.getBlockState(BlockPos.containing(pos.x, pos.y, pos.z)).isAir()) {
                if(BeyonderData.isGriefingEnabled(entity)) {
                    pos = pos.subtract(direction);
                    level.setBlockAndUpdate(BlockPos.containing(pos.x, pos.y, pos.z), Blocks.FIRE.defaultBlockState());
                }
                hasHit.set(true);
                return;
            }

            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, pos, 20, 0.27, 0.02);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SMOKE, pos, 12, 0.27, 0.02);

            currentPos.set(pos.add(direction));
        }, (ServerLevel) level);
    }
}

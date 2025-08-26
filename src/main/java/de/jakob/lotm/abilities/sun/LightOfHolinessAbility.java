package de.jakob.lotm.abilities.sun;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LightOfHolinessAbility extends AbilityItem {
    public LightOfHolinessAbility(Properties properties) {
        super(properties, .9f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        reqs.put("sun", 5);
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 50;
    }

    final int radius = 16;

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            2.25f
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        Vec3 initialPos = AbilityUtil.getTargetLocation(entity, radius, .75f).add(0, 14, 0);

        List<BlockPos> lights = new ArrayList<>();

        if (!level.isClientSide) {
            AtomicReference<Vec3> currentPos = new AtomicReference<>(initialPos);
            AtomicBoolean hasHitBlock = new AtomicBoolean(false);

            level.playSound(null, initialPos.x, initialPos.y - 14, initialPos.z, SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 3.0f, 1.0f);

            ServerScheduler.scheduleForDuration(0, 1, 22, () -> {
                Vec3 pos = currentPos.get();

                BlockPos blockPos = BlockPos.containing(pos);
                //Set the light blocks
                if (level.getBlockState(blockPos).isAir()) {
                    level.setBlockAndUpdate(blockPos, Blocks.LIGHT.defaultBlockState());
                    lights.add(blockPos);
                }
                //Animation when hit block
                else {
                    if(!hasHitBlock.get()) {
                        hasHitBlock.set(true);
                        AtomicDouble i = new AtomicDouble(2);
                        ServerScheduler.scheduleForDuration(0, 1, 12, () -> {
                            Vec3 particlePos = currentPos.get().add(0, 1, 0);

                            ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.END_ROD, particlePos, i.get(), (int) Math.round(12 * i.get()));
                            ParticleUtil.spawnCircleParticles((ServerLevel) level, dustOptions, particlePos, i.get(), (int) Math.round(12 * i.get()));
                            ParticleUtil.spawnCircleParticles((ServerLevel) level, ModParticles.HOLY_FLAME.get(), particlePos, i.get(), (int) Math.round(12 * i.get()));
                            i.addAndGet(.5);
                        }, (ServerLevel) level);
                    }
                    return;
                }

                ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.FIREWORK, pos, 2, 16);
                ParticleUtil.spawnCircleParticles((ServerLevel) level, dustOptions, pos, 2, 50);
                ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.END_ROD, pos, 2, 40);
                ParticleUtil.spawnCircleParticles((ServerLevel) level, ModParticles.HOLY_FLAME.get(), pos, 2, 50);

                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 4.35f, 26 * multiplier(entity), pos, true, false, false, 10);

                currentPos.set(pos.subtract(0, 1, 0));
            }, (ServerLevel) level);

            ServerScheduler.scheduleDelayed(40, () -> {
                lights.forEach(l -> level.setBlockAndUpdate(l, Blocks.AIR.defaultBlockState()));
            }, (ServerLevel) level);
        }
    }
}

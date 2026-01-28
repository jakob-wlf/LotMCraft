package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class IlluminateAbility extends Ability {
    public IlluminateAbility(String id) {
        super(id, .25f);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        reqs.put("sun", 8);
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 12;
    }

    final int radius = 16;
    final int duration = 20 * 25;

    final Vec3 eastFacing = new Vec3(1, 0, 0);
    final Vec3 southFacing = new Vec3(0, 0, 1);

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            5f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        BlockPos targetBlock = AbilityUtil.getTargetBlock(entity, radius);

        if (!level.isClientSide) {
            BlockState lightBlock = Blocks.LIGHT.defaultBlockState();
            level.setBlock(targetBlock, lightBlock, 3);

            ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), 1, duration, 10, 7);
            ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), southFacing, 1, duration, 10, 7);
            ParticleUtil.spawnCircleParticlesForDuration((ServerLevel) level, ParticleTypes.END_ROD, targetBlock.getCenter(), eastFacing, 1, duration, 10, 7);
            ParticleUtil.spawnParticlesForDuration((ServerLevel) level, dustOptions, targetBlock.getCenter(), duration, 10, random.nextInt(1, 6), .9);

            ServerScheduler.scheduleDelayed(duration, () -> {
                if (level.getBlockState(targetBlock).is(Blocks.LIGHT)) {
                    level.setBlock(targetBlock, Blocks.AIR.defaultBlockState(), 3);
                }
            }, (ServerLevel) level);
        }
    }
}

package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.BeyonderData;
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

public class FlaringSunAbility extends AbilityItem {
    public FlaringSunAbility(Properties properties) {
        super(properties, 8);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(1f, 185 / 255f, 3 / 255f), 4f);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 25, 2);
        Vec3 startPos = targetPos.add(0, 3, 0);

        BlockPos blockPos = BlockPos.containing(startPos);
        BlockState state = level.getBlockState(blockPos);
        if(state.getCollisionShape(level, blockPos).isEmpty()) {
            level.setBlockAndUpdate(blockPos, Blocks.LIGHT.defaultBlockState());
        }

        if(BeyonderData.isGriefingEnabled(entity)) {
            AbilityUtil.getBlocksInSphereRadius((ServerLevel) level, targetPos, 7, true, true, false).forEach(
                    b -> level.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState())
            );

            AbilityUtil.getBlocksInSphereRadius((ServerLevel) level, targetPos, 7, true).forEach(
                    b -> level.setBlockAndUpdate(b, Blocks.FIRE.defaultBlockState())
            );

            AbilityUtil.getBlocksInSphereRadius((ServerLevel) level, targetPos, 8, true, true, false).forEach(
                    b -> level.setBlockAndUpdate(b, Blocks.BASALT.defaultBlockState())
            );
        }

        ServerScheduler.scheduleForDuration(0, 4, 20 * 19, () -> {
            ParticleUtil.spawnSphereParticles((ServerLevel) level, dust, startPos, 4.5f, 92);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.FLAME, startPos, 4.75f, 92);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.END_ROD, startPos, 4.75f, 65);

            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 17, 10 * multiplier(entity), targetPos, true, false, 20 * 4);
        }, () -> {
            if(level.getBlockState(blockPos).getBlock() == Blocks.LIGHT) {
                level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
        }, (ServerLevel) level);
    }
}

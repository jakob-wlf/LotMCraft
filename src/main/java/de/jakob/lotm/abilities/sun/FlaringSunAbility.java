package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.SunEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class FlaringSunAbility extends Ability {
    public FlaringSunAbility(String id) {
        super(id, 8);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
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

        SunEntity sun = new SunEntity(ModEntities.SUN.get(), level);
        sun.setPos(startPos);
        level.addFreshEntity(sun);

        ServerScheduler.scheduleForDuration(0, 4, 20 * 19, () -> {
            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.FLAME, startPos, 4.75f, 200);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.END_ROD, startPos, 4.75f, 180);

            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 17, DamageLookup.lookupDps(4, .85, 4, 20) * multiplier(entity), targetPos, true, false, 20 * 4);
        }, () -> {
            if(level.getBlockState(blockPos).getBlock() == Blocks.LIGHT) {
                level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }

            sun.discard();
        }, (ServerLevel) level);
    }
}

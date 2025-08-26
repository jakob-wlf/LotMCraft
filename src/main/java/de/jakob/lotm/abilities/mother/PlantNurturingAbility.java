package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class PlantNurturingAbility extends AbilityItem {
    public PlantNurturingAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "mother", 9
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 5,1);
        level.playSound(null, entity, Blocks.GRASS_BLOCK.getSoundType(Blocks.GRASS_BLOCK.defaultBlockState(), level, BlockPos.containing(entity.position().x, entity.position().y, entity.position().z), null).getBreakSound(), SoundSource.BLOCKS, 5,1);

        AbilityUtil.getBlocksInCircle((ServerLevel) level, entity.position().subtract(0, 1, 0), 4.5, 25).forEach(b -> {
            BlockState blockState = level.getBlockState(b);

            if(blockState.getBlock() instanceof BonemealableBlock bonemealableBlock) {
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.HAPPY_VILLAGER, b.getCenter().add(0, 1, 0), 4, .2);
                bonemealableBlock.performBonemeal((ServerLevel) level, RandomSource.create(), b, blockState);
                bonemealableBlock.performBonemeal((ServerLevel) level, RandomSource.create(), b, blockState);
            }
        });

        AbilityUtil.getBlocksInCircle((ServerLevel) level, entity.position(), 4.5, 25).forEach(b -> {
            BlockState blockState = level.getBlockState(b);
            if(blockState.getBlock() instanceof BonemealableBlock bonemealableBlock) {
                bonemealableBlock.performBonemeal((ServerLevel) level, RandomSource.create(), b, blockState);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.HAPPY_VILLAGER, b.getCenter().add(0, 1, 0), 4, .2);
            }
        });

        AbilityUtil.getBlocksInCircle((ServerLevel) level, entity.position().add(0, 1, 0), 4.5, 25).forEach(b -> {
            BlockState blockState = level.getBlockState(b);
            if(blockState.getBlock() instanceof BonemealableBlock bonemealableBlock) {
                bonemealableBlock.performBonemeal((ServerLevel) level, RandomSource.create(), b, blockState);
                bonemealableBlock.performBonemeal((ServerLevel) level, RandomSource.create(), b, blockState);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.HAPPY_VILLAGER, b.getCenter().add(0, 1, 0), 4, .2);

            }
        });
    }
}

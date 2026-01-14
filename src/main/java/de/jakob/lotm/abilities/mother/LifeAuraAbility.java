package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class LifeAuraAbility extends ToggleAbilityItem {

    public LifeAuraAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 3;
    }

    @Override
    protected void start(Level level, LivingEntity entity) {

    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AbilityUtil.getBlocksInEllipsoid(serverLevel, entity.position(), 30, 7, true, false, false).forEach(blockPos -> {
            BlockState blockState = level.getBlockState(blockPos);
            applyBonemeal(serverLevel, blockPos, blockState, BeyonderData.isGriefingEnabled(entity));
        });
    }

    private void applyBonemeal(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, boolean shouldBonemealGrass) {
        if(blockState.is(Blocks.GRASS_BLOCK) && !shouldBonemealGrass) {
            return; // I don't want grass to grow everywhere when griefing is not enabled :P
        }

        if(blockState.is(Blocks.SHORT_GRASS)) {
            return; // It looks weird if all grass is tall
        }

        if(!(blockState.getBlock() instanceof BonemealableBlock bonemealableBlock)) {
            if(blockState.is(Blocks.SUGAR_CANE)) {
                tryGrowSugarCane(serverLevel, blockPos);
            }
            return;
        }

        if(blockState.is(Blocks.COCOA) && blockState.getValue(CocoaBlock.AGE) >= 2) {
            return; // Cocoa is fully grown and will crash if we try to bonemeal it
        }

        if(bonemealableBlock.isBonemealSuccess(serverLevel, serverLevel.random, blockPos, blockState)) {
            bonemealableBlock.performBonemeal(serverLevel, serverLevel.random, blockPos, blockState);
        }
    }

    private void tryGrowSugarCane(ServerLevel level, BlockPos pos) {
        if (level.isEmptyBlock(pos.above())) {

            int height = level.getRandom().nextInt(3) > 0 ? 1 : 2;
            for (int i = 1; i <= height; i++) {
                if (level.isEmptyBlock(pos.above(i))) {
                    level.setBlock(pos.above(i), Blocks.SUGAR_CANE.defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
    }
}

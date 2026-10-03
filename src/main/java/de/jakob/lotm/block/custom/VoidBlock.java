package de.jakob.lotm.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class VoidBlock extends Block {

    public VoidBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide) {
            entity.hurt(level.damageSources().fellOutOfWorld(), 55.0F);
        }
        super.entityInside(state, level, pos, entity);
    }
}

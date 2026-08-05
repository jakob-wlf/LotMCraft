package de.jakob.lotm.block.custom;

import de.jakob.lotm.beyonders.sefirah.KeyOfLightEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class KeyOfLightStatueBlock extends Block {
    public KeyOfLightStatueBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            KeyOfLightEventHandler.tryStartAlignment(serverPlayer, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
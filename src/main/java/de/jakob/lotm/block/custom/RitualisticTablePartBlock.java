package de.jakob.lotm.block.custom;

import com.mojang.serialization.MapCodec;
import de.jakob.lotm.block.entity.RitualisticTablePartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RitualisticTablePartBlock extends BaseEntityBlock {

    public static final MapCodec<RitualisticTablePartBlock> CODEC = simpleCodec(RitualisticTablePartBlock::new);

    public RitualisticTablePartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new RitualisticTablePartBlockEntity(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof RitualisticTablePartBlockEntity part) {
            BlockPos mainPos = part.getMainPos();
            if (mainPos != null) {
                BlockState mainState = level.getBlockState(mainPos);
                if (mainState.getBlock() instanceof RitualisticTableBlock mainBlock) {
                    return mainBlock.useItemOn(stack, mainState, level, mainPos, player, hand, hit);
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RitualisticTablePartBlockEntity part) {
            BlockPos mainPos = part.getMainPos();
            if (mainPos != null && !mainPos.equals(pos)
                    && level.getBlockState(mainPos).getBlock() instanceof RitualisticTableBlock) {
                level.destroyBlock(mainPos, true, player);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }
}
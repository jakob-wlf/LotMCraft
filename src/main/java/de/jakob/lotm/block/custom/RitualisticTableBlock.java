package de.jakob.lotm.block.custom;

import com.mojang.serialization.MapCodec;
import de.jakob.lotm.block.entity.ModBlocks;
import de.jakob.lotm.block.entity.RitualisticTableBlockEntity;
import de.jakob.lotm.block.entity.RitualisticTablePartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RitualisticTableBlock extends BaseEntityBlock {

    public static final MapCodec<RitualisticTableBlock> CODEC = simpleCodec(RitualisticTableBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final List<BlockPos> OCCUPIED_OFFSETS_NORTH = List.of(
            new BlockPos(-1, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 1),
            new BlockPos(0, 0, 1),
            new BlockPos(1, 0, 1)
            // NOTE: (0,0,0) is the anchor/main block itself - don't include it here.
    );

    public RitualisticTableBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new RitualisticTableBlockEntity(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Rotates OCCUPIED_OFFSETS_NORTH so it matches the given facing, then returns
     * the resulting absolute BlockPos list relative to the anchor position.
     */
    private static List<BlockPos> getOccupiedOffsets(Direction facing) {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos offset : OCCUPIED_OFFSETS_NORTH) {
            result.add(rotateOffset(offset, facing));
        }
        return result;
    }

    private static BlockPos rotateOffset(BlockPos offset, Direction facing) {
        int x = offset.getX();
        int z = offset.getZ();
        return switch (facing) {
            case NORTH -> new BlockPos(x, offset.getY(), z);
            case SOUTH -> new BlockPos(-x, offset.getY(), -z);
            case WEST -> new BlockPos(z, offset.getY(), -x);
            case EAST -> new BlockPos(-z, offset.getY(), x);
            default -> offset;
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide()) return;

        Direction facing = state.getValue(FACING);
        for (BlockPos offset : getOccupiedOffsets(facing)) {
            BlockPos partPos = pos.offset(offset);

            // Don't stomp on something already there (e.g. if the footprint would
            // overlap an existing block); a full mod would pre-validate this in
            // canSurvive()/getStateForPlacement() before allowing placement at all.
            if (!level.getBlockState(partPos).canBeReplaced()) {
                continue;
            }

            level.setBlock(partPos, ModBlocks.RITUALISTIC_TABLE_PART.get().defaultBlockState(), 3);
            if (level.getBlockEntity(partPos) instanceof RitualisticTablePartBlockEntity part) {
                part.setMainPos(pos);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        for (BlockPos offset : getOccupiedOffsets(facing)) {
            BlockPos partPos = pos.offset(offset);
            BlockState partState = level.getBlockState(partPos);

            if (!partState.canBeReplaced() && !partState.is(ModBlocks.RITUALISTIC_TABLE_PART.get())) {
                return false;
            }
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pPlayer.isSpectator()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof RitualisticTableBlockEntity ritualisticTableBlockEntity) {
                pPlayer.openMenu(new SimpleMenuProvider(ritualisticTableBlockEntity, Component.literal("Ritualistic Table")), pPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof RitualisticTableBlockEntity ritualisticTableBlockEntity) {
                ritualisticTableBlockEntity.drops();
            }

            // Clean up the part blocks that belong to this table.
            if (!pLevel.isClientSide()) {
                Direction facing = pState.getValue(FACING);
                for (BlockPos offset : getOccupiedOffsets(facing)) {
                    BlockPos partPos = pPos.offset(offset);
                    if (pLevel.getBlockState(partPos).is(ModBlocks.RITUALISTIC_TABLE_PART.get())) {
                        pLevel.removeBlock(partPos, false);
                    }
                }
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
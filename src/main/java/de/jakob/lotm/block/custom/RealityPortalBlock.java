package de.jakob.lotm.block.custom;

import de.jakob.lotm.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RealityPortalBlock extends Block {


    public RealityPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level,
                                                 BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) return;
        if (!(entity instanceof LivingEntity living)) return;

        ServerLevel serverLevel = (ServerLevel) level;

        if (entity.isOnPortalCooldown()) return;

        ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        double targetX = entity.getX();
        double targetZ = entity.getZ();
        double targetY = findHighestSolidGround(overworld, (int) Math.floor(targetX), (int) Math.floor(targetZ));

        entity.setPortalCooldown();

        if (entity instanceof ServerPlayer player) {
            player.teleportTo(overworld, targetX, targetY, targetZ,
                    player.getYRot(), player.getXRot());
        } else {
            entity.teleportTo(overworld, targetX, targetY, targetZ, Set.of(), entity.getYRot(), entity.getXRot());
        }
    }

    private double findHighestSolidGround(ServerLevel level, int x, int z) {
        BlockPos startPos = new BlockPos(x, level.getMaxBuildHeight() - 1, z);
        while (startPos.getY() > level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(startPos);
            if (state.isSolidRender(level, startPos)) {
                return startPos.getY() + 1;
            }
            startPos = startPos.below();
        }
        return level.getMinBuildHeight();
    }
}

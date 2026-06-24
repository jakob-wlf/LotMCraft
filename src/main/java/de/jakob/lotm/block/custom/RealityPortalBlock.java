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

    private static final int TELEPORT_COOLDOWN_TICKS = 40;

    public RealityPortalBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .noCollission()               // passable
                .noOcclusion()                // don't cull neighbour faces
                .lightLevel(state -> 6)       // faint glow (level 6 / 15)
                .strength(-1.0f, 3600000.0f)  // unbreakable like bedrock but not indestructible
                .pushReaction(PushReaction.BLOCK)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false));
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

        if (!serverLevel.dimension().equals(ModDimensions.SPACE_TIME_LABYRINTH_LEVEL_KEY)) return;

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
        int highestY = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(x, level.getMaxBuildHeight(), z)).getY();
        return highestY + 1.0;
    }
}

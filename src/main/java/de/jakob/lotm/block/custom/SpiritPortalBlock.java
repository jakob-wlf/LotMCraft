package de.jakob.lotm.block.custom;

import de.jakob.lotm.dimension.spirit_world.spirit_patch.SpiritPortalTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpiritPortalBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(0, 0, 6, 16, 16, 10);

    public SpiritPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (entity.isPassenger() || entity.isVehicle() || !entity.canUsePortal(false)) return;

        if (entity.isOnPortalCooldown()) {
            entity.setPortalCooldown();
            return;
        }
        SpiritPortalTeleporter.use(serverLevel, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) return;
        level.addParticle(ParticleTypes.PORTAL,
                pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + 0.35 + random.nextDouble() * 0.3,
                (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3);
    }
}

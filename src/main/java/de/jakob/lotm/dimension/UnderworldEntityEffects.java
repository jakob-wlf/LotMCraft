package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class UnderworldEntityEffects {

    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer entity)) {
            return;
        }

        if (!entity.level().dimension().equals(ModDimensions.UNDERWORLD_DIMENSION)) {
            return;
        }

        if(entity.level().getBlockState(entity.blockPosition()).is(Blocks.WATER)) {
            if(entity.level().random.nextInt(80) == 0) {
                travelThroughGate((ServerLevel) entity.level(), entity);
            } else {
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 3, false, false));
            }
        }
    }

    private static void travelThroughGate(ServerLevel level, ServerPlayer player) {
        ServerLevel target = level.getServer().getLevel(Level.OVERWORLD);
        if (target == null) return;

        int x = Mth.floor(player.getX());
        int z = Mth.floor(player.getZ());

        int minY = target.getMinBuildHeight() + 1;
        int maxY = target.getMaxBuildHeight() - 1 - 3;

        BlockPos dest = findSafeSpot(target, x, z, minY, maxY, Mth.floor(player.getY()));

        level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.6f, 1.2f);
        player.teleportTo(target, dest.getX() + .5, dest.getY(), dest.getZ() + .5, player.getYRot(), player.getXRot());
        player.resetFallDistance();
        target.playSound(null, dest, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.6f, 1.2f);
    }

    private static BlockPos findSafeSpot(ServerLevel level, int x, int z, int minY, int maxY, int preferredY) {
        int pref = Mth.clamp(preferredY, minY, maxY);
        int range = Math.max(pref - minY, maxY - pref);
        boolean anyNonSolid = false;

        for (int offset = 0; offset <= range; offset++) {
            for (int side = 0; side < 2; side++) {
                if (offset == 0 && side == 1) continue;

                int y = side == 0 ? pref + offset : pref - offset;
                if (y < minY || y > maxY) continue;

                BlockPos feet = new BlockPos(x, y, z);
                if (!isFree(level, feet)) {
                    BlockState state = level.getBlockState(feet);
                    if (!state.getFluidState().isEmpty() || state.getCollisionShape(level, feet).isEmpty()) {
                        anyNonSolid = true;
                    }
                    continue;
                }
                anyNonSolid = true;

                BlockPos floor = feet.below();
                BlockState floorState = level.getBlockState(floor);
                if (isFree(level, feet.above())
                        && floorState.getFluidState().isEmpty()
                        && floorState.isFaceSturdy(level, floor, Direction.UP)) {
                    return feet;
                }
            }
        }

        BlockPos feet = new BlockPos(x, pref, z);

        if (!anyNonSolid) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    for (int dy = 0; dy < 3; dy++) {
                        level.setBlockAndUpdate(feet.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        } else {
            level.setBlockAndUpdate(feet.below(), Blocks.DEEPSLATE.defaultBlockState());
            level.setBlockAndUpdate(feet, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(feet.above(), Blocks.AIR.defaultBlockState());
        }
        return feet;
    }

    private static boolean isFree(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty() && state.getCollisionShape(level, pos).isEmpty();
    }
}
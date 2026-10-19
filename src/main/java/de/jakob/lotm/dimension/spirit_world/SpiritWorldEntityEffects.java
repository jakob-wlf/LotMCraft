package de.jakob.lotm.dimension.spirit_world;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
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
public class SpiritWorldEntityEffects {
    
    private static final Map<UUID, Long> effectCooldowns = new HashMap<>();
    
    private static final float EFFECT_CHECK_CHANCE = 0.02f;
    private static final int MIN_COOLDOWN = 100;
    private static final int MAX_COOLDOWN = 400;
    
    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Pre event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!entity.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            return;
        }

        Holder<Biome> biome = entity.level().getBiome(entity.blockPosition());
        if(biome.is(ModDimensions.SPIRIT_BIOME_UNDERWORLD_PATCH) && entity.level().getBlockState(entity.blockPosition()).is(Blocks.WATER)) {
            travelThroughGate((ServerLevel) entity.level(), (ServerPlayer) entity);
        }



        UUID entityId = entity.getUUID();
        long currentTime = entity.level().getGameTime();
        
        if (effectCooldowns.containsKey(entityId)) {
            long cooldownEnd = effectCooldowns.get(entityId);
            if (currentTime < cooldownEnd) {
                return;
            }
        }

        if(BeyonderData.getSequence(entity) <= 2) return;
        
        if (entity.getRandom().nextFloat() > EFFECT_CHECK_CHANCE) {
            return;
        }
        
        int effectChoice = entity.getRandom().nextInt(100);
        
        if (effectChoice < 70) {
            int duration = 60;
            
            entity.addEffect(new MobEffectInstance(
                MobEffects.SLOW_FALLING,
                duration,
                0,
                false,
                true,
                true
            ));
            
            int cooldown = MIN_COOLDOWN + entity.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
            effectCooldowns.put(entityId, currentTime + cooldown);
            
        } else {
            int duration = 120;
            int amplifier = 1 + entity.getRandom().nextInt(3);
            
            entity.addEffect(new MobEffectInstance(
                MobEffects.JUMP,
                duration,
                amplifier,
                false,
                true,
                true
            ));
            
            int cooldown = MIN_COOLDOWN + entity.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
            effectCooldowns.put(entityId, currentTime + cooldown);
        }
    }

    private static void travelThroughGate(ServerLevel level, ServerPlayer player) {
        boolean inUnderworld = level.dimension() == ModDimensions.UNDERWORLD_DIMENSION;
        ServerLevel target = level.getServer().getLevel(inUnderworld ? Level.OVERWORLD : ModDimensions.UNDERWORLD_DIMENSION);
        if (target == null) return;

        int x = Mth.floor(player.getX());
        int z = Mth.floor(player.getZ());

        int minY = inUnderworld ? target.getMinBuildHeight() + 1 : 70;
        int maxY = inUnderworld ? target.getMaxBuildHeight() - 1 - 3 : 120;

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
    
    @SubscribeEvent
    public static void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        effectCooldowns.remove(event.getEntity().getUUID());
    }
}
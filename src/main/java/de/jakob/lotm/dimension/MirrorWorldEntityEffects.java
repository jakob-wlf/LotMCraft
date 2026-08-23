package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.demoness.handlers.GlassScanJob;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MirrorWorldEntityEffects {

    private static final int CHECK_INTERVAL_TICKS = 25;

    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(ModDimensions.MIRROR_WORLD_DIMENSION_KEY)) return;

        ServerLevel overWorldLevel = serverLevel.getServer().getLevel(Level.OVERWORLD);
        if(overWorldLevel == null) {
            serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
            return;
        }

        if (entity.tickCount % CHECK_INTERVAL_TICKS == 0) {
            ServerScheduler.scheduleDelayed(1, () -> {
                Vec3 searchCenter = MirrorGateManager.getCoordinatesInOverworld(entity.blockPosition(), serverLevel).getCenter();

                new GlassScanJob(overWorldLevel, searchCenter, 40, nearestGlassBlock -> {
                    if (nearestGlassBlock == null) {
                        return;
                    }

                    Vec3 mirrorWorldPos = MirrorGateManager.getCoordinatesInMirrorWorld(nearestGlassBlock, serverLevel).getCenter();
                    MirrorGateManager.createMirrorGate(serverLevel, BlockPos.containing(mirrorWorldPos), overWorldLevel, nearestGlassBlock);
                    serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
                }).start();
            });
        }
    }
}
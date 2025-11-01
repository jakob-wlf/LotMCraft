package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PetrificationSyncHandler {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()) {
            if (living.hasEffect(ModEffects.PETRIFICATION)) {
                if (living.tickCount % 10 == 0) {
                    ServerLevel serverLevel = (ServerLevel) living.level();
                    for (ServerPlayer player : serverLevel.players()) {
                        if (player.distanceToSqr(living) < 4096) {
                            living.getActiveEffects().forEach(effectInstance -> {
                                if (effectInstance.getEffect().value() == ModEffects.PETRIFICATION.value()) {
                                    player.connection.send(
                                        new ClientboundUpdateMobEffectPacket(
                                            living.getId(), effectInstance, false
                                        )
                                    );
                                }
                            });
                        }
                    }
                }
            }
        }
    }
}
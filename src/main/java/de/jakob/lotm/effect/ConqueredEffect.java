package de.jakob.lotm.effect;

import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;

public class ConqueredEffect extends MobEffect {
    protected ConqueredEffect(MobEffectCategory category, int color) {
        super(category, color);
    }


    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if( livingEntity.level().isClientSide()) {
            return true;
        }

        livingEntity.setDeltaMovement(new Vec3(0, 0, 0));
        livingEntity.hurtMarked = true;

        if(livingEntity.getHealth() > 1)
            livingEntity.setHealth(1.0F);

        BeyonderData.disableAbilityUse(livingEntity, "conquered");
        ServerScheduler.scheduleDelayed(20 * 2, () -> {
            if(!livingEntity.hasEffect(ModEffects.CONQUERED)) {
                BeyonderData.enableAbilityUse(livingEntity, "conquered");
            }
        });

        if(livingEntity.tickCount % 10 == 0) {
            ServerLevel serverLevel = (ServerLevel) livingEntity.level();
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(livingEntity) < 4096) {
                    MobEffectInstance effectInstance = livingEntity.getEffect(ModEffects.CONQUERED);
                    if (effectInstance != null) {
                        player.connection.send(new ClientboundUpdateMobEffectPacket(livingEntity.getId(), effectInstance, false));
                    }
                }
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        event.getEffect();
        if (event.getEffect() != ModEffects.CONQUERED) {
            return;
        }

        LivingEntity livingEntity = event.getEntity();
        sendRemoveEffectPacket(livingEntity);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null || event.getEffectInstance().getEffect().value() != ModEffects.CONQUERED.value()) {
            return;
        }

        LivingEntity livingEntity = event.getEntity();
        sendRemoveEffectPacket(livingEntity);
    }

    private static void sendRemoveEffectPacket(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) livingEntity.level();
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(livingEntity) < 4096) {
                    player.connection.send(new ClientboundRemoveMobEffectPacket(
                            livingEntity.getId(),
                            ModEffects.CONQUERED
                    ));
                }
            }
        }
    }





    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}

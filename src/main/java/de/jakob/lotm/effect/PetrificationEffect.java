package de.jakob.lotm.effect;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PetrificationEffect extends MobEffect {

    protected PetrificationEffect(MobEffectCategory category, int color) {
        super(category, color);

        // Dummy attribute modifier to trigger client-side handling without affecting movement speed
        // Also using event handler to sync effect periodically
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "petrification_sync_dummy"),
                -10.0D, // No actual effect on speed
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if(livingEntity.level().isClientSide()) {
            return true;
        }

        livingEntity.setDeltaMovement(new Vec3(0, 0, 0));
        livingEntity.hurtMarked = true;

        BeyonderData.disableAbilityUse(livingEntity, "petrification");
        ServerScheduler.scheduleDelayed(20 * 2, () -> {
            if(!livingEntity.hasEffect(ModEffects.PETRIFICATION)) {
                BeyonderData.enableAbilityUse(livingEntity, "petrification");
            }
        });

        if(!BeyonderData.isBeyonder(livingEntity) && livingEntity instanceof Mob mob) {
            mob.setNoAi(true);
            ServerScheduler.scheduleDelayed(20 * 2, () -> {
                if(!livingEntity.hasEffect(ModEffects.PETRIFICATION)) {
                    mob.setNoAi(false);
                }
            });
        }

        if(livingEntity.tickCount % 10 == 0) {
            ServerLevel serverLevel = (ServerLevel) livingEntity.level();
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(livingEntity) < 4096) {
                    MobEffectInstance effectInstance = livingEntity.getEffect(ModEffects.PETRIFICATION);
                    if (effectInstance != null) {
                        player.connection.send(new ClientboundUpdateMobEffectPacket(livingEntity.getId(), effectInstance, false ));
                    }
                }
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        event.getEffect();
        if (event.getEffect() != ModEffects.PETRIFICATION) {
            return;
        }

        LivingEntity livingEntity = event.getEntity();
        sendRemoveEffectPacket(livingEntity);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null || event.getEffectInstance().getEffect().value() != ModEffects.PETRIFICATION.value()) {
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
                            ModEffects.PETRIFICATION
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
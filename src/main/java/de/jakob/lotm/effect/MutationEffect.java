package de.jakob.lotm.effect;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.DamageLookup;
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

import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MutationEffect extends MobEffect {

    private final Random random = new Random();

    protected MutationEffect(MobEffectCategory category, int color) {
        super(category, color);

        // Dummy attribute modifier to trigger client-side handling without affecting movement speed
        // Also using event handler to sync effect periodically
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mutation_sync_dummy"),
                0.0D, // No actual effect on speed
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if(livingEntity.level().isClientSide()) {
            return true;
        }

        SanityComponent sanityComponent = livingEntity.getData(ModAttachments.SANITY_COMPONENT);
        if(random.nextInt(100) < 5) {
            sanityComponent.increaseSanityAndSync(-.01f, livingEntity);
        }
        if(random.nextInt(100) < 5) {
            livingEntity.hurt(livingEntity.damageSources().generic(), (float) DamageLookup.lookupDamage(4, .5));
        }

        if(livingEntity.tickCount % 10 == 0) {
            ServerLevel serverLevel = (ServerLevel) livingEntity.level();
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(livingEntity) < 4096) {
                    MobEffectInstance effectInstance = livingEntity.getEffect(ModEffects.MUTATED);
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
        if (event.getEffect() != ModEffects.MUTATED) {
            return;
        }

        LivingEntity livingEntity = event.getEntity();
        sendRemoveEffectPacket(livingEntity);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null || event.getEffectInstance().getEffect().value() != ModEffects.MUTATED.value()) {
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
                            ModEffects.MUTATED
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
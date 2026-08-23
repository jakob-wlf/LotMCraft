package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.beyonders.abilities.death.DeathDecreeAbility;
import de.jakob.lotm.beyonders.abilities.door.passives.VoidImmunityAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PassiveLuckAbility;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.effect.FoolingEffect;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import de.jakob.lotm.rendering.effectRendering.impl.DeathDecreeRingEffect;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.item.custom.MarionetteControllerItem;
import de.jakob.lotm.item.custom.SubordinateControllerItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncOnHoldAbilityPacket;
import de.jakob.lotm.network.packets.toClient.SyncToggleAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BeyonderDataTickHandler {

    private static final Set<PassiveAbilityItem> passiveAbilities = ConcurrentHashMap.newKeySet();


    private static final Map<UUID, Set<PassiveAbilityItem>> cachedAbilities = new ConcurrentHashMap<>();

    // Tracks the active Death Decree ring VFX per marked entity, keyed by entity UUID,
    // storing both the effect id and the stack count it was spawned with — the ring
    // effect must be re-spawned (not just repositioned) when the stack count changes,
    // since EffectManager has no "update params" packet, only position updates.
    private static final Map<UUID, DeathDecreeRingState> deathDecreeRings = new ConcurrentHashMap<>();

    private record DeathDecreeRingState(UUID effectId, int stacks, Set<UUID> viewerIds) {}

    public static void invalidateCache(LivingEntity entity) {
        cachedAbilities.remove(entity.getUUID());
    }

    // Only Death pathway players can see the Death Decree ring — everyone else still
    // sees the mob effect's other cues (screen darkening for the marked player, etc.)
    // but not this VFX, so it's sent per-player rather than broadcast to the level.
    private static boolean canSeeDeathDecreeRing(ServerPlayer player) {
        return "death".equals(BeyonderData.getPathway(player));
    }

    private static void updateDeathDecreeRing(LivingEntity entity, ServerLevel level, int stacks) {
        DeathDecreeRingState state = deathDecreeRings.get(entity.getUUID());
        List<ServerPlayer> viewers = level.players().stream()
                .filter(BeyonderDataTickHandler::canSeeDeathDecreeRing)
                .toList();
        Set<UUID> viewerIds = viewers.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet());

        if (state != null && state.stacks() == stacks) {
            // Re-sync per-player subscriptions: spawn for newly-qualifying viewers,
            // cancel for ones who left/switched pathway, update the rest in place.
            for (ServerPlayer viewer : viewers) {
                if (state.viewerIds().contains(viewer.getUUID())) {
                    EffectManager.updateEffectPosition(state.effectId(), entity.getX(), entity.getY(), entity.getZ(), viewer);
                } else {
                    float[] arr = EffectParams.defaultParamsArray();
                    arr[DeathDecreeRingEffect.STACKS_PARAM] = stacks;
                    EffectManager.playMovableEffectWithId(state.effectId(), EffectIds.DEATH_DECREE_RING, viewer, entity, new EffectParams(20, true, arr));
                }
            }
            deathDecreeRings.put(entity.getUUID(), new DeathDecreeRingState(state.effectId(), stacks, viewerIds));
            return;
        }

        if (state != null) {
            for (ServerPlayer viewer : level.players()) {
                if (state.viewerIds().contains(viewer.getUUID())) {
                    EffectManager.cancelEffect(state.effectId(), viewer);
                }
            }
        }

        float[] arr = EffectParams.defaultParamsArray();
        arr[DeathDecreeRingEffect.STACKS_PARAM] = stacks;
        EffectParams params = new EffectParams(20, true, arr);

        UUID effectId = UUID.randomUUID();
        for (ServerPlayer viewer : viewers) {
            EffectManager.playMovableEffectWithId(effectId, EffectIds.DEATH_DECREE_RING, viewer, entity, params);
        }
        deathDecreeRings.put(entity.getUUID(), new DeathDecreeRingState(effectId, stacks, viewerIds));
    }

    private static void cancelDeathDecreeRing(LivingEntity entity, ServerLevel level) {
        DeathDecreeRingState state = deathDecreeRings.remove(entity.getUUID());
        if (state != null) {
            for (ServerPlayer viewer : level.players()) {
                if (state.viewerIds().contains(viewer.getUUID())) {
                    EffectManager.cancelEffect(state.effectId(), viewer);
                }
            }
        }
    }

    private static final Object INIT_LOCK = new Object();

    private static Set<PassiveAbilityItem> getApplicableAbilities(LivingEntity entity) {
        if (passiveAbilities.isEmpty()) {
            synchronized (INIT_LOCK) {
                if (passiveAbilities.isEmpty()) {
                    List<PassiveAbilityItem> items = PassiveAbilityHandler.ITEMS
                            .getEntries()
                            .stream()
                            .map(entry -> (PassiveAbilityItem) entry.get())
                            .toList();
                    passiveAbilities.addAll(items);
                }
            }
        }

        return cachedAbilities.computeIfAbsent(entity.getUUID(), k ->
                passiveAbilities.stream()
                        .filter(a -> a.shouldApplyTo(entity))
                        .collect(Collectors.toSet())
        );
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();

        if(!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        // Tick cooldowns
        AbilityCooldownComponent component = livingEntity.getData(ModAttachments.COOLDOWN_COMPONENT);
        component.tick();

        //Virtual Personas heal
        if(livingEntity instanceof ServerPlayer player) {
            VirtualPersonaComponent virtualPersonaComponent = player.getData(ModAttachments.VIRTUAL_PERSONAS);
            virtualPersonaComponent.heal(player);
        }

        // Tick flight cooldown
        DisabledFlightComponent disabledFlightComponent = livingEntity.getData(ModAttachments.FLIGHT_DISABLE_COMPONENT);
        if(disabledFlightComponent.getCooldownTicks() > 0) {
            disabledFlightComponent.setCooldownTicks(disabledFlightComponent.getCooldownTicks() - 1);
        }

        if (!livingEntity.level().isClientSide) {
            FoolingComponent foolingComponent = livingEntity.getData(ModAttachments.FOOLING_COMPONENT);
            if (foolingComponent.isFooled()) {
                if (foolingComponent.getTicksRemaining() % FoolingEffect.STUN_INTERVAL_TICKS == 0) {
                    foolingComponent.applyStun(FoolingEffect.STUN_DURATION_TICKS);
                }

                if (foolingComponent.isStunned()) {
                    livingEntity.setDeltaMovement(0, 0, 0);
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 254, false, false, false));
                    livingEntity.hurtMarked = true;
                }

                foolingComponent.tick();
                livingEntity.addEffect(new MobEffectInstance(ModEffects.FOOLING, foolingComponent.getTicksRemaining(), 0, false, true, true));
            } else if (livingEntity.hasEffect(ModEffects.FOOLING)) {
                livingEntity.removeEffect(ModEffects.FOOLING);
            }

            EndpointComponent endpointComponent = livingEntity.getData(ModAttachments.ENDPOINT_COMPONENT);
            if (endpointComponent.isActive()) {
                if (livingEntity.level() instanceof ServerLevel serverLevel &&
                        InteractionHandler.isInteractionPossibleStrictlyHigher(
                                new Location(livingEntity.position(), serverLevel), "purification", endpointComponent.getCasterSequence(), -1)) {
                    endpointComponent.clear();
                    livingEntity.removeEffect(ModEffects.ENDPOINT);

                    serverLevel.playSound(null, livingEntity.blockPosition(),
                            SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5f, 1.2f);
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
                } else {
                    livingEntity.addEffect(new MobEffectInstance(ModEffects.ENDPOINT, 25, 0, false, true, true));
                }
            } else if (livingEntity.hasEffect(ModEffects.ENDPOINT)) {
                livingEntity.removeEffect(ModEffects.ENDPOINT);
            }

            DeathDecreeMarkComponent deathDecreeMark = livingEntity.getData(ModAttachments.DEATH_DECREE_MARK);
            if (deathDecreeMark.getStacks() > 0) {
                if (livingEntity.level() instanceof ServerLevel serverLevel &&
                        InteractionHandler.isInteractionPossibleStrictlyHigher(
                                new Location(livingEntity.position(), serverLevel), "purification", deathDecreeMark.getCasterSequence(), -1)) {
                    deathDecreeMark.clear();
                    livingEntity.removeEffect(ModEffects.DEATH_DECREE_MARK);
                    cancelDeathDecreeRing(livingEntity, serverLevel);

                    serverLevel.playSound(null, livingEntity.blockPosition(),
                            SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5f, 1.2f);
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
                } else {
                    livingEntity.addEffect(new MobEffectInstance(ModEffects.DEATH_DECREE_MARK, 25, deathDecreeMark.getStacks() - 1, false, true, true));

                    if (livingEntity.level() instanceof ServerLevel serverLevel) {
                        updateDeathDecreeRing(livingEntity, serverLevel, deathDecreeMark.getStacks());
                    }
                }
            } else if (livingEntity.hasEffect(ModEffects.DEATH_DECREE_MARK)) {
                livingEntity.removeEffect(ModEffects.DEATH_DECREE_MARK);
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    cancelDeathDecreeRing(livingEntity, serverLevel);
                }
            }
        }

        if(BeyonderData.isBeyonder(livingEntity)) {
            if(entity.getData(ModAttachments.SANITY_COMPONENT.get()).getSanity() == 0.0f){
                entity.kill();
            }

            if(entity.tickCount % 20 == 0){
                entity.getData(ModAttachments.REGEN_DISABLER.get()).incrementCount();
            }

            if(entity.tickCount % 200 == 0) {
                invalidateCache(livingEntity);
                PhysicalEnhancementsAbility.resetEnhancements(event.getEntity().getUUID(), livingEntity, false);
                invalidateCache(livingEntity);
            }

            if(entity.tickCount % (20 * 30) == 0) {
                BeyonderData.incrementWormAmount(livingEntity, 1);
            }

            // Tick Passive Abilities, and onHold for currently selected Ability and tick luck
            if(entity.tickCount % 5 == 0) {
                tickAbilities(livingEntity);

                // Remove Unluck gradually
                LuckComponent luckComponent = livingEntity.getData(ModAttachments.LUCK_COMPONENT);
                if(luckComponent.getLuck() < 0) {
                    luckComponent.addLuckWithMax(1, 0);
                }

                // Remove Luck gradually
                if(luckComponent.getLuck() > PassiveLuckAbility.getNormalLuckForEntity(livingEntity)) {
                    luckComponent.addLuckWithMin(-1, PassiveLuckAbility.getNormalLuckForEntity(livingEntity));
                }
            }

            // Tick Toggle Abilities
            ToggleAbility.getActiveAbilitiesForEntity(livingEntity).forEach(toggleAbility -> {
                if(entity.tickCount % toggleAbility.tickRate != 0) {
                    return;
                }
                toggleAbility.prepareTick(livingEntity.level(), livingEntity);
                PacketHandler.sendToTrackingAndSelf(livingEntity, new SyncToggleAbilityPacket(livingEntity.getId(), toggleAbility.getId(), SyncToggleAbilityPacket.Action.TICK.getValue()));
            });
        }
    }

    @SubscribeEvent
    public static void onEndpointHeal(LivingHealEvent event) {
        EndpointComponent endpointComponent = event.getEntity().getData(ModAttachments.ENDPOINT_COMPONENT);
        if (endpointComponent.isActive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel serverLevel) {
            cancelDeathDecreeRing(entity, serverLevel);
        }

        // Clear the mark itself, not just its VFX — otherwise the next tick (or a
        // respawn that retains this attachment) re-reads stacks > 0 and immediately
        // re-spawns both the mob effect and the ring.
        entity.getData(ModAttachments.DEATH_DECREE_MARK).clear();
        entity.removeEffect(ModEffects.DEATH_DECREE_MARK);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if(player.getY() < -200 && !VoidImmunityAbility.IMMUNE_ENTITIES.contains(player)) {
            player.kill();
        }

        if (BeyonderData.isBeyonder(player)) {
            // Regenerate Spirituality
            float amount = BeyonderData.getMaxSpirituality(BeyonderData.getPathway(player), BeyonderData.getSequence(player), player) * 0.0006f;
            BeyonderData.incrementSpirituality(player, amount);

            // Slowly digest potion
            if(player.tickCount % 20 == 0) {
                BeyonderData.digest(player, 1 / (20 * 60 * 60f), true);
            }
        }

        // Tick special items
        if(player.tickCount % 5 == 0) {
            if(player.getMainHandItem().is(ModItems.MARIONETTE_CONTROLLER.get()) && player.getMainHandItem().getItem() instanceof MarionetteControllerItem) {
                MarionetteControllerItem.onHold(player, player.getMainHandItem());
            }

            if(player.getMainHandItem().is(ModItems.SUBORDINATE_CONTROLLER.get()) && player.getMainHandItem().getItem() instanceof SubordinateControllerItem) {
                SubordinateControllerItem.onHold(player, player.getMainHandItem());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        PhysicalEnhancementsAbility.resetEnhancements(event.getEntity().getUUID(), event.getEntity(), true);
        invalidateCache(event.getEntity());
    }

    private static void tickAbilities(LivingEntity entity) {
        if(entity.level().isClientSide) return;

        getApplicableAbilities(entity).forEach(abilityItem -> {
            abilityItem.tick(entity.level(), entity);
        });

        if(entity instanceof ServerPlayer player) {
            AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            if(component.getSelectedAbility() < 0 || component.getSelectedAbility() >= component.getAbilities().size()) {
                return;
            }

            String abilityId = component.getAbilities().get(component.getSelectedAbility()).split(":")[0];
            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if(ability != null) {
                ability.onHold(player.serverLevel(), player);
                PacketHandler.sendToTrackingAndSelf(player, new SyncOnHoldAbilityPacket(player.getId(), abilityId));
            }
        }
    }

    @SubscribeEvent
    public static void disableRegen(LivingIncomingDamageEvent event) {
        var entity = event.getEntity();
        if(!BeyonderData.isBeyonder(entity)) return;

        entity.getData(ModAttachments.REGEN_DISABLER.get()).disableFor(10);

        if (entity.hasEffect(MobEffects.REGENERATION)){
            entity.removeEffect(MobEffects.REGENERATION);
        }
    }
}
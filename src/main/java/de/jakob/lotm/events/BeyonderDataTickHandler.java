package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.door.passives.VoidImmunityAbility;
import de.jakob.lotm.abilities.visionary.PsychologicalInvisibilityAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.passives.PassiveLuckAbility;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.effect.FoolingEffect;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BeyonderDataTickHandler {

    private static final Set<PassiveAbilityItem> passiveAbilities = ConcurrentHashMap.newKeySet();


    // In BeyonderDataTickHandler
    private static final Map<UUID, Set<PassiveAbilityItem>> cachedAbilities = new ConcurrentHashMap<>();

    public static void invalidateCache(LivingEntity entity) {
        cachedAbilities.remove(entity.getUUID());
    }

    private static final Object INIT_LOCK = new Object();

    private static Set<PassiveAbilityItem> getApplicableAbilities(LivingEntity entity) {
        if (passiveAbilities.isEmpty()) {
            synchronized (INIT_LOCK) {
                // Double-checked locking: re-test inside the lock
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

        // Tick flight cooldown
        DisabledFlightComponent disabledFlightComponent = livingEntity.getData(ModAttachments.FLIGHT_DISABLE_COMPONENT);
        if(disabledFlightComponent.getCooldownTicks() > 0) {
            disabledFlightComponent.setCooldownTicks(disabledFlightComponent.getCooldownTicks() - 1);
        }

        // Tick Fooling attachment — re-apply a 2-tick cosmetic effect each tick so the HUD always shows it
        if (!livingEntity.level().isClientSide) {
            FoolingComponent foolingComponent = livingEntity.getData(ModAttachments.FOOLING_COMPONENT);
            if (foolingComponent.isFooled()) {
                // Trigger a new stun on the interval, based on remaining ticks
                if (foolingComponent.getTicksRemaining() % FoolingEffect.STUN_INTERVAL_TICKS == 0) {
                    foolingComponent.applyStun(FoolingEffect.STUN_DURATION_TICKS);
                }

                // Zero velocity and suppress client movement every tick while stunned
                if (foolingComponent.isStunned()) {
                    livingEntity.setDeltaMovement(0, 0, 0);
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 254, false, false, false));
                    livingEntity.hurtMarked = true;
                }

                foolingComponent.tick();
                // Re-apply with the actual remaining ticks so the HUD countdown is accurate
                livingEntity.addEffect(new MobEffectInstance(ModEffects.FOOLING, foolingComponent.getTicksRemaining(), 0, false, true, true));
            } else if (livingEntity.hasEffect(ModEffects.FOOLING)) {
                livingEntity.removeEffect(ModEffects.FOOLING);
            }
        }

        if(BeyonderData.isBeyonder(livingEntity)) {
            if(entity.getData(ModAttachments.SANITY_COMPONENT.get()).getSanity() == 0.0f){
                entity.kill();
            }

            if(entity.tickCount % 200 == 0) {
                invalidateCache(livingEntity);
                PhysicalEnhancementsAbility.resetEnhancements(event.getEntity().getUUID());
                invalidateCache(livingEntity); // also re-filter applicable abilities
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
                BeyonderData.digest(player, 1 / (20 * 60 * 60f), false);
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
        PhysicalEnhancementsAbility.resetEnhancements(event.getEntity().getUUID());
        invalidateCache(event.getEntity()); // also re-filter applicable abilities
    }

    private static void tickAbilities(LivingEntity entity) {
        if(entity.level().isClientSide) return;

        // Passive Abilities
        getApplicableAbilities(entity).forEach(abilityItem -> {
            abilityItem.tick(entity.level(), entity);
        });

        if(entity instanceof ServerPlayer player) {
            // Sync on Hold for currently selected Ability
            AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            if(component.getSelectedAbility() < 0 || component.getSelectedAbility() >= component.getAbilities().size()) {
                return;
            }

            String abilityId = component.getAbilities().get(component.getSelectedAbility());
            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if(ability != null) {
                ability.onHold(player.serverLevel(), player);
                PacketHandler.sendToTrackingAndSelf(player, new SyncOnHoldAbilityPacket(player.getId(), abilityId));
            }
        }
    }
}
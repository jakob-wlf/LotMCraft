package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.AbilityCooldownComponent;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
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
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BeyonderDataTickHandler {

    public static final HashSet<PassiveAbilityItem> passiveAbilities = new HashSet<>();

    // In BeyonderDataTickHandler
    private static final Map<UUID, Set<PassiveAbilityItem>> cachedAbilities = new HashMap<>();

    public static void invalidateCache(LivingEntity entity) {
        cachedAbilities.remove(entity.getUUID());
    }

    private static Set<PassiveAbilityItem> getApplicableAbilities(LivingEntity entity) {
        if(passiveAbilities.isEmpty()) {
            BeyonderDataTickHandler.passiveAbilities.addAll(PassiveAbilityHandler.ITEMS.getEntries().stream().map(entry -> (PassiveAbilityItem) entry.get()).toList());
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

        if(BeyonderData.isBeyonder(livingEntity)) {
            if(entity.tickCount % 200 == 0) {
                invalidateCache(livingEntity);
            }

            // Tick Passive Abilities, Toggle Abilities and onHold for currently selected Ability
            if(entity.tickCount % 5 == 0)
                tickAbilities(livingEntity);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if(player.getY() < -200) {
            player.kill();
        }

        if (BeyonderData.isBeyonder(player)) {
            // Regenerate Spirituality
            float amount = BeyonderData.getMaxSpirituality(BeyonderData.getSequence(player)) * 0.0006f;
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

    private static void tickAbilities(LivingEntity entity) {
        if(entity.level().isClientSide) return;

        // Passive Abilities
        getApplicableAbilities(entity).forEach(abilityItem -> {
            abilityItem.tick(entity.level(), entity);
        });

        // Tick Toggle Abilities
        ToggleAbility.getActiveAbilitiesForEntity(entity).forEach(toggleAbility -> {
            toggleAbility.prepareTick(entity.level(), entity);
            PacketHandler.sendToTrackingAndSelf(entity, new SyncToggleAbilityPacket(entity.getId(), toggleAbility.getId(), SyncToggleAbilityPacket.Action.TICK.getValue()));
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
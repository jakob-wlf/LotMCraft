package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.AbilityItemHandler;
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
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BeyonderDataTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Tick cooldowns
        AbilityCooldownComponent component = player.getData(ModAttachments.COOLDOWN_COMPONENT);
        component.tick();

        if (BeyonderData.isBeyonder(player)) {
            // Regenerate Spirituality
            float amount = BeyonderData.getMaxSpirituality(BeyonderData.getSequence(player)) * 0.0006f;
            BeyonderData.incrementSpirituality(player, amount);

            // Tick Passive Abilities, Toggle Abilities and onHold for currently selected Ability
            if(player.tickCount % 5 == 0)
                tickAbilities(serverPlayer);

            // Slowly digest potion
            if(player.tickCount % 20 == 0) {
                BeyonderData.digest(player, 1 / (20 * 60 * 60f));
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

    private static void tickAbilities(ServerPlayer player) {
        // Passive Abilities
        PassiveAbilityHandler.ITEMS.getEntries().forEach(itemHolder -> {
            if (itemHolder.get() instanceof PassiveAbilityItem abilityItem) {
                if (abilityItem.shouldApplyTo(player)) {
                    abilityItem.tick(player.level(), player);
                }
            }
        });

        // Tick Toggle Abilities
        ToggleAbility.getActiveAbilitiesForEntity(player).forEach(toggleAbility -> {
            toggleAbility.prepareTick(player.serverLevel(), player);
            PacketHandler.sendToAllPlayersInSameLevel(new SyncToggleAbilityPacket(player.getId(), toggleAbility.getId(), SyncToggleAbilityPacket.Action.TICK.getValue()), player.serverLevel());
        });

        // Sync on Hold for currently selected Ability
        AbilityWheelComponent component = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        if(component.getSelectedAbility() < 0 || component.getSelectedAbility() >= component.getAbilities().size()) {
            return;
        }

        String abilityId = component.getAbilities().get(component.getSelectedAbility());
        Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
        if(ability != null) {
            ability.onHold(player.serverLevel(), player);
            PacketHandler.sendToAllPlayers(new SyncOnHoldAbilityPacket(player.getId(), abilityId));
        }
    }
}
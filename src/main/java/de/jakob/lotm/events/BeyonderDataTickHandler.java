package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ExileDoorsEntity;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.item.custom.MarionetteControllerItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BeyonderDataTickHandler {

    private static long tickCounter = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only run on server side
        if (player.level().isClientSide) {
            if(ClientBeyonderCache.isBeyonder(player.getUUID()))
                if(tickCounter % 5 == 0)
                    tickAbilitiesClientSide(player);
            return;
        }

        if (BeyonderData.isBeyonder(player)) {
            float amount = BeyonderData.getMaxSpirituality(BeyonderData.getSequence(player)) * 0.0006f;
            BeyonderData.incrementSpirituality(player, amount);

            // Passive abilities
            if(tickCounter % 5 == 0)
                tickAbilities(player);
        }

        if(tickCounter % 5 == 0) {
            if(player.getMainHandItem().is(ModItems.MARIONETTE_CONTROLLER.get()) && player.getMainHandItem().getItem() instanceof MarionetteControllerItem) {
                MarionetteControllerItem.onHold(player, player.getMainHandItem());
            }
        }

        tickCounter++;
        if(tickCounter > 2000000000)
            tickCounter = 0;
    }

    private static void tickAbilities(Player player) {
        PassiveAbilityHandler.ITEMS.getEntries().forEach(itemHolder -> {
            if (itemHolder.get() instanceof PassiveAbilityItem abilityItem) {
                if (abilityItem.shouldApplyTo(player)) {
                    abilityItem.tick(player.level(), player);
                }
            }
        });

        AbilityItemHandler.ITEMS.getEntries().forEach(item -> {
            if(item.get() instanceof AbilityItem abilityItem && player.getItemInHand(InteractionHand.MAIN_HAND).is(abilityItem)) {
                if(abilityItem.canUse(player)) {
                    abilityItem.onHold(player.level(), player);
                }
            }
        });
    }

    private static void tickAbilitiesClientSide(Player player) {
        AbilityItemHandler.ITEMS.getEntries().forEach(item -> {
            if(item.get() instanceof AbilityItem abilityItem && player.getItemInHand(InteractionHand.MAIN_HAND).is(abilityItem)) {
                if(abilityItem.canUse(player)) {
                    abilityItem.onHold(player.level(), player);
                }
            }
        });
    }
}
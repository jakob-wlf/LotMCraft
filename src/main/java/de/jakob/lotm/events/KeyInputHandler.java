package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.gui.custom.AbilityWheel.AbilityWheelMenu;
import de.jakob.lotm.gui.custom.AbilityWheel.AbilityWheelScreen;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.*;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {

    private static boolean wasHoldKeyDown = false;
    private static boolean wheelOpenedByHold = false;
    private static boolean waitingForWheelData = false;
    private static int waitTicks = 0;
    private static boolean pendingOpenByHold = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (LOTMCraft.openAbilitySelectionKey != null && LOTMCraft.openAbilitySelectionKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                int sequence = ClientBeyonderCache.getSequence(player.getUUID());
                String pathway = ClientBeyonderCache.getPathway(player.getUUID());
                PacketHandler.sendToServer(new OpenAbilitySelectionPacket(sequence, pathway));
            }
        }

        if(LOTMCraft.toggleGriefingKey != null && LOTMCraft.toggleGriefingKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PacketHandler.sendToServer(new ToggleGriefingPacket());
            }
        }

        if(LOTMCraft.pathwayInfosKey != null && LOTMCraft.pathwayInfosKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PacketHandler.sendToServer(new OpenIntrospectMenuPacket(ClientBeyonderCache.getSequence(player.getUUID()), ClientBeyonderCache.getPathway(player.getUUID())));
            }
        }

        if(LOTMCraft.enterSefirotKey != null && LOTMCraft.enterSefirotKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PacketHandler.sendToServer(new TeleportToSefirotPacket());
            }
        }

        if(LOTMCraft.toggleAbilityHotbarKey != null && LOTMCraft.toggleAbilityHotbarKey.consumeClick()) {
            PacketHandler.sendToServer(new ToggleAbilityHotbarPacket(true));
        }

        if(LOTMCraft.cycleAbilityHotbarKey != null && LOTMCraft.cycleAbilityHotbarKey.consumeClick()) {
            PacketHandler.sendToServer(new ToggleAbilityHotbarPacket(false));
        }

        if(LOTMCraft.nextAbilityKey != null && LOTMCraft.nextAbilityKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if(player != null && ClientBeyonderCache.isBeyonder(player.getUUID())) {
                if(player.getMainHandItem().getItem() instanceof SelectableAbilityItem abilityItem && abilityItem.canUse(player, player.getMainHandItem()))
                    abilityItem.nextAbility(player);
            }
        }

        if(LOTMCraft.previousAbilityKey != null && LOTMCraft.previousAbilityKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if(player != null && ClientBeyonderCache.isBeyonder(player.getUUID())) {
                if(player.getMainHandItem().getItem() instanceof SelectableAbilityItem abilityItem && abilityItem.canUse(player, player.getMainHandItem()))
                    abilityItem.previousAbility(player);
            }
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        // Handle waiting for server response
        if (waitingForWheelData) {
            waitTicks++;
            if (waitTicks > 5) { // Wait max 5 ticks (0.25 seconds)
                waitingForWheelData = false;
                waitTicks = 0;

                // Check if player has any abilities
                if (ClientData.getAbilityWheelAbilities().isEmpty()) {
                    mc.player.displayClientMessage(Component.translatable("lotm.ability_wheel.no_abilities"), true);
                } else {
                    PacketHandler.sendToServer(new OpenAbilityWheelPacket());

                    if (pendingOpenByHold) {
                        wheelOpenedByHold = true;
                        pendingOpenByHold = false;
                    }
                }
            }
            return; // Don't process other keys while waiting
        }

        // Check if we're already in a menu (but not our wheel)
        boolean inOtherMenu = mc.screen != null && !(mc.screen instanceof AbilityWheelScreen);

        // Handle toggle key
        if (LOTMCraft.openWheelToggleKey.consumeClick() && !inOtherMenu) {
            openAbilityWheel(false);
        }

        // Handle hold key
        boolean isHoldKeyDown = LOTMCraft.openWheelHoldKey.isDown();

        if (isHoldKeyDown && !wasHoldKeyDown && !inOtherMenu) {
            // Key just pressed
            openAbilityWheel(true);
        } else if (!isHoldKeyDown && wasHoldKeyDown && wheelOpenedByHold) {
            // Key just released - close if it was opened by hold
            if (mc.screen instanceof AbilityWheelScreen) {
                mc.player.closeContainer();
            }
            wheelOpenedByHold = false;
        }

        wasHoldKeyDown = isHoldKeyDown;

        // Handle use ability key
        if (LOTMCraft.useSelectedAbilityKey.consumeClick()) {
            PacketHandler.sendToServer(new UseSelectedAbilityPacket());
        }
    }

    private static void openAbilityWheel(boolean openedByHold) {
        // Request data from server and wait for response
        PacketHandler.sendToServer(new RequestAbilityWheelPacket());
        waitingForWheelData = true;
        waitTicks = 0;
        pendingOpenByHold = openedByHold;
    }
}

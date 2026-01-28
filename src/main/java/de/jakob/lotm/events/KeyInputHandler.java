package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.SelectableAbilityItem;
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
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

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

        if (LOTMCraft.openWheelToggleKey.consumeClick()) {
            openAbilityWheel();
        }

        if(LOTMCraft.openWheelHoldKey.consumeClick() && mc.screen == null) {
            openAbilityWheel();
        }

        // Handle use ability key
        if (LOTMCraft.useSelectedAbilityKey.consumeClick()) {
            PacketHandler.sendToServer(new UseSelectedAbilityPacket());
        }
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        if(event.getKeyCode() == LOTMCraft.openWheelHoldKey.getKey().getValue()) {
            if(Minecraft.getInstance().screen instanceof AbilityWheelScreen) {
                PacketHandler.sendToServer(new CloseAbilityWheelPacket());
            }
        }
    }

    private static void openAbilityWheel() {
        Minecraft mc = Minecraft.getInstance();
        if (ClientData.getAbilityWheelAbilities().isEmpty()) {
            mc.player.displayClientMessage(Component.translatable("lotm.ability_wheel.no_abilities"), true);
        } else {
            PacketHandler.sendToServer(new OpenAbilityWheelPacket());
        }
    }
}

package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
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

    // set from the ability wheel screen when clicked
    public static int holdAbilityWheelCooldownTicks = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

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

        if(LOTMCraft.nextAbilityKey != null && LOTMCraft.nextAbilityKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if(player != null && ClientBeyonderCache.isBeyonder(player.getUUID())) {
                if(ClientData.getSelectedAbility() < 0 || ClientData.getSelectedAbility() >= ClientData.getAbilityWheelAbilities().size()) {
                    return;
                }

                String abilityId = ClientData.getAbilityWheelAbilities().get(ClientData.getSelectedAbility());
                Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
                if(!(ability instanceof SelectableAbility selectableAbility)) {
                    return;
                }

                selectableAbility.nextAbility(player);
            }
        }

        if(LOTMCraft.previousAbilityKey != null && LOTMCraft.previousAbilityKey.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if(player != null && ClientBeyonderCache.isBeyonder(player.getUUID())) {
                if(ClientData.getSelectedAbility() < 0 || ClientData.getSelectedAbility() >= ClientData.getAbilityWheelAbilities().size()) {
                    return;
                }

                String abilityId = ClientData.getAbilityWheelAbilities().get(ClientData.getSelectedAbility());
                Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
                if(!(ability instanceof SelectableAbility selectableAbility)) {
                    return;
                }

                selectableAbility.previousAbility(player);
            }
        }

        if (LOTMCraft.openWheelToggleKey.consumeClick()) {
            openAbilityWheel();
        }

        if(LOTMCraft.openWheelHoldKey.consumeClick() && mc.screen == null && holdAbilityWheelCooldownTicks <= 0) {
            openAbilityWheel();
        }

        // Handle use ability key
        if (LOTMCraft.useSelectedAbilityKey.consumeClick()) {
            PacketHandler.sendToServer(new UseSelectedAbilityPacket());
        }

        if(holdAbilityWheelCooldownTicks > 0) {
            holdAbilityWheelCooldownTicks--;
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

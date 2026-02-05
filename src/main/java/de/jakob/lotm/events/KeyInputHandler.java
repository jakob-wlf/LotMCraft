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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {

    // set from the ability wheel screen when clicked
    public static int holdAbilityWheelCooldownTicks = 0;
    public static boolean wasWheelOpenedWithHold = false;

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
            wasWheelOpenedWithHold = false;
            openAbilityWheel();
        }

        if(LOTMCraft.openWheelHoldKey.consumeClick() && mc.screen == null && holdAbilityWheelCooldownTicks <= 0) {
            wasWheelOpenedWithHold = true;
            openAbilityWheel();
        }

        // Handle use ability key
        if (LOTMCraft.useSelectedAbilityKey.consumeClick()) {
            PacketHandler.sendToServer(new UseSelectedAbilityPacket());
        }

        if(holdAbilityWheelCooldownTicks > 0) {
            holdAbilityWheelCooldownTicks--;
        }

        if(LOTMCraft.useAbilityBarAbility1.consumeClick()) {
            PacketHandler.sendToServer(new UseKeyboundAbilityPacket(0));
        }
        if(LOTMCraft.useAbilityBarAbility2.consumeClick()) {
            PacketHandler.sendToServer(new UseKeyboundAbilityPacket(1));
        }
        if(LOTMCraft.useAbilityBarAbility3.consumeClick()) {
            PacketHandler.sendToServer(new UseKeyboundAbilityPacket(2));
        }
        if(LOTMCraft.useAbilityBarAbility4.consumeClick()) {
            PacketHandler.sendToServer(new UseKeyboundAbilityPacket(3));
        }
        if(LOTMCraft.useAbilityBarAbility5.consumeClick()) {
            PacketHandler.sendToServer(new UseKeyboundAbilityPacket(4));
        }
        if(LOTMCraft.useAbilityBarAbility6.consumeClick()) {
            PacketHandler.sendToServer(new UseKeyboundAbilityPacket(5));
        }
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        if(event.getKeyCode() == LOTMCraft.openWheelHoldKey.getKey().getValue()) {
            if(Minecraft.getInstance().screen instanceof AbilityWheelScreen && wasWheelOpenedWithHold) {
                Minecraft.getInstance().screen.onClose();
                PacketHandler.sendToServer(new CloseAbilityWheelPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        int key = event.getKey();

        int number = -1;

        // Check if it's a number key (top row)
        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            number = (key == GLFW.GLFW_KEY_0) ? 0 : (key - GLFW.GLFW_KEY_0);
        }

        // Or check numpad keys
        if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) {
            number = key - GLFW.GLFW_KEY_KP_0;
        }

        if(number <= 0) {
            return;
        }

        if(!(Minecraft.getInstance().screen instanceof AbilityWheelScreen)) {
            return;
        }

        if((number - 1) >= ClientData.getAbilityWheelAbilities().size()) {
            return;
        }

        if(LOTMCraft.openWheelHoldKey.getKey().getNumericKeyValue().orElse(-1) == number) {
            return;
        }
        if(LOTMCraft.openWheelToggleKey.getKey().getNumericKeyValue().orElse(-1) == number) {
            return;
        }

        PacketHandler.sendToServer(new UpdateSelectedAbilityPacket(number - 1));
        ClientData.setAbilityWheelData(
                new ArrayList<>(ClientData.getAbilityWheelAbilities()),
                number - 1
        );
        PacketHandler.sendToServer(new CloseAbilityWheelPacket());
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

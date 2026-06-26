package de.jakob.lotm.beyonders.abilities.visionary.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.visionary.MindWorldAuthorityEnvisioningAbility;
import de.jakob.lotm.gui.custom.Envisioning.EnvisioningScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class EnvisioningClientEvents {
    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || !MindWorldAuthorityEnvisioningAbility.canEnvisionClient) {
            return;
        }

        while (mc.options.keyInventory.consumeClick()) {
            mc.gameMode.setLocalMode(GameType.CREATIVE);

            mc.setScreen(new EnvisioningScreen(mc.player));
        }
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        Screen screen = event.getScreen();

        if(screen instanceof EnvisioningScreen){
            Minecraft.getInstance().gameMode.setLocalMode(GameType.SURVIVAL);

        }
    }
}

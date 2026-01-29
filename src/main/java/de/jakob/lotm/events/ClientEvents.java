package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.darkness.NightmareAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.InventoryOpenedPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        LOTMCraft.pathwayInfosKey = new KeyMapping("key.beyonders.pathway_infos", GLFW.GLFW_KEY_J, "key.categories.beyonders");
        LOTMCraft.toggleGriefingKey = new KeyMapping("key.beyonders.toggle_griefing", GLFW.GLFW_KEY_K, "key.categories.beyonders");
        LOTMCraft.showPassiveAbilitiesKey = new KeyMapping("key.beyonders.show_passives", GLFW.GLFW_KEY_I, "key.categories.beyonders");
        LOTMCraft.nextAbilityKey = new KeyMapping("key.beyonders.next_ability", GLFW.GLFW_KEY_V, "key.categories.beyonders");
        LOTMCraft.previousAbilityKey = new KeyMapping("key.beyonders.previous_ability", GLFW.GLFW_KEY_X, "key.categories.beyonders");
        LOTMCraft.enterSefirotKey = new KeyMapping("key.beyonders.enter_sefirot", GLFW.GLFW_KEY_U, "key.categories.beyonders");
        LOTMCraft.openWheelToggleKey = new KeyMapping("key.beyonders.open_wheel_toggle", GLFW.GLFW_KEY_LEFT_ALT, "key.categories.beyonders");
        LOTMCraft.openWheelHoldKey = new KeyMapping("key.beyonders.open_wheel_hold", GLFW.GLFW_KEY_P, "key.categories.beyonders");
        LOTMCraft.useSelectedAbilityKey = new KeyMapping("key.beyonders.use_ability", GLFW.GLFW_KEY_M, "key.categories.beyonders");

        event.register(LOTMCraft.pathwayInfosKey);
        event.register(LOTMCraft.toggleGriefingKey);
        event.register(LOTMCraft.showPassiveAbilitiesKey);
        event.register(LOTMCraft.nextAbilityKey);
        event.register(LOTMCraft.previousAbilityKey);
        event.register(LOTMCraft.enterSefirotKey);
        event.register(LOTMCraft.openWheelToggleKey);
        event.register(LOTMCraft.openWheelHoldKey);
        event.register(LOTMCraft.useSelectedAbilityKey);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (NightmareAbility.hasActiveNightmare(mc.player) || NightmareAbility.isAffectedByNightmare(mc.player))) {
            int color = 0xFFff2b4f; // ARGB (0xAARRGGBB)

            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            event.setRed(r);
            event.setGreen(g);
            event.setBlue(b);
        }
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (!(event.getScreen() instanceof InventoryScreen)) return;

        PacketHandler.sendToServer(new InventoryOpenedPacket());
    }
}
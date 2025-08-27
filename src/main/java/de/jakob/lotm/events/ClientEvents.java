package de.jakob.lotm.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.darkness.NightmareAbility;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        LOTMCraft.switchBeyonderKey = new KeyMapping("key.beyonders.switch_beyonder", GLFW.GLFW_KEY_N, "key.categories.beyonders");
        LOTMCraft.pathwayInfosKey = new KeyMapping("key.beyonders.pathway_infos", GLFW.GLFW_KEY_J, "key.categories.beyonders");
        LOTMCraft.openAbilitySelectionKey = new KeyMapping("key.beyonders.open_ability_selection", GLFW.GLFW_KEY_O, "key.categories.beyonders");
        LOTMCraft.toggleGriefingKey = new KeyMapping("key.beyonders.toggle_griefing", GLFW.GLFW_KEY_K, "key.categories.beyonders");
        LOTMCraft.clearBeyonderKey = new KeyMapping("key.beyonders.clear_beyonder", GLFW.GLFW_KEY_C, "key.categories.beyonders");
        LOTMCraft.showPassiveAbilitiesKey = new KeyMapping("key.beyonders.show_passives", GLFW.GLFW_KEY_I, "key.categories.beyonders");
        LOTMCraft.nextAbilityKey = new KeyMapping("key.beyonders.next_ability", GLFW.GLFW_KEY_V, "key.categories.beyonders");
        LOTMCraft.previousAbilityKey = new KeyMapping("key.beyonders.previous_ability", GLFW.GLFW_KEY_X, "key.categories.beyonders");
        LOTMCraft.increaseSequenceKey = new KeyMapping("key.beyonders.increase_sequence", GLFW.GLFW_KEY_UP, "key.categories.beyonders");
        LOTMCraft.decreaseSequenceKey = new KeyMapping("key.beyonders.decrease_sequence", GLFW.GLFW_KEY_DOWN, "key.categories.beyonders");
        event.register(LOTMCraft.increaseSequenceKey);
        event.register(LOTMCraft.decreaseSequenceKey);
        event.register(LOTMCraft.switchBeyonderKey);
        event.register(LOTMCraft.pathwayInfosKey);
        event.register(LOTMCraft.openAbilitySelectionKey);
        event.register(LOTMCraft.toggleGriefingKey);
        event.register(LOTMCraft.clearBeyonderKey);
        event.register(LOTMCraft.showPassiveAbilitiesKey);
        event.register(LOTMCraft.nextAbilityKey);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && NightmareAbility.hasActiveNightmare(mc.player)) {
            int color = 0xFFff2b4f; // ARGB (0xAARRGGBB)

            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            event.setRed(r);
            event.setGreen(g);
            event.setBlue(b);
        }
    }

}
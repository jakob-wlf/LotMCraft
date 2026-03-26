package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.HashSet;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ActiveToggleAbilitiesRenderer {

    // Modified from the SyncToggleAbilityPacket
    public static final HashSet<String> activeToggleAbilities = new HashSet<>();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "active_toggle_abilities_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        int y = 5;
        int x = 5;

        for(String abilityId : activeToggleAbilities) {
            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if(ability == null) continue;

            ResourceLocation texture = ability.getTextureLocation();
            guiGraphics.blit(texture, x, y, 0, 0, 24, 24, 24, 24);
            x += 29;
        }
    }

}

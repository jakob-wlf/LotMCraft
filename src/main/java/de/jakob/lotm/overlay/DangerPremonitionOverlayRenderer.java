package de.jakob.lotm.overlay;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.DivinationAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class DangerPremonitionOverlayRenderer {
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "danger_premonition_overlay"), (guiGraphics, deltaTracker) -> {
            renderText(guiGraphics);
        });
    }

    private static void renderText(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        String text = Component.translatable("lotm.danger_premonition").getString();

        int x = (screenWidth / 2) - (screenWidth / 6) - 30 - mc.font.width(text);
        int y = screenHeight - mc.font.lineHeight - 2;

        int redColor = 0xFFb47ad6; // Red color for text

        if (DivinationAbility.dangerPremonitionActive.contains(mc.player.getUUID())) {

            guiGraphics.drawString(mc.font, text, x, y, redColor);
        }
    }
}

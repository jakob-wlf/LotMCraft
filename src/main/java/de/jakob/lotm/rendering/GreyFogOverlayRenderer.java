package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Renders a subtle blue-grey tint over the entire screen when the local player
 * is inside an active Grey Fog seal.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class GreyFogOverlayRenderer {

    /** Set by {@code ClientHandler.handleGreyFogStatus} on packet receipt. */
    public static boolean insideGreyFog = false;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "grey_fog_tint"),
                (guiGraphics, deltaTracker) -> renderVignette(guiGraphics)
        );
    }

    private static void renderVignette(GuiGraphics guiGraphics) {
        if (!insideGreyFog) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int sw        = guiGraphics.guiWidth();
        int sh        = guiGraphics.guiHeight();
        int thickness = 80;
        // Blue-grey colour: #2A4A6A
        int r = 0x2A, g = 0x4A, b = 0x6A;

        // Same technique as the accommodation overlay: draw one-pixel-thick rectangles
        // from the screen edge inward, alpha following a quadratic falloff.
        for (int i = 0; i < thickness; i++) {
            float fade = 1.0f - (float) i / thickness;
            fade = fade * fade;
            int a = (int)(0xB0 * fade);
            if (a <= 0) continue;
            int col = (a << 24) | (r << 16) | (g << 8) | b;
            guiGraphics.fill(i,          i,          sw - i,     i + 1,      col); // top
            guiGraphics.fill(i,          sh - i - 1, sw - i,     sh - i,     col); // bottom
            guiGraphics.fill(i,          i + 1,      i + 1,      sh - i - 1, col); // left
            guiGraphics.fill(sw - i - 1, i + 1,      sw - i,     sh - i - 1, col); // right
        }
    }
}

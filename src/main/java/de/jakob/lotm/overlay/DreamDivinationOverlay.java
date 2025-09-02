package de.jakob.lotm.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.DivinationAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class DreamDivinationOverlay {

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "dream_divination_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (DivinationAbility.dreamDivinationUsers.containsKey(mc.player.getUUID())) {
            ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/dream_divination_overlay.png");
            // Push the current pose
            guiGraphics.pose().pushPose();

            // Set up alpha blending
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // Blit the texture with transparency
            guiGraphics.blit(backgroundTexture, 0, 0, screenWidth, screenHeight, 0, 0, 128, 96, 128, 96);

            // Reset blend settings and shader color
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset to opaque
            RenderSystem.disableBlend();

            // Pop the pose to avoid affecting later rendering
            guiGraphics.pose().popPose();
        }
    }
}
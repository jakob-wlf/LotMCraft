package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
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
public class GriefingOverlayRenderer {
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "griefing_overlay"), (guiGraphics, deltaTracker) -> {
            renderText(guiGraphics);
        });
    }

    private static final ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/griefing_overlay.png");
    private static final int size = 24;

    private static void renderText(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int x = screenWidth - size - 4;
        int y = 4;

        if (BeyonderData.isGriefingEnabled(mc.player) && !mc.options.hideGui) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            float ratio = 16.0f / 24.0f;
            poseStack.scale(ratio, ratio, 1.0f);
            guiGraphics.blit(iconTexture, (int) (x / ratio), (int) (y / ratio), 0, 0, size, size, size, size);
            poseStack.popPose();
        }
    }
}

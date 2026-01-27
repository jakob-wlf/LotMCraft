package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.HashSet;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class DangerPremonitionOverlayRenderer {

    public static HashSet<UUID> playersWithDangerPremonitionActivated = new HashSet<>();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "danger_premonition_overlay"), (guiGraphics, deltaTracker) -> {
            renderText(guiGraphics);
        });
    }

    private static final ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/danger_premonition.png");
    private static final int x = 2;
    private static final int y = 120 + 60 + 12;


    private static void renderText(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (playersWithDangerPremonitionActivated.contains(mc.player.getUUID())) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.scale(24.0f / 32f, 24.0f / 32f, 1.0f);
            guiGraphics.blit(iconTexture, (int)(x / (24.0f / 32f)), (int)(y / (24.0f / 32f)), 0, 0, 32, 32, 32, 32);
            poseStack.popPose();
        }
    }
}

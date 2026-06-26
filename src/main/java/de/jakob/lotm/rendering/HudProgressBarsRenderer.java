package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.ClientSacrificeCache;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class HudProgressBarsRenderer {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isDeadOrDying()) {
            ClientSacrificeCache.resetSacrificeDuration();
            return;
        }
        ClientSacrificeCache.tickDown();
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "progress_bar"), (guiGraphics, deltaTracker) -> {
            renderProgressBar(guiGraphics);
            renderSacrificeBar(guiGraphics);
            renderSanityBar(guiGraphics);
        });
    }

    private static void renderProgressBar(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Progress bar dimensions and position
        int barWidth = 14;
        int barHeight = 120;
        int barX = 6; // 10 pixels from left edge
        int barY = 60; // 50 pixels from top

        // Colors
        int backgroundColor = 0x80000000; // Semi-transparent black
        int progressColorStart = 0xFF4A90E2;
        int progressColorEnd = 0xFF50E3C2;

        // Check if current player has progress
        if ((ClientBeyonderCache.isBeyonder(mc.player.getUUID())) && !mc.options.hideGui) {
            float progress = ClientBeyonderCache.getSpirituality(mc.player.getUUID()) / BeyonderData.getMaxSpirituality(ClientBeyonderCache.getPathway(mc.player.getUUID()), ClientBeyonderCache.getSequence(mc.player.getUUID()), mc.player);

            // Draw background
            guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, backgroundColor);

            // Calculate progress fill height
            int progressHeight = (int) (barHeight * progress);
            int progressStartY = barY + barHeight - progressHeight;

            // Draw progress fill (from bottom to top)
            if (progressHeight > 0) {
                drawVerticalGradient(guiGraphics, barX, progressStartY, barWidth, progressHeight,
                        progressColorStart, progressColorEnd);
            }

            ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/spirituality_bar_background.png");
            guiGraphics.blit(backgroundTexture, barX - 4, barY - 4, barWidth + 8, barHeight + 8, 0, 0, 44, 256, 44, 256);
        }
    }

    private static void renderSacrificeBar(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (ClientSacrificeCache.getTotalTicks() <= 0 || ClientSacrificeCache.getRemainingTicks() <= 0) return;
        if (!ClientBeyonderCache.isBeyonder(mc.player.getUUID())) return;

        int barWidth = 14;
        int barHeight = 120;
        int barX = 6 + barWidth + 6; // right of the spirituality bar
        int barY = 60;

        float progress = (float) ClientSacrificeCache.getRemainingTicks() / ClientSacrificeCache.getTotalTicks();

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x80000000);

        int progressHeight = (int) (barHeight * progress);
        int progressStartY = barY + barHeight - progressHeight;
        if (progressHeight > 0) {
            drawVerticalGradient(guiGraphics, barX, progressStartY, barWidth, progressHeight,
                    0xFFFF4444, 0xFFAA0000);
        }

        ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/spirituality_bar_background.png");
        guiGraphics.blit(backgroundTexture, barX - 4, barY - 4, barWidth + 8, barHeight + 8, 0, 0, 44, 256, 44, 256);
    }

    private static void renderSanityBar(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        if (!ClientBeyonderCache.isBeyonder(mc.player.getUUID())) return;

        float sanity = mc.player.getData(ModAttachments.SANITY_COMPONENT.get()).getSanity();

        if(sanity > .85f) return; // Please leave it like this :'( -Jakob

        int barWidth = 14;
        int barHeight = 120;
        int screenWidth = guiGraphics.guiWidth();
        int barX = screenWidth - barWidth - 6;
        int barY = 60;


        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x80000000);

        int progressHeight = (int) (barHeight * sanity);
        int progressStartY = barY + barHeight - progressHeight;
        if (progressHeight > 0) {
            drawVerticalGradient(guiGraphics, barX, progressStartY, barWidth, progressHeight,
                    0xFFe8bb68, 0xFFF5ad2a);
        }

        ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/spirituality_bar_background.png");
        guiGraphics.blit(backgroundTexture, barX - 4, barY - 4, barWidth + 8, barHeight + 8, 0, 0, 44, 256, 44, 256);
    }

    private static void drawVerticalGradient(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                             int startColor, int endColor) {
        for (int i = 0; i < height; i++) {
            float ratio = (float) i / height;
            int color = interpolateColor(startColor, endColor, ratio);
            guiGraphics.fill(x, y + i, x + width, y + i + 1, color);
        }
    }

    private static int interpolateColor(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
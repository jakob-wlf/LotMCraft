package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientAccommodationCache;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.ClientLuckResourceCache;
import de.jakob.lotm.util.LuckManager;
import de.jakob.lotm.util.data.ClientSacrificeCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

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
            renderLuckResourceBar(guiGraphics);
            renderSacrificeBar(guiGraphics);
            renderSanityBar(guiGraphics);
            renderCorruptionBar(guiGraphics);
        });

        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "accommodation_bar"), (guiGraphics, deltaTracker) -> {
            renderAccommodationColorOverlay(guiGraphics);
            renderAccommodationBar(guiGraphics);
        });
    }

    private static void renderLuckResourceBar(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        int storedLuck = ClientLuckResourceCache.getStoredLuck();
        boolean wheelOfFortune = ClientLuckResourceCache.isWheelOfFortune();
        int luck = wheelOfFortune
            ? storedLuck + Math.min(0, ClientLuckResourceCache.getLuck())
            : ClientLuckResourceCache.getLuck();
        int maximum = ClientLuckResourceCache.getMaximumLuck();
        if (mc.player == null || mc.options.hideGui || (!wheelOfFortune && luck == 0) || maximum <= 0) return;

        int barWidth = 14;
        int barHeight = 120;
        int barX = 30;
        int barY = 60;
        int scale = luck < 0 ? -LuckManager.minimumLuck : maximum;
        float progress = Math.clamp((float) Math.abs(luck) / scale, 0, 1);

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x80000000);
        int progressHeight = (int) (barHeight * progress);
        if (progressHeight > 0) {
                int startColor = luck < 0 ? 0xFF8F3030 : 0xFFF8D66D;
                int endColor = luck < 0 ? 0xFFE16D6D : 0xFF72E8D2;
                drawVerticalGradient(guiGraphics, barX, barY + barHeight - progressHeight, barWidth, progressHeight,
                    startColor, endColor);
        }

        ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/spirituality_bar_background.png");
        guiGraphics.blit(backgroundTexture, barX - 4, barY - 4, barWidth + 8, barHeight + 8, 0, 0, 44, 256, 44, 256);

        String amount = "Luck: " + luck;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(barX + barWidth / 2.0f, barY + barHeight + 8, 0);
        guiGraphics.pose().scale(0.75f, 0.75f, 1.0f);
        guiGraphics.drawString(mc.font, amount, -mc.font.width(amount) / 2, 0, 0xFFF8D66D, true);
        float netRate = ClientLuckResourceCache.getRegenerationRate()
            - ClientLuckResourceCache.getDrainRate();
        String rate = netRate > 0
            ? "\u2191 " + String.format("%.1f", netRate)
            : netRate < 0
                ? "\u2193 " + String.format("%.1f", Math.abs(netRate))
                : "0.0";
        int rateColor = netRate > 0 ? 0xFF55FF55
            : netRate < 0 ? 0xFFFF5555 : 0xFFAAAAAA;
        guiGraphics.drawString(mc.font, rate, -mc.font.width(rate) / 2, 10, rateColor, true);
        guiGraphics.pose().popPose();
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

        int barWidth = 120;
        int barHeight = 6;
        int barX = guiGraphics.guiWidth() / 2 - barWidth / 2;
        int barY = guiGraphics.guiHeight() - 55;

        float progress = (float) ClientSacrificeCache.getRemainingTicks() / ClientSacrificeCache.getTotalTicks();

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x80000000);

        int fillWidth = (int) (barWidth * progress);
        if (fillWidth > 0) {
            guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, 0xFFB51F2E);
        }

        String duration = "Sacrifice: " + String.format("%.1fs", ClientSacrificeCache.getRemainingTicks() / 20.0f);
        guiGraphics.drawString(mc.font, duration,
                guiGraphics.guiWidth() / 2 - mc.font.width(duration) / 2,
                barY - 10, 0xFFFF6B72, true);
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

    private static void renderCorruptionBar(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        if (!ClientBeyonderCache.isBeyonder(mc.player.getUUID())) return;

        float corruption = mc.player.getData(ModAttachments.CORRUPTION_COMPONENT.get()).getCorruption();

        if (corruption < 0.25f) return;

        int barWidth = 14;
        int barHeight = 120;
        int screenWidth = guiGraphics.guiWidth();
        // Place it next to the sanity bar (on the left of it)
        int barX = screenWidth - (barWidth * 2) - 12;
        int barY = 60;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x80000000);

        int progressHeight = (int) (barHeight * corruption);
        int progressStartY = barY + barHeight - progressHeight;
        if (progressHeight > 0) {
            // Dark purple/red colors for corruption
            drawVerticalGradient(guiGraphics, barX, progressStartY, barWidth, progressHeight,
                    0xFF800080, 0xFF4B0082);
        }

        ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/spirituality_bar_background.png");
        guiGraphics.blit(backgroundTexture, barX - 4, barY - 4, barWidth + 8, barHeight + 8, 0, 0, 44, 256, 44, 256);

        String percentText = String.format("%.3f%%", corruption * 100.0f);
        int textX = barX + (barWidth / 2) - (mc.font.width(percentText) / 2);
        int textY = barY + barHeight + 8;
        guiGraphics.drawString(mc.font, percentText, textX, textY, 0xFF800080, true);
    }

    private static void renderAccommodationColorOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (ClientAccommodationCache.getTotalTicks() <= 0) return;

        boolean keyOfLight = "wheel_of_fortune".equalsIgnoreCase(
            ClientBeyonderCache.getPathway(mc.player.getUUID()));
        int[] colors = keyOfLight
            ? new int[] {0xbad2f5, 0x9a63d8, 0xf0d778}
            : new int[] {0x864ec7, 0x0018b8, 0x89f5f5};

        // Full cycle = 9 seconds (3s per color transition)
        long cycleMs = 3000L;
        float t = (System.currentTimeMillis() % (cycleMs * 3)) / (float)(cycleMs * 3);
        float phase = t * 3.0f;
        int fromIdx = (int) phase % 3;
        int toIdx = (fromIdx + 1) % 3;
        float frac = phase - (int) phase;
        // Smooth-step the fraction
        frac = frac * frac * (3.0f - 2.0f * frac);

        int c = interpolateColor(0xFF000000 | colors[fromIdx], 0xFF000000 | colors[toIdx], frac);
        int r = (c >> 16) & 0xFF;
        int g = (c >> 8) & 0xFF;
        int b = c & 0xFF;

        int sw = guiGraphics.guiWidth();
        int sh = guiGraphics.guiHeight();
        int thickness = 90;

        // Vignette: quadratic falloff from screen edge inward
        for (int i = 0; i < thickness; i++) {
            float fade = 1.0f - (float) i / thickness;
            fade = fade * fade;
            int a = (int)(0xA0 * fade);
            if (a <= 0) continue;
            int col = (a << 24) | (r << 16) | (g << 8) | b;
            guiGraphics.fill(i, i, sw - i, i + 1, col);           // top edge
            guiGraphics.fill(i, sh - i - 1, sw - i, sh - i, col); // bottom edge
            guiGraphics.fill(i, i + 1, i + 1, sh - i - 1, col);   // left edge
            guiGraphics.fill(sw - i - 1, i + 1, sw - i, sh - i - 1, col); // right edge
        }
    }

    private static void renderAccommodationBar(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int totalTicks = ClientAccommodationCache.getTotalTicks();
        int progressTicks = ClientAccommodationCache.getProgressTicks();
        if (totalTicks <= 0) return;

        float progress = Math.min(1.0f, (float) progressTicks / totalTicks);

        int barWidth = 120;
        int barHeight = 6;
        int barX = guiGraphics.guiWidth() / 2 - barWidth / 2;
        int barY = 20;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x80000000);

        int fillWidth = (int) (barWidth * progress);
        if (fillWidth > 0) {
            guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, 0xFF2B0A45);
        }

        String percentText = "Alignment " + Math.round(progress * 100.0f) + "%";
        int textX = guiGraphics.guiWidth() / 2 - mc.font.width(percentText) / 2;
        int textY = barY - 10;
        guiGraphics.drawString(mc.font, percentText, textX, textY, 0xFFEDEDED, true);
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
package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class MarionetteOverlayRenderer {

    public static HashMap<UUID, MarionetteInfos> currentMarionette = new HashMap<>();
    private static final Map<UUID, MarionetteInfos> cachedMarionette = new HashMap<>();
    private static final Map<UUID, Long> nullSinceTime = new HashMap<>();
    private static final Map<UUID, Double> smoothedHealth = new HashMap<>();

    private static final int PANEL_BG_TOP = 0x99120018;
    private static final int PANEL_BG_BOTTOM = 0x99000000;
    private static final int BORDER_A = 0xFFa742f5;
    private static final int BORDER_B = 0xFFe43fa3;
    private static final int ACCENT = 0xFFffffff;
    private static final int BAR_BG = 0xAA1a0a1f;

    private static final ResourceLocation wormOfSpiritTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/misc/worm_of_spirit.png");
    private static final int wormIconSize = 16;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "marionette_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    @SubscribeEvent
    public static void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        currentMarionette.remove(uuid);
        cachedMarionette.remove(uuid);
        nullSinceTime.remove(uuid);
        smoothedHealth.remove(uuid);
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        UUID playerUUID = mc.player.getUUID();

        if (currentMarionette.containsKey(playerUUID)) {
            MarionetteInfos infos = currentMarionette.get(playerUUID);
            if (infos != null) {
                cachedMarionette.put(playerUUID, infos);
                nullSinceTime.remove(playerUUID);
            } else {
                nullSinceTime.putIfAbsent(playerUUID, System.currentTimeMillis());
            }
        } else if (cachedMarionette.containsKey(playerUUID)) {
            nullSinceTime.putIfAbsent(playerUUID, System.currentTimeMillis());
        }

        MarionetteInfos infos = currentMarionette.getOrDefault(playerUUID, null);
        if (infos == null) {
            long nullSince = nullSinceTime.getOrDefault(playerUUID, System.currentTimeMillis());
            if (System.currentTimeMillis() - nullSince < 500) {
                infos = cachedMarionette.get(playerUUID);
            }
        }

        if (infos == null) {
            smoothedHealth.remove(playerUUID);
            return;
        }

        int width = (screenWidth / 3);
        int height = 45;

        int barWidth = (int) (width / 1.3);
        int barHeight = 14;

        int x = screenWidth - barWidth - 40;
        int y = 15;

        guiGraphics.fill(x + 2, y + 3, x + width + 2, y + height + 3, 0x66000000);

        renderOutLine(guiGraphics, x, y, width, height);

        String label = Component.translatable("lotm.marionette").getString() + ":";
        int labelY = y + 5;
        int labelX = x + (width / 2);
        guiGraphics.drawCenteredString(mc.font, label, labelX, labelY, 0xFFe0c8ff);

        String name = infos.name();
        int nameY = y + 5 + mc.font.lineHeight;
        int nameX = x + (width / 2);
        guiGraphics.drawCenteredString(mc.font, name, nameX, nameY, 0xFFFFFFFF);

        if (infos.hasWorm()) {
            int nameWidth = mc.font.width(name);
            int wormX = nameX + (nameWidth / 2) + 6;
            int wormY = nameY + (mc.font.lineHeight / 2) - (wormIconSize / 2);
            guiGraphics.blit(wormOfSpiritTexture, wormX, wormY, 0, 0, wormIconSize, wormIconSize, wormIconSize, wormIconSize);
        }

        int barY = y + height - barHeight - 5;
        int barX = x + (width / 2) - (barWidth / 2);

        double current = smoothedHealth.getOrDefault(playerUUID, infos.health());
        double target = infos.health();
        double next = current + (target - current) * 0.18;
        if (Math.abs(next - target) < 0.05) next = target;
        smoothedHealth.put(playerUUID, next);

        drawHealthBar(guiGraphics, barX, barY, barWidth, barHeight, next, infos.maxHealth());

        String healthText = Math.round(infos.health()) + " ❤";
        guiGraphics.drawCenteredString(mc.font, healthText, barX + (barWidth / 2), barY + 1 + ((barHeight - mc.font.lineHeight) / 2), 0xFFFFFFFF);
    }

    private static void drawHealthBar(GuiGraphics guiGraphics, int barX, int barY, int barWidth, int barHeight,
                                      double health, double maxHealth) {
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, BAR_BG);

        double fillPercentage = maxHealth > 0 ? Math.max(0, Math.min(1, health / maxHealth)) : 0;
        int filledBarWidth = (int) (barWidth * fillPercentage);

        if (filledBarWidth > 0) {
            int[] colors = healthGradient(fillPercentage);
            drawHorizontalGradient(guiGraphics, barX, barY, filledBarWidth, barHeight, colors[0], colors[1]);

            guiGraphics.fill(barX, barY, barX + filledBarWidth, barY + Math.max(1, barHeight / 4), 0x33FFFFFF);

            if (fillPercentage < 0.25) {
                float pulse = (float) (0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 150.0));
                int flashAlpha = (int) (90 * pulse);
                guiGraphics.fill(barX, barY, barX + filledBarWidth, barY + barHeight, (flashAlpha << 24) | 0xFF3030);
            }
        }

        for (int i = 1; i < 4; i++) {
            int tickX = barX + (barWidth * i / 4);
            guiGraphics.fill(tickX, barY, tickX + 1, barY + barHeight, 0x55000000);
        }

        guiGraphics.fill(barX, barY, barX + barWidth, barY + 1, 0x66FFFFFF);
        guiGraphics.fill(barX, barY + barHeight - 1, barX + barWidth, barY + barHeight, 0x66000000);
    }

    private static int[] healthGradient(double fillPercentage) {
        if (fillPercentage > 0.6) {
            return new int[]{0xFF3ddc84, 0xFFa8f7c0};
        } else if (fillPercentage > 0.3) {
            return new int[]{0xFFf5a623, 0xFFffe066};
        } else {
            return new int[]{0xFFb3001b, 0xFFff5f5f};
        }
    }

    private static void renderOutLine(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        drawVerticalGradient(guiGraphics, x, y, width, height, PANEL_BG_TOP, PANEL_BG_BOTTOM);

        float t = (float) (0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 600.0));
        int glowColor = interpolateColor(BORDER_A, BORDER_B, t);

        guiGraphics.fill(x, y, x + width, y + 1, glowColor);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, glowColor);
        guiGraphics.fill(x, y + 1, x + 1, y + height - 1, glowColor);
        guiGraphics.fill(x + width - 1, y + 1, x + width, y + height - 1, glowColor);

        int cornerSize = 4;
        drawCorner(guiGraphics, x, y, cornerSize, true, true);
        drawCorner(guiGraphics, x + width, y, cornerSize, false, true);
        drawCorner(guiGraphics, x, y + height, cornerSize, true, false);
        drawCorner(guiGraphics, x + width, y + height, cornerSize, false, false);
    }

    private static void drawCorner(GuiGraphics guiGraphics, int cx, int cy, int size, boolean right, boolean down) {
        int x0 = right ? cx : cx - size;
        int x1 = right ? cx + size : cx;
        int y0 = down ? cy : cy - size;
        int y1 = down ? cy + size : cy;

        guiGraphics.fill(x0, right ? cy : cy - 1, x1, (right ? cy : cy - 1) + 1, ACCENT);
        guiGraphics.fill(down ? cx : cx - 1, y0, (down ? cx : cx - 1) + 1, y1, ACCENT);
    }

    private static final Attribute[] attributesThatShouldGetDisplayed = new Attribute[]{
            Attributes.MOVEMENT_SPEED.value(), Attributes.ATTACK_DAMAGE.value(), Attributes.JUMP_STRENGTH.value(), Attributes.ARMOR.value()
    };

    private static void drawHorizontalGradient(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                               int startColor, int endColor) {
        for (int i = 0; i < width; i++) {
            float ratio = (float) i / width;
            int color = interpolateColor(startColor, endColor, ratio);
            guiGraphics.fill(x + i, y, x + i + 1, y + height, color);
        }
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

    public record MarionetteInfos(String name, double health, double maxHealth, boolean hasWorm) {
    }

    public static void clearCache() {
        currentMarionette.clear();
        cachedMarionette.clear();
        nullSinceTime.clear();
        smoothedHealth.clear();
    }
}
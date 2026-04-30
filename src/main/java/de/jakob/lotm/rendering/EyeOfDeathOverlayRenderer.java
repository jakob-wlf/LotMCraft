package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class EyeOfDeathOverlayRenderer {

    public static final HashMap<UUID, LivingEntity> entitiesLookedAt = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "eye_of_death_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (!entitiesLookedAt.containsKey(mc.player.getUUID())) return;

        ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/eye_of_death_overlay.png");
        guiGraphics.pose().pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(backgroundTexture, 0, 0, screenWidth, screenHeight, 0, 0, 44, 256, 44, 256);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();

        LivingEntity entity = entitiesLookedAt.get(mc.player.getUUID());
        if (entity == null) return;

        int width = screenWidth / 3;
        int height = 35;
        int x = screenWidth / 2 - width / 2;
        int y = 15;

        guiGraphics.fill(x, y, x + width, y + height, 0x77000000);

        // Entity name
        String name = entity.getName().getString();
        guiGraphics.drawCenteredString(mc.font, name, x + width / 2, y + 5, 0xFF6897cc);

        // Health bar
        int barWidth = (int) (width / 1.3);
        int barHeight = 14;
        int barX = x + (width - barWidth) / 2;
        int barY = y + height - barHeight - 5;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x88000000);

        double fillPercentage = entity.getHealth() / entity.getMaxHealth();
        int filledBarWidth = (int) (barWidth * fillPercentage);

        if (filledBarWidth > 0)
            drawHorizontalGradient(guiGraphics, barX, barY, filledBarWidth, barHeight, 0xFF6897cc, 0xFF95bfed);

        // Health text
        guiGraphics.drawString(mc.font, (int) entity.getHealth() + " / " + (int) entity.getMaxHealth() + " ❤", barX + 3, barY + 1 + (barHeight - mc.font.lineHeight) / 2, 0xFFFFFFFF);
    }

    private static void drawHorizontalGradient(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                               int startColor, int endColor) {
        for (int i = 0; i < width; i++) {
            float ratio = (float) i / width;
            int color = interpolateColor(startColor, endColor, ratio);
            guiGraphics.fill(x + i, y, x + i + 1, y + height, color);
        }
    }

    private static int interpolateColor(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}

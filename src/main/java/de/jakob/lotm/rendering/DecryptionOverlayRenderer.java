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

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class DecryptionOverlayRenderer {

    public static HashMap<UUID, LivingEntity> entitiesLookedAtByPlayerWithActiveDecryption = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "decryption_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        if (entitiesLookedAtByPlayerWithActiveDecryption.containsKey(mc.player.getUUID())) {
            LivingEntity entity = entitiesLookedAtByPlayerWithActiveDecryption.get(mc.player.getUUID());
            if(entity != null) {
                int width =  (screenWidth / 3);
                int height = 35;

                int x = screenWidth / 2 - width / 2;
                int y = 15;

                renderOutLine(guiGraphics, x, y, width, height);

                //Entity name
                String name = entity.getName().getString();
                int nameX = x + (width / 2);
                int nameY = y + 5;
                guiGraphics.drawCenteredString(mc.font, name, nameX, nameY, 0xFFFFFFFF);

                //Health Bar
                int barWidth = (int) (width / 1.3);
                int barHeight = 14;

                int barX = x + ((width - barWidth) / 2);
                int barY = y + height - barHeight - 5;

                guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x88000000);

                double fillPercentage = entity.getHealth() / entity.getMaxHealth();
                int filledBarWidth = (int) (barWidth * fillPercentage);

                if(filledBarWidth > 0)
                    drawHorizontalGradient(guiGraphics, barX, barY, filledBarWidth, barHeight, 0xFFFF0000, 0xFFe43fa3);

                //Health String
                guiGraphics.drawString(mc.font, entity.getHealth() + " ❤", barX + 3, barY + 1 + ((barHeight - mc.font.lineHeight) / 2), 0xFFFFFFFF);
            }
        }
    }

    private static void renderOutLine(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0x77000000);
        guiGraphics.fill(x, y, x + width, y + 2, 0xFF5cff68);
        guiGraphics.fill(x, y + height - 2, x + width, y + height, 0xFF5cff68);
        guiGraphics.fill(x, y + 2, x + 2, y + height - 2, 0xFF5cff68);
        guiGraphics.fill(x + width - 2, y + 2, x + width, y + height - 2, 0xFF5cff68);
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
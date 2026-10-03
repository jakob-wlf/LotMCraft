package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class TelepathyOverlayRenderer {

    public static HashMap<UUID, List<String>> entitiesLookedAtByPlayerWithActiveTelepathy = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "telepathy_overlay"), (guiGraphics, deltaTracker) -> {
            renderOverlay(guiGraphics);
        });
    }

    private static void renderOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (entitiesLookedAtByPlayerWithActiveTelepathy.containsKey(mc.player.getUUID())) {
            List<String> goalNames = entitiesLookedAtByPlayerWithActiveTelepathy.get(mc.player.getUUID());
            if (goalNames != null && !goalNames.isEmpty()) {
                displayGoals(guiGraphics, goalNames, screenWidth, screenHeight);
            }
        }
    }

    private static void renderPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int accentColor) {
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xCC0a0a12);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + 10, 0x220d0d1a);

        guiGraphics.fill(x, y + 2, x + 2, y + height - 2, accentColor);
        guiGraphics.fill(x + width - 2, y + 2, x + width, y + height - 2, accentColor);
        guiGraphics.fill(x + 2, y, x + width - 2, y + 2, accentColor);
        guiGraphics.fill(x + 2, y + height - 2, x + width - 2, y + height, accentColor);

        guiGraphics.fill(x, y, x + 2, y + 2, 0x00000000);
        guiGraphics.fill(x + width - 2, y, x + width, y + 2, 0x00000000);
        guiGraphics.fill(x, y + height - 2, x + 2, y + height, 0x00000000);
        guiGraphics.fill(x + width - 2, y + height - 2, x + width, y + height, 0x00000000);

        guiGraphics.fill(x + 2, y + 2, x + 4, y + 4, accentColor);
        guiGraphics.fill(x + width - 4, y + 2, x + width - 2, y + 4, accentColor);
        guiGraphics.fill(x + 2, y + height - 4, x + 4, y + height - 2, accentColor);
        guiGraphics.fill(x + width - 4, y + height - 4, x + width - 2, y + height - 2, accentColor);
    }

    private static void displayGoals(GuiGraphics guiGraphics, List<String> goalNames, int screenWidth, int screenHeight) {
        Font font = Minecraft.getInstance().font;

        int maxTextWidth = goalNames.stream()
                .map(g -> font.width(g.replace("%", "")))
                .max(Comparator.naturalOrder())
                .orElse(0);

        if (maxTextWidth == 0) return;

        int padding = 14;
        int lineSpacing = font.lineHeight + 6;
        int panelWidth = maxTextWidth + padding * 2;
        int panelHeight = goalNames.size() * lineSpacing + padding * 2 - 6;

        int panelX = screenWidth - panelWidth - 10;
        int panelY = screenHeight - panelHeight - 10;

        renderPanel(guiGraphics, panelX, panelY, panelWidth, panelHeight, 0xFFf7cd83);

        int textX = panelX + padding;
        int textY = panelY + padding;

        for (String s : goalNames) {
            boolean highlight = s.contains("%");
            s = s.replace("%", "");

            if (highlight) {
                guiGraphics.fill(textX - 4, textY - 2, textX + maxTextWidth + 4, textY + font.lineHeight + 1, 0x22f7cd83);
            }

            guiGraphics.drawString(font, s, textX + 1, textY + 1, 0x55000000);
            guiGraphics.drawString(font, s, textX, textY, highlight ? 0xFFffc363 : 0xFFd4cfc8);

            textY += lineSpacing;
        }
    }

    public static void clearCache() {
        entitiesLookedAtByPlayerWithActiveTelepathy.clear();
    }
}
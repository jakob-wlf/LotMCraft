package de.jakob.lotm.overlay;

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
            if(goalNames != null && !goalNames.isEmpty()) {
                displayGoals(guiGraphics, goalNames, screenWidth, screenHeight);
            }
        }
    }

    private static void renderOutLine(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0x77000000);
        guiGraphics.fill(x, y, x + width, y + 2, 0xFFf7cd83);
        guiGraphics.fill(x, y + height - 2, x + width, y + height, 0xFFf7cd83);
        guiGraphics.fill(x, y + 2, x + 2, y + height - 2, 0xFFf7cd83);
        guiGraphics.fill(x + width - 2, y + 2, x + width, y + height - 2, 0xFFf7cd83);
    }

    private static void displayGoals(GuiGraphics guiGraphics, List<String> goalNames, int screenWidth, int screenHeight) {
        int startingX = screenWidth - 10 - goalNames.stream()
                .map(g -> Minecraft.getInstance().font.width(g.replace("%", "")))
                .max(Comparator.naturalOrder())
                .orElse(0);

        if (startingX == 0)
            return;

        Font font = Minecraft.getInstance().font;

        int bottomY = screenHeight - 10;
        int outlineX = startingX - 10;
        int startingY = bottomY - (goalNames.size() * 2 * font.lineHeight);
        int outlineY = startingY - 10;
        int outlineWidth = screenWidth - outlineX;
        int outlineHeight = bottomY - outlineY;

        guiGraphics.fill(startingX - 10, startingY - 10,
                screenWidth,
                bottomY,
                0x77000000);

        renderOutLine(guiGraphics, outlineX, outlineY, outlineWidth, outlineHeight);

        for (String s : goalNames) {
            int color = s.contains("%") ? 0xFFffc363 : 0xFFFFFFFF;
            s = s.replace("%", "");
            guiGraphics.drawString(font,
                    s,
                    startingX, startingY, color);
            startingY += font.lineHeight * 2;
        }
    }
}
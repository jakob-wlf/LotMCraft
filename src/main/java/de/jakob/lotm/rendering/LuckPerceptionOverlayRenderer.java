package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public final class LuckPerceptionOverlayRenderer {
    private static final int ACCENT = 0xFFF8D66D;
    private static final int ACCENT_LIGHT = 0xFF72E8D2;
    private static final int BACKGROUND = 0xCC0A1012;

    private static boolean active;
    private static int entityId = -1;
    private static int luck;

    private LuckPerceptionOverlayRenderer() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "luck_perception_overlay"),
                (guiGraphics, deltaTracker) -> render(guiGraphics));
    }

    private static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active || minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) return;

        Entity target = minecraft.level.getEntity(entityId);
        if (target == null) return;

        int screenWidth = guiGraphics.guiWidth();
        int spiritVisionWidth = screenWidth / 3;
        int x = screenWidth / 2 + spiritVisionWidth / 2 + 6;
        int y = 12;
        int width = Math.min(110, screenWidth - x - 6);
        int height = 42;
        if (width < 54) return;

        renderPanel(guiGraphics, x, y, width, height);

        String name = minecraft.font.plainSubstrByWidth(target.getName().getString(), width - 10);
        guiGraphics.drawCenteredString(minecraft.font, name, x + width / 2, y + 7, ACCENT_LIGHT);

        String luckText = "Luck: " + luck;
        int textY = y + height - minecraft.font.lineHeight - 7;
        guiGraphics.drawCenteredString(minecraft.font, luckText, x + width / 2, textY, getLuckColor(luck));
    }

    private static void renderPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, BACKGROUND);
        guiGraphics.fill(x, y + 2, x + 2, y + height - 2, ACCENT);
        guiGraphics.fill(x + width - 2, y + 2, x + width, y + height - 2, ACCENT);
        guiGraphics.fill(x + 2, y, x + width - 2, y + 2, ACCENT);
        guiGraphics.fill(x + 2, y + height - 2, x + width - 2, y + height, ACCENT);
    }

    private static int getLuckColor(int luck) {
        if (luck > 0) return 0xFF72E8D2;
        if (luck < 0) return 0xFFE16D6D;
        return 0xFFD0D0D0;
    }

    public static void update(boolean active, int entityId, int luck) {
        LuckPerceptionOverlayRenderer.active = active;
        LuckPerceptionOverlayRenderer.entityId = active ? entityId : -1;
        LuckPerceptionOverlayRenderer.luck = luck;
    }

    public static void clearCache() {
        update(false, -1, 0);
    }
}
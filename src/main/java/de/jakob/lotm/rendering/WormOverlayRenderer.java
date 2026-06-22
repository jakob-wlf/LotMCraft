package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Arrays;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class WormOverlayRenderer {
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "coward_worms_overlay"), (guiGraphics, deltaTracker) -> {
            renderText(guiGraphics);
        });
    }

    private static final ResourceLocation iconTextureFool = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/misc/worm_of_spirit.png");
    private static final ResourceLocation iconTextureDoor = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/misc/worm_of_star.png");
    private static final ResourceLocation iconTextureError = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/misc/worm_of_time.png");
    private static final int size = 24;
    private final static int hotbarWidth = 182;
    private final static int hotbarheight = 22;

    private static final String[] cowardly_pathways = new String[]{"fool", "door", "error"};

    private static void renderText(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if(!ClientBeyonderCache.isBeyonder(mc.player.getUUID())) return;
        if(!Arrays.asList(cowardly_pathways).contains(ClientBeyonderCache.getPathway(mc.player.getUUID()))) return;
        if(ClientBeyonderCache.getSequence(mc.player.getUUID()) > 4) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int hotbarEndX = ((screenWidth + hotbarWidth) / 2);

        int x = hotbarEndX - size;
        int y = mc.getWindow().getGuiScaledHeight() - (hotbarheight) - size - 15;

        ResourceLocation iconTexture = switch (ClientBeyonderCache.getPathway(mc.player.getUUID())) {
            case "fool" -> iconTextureFool;
            case "door" -> iconTextureDoor;
            case "error" -> iconTextureError;
            default -> iconTextureFool;
        };

        guiGraphics.blit(iconTexture, x, y, 0, 0, size, size, size, size);
        guiGraphics.drawString(mc.font, ClientBeyonderCache.getCowardWormAmount(mc.player.getUUID()) + "", x + size, y + (size / 2) - (mc.font.lineHeight / 2), 0xffffff);
    }
}

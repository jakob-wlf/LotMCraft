package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityHotbarManager;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class AbilityHotbarRenderer {

    private static final ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/ability_hotbar_overlay.png");
    private static final int iconWidth = 190;
    private static final int iconHeight = 26;


    @SubscribeEvent
    public static void onRenderHotbar(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;

        AbilityHotbarManager manager = player.getData(ModAttachments.ABILITY_HOTBAR);

        if (!manager.isAbilityHotbarActive()) return;

        GuiGraphics graphics = event.getGuiGraphics();

        // Render indicator showing which ability hotbar is active
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        String text = "Ability Hotbar " + (manager.getCurrentHotbarIndex() + 1);
        int textWidth = mc.font.width(text);

        // Position above the hotbar (higher up to avoid hearts)
        int x = screenWidth / 2 - textWidth / 2;
        int y = screenHeight - 65;

        // Draw background
        graphics.fill(x - 2, y - 2, x + textWidth + 2, y + mc.font.lineHeight + 2, 0x80000000);

        // Draw text
        graphics.drawString(mc.font, text, x, y, 0xFFFFFF);


        int hotbarX = screenWidth / 2 - iconWidth / 2;
        int hotbarY = screenHeight - iconHeight;

        graphics.blit(iconTexture, hotbarX, hotbarY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
    }
}
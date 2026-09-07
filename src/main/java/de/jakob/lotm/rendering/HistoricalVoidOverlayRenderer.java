package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.fool.HistoricalVoidSummoningAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Arrays;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class HistoricalVoidOverlayRenderer {
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "historical_void_overlay"), (guiGraphics, deltaTracker) -> {
            renderText(guiGraphics);
        });
    }

    private final static int hotbarWidth = 182;
    private final static int hotbarheight = 22;

    private static void renderText(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (!ClientBeyonderCache.isBeyonder(mc.player.getUUID())) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int hotbarEndX = ((screenWidth + hotbarWidth) / 2);

        int max = HistoricalVoidSummoningAbility.getMaxSummonedForSequence(BeyonderData.getSequence(mc.player));
        int amount = Math.max(0, mc.player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).summonedCount);

        if (amount == 0) return;

        int x = hotbarEndX - 24;
        int y = mc.getWindow().getGuiScaledHeight() - (hotbarheight) - 60;
        guiGraphics.drawString(mc.font, Component.literal(amount + "/" + max).withStyle(ChatFormatting.BOLD), x, y, 0xFFFFFF, true);
    }
}
